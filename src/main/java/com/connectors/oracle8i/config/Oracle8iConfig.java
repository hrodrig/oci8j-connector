package com.connectors.oracle8i.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

import javax.sql.DataSource;

/**
 * Configuration for Oracle 8i connection
 * Supports environment variables for configuration override
 * Priority: Environment variables > Spring properties > Default values
 */
@Configuration
public class Oracle8iConfig {

    @Autowired
    private Environment environment;

    // Read from environment variables first, then from Spring properties
    @Value("${oracle8i.host:1.2.3.4}")
    private String hostDefault;

    @Value("${oracle8i.port:1521}")
    private String portDefault;

    @Value("${oracle8i.sid:orcl}")
    private String sidDefault;

    @Value("${oracle8i.username:username}")
    private String usernameDefault;

    @Value("${oracle8i.password:password}")
    private String passwordDefault;

    @Value("${oracle8i.driver:oracle.jdbc.driver.OracleDriver}")
    private String driver;

    @Value("${oracle8i.query_timeout:60000}")
    private int queryTimeoutDefault;

    private String getHost() {
        return environment.getProperty("ORACLE_HOST", hostDefault);
    }

    private String getPort() {
        return environment.getProperty("ORACLE_PORT", portDefault);
    }

    private String getSid() {
        return environment.getProperty("ORACLE_SID", sidDefault);
    }

    private String getUsername() {
        return environment.getProperty("ORACLE_USERNAME", usernameDefault);
    }

    private String getPassword() {
        return environment.getProperty("ORACLE_PASSWORD", passwordDefault);
    }

    private int getQueryTimeout() {
        String timeout = environment.getProperty("ORACLE_QUERY_TIMEOUT");
        if (timeout != null) {
            try {
                return Integer.parseInt(timeout);
            } catch (NumberFormatException e) {
                return queryTimeoutDefault;
            }
        }
        return queryTimeoutDefault;
    }

    @Bean
    public DataSource oracle8iDataSource() {
        DriverManagerDataSource dataSource = new DriverManagerDataSource();

        // Get configuration values (environment variables take priority)
        String host = getHost();
        String port = getPort();
        String sid = getSid();
        String username = getUsername();
        String password = getPassword();
        int queryTimeout = getQueryTimeout();

        // Build connection URL for Oracle 8i
        // Oracle 8i (classes12.jar) only supports "host:port:sid" format
        String url = String.format("jdbc:oracle:thin:@%s:%s:%s", host, port, sid);
        
        // Configuration with thin driver
        dataSource.setDriverClassName(driver);
        dataSource.setUrl(url);
        dataSource.setUsername(username);
        dataSource.setPassword(password);

        // Configuration log (without password)
        System.out.println("🔗 Configuring Oracle 8i connection:");
        System.out.println("   Host: " + host);
        System.out.println("   Port: " + port);
        System.out.println("   SID: " + sid);
        System.out.println("   User: " + username);
        System.out.println("   URL: " + url);
        System.out.println("   Driver: " + driver);
        System.out.println("   Query Timeout: " + queryTimeout + "ms");

        return dataSource;
    }

    @Bean
    public JdbcTemplate oracle8iJdbcTemplate(DataSource oracle8iDataSource) {
        JdbcTemplate jdbcTemplate = new JdbcTemplate(oracle8iDataSource);
        jdbcTemplate.setQueryTimeout(getQueryTimeout() / 1000); // Convert to seconds
        return jdbcTemplate;
    }
}
