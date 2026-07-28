package wrknbuycnsmndie.flight.flight;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface FlightRepository extends JpaRepository<Flight, Long> {

    @Query(value = """
            SELECT f.id AS id,
                   f.flightNumber AS flightNumber,
                   departure.code AS departureAirportCode,
                   arrival.code AS arrivalAirportCode,
                   f.departureTime AS departureTime,
                   COUNT(passenger.id) AS passengerCount
            FROM Flight f
            JOIN f.departureAirport departure
            JOIN f.arrivalAirport arrival
            LEFT JOIN f.passengers passenger
            GROUP BY f.id, f.flightNumber, departure.code, arrival.code, f.departureTime
            """,
            countQuery = "SELECT COUNT(f) FROM Flight f")
    Page<FlightListProjection> findAllForList(Pageable pageable);

    @EntityGraph(attributePaths = {"departureAirport", "arrivalAirport", "aircraft"})
    @Query("SELECT f FROM Flight f WHERE f.id = :id")
    Optional<Flight> findWithDetailsById(Long id);
}
