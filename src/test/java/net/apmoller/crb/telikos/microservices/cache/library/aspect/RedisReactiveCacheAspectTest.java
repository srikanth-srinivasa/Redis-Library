package net.apmoller.crb.telikos.microservices.cache.library.aspect;

import com.fasterxml.jackson.databind.ObjectMapper;
import net.apmoller.crb.telikos.microservices.cache.library.annotation.CacheAsideRead;
import net.apmoller.crb.telikos.microservices.cache.library.annotation.CacheAsideWrite;
import net.apmoller.crb.telikos.microservices.cache.library.util.AspectUtils;
import org.aspectj.lang.ProceedingJoinPoint;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.redisson.api.RMapCacheReactive;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.core.env.Environment;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import reactor.test.publisher.TestPublisher;

import static org.mockito.Mockito.*;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = { RedisReactiveCacheAspect.class })
class RedisReactiveCacheAspectTest {

    @MockBean
    AspectUtils aspectUtils;

    @MockBean
    Environment environment;

    @Mock
    CacheAsideRead cacheAsideRead;

    @Mock
    CacheAsideWrite cacheAsideWrite;


    @MockBean
    @Qualifier (value="cacheAsideRMapReadCache")
    private RMapCacheReactive<Object, Object> cacheAsideRMapReadCache;

    @MockBean
    @Qualifier (value="cacheAsideRMapWriteCache")
    private RMapCacheReactive<Object, Object> cacheAsideRMapWriteCache;

    @MockBean
    ProceedingJoinPoint proceedingJoinPoint;

    @Autowired
    RedisReactiveCacheAspect redisReactiveCacheAspect;



    @BeforeEach
    void setUp() {
    }

    @Test
    void cacheAsideRead() {


        Method method = mock(Method.class);

        Mono<Object>  value = Mono.just("test");

        when(aspectUtils.getMethod(any())).thenReturn(method);

        when(method.getAnnotation(any())).thenReturn(cacheAsideRead);

        when(aspectUtils.getKeyVal(any(),any())).thenReturn("productDto.id");

        when(cacheAsideRMapReadCache.get(any())).thenReturn(value);

        redisReactiveCacheAspect.CacheAsideRead(proceedingJoinPoint);

    }

    @Test
    void cacheAsideWrite() {

        ProceedingJoinPoint  proceedingJoinPoint = mock(ProceedingJoinPoint.class);


        Method method = mock(Method.class);

        Mono<Object>  value = Mono.just("test");

        when(aspectUtils.getMethod(any())).thenReturn(method);

        when(method.getAnnotation(any())).thenReturn(cacheAsideWrite);

        when(aspectUtils.getKeyVal(any(),any())).thenReturn("productDto.id");

        when(proceedingJoinPoint.getArgs()).thenReturn(new Object[] {"10"});

        when(environment.getProperty("redis.cache-ttl")).thenReturn("1");

        when(cacheAsideRMapWriteCache.put(anyString(),any(),anyLong(),any())).thenReturn(value);

        redisReactiveCacheAspect.CacheAsideWrite(proceedingJoinPoint);



    }

    @Test
    void  cacheAsideReadTest(){

        Method method = mock(Method.class);

        Mono<Object>  value = Mono.just("test");

        when(aspectUtils.getMethod(any())).thenReturn(method);

        when(method.getAnnotation(any())).thenReturn(cacheAsideRead);

        when(aspectUtils.getKeyVal(any(),any())).thenReturn("productDto.id");

        when(cacheAsideRMapReadCache.get(any())).thenReturn(value);

        final TestPublisher<String> testPublisher = TestPublisher.create();

//        StepVerifier
//                .create( redisReactiveCacheAspect.CacheAsideRead(proceedingJoinPoint))
//                .then(() -> testPublisher.emit(String.valueOf(value)))
//                //.expectNext("test")
//               // .expectNextMatches(str -> str.equals("test")) //predicate
//               // .expectNextCount(1) // for Flux
//                .assertNext(Assertions::assertNotNull)
//                .verifyComplete();


        StepVerifier
                .create( redisReactiveCacheAspect.CacheAsideRead(proceedingJoinPoint))
               // .then(() -> testPublisher.emit(String.valueOf(value)))
            .consumeNextWith(r -> {
            //assertEquals(value.block(), Mono.just(r).block()); // get the String value of the mono
                assertEquals(value.block(), r);
        }).verifyComplete();

    }

    @Test
    void  cacheAsideWriteTest(){

        ProceedingJoinPoint  proceedingJoinPoint = mock(ProceedingJoinPoint.class);

        Method method = mock(Method.class);

        Mono<Object>  value = Mono.just("test");

        when(aspectUtils.getMethod(any())).thenReturn(method);

        when(method.getAnnotation(any())).thenReturn(cacheAsideWrite);

        when(aspectUtils.getKeyVal(any(),any())).thenReturn("productDto.id");

        when(proceedingJoinPoint.getArgs()).thenReturn(new Object[] {"10"});

        when(environment.getProperty("redis.cache-ttl")).thenReturn("1");


        when(cacheAsideRMapWriteCache.put(anyString(),any(),anyLong(),any())).thenReturn(value);

        final TestPublisher<String> testPublisher = TestPublisher.create();


//        StepVerifier
//                .create( redisReactiveCacheAspect.CacheAsideWrite(proceedingJoinPoint))
//                .then(() -> testPublisher.emit(String.valueOf(value)))
//                .assertNext(Assertions::assertNotNull)
//                .expectComplete();
//        
        
        // As we do not have any thing to return, we expect no throw
        assertDoesNotThrow(() -> redisReactiveCacheAspect.CacheAsideWrite(proceedingJoinPoint));

    }

}
