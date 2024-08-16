package aroundtheeurope.apigateway.configuration;

import aroundtheeurope.apigateway.dto.ForwardedTripRequestDTO;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.support.converter.ClassMapper;
import org.springframework.amqp.support.converter.DefaultClassMapper;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

/**
 * Configuration class for RabbitMQ setup.
 * This configuration defines the queue used for trip request forwarding.
 */
@Configuration
public class RabbitMQConfig {

    /**
     * Declares a persistent queue named "tripRequestQueue".
     * This queue is used to store and forward trip requests.
     *
     * @return the queue bean
     */
    @Bean
    public Queue tripRequestQueue() {
        return new Queue("tripRequestQueue", true);
    }
}
