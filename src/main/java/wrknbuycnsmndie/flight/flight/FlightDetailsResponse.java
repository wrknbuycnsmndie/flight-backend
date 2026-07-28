package wrknbuycnsmndie.flight.flight;

import wrknbuycnsmndie.flight.aircraft.AircraftResponse;
import wrknbuycnsmndie.flight.airport.AirportResponse;

import java.time.LocalDateTime;

public record FlightDetailsResponse(
        Long id,
        String flightNumber,
        AirportResponse departureAirport,
        AirportResponse arrivalAirport,
        LocalDateTime departureTime,
        LocalDateTime arrivalTime,
        AircraftResponse aircraft,
        long passengerCount
) {
}
