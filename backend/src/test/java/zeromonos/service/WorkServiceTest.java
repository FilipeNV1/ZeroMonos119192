package zeromonos.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import zeromonos.data.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WorkServiceTest {

    @Mock
    private WorkTaskRepository workTaskRepository;
    @Mock
    private EmployeeRepository employeeRepository;
    @Mock
    private BookingRepository bookingRepository;
    
    private WorkTaskService service;

    @BeforeEach
    void setUp() {
        service = new WorkTaskService(workTaskRepository, employeeRepository, bookingRepository);
    }

    @Test
    void testAssignTaskToEmployee() {
        BookingRequest booking = new BookingRequest("Test", "Aveiro", LocalDateTime.now(), "TOKEN123");
        EmployeeRequest employee = new EmployeeRequest("John", "john@example.com", "Aveiro", "COLLECTOR");
        employee.setId(1L);
        
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee));
        when(workTaskRepository.save(any(WorkTaskRequest.class))).thenReturn(new WorkTaskRequest(booking, employee));

        WorkTaskRequest result = service.assignTaskToEmployee(booking, 1L);

        assertThat(result).isNotNull();
        verify(employeeRepository, times(1)).findById(1L);
        verify(workTaskRepository, times(1)).save(any(WorkTaskRequest.class));
    }

    @Test
    void testGetAllTasks() {
        List<WorkTaskRequest> tasks = List.of(new WorkTaskRequest());
        when(workTaskRepository.findAll()).thenReturn(tasks);

        List<WorkTaskRequest> result = service.getAllTasks();

        assertThat(result).hasSize(1);
        verify(workTaskRepository, times(1)).findAll();
    }

    @Test
    void testCompleteTask() {
        WorkTaskRequest task = new WorkTaskRequest();
        task.setId(1L);
        task.setStatus("ASSIGNED");
        
        when(workTaskRepository.findById(1L)).thenReturn(Optional.of(task));
        when(workTaskRepository.save(any(WorkTaskRequest.class))).thenReturn(task);

        WorkTaskRequest result = service.completeTask(1L, "Done");

        assertThat(result.getStatus()).isEqualTo("COMPLETED");
        assertThat(result.getCompletedAt()).isNotNull();
        verify(workTaskRepository, times(1)).save(task);
    }

    @Test
    void testGetAllEmployees() {
        EmployeeRequest emp1 = new EmployeeRequest("John", "john@example.com", "Aveiro", "COLLECTOR");
        EmployeeRequest emp2 = new EmployeeRequest("Jane", "jane@example.com", "Porto", "DRIVER");
        List<EmployeeRequest> employees = List.of(emp1, emp2);
        
        when(employeeRepository.findAll()).thenReturn(employees);
    
        List<EmployeeRequest> result = service.getAllEmployees();
    
        assertThat(result).hasSize(2);
        verify(employeeRepository, times(1)).findAll();
    }
    
    @Test
    void testDeleteTask() {
        WorkTaskRequest task = new WorkTaskRequest();
        task.setId(1L);
        
        when(workTaskRepository.findById(1L)).thenReturn(Optional.of(task));
        doNothing().when(workTaskRepository).delete(task);
    
        service.deleteTask(1L);
    
        verify(workTaskRepository, times(1)).findById(1L);
        verify(workTaskRepository, times(1)).delete(task);
    }

    @Test
    void testGetTaskById() {
        WorkTaskRequest task = new WorkTaskRequest();
        task.setId(1L);

        when(workTaskRepository.findById(1L)).thenReturn(Optional.of(task));

        Optional<WorkTaskRequest> result = service.getTaskById(1L);

        assertThat(result).isPresent();
        assertThat(result.get().getId()).isEqualTo(1L);
        verify(workTaskRepository, times(1)).findById(1L);
    }

    @Test
    void testGetTasksByEmployee() {
        EmployeeRequest employee = new EmployeeRequest("John", "john@example.com", "Aveiro", "COLLECTOR");
        employee.setId(1L);
        WorkTaskRequest task1 = new WorkTaskRequest();
        WorkTaskRequest task2 = new WorkTaskRequest();
        List<WorkTaskRequest> tasks = List.of(task1, task2);
        
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee));
        when(workTaskRepository.findByAssignedEmployee(employee)).thenReturn(tasks);
    
        List<WorkTaskRequest> result = service.getTasksByEmployee(1L);
    
        assertThat(result).hasSize(2);
        verify(employeeRepository, times(1)).findById(1L);
        verify(workTaskRepository, times(1)).findByAssignedEmployee(employee);
    }
}