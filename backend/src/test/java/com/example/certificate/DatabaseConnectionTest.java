package com.example.certificate;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DatabaseMetaData;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
class DatabaseConnectionTest {

    @Autowired
    private DataSource dataSource;

    @Test
    void testDatabaseConnection() throws Exception {
        assertNotNull(dataSource, "DataSource should not be null");
        
        try (Connection connection = dataSource.getConnection()) {
            assertNotNull(connection, "Connection should not be null");
            assertTrue(connection.isValid(1), "Connection should be valid");
            
            DatabaseMetaData metaData = connection.getMetaData();
            String databaseProductName = metaData.getDatabaseProductName();
            String databaseVersion = metaData.getDatabaseProductVersion();
            
            assertEquals("MySQL", databaseProductName, "Database should be MySQL");
            assertTrue(databaseVersion.startsWith("8."), "Database version should be 8.x");
            
            System.out.println("Database connection successful!");
            System.out.println("Database: " + databaseProductName + " " + databaseVersion);
        }
    }
}