package zeromonos.data;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface BookingHistoryRepository extends JpaRepository<BookingHistoryRequest, Long> {
    List<BookingHistoryRequest> findByBooking(BookingRequest booking);
}