package com.clubconnect.clubconnect_event_club_service.model;

import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import software.amazon.awssdk.services.dynamodb.model.AttributeValue;

/**
 * Represents a Club entity to be stored in DynamoDB.
 */
public class Club {

    private Integer clubId; // Unique ID for the club
    private String name;   // Club name
    private String description; // Description of the club
    private Set<Integer> eventIds; // Set of event IDs organized by the club
    private String imageUrl; // URL of the club's image stored in S3

    // Default constructor
    public Club() {}

    // Parameterized constructor
    public Club(Integer clubId, String name, String description, Set<Integer> eventIds, String imageUrl) {
        this.clubId = clubId;
        this.name = name;
        this.description = description;
        this.eventIds = eventIds;
        this.imageUrl = imageUrl;
    }

    // Getters and setters
    public Integer getClubId() {
        return clubId;
    }

    public void setClubId(Integer clubId) {
        this.clubId = clubId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Set<Integer> getEventIds() {
        return eventIds;
    }

    public void setEventIds(Set<Integer> eventIds) {
        this.eventIds = eventIds;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    /**
     * Converts this Club object to a DynamoDB attribute map.
     *
     * @return a map of AttributeValue for DynamoDB storage
     */
    public Map<String, AttributeValue> toDynamoDbMap() {
        return Map.of(
            "clubId", AttributeValue.builder().n(String.valueOf(this.clubId)).build(),
            "name", AttributeValue.builder().s(this.name).build(),
            "description", AttributeValue.builder().s(this.description).build(),
            "eventIds", AttributeValue.builder().ns(
                this.eventIds.stream().map(String::valueOf).collect(Collectors.toSet())
            ).build(),
            "imageUrl", AttributeValue.builder().s(this.imageUrl).build()
        );
    }

    /**
     * Creates a Club object from a DynamoDB attribute map.
     *
     * @param item the DynamoDB item map
     * @return a Club object
     */
    public static Club fromDynamoDbMap(Map<String, AttributeValue> item) {
        if (item == null || item.isEmpty()) {
            throw new IllegalArgumentException("Invalid input: Map is null or empty.");
        }
    
        Integer clubId = Integer.valueOf(item.get("clubId").n());
        String name = item.get("name").s();
        String description = item.get("description").s();
        Set<Integer> eventIds = item.containsKey("eventIds")
                ? item.get("eventIds").ns().stream().map(Integer::valueOf).collect(Collectors.toSet())
                : Set.of();
        String imageUrl = item.containsKey("imageUrl") ? item.get("imageUrl").s() : "";

        return new Club(clubId, name, description, eventIds, imageUrl);
    }

    @Override
    public String toString() {
        return "Club{" +
                "clubId=" + clubId +
                ", name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", eventIds=" + eventIds +
                ", imageUrl='" + imageUrl + '\'' +
                '}';
    }
}
