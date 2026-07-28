package wrknbuycnsmndie.flight.aircraft;

import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;

import java.util.List;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class AircraftService {

    private final AircraftRepository aircraftRepository;

    public List<AircraftResponse> findAll() {
        return aircraftRepository.findAll(Sort.by(Sort.Direction.ASC, "id")).stream()
                .map(aircraft -> new AircraftResponse(
                        aircraft.getId(),
                        aircraft.getModel(),
                        aircraft.getCapacity()))
                .toList();
    }
}
