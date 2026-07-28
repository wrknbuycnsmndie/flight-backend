package wrknbuycnsmndie.flight.flight;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import wrknbuycnsmndie.flight.aircraft.Aircraft;
import wrknbuycnsmndie.flight.aircraft.AircraftRepository;
import wrknbuycnsmndie.flight.aircraft.AircraftResponse;
import wrknbuycnsmndie.flight.airport.Airport;
import wrknbuycnsmndie.flight.airport.AirportRepository;
import wrknbuycnsmndie.flight.airport.AirportResponse;
import wrknbuycnsmndie.flight.common.exception.BusinessValidationException;
import wrknbuycnsmndie.flight.common.dto.PageResponse;
import wrknbuycnsmndie.flight.common.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;

import java.time.LocalDateTime;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class FlightService {

    private final FlightRepository flightRepository;
    private final AirportRepository airportRepository;
    private final AircraftRepository aircraftRepository;

    public PageResponse<FlightListItemResponse> findAll(Pageable pageable) {
        Page<FlightListProjection> page = flightRepository.findAllForList(pageable);

        return new PageResponse<>(
                page.getContent().stream()
                        .map(flight -> new FlightListItemResponse(
                                flight.getId(),
                                flight.getFlightNumber(),
                                flight.getDepartureAirportCode(),
                                flight.getArrivalAirportCode(),
                                flight.getDepartureTime(),
                                flight.getPassengerCount()))
                        .toList(),
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages());
    }

    public FlightDetailsResponse findById(Long id) {
        Flight flight = flightRepository.findWithDetailsById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Flight not found: " + id));

        return new FlightDetailsResponse(
                flight.getId(),
                flight.getFlightNumber(),
                toAirportResponse(flight.getDepartureAirport()),
                toAirportResponse(flight.getArrivalAirport()),
                flight.getDepartureTime(),
                flight.getArrivalTime(),
                new AircraftResponse(
                        flight.getAircraft().getId(),
                        flight.getAircraft().getModel(),
                        flight.getAircraft().getCapacity()),
                flight.getPassengers().size());
    }

    @Transactional
    public FlightDetailsResponse create(CreateFlightRequest request) {
        RelatedEntities related = validateAndLoadRelatedEntities(request);
        Flight flight = Flight.create(
                request.flightNumber(),
                related.departureAirport(),
                related.arrivalAirport(),
                request.departureTime(),
                request.arrivalTime(),
                related.aircraft());
        return toDetailsResponse(flightRepository.save(flight));
    }

    @Transactional
    public FlightDetailsResponse update(Long id, UpdateFlightRequest request) {
        Flight flight = flightRepository.findWithDetailsById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Flight not found: " + id));
        RelatedEntities related = validateAndLoadRelatedEntities(request);

        if (related.aircraft().getCapacity() < flight.getPassengers().size()) {
            throw new BusinessValidationException("Aircraft capacity is below the current passenger count");
        }

        flight.update(
                request.flightNumber(),
                related.departureAirport(),
                related.arrivalAirport(),
                request.departureTime(),
                request.arrivalTime(),
                related.aircraft());
        return toDetailsResponse(flightRepository.save(flight));
    }

    private RelatedEntities validateAndLoadRelatedEntities(CreateFlightRequest request) {
        return validateAndLoadRelatedEntities(
                request.departureAirportId(),
                request.arrivalAirportId(),
                request.departureTime(),
                request.arrivalTime(),
                request.aircraftId());
    }

    private RelatedEntities validateAndLoadRelatedEntities(UpdateFlightRequest request) {
        return validateAndLoadRelatedEntities(
                request.departureAirportId(),
                request.arrivalAirportId(),
                request.departureTime(),
                request.arrivalTime(),
                request.aircraftId());
    }

    private RelatedEntities validateAndLoadRelatedEntities(
            Long departureAirportId,
            Long arrivalAirportId,
            LocalDateTime departureTime,
            LocalDateTime arrivalTime,
            Long aircraftId) {
        if (departureAirportId.equals(arrivalAirportId)) {
            throw new BusinessValidationException("Departure and arrival airports must be different");
        }
        if (!departureTime.isBefore(arrivalTime)) {
            throw new BusinessValidationException("Departure time must be before arrival time");
        }

        Airport departureAirport = airportRepository.findById(departureAirportId)
                .orElseThrow(() -> new ResourceNotFoundException("Departure airport not found: " + departureAirportId));
        Airport arrivalAirport = airportRepository.findById(arrivalAirportId)
                .orElseThrow(() -> new ResourceNotFoundException("Arrival airport not found: " + arrivalAirportId));
        Aircraft aircraft = aircraftRepository.findById(aircraftId)
                .orElseThrow(() -> new ResourceNotFoundException("Aircraft not found: " + aircraftId));

        return new RelatedEntities(departureAirport, arrivalAirport, aircraft);
    }

    private FlightDetailsResponse toDetailsResponse(Flight flight) {
        return new FlightDetailsResponse(
                flight.getId(),
                flight.getFlightNumber(),
                toAirportResponse(flight.getDepartureAirport()),
                toAirportResponse(flight.getArrivalAirport()),
                flight.getDepartureTime(),
                flight.getArrivalTime(),
                new AircraftResponse(
                        flight.getAircraft().getId(),
                        flight.getAircraft().getModel(),
                        flight.getAircraft().getCapacity()),
                flight.getPassengers().size());
    }

    private AirportResponse toAirportResponse(Airport airport) {
        return new AirportResponse(
                airport.getId(),
                airport.getCode(),
                airport.getName(),
                airport.getCity());
    }

    private record RelatedEntities(
            Airport departureAirport,
            Airport arrivalAirport,
            Aircraft aircraft
    ) {
    }
}
