package aroundtheeurope.apigateway.service;

import aroundtheeurope.apigateway.dto.ForwardedTripRequestDTO;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import jakarta.servlet.http.HttpServletRequest;

@Service
public class TripRequestService {

    @Value("${trip-service.url}")
    private String tripServiceUrl;

    private final RabbitTemplate rabbitTemplate;
    private final RedisTemplate<String, String> redisTemplate;
    private final ObjectMapper objectMapper;

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

    public ResponseEntity<String> queueTripRequest(ForwardedTripRequestDTO request, String userId) {

        // Check if user already has a request in the queue
        if (redisTemplate.opsForZSet().score("tripRequestQueueSet", userId) != null) {
            return ResponseEntity.status(409).body("User already has a request in the queue.");
        }

        double score = System.currentTimeMillis();
        redisTemplate.opsForZSet().add("tripRequestQueueSet", userId, score);

        try {
            String serializedRequest = objectMapper.writeValueAsString(request);
            rabbitTemplate.convertAndSend("tripRequestQueue", serializedRequest);
        } catch (JsonProcessingException e) {
            return ResponseEntity.status(500).body("Failed to serialize request.");
        }

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
