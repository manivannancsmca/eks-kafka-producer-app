package com.eks_kafka_producer_app.controller;


import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.web.bind.annotation.*;

import com.eks_kafka_producer_app.model.OrderEvent;

@RestController
@RequestMapping("/api/orders")
public class OrderProducerController {

    private final KafkaTemplate<String, OrderEvent> kafkaTemplate;
    private static final String TOPIC = "orders-topic";

    public OrderProducerController(KafkaTemplate<String, OrderEvent> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    @PostMapping
    public String publishOrder(@RequestBody OrderEvent order) {
        kafkaTemplate.send(TOPIC, order.orderId(), order);
        return "Order Event Published Successfully!";
    }

    @GetMapping
    public String helloMessage() {
        return "this is your kafka producer app";
    }
}
