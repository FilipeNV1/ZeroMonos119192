package zeromonos.service;

import org.springframework.stereotype.Service;
import zeromonos.data.*;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class WorkTaskService {

    private static final String EMPLOYEE_NOT_FOUND = "Employee not found";
    private static final String TASK_NOT_FOUND = "Task not found";
    private static final String BOOKING_NOT_FOUND = "Booking not found";
    private static final String STATUS_COMPLETED = "COMPLETED";
    
    private final WorkTaskRepository workTaskRepository;
    private final EmployeeRepository employeeRepository;
    private final BookingRepository bookingRepository;
    
    public WorkTaskService(WorkTaskRepository workTaskRepository, EmployeeRepository employeeRepository, BookingRepository bookingRepository) {
        this.workTaskRepository = workTaskRepository;
        this.employeeRepository = employeeRepository;
        this.bookingRepository = bookingRepository;
    }
    
    public WorkTaskRequest assignTaskToEmployee(BookingRequest booking, Long employeeId) {
        EmployeeRequest employee = employeeRepository.findById(employeeId)
            .orElseThrow(() -> new IllegalArgumentException(EMPLOYEE_NOT_FOUND));
        
        WorkTaskRequest task = new WorkTaskRequest(booking, employee);
        return workTaskRepository.save(task);
    }
    
    public WorkTaskRequest assignTaskByIds(Long bookingId, Long employeeId) {
        BookingRequest booking = bookingRepository.findById(bookingId)
            .orElseThrow(() -> new IllegalArgumentException(BOOKING_NOT_FOUND));
        
        EmployeeRequest employee = employeeRepository.findById(employeeId)
            .orElseThrow(() -> new IllegalArgumentException(EMPLOYEE_NOT_FOUND));
        
        WorkTaskRequest task = new WorkTaskRequest(booking, employee);
        return workTaskRepository.save(task);
    }
    
    public List<WorkTaskRequest> getTasksByEmployee(Long employeeId) {
        EmployeeRequest employee = employeeRepository.findById(employeeId)
            .orElseThrow(() -> new IllegalArgumentException(EMPLOYEE_NOT_FOUND));
        return workTaskRepository.findByAssignedEmployee(employee);
    }
    
    public List<WorkTaskRequest> getAllTasks() {
        return workTaskRepository.findAll();
    }
    
    public Optional<WorkTaskRequest> getTaskById(Long taskId) {
        return workTaskRepository.findById(taskId);
    }
    
    public List<WorkTaskRequest> getTasksByStatus(String status) {
        return workTaskRepository.findByStatus(status);
    }
    
    public WorkTaskRequest updateTaskStatus(Long taskId, String status) {
        WorkTaskRequest task = workTaskRepository.findById(taskId)
            .orElseThrow(() -> new IllegalArgumentException(TASK_NOT_FOUND));
        
        task.setStatus(status);
        if (STATUS_COMPLETED.equals(status)) {
            task.setCompletedAt(LocalDateTime.now());
        }
        return workTaskRepository.save(task);
    }
    
    public WorkTaskRequest completeTask(Long taskId, String notes) {
        WorkTaskRequest task = workTaskRepository.findById(taskId)
            .orElseThrow(() -> new IllegalArgumentException(TASK_NOT_FOUND));
        
        task.setStatus(STATUS_COMPLETED);
        task.setCompletedAt(LocalDateTime.now());
        task.setNotes(notes);
        
        return workTaskRepository.save(task);
    }
    
    public List<EmployeeRequest> getAllEmployees() {
        return employeeRepository.findAll();
    }
    
    public Optional<EmployeeRequest> getEmployeeById(Long employeeId) {
        return employeeRepository.findById(employeeId);
    }
    
    public List<EmployeeRequest> getEmployeesByMunicipality(String municipality) {
        return employeeRepository.findByMunicipality(municipality);
    }
    
    public EmployeeRequest createEmployee(String name, String email, String municipality, String role) {
        EmployeeRequest employee = new EmployeeRequest(name, email, municipality, role);
        return employeeRepository.save(employee);
    }
    
    public void deleteTask(Long taskId) {
        WorkTaskRequest task = workTaskRepository.findById(taskId)
            .orElseThrow(() -> new IllegalArgumentException(TASK_NOT_FOUND));
        workTaskRepository.delete(task);
    }
}