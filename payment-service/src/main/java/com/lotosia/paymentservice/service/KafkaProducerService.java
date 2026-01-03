package com.lotosia.paymentservice.service;
import com.lotosia.paymentservice.dto.event.PaymentProcessedEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;

@Service
@RequiredArgsConstructor
public class KafkaProducerService {
    private final KafkaTemplate<String,Object> kafkaTemplate;
    private static final String PAYMENT_PROCESSED_TOPIC = "payment-processed";

    public void sendPaymentProcessedEvent(PaymentProcessedEvent paymentProcessedEvent) {
        try{
            CompletableFuture<SendResult<String, Object>> future =
                    kafkaTemplate.send(PAYMENT_PROCESSED_TOPIC, paymentProcessedEvent.getOrderId().toString(), paymentProcessedEvent);
            future.whenComplete((result,exception)->{
            });

        }catch (Exception e){
        }
    }
}
