package org.example.herizon.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

/**
 * Normalizes legacy static asset URLs stored in the database so they point to the
 * externally reachable base URL instead of localhost.
 */
@Component
public class StaticUrlMigrationRunner implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(StaticUrlMigrationRunner.class);
    private static final String LEGACY_PREFIX = "http://localhost:8080/api";

    private final JdbcTemplate jdbcTemplate;
    private final String publicBaseUrl;

    public StaticUrlMigrationRunner(JdbcTemplate jdbcTemplate,
                                    @Value("${app.public-base-url:}") String publicBaseUrl) {
        this.jdbcTemplate = jdbcTemplate;
        this.publicBaseUrl = publicBaseUrl;
    }

    @Override
    public void run(ApplicationArguments args) {
        if (!StringUtils.hasText(publicBaseUrl)) {
            log.debug("Public base URL not configured; skipping static asset URL migration.");
            return;
        }

        String normalizedBase = normalizeBase(publicBaseUrl);
        if (LEGACY_PREFIX.equals(normalizedBase)) {
            log.debug("Public base URL matches legacy prefix; skipping migration.");
            return;
        }

        migrateColumn("users", "avatar", normalizedBase);
        migrateColumn("posts", "image_urls", normalizedBase);
    }

    private void migrateColumn(String table, String column, String normalizedBase) {
        String likePattern = LEGACY_PREFIX + "%";
        int updated = jdbcTemplate.update(
                "UPDATE " + table + " SET " + column + " = REPLACE(" + column + ", ?, ?) WHERE " + column + " LIKE ?",
                LEGACY_PREFIX, normalizedBase, likePattern
        );
        if (updated > 0) {
            log.info("Migrated {} record(s) in {}.{} from {} to {}.", updated, table, column, LEGACY_PREFIX, normalizedBase);
        } else {
            log.debug("No {}.{} records required migration.", table, column);
        }
    }

    private String normalizeBase(String base) {
        String trimmed = base.trim();
        return trimmed.endsWith("/") ? trimmed.substring(0, trimmed.length() - 1) : trimmed;
    }
}
