package com.clubconnect.clubconnect_event_club_service.service;

import java.util.List;
import java.util.Map;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

import com.clubconnect.clubconnect_event_club_service.config.RabbitMQConfig;

@Service
public class NotificationPublisher {

    private final RabbitTemplate rabbitTemplate;

    public NotificationPublisher(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    public void publishEventNotification(String eventId, String clubId, String[] tags) {
        // Convert the tags array to a List
        var eventMessage = Map.of(
            "eventId", eventId,
            "clubId", clubId,
            "tags", List.of(tags) // Convert array to List
        );
    
        // Publish the message to RabbitMQ
        rabbitTemplate.convertAndSend(
            RabbitMQConfig.EXCHANGE_NAME,
            RabbitMQConfig.ROUTING_KEY,
            eventMessage
        );
    
        System.out.println("Published event message: " + eventMessage);
    }
    
}
