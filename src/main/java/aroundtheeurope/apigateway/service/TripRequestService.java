package aroundtheeurope.apigateway.service;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import jakarta.servlet.http.HttpServletRequest;

@Service
public class TripRequestService {

    private final RabbitTemplate rabbitTemplate;
    private final RedisTemplate<String, String> redisTemplate;

    @Autowired
    public TripRequestService(RabbitTemplate rabbitTemplate, RedisTemplate<String, String> redisTemplate, ChannelTopic topic, NotificationService notificationService) {
        this.rabbitTemplate = rabbitTemplate;
        this.redisTemplate = redisTemplate;
    }

    public ResponseEntity<String> queueTripRequest(HttpServletRequest request, String userId) {
        String requestURI = request.getRequestURI().replace("/gateway/api/v1/trips", "");
        String fullRequest = userId + ":" + requestURI + "?" + request.getQueryString();

        // Check if user already has a request in the queue
        if (redisTemplate.opsForZSet().score("tripRequestQueueSet", userId) != null) {
            return ResponseEntity.status(409).body("User already has a request in the queue.");
        }

        // Add userId to the sorted set with current timestamp as the score
        double score = System.currentTimeMillis();
        redisTemplate.opsForZSet().add("tripRequestQueueSet", userId, score);

        // Send request to RabbitMQ
        rabbitTemplate.convertAndSend("tripRequestQueue", fullRequest);

        return ResponseEntity.accepted().body("Request queued successfully.");
    }

    public ResponseEntity<String> removeTripRequest(String userId) {
        Long removed = redisTemplate.opsForZSet().remove("tripRequestQueueSet", userId);
        if (removed != null && removed > 0) {
            return ResponseEntity.ok("Request removed successfully.");
        }
        return ResponseEntity.status(404).body("No request found for the user.");
    }

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
