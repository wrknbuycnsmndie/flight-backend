package wrknbuycnsmndie.flight;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.env.Environment;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest(properties = {
        "SPRING_DATASOURCE_URL=jdbc:h2:mem:flight-backend;MODE=PostgreSQL;DB_CLOSE_DELAY=-1",
        "SPRING_DATASOURCE_USERNAME=sa",
        "SPRING_DATASOURCE_PASSWORD="
})
@ActiveProfiles("test")
class FlightBackendApplicationTests {

    @Autowired
    private Environment environment;

    @Test
    void contextLoads() {
        assertEquals("flight-backend", environment.getProperty("spring.application.name"));
        assertEquals("/api", environment.getProperty("server.servlet.context-path"));
        assertEquals("false", environment.getProperty("spring.jpa.open-in-view"));
    }

}
