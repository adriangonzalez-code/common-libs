package com.driagon.services.logging.annotations;

import com.driagon.services.logging.constants.Level;

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

    Level exceptionLevel() default Level.ERROR;
}