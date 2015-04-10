package net.burngames.jafig.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Sets the options for a specific field
 *
 * @author PaulBGD
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface Options {
    String name() default "";

    Class<?> listType() default Options.class;

}
