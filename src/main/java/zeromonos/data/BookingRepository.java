package zeromonos.data;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import java.util.List;

public interface BookingRepository extends JpaRepository<BookingRequest, Long> {
    Optional<BookingRequest> findByToken(String token);
    List<BookingRequest> findByMunicipality(String municipality);
}