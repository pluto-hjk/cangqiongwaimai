package com.sky.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Configuration
@Slf4j
public class RedisConfiguration {
    public RedisTemplate redisTemplate(RedisConnectionFactory redisConnectionFactory){
        RedisTemplate redisTemplate = new RedisTemplate();
        //设置连接工厂
        redisTemplate.setConnectionFactory(redisConnectionFactory);
        //设置key序列化方式,string类型的序列化器可以使数据在redis客户端可视化
        redisTemplate.setKeySerializer(new StringRedisSerializer());

        return redisTemplate;
    }
}
