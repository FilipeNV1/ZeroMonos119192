# Homework 1

## ZeroMonos

### Key Concepts

#### Architecture
```
HW1/
├── frontend/
│   ├── index.html          # Landing page with role selection
│   ├── user.html           # Citizen portal (create/track bookings)
│   ├── staff.html          # Staff portal (manage bookings)
│   ├── css/
│   │   └── style.css       # Unified styling
│   └── js/
│       └── app.js          # Frontend logic & API calls
│
├── backend/
│   └── src/
│       ├── main/java/zeromonos/
│       │   ├── boundary/
│       │   │   └── BookingController.java    # REST endpoints
│       │   ├── service/
│       │   │   └── BookingService.java       # Business logic
│       │   └── data/
│       │       ├── BookingRequest.java       # Booking entity
│       │       ├── BookingRepository.java    # Booking data access
│       │       ├── BookingHistoryRequest.java
│       │       └── BookingHistoryRepository.java
│       │
│       └── test/
│           ├── java/zeromonos/
│           │   ├── boundary/
│           │   │   ├── BookingControllerTest.java      # Unit tests (MockMvc)
│           │   │   └── BookingControllerIT.java        # Integration tests (REST-Assured)
│           │   ├── service/
│           │   │   └── BookingServiceTest.java         # Service tests (Mockito)
│           │   └── functionality/
│           │       └── WebFunctionalTest.java          # Functional tests using Selenium
│           │   └── data/
│           │       └── BookingRequest.java             # Test general booking data
│           │       └── BookingHistoryRequest.java      # Test booking history data
│           │
│           └── performance/
│               ├── bookingload.js          # K6 load test
│               ├── spiketest.js            # K6 spike test
│               └── stresstest.js           # K6 stress test
```

#### Key Features

- **Citizen Portal**
    - Create bookings
    - Track booking status using token
    - View booking history
    - Cancel bookings
- **Staff Portal**
    - View all bookings
    - Filter bookings by municipality
    - Update booking status
    - View booking history and status changes
- **Business Rules**
    - Maximum 5 bookings per municipality per time slot
    - Historical status change logging

#### Testing Strategy

1. **[Unit Tests](backend/src/test/java/zeromonos/service/BookingServiceTest.java)**:
- Tests business logic in isolation
- Mocks repository dependencies
- Validates booking creation, limits, and status management
2. **[Service Tests](backend/src/test/java/zeromonos/boundary/BookingControllerTest.java)**:
- Tests REST controller with MockMvc
- Mocks service layer
- Validates HTTP endpoints and request/response handling
3. **[Integration tests](backend/src/test/java/zeromonos/boundary/BookingControllerIT.java)**:
- Tests full API with REST-Assured
- Validates end-to-end API functionality
- Tests actual HTTP requests/responses
4. **[Functional tests](backend/src/test/java/zeromonos/functionality/WebFunctionalTest.java)**:
- Selenium WebDriver tests
- Validates user workflows through UI
- Tests role switching, form submissions, filtering

#### Performance Tests

Performance tests are implemented using K6 load testing tool

##### Running Performance Tests

1. Install [K6](https://k6.io/docs/get-started/installation/)
2. Start the application: `./start.sh`
3. Run tests:
   ```bash
   k6 run performance-tests/booking-load-test.js
   k6 run performance-tests/spike-test.js
   k6 run performance-tests/stress-test.js
   ```

##### Test Scenarios
- **Load Test**: Gradual ramp-up to 50 concurrent users
- **Spike Test**: Sudden traffic spike simulation
- **Stress Test**: Find system breaking point

#### Technologies Used
- **Frontend**:
    - HTML, CSS, JavaScript
- **Backend**:
    - Spring Boot
    - Spring Data JPA
    - PostgreSQL
- **Testing**:
    - JUnit 5
    - Mockito
    - Spring MockMvc
    - REST-Assured
    - Selenium WebDriver
    - K6 Load Testing

#### Running the Application

```bash
# Start backend and frontend
./start.sh

# Access the application
# Frontend: http://localhost:8080
# API: http://localhost:8080/api/bookings
```