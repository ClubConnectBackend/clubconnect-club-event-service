package com.clubconnect.clubconnect_event_club_service.repository;

import java.util.HashMap;
import java.util.List;
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
public class ClubRepository {

    private final DynamoDbClient dynamoDbClient;
    private final String tableName = "Clubs";

    public ClubRepository(DynamoDbClient dynamoDbClient) {
        this.dynamoDbClient = dynamoDbClient;
    }

    /**
     * Save or update a club in DynamoDB
     */
    public void saveClub(Integer clubId, String name, String description, Set<Integer> eventIds, String imageUrl) {
        Map<String, AttributeValue> item = new HashMap<>();
        item.put("clubId", AttributeValue.builder().n(String.valueOf(clubId)).build());
        item.put("name", AttributeValue.builder().s(name).build());
        item.put("description", AttributeValue.builder().s(description).build());
        item.put("imageUrl", AttributeValue.builder().s(imageUrl != null ? imageUrl : "").build());

        if (eventIds != null && !eventIds.isEmpty()) {
            item.put("eventIds", AttributeValue.builder().ns(
                eventIds.stream().map(String::valueOf).collect(Collectors.toSet())
            ).build());
        }

        PutItemRequest request = PutItemRequest.builder()
                .tableName(tableName)
                .item(item)
                .build();

        dynamoDbClient.putItem(request);
    }

    /**
     * Find a club by club ID
     */
    public Map<String, AttributeValue> findClubById(Integer clubId) {
        GetItemRequest request = GetItemRequest.builder()
                .tableName(tableName)
                .key(Map.of("clubId", AttributeValue.builder().n(String.valueOf(clubId)).build()))
                .build();

        GetItemResponse response = dynamoDbClient.getItem(request);

        if (!response.hasItem()) {
            System.out.println("No club found for ID: " + clubId);
            return Map.of(); // Return an empty map instead of null
        }

        return response.item();
    }

    /**
     * Update event IDs for a club
     */
    public void updateEventIds(Integer clubId, Set<Integer> eventIds) {
        Map<String, AttributeValue> club = findClubById(clubId);
        if (club != null) {
            Set<Integer> existingEventIds = club.containsKey("eventIds") ?
                    club.get("eventIds").ns().stream().map(Integer::valueOf).collect(Collectors.toSet()) : Set.of();

            if (!existingEventIds.equals(eventIds)) {
                saveClub(
                    clubId,
                    club.get("name").s(),
                    club.get("description").s(),
                    eventIds,
                    club.containsKey("imageUrl") ? club.get("imageUrl").s() : ""
                );
            }
        }
    }

    /**
     * Delete a club by club ID
     */
    public void deleteClub(Integer clubId) {
        Map<String, AttributeValue> key = Map.of("clubId", AttributeValue.builder().n(String.valueOf(clubId)).build());

        DeleteItemRequest request = DeleteItemRequest.builder()
                .tableName(tableName)
                .key(key)
                .build();

        dynamoDbClient.deleteItem(request);
    }

    /**
     * Find all clubs
     *
     * @return a list of all clubs as maps
     */
    public List<Map<String, AttributeValue>> findAllClubs() {
        ScanRequest scanRequest = ScanRequest.builder()
                .tableName(tableName)
                .build();

        ScanResponse scanResponse = dynamoDbClient.scan(scanRequest);
        return scanResponse.items();
    }
}
