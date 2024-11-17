package com.clubconnect.clubconnect_event_club_service.model;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import software.amazon.awssdk.services.dynamodb.model.AttributeValue;

/**
 * Represents an Event entity to be stored in DynamoDB.
 */
public class Event {

    private Integer eventId; // Unique ID for the event
    private String name;   // Event name
    private String description; // Description of the event
    private Integer clubId; // ID of the club organizing the event
    private Set<String> tags; // Tags related to the event (e.g., AI, Coding)
    private Set<Integer> attendeeIds; // Set of user IDs attending the event

    // Default constructor
    public Event() {}

    // Parameterized constructor
    public Event(Integer eventId, String name, String description, Integer clubId, Set<String> tags, Set<Integer> attendeeIds) {
        this.eventId = eventId;
        this.name = name;
        this.description = description;
        this.clubId = clubId;
        this.tags = tags;
        this.attendeeIds = attendeeIds;
    }

    // Getters and setters
    public Integer getEventId() {
        return eventId;
    }

    public void setEventId(Integer eventId) {
        this.eventId = eventId;
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

    public Integer getClubId() {
        return clubId;
    }

    public void setClubId(Integer clubId) {
        this.clubId = clubId;
    }

    public Set<String> getTags() {
        return tags;
    }

    public void setTags(Set<String> tags) {
        this.tags = tags;
    }

    public Set<Integer> getAttendeeIds() {
        return attendeeIds;
    }

    public void setAttendeeIds(Set<Integer> attendeeIds) {
        this.attendeeIds = attendeeIds;
    }

    /**
     * Converts this Event object to a DynamoDB attribute map.
     *
     * @return a map of AttributeValue for DynamoDB storage
     */
    public Map<String, AttributeValue> toDynamoDbMap() {
        return Map.of(
            "eventId", AttributeValue.builder().n(String.valueOf(this.eventId)).build(),
            "name", AttributeValue.builder().s(this.name).build(),
            "description", AttributeValue.builder().s(this.description).build(),
            "clubId", AttributeValue.builder().n(String.valueOf(this.clubId)).build(),
            "tags", AttributeValue.builder().ss(this.tags).build(),
            "attendeeIds", AttributeValue.builder().ns(
                this.attendeeIds.stream().map(String::valueOf).collect(Collectors.toSet())
            ).build()
        );
    }

    /**
    * Creates an Event object from a DynamoDB attribute map.
    *
    * @param item the DynamoDB item map
    * @return an Event object
    */
    public static Event fromDynamoDbMap(Map<String, AttributeValue> item) {
        // Extract eventId
        Integer eventId = Integer.valueOf(item.get("eventId").n());

        // Extract name
        String name = item.get("name").s();

        // Extract description
        String description = item.get("description").s();

        // Extract clubId
        Integer clubId = Integer.valueOf(item.get("clubId").n());

        // Extract tags
        Set<String> tags = item.containsKey("tags") 
                ? new HashSet<>(item.get("tags").ss()) // Convert List<String> to Set<String>
                : Set.of();

        // Extract attendeeIds
        Set<Integer> attendeeIds = item.containsKey("attendeeIds")
                ? item.get("attendeeIds").ns().stream().map(Integer::valueOf).collect(Collectors.toSet())
                : Set.of();

        // Return a new Event object
        return new Event(eventId, name, description, clubId, tags, attendeeIds);
    }


    @Override
    public String toString() {
        return "Event{" +
                "eventId=" + eventId +
                ", name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", clubId=" + clubId +
                ", tags=" + tags +
                ", attendeeIds=" + attendeeIds +
                '}';
    }
}
