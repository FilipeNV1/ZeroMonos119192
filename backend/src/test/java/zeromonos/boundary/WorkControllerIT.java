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
class WorkControllerIT {

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

    // create task
    @Test
    void testCreateAndAssignTask() {
        String tomorrow = LocalDateTime.now().plusDays(1).format(FORMATTER);
        
        // create booking
        String bookingToken =
            RestAssured.given()
                .contentType(ContentType.JSON)
                .body("{\"description\":\"Test task\",\"municipality\":\"Aveiro\",\"date\":\"" + tomorrow + "\"}")
            .when()
                .post("/api/bookings")
            .then()
                .statusCode(201)
                .extract()
                .path("token");

        // create employee
        Integer employeeIdInt =
            RestAssured.given()
                .contentType(ContentType.JSON)
                .body("{\"name\":\"John Doe\",\"email\":\"john@example.com\",\"municipality\":\"Aveiro\",\"role\":\"COLLECTOR\"}")
            .when()
                .post("/api/employees")
            .then()
                .statusCode(201)
                .extract()
                .path("id");
        Long employeeId = employeeIdInt.longValue();

        // assign task
        RestAssured.given()
            .contentType(ContentType.JSON)
            .body("{\"bookingToken\":\"" + bookingToken + "\",\"employeeId\":" + employeeId + "}")
        .when()
            .post("/api/tasks")
        .then()
            .statusCode(201)
            .body("status", anyOf(equalTo("PENDING"), equalTo("ASSIGNED")))
            .body("assignedAt", notNullValue());
    }

    // get all tasks
    @Test
    void testGetAllTasks() {
        String tomorrow = LocalDateTime.now().plusDays(1).format(FORMATTER);
        
        // create booking and employee
        String bookingToken =
            RestAssured.given()
                .contentType(ContentType.JSON)
                .body("{\"description\":\"Test\",\"municipality\":\"Aveiro\",\"date\":\"" + tomorrow + "\"}")
                .post("/api/bookings")
                .then()
                .extract()
                .path("token");

        Integer employeeIdInt =
            RestAssured.given()
                .contentType(ContentType.JSON)
                .body("{\"name\":\"Jane\",\"email\":\"jane@example.com\",\"municipality\":\"Aveiro\",\"role\":\"DRIVER\"}")
                .post("/api/employees")
                .then()
                .extract()
                .path("id");
        Long employeeId = ((Number) employeeIdInt).longValue();


        // assign task
        RestAssured.given()
            .contentType(ContentType.JSON)
            .body("{\"bookingToken\":\"" + bookingToken + "\",\"employeeId\":" + employeeId + "}")
            .post("/api/tasks");

        // get all tasks
        RestAssured.given()
        .when()
            .get("/api/tasks")
        .then()
            .statusCode(200)
            .body("$", not(empty()));
    }

    // complete task
    @Test
    void testCompleteTask() {
        String dayAfterTomorrow = LocalDateTime.now().plusDays(2).format(FORMATTER);
        
        // setup
        String bookingToken =
            RestAssured.given()
                .contentType(ContentType.JSON)
                .body("{\"description\":\"Complete test\",\"municipality\":\"Porto\",\"date\":\"" + dayAfterTomorrow + "\"}")
                .post("/api/bookings")
                .then()
                .extract()
                .path("token");

        Integer employeeIdInt =
            RestAssured.given()
                .contentType(ContentType.JSON)
                .body("{\"name\":\"Worker\",\"email\":\"worker@example.com\",\"municipality\":\"Porto\",\"role\":\"COLLECTOR\"}")
                .post("/api/employees")
                .then()
                .extract()
                .path("id");
        Long employeeId = Long.valueOf(employeeIdInt);

        Integer taskIdInt =
            RestAssured.given()
                .contentType(ContentType.JSON)
                .body("{\"bookingToken\":\"" + bookingToken + "\",\"employeeId\":" + employeeId + "}")
                .post("/api/tasks")
                .then()
                .statusCode(201)
                .extract()
                .path("id");
        Long taskId = Long.valueOf(taskIdInt);


        // complete task
        RestAssured.given()
            .contentType(ContentType.JSON)
            .body("{\"notes\":\"Work done successfully\"}")
        .when()
            .put("/api/tasks/" + taskId + "/complete")
        .then()
            .statusCode(200)
            .body("status", equalTo("COMPLETED"))
            .body("completedAt", notNullValue())
            .body("notes", equalTo("Work done successfully"));
    }

    // non-existent task
    @Test
    void testCompleteNonExistentTask() {
        RestAssured.given()
            .contentType(ContentType.JSON)
            .body("{\"notes\":\"Test\"}")
        .when()
            .put("/api/tasks/999/complete")
        .then()
            .statusCode(404);
    }

