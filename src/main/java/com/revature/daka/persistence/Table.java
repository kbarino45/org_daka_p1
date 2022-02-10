package com.revature.daka.persistence;

import java.lang.annotation.*;

/**
 * Provide additional information about an Entity, such as an explicit table name or schema.
 *
 * @see Entity
 */
@Documented
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface Table {
    String name() default "";
    String schema() default "";
}
