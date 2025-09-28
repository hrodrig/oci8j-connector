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

    @Bean
    public DataSource oracle8iDataSource() {
        DriverManagerDataSource dataSource = new DriverManagerDataSource();
        
        // Build connection URL for Oracle 8i
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
        
        return dataSource;
    }

    @Bean
    public JdbcTemplate oracle8iJdbcTemplate(DataSource oracle8iDataSource) {
        return new JdbcTemplate(oracle8iDataSource);
    }
}
