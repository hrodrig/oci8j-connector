package com.connectors.oracle8i.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

import javax.sql.DataSource;

/**
 * Configuration for Oracle 8i connection
 * Supports environment variables for configuration override
 */
@Configuration
public class Oracle8iConfig {

    @Value("${oracle8i.host:1.2.3.4}")
    private String host;

    @Value("${oracle8i.port:1521}")
    private String port;

    @Value("${oracle8i.sid:orcl}")
    private String sid;

    @Value("${oracle8i.username:username}")
    private String username;

    @Value("${oracle8i.password:password}")
    private String password;

    @Value("${oracle8i.driver:oracle.jdbc.driver.OracleDriver}")
    private String driver;

    @Value("${oracle8i.connection_timeout:10000}")
    private int connectionTimeout;

    @Value("${oracle8i.socket_timeout:30000}")
    private int socketTimeout;

    @Value("${oracle8i.query_timeout:60000}")
    private int queryTimeout;

    @Bean
    public DataSource oracle8iDataSource() {
        DriverManagerDataSource dataSource = new DriverManagerDataSource();

        // Build connection URL for Oracle 8i with timeout parameters
        String url = String.format("jdbc:oracle:thin:@%s:%s:%s", host, port, sid);
        
        // Add timeout parameters to the URL
        url += "?oracle.net.CONNECT_TIMEOUT=" + connectionTimeout;
        url += "&oracle.net.READ_TIMEOUT=" + socketTimeout;
        url += "&oracle.jdbc.ReadTimeout=" + socketTimeout;

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
        System.out.println("   Connection Timeout: " + connectionTimeout + "ms");
        System.out.println("   Socket Timeout: " + socketTimeout + "ms");
        System.out.println("   Query Timeout: " + queryTimeout + "ms");

        return dataSource;
    }

    @Bean
    public JdbcTemplate oracle8iJdbcTemplate(DataSource oracle8iDataSource) {
        JdbcTemplate jdbcTemplate = new JdbcTemplate(oracle8iDataSource);
        jdbcTemplate.setQueryTimeout(queryTimeout / 1000); // Convert to seconds
        return jdbcTemplate;
    }
}
