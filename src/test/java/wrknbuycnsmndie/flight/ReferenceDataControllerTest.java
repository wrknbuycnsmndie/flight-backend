package wrknbuycnsmndie.flight;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import wrknbuycnsmndie.flight.aircraft.AircraftController;
import wrknbuycnsmndie.flight.aircraft.AircraftResponse;
import wrknbuycnsmndie.flight.aircraft.AircraftService;
import wrknbuycnsmndie.flight.airport.AirportController;
import wrknbuycnsmndie.flight.airport.AirportResponse;
import wrknbuycnsmndie.flight.airport.AirportService;

import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class ReferenceDataControllerTest {

    private final AirportService airportService = mock(AirportService.class);
    private final AircraftService aircraftService = mock(AircraftService.class);
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(
                new AirportController(airportService),
                new AircraftController(aircraftService))
                .build();
    }

    @Test
    void returnsAirportsAsDtoList() throws Exception {
        when(airportService.findAll()).thenReturn(List.of(
                new AirportResponse(1L, "SVO", "Sheremetyevo International Airport", "Moscow")));

        mockMvc.perform(get("/airports"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].code").value("SVO"))
                .andExpect(jsonPath("$[0].name").value("Sheremetyevo International Airport"))
                .andExpect(jsonPath("$[0].city").value("Moscow"));
    }

    @Test
    void returnsAircraftAsDtoList() throws Exception {
        when(aircraftService.findAll()).thenReturn(List.of(
                new AircraftResponse(1L, "Boeing 737", 180)));

        mockMvc.perform(get("/aircrafts"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].model").value("Boeing 737"))
                .andExpect(jsonPath("$[0].capacity").value(180));
    }

    @Test
    void returnsEmptyListsWithoutError() throws Exception {
        when(airportService.findAll()).thenReturn(List.of());
        when(aircraftService.findAll()).thenReturn(List.of());

        mockMvc.perform(get("/airports"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
        mockMvc.perform(get("/aircrafts"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }
}
