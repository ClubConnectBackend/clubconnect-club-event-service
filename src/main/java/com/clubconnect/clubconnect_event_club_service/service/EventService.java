package com.clubconnect.clubconnect_event_club_service.service;

import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.clubconnect.clubconnect_event_club_service.model.Event;
import com.clubconnect.clubconnect_event_club_service.repository.ClubRepository;
import com.clubconnect.clubconnect_event_club_service.repository.EventRepository;

import software.amazon.awssdk.services.dynamodb.model.AttributeValue;

@Service
public class EventService {

    private final EventRepository eventRepository;
    private final ClubRepository clubRepository;

    @Autowired
    public EventService(EventRepository eventRepository, ClubRepository clubRepository) {
        this.eventRepository = eventRepository;
        this.clubRepository = clubRepository;
    }

    /**
     * Save or update an event in DynamoDB
     *
     * @param event the Event object to save or update
     */
    public void saveEvent(Event event) {
        eventRepository.saveEvent(
            event.getEventId(),
            event.getName(),
            event.getDescription(),
            event.getClubId(),
            event.getTags(),
            event.getAttendeeIds(),
            event.getImageUrl() != null ? event.getImageUrl() : "" // Default to empty string
        );
    }

    /**
     * Retrieve an event by its ID
     *
     * @param eventId the ID of the event
     * @return an Optional containing the Event object, if found
     */
    public Optional<Event> getEventById(Integer eventId) {
        Map<String, AttributeValue> eventMap = eventRepository.findEventById(eventId);
        if (eventMap == null || eventMap.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(Event.fromDynamoDbMap(eventMap));
    }

    /**
     * Retrieve all events that contain a specific tag
     *
     * @param tag the tag to search for
     * @return a set of events that contain the specified tag
     */
    public Set<Event> getEventsByTag(String tag) {
        return eventRepository.findAllEvents().values().stream()
                .map(Event::fromDynamoDbMap)
                .filter(event -> event.getTags().contains(tag))
                .collect(Collectors.toSet());
    }

    /**
     * Add an attendee to an event
     *
     * @param eventId    the ID of the event
     * @param attendeeId the ID of the attendee to add
     * @return true if the attendee was added successfully, false otherwise
     */
    public boolean addAttendeeToEvent(Integer eventId, Integer attendeeId) {
        var eventMap = eventRepository.findEventById(eventId);
        if (eventMap != null && !eventMap.isEmpty()) {
            Set<Integer> existingAttendees = eventMap.containsKey("attendeeIds") && eventMap.get("attendeeIds").hasNs()
                    ? eventMap.get("attendeeIds").ns().stream().map(Integer::valueOf).collect(Collectors.toSet())
                    : Set.of();
            existingAttendees.add(attendeeId);

            // Preserve other fields while updating attendees
            Event event = Event.fromDynamoDbMap(eventMap);
            event.setAttendeeIds(existingAttendees);
            saveEvent(event);

            return true;
        }
        return false;
    }

    /**
     * Remove an attendee from an event
     *
     * @param eventId    the ID of the event
     * @param attendeeId the ID of the attendee to remove
     * @return true if the attendee was removed successfully, false otherwise
     */
    public boolean removeAttendeeFromEvent(Integer eventId, Integer attendeeId) {
        var eventMap = eventRepository.findEventById(eventId);
        if (eventMap != null && !eventMap.isEmpty()) {
            Set<Integer> existingAttendees = eventMap.containsKey("attendeeIds") && eventMap.get("attendeeIds").hasNs()
                    ? eventMap.get("attendeeIds").ns().stream().map(Integer::valueOf).collect(Collectors.toSet())
                    : Set.of();
            if (existingAttendees.remove(attendeeId)) {
                // Preserve other fields while updating attendees
                Event event = Event.fromDynamoDbMap(eventMap);
                event.setAttendeeIds(existingAttendees);
                saveEvent(event);

                return true;
            }
        }
        return false;
    }

    /**
     * Delete an event by its ID
     *
     * @param eventId the ID of the event to delete
     */
    public void deleteEvent(Integer eventId) {
        // Retrieve the event to determine which club it belongs to
        Optional<Event> eventOptional = getEventById(eventId);
        if (eventOptional.isPresent()) {
            Event event = eventOptional.get();
            Integer clubId = event.getClubId();

            // Remove the event from the club
            var clubMap = clubRepository.findClubById(clubId);
            if (clubMap != null && !clubMap.isEmpty()) {
                Set<Integer> eventIds = clubMap.containsKey("eventIds") && clubMap.get("eventIds").hasNs()
                        ? clubMap.get("eventIds").ns().stream().map(Integer::valueOf).collect(Collectors.toSet())
                        : Set.of();
                eventIds.remove(eventId);
                clubRepository.updateEventIds(clubId, eventIds);
            }
        }

        // Finally, delete the event
        eventRepository.deleteEvent(eventId);
    }

    /**
     * Retrieve all events
     *
     * @return a set of all Event objects
     */
    public Set<Event> getAllEvents() {
        return eventRepository.findAllEvents().values().stream()
                .map(Event::fromDynamoDbMap)
                .collect(Collectors.toSet());
    }
}
