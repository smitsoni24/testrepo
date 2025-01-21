package com.azguards.application.service;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import com.azguards.application.controller.Entity.TicketMessage;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

@Service
public class KafkaProducerService {


	   private static final String TOPIC = "ticker-topic";


	    @Autowired
	    private KafkaTemplate<String, String> kafkaTemplate;

	    private final ObjectMapper objectMapper = new ObjectMapper();

	    public void sendTicketMessage(TicketMessage ticketMessage) {
	        try {
	            String jsonMessage = objectMapper.writeValueAsString(ticketMessage);
	            kafkaTemplate.send(TOPIC, ticketMessage.getId(), jsonMessage);
	            System.out.println("Sent Message: " + jsonMessage);
	        } catch (JsonProcessingException e) {
	            e.printStackTrace();
	        }
	    }
}