### README for Club and Event Microservice

---

# Club and Event Microservice

The **Club and Event Microservice** is a crucial component of the **ClubConnect** platform. It manages the creation, retrieval, and modification of clubs and events, enabling efficient organization and participation across the platform. This service interacts with AWS DynamoDB for data persistence and RabbitMQ for publishing event notifications.

---

## Features

### **Club Management**
- Create, retrieve, update, and delete clubs.
- Associate multiple events with a club.
- Fetch all events for a specific club.

### **Event Management**
- Create, retrieve, update, and delete events.
- Manage attendees for events (add/remove).
- Fetch events by specific tags.
- Fetch all events across the system.

### **Notification Integration**
- Publish event notifications to RabbitMQ for subscribers.

---

## Tech Stack

- **Frameworks**: Spring Boot
- **Database**: AWS DynamoDB
- **Messaging**: RabbitMQ
- **Build Tool**: Maven
- **Programming Language**: Java 17

---

## API Endpoints

### Club Management

| HTTP Method | Endpoint                          | Description                                           |
|-------------|-----------------------------------|-------------------------------------------------------|
| POST        | `/api/clubs`                     | Create a new club.                                   |
| GET         | `/api/clubs/{clubId}`            | Retrieve a club by its ID.                          |
| DELETE      | `/api/clubs/{clubId}`            | Delete a club and its associated events.            |
| POST        | `/api/clubs/{clubId}/events/{eventId}` | Add an event to a club.                         |
| DELETE      | `/api/clubs/{clubId}/events/{eventId}` | Remove an event from a club.                      |
| GET         | `/api/clubs/{clubId}/events`     | Retrieve all events associated with a club.         |
| GET         | `/api/clubs`                     | Retrieve all clubs in the system.                   |

---

### Event Management

| HTTP Method | Endpoint                        | Description                                           |
|-------------|---------------------------------|-------------------------------------------------------|
| POST        | `/api/events`                  | Create a new event.                                  |
| GET         | `/api/events/{eventId}`        | Retrieve an event by its ID.                        |
| DELETE      | `/api/events/{eventId}`        | Delete an event and update its associated club.     |
| POST        | `/api/events/{eventId}/attendees/{attendeeId}` | Add an attendee to an event.                    |
| DELETE      | `/api/events/{eventId}/attendees/{attendeeId}` | Remove an attendee from an event.                |
| GET         | `/api/events/tag/{tag}`        | Retrieve all events with a specific tag.            |
| GET         | `/api/events`                  | Retrieve all events in the system.                  |

---

## Architecture

### Layers

1. **Controller Layer**:
   - Contains RESTful API endpoints for handling HTTP requests.
   - Processes and validates user inputs.
   - Returns appropriate HTTP responses.

2. **Service Layer**:
   - Implements business logic for club and event management.
   - Interacts with repositories for persistence.
   - Publishes event notifications to RabbitMQ.

3. **Repository Layer**:
   - Handles DynamoDB interactions using AWS SDK.
   - Performs CRUD operations for club and event data.

4. **Messaging Layer**:
   - Publishes event notifications to RabbitMQ.

---

## AWS DynamoDB Schema

### Table: **Clubs**

| Attribute      | Type   | Description                                    |
|-----------------|--------|------------------------------------------------|
| `clubId`       | Number | Primary key, unique for each club.             |
| `name`         | String | Name of the club.                              |
| `description`  | String | Description of the club.                       |
| `eventIds`     | List   | List of event IDs associated with the club.    |
| `imageUrl`     | String | S3 URL of the club's image.                    |

---

### Table: **Events**

| Attribute      | Type   | Description                                    |
|-----------------|--------|------------------------------------------------|
| `eventId`      | Number | Primary key, unique for each event.            |
| `name`         | String | Name of the event.                             |
| `description`  | String | Description of the event.                      |
| `clubId`       | Number | Club ID organizing the event.                  |
| `tags`         | List   | Tags associated with the event (e.g., AI).     |
| `attendeeIds`  | List   | List of user IDs attending the event.          |
| `imageUrl`     | String | S3 URL of the event's image.                   |

---

## Security

- **Data Validation**:
  - Input validation for club and event creation.
  - Proper handling of missing or invalid IDs.

---

## How to Run the Service

1. **Clone the Repository**:
   ```bash
   git clone https://github.com/your-repository/club-event-microservice.git
   cd club-event-microservice
   ```

2. **Set Up AWS Credentials**:
   Configure your AWS credentials in `~/.aws/credentials` or use environment variables:
   ```bash
   export AWS_ACCESS_KEY_ID=your-access-key
   export AWS_SECRET_ACCESS_KEY=your-secret-key
   ```

3. **Configure RabbitMQ**:
   - Add RabbitMQ credentials in `application.properties`:
     ```properties
     spring.rabbitmq.host=<RABBITMQ_HOST>
     spring.rabbitmq.username=<RABBITMQ_USERNAME>
     spring.rabbitmq.password=<RABBITMQ_PASSWORD>
     ```

4. **Build the Project**:
   ```bash
   mvn clean install
   ```

5. **Run the Service**:
   ```bash
   mvn spring-boot:run
   ```

---

## Testing

### Run Unit Tests
```bash
mvn test
```

### Run Tests with Coverage
```bash
mvn test jacoco:report
```

---

## Example Requests

### Create a Club
**Endpoint**: `/api/clubs`  
**Method**: `POST`  
**Request Body**:
```json
{
  "clubId": 1,
  "name": "AI Club",
  "description": "A club for AI enthusiasts.",
  "imageUrl": "https://example.com/images/ai-club.png"
}
```

### Create an Event
**Endpoint**: `/api/events`  
**Method**: `POST`  
**Request Body**:
```json
{
  "eventId": 101,
  "name": "AI Workshop",
  "description": "Learn the basics of AI.",
  "clubId": 1,
  "tags": ["AI", "Workshop"],
  "imageUrl": "https://example.com/images/ai-workshop.png"
}
```

### Add an Event to a Club
**Endpoint**: `/api/clubs/1/events/101`  
**Method**: `POST`

---

## Future Improvements

- Implement advanced search and filter functionality for events and clubs.
- Enhance notification mechanisms with message priorities.
- Add caching for frequently accessed data.

---
