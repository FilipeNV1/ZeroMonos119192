package zeromonos.boundary;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import zeromonos.data.WorkTaskRequest;
import zeromonos.data.BookingRequest;
import zeromonos.data.EmployeeRequest;
import zeromonos.data.WorkTaskRepository;
import zeromonos.data.EmployeeRepository;
import zeromonos.service.BookingService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/api/tasks")
public class WorkTaskController {

    private final WorkTaskRepository workTaskRepository;
    private final EmployeeRepository employeeRepository;
    private final BookingService bookingService;
    private static final Logger logger = LoggerFactory.getLogger(WorkTaskController.class);

    public WorkTaskController(WorkTaskRepository workTaskRepository, EmployeeRepository employeeRepository, BookingService bookingService) {
        this.workTaskRepository = workTaskRepository;
        this.employeeRepository = employeeRepository;
        this.bookingService = bookingService;
    }

    @PostMapping
    public ResponseEntity<WorkTaskRequest> assignTask(@RequestBody Map<String, Object> request) {
        String token = (String) request.get("bookingToken");
        Long employeeId = Long.valueOf(request.get("employeeId").toString());

        Optional<BookingRequest> bookingOpt = bookingService.getBookingByToken(token);
        Optional<EmployeeRequest> employeeOpt = employeeRepository.findById(employeeId);

        if (bookingOpt.isEmpty() || employeeOpt.isEmpty()) {
            logger.warn("Booking or employee not found");
            return ResponseEntity.notFound().build();
        }

        BookingRequest booking = bookingOpt.get();
        EmployeeRequest employee = employeeOpt.get();

        // Check if booking already has a task
        List<WorkTaskRequest> existingTasks = workTaskRepository.findByBooking(booking);
        if (!existingTasks.isEmpty()) {
            logger.warn("Booking already has a task assigned");
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }

        WorkTaskRequest task = new WorkTaskRequest(booking, employee);
        WorkTaskRequest saved = workTaskRepository.save(task);
        
        booking.setStatus("ASSIGNED");
        bookingService.save(booking);

        logger.info("Task assigned");
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    @GetMapping
    public ResponseEntity<List<WorkTaskRequest>> getAllTasks() {
        logger.info("Received request to get all tasks");
        return ResponseEntity.ok(workTaskRepository.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<WorkTaskRequest> getTask(@PathVariable Long id) {
        logger.info("Received request to get task");
        Optional<WorkTaskRequest> task = workTaskRepository.findById(id);
        return task.map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}/complete")
    public ResponseEntity<WorkTaskRequest> completeTask(@PathVariable Long id, @RequestBody Map<String, String> body) {
        Optional<WorkTaskRequest> taskOpt = workTaskRepository.findById(id);
        if (taskOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        WorkTaskRequest task = taskOpt.get();
        task.setStatus("COMPLETED");
        task.setCompletedAt(LocalDateTime.now());
        task.setNotes(body.get("notes"));
        workTaskRepository.save(task);

        task.getBooking().setStatus("COMPLETED");
        bookingService.save(task.getBooking());

        logger.info("Task {} completed", id);
        return ResponseEntity.ok(task);
    }

    @GetMapping("/employee/{employeeId}")
    public ResponseEntity<List<WorkTaskRequest>> getTasksByEmployee(@PathVariable Long employeeId) {
        Optional<EmployeeRequest> employee = employeeRepository.findById(employeeId);
        if (employee.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        logger.info("Received request to get tasks for employee");
        return ResponseEntity.ok(workTaskRepository.findByAssignedEmployee(employee.get()));
    }
}