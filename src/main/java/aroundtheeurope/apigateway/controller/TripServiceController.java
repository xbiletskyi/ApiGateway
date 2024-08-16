package aroundtheeurope.apigateway.controller;

import aroundtheeurope.apigateway.dto.ForwardedTripRequestDTO;
import aroundtheeurope.apigateway.dto.TripRequestDTO;
import aroundtheeurope.apigateway.service.RequestForwardingService;
import aroundtheeurope.apigateway.service.TripRequestService;
import aroundtheeurope.apigateway.service.TripRequestValidator;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

/**
 * Controller for handling trip-related requests.
 * This includes queuing, deleting, and retrieving trip requests.
 */
@RestController
@RequestMapping("/gateway/api/v1/trips")
public class TripServiceController {

    @Value("${trip-service.url}")
    private String tripServiceUrl;

    private final TripRequestService tripRequestService;
    private final RequestForwardingService requestForwardingService;
    private final TripRequestValidator tripRequestValidator;

    /**
     * Constructor for TripServiceController, autowiring required services.
     *
     * @param tripRequestService handles trip-related operations like queuing and deleting requests
     * @param requestForwardingService forwards requests to the trip service
     * @param tripRequestValidator validates trip requests and sets default values
     */
    @Autowired
    public TripServiceController(
            TripRequestService tripRequestService,
            RequestForwardingService requestForwardingService,
            TripRequestValidator tripRequestValidator
    ) {
        this.tripRequestService = tripRequestService;
        this.requestForwardingService = requestForwardingService;
        this.tripRequestValidator = tripRequestValidator;
    }

    /**
     * Endpoint to queue a new trip request.
     * The request is validated and then sent to the queue for further processing.
     *
     * @param tripRequestDTO the trip request data transfer object
     * @param jwt the JWT token used to extract the user ID
     * @return a ResponseEntity indicating success or failure of the request
     */
    @PostMapping
    public ResponseEntity<String> queueTripRequest(@RequestBody @Valid TripRequestDTO tripRequestDTO, @AuthenticationPrincipal Jwt jwt) {
        String userId = jwt.getSubject();
        tripRequestDTO = tripRequestValidator.validateAndSetDefaults(tripRequestDTO);

        if (tripRequestDTO == null) {
            return ResponseEntity.badRequest().build();
        }

        ForwardedTripRequestDTO forwardedTripRequestDTO = new ForwardedTripRequestDTO(tripRequestDTO, userId);
        return tripRequestService.queueTripRequest(forwardedTripRequestDTO, userId);
    }

    /**
     * Endpoint to delete the trip request for the authenticated user.
     *
     * @param jwt the JWT token used to extract the user ID
     * @return a ResponseEntity indicating success or failure of the deletion
     */
    @DeleteMapping
    public ResponseEntity<String> deleteTripRequests(@AuthenticationPrincipal Jwt jwt) {
        String userId = jwt.getSubject();
        return tripRequestService.removeTripRequest(userId);
    }

    /**
     * Endpoint to get the position of the authenticated user's trip request in the queue.
     *
     * @param jwt the JWT token used to extract the user ID
     * @return a ResponseEntity with the position of the trip request in the queue
     */
    @GetMapping("/position")
    public ResponseEntity<String> getTripPosition(@AuthenticationPrincipal Jwt jwt) {
        String userId = jwt.getSubject();
        return tripRequestService.getTripRequestPosition(userId);
    }

    /**
     * Endpoint to retrieve all user's requests
     *
     * @param jwt the JWT token used to extract the user ID
     * @param request the HttpServletRequest object
     * @return ResponseEntity containing requests made by the user
     */
    @GetMapping("/requests")
    public ResponseEntity<String> getRequests(
            @AuthenticationPrincipal Jwt jwt,
            HttpServletRequest request
    ){
        String userId = jwt.getSubject();
        String targetUrl = tripServiceUrl + "/api/v1/trips/requests" + "?userId=" + userId;

        return requestForwardingService.forwardRequest(request, targetUrl, HttpMethod.GET, null);
    }


    /**
     * Endpoint to retrieve the search results on specific request
     *
     * @param requestId unique identified of previous request
     * @param request the HttpServletRequest object
     * @return ResponseEntity containing results on the provided request
     */
    @GetMapping("/{requestId}")
    public ResponseEntity<String> getTripsByRequestId(
            @PathVariable UUID requestId,
            HttpServletRequest request
            ){
        String targetUrl = tripServiceUrl + "/api/v1/trips/" + requestId;

        return requestForwardingService.forwardRequest(request, targetUrl, HttpMethod.GET, null);
    }

    /**
     * Endpoint to retrieve all trips associated with the authenticated user or a specific request ID.
     *
     * @param jwt the JWT token used to extract the user ID
     * @param request the HttpServletRequest object
     * @return a ResponseEntity with the trips data
     */
    @GetMapping
    public ResponseEntity<String> getTripsByUserId(
            @AuthenticationPrincipal Jwt jwt,
            HttpServletRequest request
    ) {
        String userId = jwt.getSubject();
        String targetUrl = tripServiceUrl + "/api/v1/trips" + "?userId=" + userId;

        return requestForwardingService.forwardRequest(request, targetUrl, HttpMethod.GET, null);
    }

    /**
     * Endpoint to retrieve a preview of trips associated with the authenticated user.
     *
     * @param jwt the JWT token used to extract the user ID
     * @param request the HttpServletRequest object
     * @return a ResponseEntity with the trips preview data
     */
    @GetMapping("/preview")
    public ResponseEntity<?> getTripsPreviewByUserId(
            @AuthenticationPrincipal Jwt jwt,
            HttpServletRequest request
    ) {
        String userId = jwt.getSubject();
        String targetUrl = tripServiceUrl + "/api/v1/trips/preview" + "?userId=" + userId;
        return requestForwardingService.forwardRequest(request, targetUrl, HttpMethod.GET, null);
    }
}
