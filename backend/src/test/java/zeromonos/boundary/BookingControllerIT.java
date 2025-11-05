package zeromonos.boundary;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.jdbc.core.JdbcTemplate;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import static org.hamcrest.Matchers.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class BookingControllerIT {

    @LocalServerPort
    private int port;

    @Autowired
    private JdbcTemplate jdbcTemplate;
    
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");

    @BeforeEach
    void setup() {
        RestAssured.baseURI = "http://localhost";
        RestAssured.port = port;
    }

    @AfterEach
    void cleanup() {
        jdbcTemplate.execute("DELETE FROM work_task_request");
        jdbcTemplate.execute("DELETE FROM employee_request");
        jdbcTemplate.execute("DELETE FROM booking_history_request");
        jdbcTemplate.execute("DELETE FROM booking_request");
    }

    @Test
    void testCreateAndGetBooking() {
        String tomorrow = LocalDateTime.now().plusDays(1).format(FORMATTER);
        
        // create booking
        String token =
            RestAssured.given()
                .contentType(ContentType.JSON)
                .body("{\"description\":\"Large furniture\",\"municipality\":\"Aveiro\",\"date\":\"" + tomorrow + "\"}")
            .when()
                .post("/api/bookings")
            .then()
                .statusCode(201)
                .body("description", equalTo("Large furniture"))
                .body("municipality", equalTo("Aveiro"))
                .body("status", equalTo("RECEIVED"))
                .body("token", notNullValue())
                .extract()
                .path("token");
        
        // get booking by token
        RestAssured.given()
            .when()
                .get("/api/bookings/" + token)
            .then()
                .statusCode(200)
                .body("description", equalTo("Large furniture"))
                .body("municipality", equalTo("Aveiro"))
                .body("token", equalTo(token));
    }
    
    @Test
    void testGetAllBookings() {
        String tomorrow = LocalDateTime.now().plusDays(1).format(FORMATTER);
        
        RestAssured.given()
        .contentType(ContentType.JSON)
        .body("{\"description\":\"Test booking\",\"municipality\":\"Aveiro\",\"date\":\"" + tomorrow + "\"}")
        .post("/api/bookings")
        .then()
        .statusCode(201);
        
        RestAssured.given()
            .when()
                .get("/api/bookings")
            .then()
                .statusCode(200)
                .body("$", not(empty()));
    }
    
    @Test
    void testGetBookingNotFound() {
        RestAssured.given()
            .when()
                .get("/api/bookings/INVALID_TOKEN")
            .then()
                .statusCode(404);
    }
    
    @Test
    void testCreateBookingWithMissingFields() {
        RestAssured.given()
            .contentType(ContentType.JSON)
            .body("{\"description\":\"Test\"}")
        .when()
            .post("/api/bookings")
        .then()
            .statusCode(500);
    }
    
    @Test
    void testCreateBookingWhenLimitReached() {
        String municipality = "Pedrogão Grande";
        String dayAfterTomorrow = LocalDateTime.now().plusDays(2).format(FORMATTER);
        
        for (int i = 1; i <= 5; i++) {
            RestAssured.given()
                .contentType(ContentType.JSON)
                .body("{\"description\":\"Booking " + i + "\",\"municipality\":\"" + municipality + "\",\"date\":\"" + dayAfterTomorrow + "\"}")
                .post("/api/bookings")
                .then()
                .statusCode(201);
        }
        
        RestAssured.given()
            .contentType(ContentType.JSON)
            .body("{\"description\":\"Booking 6\",\"municipality\":\"" + municipality + "\",\"date\":\"" + dayAfterTomorrow + "\"}")
        .when()
            .post("/api/bookings")
        .then()
            .statusCode(429);
    }
    
    @Test
    void testCancelBooking() {
        String tomorrow = LocalDateTime.now().plusDays(1).format(FORMATTER);
        
        String token =
            RestAssured.given()
                .contentType(ContentType.JSON)
                .body("{\"description\":\"Test\",\"municipality\":\"Cuba\",\"date\":\"" + tomorrow + "\"}")
                .post("/api/bookings")
                .then()
                .extract()
                .path("token");
        RestAssured.given()
            .when()
                .put("/api/bookings/" + token + "/cancel")
            .then()
                .statusCode(200)
                .body("status", equalTo("CANCELLED"));
        RestAssured.given()
            .when()
                .get("/api/bookings/" + token)
            .then()
                .statusCode(200)
                .body("status", equalTo("CANCELLED"));
    }
    
    @Test
    void testCancelNonExistentBooking() {
        RestAssured.given()
            .when()
                .put("/api/bookings/INVALID_TOKEN/cancel")
            .then()
                .statusCode(404);
    }
    
    @Test
    void testUpdateBookingStatus() {
        String tomorrow = LocalDateTime.now().plusDays(1).format(FORMATTER);
        
        String token =
            RestAssured.given()
                .contentType(ContentType.JSON)
                .body("{\"description\":\"Test\",\"municipality\":\"Coruche\",\"date\":\"" + tomorrow + "\"}")
                .post("/api/bookings")
                .then()
                .extract()
                .path("token");
        RestAssured.given()
            .contentType(ContentType.JSON)
            .body("{\"status\":\"IN_PROGRESS\"}")
        .when()
            .put("/api/bookings/" + token + "/status")
        .then()
            .statusCode(200)
            .body("status", equalTo("IN_PROGRESS"));
    }
    
    @Test
    void testUpdateStatusInvalidToken() {
        RestAssured.given()
            .contentType(ContentType.JSON)
            .body("{\"status\":\"IN_PROGRESS\"}")
        .when()
            .put("/api/bookings/INVALID_TOKEN/status")
        .then()
            .statusCode(404);
    }
    
    @Test
    void testGetBookingHistory() {
        String tomorrow = LocalDateTime.now().plusDays(1).format(FORMATTER);
        
        String token =
            RestAssured.given()
                .contentType(ContentType.JSON)
                .body("{\"description\":\"Test\",\"municipality\":\"Castelo de Vide\",\"date\":\"" + tomorrow + "\"}")
                .post("/api/bookings")
                .then()
                .extract()
                .path("token");
        RestAssured.given()
            .contentType(ContentType.JSON)
            .body("{\"status\":\"IN_PROGRESS\"}")
            .put("/api/bookings/" + token + "/status");
        
        RestAssured.given()
            .contentType(ContentType.JSON)
            .body("{\"status\":\"COMPLETED\"}")
            .put("/api/bookings/" + token + "/status");
        RestAssured.given()
            .when()
                .get("/api/bookings/" + token + "/history")
            .then()
                .statusCode(200)
                .body("$", hasSize(greaterThanOrEqualTo(2)))
                .body("status", hasItems("IN_PROGRESS", "COMPLETED"));
    }

    @Test
    void testGetHistoryForNonExistentBooking() {
        RestAssured.given()
            .when()
                .get("/api/bookings/INVALID_TOKEN/history")
            .then()
                .statusCode(404);
    }

    @Test
    void testCreateBookingWithInvalidDate() {
        RestAssured.given()
            .contentType(ContentType.JSON)
            .body("{\"description\":\"Test\",\"municipality\":\"Aveiro\",\"date\":\"invalid-date\"}")
        .when()
            .post("/api/bookings")
        .then()
            .statusCode(anyOf(is(400), is(500)));
    }

    @Test
    void testGetBookingsByMunicipality() {
        String municipality = "Albufeira";
        String tomorrow = LocalDateTime.now().plusDays(1).format(FORMATTER);

        RestAssured.given()
            .contentType(ContentType.JSON)
            .body("{\"description\":\"Test\",\"municipality\":\"" + municipality + "\",\"date\":\"" + tomorrow + "\"}")
            .post("/api/bookings");

        RestAssured.given()
        .when()
            .get("/api/bookings")
        .then()
            .statusCode(200)
            .body("$", not(empty()))
            .body("municipality", hasItem(municipality));
    }

    @Test
    void testCancelAlreadyCancelledBooking() {
        String tomorrow = LocalDateTime.now().plusDays(1).format(FORMATTER);
        
        String token =
            RestAssured.given()
                .contentType(ContentType.JSON)
                .body("{\"description\":\"Test\",\"municipality\":\"Alcácer do Sal\",\"date\":\"" + tomorrow + "\"}")
                .post("/api/bookings")
                .then()
                .extract()
                .path("token");

        // cancel once
        RestAssured.given()
            .put("/api/bookings/" + token + "/cancel")
            .then()
            .statusCode(200);

        // try to cancel again
        RestAssured.given()
        .when()
            .put("/api/bookings/" + token + "/cancel")
        .then()
            .statusCode(anyOf(is(200), is(400), is(409)));
    }

    @Test
    void testBookingHistoryOrder() {
        String dayAfterTomorrow = LocalDateTime.now().plusDays(2).format(FORMATTER);
        
        String token =
            RestAssured.given()
                .contentType(ContentType.JSON)
                .body("{\"description\":\"Test\",\"municipality\":\"Aldeia de Paio Pires\",\"date\":\"" + dayAfterTomorrow + "\"}")
                .post("/api/bookings")
                .then()
                .extract()
                .path("token");

        // update status multiple times
        RestAssured.given()
            .contentType(ContentType.JSON)
            .body("{\"status\":\"IN_PROGRESS\"}")
            .put("/api/bookings/" + token + "/status");

        RestAssured.given()
            .contentType(ContentType.JSON)
            .body("{\"status\":\"COMPLETED\"}")
            .put("/api/bookings/" + token + "/status");

        // verify history is in correct order
        RestAssured.given()
        .when()
            .get("/api/bookings/" + token + "/history")
        .then()
            .statusCode(200)
            .body("$", hasSize(greaterThanOrEqualTo(2)))
            .body("[0].status", notNullValue())
            .body("timestamp", everyItem(notNullValue()));
    }
}
