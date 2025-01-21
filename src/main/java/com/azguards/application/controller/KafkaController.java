package com.azguards.application.controller;

import com.azguards.application.controller.Entity.TicketMessage;
import com.azguards.application.service.KafkaProducerService;

import java.util.Random;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class KafkaController {

	   @Autowired
	    private KafkaProducerService kafkaProducerService;

	    private static final String[] NAMES = {"John", "Jane", "Alice", "Bob", "Charlie", "Diana", "Eve"};

	    @GetMapping("/publish")
	    public String publishMessages() {
	        for (int i = 1; i <= 100; i++) {
	            String uniqueId = UUID.randomUUID().toString();
	            String randomName = NAMES[new Random().nextInt(NAMES.length)];
	            String randomEmail = randomName.toLowerCase() + uniqueId.substring(0, 5) + "@example.com";
	            String content = "This is message number " + i;
	            TicketMessage message = new TicketMessage(uniqueId, randomName, randomEmail, content);
	            kafkaProducerService.sendTicketMessage(message);
	        }
	        return "100 messages published successfully!";
	    }
}