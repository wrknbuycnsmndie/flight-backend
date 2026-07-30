package wrknbuycnsmndie.flight.flight;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.Column;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import wrknbuycnsmndie.flight.aircraft.Aircraft;
import wrknbuycnsmndie.flight.airport.Airport;
import wrknbuycnsmndie.flight.passenger.Passenger;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "flights")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Flight {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "flight_number", nullable = false, length = 50)
    private String flightNumber;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "departure_airport_id", nullable = false)
    private Airport departureAirport;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "arrival_airport_id", nullable = false)
    private Airport arrivalAirport;

    @Column(name = "departure_time", nullable = false)
    private LocalDateTime departureTime;

    @Column(name = "arrival_time", nullable = false)
    private LocalDateTime arrivalTime;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "aircraft_id", nullable = false)
    private Aircraft aircraft;

    @OneToMany(mappedBy = "flight", fetch = FetchType.LAZY)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private List<Passenger> passengers = new ArrayList<>();

    public static Flight create(
            String flightNumber,
            Airport departureAirport,
            Airport arrivalAirport,
            LocalDateTime departureTime,
            LocalDateTime arrivalTime,
            Aircraft aircraft) {
        Flight flight = new Flight();
        flight.update(flightNumber, departureAirport, arrivalAirport, departureTime, arrivalTime, aircraft);
        return flight;
    }

    public void update(
            String flightNumber,
            Airport departureAirport,
            Airport arrivalAirport,
            LocalDateTime departureTime,
            LocalDateTime arrivalTime,
            Aircraft aircraft) {
        this.flightNumber = flightNumber;
        this.departureAirport = departureAirport;
        this.arrivalAirport = arrivalAirport;
        this.departureTime = departureTime;
        this.arrivalTime = arrivalTime;
        this.aircraft = aircraft;
    }

}
