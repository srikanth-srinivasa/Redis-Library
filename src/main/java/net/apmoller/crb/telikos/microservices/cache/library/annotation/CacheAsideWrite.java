package net.apmoller.crb.telikos.microservices.cache.library.annotation;

import java.lang.annotation.*;

@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface CacheAsideWrite {
    String key() default "";
}
