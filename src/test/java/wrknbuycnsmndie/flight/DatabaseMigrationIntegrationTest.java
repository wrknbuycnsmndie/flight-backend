package wrknbuycnsmndie.flight;

import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.sql.Timestamp;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@Testcontainers
@SpringBootTest
class DatabaseMigrationIntegrationTest {

    @Container
    static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>("postgres:18-alpine");

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private Flyway flyway;

    @DynamicPropertySource
    static void configureDatabase(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", POSTGRES::getJdbcUrl);
        registry.add("spring.datasource.username", POSTGRES::getUsername);
        registry.add("spring.datasource.password", POSTGRES::getPassword);
        registry.add("spring.flyway.enabled", () -> true);
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "validate");
    }

    @Test
    void appliesSchemaMigrationToPostgres() {
        Integer tableCount = jdbcTemplate.queryForObject("""
                SELECT COUNT(*)
                FROM information_schema.tables
                WHERE table_schema = 'public'
                  AND table_name IN ('airports', 'aircrafts', 'flights', 'passengers')
                """, Integer.class);

        assertThat(tableCount).isEqualTo(4);
        assertThat(flyway.info().applied()).hasSize(2);
        assertThat(flyway.info().applied()[0].getVersion().getVersion()).isEqualTo("1");
        assertThat(flyway.info().applied()[1].getVersion().getVersion()).isEqualTo("2");
    }

    @Test
    void loadsExpectedSeedData() {
        assertThat(countRows("airports")).isEqualTo(5);
        assertThat(countRows("aircrafts")).isEqualTo(3);
        assertThat(countRows("flights")).isEqualTo(10);
        assertThat(countRows("passengers")).isEqualTo(20);

        Integer orphanedFlights = jdbcTemplate.queryForObject("""
                SELECT COUNT(*)
                FROM flights f
                LEFT JOIN airports departure ON departure.id = f.departure_airport_id
                LEFT JOIN airports arrival ON arrival.id = f.arrival_airport_id
                LEFT JOIN aircrafts aircraft ON aircraft.id = f.aircraft_id
                WHERE departure.id IS NULL OR arrival.id IS NULL OR aircraft.id IS NULL
                """, Integer.class);

        Integer flightsWithInvalidPassengerCount = jdbcTemplate.queryForObject("""
                SELECT COUNT(*)
                FROM (
                    SELECT f.id
                    FROM flights f
                    LEFT JOIN passengers p ON p.flight_id = f.id
                    GROUP BY f.id
                    HAVING COUNT(p.id) NOT BETWEEN 2 AND 3
                ) invalid_flights
                """, Integer.class);

        assertThat(orphanedFlights).isZero();
        assertThat(flightsWithInvalidPassengerCount).isZero();
    }

    @Test
    void createsRequiredIndexesAndConstraints() {
        Integer indexCount = jdbcTemplate.queryForObject("""
                SELECT COUNT(*)
                FROM pg_indexes
                WHERE schemaname = 'public'
                  AND indexname IN (
                      'ux_airports_code',
                      'ux_passengers_passport_number',
                      'ix_flights_departure_airport_id',
                      'ix_flights_arrival_airport_id',
                      'ix_flights_aircraft_id',
                      'ix_passengers_flight_id'
                  )
                """, Integer.class);

        Integer constraintCount = jdbcTemplate.queryForObject("""
                SELECT COUNT(*)
                FROM pg_constraint
                WHERE conname IN (
                    'chk_aircrafts_capacity_positive',
                    'fk_flights_departure_airport',
                    'fk_flights_arrival_airport',
                    'fk_flights_aircraft',
                    'chk_flights_different_airports',
                    'chk_flights_time_order',
                    'fk_passengers_flight'
                )
                """, Integer.class);

        assertThat(indexCount).isEqualTo(6);
        assertThat(constraintCount).isEqualTo(7);
    }

    @Test
    void cascadesPassengerDeletionWithFlightDeletion() {
        jdbcTemplate.update("INSERT INTO airports (code, name, city) VALUES ('AAA', 'Test Departure Airport', 'Test City A')");
        jdbcTemplate.update("INSERT INTO airports (code, name, city) VALUES ('BBB', 'Test Arrival Airport', 'Test City B')");
        jdbcTemplate.update("INSERT INTO aircrafts (model, capacity) VALUES ('Test Aircraft', 180)");

        Long departureAirportId = jdbcTemplate.queryForObject(
                "SELECT id FROM airports WHERE code = 'AAA'", Long.class);
        Long arrivalAirportId = jdbcTemplate.queryForObject(
                "SELECT id FROM airports WHERE code = 'BBB'", Long.class);
        Long aircraftId = jdbcTemplate.queryForObject(
                "SELECT id FROM aircrafts WHERE model = 'Test Aircraft'", Long.class);

        jdbcTemplate.update("""
                INSERT INTO flights (
                    flight_number,
                    departure_airport_id,
                    arrival_airport_id,
                    departure_time,
                    arrival_time,
                    aircraft_id
                ) VALUES (?, ?, ?, ?, ?, ?)
                """, "TEST123", departureAirportId, arrivalAirportId,
                Timestamp.valueOf("2026-08-01 10:00:00"),
                Timestamp.valueOf("2026-08-01 12:30:00"), aircraftId);

        Long flightId = jdbcTemplate.queryForObject(
                "SELECT id FROM flights WHERE flight_number = 'TEST123'", Long.class);
        jdbcTemplate.update("""
                INSERT INTO passengers (first_name, last_name, passport_number, flight_id)
                VALUES ('Test', 'Passenger', 'TEST-PASSPORT-1', ?)
                """, flightId);

        jdbcTemplate.update("DELETE FROM flights WHERE id = ?", flightId);

        Integer passengerCount = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM passengers WHERE flight_id = ?", Integer.class, flightId);
        assertThat(passengerCount).isZero();

        jdbcTemplate.update("DELETE FROM aircrafts WHERE model = 'Test Aircraft'");
        jdbcTemplate.update("DELETE FROM airports WHERE code IN ('AAA', 'BBB')");
    }

    @Test
    void rejectsInvalidDatabaseValues() {
        assertThrows(DataIntegrityViolationException.class, () -> jdbcTemplate.update(
                "INSERT INTO aircrafts (model, capacity) VALUES ('Invalid', 0)"));
    }

    @Test
    void doesNotApplyMigrationTwice() {
        flyway.migrate();

        Integer migrationCount = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM flyway_schema_history", Integer.class);
        assertThat(migrationCount).isEqualTo(2);
    }

    private int countRows(String tableName) {
        return jdbcTemplate.queryForObject("SELECT COUNT(*) FROM " + tableName, Integer.class);
    }
}
