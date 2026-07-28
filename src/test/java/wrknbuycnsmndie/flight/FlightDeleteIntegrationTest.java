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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Testcontainers
@SpringBootTest
class FlightDeleteIntegrationTest {

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
    void deletesFlightAndItsPassengers() throws Exception {
        assertThat(count("SELECT COUNT(*) FROM flights WHERE id = 1")).isEqualTo(1);
        assertThat(count("SELECT COUNT(*) FROM passengers WHERE flight_id = 1")).isGreaterThan(0);

        mockMvc.perform(delete("/api/flights/1").contextPath("/api"))
                .andExpect(status().isNoContent());

        assertThat(count("SELECT COUNT(*) FROM flights WHERE id = 1")).isZero();
        assertThat(count("SELECT COUNT(*) FROM passengers WHERE flight_id = 1")).isZero();
    }

    @Test
    void rejectsUnknownFlight() throws Exception {
        mockMvc.perform(delete("/api/flights/999").contextPath("/api"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.detail").value("Flight not found: 999"));
    }

    private long count(String sql) {
        return jdbcTemplate.queryForObject(sql, Long.class);
    }
}
