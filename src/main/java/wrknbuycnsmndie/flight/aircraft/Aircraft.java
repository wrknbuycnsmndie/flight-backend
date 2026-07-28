package wrknbuycnsmndie.flight.aircraft;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "aircrafts")
public class Aircraft {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String model;

    @Column(nullable = false)
    private Integer capacity;

    protected Aircraft() {
    }

    public Long getId() {
        return id;
    }

    public String getModel() {
        return model;
    }

    public Integer getCapacity() {
        return capacity;
    }
}
