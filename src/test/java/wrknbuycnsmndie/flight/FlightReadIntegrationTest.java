package wrknbuycnsmndie.flight;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Testcontainers
@SpringBootTest
class FlightReadIntegrationTest {

    @Container
    static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>("postgres:18-alpine");

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
    void returnsFirstPageWithDefaultSizeAndPassengerCounts() throws Exception {
        mockMvc.perform(get("/api/flights").contextPath("/api"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(10)))
                .andExpect(jsonPath("$.page").value(0))
                .andExpect(jsonPath("$.size").value(10))
                .andExpect(jsonPath("$.totalElements").value(10))
                .andExpect(jsonPath("$.totalPages").value(1))
                .andExpect(jsonPath("$.content[0].id").value(1))
                .andExpect(jsonPath("$.content[0].flightNumber").value("SU101"))
                .andExpect(jsonPath("$.content[0].departureAirportCode").value("SVO"))
                .andExpect(jsonPath("$.content[0].arrivalAirportCode").value("LED"))
                .andExpect(jsonPath("$.content[0].passengerCount").value(2));
    }

    @Test
    void supportsExplicitPagination() throws Exception {
        mockMvc.perform(get("/api/flights?page=1&size=3").contextPath("/api"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(3)))
                .andExpect(jsonPath("$.page").value(1))
                .andExpect(jsonPath("$.size").value(3))
                .andExpect(jsonPath("$.totalElements").value(10))
                .andExpect(jsonPath("$.totalPages").value(4))
                .andExpect(jsonPath("$.content[0].flightNumber").value("SU104"));
    }

    @Test
    void returnsFlightDetailsWithRelatedData() throws Exception {
        mockMvc.perform(get("/api/flights/1").contextPath("/api"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.flightNumber").value("SU101"))
                .andExpect(jsonPath("$.departureAirport.id").value(1))
                .andExpect(jsonPath("$.departureAirport.code").value("SVO"))
                .andExpect(jsonPath("$.arrivalAirport.code").value("LED"))
                .andExpect(jsonPath("$.aircraft.model").value("Boeing 737"))
                .andExpect(jsonPath("$.aircraft.capacity").value(180))
                .andExpect(jsonPath("$.passengerCount").value(2));
    }

    @Test
    void returnsProblemDetailForUnknownFlight() throws Exception {
        mockMvc.perform(get("/api/flights/999").contextPath("/api"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.title").value("Resource Not Found"))
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.detail").value("Flight not found: 999"))
                .andExpect(jsonPath("$.path").value("/api/flights/999"));
    }
}
