package wrknbuycnsmndie.flight.flight;

import wrknbuycnsmndie.flight.aircraft.AircraftResponse;
import wrknbuycnsmndie.flight.airport.Airport;
import wrknbuycnsmndie.flight.airport.AirportResponse;

final class FlightMapper {

    private FlightMapper() {
    }

    static FlightListItemResponse toListItem(FlightListProjection flight) {
        return new FlightListItemResponse(
                flight.getId(),
                flight.getFlightNumber(),
                flight.getDepartureAirportCode(),
                flight.getArrivalAirportCode(),
                flight.getDepartureTime(),
                flight.getPassengerCount());
    }

    static FlightDetailsResponse toDetails(Flight flight, long passengerCount) {
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
                passengerCount);
    }

    private static AirportResponse toAirportResponse(Airport airport) {
        return new AirportResponse(
                airport.getId(),
                airport.getCode(),
                airport.getName(),
                airport.getCity());
    }
}
