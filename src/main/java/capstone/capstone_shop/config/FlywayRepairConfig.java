package capstone.capstone_shop.config;

import org.flywaydb.core.Flyway;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

@Configuration
public class FlywayRepairConfig {
    private static final Logger log = LoggerFactory.getLogger(FlywayRepairConfig.class);

    @Bean
    ApplicationRunner flywayRepairRunner(Flyway flyway, Environment env) {
        return args -> {
            // app.flyway.repair-now 또는 APP_FLYWAY_REPAIR_NOW 두 키 모두 지원
            boolean repairNow =
                    env.getProperty("app.flyway.repair-now", Boolean.class, false)
                            || env.getProperty("APP_FLYWAY_REPAIR_NOW", Boolean.class, false);

            if (repairNow) {
                log.warn(">>> Running Flyway.repair() (one-off) ...");
                flyway.repair();
                log.warn(">>> Flyway.repair() completed.");
            } else {
                log.info("Flyway.repair() skipped (repair flag is false).");
            }
        };
    }
}
