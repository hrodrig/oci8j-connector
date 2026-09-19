package com.connectors.oracle8i.config;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;

import java.util.HashMap;
import java.util.Map;

/**
 * Gates springdoc (SPEC §7.1). Default off; on for openapi.enabled=true / OPENAPI_ENABLED
 * or active profiles dev|local; prod stays off unless forced true.
 */
public class OpenApiEnvironmentPostProcessor implements EnvironmentPostProcessor {

    @Override
    public void postProcessEnvironment(ConfigurableEnvironment environment, SpringApplication application) {
        boolean enabled = resolveEnabled(environment);
        Map<String, Object> props = new HashMap<String, Object>();
        props.put("springdoc.api-docs.enabled", Boolean.toString(enabled));
        props.put("springdoc.swagger-ui.enabled", Boolean.toString(enabled));
        props.put("oci8j.openapi.enabled", Boolean.toString(enabled));
        environment.getPropertySources().addFirst(new MapPropertySource("openapiGate", props));
    }

    static boolean resolveEnabled(ConfigurableEnvironment environment) {
        String explicit = firstNonEmpty(
                environment.getProperty("openapi.enabled"),
                environment.getProperty("OPENAPI_ENABLED"));
        String[] profiles = environment.getActiveProfiles();
        boolean prod = containsProfile(profiles, "prod");
        boolean devish = containsProfile(profiles, "dev") || containsProfile(profiles, "local");

        if ("false".equalsIgnoreCase(explicit)) {
            return false;
        }
        if ("true".equalsIgnoreCase(explicit)) {
            return true;
        }
        if (prod) {
            return false;
        }
        return devish;
    }

    private static boolean containsProfile(String[] profiles, String name) {
        for (String p : profiles) {
            if (name.equalsIgnoreCase(p)) {
                return true;
            }
        }
        return false;
    }

    private static String firstNonEmpty(String a, String b) {
        if (a != null && !a.trim().isEmpty()) {
            return a.trim();
        }
        if (b != null && !b.trim().isEmpty()) {
            return b.trim();
        }
        return "";
    }
}
