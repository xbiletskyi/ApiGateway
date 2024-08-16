package aroundtheeurope.apigateway.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Configuration class to set up CORS (Cross-Origin Resource Sharing) for the application.
 * This configuration ensures that the API can be accessed from specified origins with the specified methods, headers, and credentials.
 */
@Configuration
public class CorsConfig {

    /**
     * Bean to configure global CORS settings. This applies to all endpoints in the application.
     *
     * @return a WebMvcConfigurer with custom CORS mappings
     */
    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {

            /**
             * Override the addCorsMappings method to define CORS rules.
             * This method sets up CORS for all paths ("/**") and allows requests from a specific origin,
             * with specified methods, headers, and credentials.
             *
             * @param registry the CorsRegistry to add CORS mappings to
             */
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                registry.addMapping("/**")
                        .allowedOrigins("http://localhost:4200")
                        .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                        .allowedHeaders("*")
                        .allowCredentials(true);
            }
        };
    }
}
