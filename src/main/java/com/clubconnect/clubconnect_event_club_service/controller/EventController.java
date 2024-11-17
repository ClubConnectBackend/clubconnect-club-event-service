package com.clubconnect.clubconnect_event_club_service.controller;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.clubconnect.clubconnect_event_club_service.model.Club;
import com.clubconnect.clubconnect_event_club_service.model.Event;
import com.clubconnect.clubconnect_event_club_service.service.ClubService;
import com.clubconnect.clubconnect_event_club_service.service.EventService;

@RestController
@RequestMapping("/api/events")
public class EventController {

    private final EventService eventService;
    private final ClubService clubService;

    @Autowired
    public EventController(EventService eventService, ClubService clubService) {
        this.eventService = eventService;
        this.clubService = clubService;
    }

    /**
     * Create or update an event
     *
     * @param event the event object
     * @return a response indicating success or failure
     */
    @PostMapping
    public ResponseEntity<?> createEvent(@RequestBody Event event) {
        try {
            // Check if an event with the same eventId already exists
            Optional<Event> existingEvent = eventService.getEventById(event.getEventId());
            if (existingEvent.isPresent()) {
                return ResponseEntity.status(HttpStatus.CONFLICT)
                        .body("Event with the same ID already exists.");
            }

            // Check if the club exists
            Optional<Club> clubOptional = clubService.getClubById(event.getClubId());
            if (clubOptional.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body("Club with ID " + event.getClubId() + " does not exist.");
            }

            // Save the event
            eventService.saveEvent(event);

            // Update the club with the new eventId
            Club club = clubOptional.get();
            Set<Integer> eventIds = club.getEventIds() != null ? 
                    new HashSet<>(club.getEventIds()) : 
                    new HashSet<>();
            eventIds.add(event.getEventId());
            club.setEventIds(eventIds);
            clubService.saveClub(club);

            return ResponseEntity.status(HttpStatus.CREATED).body("Event created successfully and Club updated.");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error creating event: " + e.getMessage());
        }
    }



    /**
     * Get an event by its ID
     *
     * @param eventId the event ID
     * @return the event details or an error response
     */
    @GetMapping("/{eventId}")
    public ResponseEntity<?> getEventById(@PathVariable Integer eventId) {
        Optional<Event> eventOptional = eventService.getEventById(eventId);
        if (eventOptional.isPresent()) {
            return ResponseEntity.ok(eventOptional.get());
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Event not found.");
        }
    }


    /**
     * Get all events by a specific tag
     *
     * @param tag the tag to filter events
     * @return a set of events with the specified tag
     */
    @GetMapping("/tag/{tag}")
    public ResponseEntity<?> getEventsByTag(@PathVariable String tag) {
        try {
            Set<Event> events = eventService.getEventsByTag(tag);
            return ResponseEntity.ok(events);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error retrieving events: " + e.getMessage());
        }
    }

    /**
     * Add an attendee to an event
     *
     * @param eventId    the event ID
     * @param attendeeId the attendee ID
     * @return a response indicating success or failure
     */
    @PostMapping("/{eventId}/attendees/{attendeeId}")
    public ResponseEntity<?> addAttendeeToEvent(@PathVariable Integer eventId, @PathVariable Integer attendeeId) {
        if (eventService.addAttendeeToEvent(eventId, attendeeId)) {
            return ResponseEntity.ok("Attendee added successfully.");
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Event not found or attendee could not be added.");
        }
    }

    /**
     * Remove an attendee from an event
     *
     * @param eventId    the event ID
     * @param attendeeId the attendee ID
     * @return a response indicating success or failure
     */
    @DeleteMapping("/{eventId}/attendees/{attendeeId}")
    public ResponseEntity<?> removeAttendeeFromEvent(@PathVariable Integer eventId, @PathVariable Integer attendeeId) {
        if (eventService.removeAttendeeFromEvent(eventId, attendeeId)) {
            return ResponseEntity.ok("Attendee removed successfully.");
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Event not found or attendee could not be removed.");
        }
    }

    /**
     * Delete an event by its ID
     *
     * @param eventId the event ID
     * @return a response indicating success or failure
     */
    @DeleteMapping("/{eventId}")
    public ResponseEntity<?> deleteEvent(@PathVariable Integer eventId) {
        try {
            // Check if the event exists
            Optional<Event> existingEvent = eventService.getEventById(eventId);
            if (existingEvent.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Event with ID " + eventId + " does not exist.");
            }

            // Retrieve the event
            Event event = existingEvent.get();

            // Delete the event from the Event table
            eventService.deleteEvent(eventId);

            // Update the Club table to remove the eventId
            Optional<Club> clubOptional = clubService.getClubById(event.getClubId());
            if (clubOptional.isPresent()) {
                Club club = clubOptional.get();
                Set<Integer> eventIds = club.getEventIds();
                if (eventIds != null && eventIds.contains(eventId)) {
                    eventIds.remove(eventId);
                    club.setEventIds(eventIds);
                    clubService.saveClub(club);
                }
            }

            return ResponseEntity.ok("Event deleted successfully and removed from the associated club.");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error deleting event: " + e.getMessage());
        }
    }

    /**
     * Get all events
     *
     * @return a set of all events
     */
    @GetMapping
    public ResponseEntity<?> getAllEvents() {
        try {
            Set<Event> events = eventService.getAllEvents();
            return ResponseEntity.ok(events);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error retrieving events: " + e.getMessage());
        }
    }
}
