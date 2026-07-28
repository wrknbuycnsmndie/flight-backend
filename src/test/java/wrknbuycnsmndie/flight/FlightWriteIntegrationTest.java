package wrknbuycnsmndie.flight;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Testcontainers
@SpringBootTest
class FlightWriteIntegrationTest {

    @Container
    static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>("postgres:18-alpine");

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private MockMvc mockMvc;

    @Autowired
    void setUpMockMvc(WebApplicationContext context) {
        mockMvc = MockMvcBuilders.webAppContextSetup(context).build();
    }

    @DynamicPropertySource
    static void configureDatabase(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", POSTGRES::getJdbcUrl);
        registry.add("spring.datasource.username", POSTGRES::getUsername);
        registry.add("spring.datasource.password", POSTGRES::getPassword);
        registry.add("spring.flyway.enabled", () -> true);
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "validate");
    }

    @Test
    void createsFlight() throws Exception {
        mockMvc.perform(post("/api/flights").contextPath("/api")
                        .contentType(APPLICATION_JSON)
                        .content(validRequest("NEW101", 1, 2, 1)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.flightNumber").value("NEW101"))
                .andExpect(jsonPath("$.departureAirport.code").value("SVO"))
                .andExpect(jsonPath("$.arrivalAirport.code").value("LED"))
                .andExpect(jsonPath("$.aircraft.model").value("Boeing 737"))
                .andExpect(jsonPath("$.passengerCount").value(0));
    }

    @Test
    void updatesFlight() throws Exception {
        mockMvc.perform(put("/api/flights/1").contextPath("/api")
                        .contentType(APPLICATION_JSON)
                        .content(validRequest("UPDATED101", 3, 4, 3)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.flightNumber").value("UPDATED101"))
                .andExpect(jsonPath("$.departureAirport.code").value("VVO"))
                .andExpect(jsonPath("$.arrivalAirport.code").value("KHV"))
                .andExpect(jsonPath("$.aircraft.model").value("Sukhoi Superjet 100"))
                .andExpect(jsonPath("$.passengerCount").value(2));
    }

    @Test
    void rejectsMissingRequiredFields() throws Exception {
        mockMvc.perform(post("/api/flights").contextPath("/api")
                        .contentType(APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Validation Failed"))
                .andExpect(jsonPath("$.errors.flightNumber").exists())
                .andExpect(jsonPath("$.errors.departureAirportId").exists());
    }

    @Test
    void rejectsUnknownRelatedEntities() throws Exception {
        mockMvc.perform(post("/api/flights").contextPath("/api")
                        .contentType(APPLICATION_JSON)
                        .content(validRequest("NEW102", 999, 2, 1)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.detail").value("Departure airport not found: 999"));
    }

    @Test
    void rejectsUnknownAircraft() throws Exception {
        mockMvc.perform(post("/api/flights").contextPath("/api")
                        .contentType(APPLICATION_JSON)
                        .content(validRequest("NEW105", 1, 2, 999)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.detail").value("Aircraft not found: 999"));
    }

    @Test
    void rejectsEqualAirports() throws Exception {
        mockMvc.perform(post("/api/flights").contextPath("/api")
                        .contentType(APPLICATION_JSON)
                        .content(validRequest("NEW103", 1, 1, 1)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detail").value("Departure and arrival airports must be different"));
    }

    @Test
    void rejectsInvalidTimeOrder() throws Exception {
        mockMvc.perform(post("/api/flights").contextPath("/api")
                        .contentType(APPLICATION_JSON)
                        .content("""
                                {
                                  "flightNumber": "NEW104",
                                  "departureAirportId": 1,
                                  "arrivalAirportId": 2,
                                  "departureTime": "2026-08-01T12:30:00",
                                  "arrivalTime": "2026-08-01T10:00:00",
                                  "aircraftId": 1
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detail").value("Departure time must be before arrival time"));
    }

    @Test
    void rejectsAircraftWithInsufficientCapacityOnUpdate() throws Exception {
        jdbcTemplate.update("INSERT INTO aircrafts (model, capacity) VALUES ('One Seat Aircraft', 1)");
        Long aircraftId = jdbcTemplate.queryForObject(
                "SELECT id FROM aircrafts WHERE model = 'One Seat Aircraft'", Long.class);

        mockMvc.perform(put("/api/flights/1").contextPath("/api")
                        .contentType(APPLICATION_JSON)
                        .content(validRequest("UPDATED102", 1, 2, aircraftId)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detail").value("Aircraft capacity is below the current passenger count"));
    }

    private String validRequest(String flightNumber, long departureAirportId, long arrivalAirportId, long aircraftId) {
        return """
                {
                  "flightNumber": "%s",
                  "departureAirportId": %d,
                  "arrivalAirportId": %d,
                  "departureTime": "2026-08-01T10:00:00",
                  "arrivalTime": "2026-08-01T12:30:00",
                  "aircraftId": %d
                }
                """.formatted(flightNumber, departureAirportId, arrivalAirportId, aircraftId);
    }
}
