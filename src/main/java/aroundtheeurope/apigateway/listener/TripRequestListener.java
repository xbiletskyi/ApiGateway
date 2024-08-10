package aroundtheeurope.apigateway.listener;

import aroundtheeurope.apigateway.dto.ForwardedTripRequestDTO;
import aroundtheeurope.apigateway.service.NotificationService;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class TripRequestListener {

    @Value("${trip-service.url}")
    private String baseUrl;
    @Value("${trip-service.trips.path}")
    private String tripsPath;

    private final RestTemplate restTemplate;
    private final RedisTemplate<String, String> redisTemplate;
    private final NotificationService notificationService;

    @Autowired
    public TripRequestListener(
            RestTemplate restTemplate,
            RedisTemplate<String, String> redisTemplate,
            NotificationService notificationService
    ) {
        this.restTemplate = restTemplate;
        this.redisTemplate = redisTemplate;
        this.notificationService = notificationService;
    }

    @RabbitListener(queues = "tripRequestQueue")
    public void processTripRequest(ForwardedTripRequestDTO forwardedRequest) {
        // Check if the request is still valid by checking its presence in the sorted set
        if (redisTemplate.opsForZSet().score("tripRequestQueueSet", forwardedRequest.getUserId()) == null) {
            // Skip processing if the request has been removed
            return;
        }

        String targetURL = baseUrl + tripsPath;
        restTemplate.getForEntity(targetURL, String.class);

        // Remove the processed request from the sorted set
        redisTemplate.opsForZSet().remove("tripRequestQueueSet", forwardedRequest.getUserId());

        // Notify the user upon completion
        notificationService.notifyUser(forwardedRequest.getUserId(), "Request processed successfully for user "
                + forwardedRequest.getUserId());
    }
}
