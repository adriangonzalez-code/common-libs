package com.driagon.services.logging.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "logging.aspect")
public class LoggingProperties {

    private boolean enabled = true;
    private String defaultMaskChar = "*";
    private int defaultVisibleChars = 4;
    private boolean logRequestDuration = true;
    private boolean prettyPrint = true;

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public String getDefaultMaskChar() {
        return defaultMaskChar;
    }

    public void setDefaultMaskChar(String defaultMaskChar) {
        this.defaultMaskChar = defaultMaskChar;
    }

    public int getDefaultVisibleChars() {
        return defaultVisibleChars;
    }

    public void setDefaultVisibleChars(int defaultVisibleChars) {
        this.defaultVisibleChars = defaultVisibleChars;
    }

    public boolean isLogRequestDuration() {
        return logRequestDuration;
    }

    public void setLogRequestDuration(boolean logRequestDuration) {
        this.logRequestDuration = logRequestDuration;
    }

    public boolean isPrettyPrint() {
        return prettyPrint;
    }

    public void setPrettyPrint(boolean prettyPrint) {
        this.prettyPrint = prettyPrint;
    }
}