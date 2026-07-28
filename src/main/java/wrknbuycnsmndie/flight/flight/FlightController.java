package wrknbuycnsmndie.flight.flight;

import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.data.domain.Sort;
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
    public FlightDetailsResponse getById(@PathVariable Long id) {
        return flightService.findById(id);
    }
}
