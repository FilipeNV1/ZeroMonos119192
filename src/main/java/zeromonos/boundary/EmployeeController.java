package zeromonos.boundary;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import zeromonos.data.EmployeeRequest;
import zeromonos.data.EmployeeRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/api/employees")
public class EmployeeController {

    private final EmployeeRepository employeeRepository;
    private static final Logger logger = LoggerFactory.getLogger(EmployeeController.class);

    public EmployeeController(EmployeeRepository employeeRepository) {
        this.employeeRepository = employeeRepository;
    }

    @PostMapping
    public ResponseEntity<EmployeeRequest> createEmployee(@RequestBody EmployeeRequest employee) {
        EmployeeRequest saved = employeeRepository.save(employee);
        logger.info("Employee created: {}", saved.getName());
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    @GetMapping
    public ResponseEntity<List<EmployeeRequest>> getAllEmployees() {
        logger.info("Received request to get all employees");
        return ResponseEntity.ok(employeeRepository.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<EmployeeRequest> getEmployee(@PathVariable Long id) {
        logger.info("Received request to get employee {}", id);
        return employeeRepository.findById(id)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/municipality/{municipality}")
    public ResponseEntity<List<EmployeeRequest>> getEmployeesByMunicipality(@PathVariable String municipality) {
        logger.info("Received request to get employees for municipality {}", municipality);
        return ResponseEntity.ok(employeeRepository.findByMunicipality(municipality));
    }
}