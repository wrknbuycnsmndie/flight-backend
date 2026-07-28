package wrknbuycnsmndie.flight.passenger;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class PassengerController {

    private final PassengerService passengerService;

    @GetMapping("/flights/{flightId}/passengers")
    public List<PassengerResponse> getByFlight(@PathVariable Long flightId) {
        return passengerService.findByFlightId(flightId);
    }

    @PostMapping("/flights/{flightId}/passengers")
    public ResponseEntity<PassengerResponse> create(
            @PathVariable Long flightId,
            @Valid @RequestBody CreatePassengerRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(passengerService.create(flightId, request));
    }

    @DeleteMapping("/passengers/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        passengerService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
