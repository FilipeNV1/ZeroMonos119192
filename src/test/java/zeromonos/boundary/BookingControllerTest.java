package zeromonos.boundary;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import zeromonos.data.BookingHistoryRequest;
import zeromonos.data.BookingRequest;
import zeromonos.service.BookingService;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.hamcrest.Matchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(BookingController.class)
class BookingControllerTest {
        
    @Autowired
    private MockMvc mvc;

    @MockitoBean
    private BookingService bookingService;

    @Test
    void whenPostBooking_thenCreateBooking() throws Exception {
        BookingRequest booking = new BookingRequest("room", "Aveiro", LocalDateTime.of(2024, 6, 10, 10, 0), "TOKEN123");

        when(bookingService.createBooking(Mockito.anyString(), Mockito.anyString(), Mockito.any(LocalDateTime.class)))
                .thenReturn(booking);

        mvc.perform(
                post("/api/bookings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"description\":\"room\",\"municipality\":\"Aveiro\",\"date\":\"2024-06-10T10:00:00\"}")
        )
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.description", is("room")))
                .andExpect(jsonPath("$.municipality", is("Aveiro")))
                .andExpect(jsonPath("$.date", is("2024-06-10T10:00:00")))
                .andExpect(jsonPath("$.token", is("TOKEN123")));

        verify(bookingService, times(1)).createBooking(Mockito.anyString(), Mockito.anyString(), Mockito.any(LocalDateTime.class));
    }

    @Test
    void givenManyBookings_whenGetBookings_thenReturnJsonArray() throws Exception {
        BookingRequest b1 = new BookingRequest("room", "Aveiro", LocalDateTime.of(2024, 6, 10, 10, 0), "T1");
        BookingRequest b2 = new BookingRequest("car", "Porto", LocalDateTime.of(2024, 6, 11, 11, 0), "T2");
        List<BookingRequest> allBookings = Arrays.asList(b1, b2);

        when(bookingService.getAllBookings()).thenReturn(allBookings);

        mvc.perform(
                get("/api/bookings").contentType(MediaType.APPLICATION_JSON)
        )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].description", is("room")))
                .andExpect(jsonPath("$[1].description", is("car")));

        verify(bookingService, times(1)).getAllBookings();
    }

    @Test
    void whenGetBookingByToken_thenReturnBooking() throws Exception {
        BookingRequest booking = new BookingRequest("room", "Aveiro", LocalDateTime.of(2024, 6, 10, 10, 0), "TOKEN123");
        when(bookingService.getBookingByToken("TOKEN123")).thenReturn(Optional.of(booking));

        mvc.perform(get("/api/bookings/TOKEN123"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token", is("TOKEN123")))
                .andExpect(jsonPath("$.description", is("room")));

        verify(bookingService, times(1)).getBookingByToken("TOKEN123");
    }

    @Test
    void whenGetBookingByInvalidToken_thenReturn404() throws Exception {
        when(bookingService.getBookingByToken("INVALID")).thenReturn(Optional.empty());

        mvc.perform(get("/api/bookings/INVALID"))
                .andExpect(status().isNotFound());

        verify(bookingService, times(1)).getBookingByToken("INVALID");
    }

    @Test
    void whenCancelBooking_thenReturnCancelledBooking() throws Exception {
        BookingRequest booking = new BookingRequest("room", "Aveiro", LocalDateTime.of(2024, 6, 10, 10, 0), "TOKEN123");
        BookingRequest cancelledBooking = new BookingRequest("room", "Aveiro", LocalDateTime.of(2024, 6, 10, 10, 0), "TOKEN123");
        cancelledBooking.setStatus("CANCELLED");
    
        when(bookingService.getBookingByToken("TOKEN123")).thenReturn(Optional.of(booking));
        when(bookingService.save(Mockito.any(BookingRequest.class))).thenReturn(cancelledBooking);
    
        mvc.perform(put("/api/bookings/TOKEN123/cancel"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is("CANCELLED")));
    
        verify(bookingService, times(1)).getBookingByToken("TOKEN123");
        verify(bookingService, times(1)).save(Mockito.any(BookingRequest.class));
    }

    @Test
    void whenCancelNonExistentBooking_thenReturn404() throws Exception {
        when(bookingService.getBookingByToken("INVALID")).thenReturn(Optional.empty());

        mvc.perform(put("/api/bookings/INVALID/cancel"))
                .andExpect(status().isNotFound());

        verify(bookingService, times(1)).getBookingByToken("INVALID");
    }

    @Test
    void whenUpdateStatus_thenReturnUpdatedBooking() throws Exception {
        BookingRequest booking = new BookingRequest("room", "Aveiro", LocalDateTime.of(2024, 6, 10, 10, 0), "TOKEN123");
        BookingRequest updatedBooking = new BookingRequest("room", "Aveiro", LocalDateTime.of(2024, 6, 10, 10, 0), "TOKEN123");
        updatedBooking.setStatus("IN_PROGRESS");
    
        when(bookingService.getBookingByToken("TOKEN123")).thenReturn(Optional.of(booking));
        when(bookingService.save(Mockito.any(BookingRequest.class))).thenReturn(updatedBooking);

        mvc.perform(put("/api/bookings/TOKEN123/status")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("{\"status\":\"IN_PROGRESS\"}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.status", is("IN_PROGRESS")));

        verify(bookingService, times(1)).getBookingByToken("TOKEN123");
        verify(bookingService, times(1)).save(Mockito.any(BookingRequest.class));
    }

    @Test
    void whenUpdateStatusForNonExistentBooking_thenReturn404() throws Exception {
        when(bookingService.getBookingByToken("INVALID")).thenReturn(Optional.empty());

        mvc.perform(put("/api/bookings/INVALID/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\":\"IN_PROGRESS\"}"))
                .andExpect(status().isNotFound());

        verify(bookingService, times(1)).getBookingByToken("INVALID");
    }

    @Test
    void whenGetHistory_thenReturnHistoryList() throws Exception {
        BookingRequest booking = new BookingRequest("room", "Aveiro", LocalDateTime.of(2024, 6, 10, 10, 0), "TOKEN123");
        BookingHistoryRequest h1 = new BookingHistoryRequest(booking, "IN_PROGRESS", LocalDateTime.now());
        BookingHistoryRequest h2 = new BookingHistoryRequest(booking, "COMPLETED", LocalDateTime.now());
        
        when(bookingService.getBookingByToken("TOKEN123")).thenReturn(Optional.of(booking));
        when(bookingService.getStatusHistory(booking)).thenReturn(List.of(h1, h2));

        mvc.perform(get("/api/bookings/TOKEN123/history"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].status", is("IN_PROGRESS")))
                .andExpect(jsonPath("$[1].status", is("COMPLETED")));

        verify(bookingService, times(1)).getBookingByToken("TOKEN123");
        verify(bookingService, times(1)).getStatusHistory(booking);
    }

    @Test
    void whenGetHistoryForNonExistentBooking_thenReturn404() throws Exception {
        when(bookingService.getBookingByToken("INVALID")).thenReturn(Optional.empty());

        mvc.perform(get("/api/bookings/INVALID/history"))
                .andExpect(status().isNotFound());

        verify(bookingService, times(1)).getBookingByToken("INVALID");
    }
}