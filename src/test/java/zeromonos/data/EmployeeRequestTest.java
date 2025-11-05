package zeromonos.data;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class EmployeeRequestTest {

    @Test
    void testDefaultConstructor() {
        EmployeeRequest employee = new EmployeeRequest();
        assertThat(employee).isNotNull();
    }

    @Test
    void testParameterizedConstructor() {
        EmployeeRequest employee = new EmployeeRequest("John Doe", "john@example.com", "Aveiro", "COLLECTOR");
        
        assertThat(employee.getName()).isEqualTo("John Doe");
        assertThat(employee.getEmail()).isEqualTo("john@example.com");
        assertThat(employee.getMunicipality()).isEqualTo("Aveiro");
        assertThat(employee.getRole()).isEqualTo("COLLECTOR");
    }

    @Test
    void testAllSettersAndGetters() {
        EmployeeRequest employee = new EmployeeRequest();
        
        employee.setId(1L);
        employee.setName("Jane Smith");
        employee.setEmail("jane@example.com");
        employee.setMunicipality("Porto");
        employee.setRole("DRIVER");
        
        assertThat(employee.getId()).isEqualTo(1L);
        assertThat(employee.getName()).isEqualTo("Jane Smith");
        assertThat(employee.getEmail()).isEqualTo("jane@example.com");
        assertThat(employee.getMunicipality()).isEqualTo("Porto");
        assertThat(employee.getRole()).isEqualTo("DRIVER");
    }
}