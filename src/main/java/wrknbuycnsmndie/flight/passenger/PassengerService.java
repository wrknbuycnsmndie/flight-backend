package wrknbuycnsmndie.flight.passenger;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import wrknbuycnsmndie.flight.common.exception.BusinessValidationException;
import wrknbuycnsmndie.flight.common.exception.ResourceNotFoundException;
import wrknbuycnsmndie.flight.flight.Flight;
import wrknbuycnsmndie.flight.flight.FlightRepository;

import java.util.List;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class PassengerService {

    private final PassengerRepository passengerRepository;
    private final FlightRepository flightRepository;

    public List<PassengerResponse> findByFlightId(Long flightId) {
        ensureFlightExists(flightId);
        return passengerRepository.findAllByFlightIdOrderByIdAsc(flightId).stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public PassengerResponse create(Long flightId, CreatePassengerRequest request) {
        Flight flight = flightRepository.findWithDetailsById(flightId)
                .orElseThrow(() -> new ResourceNotFoundException("Flight not found: " + flightId));

        if (passengerRepository.existsByPassportNumber(request.passportNumber())) {
            throw new BusinessValidationException("Passport number already exists: " + request.passportNumber());
        }
        if (flight.getPassengers().size() >= flight.getAircraft().getCapacity()) {
            throw new BusinessValidationException("Flight has reached the aircraft capacity");
        }

        Passenger passenger = Passenger.create(
                request.firstName(),
                request.lastName(),
                request.passportNumber(),
                flight);
        return toResponse(passengerRepository.save(passenger));
    }

    @Transactional
    public void delete(Long id) {
        Passenger passenger = passengerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Passenger not found: " + id));
        passengerRepository.delete(passenger);
    }

    private void ensureFlightExists(Long flightId) {
        if (!flightRepository.existsById(flightId)) {
            throw new ResourceNotFoundException("Flight not found: " + flightId);
        }
    }

    private PassengerResponse toResponse(Passenger passenger) {
        return new PassengerResponse(
                passenger.getId(),
                passenger.getFirstName(),
                passenger.getLastName(),
                passenger.getPassportNumber());
    }
}
