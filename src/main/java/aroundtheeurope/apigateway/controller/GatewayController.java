package aroundtheeurope.apigateway.controller;

import aroundtheeurope.apigateway.configuration.EndpointConfig;
import aroundtheeurope.apigateway.service.TripRequestService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.Collections;
import java.util.Enumeration;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/gateway")
public class GatewayController {

    private final RestTemplate restTemplate;
    private final TripRequestService tripRequestService;
    private final Map<String, String> endpointMappings;

    @Autowired
    public GatewayController(RestTemplate restTemplate, TripRequestService tripRequestService, EndpointConfig endpointConfig) {
        this.restTemplate = restTemplate;
        this.tripRequestService = tripRequestService;
        this.endpointMappings = endpointConfig.getEndpointMappings();
    }

    @PostMapping("/api/v1/trips/**")
    public ResponseEntity<String> forwardTripRequests(HttpServletRequest request, @AuthenticationPrincipal Jwt jwt) {
        String userId = jwt.getSubject();
        return tripRequestService.queueTripRequest(request, userId);
    }

    @DeleteMapping("/api/v1/trips")
    public ResponseEntity<String> deleteTripRequests(@AuthenticationPrincipal Jwt jwt) {
        String userId = jwt.getSubject();
        return tripRequestService.removeTripRequest(userId);
    }

    @GetMapping("/api/v1/trips/position")
    public ResponseEntity<String> getTripPosition(@AuthenticationPrincipal Jwt jwt) {
        String userId = jwt.getSubject();
        return tripRequestService.getTripRequestPosition(userId);
    }

    @RequestMapping("/api/v1/**")
    public ResponseEntity<String> forwardRequests(HttpServletRequest request) {
        String requestUri = request.getRequestURI().replace("/gateway", "");

        if (!isValidEndpoint(requestUri)) {
            return ResponseEntity.badRequest().body("Invalid endpoint");
        }

        String targetUrl = buildTargetUrl(requestUri, request);
        return forwardRequest(targetUrl, request);
    }

    private boolean isValidEndpoint(String requestUri) {
        // Check if the request URI matches any of the valid endpoints
        return endpointMappings.keySet().stream().anyMatch(requestUri::equals);
    }

    private String buildTargetUrl(String requestUri, HttpServletRequest request) {
        String serviceUrl = getServiceUrl(requestUri);
        return UriComponentsBuilder.fromHttpUrl(serviceUrl + requestUri)
                .query(request.getQueryString())
                .build()
                .toUriString();
    }

    private String getServiceUrl(String requestUri) {
        String serviceUrl = endpointMappings.get(requestUri);
        if (serviceUrl == null) {
            throw new IllegalArgumentException("No service mapped for URI: " + requestUri);
        }
        return serviceUrl;
    }

    private ResponseEntity<String> forwardRequest(String targetUrl, HttpServletRequest request) {
        HttpHeaders headers = new HttpHeaders();
        Enumeration<String> headerNames = request.getHeaderNames();

        while (headerNames.hasMoreElements()) {
            String headerName = headerNames.nextElement();
            Enumeration<String> headerValues = request.getHeaders(headerName);
            while (headerValues.hasMoreElements()) {
                headers.add(headerName, headerValues.nextElement());
            }
        }

        HttpEntity<String> entity = new HttpEntity<>(headers);
        return restTemplate.exchange(targetUrl, HttpMethod.valueOf(request.getMethod()), entity, String.class);
    }
}
