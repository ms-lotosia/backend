package com.lotosia.shoppingservice.service;

import com.lotosia.shoppingservice.dto.event.PaymentProcessedEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class KafkaConsumerService {
    private final OrderService orderService;

    @KafkaListener(topics = "payment-processed", groupId = "shopping-service-group")
    public void consumePaymentProcessedEvent(PaymentProcessedEvent paymentProcessedEvent) {
                paymentProcessedEvent.getOrderId(), paymentProcessedEvent.getPaymentId(), paymentProcessedEvent.getStatus());

        try {
            orderService.handlePaymentProcessed(paymentProcessedEvent.getOrderId(), paymentProcessedEvent.getStatus());
        }catch(Exception e){
        }
    }
}
