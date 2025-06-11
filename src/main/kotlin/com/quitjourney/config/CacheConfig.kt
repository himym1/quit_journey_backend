package com.quitjourney.config

import org.springframework.boot.autoconfigure.condition.ConditionalOnBean
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.cache.CacheManager
import org.springframework.cache.annotation.EnableCaching
import org.springframework.cache.concurrent.ConcurrentMapCacheManager
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Primary
import org.springframework.data.redis.cache.RedisCacheConfiguration
import org.springframework.data.redis.cache.RedisCacheManager
import org.springframework.data.redis.connection.RedisConnectionFactory
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer
import org.springframework.data.redis.serializer.RedisSerializationContext
import org.springframework.data.redis.serializer.StringRedisSerializer
import java.time.Duration

/**
 * 缓存配置
 * 
 * 配置Redis缓存管理器和缓存策略
 */
@Configuration
@EnableCaching
class CacheConfig {
    
    /**
     * Redis缓存管理器 - 仅在Redis可用时启用
     */
    @Bean
    @Primary
    @ConditionalOnBean(RedisConnectionFactory::class)
    fun redisCacheManager(redisConnectionFactory: RedisConnectionFactory): CacheManager {
        val config = RedisCacheConfiguration.defaultCacheConfig()
            .entryTtl(Duration.ofMinutes(10)) // 默认缓存10分钟
            .serializeKeysWith(
                RedisSerializationContext.SerializationPair.fromSerializer(StringRedisSerializer())
            )
            .serializeValuesWith(
                RedisSerializationContext.SerializationPair.fromSerializer(GenericJackson2JsonRedisSerializer())
            )
            .disableCachingNullValues()
        
        // 为不同的缓存设置不同的过期时间
        val cacheConfigurations = mapOf(
            "users" to config.entryTtl(Duration.ofHours(1)),
            "achievements" to config.entryTtl(Duration.ofHours(24)),
            "stats" to config.entryTtl(Duration.ofMinutes(30)),
            "checkins" to config.entryTtl(Duration.ofMinutes(15)),
            "smoking-records" to config.entryTtl(Duration.ofMinutes(15))
        )
        
        return RedisCacheManager.builder(redisConnectionFactory)
            .cacheDefaults(config)
            .withInitialCacheConfigurations(cacheConfigurations)
            .build()
    }
    
    /**
     * 简单缓存管理器 - Redis不可用时的备选方案
     */
    @Bean
    @ConditionalOnMissingBean(RedisConnectionFactory::class)
    fun simpleCacheManager(): CacheManager {
        return ConcurrentMapCacheManager(
            "users", "achievements", "stats", "checkins", "smoking-records"
        )
    }
}
