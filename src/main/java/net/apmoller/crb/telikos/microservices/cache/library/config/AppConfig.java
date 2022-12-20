package net.apmoller.crb.telikos.microservices.cache.library.config;


import lombok.extern.slf4j.Slf4j;
import net.apmoller.crb.telikos.microservices.cache.library.util.AspectUtils;
import org.redisson.Redisson;
import org.redisson.api.MapOptions;
import org.redisson.api.RMapCacheReactive;
import org.redisson.api.RedissonClient;
import org.redisson.api.RedissonReactiveClient;
import org.redisson.client.codec.StringCodec;
import org.redisson.codec.JsonJacksonCodec;
import org.redisson.config.Config;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

@Slf4j
@Configuration
@ComponentScan(basePackages = "net.apmoller.crb.telikos.microservices.cache.library")
public class AppConfig {

    private AspectUtils aspectUtils;

    @Autowired
    Environment env;

    @Bean
    public RedissonReactiveClient redissonReactiveClient() {

        Config config = new Config();

        String redisProtocol = Boolean.parseBoolean(env.getProperty("redis.ssl")) ? "redis://" : "rediss://";
        config.useSingleServer().setAddress(redisProtocol + env.getProperty("redis.host")  +  ":" +   Integer.parseInt(env.getProperty("redis.port")));

        RedissonClient redisson = Redisson.create(config);
        RedissonReactiveClient redissonReactive = redisson.reactive();

        return redissonReactive;
    }

    @Bean
    @DependsOn("redissonReactiveClient")
    public RMapCacheReactive<Object, Object> cacheAsideRMapReadCache() {
        final RMapCacheReactive<Object, Object> employeeRMapCache = redissonReactiveClient().getMapCache(env.getProperty("redis.cache-name"), JsonJacksonCodec.INSTANCE, MapOptions.<Object, Object>defaults());
        return employeeRMapCache;
    }

    @Bean
    @DependsOn("redissonReactiveClient")
    public RMapCacheReactive<Object, Object> cacheAsideRMapWriteCache() {
        final RMapCacheReactive<Object, Object> employeeRMapCache = redissonReactiveClient().getMapCache(env.getProperty("redis.cache-name"), JsonJacksonCodec.INSTANCE, MapOptions.<Object, Object>defaults());
        return employeeRMapCache;
    }

    @Bean
    public AspectUtils aspectUtils() {
        return new AspectUtils();
    }

}
