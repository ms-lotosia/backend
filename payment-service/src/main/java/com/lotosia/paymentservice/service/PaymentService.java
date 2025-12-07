package com.lotosia.paymentservice.service;

import com.lotosia.paymentservice.dto.PaymentRequest;
import com.lotosia.paymentservice.dto.PaymentResponse;
import com.lotosia.paymentservice.dto.event.PaymentProcessedEvent;
import com.lotosia.paymentservice.entity.Card;
import com.lotosia.paymentservice.entity.Payment;
import com.lotosia.paymentservice.enums.PaymentMethod;
import com.lotosia.paymentservice.enums.PaymentStatus;
import com.lotosia.paymentservice.exception.ResourceNotFoundException;
import com.lotosia.paymentservice.repository.CardRepository;
import com.lotosia.paymentservice.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final CardRepository cardRepository;
    private final KafkaProducerService kafkaProducerService;


    @Transactional
    public PaymentResponse processPayment(Long userId, PaymentRequest request) {
        Payment payment = new Payment();
        payment.setUserId(userId);
        payment.setOrderId(request.getOrderId());
        payment.setAmount(request.getAmount());
        payment.setPaymentMethod(request.getPaymentMethod());
        payment.setStatus(PaymentStatus.PENDING);
        payment.setDescription(request.getDescription());
        payment.setTransactionId(generateTransactionId());

        if (request.getPaymentMethod() == PaymentMethod.CARD) {
            if (request.getCardId() == null) {
                throw new IllegalArgumentException("Card ID is required for card payments");
            }

            Card card = cardRepository.findByUserIdAndIdAndIsActiveTrue(userId, request.getCardId())
                    .orElseThrow(() -> new ResourceNotFoundException("Card not found with id: " + request.getCardId()));

            payment.setCard(card);
        }

        try {
            boolean paymentSuccess = processPaymentWithGateway(payment);

            if (paymentSuccess) {
                payment.setStatus(PaymentStatus.COMPLETED);
            } else {
                payment.setStatus(PaymentStatus.FAILED);
            }
        } catch (Exception e) {
            payment.setStatus(PaymentStatus.FAILED);
        }

        Payment saved = paymentRepository.save(payment);
        PaymentProcessedEvent paymentProcessedEvent = new PaymentProcessedEvent(
                saved.getId(),
                saved.getUserId(),
                saved.getOrderId(),
                saved.getAmount(),
                saved.getPaymentMethod() != null ? saved.getPaymentMethod().name() : null,
                saved.getStatus() != null ? saved.getStatus().name() : null,
                saved.getTransactionId(),
                LocalDateTime.now()
                );

        kafkaProducerService.sendPaymentProcessedEvent(paymentProcessedEvent);
        return mapToDto(saved);
    }

    public PaymentResponse getPayment(Long userId, Long paymentId) {
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new ResourceNotFoundException("Payment not found with id: " + paymentId));

        if (!payment.getUserId().equals(userId)) {
            throw new ResourceNotFoundException("Payment not found");
        }

        return mapToDto(payment);
    }

    public Page<PaymentResponse> getUserPayments(Long userId, Pageable pageable) {
        Page<Payment> payments = paymentRepository.findByUserId(userId, pageable);
        List<PaymentResponse> dtos = payments.getContent().stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
        return new PageImpl<>(dtos, pageable, payments.getTotalElements());
    }

    public List<PaymentResponse> getOrderPayments(Long orderId) {
        List<Payment> payments = paymentRepository.findByOrderId(orderId);
        return payments.stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public PaymentResponse cancelPayment(Long userId, Long paymentId) {
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new ResourceNotFoundException("Payment not found with id: " + paymentId));

        if (!payment.getUserId().equals(userId)) {
            throw new ResourceNotFoundException("Payment not found");
        }

        if (payment.getStatus() != PaymentStatus.PENDING) {
            throw new IllegalArgumentException("Only pending payments can be cancelled");
        }

        payment.setStatus(PaymentStatus.CANCELLED);
        Payment saved = paymentRepository.save(payment);
        return mapToDto(saved);
    }

    private boolean processPaymentWithGateway(Payment payment) {
        return true;
    }

    private String generateTransactionId() {
        return "TXN-" + UUID.randomUUID().toString().replace("-", "").substring(0, 16).toUpperCase();
    }

    private PaymentResponse mapToDto(Payment payment) {
        PaymentResponse response = new PaymentResponse();
        response.setId(payment.getId());
        response.setUserId(payment.getUserId());
        response.setOrderId(payment.getOrderId());
        response.setAmount(payment.getAmount());
        response.setPaymentMethod(payment.getPaymentMethod());
        response.setStatus(payment.getStatus());
        response.setTransactionId(payment.getTransactionId());
        response.setDescription(payment.getDescription());
        response.setCreatedDate(payment.getCreatedDate());
        response.setLastModifiedDate(payment.getLastModifiedDate());

        if (payment.getCard() != null) {
            response.setMaskedCardNumber(maskCardNumber(payment.getCard().getCardNumber()));
        }

        return response;
    }

    private String maskCardNumber(String cardNumber) {
        if (cardNumber == null || cardNumber.length() < 4) {
            return "****";
        }
        return "**** **** **** " + cardNumber.substring(cardNumber.length() - 4);
    }
}


