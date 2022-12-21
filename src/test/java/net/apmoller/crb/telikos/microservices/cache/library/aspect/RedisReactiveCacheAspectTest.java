package net.apmoller.crb.telikos.microservices.cache.library.aspect;

import com.fasterxml.jackson.databind.ObjectMapper;
import net.apmoller.crb.telikos.microservices.cache.library.annotation.CacheAsideRead;
import net.apmoller.crb.telikos.microservices.cache.library.annotation.CacheAsideWrite;
import net.apmoller.crb.telikos.microservices.cache.library.util.AspectUtils;
import org.aspectj.lang.ProceedingJoinPoint;
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
import reactor.core.publisher.Mono;

import static org.mockito.Mockito.*;

import java.lang.reflect.Method;
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
}