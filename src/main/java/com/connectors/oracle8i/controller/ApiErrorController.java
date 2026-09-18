package com.connectors.oracle8i.controller;

import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.RequestDispatcher;
import javax.servlet.http.HttpServletRequest;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Minimal JSON errors instead of Spring Boot Whitelabel HTML.
 */
@RestController
public class ApiErrorController implements ErrorController {

    @RequestMapping(value = "/error", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Map<String, Object>> error(HttpServletRequest request) {
        Object statusAttr = request.getAttribute(RequestDispatcher.ERROR_STATUS_CODE);
        int statusCode = statusAttr != null ? Integer.parseInt(statusAttr.toString()) : 500;
        HttpStatus status = HttpStatus.resolve(statusCode);
        if (status == null) {
            status = HttpStatus.INTERNAL_SERVER_ERROR;
        }

        Map<String, Object> body = new LinkedHashMap<String, Object>();
        body.put("code", statusCode);
        if (statusCode == 404) {
            body.put("message", "Not found");
        } else {
            Object messageAttr = request.getAttribute(RequestDispatcher.ERROR_MESSAGE);
            if (messageAttr != null && !messageAttr.toString().isEmpty()) {
                body.put("message", messageAttr.toString());
            } else {
                body.put("message", status.getReasonPhrase());
            }
        }

        return ResponseEntity.status(status).body(body);
    }
}
