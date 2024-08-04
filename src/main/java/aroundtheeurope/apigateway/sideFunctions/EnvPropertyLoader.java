package aroundtheeurope.apigateway.sideFunctions;

import io.github.cdimascio.dotenv.Dotenv;
import io.github.cdimascio.dotenv.DotenvEntry;

/**
 * Loads environment variables from a .env file and sets them as system properties
 * so that they can be used directly in Spring Boot configurations
 */
public class EnvPropertyLoader {

    /**
     * Loads properties from the .env file and sets them as system properties.
     */
    public static void loadProperties() {
        Dotenv.configure()
                .ignoreIfMissing()
                .systemProperties()
                .load();
    }
}
