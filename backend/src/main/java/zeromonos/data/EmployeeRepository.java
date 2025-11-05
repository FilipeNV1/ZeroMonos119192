package zeromonos.data;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface EmployeeRepository extends JpaRepository<EmployeeRequest, Long> {
    List<EmployeeRequest> findByMunicipality(String municipality);
}