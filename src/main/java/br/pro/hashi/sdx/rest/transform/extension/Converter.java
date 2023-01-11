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
public abstract class Converter<S, T> {
	private final Type sourceType;
	private final Type targetType;

	/**
	 * Constructs a new converter.
	 */
	protected Converter() {
		this.sourceType = Reflection.getSpecificType(Converter.class, this, 0);
		this.targetType = Reflection.getSpecificType(Converter.class, this, 1);
	}

	/**
	 * Obtains the source type.
	 * 
	 * @return an object representing {@code S}
	 */
	public final Type getSourceType() {
		return sourceType;
	}

	/**
	 * Obtains the target type.
	 * 
	 * @return an object representing {@code T}
	 */
	public final Type getTargetType() {
		return targetType;
	}

	/**
	 * Converts an object of the source type to an object of the target type.
	 * 
	 * @param source an object of type {@code S}
	 * @return an object of type {@code T}
	 */
	public abstract T to(S source);

	/**
	 * Converts an object of the source type from an object of the target type.
	 * 
	 * @param target an object of type {@code T}
	 * @return an object of type {@code S}
	 */
	public abstract S from(T target);
}
