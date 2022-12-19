package net.apmoller.crb.telikos.microservices.cache.library.config;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateDeserializer;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateSerializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalTimeSerializer;
import net.apmoller.crb.telikos.microservices.cache.library.util.AspectUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.connection.ReactiveRedisConnectionFactory;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.data.redis.serializer.GenericToStringSerializer;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

@Slf4j
@Configuration
@ConditionalOnClass(ReactiveRedisConnectionFactory.class)
@ComponentScan(basePackages = "net.apmoller.crb.telikos.microservices.cache.library")
public class RedisReactiveCacheConfig {

    @Value("${spring.redis.date_format:dd-MM-yyyy}")
    public String DEFAULT_DATE_FORMAT;
    @Value("${spring.redis.time_format:HH:mm:ss}")
    public String DEFAULT_TIME_FORMAT;

    @Bean
    public ObjectMapper objectMapper() {

        log.info(" In  ####################RedisReactiveCacheConfig #################### objectMapper   #################### "  );


        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.ANY);

        JavaTimeModule javaTimeModule = new JavaTimeModule();

        javaTimeModule.addSerializer(LocalDateTime.class, new LocalDateTimeSerializer( DateTimeFormatter.ofPattern(DEFAULT_DATE_FORMAT + " " + DEFAULT_TIME_FORMAT)));

        javaTimeModule.addSerializer(LocalDate.class,  new LocalDateSerializer(DateTimeFormatter.ofPattern(DEFAULT_DATE_FORMAT)));
        javaTimeModule.addSerializer(LocalTime.class,  new LocalTimeSerializer(DateTimeFormatter.ofPattern(DEFAULT_TIME_FORMAT)));

        javaTimeModule.addDeserializer(LocalDateTime.class, new LocalDateTimeDeserializer( DateTimeFormatter.ofPattern(DEFAULT_DATE_FORMAT + " " + DEFAULT_TIME_FORMAT)));
        javaTimeModule.addDeserializer(LocalDate.class, new LocalDateDeserializer(DateTimeFormatter.ofPattern(DEFAULT_DATE_FORMAT)));
        javaTimeModule.addDeserializer(LocalTime.class,  new LocalTimeDeserializer(DateTimeFormatter.ofPattern(DEFAULT_TIME_FORMAT)));

        objectMapper.registerModule(javaTimeModule);
        return objectMapper;
    }

    @Bean
    @Primary
    public ReactiveRedisTemplate<String, Object> reactiveRedisTemplate(ReactiveRedisConnectionFactory reactiveRedisConnectionFactory, ObjectMapper objectMapper) {

        log.info(" In  #################### RedisReactiveCacheConfig #################### reactiveRedisTemplate   ####################  "  );

        Jackson2JsonRedisSerializer<Object> serializer = new Jackson2JsonRedisSerializer<>(Object.class);
        serializer.setObjectMapper(objectMapper);

        return new ReactiveRedisTemplate(reactiveRedisConnectionFactory,
                        RedisSerializationContext.newSerializationContext(serializer)
                        .key(new GenericToStringSerializer(String.class))
                        .hashKey(new GenericToStringSerializer(String.class))
                        .value(serializer)
                        .hashValue(serializer)
                        .build()
        );
    }

    @Bean
    public AspectUtils aspectUtils() {
        return new AspectUtils();
    }
}
