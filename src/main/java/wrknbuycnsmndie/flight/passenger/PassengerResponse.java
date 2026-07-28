package wrknbuycnsmndie.flight.passenger;

public record PassengerResponse(
        Long id,
        String firstName,
        String lastName,
        String passportNumber
) {
}
