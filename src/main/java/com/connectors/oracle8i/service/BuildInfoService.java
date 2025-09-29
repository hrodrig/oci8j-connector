package com.connectors.oracle8i.service;

import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Properties;

/**
 * Service to provide build information from build-info.properties
 */
@Service
public class BuildInfoService {

    private final Properties buildProperties;
    private final String version;
    private final String buildTimestamp;
    private final String gitCommitId;
    private final String gitCommitIdAbbrev;
    private final String gitBranch;
    private final String gitCommitTime;

    public BuildInfoService() {
        this.buildProperties = loadBuildProperties();
        this.version = buildProperties.getProperty("app.version", "1.0.0");
        this.buildTimestamp = buildProperties.getProperty("build.timestamp", getCurrentTimestamp());
        this.gitCommitId = buildProperties.getProperty("git.commit.id", "unknown");
        this.gitCommitIdAbbrev = buildProperties.getProperty("git.commit.id.abbrev", "unknown");
        this.gitBranch = buildProperties.getProperty("git.branch", "unknown");
        this.gitCommitTime = buildProperties.getProperty("git.commit.time", getCurrentTimestamp());
    }

    private Properties loadBuildProperties() {
        Properties props = new Properties();
        try {
            ClassPathResource resource = new ClassPathResource("build-info.properties");
            if (resource.exists()) {
                try (InputStream inputStream = resource.getInputStream()) {
                    props.load(inputStream);
                }
            }
        } catch (IOException e) {
            System.err.println("Warning: Could not load build-info.properties: " + e.getMessage());
        }
        return props;
    }

    private String getCurrentTimestamp() {
        return DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss'Z'")
                .format(LocalDateTime.now(ZoneId.of("UTC")));
    }

    public String getVersion() {
        return version;
    }

    public String getBuildTimestamp() {
        return buildTimestamp;
    }

    public String getGitCommitId() {
        return gitCommitId;
    }

    public String getGitCommitIdAbbrev() {
        return gitCommitIdAbbrev;
    }

    public String getGitBranch() {
        return gitBranch;
    }

    public String getGitCommitTime() {
        return gitCommitTime;
    }

    public String getFormattedBuildInfo() {
        return String.format("%s (%s)", version, gitCommitIdAbbrev);
    }

    public String getDetailedBuildInfo() {
        return String.format("v%s | %s | %s | %s", 
                version, 
                gitCommitIdAbbrev, 
                gitBranch, 
                buildTimestamp);
    }
}
