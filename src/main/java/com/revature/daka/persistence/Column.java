package com.revature.daka.persistence;

import java.lang.annotation.*;

/**
 * This annotation marks any field that is a column in a relational database table.
 *
 * @see Entity
 * @see Table
 */
@Documented
@Target({ElementType.METHOD, ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface Column {
    String name() default "";
    boolean unique() default false;
    boolean nullable() default true;
    String dataType() default "";
    String table() default "";
}
