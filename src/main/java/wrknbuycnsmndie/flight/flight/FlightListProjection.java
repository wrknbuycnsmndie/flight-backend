package wrknbuycnsmndie.flight.flight;

import java.time.LocalDateTime;

public interface FlightListProjection {

    Long getId();

    String getFlightNumber();

    String getDepartureAirportCode();

    String getArrivalAirportCode();

    LocalDateTime getDepartureTime();

    Long getPassengerCount();
}
