package zeromonos.data;

import org.junit.jupiter.api.Test;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class WorkRequestTest {

    @Test
    void testDefaultConstructor() {
        WorkTaskRequest work = new WorkTaskRequest();
        assertThat(work).isNotNull();
    }

    @Test
    void testParameterizedConstructor() {
        BookingRequest booking = new BookingRequest("Test", "Aveiro", LocalDateTime.now(), "TOKEN123");
        EmployeeRequest employee = new EmployeeRequest("John Doe", "john@example.com", "Aveiro", "COLLECTOR");
        
        WorkTaskRequest work = new WorkTaskRequest(booking, employee);
        
        assertThat(work.getBooking()).isEqualTo(booking);
        assertThat(work.getAssignedEmployee()).isEqualTo(employee);
        assertThat(work.getStatus()).isEqualTo("ASSIGNED");
        assertThat(work.getAssignedAt()).isNotNull();
    }

    @Test
    void testAllSettersAndGetters() {
        WorkTaskRequest work = new WorkTaskRequest();
        BookingRequest booking = new BookingRequest("Test", "Porto", LocalDateTime.now(), "TOKEN456");
        EmployeeRequest employee = new EmployeeRequest("Jane Smith", "jane@example.com", "Porto", "DRIVER");
        LocalDateTime assignedAt = LocalDateTime.of(2025, 12, 25, 10, 0);
        LocalDateTime completedAt = LocalDateTime.of(2025, 12, 25, 15, 0);
        
        work.setId(1L);
        work.setBooking(booking);
        work.setAssignedEmployee(employee);
        work.setStatus("COMPLETED");
        work.setAssignedAt(assignedAt);
        work.setCompletedAt(completedAt);
        work.setNotes("Task completed successfully");
        
        assertThat(work.getId()).isEqualTo(1L);
        assertThat(work.getBooking()).isEqualTo(booking);
        assertThat(work.getAssignedEmployee()).isEqualTo(employee);
        assertThat(work.getStatus()).isEqualTo("COMPLETED");
        assertThat(work.getAssignedAt()).isEqualTo(assignedAt);
        assertThat(work.getCompletedAt()).isEqualTo(completedAt);
        assertThat(work.getNotes()).isEqualTo("Task completed successfully");
    }

    @Test
    void testSetStatusNull() {
        WorkTaskRequest work = new WorkTaskRequest();
        work.setStatus(null);
        assertThat(work.getStatus()).isNull();
    }

    @Test
    void testSetNotesNull() {
        WorkTaskRequest work = new WorkTaskRequest();
        work.setNotes(null);
        assertThat(work.getNotes()).isNull();
    }
}