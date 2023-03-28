package br.pro.hashi.sdx.rest.server.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import br.pro.hashi.sdx.rest.server.RestResource;

/**
 * Indicates that a {@link RestResource} is nested in another resource.
 */
@Documented
@Inherited
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface Nested {
	/**
	 * The enclosing resource.
	 * 
	 * @return a type representing the resource
	 */
	Class<? extends RestResource> in();

	/**
	 * <p>
	 * The number of arguments after the enclosing resource base.
	 * </p>
	 * <p>
	 * For example, if the base of the enclosing resource is {@code /users}, the
	 * base of the nested resource is {@code /posts} and this number is 2, then the
	 * nested resource can be found at {@code /users/{arg0}/{arg1}/posts}.
	 * </p>
	 * 
	 * @return an integer representing the number
	 */
	int at() default 1;
}
