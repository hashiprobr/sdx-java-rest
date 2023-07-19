package br.pro.hashi.sdx.rest.server.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import br.pro.hashi.sdx.rest.server.RestServerBuilder;

/**
 * Indicates that the parameter represents the request body.
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.PARAMETER)
public @interface Body {
	/**
	 * The maximum size allowed. Default is {@code 0}, which means the value of
	 * {@link RestServerBuilder#withMaxBodySize(int)}.
	 *
	 * @return a long representing the limit
	 */
	long value() default 0;
}
