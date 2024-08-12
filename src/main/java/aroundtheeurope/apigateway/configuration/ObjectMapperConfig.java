package aroundtheeurope.apigateway.configuration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.module.paramnames.ParameterNamesModule;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ObjectMapperConfig {

    @Bean
    public ObjectMapper objectMapper() {
        ObjectMapper objectMapper = new ObjectMapper();

        // Register modules to handle specific types
        objectMapper.registerModule(new JavaTimeModule());  // Handles Java 8 date/time types
        objectMapper.registerModule(new ParameterNamesModule());  // Supports constructor-based injection

        // Configure serialization features
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);  // Write dates as ISO strings, not timestamps
        objectMapper.enable(SerializationFeature.INDENT_OUTPUT);  // Pretty print JSON

        // Configure deserialization features
        objectMapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);  // Ignore unknown properties during deserialization

        return objectMapper;
    }
}
