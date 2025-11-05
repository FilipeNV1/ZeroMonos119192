package zeromonos.boundary;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import zeromonos.data.BookingHistoryRequest;
import zeromonos.data.BookingRequest;
import zeromonos.service.BookingService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/api/bookings")
public class BookingController {

    private final BookingService bookingService;

    public BookingController(BookingService bookingService) {
        this.bookingService = bookingService;
    }

    private static final Logger logger = LoggerFactory.getLogger(BookingController.class);

    // POST /api/bookings - Request a new booking
    @PostMapping
    public ResponseEntity<BookingRequest> createBooking(@RequestBody Map<String, String> request) {
        String description = request.get("description");
        String municipality = request.get("municipality");
        LocalDateTime date = LocalDateTime.parse(request.get("date"));
        if (description == null || municipality == null || date == null) {
            return ResponseEntity.badRequest().build();
        }
        logger.info("Received booking request for municipality {}", municipality);
        try {
            BookingRequest booking = bookingService.createBooking(description, municipality, date);
            logger.info("Booking created with token {}", booking.getToken());
            return ResponseEntity.status(HttpStatus.CREATED).body(booking);
        } catch (IllegalStateException e) {
            logger.warn("Booking limit reached for municipality {} on date {}", municipality, date);
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).build();
        }
    }

    // GET /api/bookings - Get all bookings
    @GetMapping
    public ResponseEntity<List<BookingRequest>> getAllBookings() {
        logger.info("Received request to see all bookings");
        List<BookingRequest> bookings = bookingService.getAllBookings();
        return ResponseEntity.ok(bookings);
    }

    // GET /api/bookings - Get booking with a specific token
    @GetMapping("/{token}")
    public ResponseEntity<BookingRequest> getBooking(@PathVariable String token) {
        logger.info("Received request to see booking with token {}", token);
        Optional<BookingRequest> booking = bookingService.getBookingByToken(token);
        return booking.map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }

    // PUT /api/bookings/{token}/cancel - Cancel booking
    @PutMapping("/{token}/cancel")
    public ResponseEntity<BookingRequest> cancelBooking(@PathVariable String token) {
        Optional<BookingRequest> bookingOpt = bookingService.getBookingByToken(token);
        if (bookingOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        BookingRequest booking = bookingOpt.get();
        if ("CANCELLED".equals(booking.getStatus())) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(booking);
        }
        logger.info("Received request to cancel booking with token {}", token);
        booking.setStatus("CANCELLED");
        bookingService.save(booking);
        return ResponseEntity.ok(booking);
    }

    // PUT /api/bookings/{token}/status - Staff updates booking status
    @PutMapping("/{token}/status")
    public ResponseEntity<BookingRequest> updateStatus(@PathVariable String token, @RequestBody Map<String, String> body) {
        Optional<BookingRequest> bookingOpt = bookingService.getBookingByToken(token);
        if (bookingOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        BookingRequest booking = bookingOpt.get();
        String newStatus = body.get("status");
        if (newStatus == null) {
            return ResponseEntity.badRequest().build();
        }
        String oldStatus = booking.getStatus();
        booking.setStatus(newStatus);
        bookingService.save(booking);
        logger.info("Received request to update booking with token {} from {} to {}", token, oldStatus, newStatus);
        return ResponseEntity.ok(booking);
    }

    // PUT /api/bookings/{token}/history - Check booking status history
    @GetMapping("/{token}/history")
    public ResponseEntity<List<BookingHistoryRequest>> getBookingHistory(@PathVariable String token) {
        Optional<BookingRequest> booking = bookingService.getBookingByToken(token);
        if (booking.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        logger.info("Received request to check the history of booking with token {}", token);
        List<BookingHistoryRequest> history = bookingService.getStatusHistory(booking.get());
        return ResponseEntity.ok(history);
    }
}