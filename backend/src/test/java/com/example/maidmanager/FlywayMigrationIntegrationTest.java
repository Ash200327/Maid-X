package com.example.maidmanager;

import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import javax.sql.DataSource;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
class FlywayMigrationIntegrationTest {

    @Autowired
    private DataSource dataSource;

    @Test
    @DisplayName("Verify all Flyway migrations execute successfully against datasource")
    void testFlywayMigrations() {
        Flyway flyway = Flyway.configure()
                .dataSource(dataSource)
                .locations("classpath:db/migration")
                .cleanDisabled(false)
                .load();

        flyway.clean();
        var result = flyway.migrate();

        assertThat(result.migrationsExecuted).isGreaterThanOrEqualTo(5);
        assertThat(result.success).isTrue();
    }
}
