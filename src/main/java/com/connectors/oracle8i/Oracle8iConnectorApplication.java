package com.connectors.oracle8i;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;

/**
 * Main application for Oracle 8i Connector
 * Connects directly to Oracle 8i using JDBC with classes12.jar
 */
@SpringBootApplication(exclude = {
    DataSourceAutoConfiguration.class
})
public class Oracle8iConnectorApplication {

    public static void main(String[] args) {
        // Configure NLS exactly as in Oracle 8i
        System.setProperty("NLS_LANG", "AMERICAN_AMERICA.WE8MSWIN1252");
        System.setProperty("oracle.jdbc.timezoneAsRegion", "false");
        
        // Configure system locale
        System.setProperty("user.language", "en");
        System.setProperty("user.country", "US");
        System.setProperty("user.variant", "");
        System.setProperty("file.encoding", "Cp1252");
        
        System.out.println("🚀 Starting Oracle 8i Connector...");
        System.out.println("📊 Connecting to Oracle 8i...");
        System.out.println("🔧 NLS_LANG: AMERICAN_AMERICA.WE8MSWIN1252");
        System.out.println("🔧 Locale: en_US, Encoding: Cp1252");
        
        SpringApplication.run(Oracle8iConnectorApplication.class, args);
        
        System.out.println("✅ Oracle 8i Connector started successfully");
        System.out.println("🌐 API available at: http://localhost:8080");
        System.out.println("📝 Query endpoint: POST /api/v1/oci8j-connector/query");
        System.out.println("🔍 Health check: GET /api/v1/oci8j-connector/healthz");
        System.out.println("ℹ️  Info endpoint: GET /api/v1/oci8j-connector/info");
    }
}
