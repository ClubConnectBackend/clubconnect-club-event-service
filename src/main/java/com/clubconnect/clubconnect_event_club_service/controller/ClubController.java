package com.clubconnect.clubconnect_event_club_service.controller;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.clubconnect.clubconnect_event_club_service.model.Club;
import com.clubconnect.clubconnect_event_club_service.service.ClubService;
import com.clubconnect.clubconnect_event_club_service.service.EventService;

@RestController
@RequestMapping("/api/clubs")
@CrossOrigin(origins = "http://localhost:4200",allowedHeaders = "*")
public class ClubController {

    private final ClubService clubService;
    private final EventService eventService;

    @Autowired
    public ClubController(ClubService clubService, EventService eventService) {
        this.clubService = clubService;
        this.eventService = eventService;
    }

    /**
     * Create or update a club
     *
     * @param club the club object
     * @return a response indicating success or failure
     */
    @PostMapping
    public ResponseEntity<?> createClub(@RequestBody Club club) {
        try {
            // Check if a club with the same clubId already exists
            Optional<Club> existingClub = clubService.getClubById(club.getClubId());
            if (existingClub.isPresent()) {
                return ResponseEntity.status(HttpStatus.CONFLICT)
                        .body("Club with the same ID already exists.");
            }
            // Save the club
            clubService.saveClub(club);
            return ResponseEntity.status(HttpStatus.CREATED).body("Club created successfully.");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error creating club: " + e.getMessage());
        }
    }


    /**
     * Get a club by its ID
     *
     * @param clubId the club ID
     * @return the club details or an error response
     */
    @GetMapping("/{clubId}")
    public ResponseEntity<?> getClubById(@PathVariable Integer clubId) {
        try {
            Optional<Club> clubOptional = clubService.getClubById(clubId);
            if (clubOptional.isPresent()) {
                return ResponseEntity.ok(clubOptional.get());
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Club not found.");
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error retrieving club: " + e.getMessage());
        }
    }


    /**
     * Add an event to a club
     *
     * @param clubId  the club ID
     * @param eventId the event ID
     * @return a response indicating success or failure
     */
    @PostMapping("/{clubId}/events/{eventId}")
    public ResponseEntity<?> addEventToClub(@PathVariable Integer clubId, @PathVariable Integer eventId) {
        try {
            if (clubService.addEventToClub(clubId, eventId)) {
                return ResponseEntity.ok("Event added to club successfully.");
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Club not found or event could not be added.");
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error adding event to club: " + e.getMessage());
        }
    }

    /**
     * Remove an event from a club
     *
     * @param clubId  the club ID
     * @param eventId the event ID
     * @return a response indicating success or failure
     */
    @DeleteMapping("/{clubId}/events/{eventId}")
    public ResponseEntity<?> removeEventFromClub(@PathVariable Integer clubId, @PathVariable Integer eventId) {
        try {
            if (clubService.removeEventFromClub(clubId, eventId)) {
                return ResponseEntity.ok("Event removed from club successfully.");
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Club not found or event could not be removed.");
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error removing event from club: " + e.getMessage());
        }
    }

    /**
     * Get all events for a specific club
     *
     * @param clubId the club ID
     * @return a set of event IDs belonging to the club or an error response
     */
    @GetMapping("/{clubId}/events")
    public ResponseEntity<?> getAllEventsForClub(@PathVariable Integer clubId) {
        try {
            Optional<Set<Integer>> eventIdsOptional = clubService.getAllEventsForClub(clubId);
            if (eventIdsOptional.isPresent()) {
                return ResponseEntity.ok(eventIdsOptional.get());
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("No events found for the specified club.");
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error retrieving events: " + e.getMessage());
        }
    }

    /**
     * Delete a club by its ID
     *
     * @param clubId the club ID
     * @return a response indicating success or failure
     */
    @DeleteMapping("/{clubId}")
    public ResponseEntity<?> deleteClub(@PathVariable Integer clubId) {
        try {
            // Check if the club exists
            Optional<Club> existingClub = clubService.getClubById(clubId);
            if (existingClub.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Club with ID " + clubId + " does not exist.");
            }

            // Retrieve the club
            Club club = existingClub.get();

            // Delete all events associated with the club
            Set<Integer> eventIds = club.getEventIds();
            if (eventIds != null && !eventIds.isEmpty()) {
                for (Integer eventId : eventIds) {
                    eventService.deleteEvent(eventId);
                }
            }

            // Delete the club
            clubService.deleteClub(clubId);

            return ResponseEntity.ok("Club and all its associated events deleted successfully.");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error deleting club: " + e.getMessage());
        }
    }

    /**
     * Get all clubs
     *
     * @return a list of all clubs or an error response
     */
    @GetMapping
    public ResponseEntity<?> getAllClubs() {
        try {
            List<Club> clubs = clubService.getAllClubs();
            if (clubs.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NO_CONTENT).body("No clubs found.");
            }
            return ResponseEntity.ok(clubs);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error fetching clubs: " + e.getMessage());
        }
    }


}
