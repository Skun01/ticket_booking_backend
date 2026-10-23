package com.example.base.config;

import java.util.Map;

import com.example.base.config.property.CacheProperties;

import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.RedisSerializer;

@Configuration
@EnableCaching
public class RedisConfig {

	private final CacheProperties cacheProperties;

	public RedisConfig(CacheProperties cacheProperties) {
		this.cacheProperties = cacheProperties;
	}

	@Bean
	public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory connectionFactory) {
		RedisTemplate<String, Object> template = new RedisTemplate<>();
		template.setConnectionFactory(connectionFactory);

		RedisSerializer<String> stringSerializer = RedisSerializer.string();
		RedisSerializer<Object> jsonSerializer = RedisSerializer.json();

		template.setKeySerializer(stringSerializer);
		template.setValueSerializer(jsonSerializer);
		
		template.setHashKeySerializer(stringSerializer);
		template.setHashValueSerializer(jsonSerializer);
		template.afterPropertiesSet();

		return template;
	}

	@Bean
	public RedisCacheManager cacheManager(RedisConnectionFactory connectionFactory) {
		RedisCacheConfiguration defaultConfig = RedisCacheConfiguration.defaultCacheConfig()
				.entryTtl(cacheProperties.defaultTtl())
				.disableCachingNullValues()
				.serializeKeysWith(RedisSerializationContext.SerializationPair.fromSerializer(RedisSerializer.string()))
				.serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(RedisSerializer.json()));

		Map<String, RedisCacheConfiguration> cacheConfigs = Map.of(
				"event-detail-v2", defaultConfig.entryTtl(cacheProperties.eventDetailTtl())
		);

		return RedisCacheManager.builder(connectionFactory)
				.cacheDefaults(defaultConfig)
				.withInitialCacheConfigurations(cacheConfigs)
				.build();
	}

}
