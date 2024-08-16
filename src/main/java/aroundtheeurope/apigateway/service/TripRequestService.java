package aroundtheeurope.apigateway.service;

import aroundtheeurope.apigateway.dto.ForwardedTripRequestDTO;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;


/**
 * Service responsible for managing trip requests, including queuing, removal, and position retrieval.
 * Uses RabbitMQ for queuing requests and Redis for tracking the state of requests.
 */
@Service
public class TripRequestService {

    @Value("${trip-service.url}")
    private String tripServiceUrl;

    private final RabbitTemplate rabbitTemplate;
    private final RedisTemplate<String, String> redisTemplate;
    private final ObjectMapper objectMapper;

    /**
     * Constructor for TripRequestService, autowiring necessary components.
     *
     * @param rabbitTemplate the RabbitTemplate for sending messages to RabbitMQ
     * @param redisTemplate the RedisTemplate for interacting with Redis (used to store request states)
     * @param objectMapper the ObjectMapper for serializing trip request data
     */
    @Autowired
    public TripRequestService(
            RabbitTemplate rabbitTemplate,
            RedisTemplate<String, String> redisTemplate,
            ObjectMapper objectMapper
    ) {
        this.rabbitTemplate = rabbitTemplate;
        this.redisTemplate = redisTemplate;
        this.objectMapper = objectMapper;
    }

    /**
     * Queues a new trip request if the user doesn't already have one in the queue.
     * The request is added to RabbitMQ and tracked in Redis.
     *
     * @param request the trip request to queue
     * @param userId the ID of the user making the request
     * @return ResponseEntity indicating the result of the queuing operation
     */
    public ResponseEntity<String> queueTripRequest(ForwardedTripRequestDTO request, String userId) {

        // Check if user already has a request in the queue
        if (redisTemplate.opsForZSet().score("tripRequestQueueSet", userId) != null) {
            return ResponseEntity.status(409).body("User already has a request in the queue.");
        }

        // Add the request to Redis with the current timestamp as the score
        double score = System.currentTimeMillis();
        redisTemplate.opsForZSet().add("tripRequestQueueSet", userId, score);

        try {
            // Serialize the request and send it to RabbitMQ
            String serializedRequest = objectMapper.writeValueAsString(request);
            rabbitTemplate.convertAndSend("tripRequestQueue", serializedRequest);
        } catch (JsonProcessingException e) {
            return ResponseEntity.status(500).body("Failed to serialize request.");
        }

        return ResponseEntity.accepted().body("Request queued successfully.");
    }

    /**
     * Removes a trip request for the specified user from the queue.
     *
     * @param userId the ID of the user whose request should be removed
     * @return ResponseEntity indicating the result of the removal operation
     */
    public ResponseEntity<String> removeTripRequest(String userId) {
        Long removed = redisTemplate.opsForZSet().remove("tripRequestQueueSet", userId);
        if (removed != null && removed > 0) {
            return ResponseEntity.ok("Request removed successfully.");
        }
        return ResponseEntity.status(404).body("No request found for the user.");
    }

    /**
     * Retrieves the position of the user's trip request in the queue.
     *
     * @param userId the ID of the user whose request position is to be retrieved
     * @return ResponseEntity with the position of the request or an error message
     */
    public ResponseEntity<String> getTripRequestPosition(String userId) {
        Double score = redisTemplate.opsForZSet().score("tripRequestQueueSet", userId);
        if (score == null) {
            return ResponseEntity.status(404).body("No request found for the user.");
        }

        Long rank = redisTemplate.opsForZSet().rank("tripRequestQueueSet", userId);
        if (rank == null) {
            return ResponseEntity.status(404).body("No request found for the user.");
        }

        return ResponseEntity.ok(String.valueOf(rank + 1));
    }
}
