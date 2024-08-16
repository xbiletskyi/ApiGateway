package aroundtheeurope.apigateway.controller;

import aroundtheeurope.apigateway.dto.LogoutRequest;
import aroundtheeurope.apigateway.dto.RefreshRequestDTO;
import aroundtheeurope.apigateway.service.RequestForwardingService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

/**
 * Controller for handling identity-related requests.
 * This includes user registration, login, token refresh, and logout.
 */
@RestController
@RequestMapping("/gateway/api/v1/")
public class IdentityServiceController {

    @Value("${identity-service.url}")
    private String identityServiceUrl;

    private final RequestForwardingService requestForwardingService;

    /**
     * Constructor for IdentityServiceController, autowiring the RequestForwardingService.
     *
     * @param requestForwardingService forwards requests to the identity service
     */
    @Autowired
    public IdentityServiceController(RequestForwardingService requestForwardingService) {
        this.requestForwardingService = requestForwardingService;
    }

    /**
     * Endpoint to forward registration requests to the identity service.
     *
     * @param request the HttpServletRequest object
     * @return a ResponseEntity with the result of the registration
     */
    @PostMapping("/register")
    public ResponseEntity<?> register(HttpServletRequest request) {
        String targetUrl = identityServiceUrl + "/api/v1/register";
        return requestForwardingService.forwardRequest(request, targetUrl, HttpMethod.POST, null);
    }

    /**
     * Endpoint to forward login requests to the identity service.
     *
     * @param request the HttpServletRequest object
     * @return a ResponseEntity with the result of the login
     */
    @PostMapping("/login")
    public ResponseEntity<?> login(HttpServletRequest request) {
        String targetUrl = identityServiceUrl + "/api/v1/login";
        return requestForwardingService.forwardRequest(request, targetUrl, HttpMethod.POST, null);
    }

    /**
     * Endpoint to refresh the JWT token by forwarding the request to the identity service.
     *
     * @param jwt the JWT token to be refreshed
     * @param expiration the new expiration time for the token
     * @param request the HttpServletRequest object
     * @return a ResponseEntity with the new token or error information
     */
    @GetMapping("/refresh")
    public ResponseEntity<?> refresh(
            @AuthenticationPrincipal Jwt jwt,
            @RequestParam(value = "expiration", required = false, defaultValue = "600") long expiration,
            HttpServletRequest request
    ) {
        RefreshRequestDTO refreshRequestDTO = new RefreshRequestDTO(jwt.getTokenValue(), expiration);
        String targetUrl = identityServiceUrl + "/api/v1/refresh";
        return requestForwardingService.forwardRequest(request, targetUrl, HttpMethod.POST, refreshRequestDTO);
    }

    /**
     * Endpoint to log out the user by invalidating the JWT token in the identity service.
     *
     * @param jwt the JWT token to be invalidated
     * @param request the HttpServletRequest object
     * @return a ResponseEntity indicating success or failure of the logout
     */
    @PostMapping("/logout")
    public ResponseEntity<?> logout(
            @AuthenticationPrincipal Jwt jwt,
            HttpServletRequest request
    ) {
        LogoutRequest logoutRequest = new LogoutRequest(jwt.getTokenValue());
        String targetUrl = identityServiceUrl + "/api/v1/logout";
        return requestForwardingService.forwardRequest(request, targetUrl, HttpMethod.POST, logoutRequest);
    }
}
