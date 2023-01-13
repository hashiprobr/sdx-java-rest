package br.pro.hashi.sdx.rest.transform.simple;

import java.io.Reader;
import java.io.StringReader;
import java.io.UncheckedIOException;

import br.pro.hashi.sdx.rest.transform.Serializer;
import br.pro.hashi.sdx.rest.transform.exception.SerializingException;

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
	 * @throws UncheckedIOException {@inheritDoc}
	 * @throws SerializingException {@inheritDoc}
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
	 * passing {@code body.getClass()} as the second parameter. Since
	 * {@code body.getClass()} loses generic information due to type erasure, this
	 * implementation might not be recommended if {@code T} is a generic type. It
	 * might be better to call {@code toString(T, Class<T>)} or provide an
	 * alternative implementation that ensures generic information is not lost.
	 * </p>
	 * 
	 * @param <T>  the type of the object
	 * @param body the object
	 * @return the representation
	 * @throws SerializingException if the object cannot be transformed
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
	 * @throws SerializingException if the object cannot be transformed
	 */
	<T> String toString(T body, Class<T> type);
}
