package com.connectors.oracle8i.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import java.util.Map;

/**
 * Model for SQL query response
 */
public class QueryResponse {
    
    @JsonProperty("success")
    private boolean success;
    
    @JsonProperty("message")
    private String message;
    
    @JsonProperty("data")
    private List<Map<String, Object>> data;
    
    @JsonProperty("rowCount")
    private int rowCount;
    
    @JsonProperty("executionTimeMs")
    private long executionTimeMs;
    
    public QueryResponse() {
    }
    
    public QueryResponse(boolean success, String message) {
        this.success = success;
        this.message = message;
    }
    
    public QueryResponse(boolean success, String message, List<Map<String, Object>> data, int rowCount, long executionTimeMs) {
        this.success = success;
        this.message = message;
        this.data = data;
        this.rowCount = rowCount;
        this.executionTimeMs = executionTimeMs;
    }
    
    // Getters y Setters
    public boolean isSuccess() {
        return success;
    }
    
    public void setSuccess(boolean success) {
        this.success = success;
    }
    
    public String getMessage() {
        return message;
    }
    
    public void setMessage(String message) {
        this.message = message;
    }
    
    public List<Map<String, Object>> getData() {
        return data;
    }
    
    public void setData(List<Map<String, Object>> data) {
        this.data = data;
    }
    
    public int getRowCount() {
        return rowCount;
    }
    
    public void setRowCount(int rowCount) {
        this.rowCount = rowCount;
    }
    
    public long getExecutionTimeMs() {
        return executionTimeMs;
    }
    
    public void setExecutionTimeMs(long executionTimeMs) {
        this.executionTimeMs = executionTimeMs;
    }
    
    @Override
    public String toString() {
        return "QueryResponse{" +
                "success=" + success +
                ", message='" + message + '\'' +
                ", rowCount=" + rowCount +
                ", executionTimeMs=" + executionTimeMs +
                '}';
    }
}
