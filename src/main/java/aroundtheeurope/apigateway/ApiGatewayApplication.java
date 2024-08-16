package aroundtheeurope.apigateway;

import aroundtheeurope.apigateway.sideFunctions.EnvPropertyLoader;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * The main class for the API Gateway application.
 * This class serves as the entry point for the Spring Boot application and handles the initialization process.
 */
@SpringBootApplication
public class ApiGatewayApplication {

    // Static block that loads environment-specific properties before the application starts.
    static {
        EnvPropertyLoader.loadProperties();
    }

    /**
     * The main method that starts the Spring Boot application.
     *
     * @param args command line arguments passed to the application
     */
    public static void main(String[] args) {
        // Runs the Spring Boot application, initializing the Spring context and starting the embedded server.
        SpringApplication.run(ApiGatewayApplication.class, args);
    }
}
