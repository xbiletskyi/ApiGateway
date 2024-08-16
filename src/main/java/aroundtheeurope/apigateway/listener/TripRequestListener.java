package aroundtheeurope.apigateway.listener;

import aroundtheeurope.apigateway.dto.ForwardedTripRequestDTO;
import aroundtheeurope.apigateway.service.NotificationService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

/**
 * Listener class for processing trip requests from the RabbitMQ queue.
 * The listener listens to the "tripRequestQueue", processes the requests, and interacts with external services.
 */
@Service
public class TripRequestListener {

    @Value("${trip-service.url}")
    private String baseUrl;
    @Value("${trip-service.trips.path}")
    private String tripsPath;

    private final RestTemplate restTemplate;
    private final RedisTemplate<String, String> redisTemplate;
    private final NotificationService notificationService;
    private final ObjectMapper objectMapper;

    /**
     * Constructor for TripRequestListener, autowiring necessary services and components.
     *
     * @param restTemplate the RestTemplate for making HTTP requests to the trip service
     * @param redisTemplate the RedisTemplate for interacting with Redis (to check the validity of requests)
     * @param notificationService the service used for notifying users about request processing status
     * @param objectMapper the ObjectMapper for serializing and deserializing JSON data
     */
    @Autowired
    public TripRequestListener(
            RestTemplate restTemplate,
            RedisTemplate<String, String> redisTemplate,
            NotificationService notificationService,
            ObjectMapper objectMapper
    ) {
        this.restTemplate = restTemplate;
        this.redisTemplate = redisTemplate;
        this.notificationService = notificationService;
        this.objectMapper = objectMapper;
    }

    /**
     * Method that listens to the "tripRequestQueue" and processes incoming trip requests.
     * The request is deserialized, validated against the Redis store, sent to the trip service,
     * and finally, the request is removed from the Redis sorted set.
     *
     * @param serializedRequest the serialized JSON string representing the trip request
     */
    @RabbitListener(queues = "tripRequestQueue")
    public void processTripRequest(String serializedRequest) {
        ForwardedTripRequestDTO request;
        try{
             request = objectMapper.readValue(serializedRequest, ForwardedTripRequestDTO.class);
        }
        catch (Exception e){
            return;
        }
        // Check if the request is still valid by checking its presence in the sorted set
        if (redisTemplate.opsForZSet().score("tripRequestQueueSet", request.getUserId()) == null) {
            return;
        }

        String targetURL = baseUrl + tripsPath;
        restTemplate.postForEntity(targetURL, request, String.class);

        // Remove the processed request from the sorted set
        redisTemplate.opsForZSet().remove("tripRequestQueueSet", request.getUserId());

//        // Notify the user upon completion
//        notificationService.notifyUser(request.getUserId(), "Request processed successfully for user "
//                + request.getUserId());
    }
}
