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

@Configuration
public class RabbitMQConfig {

    @Bean
    public Queue tripRequestQueue() {
        return new Queue("tripRequestQueue", true);
    }
}
