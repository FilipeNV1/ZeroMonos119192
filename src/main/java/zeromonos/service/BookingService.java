package zeromonos.service;

import org.springframework.stereotype.Service;

import zeromonos.data.BookingHistoryRepository;
import zeromonos.data.BookingHistoryRequest;
import zeromonos.data.BookingRepository;
import zeromonos.data.BookingRequest;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class BookingService {

    private BookingRepository repository;
    private BookingHistoryRepository historyRepository;

    public BookingService(BookingRepository repository, BookingHistoryRepository historyRepository) {
        this.repository = repository;
        this.historyRepository = historyRepository;
    }

    public BookingRequest createBooking(String description, String municipality, LocalDateTime date) {
        long count = repository.findByMunicipality(municipality).stream()
        .filter(b -> b.getDate().toLocalDate().equals(date.toLocalDate()))
        .count();
        if (count >= 5) {
            throw new IllegalStateException("Booking limit reached for this municipality");
        }
        String token = UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        BookingRequest booking = new BookingRequest(description, municipality, date, token);
        return repository.save(booking);
    }
    public Optional<BookingRequest> getBookingByToken(String token) {
        return repository.findByToken(token);
    }
    public List<BookingRequest> getAllBookings() {
        return repository.findAll();
    }
    public List<BookingRequest> getBookingsByMunicipality(String municipality) {
        return repository.findByMunicipality(municipality);
    }
    public BookingRequest save(BookingRequest booking) {
        addStatusHistory(booking, booking.getStatus());
        return repository.save(booking);
    }
    public void addStatusHistory(BookingRequest booking, String status) {
        BookingHistoryRequest history = new BookingHistoryRequest(booking, status, LocalDateTime.now());
        historyRepository.save(history);
    }
    public List<BookingHistoryRequest> getStatusHistory(BookingRequest booking) {
        return historyRepository.findByBooking(booking);
    }
}