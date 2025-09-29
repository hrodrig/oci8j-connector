package com.connectors.oracle8i.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * Configuration for query security settings
 */
@Component
@ConfigurationProperties(prefix = "security")
public class QuerySecurityConfig {
    
    private List<String> forbiddenKeywords = new ArrayList<>();
    
    /**
     * Get the list of forbidden keywords
     * @return List of forbidden keywords
     */
    public List<String> getForbiddenKeywords() {
        return forbiddenKeywords;
    }
    
    /**
     * Set the list of forbidden keywords
     * @param forbiddenKeywords List of forbidden keywords
     */
    public void setForbiddenKeywords(List<String> forbiddenKeywords) {
        this.forbiddenKeywords = forbiddenKeywords != null ? forbiddenKeywords : new ArrayList<>();
    }
    
    /**
     * Check if a keyword is forbidden
     * @param keyword The keyword to check
     * @return true if the keyword is forbidden
     */
    public boolean isKeywordForbidden(String keyword) {
        if (forbiddenKeywords.isEmpty()) {
            return false;
        }
        
        String upperKeyword = keyword.toUpperCase();
        return forbiddenKeywords.stream()
                .anyMatch(forbidden -> upperKeyword.contains(forbidden.toUpperCase()));
    }
    
    /**
     * Get the first forbidden keyword found in the query
     * @param query The query to check
     * @return The first forbidden keyword found, or null if none found
     */
    public String getFirstForbiddenKeyword(String query) {
        if (forbiddenKeywords.isEmpty()) {
            return null;
        }
        
        String upperQuery = query.toUpperCase();
        return forbiddenKeywords.stream()
                .filter(forbidden -> upperQuery.contains(forbidden.toUpperCase()))
                .findFirst()
                .orElse(null);
    }
}
