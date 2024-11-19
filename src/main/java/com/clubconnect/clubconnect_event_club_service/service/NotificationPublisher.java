package com.clubconnect.clubconnect_event_club_service.service;

import java.util.List;
import java.util.Map;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

import com.clubconnect.clubconnect_event_club_service.config.RabbitMQConfig;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

@Service
public class NotificationPublisher {

    private final RabbitTemplate rabbitTemplate;
    private final ObjectMapper objectMapper; // Jackson's ObjectMapper for JSON processing

    public NotificationPublisher(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
        this.objectMapper = new ObjectMapper(); // Initialize ObjectMapper
    }

    public void publishEventNotification(String eventId, String clubId, String[] tags) {
        List<String> tagList = List.of(tags); // Convert the tags array to a List<String>

        // Construct the event message as a Map
        Map<String, Object> eventMessage = Map.of(
            "eventId", eventId,
            "clubId", clubId,
            "tags", tagList
        );

        try {
            // Serialize the event message map to a JSON string
            String jsonMessage = objectMapper.writeValueAsString(eventMessage);

            // Publish the JSON string message to RabbitMQ
            rabbitTemplate.convertAndSend(
                RabbitMQConfig.EXCHANGE_NAME,
                RabbitMQConfig.ROUTING_KEY,
                jsonMessage
            );

            System.out.println("Published event message: " + jsonMessage);
        } catch (JsonProcessingException e) {
            // Log or handle the exception if serialization fails
            System.err.println("Error serializing event message: " + e.getMessage());
        }
    }
}
