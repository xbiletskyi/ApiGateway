package aroundtheeurope.apigateway.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class NotificationService {

    private final RestTemplate restTemplate;

    @Autowired
    public NotificationService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public void notifyUser(String userId, String message) {
        String notificationEndpoint = "http://localhost:60003/api/v1/notify";
        NotificationRequest notificationRequest = new NotificationRequest(userId, message);

        restTemplate.postForEntity(notificationEndpoint, notificationRequest, String.class);
    }

    static class NotificationRequest {
        private String userId;
        private String message;

        public NotificationRequest(String userId, String message) {
            this.userId = userId;
            this.message = message;
        }

        public String getUserId() {
            return userId;
        }

        public String getMessage() {
            return message;
        }
    }
}
