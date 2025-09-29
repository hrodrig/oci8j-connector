package com.connectors.oracle8i.service;

import com.connectors.oracle8i.config.QuerySecurityConfig;
import com.connectors.oracle8i.model.QueryRequest;
import com.connectors.oracle8i.model.QueryResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;

/**
 * Service for executing SQL queries in Oracle 8i
 */
@Service
public class Oracle8iService {

    private static final Logger logger = LoggerFactory.getLogger(Oracle8iService.class);

    @Autowired
    private JdbcTemplate jdbcTemplate;
    
    @Autowired
    private QuerySecurityConfig querySecurityConfig;
    
    /**
     * Executes a SQL query and returns the results
     * @param request Request with SQL query and parameters
     * @return QueryResponse with the results
     */
    public QueryResponse executeQuery(QueryRequest request) {
        long startTime = System.currentTimeMillis();
        
        try {
            // Validate that the query is not empty
            if (request.getQuery() == null || request.getQuery().trim().isEmpty()) {
                return new QueryResponse(false, "SQL query cannot be empty");
            }
            
            // Validate query against forbidden keywords
            String forbiddenKeyword = querySecurityConfig.getFirstForbiddenKeyword(request.getQuery());
            if (forbiddenKeyword != null) {
                logger.warn("Query blocked due to forbidden keyword: {}", forbiddenKeyword);
                return new QueryResponse(false, "Query blocked due to forbidden keyword: " + forbiddenKeyword);
            }
            
            // Execute the query (any type of SQL query allowed)
            String query = request.getQuery().trim().toLowerCase();
            List<Map<String, Object>> results = null;
            int affectedRows = 0;
            String message = "Query executed successfully";
            
            if (query.startsWith("select") || query.startsWith("with")) {
                // For SELECT queries, return results
                if (request.getParameters() != null && request.getParameters().length > 0) {
                    results = jdbcTemplate.queryForList(request.getQuery(), request.getParameters());
                } else {
                    results = jdbcTemplate.queryForList(request.getQuery());
                }
                affectedRows = results.size();
            } else {
                // For other queries (INSERT, UPDATE, DELETE, etc.), execute update
                if (request.getParameters() != null && request.getParameters().length > 0) {
                    affectedRows = jdbcTemplate.update(request.getQuery(), request.getParameters());
                } else {
                    affectedRows = jdbcTemplate.update(request.getQuery());
                }
                message = "Query executed successfully. " + affectedRows + " rows affected.";
            }
            
            long executionTime = System.currentTimeMillis() - startTime;
            
            return new QueryResponse(
                true, 
                message, 
                results, 
                affectedRows, 
                executionTime
            );
            
                } catch (Exception e) {
                    long executionTime = System.currentTimeMillis() - startTime;
                    logger.error("Error executing query: {}", e.getMessage(), e);

                    return new QueryResponse(
                        false,
                        "Error executing query: " + e.getMessage(),
                        null,
                        0,
                        executionTime
                    );
                }
    }
    
    /**
     * Verifies the database connection
     * @return true if the connection is successful
     */
    public boolean testConnection() {
        try {
            jdbcTemplate.queryForObject("SELECT 1 FROM DUAL", Integer.class);
            return true;
        } catch (Exception e) {
            logger.error("Error testing connection: {}", e.getMessage(), e);
            return false;
        }
    }
}
