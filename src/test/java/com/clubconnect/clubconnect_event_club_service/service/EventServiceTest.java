package com.clubconnect.clubconnect_event_club_service.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anySet;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.clubconnect.clubconnect_event_club_service.model.Event;
import com.clubconnect.clubconnect_event_club_service.repository.ClubRepository;
import com.clubconnect.clubconnect_event_club_service.repository.EventRepository;

import software.amazon.awssdk.services.dynamodb.model.AttributeValue;

class EventServiceTest {

    @Mock
    private EventRepository eventRepository;

    @Mock
    private ClubRepository clubRepository;

    @InjectMocks
    private EventService eventService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testSaveEvent() {
        Event event = new Event(1, "Tech Talk", "An event about technology", 101, Set.of("Technology"), Set.of(1, 2), "image_url");
        doNothing().when(eventRepository).saveEvent(anyInt(), anyString(), anyString(), anyInt(), anySet(), anySet(), anyString());

        eventService.saveEvent(event);

        verify(eventRepository, times(1)).saveEvent(
                eq(1), eq("Tech Talk"), eq("An event about technology"), eq(101), eq(Set.of("Technology")), eq(Set.of(1, 2)), eq("image_url")
        );
    }

    @Test
    void testGetEventById_EventExists() {
        Map<String, AttributeValue> mockResponse = Map.of(
                "eventId", AttributeValue.builder().n("1").build(),
                "name", AttributeValue.builder().s("Tech Talk").build(),
                "description", AttributeValue.builder().s("An event about technology").build(),
                "clubId", AttributeValue.builder().n("101").build(),
                "tags", AttributeValue.builder().ss("Technology").build(),
                "attendeeIds", AttributeValue.builder().ns("1", "2").build(),
                "imageUrl", AttributeValue.builder().s("image_url").build()
        );
        when(eventRepository.findEventById(1)).thenReturn(mockResponse);

        Optional<Event> result = eventService.getEventById(1);

        assertTrue(result.isPresent());
        assertEquals("Tech Talk", result.get().getName());
        verify(eventRepository, times(1)).findEventById(1);
    }

    @Test
    void testGetEventById_EventDoesNotExist() {
        when(eventRepository.findEventById(2)).thenReturn(Map.of());

        Optional<Event> result = eventService.getEventById(2);

        assertFalse(result.isPresent());
        verify(eventRepository, times(1)).findEventById(2);
    }


    @Test
    void testGetAllEvents() {
        Map<Integer, Map<String, AttributeValue>> mockResponse = Map.of(
                1, Map.of(
                        "eventId", AttributeValue.builder().n("1").build(),
                        "name", AttributeValue.builder().s("Tech Talk").build(),
                        "description", AttributeValue.builder().s("An event about technology").build(),
                        "clubId", AttributeValue.builder().n("101").build(),
                        "tags", AttributeValue.builder().ss("Technology").build(),
                        "attendeeIds", AttributeValue.builder().ns("1", "2").build(),
                        "imageUrl", AttributeValue.builder().s("image_url").build()
                )
        );
        when(eventRepository.findAllEvents()).thenReturn(mockResponse);

        Set<Event> result = eventService.getAllEvents();

        assertEquals(1, result.size());
        assertTrue(result.stream().anyMatch(event -> "Tech Talk".equals(event.getName())));
        verify(eventRepository, times(1)).findAllEvents();
    }

}
