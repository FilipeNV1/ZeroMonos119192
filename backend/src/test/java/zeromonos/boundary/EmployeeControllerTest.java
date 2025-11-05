package zeromonos.boundary;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import zeromonos.data.EmployeeRequest;
import zeromonos.data.EmployeeRepository;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.hamcrest.Matchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(EmployeeController.class)
class EmployeeControllerTest {

    @Autowired
    private MockMvc mvc;

    @MockitoBean
    private EmployeeRepository employeeRepository;

    @Test
    void whenPostEmployee_thenCreateEmployee() throws Exception {
        EmployeeRequest employee = new EmployeeRequest("John Doe", "john@example.com", "Aveiro", "COLLECTOR");
        employee.setId(1L);

        when(employeeRepository.save(Mockito.any(EmployeeRequest.class))).thenReturn(employee);

        mvc.perform(
                post("/api/employees")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"John Doe\",\"email\":\"john@example.com\",\"municipality\":\"Aveiro\",\"role\":\"COLLECTOR\"}")
        )
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name", is("John Doe")))
                .andExpect(jsonPath("$.email", is("john@example.com")))
                .andExpect(jsonPath("$.municipality", is("Aveiro")))
                .andExpect(jsonPath("$.role", is("COLLECTOR")));

        verify(employeeRepository, times(1)).save(Mockito.any(EmployeeRequest.class));
    }

    @Test
    void givenManyEmployees_whenGetEmployees_thenReturnJsonArray() throws Exception {
        EmployeeRequest emp1 = new EmployeeRequest("John Doe", "john@example.com", "Aveiro", "COLLECTOR");
        EmployeeRequest emp2 = new EmployeeRequest("Jane Smith", "jane@example.com", "Porto", "DRIVER");
        List<EmployeeRequest> allEmployees = Arrays.asList(emp1, emp2);

        when(employeeRepository.findAll()).thenReturn(allEmployees);

        mvc.perform(get("/api/employees").contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].name", is("John Doe")))
                .andExpect(jsonPath("$[1].name", is("Jane Smith")));

        verify(employeeRepository, times(1)).findAll();
    }

    @Test
    void whenGetEmployeeById_thenReturnEmployee() throws Exception {
        EmployeeRequest employee = new EmployeeRequest("John Doe", "john@example.com", "Aveiro", "COLLECTOR");
        employee.setId(1L);

        when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee));

        mvc.perform(get("/api/employees/1").contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name", is("John Doe")))
                .andExpect(jsonPath("$.role", is("COLLECTOR")));

        verify(employeeRepository, times(1)).findById(1L);
    }

    @Test
    void whenGetNonExistentEmployee_thenReturn404() throws Exception {
        when(employeeRepository.findById(999L)).thenReturn(Optional.empty());

        mvc.perform(get("/api/employees/999").contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());

        verify(employeeRepository, times(1)).findById(999L);
    }

    @Test
    void whenGetEmployeesByMunicipality_thenReturnEmployees() throws Exception {
        EmployeeRequest emp1 = new EmployeeRequest("John Doe", "john@example.com", "Aveiro", "COLLECTOR");
        EmployeeRequest emp2 = new EmployeeRequest("Maria Silva", "maria@example.com", "Aveiro", "DRIVER");
        List<EmployeeRequest> aveiroEmployees = Arrays.asList(emp1, emp2);

        when(employeeRepository.findByMunicipality("Aveiro")).thenReturn(aveiroEmployees);

        mvc.perform(get("/api/employees/municipality/Aveiro"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].municipality", is("Aveiro")))
                .andExpect(jsonPath("$[1].municipality", is("Aveiro")));

        verify(employeeRepository, times(1)).findByMunicipality("Aveiro");
    }

    @Test
    void whenGetEmployeesByMunicipality_withNoResults_thenReturnEmptyList() throws Exception {
        when(employeeRepository.findByMunicipality("Lisboa")).thenReturn(List.of());

        mvc.perform(get("/api/employees/municipality/Lisboa"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));

        verify(employeeRepository, times(1)).findByMunicipality("Lisboa");
    }
}