package net.apmoller.crb.telikos.microservices.cache.library.config;


import lombok.extern.slf4j.Slf4j;
import net.apmoller.crb.telikos.microservices.cache.library.util.AspectUtils;
import net.apmoller.crb.telikos.microservices.cache.library.util.CacheConstants;
import org.redisson.Redisson;
import org.redisson.api.MapOptions;
import org.redisson.api.RMapCacheReactive;
import org.redisson.api.RedissonClient;
import org.redisson.api.RedissonReactiveClient;
import org.redisson.codec.JsonJacksonCodec;
import org.redisson.config.Config;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.core.env.Environment;


/**
 * This class  is used  for configuring  the Redis parameter and Read/Write caching RMap
 */

@Slf4j
@Configuration
@ComponentScan(basePackages = "net.apmoller.crb.telikos.microservices.cache.library")
public class AppConfig {

    /*
     AspectUtils  utility class
   */
    private AspectUtils aspectUtils;

    /*
    Environment for TTL
   */
    @Autowired
    Environment env;


    /**
     * Method to  Config redis connection
     * @return RedissonReactiveClient
     */

    @Bean
    public RedissonReactiveClient redissonReactiveClient() {

        Config config = new Config();

        String redisProtocol = Boolean.parseBoolean(env.getProperty(CacheConstants.REDIS_SSL)) ? CacheConstants.REDIS_SSL_REDIS : CacheConstants.REDIS_SSL_REDISS;
        config.useSingleServer().setAddress(redisProtocol + env.getProperty(CacheConstants.REDIS_HOST)  +  ":" +   Integer.parseInt(env.getProperty(CacheConstants.REDIS_PORT)));

        RedissonClient redisson = Redisson.create(config);
        RedissonReactiveClient redissonReactive = redisson.reactive();

        return redissonReactive;
    }


    /**
     * Method to  fetch the cache map for Read
     * @return RMapCacheReactive
     */

    @Bean
    @DependsOn("redissonReactiveClient")
    public RMapCacheReactive<Object, Object> cacheAsideRMapReadCache() {
        final RMapCacheReactive<Object, Object> employeeRMapCache = redissonReactiveClient().getMapCache(env.getProperty("redis.cache-name"), JsonJacksonCodec.INSTANCE, MapOptions.<Object, Object>defaults());
        return employeeRMapCache;
    }

    /**
     * Method to  fetch the cache map for Write
     * @return RMapCacheReactive
     */

    @Bean
    @DependsOn("redissonReactiveClient")
    public RMapCacheReactive<Object, Object> cacheAsideRMapWriteCache() {
        final RMapCacheReactive<Object, Object> employeeRMapCache = redissonReactiveClient().getMapCache(env.getProperty("redis.cache-name"), JsonJacksonCodec.INSTANCE, MapOptions.<Object, Object>defaults());
        return employeeRMapCache;
    }

    /**
     * Method for  AspectUtils
     * @return AspectUtils
     */

    @Bean
    public AspectUtils aspectUtils() {
        return new AspectUtils();
    }

}
