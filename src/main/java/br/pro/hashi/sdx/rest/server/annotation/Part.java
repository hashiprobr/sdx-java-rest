package br.pro.hashi.sdx.rest.server.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Indicates that the request should be multipart and the parameter represents a
 * part.
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.PARAMETER)
public @interface Part {
	/**
	 * The name of the part.
	 * 
	 * @return a string representing the name
	 */
	String value() default "";
}
