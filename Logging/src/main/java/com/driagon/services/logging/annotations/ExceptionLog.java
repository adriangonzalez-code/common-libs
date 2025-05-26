package com.driagon.services.logging.annotations;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface ExceptionLog {

    Class<? extends Throwable> value();

    String message() default "";

    boolean printStackTrace() default false;
}