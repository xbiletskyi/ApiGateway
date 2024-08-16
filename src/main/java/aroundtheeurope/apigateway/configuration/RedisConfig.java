package aroundtheeurope.apigateway.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.listener.ChannelTopic;

/**
 * Configuration class for Redis setup.
 * This configuration provides the connection factory, template, and topic for Redis integration.
 */
@Configuration
public class RedisConfig {

    /**
     * Creates a LettuceConnectionFactory for connecting to the Redis server.
     *
     * @return the Redis connection factory bean
     */
    @Bean
    public RedisConnectionFactory redisConnectionFactory() {
        return new LettuceConnectionFactory();
    }

    /**
     * Configures a RedisTemplate for interacting with Redis.
     * This template is used to perform Redis operations.
     *
     * @param redisConnectionFactory the connection factory to be used by the template
     * @return the Redis template bean
     */
    @Bean
    public RedisTemplate<String, String> redisTemplate(RedisConnectionFactory redisConnectionFactory) {
        RedisTemplate<String, String> redisTemplate = new RedisTemplate<>();
        redisTemplate.setConnectionFactory(redisConnectionFactory);
        return redisTemplate;
    }

    /**
     * Defines a Redis topic named "tripRequestQueue".
     * This topic is used to publish and subscribe to messages related to trip requests.
     *
     * @return the Redis topic bean
     */
    @Bean
    public ChannelTopic topic() {
        return new ChannelTopic("tripRequestQueue");
    }
}
