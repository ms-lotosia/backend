package com.lotosia.paymentservice.service;
import com.lotosia.paymentservice.dto.event.PaymentProcessedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;

@Service
@RequiredArgsConstructor
@Slf4j
public class KafkaProducerService {
    private final KafkaTemplate<String,Object> kafkaTemplate;
    private static final String PAYMENT_PROCESSED_TOPIC = "payment-processed";

    public void sendPaymentProcessedEvent(PaymentProcessedEvent paymentProcessedEvent) {
        try{
            CompletableFuture<SendResult<String, Object>> future =
                    kafkaTemplate.send(PAYMENT_PROCESSED_TOPIC, paymentProcessedEvent.getOrderId().toString(), paymentProcessedEvent);
            future.whenComplete((result,exception)->{
                if(exception==null){
                    log.info("Payment event sent successfully: orderId={}, paymentId={}, status={}");
                }else{
                    log.error("Failed to send payment: orderId={}", paymentProcessedEvent.getOrderId(), exception.getMessage());
                }
            });

        }catch (Exception e){
            log.error("Error sending payment event: orderId={}", paymentProcessedEvent.getOrderId(), e);
        }
    }
}