    // tasks by employee
    @Test
    void testGetTasksByEmployee() {
        String tomorrow = LocalDateTime.now().plusDays(1).format(FORMATTER);
        
        // create employee
        Integer employeeIdInt =
            RestAssured.given()
                .contentType(ContentType.JSON)
                .body("{\"name\":\"Test Employee\",\"email\":\"test@example.com\",\"municipality\":\"Aveiro\",\"role\":\"SUPERVISOR\"}")
                .post("/api/employees")
                .then()
                .extract()
                .path("id");
        Long employeeId = ((Number) employeeIdInt).longValue();

        // create booking and assign
        String bookingToken =
            RestAssured.given()
                .contentType(ContentType.JSON)
                .body("{\"description\":\"Employee task\",\"municipality\":\"Aveiro\",\"date\":\"" + tomorrow + "\"}")
                .post("/api/bookings")
                .then()
                .extract()
                .path("token");

        RestAssured.given()
            .contentType(ContentType.JSON)
            .body("{\"bookingToken\":\"" + bookingToken + "\",\"employeeId\":" + employeeId + "}")
            .post("/api/tasks");

        // get tasks by employee
        RestAssured.given()
        .when()
            .get("/api/tasks/employee/" + employeeId)
        .then()
            .statusCode(200)
            .body("$", hasSize(greaterThanOrEqualTo(1)));
    }

    // invalid booking
    @Test
    void testAssignTaskWithInvalidBooking() {
        Integer employeeIdInt =
            RestAssured.given()
                .contentType(ContentType.JSON)
                .body("{\"name\":\"Worker\",\"email\":\"worker@example.com\",\"municipality\":\"Aveiro\",\"role\":\"COLLECTOR\"}")
                .post("/api/employees")
                .then()
                .extract()
                .path("id");
        Long employeeId = ((Number) employeeIdInt).longValue();

        RestAssured.given()
            .contentType(ContentType.JSON)
            .body("{\"bookingToken\":\"INVALID\",\"employeeId\":" + employeeId + "}")
        .when()
            .post("/api/tasks")
        .then()
            .statusCode(anyOf(is(400), is(404), is(500)));
    }

    // invalid employee
    @Test
    void testAssignTaskWithInvalidEmployee() {
        String tomorrow = LocalDateTime.now().plusDays(1).format(FORMATTER);
        
        String bookingToken =
            RestAssured.given()
                .contentType(ContentType.JSON)
                .body("{\"description\":\"Test\",\"municipality\":\"Aveiro\",\"date\":\"" + tomorrow + "\"}")
                .post("/api/bookings")
                .then()
                .extract()
                .path("token");

        RestAssured.given()
            .contentType(ContentType.JSON)
            .body("{\"bookingToken\":\"" + bookingToken + "\",\"employeeId\":999}")
        .when()
            .post("/api/tasks")
        .then()
            .statusCode(anyOf(is(400), is(404), is(500)));
    }

    // by id
    @Test
    void testGetTaskById() {
        String dayAfterTomorrow = LocalDateTime.now().plusDays(2).format(FORMATTER);
        
        // setup
        String bookingToken =
            RestAssured.given()
                .contentType(ContentType.JSON)
                .body("{\"description\":\"Get task test\",\"municipality\":\"Lisboa\",\"date\":\"" + dayAfterTomorrow + "\"}")
                .post("/api/bookings")
                .then()
                .extract()
                .path("token");

        Integer employeeIdInt =
            RestAssured.given()
                .contentType(ContentType.JSON)
                .body("{\"name\":\"Tasker\",\"email\":\"tasker@example.com\",\"municipality\":\"Lisboa\",\"role\":\"DRIVER\"}")
                .post("/api/employees")
                .then()
                .extract()
                .path("id");
        Long employeeId = ((Number) employeeIdInt).longValue();

        Integer taskIdInt =
            RestAssured.given()
                .contentType(ContentType.JSON)
                .body("{\"bookingToken\":\"" + bookingToken + "\",\"employeeId\":" + employeeId + "}")
                .post("/api/tasks")
                .then()
                .statusCode(201)
                .extract()
                .path("id");
        Long taskId = Long.valueOf(taskIdInt);

        // get task
        RestAssured.given()
        .when()
            .get("/api/tasks/" + taskId)
        .then()
            .statusCode(200)
            .body("id", equalTo(taskId.intValue()))
            .body("status", anyOf(equalTo("PENDING"), equalTo("ASSIGNED")));
    }

    // non-existent id
    @Test
    void testGetNonExistentTaskById() {
        RestAssured.given()
        .when()
            .get("/api/tasks/999")
        .then()
            .statusCode(404);
    }
}