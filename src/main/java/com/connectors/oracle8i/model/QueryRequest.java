package com.connectors.oracle8i.model;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Model for SQL query request
 */
public class QueryRequest {
    
    @JsonProperty("query")
    private String query;
    
    @JsonProperty("parameters")
    private Object[] parameters;
    
    public QueryRequest() {
    }
    
    public QueryRequest(String query) {
        this.query = query;
    }
    
    public QueryRequest(String query, Object[] parameters) {
        this.query = query;
        this.parameters = parameters;
    }
    
    public String getQuery() {
        return query;
    }
    
    public void setQuery(String query) {
        this.query = query;
    }
    
    public Object[] getParameters() {
        return parameters;
    }
    
    public void setParameters(Object[] parameters) {
        this.parameters = parameters;
    }
    
    @Override
    public String toString() {
        return "QueryRequest{" +
                "query='" + query + '\'' +
                ", parameters=" + (parameters != null ? parameters.length + " parameters" : "null") +
                '}';
    }
}
