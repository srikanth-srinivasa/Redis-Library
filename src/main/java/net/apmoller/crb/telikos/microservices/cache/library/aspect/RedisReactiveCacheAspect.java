package net.apmoller.crb.telikos.microservices.cache.library.aspect;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.apmoller.crb.telikos.microservices.cache.library.annotation.CacheAsideRead;
import net.apmoller.crb.telikos.microservices.cache.library.annotation.CacheAsideWrite;
import net.apmoller.crb.telikos.microservices.cache.library.util.AspectUtils;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.redisson.api.RMapCacheReactive;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.core.env.Environment;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.lang.reflect.Method;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

@Slf4j
@Aspect
@Component
@ConditionalOnClass({ReactiveRedisTemplate.class})
@RequiredArgsConstructor
public class RedisReactiveCacheAspect {

    private final ReactiveRedisTemplate reactiveRedisTemplate;
    private final AspectUtils aspectUtils;
    private final ObjectMapper objectMapper;

    @Autowired
    Environment env;

    @Autowired
    @Qualifier("cacheAsideRMapReadCache")
    private RMapCacheReactive<Object, Object> cacheAsideRMapReadCache;


    @Autowired
    @Qualifier("cacheAsideRMapWriteCache")
    private RMapCacheReactive<Object, Object> cacheAsideRMapWriteCache;

    @Around("execution(public * *(..)) && @annotation(net.apmoller.crb.telikos.microservices.cache.library.annotation.CacheAsideRead)")
    public Object CacheAsideRead(ProceedingJoinPoint joinPoint) {

        Method method = aspectUtils.getMethod(joinPoint);

        Class<?> returnType = method.getReturnType();

        CacheAsideRead annotation = method.getAnnotation(CacheAsideRead.class);

        String key = aspectUtils.getKeyVal(joinPoint, annotation.key());

        if (returnType.isAssignableFrom(Mono.class)) {

            // Get the  value for the given Key in cache else return empty
            return cacheAsideRMapReadCache.get(key).switchIfEmpty(Mono.defer(() ->Mono.empty()));


        }

        throw new RuntimeException("RedisReactiveCacheAdd: Annotated method has unsupported return type, expected Mono<?> or Flux<?>");
    }

    @Around("execution(public * *(..)) && @annotation(net.apmoller.crb.telikos.microservices.cache.library.annotation.CacheAsideWrite)")
    public Object CacheAsideWrite(ProceedingJoinPoint joinPoint) {

        Method method = aspectUtils.getMethod(joinPoint);

        Class<?> returnType = method.getReturnType();

        CacheAsideWrite annotation = method.getAnnotation(CacheAsideWrite.class);

        var key = aspectUtils.getKeyVal(joinPoint, annotation.key());

        if (returnType.isAssignableFrom(Mono.class)) {
            return  CacheAsideWriteSave(joinPoint, key);

        }

        throw new RuntimeException("RedisReactiveCacheAdd: Annotated method has unsupported return type, expected Mono<?> or Flux<?>");
    }

    private Mono<?> CacheAsideWriteSave(ProceedingJoinPoint joinPoint, Object key) {
        try {

            Object cacheValueObject = Stream.of(joinPoint.getArgs()).findFirst().get();
            return this.cacheAsideRMapWriteCache.put(key, cacheValueObject,Long.parseLong(env.getProperty("redis.cache-ttl")), TimeUnit.MINUTES).then();

        } catch (Throwable e) {
            return Mono.error(e);
        }
    }

}



