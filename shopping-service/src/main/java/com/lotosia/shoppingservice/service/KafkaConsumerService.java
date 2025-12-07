package com.lotosia.shoppingservice.service;

import com.lotosia.shoppingservice.dto.event.PaymentProcessedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class KafkaConsumerService {
    private final OrderService orderService;

    @KafkaListener(topics = "payment-processed", groupId = "shopping-service-group")
    public void consumePaymentProcessedEvent(PaymentProcessedEvent paymentProcessedEvent) {
        log.info("Received payment event: orderId={}, paymentId={}, status={}",
                paymentProcessedEvent.getOrderId(), paymentProcessedEvent.getPaymentId(), paymentProcessedEvent.getStatus());

        try {
            orderService.handlePaymentProcessed(paymentProcessedEvent.getOrderId(), paymentProcessedEvent.getStatus());
        }catch(Exception e){
            log.error("Error processing payment event for orderId={}", paymentProcessedEvent.getOrderId(), e);
        }
    }
}
