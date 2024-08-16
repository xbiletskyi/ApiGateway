package aroundtheeurope.apigateway.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

/**
 * Configuration class for RestTemplate.
 * This configuration customizes RestTemplate with a custom error handler to pass through all HTTP responses.
 */
@Configuration
public class RestTemplateConfig {

    /**
     * Configures a RestTemplate bean with a custom error handler.
     * The error handler allows all HTTP responses to be treated as successful.
     *
     * @return the RestTemplate bean
     */
    @Bean
    public RestTemplate restTemplate() {
        RestTemplate restTemplate = new RestTemplate();
        restTemplate.setErrorHandler(new PassThroughResponseErrorHandler());
        return restTemplate;
    }
}

