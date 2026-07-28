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
import org.testcontainers.postgresql.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Testcontainers
@SpringBootTest
class PassengerIntegrationTest {

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

    @Test
    void getsPassengersForFlight() throws Exception {
        mockMvc.perform(get("/api/flights/2/passengers").contextPath("/api"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].firstName").value("Michael"))
                .andExpect(jsonPath("$[0].passportNumber").value("4010000003"))
                .andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    void createsPassenger() throws Exception {
        mockMvc.perform(post("/api/flights/1/passengers").contextPath("/api")
                        .contentType(APPLICATION_JSON)
                        .content(passengerRequest("Grace", "Lee", "5010000001")))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.firstName").value("Grace"))
                .andExpect(jsonPath("$.lastName").value("Lee"))
                .andExpect(jsonPath("$.passportNumber").value("5010000001"));
    }

    @Test
    void deletesPassenger() throws Exception {
        mockMvc.perform(post("/api/flights/1/passengers").contextPath("/api")
                        .contentType(APPLICATION_JSON)
                        .content(passengerRequest("Delete", "Me", "5010000002")))
                .andExpect(status().isCreated());
        Long passengerId = jdbcTemplate.queryForObject(
                "SELECT id FROM passengers WHERE passport_number = '5010000002'", Long.class);

        mockMvc.perform(delete("/api/passengers/" + passengerId).contextPath("/api"))
                .andExpect(status().isNoContent());

        assertThat(jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM passengers WHERE id = " + passengerId, Long.class)).isZero();
    }

    @Test
    void rejectsMissingRequiredFields() throws Exception {
        mockMvc.perform(post("/api/flights/1/passengers").contextPath("/api")
                        .contentType(APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Validation Failed"))
                .andExpect(jsonPath("$.errors.firstName").exists())
                .andExpect(jsonPath("$.errors.passportNumber").exists());
    }

    @Test
    void rejectsDuplicatePassport() throws Exception {
        mockMvc.perform(post("/api/flights/1/passengers").contextPath("/api")
                        .contentType(APPLICATION_JSON)
                        .content(passengerRequest("Another", "John", "4010000001")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detail").value("Passport number already exists: 4010000001"));
    }

    @Test
    void rejectsPassengerWhenFlightIsFull() throws Exception {
        jdbcTemplate.update("INSERT INTO aircrafts (model, capacity) VALUES ('One Seat Aircraft', 1)");
        Long aircraftId = jdbcTemplate.queryForObject(
                "SELECT id FROM aircrafts WHERE model = 'One Seat Aircraft'", Long.class);
        jdbcTemplate.update("""
                INSERT INTO flights (flight_number, departure_airport_id, arrival_airport_id,
                                     departure_time, arrival_time, aircraft_id)
                VALUES ('CAP001', 1, 2, '2026-09-01 10:00:00', '2026-09-01 12:00:00', ?)
                """, aircraftId);
        Long flightId = jdbcTemplate.queryForObject(
                "SELECT id FROM flights WHERE flight_number = 'CAP001'", Long.class);

        mockMvc.perform(post("/api/flights/" + flightId + "/passengers").contextPath("/api")
                        .contentType(APPLICATION_JSON)
                        .content(passengerRequest("First", "Passenger", "5010000003")))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/flights/" + flightId + "/passengers").contextPath("/api")
                        .contentType(APPLICATION_JSON)
                        .content(passengerRequest("Second", "Passenger", "5010000004")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detail").value("Flight has reached the aircraft capacity"));
    }

    @Test
    void rejectsUnknownFlight() throws Exception {
        mockMvc.perform(get("/api/flights/999/passengers").contextPath("/api"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.detail").value("Flight not found: 999"));
    }

    @Test
    void rejectsUnknownPassenger() throws Exception {
        mockMvc.perform(delete("/api/passengers/999").contextPath("/api"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.detail").value("Passenger not found: 999"));
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
}
