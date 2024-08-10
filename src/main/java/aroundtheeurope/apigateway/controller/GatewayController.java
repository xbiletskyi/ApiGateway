package aroundtheeurope.apigateway.controller;

import aroundtheeurope.apigateway.dto.ForwardedTripRequestDTO;
import aroundtheeurope.apigateway.dto.TripRequestDTO;
import aroundtheeurope.apigateway.service.RequestForwardingService;
import aroundtheeurope.apigateway.service.TripRequestService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.util.UUID;

@RestController
@RequestMapping("/gateway")
public class GatewayController {

    @Value("${trip-service.url}")
    private String tripServiceUrl;
    @Value("${identity-service.url}")
    private String identityServiceUrl;

    private final RestTemplate restTemplate;
    private final TripRequestService tripRequestService;
    private final RequestForwardingService requestForwardingService;

    @Autowired
    public GatewayController(
            RestTemplate restTemplate,
            TripRequestService tripRequestService,
            RequestForwardingService requestForwardingService
    ) {
        this.restTemplate = restTemplate;
        this.tripRequestService = tripRequestService;
        this.requestForwardingService = requestForwardingService;
    }

    @PostMapping("/api/v1/trips")
    public ResponseEntity<String> queueTripRequest(@RequestBody @Valid TripRequestDTO tripRequestDTO, @AuthenticationPrincipal Jwt jwt) {

        String userId = jwt.getSubject();
        ForwardedTripRequestDTO forwardedTripRequestDTO = new ForwardedTripRequestDTO(tripRequestDTO, userId);

        return tripRequestService.queueTripRequest(forwardedTripRequestDTO, userId);
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

    @GetMapping("/api/v1/trips")
    public ResponseEntity<String> getTrips(
            @RequestParam(required = false) UUID requestId,
            @AuthenticationPrincipal Jwt jwt,
            HttpServletRequest request
            ) {
        String userId = jwt.getSubject();
        String targetUrl = tripServiceUrl + "/api/v1/trips";
        if (requestId != null) {
            targetUrl += "?requestId=" + requestId;
        }
        else{
            targetUrl += "?userId=" + userId;
        }

        return requestForwardingService.forwardRequest(request, targetUrl, HttpMethod.GET);
    }
}
