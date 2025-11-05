package zeromonos.data;

import org.junit.jupiter.api.Test;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class BookingHistoryRequestTest {

    @Test
    void testDefaultConstructor() {
        BookingHistoryRequest history = new BookingHistoryRequest();
        assertThat(history).isNotNull();
        assertThat(history.getId()).isNull();
        assertThat(history.getStatus()).isNull();
        assertThat(history.getTimestamp()).isNull();
        assertThat(history.getBooking()).isNull();
    }

    @Test
    void testParameterizedConstructor() {
        BookingRequest booking = new BookingRequest("Test", "Aveiro", LocalDateTime.now(), "TOKEN123");
        LocalDateTime timestamp = LocalDateTime.now();
        
        BookingHistoryRequest history = new BookingHistoryRequest(booking, "IN_PROGRESS", timestamp);
        
        assertThat(history.getBooking()).isEqualTo(booking);
        assertThat(history.getStatus()).isEqualTo("IN_PROGRESS");
        assertThat(history.getTimestamp()).isEqualTo(timestamp);
    }

    @Test
    void testSettersAndGetters() {
        BookingHistoryRequest history = new BookingHistoryRequest();
        BookingRequest booking = new BookingRequest("Test", "Porto", LocalDateTime.now(), "TOKEN456");
        LocalDateTime timestamp = LocalDateTime.of(2025, 11, 2, 10, 30);
        
        history.setId(1L);
        history.setStatus("COMPLETED");
        history.setTimestamp(timestamp);
        history.setBooking(booking);
        
        assertThat(history.getId()).isEqualTo(1L);
        assertThat(history.getStatus()).isEqualTo("COMPLETED");
        assertThat(history.getTimestamp()).isEqualTo(timestamp);
        assertThat(history.getBooking()).isEqualTo(booking);
    }

    @Test
    void testSetStatusNull() {
        BookingHistoryRequest history = new BookingHistoryRequest();
        history.setStatus(null);
        assertThat(history.getStatus()).isNull();
    }

    @Test
    void testSetBookingNull() {
        BookingHistoryRequest history = new BookingHistoryRequest();
        history.setBooking(null);
        assertThat(history.getBooking()).isNull();
    }
}