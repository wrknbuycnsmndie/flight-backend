package wrknbuycnsmndie.flight.flight;

import java.time.LocalDateTime;

public record FlightListItemResponse(
        Long id,
        String flightNumber,
        String departureAirportCode,
        String arrivalAirportCode,
        LocalDateTime departureTime,
        long passengerCount
) {
}
