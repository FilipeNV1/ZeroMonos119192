package zeromonos.data;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
public class BookingHistoryRequest {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String status;
    private LocalDateTime timestamp;

    @ManyToOne
    private BookingRequest booking;

    public BookingHistoryRequest() {}
    public BookingHistoryRequest(BookingRequest booking, String status, LocalDateTime timestamp) {
        this.booking = booking;
        this.status = status;
        this.timestamp = timestamp;
    }
    public Long getId() {
        return id;
    }
    public void setId(Long id) {
        this.id = id;
    }
    public String getStatus() {
        return status;
    }
    public void setStatus(String status) {
        this.status = status;
    }
    public LocalDateTime getTimestamp() {
        return timestamp;
    }
    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }
    public BookingRequest getBooking() {
        return booking;
    }
    public void setBooking(BookingRequest booking) {
        this.booking = booking;
    }
}
