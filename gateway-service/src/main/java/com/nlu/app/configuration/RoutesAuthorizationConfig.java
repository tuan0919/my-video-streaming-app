package com.nlu.app.configuration;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
@ConfigurationProperties(prefix = "gateway-service")
public class RoutesAuthorizationConfig {

    private List<RouteAuthorization> routesAuthorization;

    // Getters and Setters
    public List<RouteAuthorization> getRoutesAuthorization() {
        return routesAuthorization;
    }

    public void setRoutesAuthorization(List<RouteAuthorization> routesAuthorization) {
        this.routesAuthorization = routesAuthorization;
    }

    public static class RouteAuthorization {
        private String path;
        private List<String> method;
        private List<String> roles;

        // Getters and Setters
        public String getPath() {
            return path;
        }

        public void setPath(String path) {
            this.path = path;
        }

        public List<String> getMethod() {
            return method;
        }

        public void setMethod(List<String> method) {
            this.method = method;
        }

        public List<String> getRoles() {
            return roles;
        }

        public void setRoles(List<String> roles) {
            this.roles = roles;
        }
    }
}