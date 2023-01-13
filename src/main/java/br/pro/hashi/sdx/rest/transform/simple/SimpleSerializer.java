package br.pro.hashi.sdx.rest.transform.simple;

import java.io.Reader;
import java.io.StringReader;
import java.io.UncheckedIOException;

import br.pro.hashi.sdx.rest.transform.Serializer;

/**
 * A simple serializer can transform arbitrary objects into non-streaming text
 * representations.
 */
public interface SimpleSerializer extends Serializer {
	/**
	 * <p>
	 * {@inheritDoc}
	 * </p>
	 * <p>
	 * The default implementation simply calls {@code toString(T, Class<T>)} and
	 * instantiates a {@link StringReader} from the {@code String} representation.
	 * Classes are encouraged to provide a more efficient implementation.
	 * </p>
	 * 
	 * @throws IllegalArgumentException {@inheritDoc}
	 * @throws UncheckedIOException     {@inheritDoc}
	 */
	@Override
	default <T> Reader toReader(T body, Class<T> type) {
		return new StringReader(toString(body, type));
	}

	/**
	 * <p>
	 * Transforms an arbitrary object into a {@code String} representation.
	 * </p>
	 * <p>
	 * The default implementation simply calls {@code toString(T, Class<T>)},
	 * passing {@code body.getClass()} as the second parameter. <strong>Do not use
	 * this implementation if {@code T} is a generic type.</strong> Either call
	 * {@code toString(T, Class<T>)} or provide an alternative implementation.
	 * </p>
	 * 
	 * @param <T>  the type of the object
	 * @param body the object
	 * @return the representation
	 * @throws IllegalArgumentException if the type of the object is not supported
	 */
	@SuppressWarnings("unchecked")
	default <T> String toString(T body) {
		return toString(body, (Class<T>) body.getClass());
	}

	/**
	 * Transforms a typed object into a {@code String} representation.
	 * 
	 * @param <T>  the type of the object
	 * @param body the object
	 * @param type an object representing {@code T}
	 * @return the representation
	 * @throws IllegalArgumentException if the type of the object is not supported
	 */
	<T> String toString(T body, Class<T> type);
}
