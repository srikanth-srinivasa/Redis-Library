package net.apmoller.crb.telikos.microservices.cache.library.annotation;

import org.springframework.beans.factory.annotation.Qualifier;

import java.lang.annotation.*;

@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface WriteThroughCache {
    String key() default "";
    boolean useArgsHash() default false;
}
