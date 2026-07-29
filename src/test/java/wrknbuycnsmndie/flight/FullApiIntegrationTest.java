package wrknbuycnsmndie.flight;

import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.testcontainers.postgresql.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.http.MediaType.APPLICATION_PROBLEM_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Testcontainers
@SpringBootTest
class FullApiIntegrationTest {

    @Container
    static final PostgreSQLContainer POSTGRES = new PostgreSQLContainer("postgres:18-alpine");

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

    @BeforeEach
    @AfterEach
    void cleanEndToEndFixtures() {
        jdbcTemplate.update("DELETE FROM flights WHERE flight_number LIKE 'E2E-%'");
        jdbcTemplate.update("DELETE FROM aircrafts WHERE model LIKE 'E2E-%'");
    }

    @Test
    void startsWithMigratedSeededDatabaseAndServesReferenceData() throws Exception {
        assertThat(count("SELECT COUNT(*) FROM flyway_schema_history")).isEqualTo(2);
        assertThat(count("SELECT COUNT(*) FROM airports")).isGreaterThanOrEqualTo(5);
        assertThat(count("SELECT COUNT(*) FROM flights")).isGreaterThanOrEqualTo(10);

        mockMvc.perform(get("/api/airports").contextPath("/api"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].code").value("SVO"));
    }

    @Test
    void runsFlightAndPassengerHttpLifecycleWithCascadeDelete() throws Exception {
        Long flightId = createFlight("E2E-FLIGHT", 1L);

        mockMvc.perform(get("/api/flights/" + flightId).contextPath("/api"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.flightNumber").value("E2E-FLIGHT"));

        mockMvc.perform(put("/api/flights/" + flightId).contextPath("/api")
                        .contentType(APPLICATION_JSON)
                        .content(flightRequest("E2E-FLIGHT-UPDATED", 1L)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.flightNumber").value("E2E-FLIGHT-UPDATED"));

        MvcResult deletedPassengerResult = mockMvc.perform(post("/api/flights/" + flightId + "/passengers")
                        .contextPath("/api")
                        .contentType(APPLICATION_JSON)
                        .content(passengerRequest("Delete", "Passenger", "E2E-PASSPORT-1")))
                .andExpect(status().isCreated())
                .andReturn();
        Long deletedPassengerId = ((Number) JsonPath.read(
                deletedPassengerResult.getResponse().getContentAsString(), "$.id")).longValue();

        MvcResult remainingPassengerResult = mockMvc.perform(post("/api/flights/" + flightId + "/passengers")
                        .contextPath("/api")
                        .contentType(APPLICATION_JSON)
                        .content(passengerRequest("Keep", "Passenger", "E2E-PASSPORT-2")))
                .andExpect(status().isCreated())
                .andReturn();
        Long remainingPassengerId = ((Number) JsonPath.read(
                remainingPassengerResult.getResponse().getContentAsString(), "$.id")).longValue();

        mockMvc.perform(delete("/api/passengers/" + deletedPassengerId).contextPath("/api"))
                .andExpect(status().isNoContent());
        assertThat(count("SELECT COUNT(*) FROM passengers WHERE id = ?", deletedPassengerId)).isZero();

        mockMvc.perform(get("/api/flights/" + flightId + "/passengers").contextPath("/api"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(remainingPassengerId))
                .andExpect(jsonPath("$[0].passportNumber").value("E2E-PASSPORT-2"));

        mockMvc.perform(delete("/api/flights/" + flightId).contextPath("/api"))
                .andExpect(status().isNoContent());

        assertThat(count("SELECT COUNT(*) FROM flights WHERE id = ?", flightId)).isZero();
        assertThat(count("SELECT COUNT(*) FROM passengers WHERE id = ?", remainingPassengerId)).isZero();
    }

    @Test
    void rejectsCapacityWithoutPartiallyPersistingPassenger() throws Exception {
        jdbcTemplate.update("INSERT INTO aircrafts (model, capacity) VALUES ('E2E-One-Seat', 1)");
        Long aircraftId = jdbcTemplate.queryForObject(
                "SELECT id FROM aircrafts WHERE model = 'E2E-One-Seat'", Long.class);
        Long flightId = createFlight("E2E-CAPACITY", aircraftId);

        mockMvc.perform(post("/api/flights/" + flightId + "/passengers").contextPath("/api")
                        .contentType(APPLICATION_JSON)
                        .content(passengerRequest("First", "Passenger", "E2E-PASSPORT-2")))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/flights/" + flightId + "/passengers").contextPath("/api")
                        .contentType(APPLICATION_JSON)
                        .content(passengerRequest("Second", "Passenger", "E2E-PASSPORT-3")))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.detail").value("Flight has reached the aircraft capacity"));

        assertThat(count("SELECT COUNT(*) FROM passengers WHERE flight_id = ?", flightId)).isEqualTo(1);
    }

    @Test
    void returnsProblemDetailsForUnknownResourcesAcrossTheHttpCycle() throws Exception {
        mockMvc.perform(get("/api/flights/999/passengers").contextPath("/api"))
                .andExpect(status().isNotFound())
                .andExpect(content().contentTypeCompatibleWith(APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.path").value("/api/flights/999/passengers"));

        mockMvc.perform(delete("/api/passengers/999").contextPath("/api"))
                .andExpect(status().isNotFound())
                .andExpect(content().contentTypeCompatibleWith(APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.path").value("/api/passengers/999"));
    }

    private Long createFlight(String flightNumber, Long aircraftId) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/flights").contextPath("/api")
                        .contentType(APPLICATION_JSON)
                        .content(flightRequest(flightNumber, aircraftId)))
                .andExpect(status().isCreated())
                .andReturn();
        return ((Number) JsonPath.read(result.getResponse().getContentAsString(), "$.id")).longValue();
    }

    private String flightRequest(String flightNumber, Long aircraftId) {
        return """
                {
                  "flightNumber": "%s",
                  "departureAirportId": 1,
                  "arrivalAirportId": 2,
                  "departureTime": "2026-09-01T10:00:00",
                  "arrivalTime": "2026-09-01T12:30:00",
                  "aircraftId": %d
                }
                """.formatted(flightNumber, aircraftId);
    }

    private String passengerRequest(String firstName, String lastName, String passportNumber) {
        return """
                {
                  "firstName": "%s",
                  "lastName": "%s",
                  "passportNumber": "%s"
                }
                """.formatted(firstName, lastName, passportNumber);
    }

    private Long count(String sql, Object... args) {
        return jdbcTemplate.queryForObject(sql, Long.class, args);
    }
}
