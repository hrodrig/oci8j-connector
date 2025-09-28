package com.connectors.oracle8i.controller;

import com.connectors.oracle8i.model.QueryRequest;
import com.connectors.oracle8i.model.QueryResponse;
import com.connectors.oracle8i.service.Oracle8iService;
import com.connectors.oracle8i.service.BuildInfoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * Main controller for Oracle 8i Connector
 */
@RestController
@RequestMapping("/api/v1/oci8j-connector")
@CrossOrigin(origins = "*")
public class Oracle8iController {
    
    @Autowired
    private Oracle8iService oracle8iService;

    @Autowired
    private BuildInfoService buildInfoService;
    
    /**
     * Main endpoint for executing SQL queries
     * POST /api/v1/oci8j-connector/query
     */
    @PostMapping("/query")
    public ResponseEntity<QueryResponse> executeQuery(@RequestBody QueryRequest request) {
        System.out.println("📝 Received query: " + request);
        
        try {
            QueryResponse response = oracle8iService.executeQuery(request);
            
            if (response.isSuccess()) {
                System.out.println("✅ Query executed successfully. Rows: " + response.getRowCount());
                return ResponseEntity.ok(response);
            } else {
                System.out.println("❌ Query error: " + response.getMessage());
                return ResponseEntity.badRequest().body(response);
            }
            
        } catch (Exception e) {
            System.err.println("💥 Unexpected error: " + e.getMessage());
            e.printStackTrace();
            
            QueryResponse errorResponse = new QueryResponse(
                false, 
                "Internal server error: " + e.getMessage()
            );
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }
    
    /**
     * Endpoint to verify application status
     * GET /api/v1/oci8j-connector/healthz
     * 
     * For Kubernetes: This endpoint should return 200 OK if the application
     * is running, regardless of database connectivity. Database issues should
     * be handled by readiness/liveness probes separately.
     */
    @GetMapping("/healthz")
    public ResponseEntity<Map<String, Object>> health() {
        boolean dbConnected = oracle8iService.testConnection();

        Map<String, Object> status = new HashMap<>();
        status.put("status", "UP"); // Always UP if application is running
        status.put("database", dbConnected ? "CONNECTED" : "DISCONNECTED");
        status.put("timestamp", System.currentTimeMillis());
        status.put("uptime", System.currentTimeMillis() - getStartTime());
        status.put("version", buildInfoService.getVersion());
        status.put("build", buildInfoService.getFormattedBuildInfo());

        // Always return 200 OK for Kubernetes liveness probe
        // Database connectivity is a separate concern
        return ResponseEntity.ok(status);
    }
    
    /**
     * Endpoint for Kubernetes readiness probe
     * GET /api/v1/oci8j-connector/ready
     * 
     * This endpoint checks if the application is ready to serve traffic,
     * including database connectivity.
     */
    @GetMapping("/ready")
    public ResponseEntity<Map<String, Object>> ready() {
        boolean dbConnected = oracle8iService.testConnection();

        Map<String, Object> status = new HashMap<>();
        status.put("status", dbConnected ? "READY" : "NOT_READY");
        status.put("database", dbConnected ? "CONNECTED" : "DISCONNECTED");
        status.put("timestamp", System.currentTimeMillis());

        HttpStatus httpStatus = dbConnected ? HttpStatus.OK : HttpStatus.SERVICE_UNAVAILABLE;
        return ResponseEntity.status(httpStatus).body(status);
    }
    
    private static final long START_TIME = System.currentTimeMillis();
    
    private long getStartTime() {
        return START_TIME;
    }
    
    /**
     * Endpoint to get application information
     * GET /api/v1/oci8j-connector/info
     */
    @GetMapping("/info")
    public ResponseEntity<Map<String, Object>> info() {
        Map<String, Object> endpoints = new HashMap<>();
        endpoints.put("query", "POST /api/v1/oci8j-connector/query");
        endpoints.put("healthz", "GET /api/v1/oci8j-connector/healthz");
        endpoints.put("ready", "GET /api/v1/oci8j-connector/ready");
        endpoints.put("info", "GET /api/v1/oci8j-connector/info");
        
        Map<String, Object> info = new HashMap<>();
        info.put("name", "Oracle 8i Connector");
        info.put("version", buildInfoService.getVersion());
        info.put("description", "Oracle 8i connector using classes12.jar");
        info.put("build", buildInfoService.getDetailedBuildInfo());
        Map<String, Object> gitInfo = new HashMap<>();
        gitInfo.put("commit", buildInfoService.getGitCommitIdAbbrev());
        gitInfo.put("branch", buildInfoService.getGitBranch());
        gitInfo.put("commitTime", buildInfoService.getGitCommitTime());
        info.put("git", gitInfo);
        info.put("endpoints", endpoints);

        return ResponseEntity.ok(info);
    }
}
