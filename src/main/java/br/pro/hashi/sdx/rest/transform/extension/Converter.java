package br.pro.hashi.sdx.rest.transform.extension;

import java.lang.reflect.Type;

import br.pro.hashi.sdx.rest.reflection.Reflector;

/**
 * <p>
 * Implemented to convert objects of a source type to and from objects of a
 * target type.
 * </p>
 * <p>
 * The idea is that the source type is not supported by a third-party library,
 * but the target type is (possibly via other converters).
 * </p>
 * 
 * @param <S> the source type
 * @param <T> the target type
 */
public interface Converter<S, T> {
	/**
	 * <p>
	 * Obtains the source type of this converter.
	 * </p>
	 * <p>
	 * Classes are encouraged to provide an alternative implementation.
	 * </p>
	 * 
	 * @return a {@link Class} representing {@code S}
	 */
	default Type getSourceType() {
		return Reflector.getInstance().getSpecificType(this, Converter.class, 0);
	}

	/**
	 * <p>
	 * Obtains the target type of this converter.
	 * </p>
	 * <p>
	 * Classes are encouraged to provide an alternative implementation.
	 * </p>
	 * 
	 * @return a {@link Class} representing {@code T}
	 */
	default Type getTargetType() {
		return Reflector.getInstance().getSpecificType(this, Converter.class, 1);
	}

	/**
	 * Converts an object of the source type to an object of the target type.
	 * 
	 * @param source an object of type {@code S}
	 * @return an object of type {@code T}
	 */
	T to(S source);

	/**
	 * Converts an object of the source type from an object of the target type.
	 * 
	 * @param target an object of type {@code T}
	 * @return an object of type {@code S}
	 */
	S from(T target);
}
