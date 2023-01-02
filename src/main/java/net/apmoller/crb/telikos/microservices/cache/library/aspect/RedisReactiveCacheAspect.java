package net.apmoller.crb.telikos.microservices.cache.library.aspect;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.apmoller.crb.telikos.microservices.cache.library.annotation.CacheAsideRead;
import net.apmoller.crb.telikos.microservices.cache.library.annotation.CacheAsideWrite;
import net.apmoller.crb.telikos.microservices.cache.library.exception.CacheException;
import net.apmoller.crb.telikos.microservices.cache.library.util.AspectUtils;
import net.apmoller.crb.telikos.microservices.cache.library.util.CacheConstants;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.redisson.api.RMapCacheReactive;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.lang.reflect.Method;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class RedisReactiveCacheAspect {


    private final AspectUtils aspectUtils;

    @Autowired
    Environment env;

    @Autowired
    @Qualifier("cacheAsideRMapReadCache")
    public  RMapCacheReactive<Object , Object>   cacheAsideRMapReadCache ;


    @Autowired
    @Qualifier("cacheAsideRMapWriteCache")
    private  RMapCacheReactive<Object,Object> cacheAsideRMapWriteCache;

    /**
     * @param joinPoint
     * @return object
     */
    @Around("execution(public * *(..)) && @annotation(net.apmoller.crb.telikos.microservices.cache.library.annotation.CacheAsideRead)")
    public <T> T CacheAsideRead(ProceedingJoinPoint joinPoint) {

        try{
            log.info(" In CacheAsideRead  ");



        Method method = aspectUtils.getMethod(joinPoint);

            log.info(" Calling cache Method  details  {} ",method);


        CacheAsideRead annotation = method.getAnnotation(CacheAsideRead.class);

        String key = aspectUtils.getKeyVal(joinPoint, annotation.key());



            log.info("  cache  key   {} ",key);


            // Get the  value for the given Key in cache else return empty
            return (T)  cacheAsideRMapReadCache.get(key).switchIfEmpty(Mono.defer(() ->Mono.empty())).doOnError(e->{

                log.error("exception occurred while reading data from   cache {}", e.getMessage());
                throw new CacheException(e.getMessage());
            });

        }
        catch(Exception e){
            log.error("exception occurred while fetching data from cache {}", e.getMessage());
            throw new CacheException(e.getMessage());
        }
    }


    /**
     * @param joinPoint
     * @return object
     */
    @Around("execution(public * *(..)) && @annotation(net.apmoller.crb.telikos.microservices.cache.library.annotation.CacheAsideWrite)")
    public <T> T  CacheAsideWrite(ProceedingJoinPoint joinPoint) {



        try{
            log.info(" In CacheAsideWrite  ");

        Method method = aspectUtils.getMethod(joinPoint);

            log.info("   Calling cache Method  details   {} ",method);


        CacheAsideWrite annotation = method.getAnnotation(CacheAsideWrite.class);

        var key = aspectUtils.getKeyVal(joinPoint, annotation.key());

            log.info(" Cache key    {} ",key);

        var cacheValueObject = Stream.of(joinPoint.getArgs()).findFirst().get();

            log.info(" Cache Value   {} ",cacheValueObject);

        return (T)  cacheAsideRMapWriteCache.put(key, cacheValueObject,Long.parseLong(env.getProperty(CacheConstants.REDIS_CACHE_TTL)), TimeUnit.MINUTES).then().doOnError(e->{

            log.error("exception occurred while saving data to  cache {}", e.getMessage());

            throw new CacheException(e.getMessage());
        });

        }
        catch(Exception e){
            log.error("exception occurred while fetching data from cache {}", e.getMessage());
            throw new CacheException(e.getMessage());
        }

    }


}



