package aroundtheeurope.apigateway.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

/**
 * Service responsible for sending notifications to users.
 * Uses a REST endpoint to send notification messages.
 */
@Service
public class NotificationService {

    private final RestTemplate restTemplate;

    /**
     * Constructor for NotificationService, autowiring the RestTemplate.
     *
     * @param restTemplate the RestTemplate used for sending HTTP requests
     */
    @Autowired
    public NotificationService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    /**
     * Sends a notification message to the specified user.
     *
     * @param userId the ID of the user to notify
     * @param message the message content to send
     */
    public void notifyUser(String userId, String message) {
        String notificationEndpoint = "http://localhost:60003/api/v1/notify";
        NotificationRequest notificationRequest = new NotificationRequest(userId, message);

        restTemplate.postForEntity(notificationEndpoint, notificationRequest, String.class);
    }

    /**
     * Inner class representing a notification request payload.
     * Contains user ID and message fields.
     */
    static class NotificationRequest {
        private String userId;
        private String message;

        /**
         * Constructor for NotificationRequest.
         *
         * @param userId the ID of the user to notify
         * @param message the message content to send
         */
        public NotificationRequest(
                String userId,
                String message
        ) {
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
