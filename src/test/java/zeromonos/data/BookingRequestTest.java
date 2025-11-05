package zeromonos.data;

import org.junit.jupiter.api.Test;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class BookingRequestTest {

    @Test
    void testDefaultConstructor() {
        BookingRequest booking = new BookingRequest();
        assertThat(booking).isNotNull();
    }

    @Test
    void testParameterizedConstructor() {
        LocalDateTime date = LocalDateTime.of(2025, 11, 2, 10, 0);
        BookingRequest booking = new BookingRequest("Test", "Aveiro", date, "TOKEN123");
        
        assertThat(booking.getDescription()).isEqualTo("Test");
        assertThat(booking.getMunicipality()).isEqualTo("Aveiro");
        assertThat(booking.getDate()).isEqualTo(date);
        assertThat(booking.getToken()).isEqualTo("TOKEN123");
    }

    @Test
    void testAllSettersAndGetters() {
        BookingRequest booking = new BookingRequest();
        LocalDateTime date = LocalDateTime.of(2025, 12, 25, 15, 30);
        
        booking.setId(1L);
        booking.setDescription("Large furniture");
        booking.setMunicipality("Porto");
        booking.setDate(date);
        booking.setStatus("IN_PROGRESS");
        booking.setToken("TOKEN456");
        
        assertThat(booking.getId()).isEqualTo(1L);
        assertThat(booking.getDescription()).isEqualTo("Large furniture");
        assertThat(booking.getMunicipality()).isEqualTo("Porto");
        assertThat(booking.getDate()).isEqualTo(date);
        assertThat(booking.getStatus()).isEqualTo("IN_PROGRESS");
        assertThat(booking.getToken()).isEqualTo("TOKEN456");
    }
}