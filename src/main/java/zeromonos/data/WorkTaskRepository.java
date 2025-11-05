package zeromonos.data;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface WorkTaskRepository extends JpaRepository<WorkTaskRequest, Long> {
    List<WorkTaskRequest> findByAssignedEmployee(EmployeeRequest employee);
    List<WorkTaskRequest> findByStatus(String status);
    List<WorkTaskRequest> findByBooking(BookingRequest booking);
}