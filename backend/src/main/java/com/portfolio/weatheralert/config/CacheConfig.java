package com.portfolio.weatheralert.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.portfolio.weatheralert.service.CacheTtlProperties;
import com.portfolio.weatheralert.service.dto.CurrentWeatherResponse;
import com.portfolio.weatheralert.service.dto.HourlyWeatherResponse;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;

@Configuration
@EnableConfigurationProperties(CacheTtlProperties.class)
@ConditionalOnProperty(name = "spring.cache.type", havingValue = "redis")
public class CacheConfig {

    @Bean
    public RedisCacheManager cacheManager(RedisConnectionFactory connectionFactory,
                                         CacheTtlProperties properties,
                                         ObjectMapper objectMapper) {
        Jackson2JsonRedisSerializer<CurrentWeatherResponse> currentWeatherSerializer =
                new Jackson2JsonRedisSerializer<>(CurrentWeatherResponse.class);
        currentWeatherSerializer.setObjectMapper(objectMapper);

        Jackson2JsonRedisSerializer<HourlyWeatherResponse> hourlyWeatherSerializer =
                new Jackson2JsonRedisSerializer<>(HourlyWeatherResponse.class);
        hourlyWeatherSerializer.setObjectMapper(objectMapper);

        RedisCacheConfiguration defaultConfig = RedisCacheConfiguration.defaultCacheConfig();
        RedisCacheConfiguration currentWeatherConfig = defaultConfig
                .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(currentWeatherSerializer))
                .entryTtl(properties.currentWeatherTtl());

        RedisCacheConfiguration hourlyWeatherConfig = defaultConfig
                .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(hourlyWeatherSerializer))
                .entryTtl(properties.hourlyWeatherTtl());

        return RedisCacheManager.builder(connectionFactory)
                .cacheDefaults(defaultConfig)
                .withCacheConfiguration("currentWeather", currentWeatherConfig)
                .withCacheConfiguration("hourlyWeather", hourlyWeatherConfig)
                .build();
    }
}
