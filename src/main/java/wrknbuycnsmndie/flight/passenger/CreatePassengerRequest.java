package wrknbuycnsmndie.flight.passenger;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreatePassengerRequest(
        @NotBlank(message = "First name is required")
        @Size(max = 255, message = "First name must not exceed 255 characters")
        String firstName,

        @NotBlank(message = "Last name is required")
        @Size(max = 255, message = "Last name must not exceed 255 characters")
        String lastName,

        @NotBlank(message = "Passport number is required")
        @Size(max = 50, message = "Passport number must not exceed 50 characters")
        String passportNumber
) {
}
