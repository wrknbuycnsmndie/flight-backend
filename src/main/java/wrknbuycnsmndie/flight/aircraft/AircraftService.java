package wrknbuycnsmndie.flight.aircraft;

import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class AircraftService {

    private final AircraftRepository aircraftRepository;

    public AircraftService(AircraftRepository aircraftRepository) {
        this.aircraftRepository = aircraftRepository;
    }

    public List<AircraftResponse> findAll() {
        return aircraftRepository.findAll(Sort.by(Sort.Direction.ASC, "id")).stream()
                .map(aircraft -> new AircraftResponse(
                        aircraft.getId(),
                        aircraft.getModel(),
                        aircraft.getCapacity()))
                .toList();
    }
}
