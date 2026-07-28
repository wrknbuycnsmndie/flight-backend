package wrknbuycnsmndie.flight.flight;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

public record UpdateFlightRequest(
        @NotBlank(message = "Flight number is required")
        @Size(max = 50, message = "Flight number must not exceed 50 characters")
        String flightNumber,

        @NotNull(message = "Departure airport is required")
        Long departureAirportId,

        @NotNull(message = "Arrival airport is required")
        Long arrivalAirportId,

        @NotNull(message = "Departure time is required")
        LocalDateTime departureTime,

        @NotNull(message = "Arrival time is required")
        LocalDateTime arrivalTime,

        @NotNull(message = "Aircraft is required")
        Long aircraftId
) {
}
