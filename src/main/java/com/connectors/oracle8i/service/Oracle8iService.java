package com.connectors.oracle8i.service;

import com.connectors.oracle8i.model.QueryRequest;
import com.connectors.oracle8i.model.QueryResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * Service for executing SQL queries in Oracle 8i
 */
@Service
public class Oracle8iService {
    
    @Autowired
    private JdbcTemplate jdbcTemplate;
    
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
            System.err.println("Error executing query: " + e.getMessage());
            e.printStackTrace();
            
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
            System.err.println("Error testing connection: " + e.getMessage());
            return false;
        }
    }
}
