package net.apmoller.crb.telikos.microservices.cache.library;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class RedisReactiveCacheApplication {

    public static void main(String[] args) {
        SpringApplication.run(RedisReactiveCacheApplication.class, args);
    }

}
