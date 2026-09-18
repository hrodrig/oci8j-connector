package com.connectors.oracle8i.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Default route: minimal 404 JSON (discovery lives at /api/v1/oci8j-connector/info).
 */
@RestController
@CrossOrigin(origins = "*")
public class RootController {

    @GetMapping("/")
    public ResponseEntity<Map<String, Object>> root() {
        Map<String, Object> body = new LinkedHashMap<String, Object>();
        body.put("code", 404);
        body.put("message", "Not found");
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(body);
    }
}
