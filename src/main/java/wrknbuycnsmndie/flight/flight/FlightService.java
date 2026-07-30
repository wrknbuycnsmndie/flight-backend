package wrknbuycnsmndie.flight.flight;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import wrknbuycnsmndie.flight.aircraft.Aircraft;
import wrknbuycnsmndie.flight.aircraft.AircraftRepository;
import wrknbuycnsmndie.flight.airport.Airport;
import wrknbuycnsmndie.flight.airport.AirportRepository;
import wrknbuycnsmndie.flight.common.exception.BusinessValidationException;
import wrknbuycnsmndie.flight.common.dto.PageResponse;
import wrknbuycnsmndie.flight.common.exception.ResourceNotFoundException;
import wrknbuycnsmndie.flight.passenger.PassengerRepository;
import lombok.RequiredArgsConstructor;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class FlightService {

    private final FlightRepository flightRepository;
    private final AirportRepository airportRepository;
    private final AircraftRepository aircraftRepository;
    private final PassengerRepository passengerRepository;

    public PageResponse<FlightListItemResponse> findAll(Pageable pageable) {
        Page<FlightListProjection> page = flightRepository.findAllForList(pageable);

        return new PageResponse<>(
                page.getContent().stream()
                        .map(FlightMapper::toListItem)
                        .toList(),
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages());
    }

    public FlightDetailsResponse findById(Long id) {
        Flight flight = flightRepository.findWithDetailsById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Flight not found: " + id));
        return FlightMapper.toDetails(flight, passengerRepository.countByFlightId(id));
    }

    @Transactional
    public FlightDetailsResponse create(CreateFlightRequest request) {
        FlightReferences references = resolveFlightReferences(request);
        Flight flight = Flight.create(
                request.flightNumber(),
                references.departureAirport(),
                references.arrivalAirport(),
                request.departureTime(),
                request.arrivalTime(),
                references.aircraft());
        return FlightMapper.toDetails(flightRepository.save(flight), 0);
    }

    @Transactional
    public FlightDetailsResponse update(Long id, UpdateFlightRequest request) {
        Flight flight = flightRepository.findWithDetailsById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Flight not found: " + id));
        FlightReferences references = resolveFlightReferences(request);
        long passengerCount = passengerRepository.countByFlightId(id);

        if (references.aircraft().getCapacity() < passengerCount) {
            throw new BusinessValidationException("Aircraft capacity is below the current passenger count");
        }

        flight.update(
                request.flightNumber(),
                references.departureAirport(),
                references.arrivalAirport(),
                request.departureTime(),
                request.arrivalTime(),
                references.aircraft());
        return FlightMapper.toDetails(flight, passengerCount);
    }

    @Transactional
    public void delete(Long id) {
        Flight flight = flightRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Flight not found: " + id));
        flightRepository.delete(flight);
    }

    private FlightReferences resolveFlightReferences(FlightRequestData request) {
        if (request.departureAirportId().equals(request.arrivalAirportId())) {
            throw new BusinessValidationException("Departure and arrival airports must be different");
        }
        if (!request.departureTime().isBefore(request.arrivalTime())) {
            throw new BusinessValidationException("Departure time must be before arrival time");
        }

        Airport departureAirport = airportRepository.findById(request.departureAirportId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Departure airport not found: " + request.departureAirportId()));
        Airport arrivalAirport = airportRepository.findById(request.arrivalAirportId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Arrival airport not found: " + request.arrivalAirportId()));
        Aircraft aircraft = aircraftRepository.findById(request.aircraftId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Aircraft not found: " + request.aircraftId()));

        return new FlightReferences(departureAirport, arrivalAirport, aircraft);
    }

    private record FlightReferences(
            Airport departureAirport,
            Airport arrivalAirport,
            Aircraft aircraft
    ) {
    }
}
