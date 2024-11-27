package com.clubconnect.clubconnect_event_club_service.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.Map;
import java.util.Set;

import org.junit.jupiter.api.Test;

import software.amazon.awssdk.services.dynamodb.model.AttributeValue;

class ClubTest {

    @Test
    void testConstructorAndGetters() {
        Club club = new Club(1, "Tech Club", "A club for tech enthusiasts", Set.of(101, 102), "http://example.com/image.jpg");

        assertEquals(1, club.getClubId());
        assertEquals("Tech Club", club.getName());
        assertEquals("A club for tech enthusiasts", club.getDescription());
        assertEquals(Set.of(101, 102), club.getEventIds());
        assertEquals("http://example.com/image.jpg", club.getImageUrl());
    }

    @Test
    void testSetters() {
        Club club = new Club();

        club.setClubId(2);
        club.setName("Science Club");
        club.setDescription("A club for science lovers");
        club.setEventIds(Set.of(201, 202));
        club.setImageUrl("http://example.com/science.jpg");

        assertEquals(2, club.getClubId());
        assertEquals("Science Club", club.getName());
        assertEquals("A club for science lovers", club.getDescription());
        assertEquals(Set.of(201, 202), club.getEventIds());
        assertEquals("http://example.com/science.jpg", club.getImageUrl());
    }

    @Test
    void testToDynamoDbMap() {
        Club club = new Club(3, "Art Club", "A club for art enthusiasts", Set.of(301, 302), "http://example.com/art.jpg");

        Map<String, AttributeValue> dynamoMap = club.toDynamoDbMap();

        assertEquals("3", dynamoMap.get("clubId").n());
        assertEquals("Art Club", dynamoMap.get("name").s());
        assertEquals("A club for art enthusiasts", dynamoMap.get("description").s());
        assertEquals(Set.of("301", "302"), Set.copyOf(dynamoMap.get("eventIds").ns()));
        assertEquals("http://example.com/art.jpg", dynamoMap.get("imageUrl").s());
    }

    @Test
    void testFromDynamoDbMap() {
        Map<String, AttributeValue> dynamoMap = Map.of(
            "clubId", AttributeValue.builder().n("4").build(),
            "name", AttributeValue.builder().s("Music Club").build(),
            "description", AttributeValue.builder().s("A club for music lovers").build(),
            "eventIds", AttributeValue.builder().ns("401", "402").build(),
            "imageUrl", AttributeValue.builder().s("http://example.com/music.jpg").build()
        );

        Club club = Club.fromDynamoDbMap(dynamoMap);

        assertEquals(4, club.getClubId());
        assertEquals("Music Club", club.getName());
        assertEquals("A club for music lovers", club.getDescription());
        assertEquals(Set.of(401, 402), club.getEventIds());
        assertEquals("http://example.com/music.jpg", club.getImageUrl());
    }

    @Test
    void testFromDynamoDbMap_InvalidInput() {
        Exception exception = assertThrows(IllegalArgumentException.class, () -> Club.fromDynamoDbMap(null));
        assertEquals("Invalid input: Map is null or empty.", exception.getMessage());

        exception = assertThrows(IllegalArgumentException.class, () -> Club.fromDynamoDbMap(Map.of()));
        assertEquals("Invalid input: Map is null or empty.", exception.getMessage());
    }

    @Test
    void testToString() {
        Club club = new Club(5, "Drama Club", "A club for drama enthusiasts", Set.of(501), "http://example.com/drama.jpg");
        String expectedString = "Club{clubId=5, name='Drama Club', description='A club for drama enthusiasts', eventIds=[501], imageUrl='http://example.com/drama.jpg'}";

        assertEquals(expectedString, club.toString());
    }
}
