package com.clubconnect.clubconnect_event_club_service.service;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.clubconnect.clubconnect_event_club_service.model.Club;
import com.clubconnect.clubconnect_event_club_service.repository.ClubRepository;

import software.amazon.awssdk.services.dynamodb.model.AttributeValue;

@Service
public class ClubService {

    private final ClubRepository clubRepository;

    @Autowired
    public ClubService(ClubRepository clubRepository) {
        this.clubRepository = clubRepository;
    }

    /**
     * Save or update a club in DynamoDB
     *
     * @param club the Club object to save or update
     */
    public void saveClub(Club club) {
        System.out.println("Saving Club: " + club);
        clubRepository.saveClub(      
            club.getClubId(),
            club.getName(),
            club.getDescription(),
            club.getEventIds()
        );
    }

    /**
     * Retrieve a club by its ID
     *
     * @param clubId the ID of the club
     * @return an Optional containing the Club object, if found
     */
    public Optional<Club> getClubById(Integer clubId) {
        Map<String, AttributeValue> item = clubRepository.findClubById(clubId);
        if (item == null || item.isEmpty()) {
            System.out.println("Club not found for ID: " + clubId);
            return Optional.empty();
        }

        Club club = Club.fromDynamoDbMap(item);
        System.out.println("Club fetched: " + club);
        return Optional.of(club);
    }

    

    /**
     * Add an event to a club's list of events
     *
     * @param clubId  the ID of the club
     * @param eventId the ID of the event to add
     * @return true if the event was added successfully, false otherwise
     */
    public boolean addEventToClub(Integer clubId, Integer eventId) {
        try {
            Set<Integer> existingEventIds = clubRepository.findClubById(clubId)
                    .getOrDefault("eventIds", AttributeValue.builder().ns().build())
                    .ns().stream().map(Integer::valueOf).collect(Collectors.toSet());
            existingEventIds.add(eventId);
            clubRepository.updateEventIds(clubId, existingEventIds);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Remove an event from a club's list of events
     *
     * @param clubId  the ID of the club
     * @param eventId the ID of the event to remove
     * @return true if the event was removed successfully, false otherwise
     */
    public boolean removeEventFromClub(Integer clubId, Integer eventId) {
        try {
            Set<Integer> existingEventIds = clubRepository.findClubById(clubId)
                    .getOrDefault("eventIds", AttributeValue.builder().ns().build())
                    .ns().stream().map(Integer::valueOf).collect(Collectors.toSet());
            if (existingEventIds.contains(eventId)) {
                existingEventIds.remove(eventId);
                clubRepository.updateEventIds(clubId, existingEventIds);
                return true;
            }
            return false;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Retrieve all events for a specific club by its ID
     *
     * @param clubId the ID of the club
     * @return a set of event IDs belonging to the club
     */
    public Optional<Set<Integer>> getAllEventsForClub(Integer clubId) {
        try {
            Set<Integer> eventIds = clubRepository.findClubById(clubId)
                    .getOrDefault("eventIds", AttributeValue.builder().ns().build())
                    .ns().stream().map(Integer::valueOf).collect(Collectors.toSet());
            return Optional.of(eventIds);
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    /**
     * Delete a club by its ID
     *
     * @param clubId the ID of the club to delete
     */
    public void deleteClub(Integer clubId) {
        clubRepository.deleteClub(clubId);
    }
    

    public List<Club> getAllClubs() {
        List<Map<String, AttributeValue>> clubItems = clubRepository.findAllClubs();
        return clubItems.stream()
                .map(Club::fromDynamoDbMap)
                .collect(Collectors.toList());
    }

}
