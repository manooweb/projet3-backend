package com.chatop.api.system.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;
import java.util.Map;
import java.util.Set;

import javax.sql.DataSource;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.info.BuildProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.chatop.api.config.properties.ChatopProperties;
import com.chatop.api.config.properties.ChatopPropertiesTestFactory;
import com.chatop.api.system.controller.HealthController.HealthResponse;
import com.chatop.api.system.controller.HealthController.SchemaHealthResponse;

class HealthControllerTest {

    private static final Instant BUILD_TIME = Instant.parse("2026-07-13T10:00:00Z");

    private DataSource dataSource;
    private HealthController healthController;

    @BeforeEach
    void setUp() {
        ChatopProperties chatopProperties = ChatopPropertiesTestFactory.defaultProperties();
        dataSource = mock(DataSource.class);
        ObjectProvider<BuildProperties> buildPropertiesProvider = mock();
        BuildProperties buildProperties = mock();
        when(buildProperties.getVersion()).thenReturn("1.2.3");
        when(buildProperties.getTime()).thenReturn(BUILD_TIME);
        when(buildPropertiesProvider.getIfAvailable()).thenReturn(buildProperties);

        healthController = new HealthController(chatopProperties, dataSource, buildPropertiesProvider);
    }

    @Test
    void healthReturnsOkWhenDatabaseConnectionIsValid() throws Exception {
        Connection connection = mock(Connection.class);
        DatabaseMetaData metaData = mock(DatabaseMetaData.class);
        when(dataSource.getConnection()).thenReturn(connection);
        when(connection.isValid(1)).thenReturn(true);
        when(connection.getMetaData()).thenReturn(metaData);
        when(metaData.getDatabaseProductName()).thenReturn("MySQL");
        when(metaData.getDatabaseProductVersion()).thenReturn("9.7.1");

        HealthResponse response = healthController.health();

        assertThat(response.status()).isEqualTo("OK");
        assertThat(response.application().name()).isEqualTo("Châtop API");
        assertThat(response.application().status()).isEqualTo("OK");
        assertThat(response.application().version()).isEqualTo("1.2.3");
        assertThat(response.application().timestamp()).isEqualTo(BUILD_TIME);
        assertThat(response.database().status()).isEqualTo("OK");
        assertThat(response.database().version()).isEqualTo("MySQL 9.7.1");
        assertThat(response.database().timestamp()).isNotNull();
    }

    @Test
    void healthReturnsDegradedWhenDatabaseConnectionFails() throws Exception {
        when(dataSource.getConnection()).thenThrow(new SQLException("Database unavailable"));

        HealthResponse response = healthController.health();

        assertThat(response.status()).isEqualTo("DEGRADED");
        assertThat(response.application().status()).isEqualTo("OK");
        assertThat(response.application().version()).isEqualTo("1.2.3");
        assertThat(response.application().timestamp()).isEqualTo(BUILD_TIME);
        assertThat(response.database().status()).isEqualTo("DOWN");
        assertThat(response.database().version()).isNull();
        assertThat(response.database().timestamp()).isNotNull();
    }

    @Test
    void schemaReturnsOkWhenExpectedTablesAndColumnsExist() throws Exception {
        Connection connection = schemaConnection(Set.of("USERS", "RENTALS", "MESSAGES"), expectedColumns());
        when(dataSource.getConnection()).thenReturn(connection);

        SchemaHealthResponse response = healthController.schema();

        assertThat(response.status()).isEqualTo("OK");
        assertThat(response.missing()).isEmpty();
        assertThat(response.timestamp()).isNotNull();
    }

    @Test
    void schemaReturnsInvalidWhenExpectedTableIsMissing() throws Exception {
        Connection connection = schemaConnection(Set.of("USERS", "RENTALS"), expectedColumns());
        when(dataSource.getConnection()).thenReturn(connection);

        SchemaHealthResponse response = healthController.schema();

        assertThat(response.status()).isEqualTo("INVALID");
        assertThat(response.missing()).contains("MESSAGES");
        assertThat(response.timestamp()).isNotNull();
    }

    @Test
    void schemaReturnsDownWhenDatabaseConnectionFails() throws Exception {
        when(dataSource.getConnection()).thenThrow(new SQLException("Database unavailable"));

        SchemaHealthResponse response = healthController.schema();

        assertThat(response.status()).isEqualTo("DOWN");
        assertThat(response.missing()).isEmpty();
        assertThat(response.timestamp()).isNotNull();
    }

    private Connection schemaConnection(Set<String> existingTables, Map<String, Set<String>> columnsByTable) throws Exception {
        Connection connection = mock(Connection.class);
        DatabaseMetaData metaData = mock(DatabaseMetaData.class);
        when(connection.getCatalog()).thenReturn("chatop_db");
        when(connection.getMetaData()).thenReturn(metaData);

        when(metaData.getTables(any(), any(), any(), any())).thenAnswer(invocation -> {
            String tableName = invocation.getArgument(2, String.class).toUpperCase();
            boolean exists = existingTables.contains(tableName);

            return resultSet(exists, tableName);
        });

        when(metaData.getColumns(any(), any(), any(), any())).thenAnswer(invocation -> {
            String tableName = invocation.getArgument(2, String.class).toUpperCase();
            String columnName = invocation.getArgument(3, String.class).toLowerCase();
            boolean exists = columnsByTable.getOrDefault(tableName, Set.of()).contains(columnName);

            return resultSet(exists, tableName);
        });

        return connection;
    }

    private ResultSet resultSet(boolean exists, String tableName) throws SQLException {
        ResultSet resultSet = mock(ResultSet.class);
        when(resultSet.next()).thenReturn(exists);
        when(resultSet.getString("TABLE_NAME")).thenReturn(tableName);

        return resultSet;
    }

    private Map<String, Set<String>> expectedColumns() {
        return Map.of(
            "USERS",
            Set.of("id", "email", "name", "password", "created_at", "updated_at"),
            "RENTALS",
            Set.of("id", "name", "surface", "price", "picture", "description", "owner_id", "created_at", "updated_at"),
            "MESSAGES",
            Set.of("id", "rental_id", "user_id", "message", "created_at", "updated_at")
        );
    }
}
