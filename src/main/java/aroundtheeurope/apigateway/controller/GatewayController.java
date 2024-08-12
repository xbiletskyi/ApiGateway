package aroundtheeurope.apigateway.controller;

import aroundtheeurope.apigateway.dto.ForwardedTripRequestDTO;
import aroundtheeurope.apigateway.dto.LogoutRequest;
import aroundtheeurope.apigateway.dto.RefreshRequestDTO;
import aroundtheeurope.apigateway.dto.TripRequestDTO;
import aroundtheeurope.apigateway.service.RequestForwardingService;
import aroundtheeurope.apigateway.service.TripRequestService;
import aroundtheeurope.apigateway.service.TripRequestValidator;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.util.UUID;

@RestController
@RequestMapping("/gateway/api/v1/")
public class GatewayController {

    @Value("${trip-service.url}")
    private String tripServiceUrl;
    @Value("${identity-service.url}")
    private String identityServiceUrl;

    private final TripRequestService tripRequestService;
    private final RequestForwardingService requestForwardingService;
    private final TripRequestValidator tripRequestValidator;
    private final RestTemplate restTemplate;

    @Autowired
    public GatewayController(
            TripRequestService tripRequestService,
            RequestForwardingService requestForwardingService,
            TripRequestValidator tripRequestValidator,
            RestTemplate restTemplate
    ) {
        this.tripRequestService = tripRequestService;
        this.requestForwardingService = requestForwardingService;
        this.tripRequestValidator = tripRequestValidator;
        this.restTemplate = restTemplate;
    }

    @PostMapping("/trips")
    public ResponseEntity<String> queueTripRequest(@RequestBody @Valid TripRequestDTO tripRequestDTO, @AuthenticationPrincipal Jwt jwt) {

        String userId = jwt.getSubject();
        tripRequestDTO = tripRequestValidator.validateAndSetDefaults(tripRequestDTO);

        if (tripRequestDTO == null){
            return ResponseEntity.badRequest().build();
        }

        ForwardedTripRequestDTO forwardedTripRequestDTO = new ForwardedTripRequestDTO(tripRequestDTO, userId);

        return tripRequestService.queueTripRequest(forwardedTripRequestDTO, userId);
    }

    @DeleteMapping("/trips")
    public ResponseEntity<String> deleteTripRequests(@AuthenticationPrincipal Jwt jwt) {
        String userId = jwt.getSubject();
        return tripRequestService.removeTripRequest(userId);
    }

    @GetMapping("/trips/position")
    public ResponseEntity<String> getTripPosition(@AuthenticationPrincipal Jwt jwt) {
        String userId = jwt.getSubject();
        return tripRequestService.getTripRequestPosition(userId);
    }

    @GetMapping("/trips")
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

        return requestForwardingService.forwardRequest(request, targetUrl, HttpMethod.GET, null);
    }

    @GetMapping("/trips/preview")
    public ResponseEntity<?> getTripsPreview(
            @AuthenticationPrincipal Jwt jwt,
            HttpServletRequest request
    ) {
        String userId = jwt.getSubject();
        String targetUrl = tripServiceUrl + "/api/v1/trips/preview" + "?userId=" + userId;
        return requestForwardingService.forwardRequest(request, targetUrl, HttpMethod.GET, null);
    }

////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    @PostMapping("/register")
    public ResponseEntity<?> register(HttpServletRequest request){
        String targetUrl = identityServiceUrl + "/api/v1/register";
        return requestForwardingService.forwardRequest(request, targetUrl, HttpMethod.POST, null);
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(HttpServletRequest request) {
        String targetUrl = identityServiceUrl + "/api/v1/login";
        return requestForwardingService.forwardRequest(request, targetUrl, HttpMethod.POST, null);
    }

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

    @PostMapping("/logout")
    public ResponseEntity<?> logout(
            @AuthenticationPrincipal Jwt jwt,
            HttpServletRequest request
    ){
        LogoutRequest logoutRequest = new LogoutRequest(jwt.getTokenValue());
        String targetUrl = identityServiceUrl + "/api/v1/logout";
        return requestForwardingService.forwardRequest(request, targetUrl, HttpMethod.POST, logoutRequest);
    }
}
