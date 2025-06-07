package com.driagon.services.logging.annotations;

import com.driagon.services.logging.constants.Level;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Loggable {

    String message() default "";

    Level level() default Level.INFO;

    boolean includeArgs() default true;

    boolean includeResult() default true;

    ExceptionLog[] exceptions() default {};

    boolean logUnexpectedExceptions() default true;

    Level unexpectedExceptionLevel() default Level.ERROR;
}