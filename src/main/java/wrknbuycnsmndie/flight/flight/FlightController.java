package wrknbuycnsmndie.flight.flight;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.http.HttpStatus;
import lombok.RequiredArgsConstructor;
import wrknbuycnsmndie.flight.common.dto.PageResponse;

@RestController
@RequestMapping("/flights")
@RequiredArgsConstructor
public class FlightController {

    private final FlightService flightService;

    @GetMapping
    public PageResponse<FlightListItemResponse> getAll(
            @PageableDefault(size = 10, sort = "id", direction = Sort.Direction.ASC)
            Pageable pageable) {
        return flightService.findAll(pageable);
    }

    @GetMapping("/{id}")
    public FlightDetailsResponse getById(@PathVariable @Positive Long id) {
        return flightService.findById(id);
    }

    @PostMapping
    public ResponseEntity<FlightDetailsResponse> create(
            @Valid @RequestBody CreateFlightRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(flightService.create(request));
    }

    @PutMapping("/{id}")
    public FlightDetailsResponse update(
            @PathVariable @Positive Long id,
            @Valid @RequestBody UpdateFlightRequest request) {
        return flightService.update(id, request);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable @Positive Long id) {
        flightService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
