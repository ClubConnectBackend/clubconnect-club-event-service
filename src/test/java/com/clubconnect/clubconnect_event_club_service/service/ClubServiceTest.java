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

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.clubconnect.clubconnect_event_club_service.model.Club;
import com.clubconnect.clubconnect_event_club_service.repository.ClubRepository;

import software.amazon.awssdk.services.dynamodb.model.AttributeValue;

class ClubServiceTest {

    @Mock
    private ClubRepository clubRepository;

    @InjectMocks
    private ClubService clubService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testSaveClub() {
        Club club = new Club(1, "Tech Club", "A club about tech", Set.of(101, 102), "image_url");
        doNothing().when(clubRepository).saveClub(anyInt(), anyString(), anyString(), anySet(), anyString());

        clubService.saveClub(club);

        verify(clubRepository, times(1)).saveClub(
                eq(1), eq("Tech Club"), eq("A club about tech"), eq(Set.of(101, 102)), eq("image_url")
        );
    }

    @Test
    void testGetClubById_ClubExists() {
        Map<String, AttributeValue> mockResponse = Map.of(
                "clubId", AttributeValue.builder().n("1").build(),
                "name", AttributeValue.builder().s("Tech Club").build(),
                "description", AttributeValue.builder().s("A club about tech").build(),
                "eventIds", AttributeValue.builder().ns("101", "102").build(),
                "imageUrl", AttributeValue.builder().s("image_url").build()
        );
        when(clubRepository.findClubById(1)).thenReturn(mockResponse);

        Optional<Club> result = clubService.getClubById(1);

        assertTrue(result.isPresent());
        assertEquals("Tech Club", result.get().getName());
        verify(clubRepository, times(1)).findClubById(1);
    }

    @Test
    void testGetClubById_ClubDoesNotExist() {
        when(clubRepository.findClubById(2)).thenReturn(Map.of());

        Optional<Club> result = clubService.getClubById(2);

        assertFalse(result.isPresent());
        verify(clubRepository, times(1)).findClubById(2);
    }

    @Test
    void testGetAllClubs() {
        List<Map<String, AttributeValue>> mockResponse = List.of(
                Map.of(
                        "clubId", AttributeValue.builder().n("1").build(),
                        "name", AttributeValue.builder().s("Tech Club").build(),
                        "description", AttributeValue.builder().s("A club about tech").build(),
                        "imageUrl", AttributeValue.builder().s("image_url").build()
                )
        );
        when(clubRepository.findAllClubs()).thenReturn(mockResponse);

        List<Club> result = clubService.getAllClubs();

        assertEquals(1, result.size());
        assertEquals("Tech Club", result.get(0).getName());
        verify(clubRepository, times(1)).findAllClubs();
    }
}
