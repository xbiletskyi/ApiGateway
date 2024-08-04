package aroundtheeurope.apigateway.configuration;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import jakarta.annotation.PostConstruct;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Configuration
@ConfigurationProperties(prefix = "microservices")
public class EndpointConfig {

    private Map<String, ServiceConfig> services = new HashMap<>();

    public static class ServiceConfig {
        private String baseUrl;
        private List<String> paths;

        // Getters and Setters
        public String getBaseUrl() {
            return baseUrl;
        }

        public void setBaseUrl(String baseUrl) {
            this.baseUrl = baseUrl;
        }

        public List<String> getPaths() {
            return paths;
        }

        public void setPaths(List<String> paths) {
            this.paths = paths;
        }
    }

    public Map<String, ServiceConfig> getServices() {
        return services;
    }

    public void setServices(Map<String, ServiceConfig> services) {
        this.services = services;
    }

    private final Map<String, String> endpointMappings = new HashMap<>();

    @PostConstruct
    public void init() {
        for (Map.Entry<String, ServiceConfig> entry : services.entrySet()) {
            ServiceConfig config = entry.getValue();
            for (String path : config.getPaths()) {
                endpointMappings.put(path, config.getBaseUrl());
            }
        }
    }

    public Map<String, String> getEndpointMappings() {
        return endpointMappings;
    }
}
