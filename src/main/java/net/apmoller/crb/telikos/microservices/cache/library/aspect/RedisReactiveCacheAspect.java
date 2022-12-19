package net.apmoller.crb.telikos.microservices.cache.library.aspect;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import net.apmoller.crb.telikos.microservices.cache.library.annotation.*;
import net.apmoller.crb.telikos.microservices.cache.library.dto.ProductDto;
import net.apmoller.crb.telikos.microservices.cache.library.entity.Booking;
import net.apmoller.crb.telikos.microservices.cache.library.repository.BookingRepository;
import net.apmoller.crb.telikos.microservices.cache.library.util.AspectUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.redisson.api.RMapCacheReactive;
import org.redisson.api.RedissonReactiveClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.core.env.Environment;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.lang.reflect.Method;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
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
    @Qualifier("writeThroughRMapCache")
    private RMapCacheReactive<String, ProductDto> writeThroughRMapCache;

    @Autowired
    @Qualifier("writeBehindRMapCache")
    private RMapCacheReactive<String, ProductDto> writeBehindRMapCache;

    @Autowired
    @Qualifier("readThroughRMapCacheReader")
    private RMapCacheReactive<String, Booking> readThroughRMapCacheReader;

    @Autowired
    @Qualifier("cacheAsideRMapReadCache")
    private RMapCacheReactive<String, Booking> cacheAsideRMapReadCache;


    @Autowired
    @Qualifier("cacheAsideRMapWriteCache")
    private RMapCacheReactive<String, ProductDto> cacheAsideRMapWriteCache;


    @Autowired
    private BookingRepository bookingRepository;

    @Around("execution(public * *(..)) && @annotation(net.apmoller.crb.telikos.microservices.cache.library.annotation.WriteThroughCache)")
    public Object writeThroughCache(ProceedingJoinPoint joinPoint) {

        Method method = aspectUtils.getMethod(joinPoint);

         Class<?> returnType = method.getReturnType();

        WriteThroughCache annotation = method.getAnnotation(WriteThroughCache.class);

        String key = aspectUtils.getKeyVal(joinPoint, annotation.key());

         if (returnType.isAssignableFrom(Mono.class)) {

          return  writeThroughToCacheSave(joinPoint, key);


        }

        throw new RuntimeException("RedisReactiveCacheAdd: Annotated method has unsupported return type, expected Mono<?> or Flux<?>");
    }

    private Mono<?> writeThroughToCacheSave(ProceedingJoinPoint joinPoint, String key) {
        try {

            ProductDto productDto = (ProductDto) Stream.of(joinPoint.getArgs()).findFirst().get();
             return this.writeThroughRMapCache.put(key, productDto,Long.parseLong(env.getProperty("redis.cache-ttl")), TimeUnit.MINUTES).then();

        } catch (Throwable e) {
            return Mono.error(e);
        }
    }


    @Around("execution(public * *(..)) && @annotation(net.apmoller.crb.telikos.microservices.cache.library.annotation.WriteBehindCache)")
    public Object writeBehindCache(ProceedingJoinPoint joinPoint) {

        Method method = aspectUtils.getMethod(joinPoint);

        Class<?> returnType = method.getReturnType();

        WriteBehindCache annotation = method.getAnnotation(WriteBehindCache.class);

        String key = aspectUtils.getKeyVal(joinPoint, annotation.key());

        if (returnType.isAssignableFrom(Mono.class)) {

            return  writeBehindCacheSave(joinPoint, key);


        }

        throw new RuntimeException("RedisReactiveCacheAdd: Annotated method has unsupported return type, expected Mono<?> or Flux<?>");
    }

    private Mono<?> writeBehindCacheSave(ProceedingJoinPoint joinPoint, String key) {
        try {

            ProductDto productDto = (ProductDto) Stream.of(joinPoint.getArgs()).findFirst().get();
            return this.writeBehindRMapCache.put(key, productDto,Long.parseLong(env.getProperty("redis.cache-ttl")), TimeUnit.MINUTES).then();
        } catch (Throwable e) {
            return Mono.error(e);
        }
    }


    @Around("execution(public * *(..)) && @annotation(net.apmoller.crb.telikos.microservices.cache.library.annotation.ReadThroughCache)")
    public Object readThroughCache(ProceedingJoinPoint joinPoint) {

        Method method = aspectUtils.getMethod(joinPoint);

        Class<?> returnType = method.getReturnType();

        ReadThroughCache annotation = method.getAnnotation(ReadThroughCache.class);

        String key = aspectUtils.getKeyVal(joinPoint, annotation.key());

        TypeReference typeRefForMapper = aspectUtils.getTypeReference(method);

        if (returnType.isAssignableFrom(Mono.class)) {

            return readThroughRMapCacheReader.get(key)
                    .switchIfEmpty(Mono.defer(() -> bookingRepository.findById(key)));


        }

        throw new RuntimeException("RedisReactiveCacheAdd: Annotated method has unsupported return type, expected Mono<?> or Flux<?>");


    }

    private Mono<?> readThroughCacheSave(ProceedingJoinPoint joinPoint, String key) {
        try {

            Booking  booking = bookingRepository.findById(key).toFuture().get();
            // need to convert to Dto
            ProductDto cacheProductDto = ProductDto.builder().id(booking.getId()).price(booking.getPrice()).description(booking.getDescription()).build();
            return this.readThroughRMapCacheReader.put(key, booking,Long.parseLong(env.getProperty("redis.cache-ttl")), TimeUnit.MINUTES).then();

        } catch (Throwable e) {
            return Mono.error(e);
        }
    }



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

        String key = aspectUtils.getKeyVal(joinPoint, annotation.key());

        if (returnType.isAssignableFrom(Mono.class)) {
            return  CacheAsideWriteSave(joinPoint, key);

        }

        throw new RuntimeException("RedisReactiveCacheAdd: Annotated method has unsupported return type, expected Mono<?> or Flux<?>");
    }

    private Mono<?> CacheAsideWriteSave(ProceedingJoinPoint joinPoint, String key) {
        try {

            ProductDto productDto = (ProductDto) Stream.of(joinPoint.getArgs()).findFirst().get();
            return this.cacheAsideRMapWriteCache.put(key, productDto,Long.parseLong(env.getProperty("redis.cache-ttl")), TimeUnit.MINUTES).then();

        } catch (Throwable e) {
            return Mono.error(e);
        }
    }


}



