package zeromonos.data;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
public class WorkTaskRequest {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @OneToOne
    private BookingRequest booking;
    
    @ManyToOne
    @JoinColumn(name = "employee_id")
    private EmployeeRequest assignedEmployee;
    
    private String status;
    private LocalDateTime assignedAt;
    private LocalDateTime completedAt;
    private String notes;
    
    public WorkTaskRequest() {}
    
    public WorkTaskRequest(BookingRequest booking, EmployeeRequest employee) {
        this.booking = booking;
        this.assignedEmployee = employee;
        this.status = "ASSIGNED";
        this.assignedAt = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }
    public void setId(Long id) {
        this.id = id;
    }
    public BookingRequest getBooking() {
        return booking;
    }
    public void setBooking(BookingRequest booking) {
        this.booking = booking;
    }
    public EmployeeRequest getAssignedEmployee() {
        return assignedEmployee;
    }
    public void setAssignedEmployee(EmployeeRequest assignedEmployee) {
        this.assignedEmployee = assignedEmployee;
    }
    public String getStatus() {
        return status;
    }
    public void setStatus(String status) {
        this.status = status;
    }
    public LocalDateTime getAssignedAt() {
        return assignedAt;
    }
    public void setAssignedAt(LocalDateTime assignedAt) {
        this.assignedAt = assignedAt;
    }
    public LocalDateTime getCompletedAt() {
        return completedAt;
    }
    public void setCompletedAt(LocalDateTime completedAt) {
        this.completedAt = completedAt;
    }
    public String getNotes() {
        return notes;
    }
    public void setNotes(String notes) {
        this.notes = notes;
    }
}