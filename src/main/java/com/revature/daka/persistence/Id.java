package com.revature.daka.persistence;

import java.lang.annotation.*;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Specifies the primary key of an entity.
 *
 * @see Column
 */
@Documented
@Target({METHOD, FIELD})
@Retention(RUNTIME)

public @interface Id {
    String type() default "";
}
