package com.connectors.oracle8i.config;

import org.junit.jupiter.api.Test;
import org.springframework.mock.env.MockEnvironment;

import static org.junit.jupiter.api.Assertions.*;

class OpenApiEnvironmentPostProcessorTest {

    @Test
    void defaultOffWithNoProfile() {
        MockEnvironment env = new MockEnvironment();
        assertFalse(OpenApiEnvironmentPostProcessor.resolveEnabled(env));
    }

    @Test
    void enabledOnDevProfile() {
        MockEnvironment env = new MockEnvironment();
        env.setActiveProfiles("dev");
        assertTrue(OpenApiEnvironmentPostProcessor.resolveEnabled(env));
    }

    @Test
    void disabledOnProdEvenWithDevUnset() {
        MockEnvironment env = new MockEnvironment();
        env.setActiveProfiles("prod");
        assertFalse(OpenApiEnvironmentPostProcessor.resolveEnabled(env));
    }

    @Test
    void explicitTrueOverridesProd() {
        MockEnvironment env = new MockEnvironment();
        env.setActiveProfiles("prod");
        env.setProperty("openapi.enabled", "true");
        assertTrue(OpenApiEnvironmentPostProcessor.resolveEnabled(env));
    }

    @Test
    void explicitFalseOverridesDev() {
        MockEnvironment env = new MockEnvironment();
        env.setActiveProfiles("dev");
        env.setProperty("OPENAPI_ENABLED", "false");
        assertFalse(OpenApiEnvironmentPostProcessor.resolveEnabled(env));
    }
}
