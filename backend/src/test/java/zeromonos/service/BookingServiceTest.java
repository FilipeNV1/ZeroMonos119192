package zeromonos.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import zeromonos.data.BookingHistoryRepository;
import zeromonos.data.BookingHistoryRequest;
import zeromonos.data.BookingRepository;
import zeromonos.data.BookingRequest;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BookingServiceTest {

    @Mock
    private BookingRepository repository;
    @Mock
    private BookingHistoryRepository historyRepository;
    
    private BookingService service;

    @BeforeEach
    void setUp() {
        service = new BookingService(repository, historyRepository);
    }

    @Test
    void testCreateBookingSavesBooking() {
        BookingRequest booking = new BookingRequest();
        booking.setDescription("room");
        booking.setMunicipality("Aveiro");
        booking.setDate(LocalDateTime.of(2024, 6, 10, 10, 0));
        booking.setToken("TOKEN123");
        when(repository.findByMunicipality("Aveiro")).thenReturn(List.of());
        when(repository.save(any(BookingRequest.class))).thenReturn(booking);

        BookingRequest result = service.createBooking("room", "Aveiro", LocalDateTime.of(2024, 6, 10, 10, 0));

        assertThat(result.getDescription()).isEqualTo("room");
        assertThat(result.getMunicipality()).isEqualTo("Aveiro");
        assertThat(result.getDate()).isEqualTo(LocalDateTime.of(2024, 6, 10, 10, 0));
        assertThat(result.getToken()).isEqualTo("TOKEN123");
        verify(repository, times(1)).save(any(BookingRequest.class));
    }

    @Test
    void testCreateBookingThrowsExceptionWhenLimitReached() {
        LocalDateTime date = LocalDateTime.of(2025, 11, 1, 10, 0);
        List<BookingRequest> existingBookings = List.of(
            new BookingRequest("r1", "Aveiro", date, "T1"),
            new BookingRequest("r2", "Aveiro", date, "T2"),
            new BookingRequest("r3", "Aveiro", date, "T3"),
            new BookingRequest("r4", "Aveiro", date, "T4"),
            new BookingRequest("r5", "Aveiro", date, "T5")
        );
        when(repository.findByMunicipality("Aveiro")).thenReturn(existingBookings);
        assertThatThrownBy(() -> service.createBooking("room", "Aveiro", date))
            .isInstanceOf(IllegalStateException.class)
            .hasMessage("Booking limit reached for this municipality");
    }

    @Test
    void testGetAllBookingsReturnsList() {
        BookingRequest b1 = new BookingRequest();
        b1.setDescription("room");
        BookingRequest b2 = new BookingRequest();
        b2.setDescription("car");
        when(repository.findAll()).thenReturn(List.of(b1, b2));

        List<BookingRequest> bookings = service.getAllBookings();

        assertThat(bookings).hasSize(2);
        verify(repository, times(1)).findAll();
    }

    @Test
    void testGetBookingByToken() {
        BookingRequest booking = new BookingRequest();
        booking.setDescription("room");
        booking.setToken("TOKEN123");
        when(repository.findByToken("TOKEN123")).thenReturn(Optional.of(booking));

        Optional<BookingRequest> found = service.getBookingByToken("TOKEN123");

        assertThat(found).isPresent();
        assertThat(found.get().getDescription()).isEqualTo("room");
        verify(repository, times(1)).findByToken("TOKEN123");
    }

    @Test
    void testGetBookingsByMunicipality() {
        BookingRequest b1 = new BookingRequest("room", "Aveiro", LocalDateTime.now(), "T1");
        BookingRequest b2 = new BookingRequest("car", "Aveiro", LocalDateTime.now(), "T2");
        when(repository.findByMunicipality("Aveiro")).thenReturn(List.of(b1, b2));

        List<BookingRequest> bookings = service.getBookingsByMunicipality("Aveiro");

        assertThat(bookings).hasSize(2);
        assertThat(bookings.get(0).getMunicipality()).isEqualTo("Aveiro");
        assertThat(bookings.get(1).getMunicipality()).isEqualTo("Aveiro");
        verify(repository, times(1)).findByMunicipality("Aveiro");
    }

    @Test
    void testSaveBooking() {
        BookingRequest booking = new BookingRequest("room", "Aveiro", LocalDateTime.now(), "T1");
        when(repository.save(booking)).thenReturn(booking);

        BookingRequest saved = service.save(booking);

        assertThat(saved).isEqualTo(booking);
        verify(repository, times(1)).save(booking);
    }

    @Test
    void testAddStatusHistory() {
        BookingRequest booking = new BookingRequest("room", "Aveiro", LocalDateTime.now(), "T1");
        BookingHistoryRequest history = new BookingHistoryRequest(booking, "IN_PROGRESS", LocalDateTime.now());
        when(historyRepository.save(any(BookingHistoryRequest.class))).thenReturn(history);

        service.addStatusHistory(booking, "IN_PROGRESS");

        verify(historyRepository, times(1)).save(any(BookingHistoryRequest.class));
    }

    @Test
    void testGetStatusHistory() {
        BookingRequest booking = new BookingRequest("room", "Aveiro", LocalDateTime.now(), "T1");
        BookingHistoryRequest h1 = new BookingHistoryRequest(booking, "IN_PROGRESS", LocalDateTime.now());
        BookingHistoryRequest h2 = new BookingHistoryRequest(booking, "COMPLETED", LocalDateTime.now());
        when(historyRepository.findByBooking(booking)).thenReturn(List.of(h1, h2));

        List<BookingHistoryRequest> history = service.getStatusHistory(booking);

        assertThat(history).hasSize(2);
        assertThat(history.get(0).getStatus()).isEqualTo("IN_PROGRESS");
        assertThat(history.get(1).getStatus()).isEqualTo("COMPLETED");
        verify(historyRepository, times(1)).findByBooking(booking);
    }
}