package net.apmoller.crb.telikos.microservices.cache.library.config;


import lombok.extern.slf4j.Slf4j;
import net.apmoller.crb.telikos.microservices.cache.library.dto.ProductDto;
import net.apmoller.crb.telikos.microservices.cache.library.entity.Booking;
import net.apmoller.crb.telikos.microservices.cache.library.repository.BookingRepository;
import net.apmoller.crb.telikos.microservices.cache.library.util.AspectUtils;
import net.apmoller.crb.telikos.microservices.cache.library.util.EntityDtoUtil;
import org.redisson.Redisson;
import org.redisson.api.MapOptions;
import org.redisson.api.RMapCacheReactive;
import org.redisson.api.RedissonClient;
import org.redisson.api.RedissonReactiveClient;
import org.redisson.api.map.MapLoader;
import org.redisson.api.map.MapWriter;
import org.redisson.client.codec.StringCodec;
import org.redisson.config.Config;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.core.env.Environment;
import org.springframework.data.r2dbc.repository.config.EnableR2dbcRepositories;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ExecutionException;

@Slf4j
@Component
@EnableR2dbcRepositories(basePackages="net.apmoller.crb.telikos.microservices.cache.library.repository")
public class AppConfig {


    private AspectUtils aspectUtils;


    @Autowired
    Environment env;

    private BookingRepository bookingRepository;

    public AppConfig(BookingRepository bookingRepository) {
        this.bookingRepository = bookingRepository;
    }



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
    public RMapCacheReactive<String, ProductDto> writeThroughRMapCache() {
        final RMapCacheReactive<String, ProductDto> employeeRMapCache = redissonReactiveClient().getMapCache(env.getProperty("redis.cache-name"), StringCodec.INSTANCE, MapOptions.<String, ProductDto>defaults()
                .writer(getWriteThroughMapWriter())
                .writeMode(MapOptions.WriteMode.WRITE_THROUGH));

        return employeeRMapCache;
    }
    private MapWriter<String, ProductDto> getWriteThroughMapWriter() {
        return new MapWriter<String, ProductDto>() {

            @Override
            public void write(final Map<String, ProductDto> map) {

                map.forEach((k, v) -> {
                    Booking booking = EntityDtoUtil.toEntity(v);
                    bookingRepository.save(booking.setAsNew()).subscribe();
                    log.info(" In  #################### writeThroughRMapCache ############# DB SAVE  "  );
                });
            }

            @Override
            public void delete(Collection<String> keys) {
                    // do nothing here
            }
        };
    }


    @Bean
    @DependsOn("redissonReactiveClient")
    public RMapCacheReactive<String, ProductDto> writeBehindRMapCache() {
        final RMapCacheReactive<String, ProductDto> employeeRMapCache = redissonReactiveClient().getMapCache(env.getProperty("redis.cache-name"), StringCodec.INSTANCE, MapOptions.<String, ProductDto>defaults()
                .writer(getMapWriterForWriteBehind())
                .writeBehindDelay(6000)
                .writeMode(MapOptions.WriteMode.WRITE_BEHIND));

        return employeeRMapCache;
    }
    private MapWriter<String, ProductDto> getMapWriterForWriteBehind() {
        return new MapWriter<String, ProductDto>() {

            @Override
            public void write(final Map<String, ProductDto> map) {

                map.forEach((k, v) -> {

                    Booking booking = EntityDtoUtil.toEntity(v);
                    bookingRepository.save(booking.setAsNew()).subscribe();
                    log.info(" In  #################### writeBehindRMapCache ############# DB SAVE  "  );



                });
            }

            @Override
            public void delete(Collection<String> keys) {
                // do nothing here
            }
        };
    }



    @Bean
    @DependsOn("redissonReactiveClient")
    public RMapCacheReactive<String, Booking> readThroughRMapCacheReader() {
        System.out.println("in readThroughRMapCacheReader");


        final RMapCacheReactive<String, Booking> employeeRMapCache = redissonReactiveClient().getMapCache(env.getProperty("redis.cache-name"), StringCodec.INSTANCE, MapOptions.<String, Booking>defaults()
                .loader(readThroughMapLoader));
        return employeeRMapCache;
    }
    MapLoader<String, Booking> readThroughMapLoader = new MapLoader<String, Booking>() {

        @Override
        public Booking load(String bookingId) {
            try {
                System.out.println("in maploader");
                try {

                    log.info(" In readThroughMapLoader");
                    return bookingRepository.findById(bookingId).toFuture().get();

                } catch (ExecutionException e) {
                    e.printStackTrace();
                }



            } catch (InterruptedException e) {
                e.printStackTrace();


        }

            return null;

    }

        @Override
        public Iterable<String> loadAllKeys() {
            return null;
        }

    };



    @Bean
    @DependsOn("redissonReactiveClient")
    public RMapCacheReactive<String, Booking> cacheAsideRMapReadCache() {
        final RMapCacheReactive<String, Booking> employeeRMapCache = redissonReactiveClient().getMapCache(env.getProperty("redis.cache-name"), StringCodec.INSTANCE, MapOptions.<String, Booking>defaults());
        return employeeRMapCache;
    }

    @Bean
    @DependsOn("redissonReactiveClient")
    public RMapCacheReactive<String, ProductDto> cacheAsideRMapWriteCache() {
        final RMapCacheReactive<String, ProductDto> employeeRMapCache = redissonReactiveClient().getMapCache(env.getProperty("redis.cache-name"), StringCodec.INSTANCE, MapOptions.<String, ProductDto>defaults());
        return employeeRMapCache;
    }



}
