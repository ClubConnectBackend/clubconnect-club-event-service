package com.clubconnect.clubconnect_event_club_service.model;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Map;
import java.util.Set;

import org.junit.jupiter.api.Test;

import software.amazon.awssdk.services.dynamodb.model.AttributeValue;

class EventTest {

    @Test
    void testConstructorAndGetters() {
        Event event = new Event(
            1,
            "Tech Meetup",
            "A meetup for tech enthusiasts",
            101,
            Set.of("Innovation", "AI"),
            Set.of(201, 202),
            "http://example.com/event.jpg"
        );

        assertEquals(1, event.getEventId());
        assertEquals("Tech Meetup", event.getName());
        assertEquals("A meetup for tech enthusiasts", event.getDescription());
        assertEquals(101, event.getClubId());
        assertEquals(Set.of("Innovation", "AI"), event.getTags());
        assertEquals(Set.of(201, 202), event.getAttendeeIds());
        assertEquals("http://example.com/event.jpg", event.getImageUrl());
    }

    @Test
    void testSetters() {
        Event event = new Event();

        event.setEventId(2);
        event.setName("Science Workshop");
        event.setDescription("A workshop for science enthusiasts");
        event.setClubId(102);
        event.setTags(Set.of("Physics", "Chemistry"));
        event.setAttendeeIds(Set.of(301, 302));
        event.setImageUrl("http://example.com/workshop.jpg");

        assertEquals(2, event.getEventId());
        assertEquals("Science Workshop", event.getName());
        assertEquals("A workshop for science enthusiasts", event.getDescription());
        assertEquals(102, event.getClubId());
        assertEquals(Set.of("Physics", "Chemistry"), event.getTags());
        assertEquals(Set.of(301, 302), event.getAttendeeIds());
        assertEquals("http://example.com/workshop.jpg", event.getImageUrl());
    }

    @Test
    void testToDynamoDbMap() {
        Event event = new Event(
            3,
            "Art Exhibition",
            "An exhibition for art lovers",
            103,
            Set.of("Painting", "Sculpture"),
            Set.of(401, 402),
            "http://example.com/art.jpg"
        );

        Map<String, AttributeValue> dynamoMap = event.toDynamoDbMap();

        assertEquals("3", dynamoMap.get("eventId").n());
        assertEquals("Art Exhibition", dynamoMap.get("name").s());
        assertEquals("An exhibition for art lovers", dynamoMap.get("description").s());
        assertEquals("103", dynamoMap.get("clubId").n());
        assertEquals(Set.of("Painting", "Sculpture"), Set.copyOf(dynamoMap.get("tags").ss()));
        assertEquals(Set.of("401", "402"), Set.copyOf(dynamoMap.get("attendeeIds").ns()));
        assertEquals("http://example.com/art.jpg", dynamoMap.get("imageUrl").s());
    }

    @Test
    void testFromDynamoDbMap() {
        Map<String, AttributeValue> dynamoMap = Map.of(
            "eventId", AttributeValue.builder().n("4").build(),
            "name", AttributeValue.builder().s("Music Concert").build(),
            "description", AttributeValue.builder().s("A concert for music lovers").build(),
            "clubId", AttributeValue.builder().n("104").build(),
            "tags", AttributeValue.builder().ss("Rock", "Jazz").build(),
            "attendeeIds", AttributeValue.builder().ns("501", "502").build(),
            "imageUrl", AttributeValue.builder().s("http://example.com/concert.jpg").build()
        );

        Event event = Event.fromDynamoDbMap(dynamoMap);

        assertEquals(4, event.getEventId());
        assertEquals("Music Concert", event.getName());
        assertEquals("A concert for music lovers", event.getDescription());
        assertEquals(104, event.getClubId());
        assertEquals(Set.of("Rock", "Jazz"), event.getTags());
        assertEquals(Set.of(501, 502), event.getAttendeeIds());
        assertEquals("http://example.com/concert.jpg", event.getImageUrl());
    }

    @Test
    void testToString() {
        Event event = new Event(
            5,
            "Drama Performance",
            "A performance for drama enthusiasts",
            105,
            Set.of("Theatre"),
            Set.of(601),
            "http://example.com/drama.jpg"
        );

        String expectedString = "Event{eventId=5, name='Drama Performance', description='A performance for drama enthusiasts', clubId=105, tags=[Theatre], attendeeIds=[601], imageUrl='http://example.com/drama.jpg'}";

        assertEquals(expectedString, event.toString());
    }
}
