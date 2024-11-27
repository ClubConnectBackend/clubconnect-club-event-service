package com.clubconnect.clubconnect_event_club_service.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Optional;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.clubconnect.clubconnect_event_club_service.model.Club;
import com.clubconnect.clubconnect_event_club_service.model.Event;
import com.clubconnect.clubconnect_event_club_service.service.ClubService;
import com.clubconnect.clubconnect_event_club_service.service.EventService;
import com.clubconnect.clubconnect_event_club_service.service.NotificationPublisher;

class EventControllerTest {

    @Mock
    private EventService eventService;

    @Mock
    private ClubService clubService;

    @Mock
    private NotificationPublisher notificationPublisher;

    @InjectMocks
    private EventController eventController;

    @Autowired
    private MockMvc mockMvc;

    private Event sampleEvent;
    private Club sampleClub;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        mockMvc = MockMvcBuilders.standaloneSetup(eventController).build();

        sampleEvent = new Event();
        sampleEvent.setEventId(1);
        sampleEvent.setName("Tech Event");
        sampleEvent.setDescription("A technology-focused event");
        sampleEvent.setClubId(1);
        sampleEvent.setTags(Set.of("Technology", "Innovation"));

        sampleClub = new Club();
        sampleClub.setClubId(1);
        sampleClub.setName("Tech Club");
        sampleClub.setEventIds(Set.of(1));
    }
    
    @Test
    void testCreateEvent_Conflict() throws Exception {
        when(eventService.getEventById(1)).thenReturn(Optional.of(sampleEvent));

        mockMvc.perform(post("/api/events")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"eventId\":1,\"name\":\"Tech Event\",\"description\":\"A technology-focused event\",\"clubId\":1,\"tags\":[\"Technology\",\"Innovation\"]}"))
                .andExpect(status().isConflict())
                .andExpect(content().string("Event with the same ID already exists."));

        verify(eventService, never()).saveEvent(any(Event.class));
        verify(notificationPublisher, never()).publishEventNotification(anyString(), anyString(), any());
    }

    @Test
    void testCreateEvent_ClubNotFound() throws Exception {
        when(eventService.getEventById(1)).thenReturn(Optional.empty());
        when(clubService.getClubById(1)).thenReturn(Optional.empty());

        mockMvc.perform(post("/api/events")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"eventId\":1,\"name\":\"Tech Event\",\"description\":\"A technology-focused event\",\"clubId\":1,\"tags\":[\"Technology\",\"Innovation\"]}"))
                .andExpect(status().isNotFound())
                .andExpect(content().string("Club with ID 1 does not exist."));

        verify(eventService, never()).saveEvent(any(Event.class));
        verify(notificationPublisher, never()).publishEventNotification(anyString(), anyString(), any());
    }

    @Test
    void testGetEventById_Success() throws Exception {
        when(eventService.getEventById(1)).thenReturn(Optional.of(sampleEvent));

        mockMvc.perform(get("/api/events/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.eventId").value(1))
                .andExpect(jsonPath("$.name").value("Tech Event"));

        verify(eventService, times(1)).getEventById(1);
    }

    @Test
    void testGetEventById_NotFound() throws Exception {
        when(eventService.getEventById(1)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/events/1"))
                .andExpect(status().isNotFound())
                .andExpect(content().string("Event not found."));

        verify(eventService, times(1)).getEventById(1);
    }

    @Test
    void testGetEventsByTag_Success() throws Exception {
        when(eventService.getEventsByTag("Technology")).thenReturn(Set.of(sampleEvent));

        mockMvc.perform(get("/api/events/tag/Technology"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].eventId").value(1))
                .andExpect(jsonPath("$[0].name").value("Tech Event"));

        verify(eventService, times(1)).getEventsByTag("Technology");
    }


    @Test
    void testDeleteEvent_NotFound() throws Exception {
        when(eventService.getEventById(1)).thenReturn(Optional.empty());

        mockMvc.perform(delete("/api/events/1"))
                .andExpect(status().isNotFound())
                .andExpect(content().string("Event with ID 1 does not exist."));

        verify(eventService, never()).deleteEvent(anyInt());
        verify(clubService, never()).saveClub(any(Club.class));
    }
}
