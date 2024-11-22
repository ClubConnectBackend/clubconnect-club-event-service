package com.clubconnect.clubconnect_event_club_service.repository;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Repository;

import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.dynamodb.model.DeleteItemRequest;
import software.amazon.awssdk.services.dynamodb.model.GetItemRequest;
import software.amazon.awssdk.services.dynamodb.model.GetItemResponse;
import software.amazon.awssdk.services.dynamodb.model.PutItemRequest;
import software.amazon.awssdk.services.dynamodb.model.ScanRequest;
import software.amazon.awssdk.services.dynamodb.model.ScanResponse;

@Repository
public class EventRepository {

    private final DynamoDbClient dynamoDbClient;
    private final String tableName = "Events";

    public EventRepository(DynamoDbClient dynamoDbClient) {
        this.dynamoDbClient = dynamoDbClient;
    }

    /**
     * Save an event to DynamoDB
     */
    public void saveEvent(Integer eventId, String name, String description, Integer clubId, Set<String> tags, Set<Integer> attendeeIds, String imageUrl) {
        Map<String, AttributeValue> item = new HashMap<>();
        item.put("eventId", AttributeValue.builder().n(String.valueOf(eventId)).build());
        item.put("name", AttributeValue.builder().s(name).build());
        item.put("description", AttributeValue.builder().s(description).build());
        item.put("clubId", AttributeValue.builder().n(String.valueOf(clubId)).build());
        item.put("imageUrl", AttributeValue.builder().s(imageUrl != null ? imageUrl : "").build());

        if (tags != null && !tags.isEmpty()) {
            item.put("tags", AttributeValue.builder().ss(tags).build());
        }

        if (attendeeIds != null && !attendeeIds.isEmpty()) {
            item.put("attendeeIds", AttributeValue.builder().ns(
                attendeeIds.stream().map(String::valueOf).collect(Collectors.toSet())
            ).build());
        }

        PutItemRequest request = PutItemRequest.builder()
                .tableName(tableName)
                .item(item)
                .build();

        dynamoDbClient.putItem(request);
    }

    /**
     * Find an event by event ID
     */
    public Map<String, AttributeValue> findEventById(Integer eventId) {
        GetItemRequest request = GetItemRequest.builder()
                .tableName(tableName)
                .key(Map.of("eventId", AttributeValue.builder().n(String.valueOf(eventId)).build()))
                .build();

        GetItemResponse response = dynamoDbClient.getItem(request);

        // Check if the item exists in the response
        return response.hasItem() ? response.item() : Map.of();
    }

    /**
     * Update attendee IDs for an event
     */
    public void updateAttendeeIds(Integer eventId, Set<Integer> attendeeIds) {
        Map<String, AttributeValue> eventMap = findEventById(eventId);
        if (eventMap != null && !eventMap.isEmpty()) {
            saveEvent(
                eventId,
                eventMap.get("name").s(),
                eventMap.get("description").s(),
                Integer.valueOf(eventMap.get("clubId").n()),
                eventMap.containsKey("tags") && eventMap.get("tags").hasSs()
                    ? new java.util.HashSet<>(eventMap.get("tags").ss()) // Convert List<String> to Set<String>
                    : Set.of(),
                attendeeIds,
                eventMap.containsKey("imageUrl") ? eventMap.get("imageUrl").s() : ""
            );
        }
    }

    /**
     * Delete an event by its ID
     *
     * @param eventId the ID of the event to delete
     */
    public void deleteEvent(Integer eventId) {
        Map<String, AttributeValue> key = Map.of("eventId", AttributeValue.builder().n(String.valueOf(eventId)).build());

        DeleteItemRequest request = DeleteItemRequest.builder()
                .tableName(tableName)
                .key(key)
                .build();

        dynamoDbClient.deleteItem(request);
    }

    /**
     * Retrieve all events
     *
     * @return a set of all Event objects
     */
    public Map<Integer, Map<String, AttributeValue>> findAllEvents() {
        ScanRequest scanRequest = ScanRequest.builder()
                .tableName(tableName)
                .build();

        ScanResponse scanResponse = dynamoDbClient.scan(scanRequest);

        return scanResponse.items().stream()
                .collect(Collectors.toMap(
                    item -> Integer.valueOf(item.get("eventId").n()),
                    item -> {
                        // Ensure imageUrl defaults to an empty string if missing
                        if (!item.containsKey("imageUrl")) {
                            item.put("imageUrl", AttributeValue.builder().s("").build());
                        }
                        return item;
                    }
                ));
    }
}
