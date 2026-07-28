package wrknbuycnsmndie.flight.airport;

import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class AirportService {

    private final AirportRepository airportRepository;

    public AirportService(AirportRepository airportRepository) {
        this.airportRepository = airportRepository;
    }

    public List<AirportResponse> findAll() {
        return airportRepository.findAll(Sort.by(Sort.Direction.ASC, "id")).stream()
                .map(airport -> new AirportResponse(
                        airport.getId(),
                        airport.getCode(),
                        airport.getName(),
                        airport.getCity()))
                .toList();
    }
}
