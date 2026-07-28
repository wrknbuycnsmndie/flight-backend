package wrknbuycnsmndie.flight.flight;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import wrknbuycnsmndie.flight.aircraft.AircraftResponse;
import wrknbuycnsmndie.flight.airport.Airport;
import wrknbuycnsmndie.flight.airport.AirportResponse;
import wrknbuycnsmndie.flight.common.dto.PageResponse;
import wrknbuycnsmndie.flight.common.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class FlightService {

    private final FlightRepository flightRepository;

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

    private AirportResponse toAirportResponse(Airport airport) {
        return new AirportResponse(
                airport.getId(),
                airport.getCode(),
                airport.getName(),
                airport.getCity());
    }
}
