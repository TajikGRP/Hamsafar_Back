package com.tajikGRP.Hansafar;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Tag("integration")
@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.NONE,
        properties = {
                "spring.flyway.enabled=false",
                "spring.jpa.hibernate.ddl-auto=none"
        }
)
class DatabaseConnectionSmokeTest {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    void connectsToPostgresOverSsl() {
        Integer result = jdbcTemplate.queryForObject(
                "SELECT 1",
                Integer.class
        );

        Boolean sslEnabled = jdbcTemplate.queryForObject(
                "SELECT ssl FROM pg_stat_ssl WHERE pid = pg_backend_pid()",
                Boolean.class
        );

        assertEquals(1, result);
        assertTrue(Boolean.TRUE.equals(sslEnabled));
    }
}
