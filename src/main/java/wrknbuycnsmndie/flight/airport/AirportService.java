package wrknbuycnsmndie.flight.airport;

import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;

import java.util.List;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class AirportService {

    private final AirportRepository airportRepository;

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
