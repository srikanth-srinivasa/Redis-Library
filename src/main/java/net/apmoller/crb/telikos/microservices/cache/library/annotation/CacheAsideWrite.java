package net.apmoller.crb.telikos.microservices.cache.library.annotation;

import java.lang.annotation.*;

/**
 * This class  is used to handle the   Advice Aspect  for CacheAside Write
 */
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface CacheAsideWrite {
    String key() default "";
}
