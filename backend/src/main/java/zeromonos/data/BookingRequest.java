package zeromonos.data;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
public class BookingRequest {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String token;
    private String description;
    private String municipality;
    private LocalDateTime date;
    private String status;

    public BookingRequest() {}
    public BookingRequest(String description, String municipality, LocalDateTime date, String token) {
        this.description = description;
        this.municipality = municipality;
        this.date = date;
        this.status = "RECEIVED";
        this.token = token;
    }
    public Long getId() {
        return id;
    }
    public void setId(Long id) {
        this.id = id;
    }
    public String getToken() {
        return token;
    }
    public void setToken(String token) {
        this.token = token;
    }
    public String getDescription() {
        return description;
    }
    public void setDescription(String description) {
        this.description = description;
    }
    public String getMunicipality() {
        return municipality;
    }
    public void setMunicipality(String municipality) {
        this.municipality = municipality;
    }
    public LocalDateTime getDate() {
        return date;
    }
    public void setDate(LocalDateTime date) {
        this.date = date;
    }
    public String getStatus() {
        return status;
    }
    public void setStatus(String status) {
        this.status = status;
    }
}

