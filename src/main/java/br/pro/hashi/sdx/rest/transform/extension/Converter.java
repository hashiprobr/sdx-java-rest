package br.pro.hashi.sdx.rest.transform.extension;

import java.lang.reflect.Type;

import br.pro.hashi.sdx.rest.reflection.Reflection;

/**
 * <p>
 * A converter can convert objects of a source type to and from objects of a
 * target type.
 * </p>
 * <p>
 * The idea is that the source type is not supported by a third-party library,
 * but the target type is (possibly via another converter).
 * </p>
 * 
 * @param <S> the source type
 * @param <T> the target type
 */
public interface Converter<S, T> {
	/**
	 * <p>
	 * Obtains the source type.
	 * </p>
	 * <p>
	 * Classes are encouraged to provide an alternative implementation.
	 * </p>
	 * 
	 * @return an object representing {@code S}
	 */
	default Type getSourceType() {
		return Reflection.getSpecificType(Converter.class, 0, this);
	}

	/**
	 * <p>
	 * Obtains the target type.
	 * </p>
	 * <p>
	 * Classes are encouraged to provide an alternative implementation.
	 * </p>
	 * 
	 * @return an object representing {@code T}
	 */
	default Type getTargetType() {
		return Reflection.getSpecificType(Converter.class, 1, this);
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
