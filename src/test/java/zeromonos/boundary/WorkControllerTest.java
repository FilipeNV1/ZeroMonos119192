package zeromonos.boundary;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import zeromonos.data.BookingRequest;
import zeromonos.data.EmployeeRequest;
import zeromonos.data.WorkTaskRequest;
import zeromonos.data.WorkTaskRepository;
import zeromonos.data.EmployeeRepository;
import zeromonos.data.BookingRepository;
import zeromonos.service.WorkTaskService;
import zeromonos.service.BookingService;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.hamcrest.Matchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(WorkTaskController.class)
class WorkControllerTest {

    @Autowired
    private MockMvc mvc;

    @MockitoBean
    private WorkTaskService workService;
    @MockitoBean
    private WorkTaskRepository workTaskRepository;
    @MockitoBean
    private EmployeeRepository employeeRepository;
    @MockitoBean
    private BookingRepository bookingRepository;
    @MockitoBean
    private BookingService bookingService;

    @Test
    void whenPostWork_thenCreateWork() throws Exception {
        BookingRequest booking = new BookingRequest("Test", "Aveiro", LocalDateTime.now(), "TOKEN123");
        booking.setId(1L);
        EmployeeRequest employee = new EmployeeRequest("John Doe", "john@example.com", "Aveiro", "COLLECTOR");
        employee.setId(1L);
        WorkTaskRequest work = new WorkTaskRequest(booking, employee);
        work.setId(1L);
        work.setStatus("ASSIGNED"); 

        when(bookingService.getBookingByToken("TOKEN123")).thenReturn(Optional.of(booking));
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee));
        when(workTaskRepository.findByBooking(booking)).thenReturn(List.of());
        when(workTaskRepository.save(Mockito.any(WorkTaskRequest.class))).thenReturn(work);

        mvc.perform(
                post("/api/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"bookingToken\":\"TOKEN123\",\"employeeId\":1}")
        )
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status", is("ASSIGNED")))
                .andExpect(jsonPath("$.assignedAt", notNullValue()));

        verify(bookingService, times(1)).getBookingByToken("TOKEN123");
        verify(employeeRepository, times(1)).findById(1L);
    }

    @Test
    void givenManyTasks_whenGetTasks_thenReturnJsonArray() throws Exception {
        BookingRequest booking1 = new BookingRequest("Task1", "Aveiro", LocalDateTime.now(), "T1");
        BookingRequest booking2 = new BookingRequest("Task2", "Porto", LocalDateTime.now(), "T2");
        EmployeeRequest employee = new EmployeeRequest("John", "john@example.com", "Aveiro", "COLLECTOR");
        
        WorkTaskRequest w1 = new WorkTaskRequest(booking1, employee);
        WorkTaskRequest w2 = new WorkTaskRequest(booking2, employee);
        List<WorkTaskRequest> allTasks = Arrays.asList(w1, w2);

        when(workTaskRepository.findAll()).thenReturn(allTasks);

        mvc.perform(get("/api/tasks").contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].status", is("ASSIGNED")))
                .andExpect(jsonPath("$[1].status", is("ASSIGNED")));

        verify(workTaskRepository, times(1)).findAll();
    }

    @Test
    void whenCompleteTask_thenReturnCompletedTask() throws Exception {
        BookingRequest booking = new BookingRequest("Test", "Aveiro", LocalDateTime.now(), "TOKEN123");
        booking.setStatus("ASSIGNED");
        EmployeeRequest employee = new EmployeeRequest("John", "john@example.com", "Aveiro", "COLLECTOR");
        WorkTaskRequest work = new WorkTaskRequest(booking, employee);
        work.setId(1L);
        work.setStatus("ASSIGNED");
        
        WorkTaskRequest completedWork = new WorkTaskRequest(booking, employee);
        completedWork.setId(1L);
        completedWork.setStatus("COMPLETED");
        completedWork.setCompletedAt(LocalDateTime.now());
        completedWork.setNotes("Task completed");

        when(workTaskRepository.findById(1L)).thenReturn(Optional.of(work));
        when(workTaskRepository.save(Mockito.any(WorkTaskRequest.class))).thenReturn(completedWork);

        mvc.perform(put("/api/tasks/1/complete")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"notes\":\"Task completed\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is("COMPLETED")))
                .andExpect(jsonPath("$.completedAt", notNullValue()));

        verify(workTaskRepository, times(1)).findById(1L);
        verify(workTaskRepository, times(1)).save(Mockito.any(WorkTaskRequest.class));
        verify(bookingService, times(1)).save(booking);
    }

    @Test
    void whenCompleteNonExistentTask_thenReturn404() throws Exception {
        when(workTaskRepository.findById(999L)).thenReturn(Optional.empty());

        mvc.perform(put("/api/tasks/999/complete")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"notes\":\"Test\"}"))
                .andExpect(status().isNotFound());

        verify(workTaskRepository, times(1)).findById(999L);
    }

    @Test
    void whenGetTasksByEmployee_thenReturnTasks() throws Exception {
        BookingRequest booking = new BookingRequest("Test", "Aveiro", LocalDateTime.now(), "TOKEN123");
        EmployeeRequest employee = new EmployeeRequest("John", "john@example.com", "Aveiro", "COLLECTOR");
        employee.setId(1L);
        WorkTaskRequest work = new WorkTaskRequest(booking, employee);

        when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee));
        when(workTaskRepository.findByAssignedEmployee(employee)).thenReturn(List.of(work));

        mvc.perform(get("/api/tasks/employee/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].status", is("ASSIGNED")));

        verify(employeeRepository, times(1)).findById(1L);
        verify(workTaskRepository, times(1)).findByAssignedEmployee(employee);
    }

    @Test
    void whenGetTaskById_thenReturnTask() throws Exception {
        BookingRequest booking = new BookingRequest("Test", "Aveiro", LocalDateTime.now(), "TOKEN123");
        EmployeeRequest employee = new EmployeeRequest("John", "john@example.com", "Aveiro", "COLLECTOR");
        WorkTaskRequest work = new WorkTaskRequest(booking, employee);
        work.setId(1L);

        when(workTaskRepository.findById(1L)).thenReturn(Optional.of(work));

        mvc.perform(get("/api/tasks/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is("ASSIGNED")));

        verify(workTaskRepository, times(1)).findById(1L);
    }

    @Test
    void whenGetNonExistentTask_thenReturn404() throws Exception {
        when(workTaskRepository.findById(999L)).thenReturn(Optional.empty());

        mvc.perform(get("/api/tasks/999"))
                .andExpect(status().isNotFound());

        verify(workTaskRepository, times(1)).findById(999L);
    }

    @Test
    void whenGetAllTasks_thenReturnAllTasks() throws Exception {
        BookingRequest booking1 = new BookingRequest("Task1", "Aveiro", LocalDateTime.now(), "T1");
        BookingRequest booking2 = new BookingRequest("Task2", "Porto", LocalDateTime.now(), "T2");
        EmployeeRequest employee1 = new EmployeeRequest("John", "john@example.com", "Aveiro", "COLLECTOR");
        EmployeeRequest employee2 = new EmployeeRequest("Jane", "jane@example.com", "Porto", "DRIVER");
        
        WorkTaskRequest w1 = new WorkTaskRequest(booking1, employee1);
        WorkTaskRequest w2 = new WorkTaskRequest(booking2, employee2);
        List<WorkTaskRequest> allTasks = Arrays.asList(w1, w2);

        when(workTaskRepository.findAll()).thenReturn(allTasks);

        mvc.perform(get("/api/tasks"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)));

        verify(workTaskRepository, times(1)).findAll();
    }

    @Test
    void whenAssignTaskWithInvalidEmployee_thenReturn404() throws Exception {
        BookingRequest booking = new BookingRequest("Test", "Aveiro", LocalDateTime.now(), "TOKEN123");
        booking.setId(1L);

        when(bookingService.getBookingByToken("TOKEN123")).thenReturn(Optional.of(booking));
        when(employeeRepository.findById(999L)).thenReturn(Optional.empty());

        mvc.perform(
                post("/api/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"bookingToken\":\"TOKEN123\",\"employeeId\":999}")
        )
                .andExpect(status().isNotFound());

        verify(bookingService, times(1)).getBookingByToken("TOKEN123");
        verify(employeeRepository, times(1)).findById(999L);
    }
}