package aroundtheeurope.apigateway.listener;

import aroundtheeurope.apigateway.service.NotificationService;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class TripRequestListener {

    private final RestTemplate restTemplate;
    private final RedisTemplate<String, String> redisTemplate;
    private final NotificationService notificationService;

    @Autowired
    public TripRequestListener(RestTemplate restTemplate, RedisTemplate<String, String> redisTemplate, NotificationService notificationService) {
        this.restTemplate = restTemplate;
        this.redisTemplate = redisTemplate;
        this.notificationService = notificationService;
    }

    @RabbitListener(queues = "tripRequestQueue")
    public void processTripRequest(String message) {
        String[] parts = message.split(":", 2);
        String userId = parts[0];
        String requestURI = parts[1];

        // Check if the request is still valid by checking its presence in the sorted set
        if (redisTemplate.opsForZSet().score("tripRequestQueueSet", userId) == null) {
            // Skip processing if the request has been removed
            return;
        }

        String targetURL = "http://trip-service/api/v1/trips" + requestURI;
        restTemplate.getForEntity(targetURL, String.class);

        // Remove the processed request from the sorted set
        redisTemplate.opsForZSet().remove("tripRequestQueueSet", userId);

        // Notify the user upon completion
        notificationService.notifyUser(userId, "Request processed successfully for user " + userId);
    }
}
