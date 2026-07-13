package com.chatop.api.system.controller;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.info.BuildProperties;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.chatop.api.config.properties.ChatopProperties;
import com.chatop.api.config.properties.SystemProperties;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;

/**
 * Exposes system health information.
 */
@Tag(name = "System", description = "System status endpoints")
@RestController
@RequestMapping("/api")
public class HealthController {

    private static final String SCHEMA_STATUS_INVALID = "INVALID";
    private static final String SCHEMA_STATUS_DOWN = "DOWN";
    private static final Map<String, List<String>> EXPECTED_SCHEMA = Map.of(
        "USERS",
        List.of("id", "email", "name", "password", "created_at", "updated_at"),
        "RENTALS",
        List.of("id", "name", "surface", "price", "picture", "description", "owner_id", "created_at", "updated_at"),
        "MESSAGES",
        List.of("id", "rental_id", "user_id", "message", "created_at", "updated_at")
    );

    private final SystemProperties system;
    private final DataSource dataSource;
    private final BuildProperties buildProperties;

    public HealthController(
        ChatopProperties chatopProperties,
        DataSource dataSource,
        ObjectProvider<BuildProperties> buildPropertiesProvider
    ) {
        this.system = chatopProperties.getSystem();
        this.dataSource = dataSource;
        this.buildProperties = buildPropertiesProvider.getIfAvailable();
    }

    /**
     * Returns the API health status.
     *
     * @return the current health status
     */
    @Operation(summary = "Get API health status")
    @ApiResponse(
        responseCode = "200",
        description = "API health status",
        content = @Content(schema = @Schema(implementation = HealthResponse.class))
    )
    @GetMapping("/health")
    public HealthResponse health() {
        DatabaseHealthResponse database = databaseHealth();
        ApplicationHealthResponse application = new ApplicationHealthResponse(
            system.getName(),
            system.getHealthStatus(),
            applicationVersion(),
            applicationTimestamp()
        );

        return new HealthResponse(overallStatus(application, database), application, database);
    }

    /**
     * Checks whether the database schema required by the API is present.
     *
     * @return the current database schema status
     */
    @Operation(summary = "Check API database schema")
    @ApiResponse(
        responseCode = "200",
        description = "API database schema status",
        content = @Content(schema = @Schema(implementation = SchemaHealthResponse.class))
    )
    @GetMapping("/health/schema")
    public SchemaHealthResponse schema() {
        try (Connection connection = dataSource.getConnection()) {
            List<String> missing = missingSchemaElements(connection);
            String status = missing.isEmpty() ? system.getHealthStatus() : SCHEMA_STATUS_INVALID;

            return new SchemaHealthResponse(status, missing, Instant.now());
        } catch (SQLException | RuntimeException exception) {
            return new SchemaHealthResponse(SCHEMA_STATUS_DOWN, List.of(), Instant.now());
        }
    }

    private String overallStatus(ApplicationHealthResponse application, DatabaseHealthResponse database) {
        return system.getHealthStatus().equals(application.status()) && system.getHealthStatus().equals(database.status())
            ? system.getHealthStatus()
            : "DEGRADED";
    }

    private DatabaseHealthResponse databaseHealth() {
        try (Connection connection = dataSource.getConnection()) {
            boolean valid = connection.isValid(1);

            return valid
                ? new DatabaseHealthResponse(system.getHealthStatus(), databaseVersion(connection), Instant.now())
                : new DatabaseHealthResponse("DOWN", null, Instant.now());
        } catch (SQLException | RuntimeException exception) {
            return new DatabaseHealthResponse("DOWN", null, Instant.now());
        }
    }

    private String applicationVersion() {
        return buildProperties != null ? buildProperties.getVersion() : system.getVersion();
    }

    private Instant applicationTimestamp() {
        return buildProperties != null ? buildProperties.getTime() : Instant.now();
    }

    private String databaseVersion(Connection connection) throws SQLException {
        DatabaseMetaData metaData = connection.getMetaData();

        return "%s %s".formatted(metaData.getDatabaseProductName(), metaData.getDatabaseProductVersion());
    }

    private List<String> missingSchemaElements(Connection connection) throws SQLException {
        DatabaseMetaData metaData = connection.getMetaData();
        String catalog = connection.getCatalog();
        List<String> missing = new ArrayList<>();

        for (Map.Entry<String, List<String>> table : EXPECTED_SCHEMA.entrySet()) {
            String tableName = table.getKey();
            String resolvedTableName = resolveTableName(metaData, catalog, tableName);

            if (resolvedTableName == null) {
                missing.add(tableName);
                continue;
            }

            for (String column : table.getValue()) {
                if (!columnExists(metaData, catalog, resolvedTableName, column)) {
                    missing.add(tableName + "." + column);
                }
            }
        }

        return missing;
    }

    private String resolveTableName(DatabaseMetaData metaData, String catalog, String tableName) throws SQLException {
        for (String candidate : List.of(tableName, tableName.toLowerCase(), tableName.toUpperCase())) {
            try (ResultSet tables = metaData.getTables(catalog, null, candidate, new String[] {"TABLE"})) {
                if (tables.next()) {
                    return tables.getString("TABLE_NAME");
                }
            }
        }

        return null;
    }

    private boolean columnExists(DatabaseMetaData metaData, String catalog, String tableName, String column) throws SQLException {
        for (String candidate : List.of(column, column.toLowerCase(), column.toUpperCase())) {
            try (ResultSet columns = metaData.getColumns(catalog, null, tableName, candidate)) {
                if (columns.next()) {
                    return true;
                }
            }
        }

        return false;
    }

    public record HealthResponse(String status, ApplicationHealthResponse application, DatabaseHealthResponse database) {
    }

    public record ApplicationHealthResponse(String name, String status, String version, Instant timestamp) {
    }

    public record DatabaseHealthResponse(String status, String version, Instant timestamp) {
    }

    public record SchemaHealthResponse(String status, List<String> missing, Instant timestamp) {
    }
}
