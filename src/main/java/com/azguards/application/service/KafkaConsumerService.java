package com.azguards.application.service;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
public class KafkaConsumerService {

    @KafkaListener(topics = "test-topic" , groupId = "test-group")
    public void listen(String message)
    {
        System.out.println("Received Message : "+ message);
    }
}