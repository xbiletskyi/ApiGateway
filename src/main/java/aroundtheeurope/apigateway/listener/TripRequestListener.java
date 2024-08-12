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
