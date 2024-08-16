package aroundtheeurope.apigateway.configuration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.web.SecurityFilterChain;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

/**
 * Security configuration class for the API Gateway.
 * This configuration secures the gateway endpoints and configures JWT-based authentication.
 */
@Configuration
public class SecurityConfig {

    @Value("${jwt.secret}")
    private String signingKey;

    /**
     * Configures the security filter chain for HTTP requests.
     * It disables CSRF protection and sets up authorization rules for different endpoints.
     *
     * @param http the HttpSecurity object used to configure security settings
     * @return the SecurityFilterChain bean
     * @throws Exception if an error occurs during configuration
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(authorizeRequests ->
                        authorizeRequests
                                .requestMatchers("/gateway/api/v1/login").permitAll()
                                .requestMatchers("/gateway/api/v1/register").permitAll()
                                .requestMatchers(request -> {
                                    String remoteAddr = request.getRemoteAddr();
                                    return "127.0.0.1".equals(remoteAddr) || "::1".equals(remoteAddr);
                                }).permitAll()
                                .anyRequest().authenticated()
                )
                .oauth2ResourceServer(oauth2 -> oauth2.jwt(
                        jwt -> jwt.decoder(jwtDecoder())
                ));
        return http.build();
    }

    /**
     * Configures the JwtDecoder to decode JWT tokens using the HMAC SHA-256 algorithm.
     * The signing key is provided via configuration.
     *
     * @return the JwtDecoder bean
     */
    @Bean
    public JwtDecoder jwtDecoder(){
        SecretKey key = new SecretKeySpec(signingKey.getBytes(), "HmacSHA256");
        return NimbusJwtDecoder.withSecretKey(key).build();
    }
}
