package com.clubconnect.clubconnect_event_club_service.controller;

import static org.mockito.ArgumentMatchers.any;
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

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.clubconnect.clubconnect_event_club_service.model.Club;
import com.clubconnect.clubconnect_event_club_service.service.ClubService;
import com.clubconnect.clubconnect_event_club_service.service.EventService;

public class ClubControllerTest {

    @Mock
    private ClubService clubService;

    @Mock
    private EventService eventService;

    @InjectMocks
    private ClubController clubController;

    private MockMvc mockMvc;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
        mockMvc = MockMvcBuilders.standaloneSetup(clubController).build();
    }

    @Test
    void testCreateClub_Success() throws Exception {
        Club club = new Club();
        club.setClubId(1);
        club.setName("Test Club");

        when(clubService.getClubById(1)).thenReturn(Optional.empty());

        mockMvc.perform(post("/api/clubs")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"clubId\":1,\"name\":\"Test Club\"}"))
                .andExpect(status().isCreated())
                .andExpect(content().string("Club created successfully."));

        verify(clubService, times(1)).saveClub(any(Club.class));
    }

    @Test
    void testCreateClub_Conflict() throws Exception {
        Club club = new Club();
        club.setClubId(1);
        club.setName("Test Club");

        when(clubService.getClubById(1)).thenReturn(Optional.of(club));

        mockMvc.perform(post("/api/clubs")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"clubId\":1,\"name\":\"Test Club\"}"))
                .andExpect(status().isConflict())
                .andExpect(content().string("Club with the same ID already exists."));

        verify(clubService, never()).saveClub(any(Club.class));
    }

    @Test
    void testGetClubById_Success() throws Exception {
        Club club = new Club();
        club.setClubId(1);
        club.setName("Test Club");

        when(clubService.getClubById(1)).thenReturn(Optional.of(club));

        mockMvc.perform(get("/api/clubs/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.clubId").value(1))
                .andExpect(jsonPath("$.name").value("Test Club"));

        verify(clubService, times(1)).getClubById(1);
    }

    @Test
    void testGetClubById_NotFound() throws Exception {
        when(clubService.getClubById(1)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/clubs/1"))
                .andExpect(status().isNotFound())
                .andExpect(content().string("Club not found."));

        verify(clubService, times(1)).getClubById(1);
    }

    @Test
    void testAddEventToClub_Success() throws Exception {
        when(clubService.addEventToClub(1, 101)).thenReturn(true);

        mockMvc.perform(post("/api/clubs/1/events/101"))
                .andExpect(status().isOk())
                .andExpect(content().string("Event added to club successfully."));

        verify(clubService, times(1)).addEventToClub(1, 101);
    }

    @Test
    void testAddEventToClub_NotFound() throws Exception {
        when(clubService.addEventToClub(1, 101)).thenReturn(false);

        mockMvc.perform(post("/api/clubs/1/events/101"))
                .andExpect(status().isNotFound())
                .andExpect(content().string("Club not found or event could not be added."));

        verify(clubService, times(1)).addEventToClub(1, 101);
    }

    @Test
    void testDeleteClub_Success() throws Exception {
        Club club = new Club();
        club.setClubId(1);
        club.setEventIds(Set.of(101, 102));

        when(clubService.getClubById(1)).thenReturn(Optional.of(club));

        mockMvc.perform(delete("/api/clubs/1"))
                .andExpect(status().isOk())
                .andExpect(content().string("Club and all its associated events deleted successfully."));

        verify(eventService, times(1)).deleteEvent(101);
        verify(eventService, times(1)).deleteEvent(102);
        verify(clubService, times(1)).deleteClub(1);
    }

    @Test
    void testDeleteClub_NotFound() throws Exception {
        when(clubService.getClubById(1)).thenReturn(Optional.empty());

        mockMvc.perform(delete("/api/clubs/1"))
                .andExpect(status().isNotFound())
                .andExpect(content().string("Club with ID 1 does not exist."));

        verify(clubService, never()).deleteClub(1);
    }

    @Test
    void testGetAllClubs_NoContent() throws Exception {
        when(clubService.getAllClubs()).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/api/clubs"))
                .andExpect(status().isNoContent())
                .andExpect(content().string("No clubs found."));

        verify(clubService, times(1)).getAllClubs();
    }

    @Test
    void testGetAllClubs_Success() throws Exception {
        Club club1 = new Club();
        club1.setClubId(1);
        club1.setName("Test Club 1");

        Club club2 = new Club();
        club2.setClubId(2);
        club2.setName("Test Club 2");

        when(clubService.getAllClubs()).thenReturn(List.of(club1, club2));

        mockMvc.perform(get("/api/clubs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].clubId").value(1))
                .andExpect(jsonPath("$[1].clubId").value(2));

        verify(clubService, times(1)).getAllClubs();
    }
}
