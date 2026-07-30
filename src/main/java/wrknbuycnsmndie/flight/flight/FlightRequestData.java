package wrknbuycnsmndie.flight.flight;

import java.time.LocalDateTime;

interface FlightRequestData {

    Long departureAirportId();

    Long arrivalAirportId();

    LocalDateTime departureTime();

    LocalDateTime arrivalTime();

    Long aircraftId();
}
