package wrknbuycnsmndie.flight.passenger;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PassengerRepository extends JpaRepository<Passenger, Long> {

    List<Passenger> findAllByFlightIdOrderByIdAsc(Long flightId);

    long countByFlightId(Long flightId);

    boolean existsByPassportNumber(String passportNumber);
}
