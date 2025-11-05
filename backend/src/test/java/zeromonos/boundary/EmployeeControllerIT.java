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

import static org.hamcrest.Matchers.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class EmployeeControllerIT {

    @LocalServerPort
    private int port;

    @Autowired
    private JdbcTemplate jdbcTemplate;

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

    // create employee
    @Test
    void testCreateEmployee() {
        RestAssured.given()
            .contentType(ContentType.JSON)
            .body("{\"name\":\"John Doe\",\"email\":\"john@example.com\",\"municipality\":\"Aveiro\",\"role\":\"COLLECTOR\"}")
        .when()
            .post("/api/employees")
        .then()
            .statusCode(201)
            .body("name", equalTo("John Doe"))
            .body("email", equalTo("john@example.com"))
            .body("municipality", equalTo("Aveiro"))
            .body("role", equalTo("COLLECTOR"))
            .body("id", notNullValue());
    }

    // get all employees
    @Test
    void testGetAllEmployees() {
        // create employees
        RestAssured.given()
            .contentType(ContentType.JSON)
            .body("{\"name\":\"John Doe\",\"email\":\"john@example.com\",\"municipality\":\"Aveiro\",\"role\":\"COLLECTOR\"}")
            .post("/api/employees");

        RestAssured.given()
            .contentType(ContentType.JSON)
            .body("{\"name\":\"Jane Smith\",\"email\":\"jane@example.com\",\"municipality\":\"Porto\",\"role\":\"DRIVER\"}")
            .post("/api/employees");

        // get all
        RestAssured.given()
        .when()
            .get("/api/employees")
        .then()
            .statusCode(200)
            .body("$", hasSize(greaterThanOrEqualTo(2)))
            .body("name", hasItems("John Doe", "Jane Smith"));
    }

    // get employee by id
    @Test
    void testGetEmployeeById() {
        Integer employeeIdInt = RestAssured.given()
            .contentType(ContentType.JSON)
            .body("{\"name\":\"Test Employee\",\"email\":\"test@example.com\",\"municipality\":\"Aveiro\",\"role\":\"SUPERVISOR\"}")
            .post("/api/employees")
            .then()
            .extract()
            .path("id");

        Long employeeId = Long.valueOf(employeeIdInt);

        RestAssured.given()
        .when()
            .get("/api/employees/" + employeeId)
        .then()
            .statusCode(200)
            .body("name", equalTo("Test Employee"))
            .body("role", equalTo("SUPERVISOR"));
    }

    // non-existent
    @Test
    void testGetNonExistentEmployee() {
        RestAssured.given()
        .when()
            .get("/api/employees/999")
        .then()
            .statusCode(404);
    }

    // by municipality
    @Test
    void testGetEmployeesByMunicipality() {
        // create employee
        RestAssured.given()
            .contentType(ContentType.JSON)
            .body("{\"name\":\"Aveiro Worker 1\",\"email\":\"worker1@example.com\",\"municipality\":\"Aveiro\",\"role\":\"COLLECTOR\"}")
            .post("/api/employees");

        RestAssured.given()
            .contentType(ContentType.JSON)
            .body("{\"name\":\"Aveiro Worker 2\",\"email\":\"worker2@example.com\",\"municipality\":\"Aveiro\",\"role\":\"DRIVER\"}")
            .post("/api/employees");

        RestAssured.given()
            .contentType(ContentType.JSON)
            .body("{\"name\":\"Porto Worker\",\"email\":\"porto@example.com\",\"municipality\":\"Porto\",\"role\":\"COLLECTOR\"}")
            .post("/api/employees");

        RestAssured.given()
        .when()
            .get("/api/employees/municipality/Aveiro")
        .then()
            .statusCode(200)
            .body("$", hasSize(greaterThanOrEqualTo(2)))
            .body("municipality", everyItem(equalTo("Aveiro")));
    }

    // no results from municipality
    @Test
    void testGetEmployeesByMunicipalityWithNoResults() {
        RestAssured.given()
        .when()
            .get("/api/employees/municipality/NonExistentCity")
        .then()
            .statusCode(200)
            .body("$", hasSize(0));
    }

    // create multiple employees
    @Test
    void testCreateMultipleEmployeesWithDifferentRoles() {
        String[] roles = {"COLLECTOR", "DRIVER", "SUPERVISOR", "ADMIN"};

        for (String role : roles) {
            RestAssured.given()
                .contentType(ContentType.JSON)
                .body("{\"name\":\"Employee " + role + "\",\"email\":\"" + role.toLowerCase() + "@example.com\",\"municipality\":\"Aveiro\",\"role\":\"" + role + "\"}")
                .post("/api/employees")
                .then()
                .statusCode(201)
                .body("role", equalTo(role));
        }

        RestAssured.given()
        .when()
            .get("/api/employees")
        .then()
            .statusCode(200)
            .body("$", hasSize(greaterThanOrEqualTo(4)));
    }
}