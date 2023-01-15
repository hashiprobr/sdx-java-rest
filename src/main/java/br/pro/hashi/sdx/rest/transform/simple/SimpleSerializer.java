package br.pro.hashi.sdx.rest.transform.simple;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.io.UncheckedIOException;
import java.io.Writer;

import br.pro.hashi.sdx.rest.transform.Hint;
import br.pro.hashi.sdx.rest.transform.Serializer;
import br.pro.hashi.sdx.rest.transform.exception.SerializingException;

/**
 * A simple serializer can transform objects into non-streaming text
 * representations.
 */
public interface SimpleSerializer extends Serializer {
	/**
	 * <p>
	 * {@inheritDoc}
	 * </p>
	 * <p>
	 * The default implementation simply calls {@code toString(T, Class<T>)} and
	 * writes the {@code String} representation. Classes are encouraged to provide a
	 * more efficient implementation.
	 * </p>
	 * 
	 * @throws UncheckedIOException {@inheritDoc}
	 * @throws SerializingException {@inheritDoc}
	 */
	@Override
	default <T> void write(T body, Class<T> type, Writer writer) {
		write(toString(body, type), writer);
	}

	/**
	 * <p>
	 * {@inheritDoc}
	 * </p>
	 * <p>
	 * The default implementation simply calls {@code toString(T, Hint<T>)} and
	 * writes the {@code String} representation. Classes are encouraged to provide a
	 * more efficient implementation.
	 * </p>
	 * 
	 * @throws UncheckedIOException {@inheritDoc}
	 * @throws SerializingException {@inheritDoc}
	 */
	@Override
	default <T> void write(T body, Hint<T> hint, Writer writer) {
		write(toString(body, hint), writer);
	}

	private <T> void write(String content, Writer writer) {
		try {
			writer.write(content);
			writer.close();
		} catch (IOException exception) {
			throw new UncheckedIOException(exception);
		}
	}

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
		return toReader(toString(body, type));
	}

	/**
	 * <p>
	 * {@inheritDoc}
	 * </p>
	 * <p>
	 * The default implementation simply calls {@code toString(T, Hint<T>)} and
	 * instantiates a {@link StringReader} from the {@code String} representation.
	 * Classes are encouraged to provide a more efficient implementation.
	 * </p>
	 * 
	 * @throws UncheckedIOException {@inheritDoc}
	 * @throws SerializingException {@inheritDoc}
	 */
	@Override
	default <T> Reader toReader(T body, Hint<T> hint) {
		return toReader(toString(body, hint));
	}

	private <T> Reader toReader(String content) {
		return new StringReader(content);
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
	 * <p>
	 * Transforms a typed object into a {@code String} representation.
	 * </p>
	 * <p>
	 * Do not call this method if {@code T} is a generic type. Call
	 * {@code toString(T, Hint<T>)} instead.
	 * </p>
	 * 
	 * @param <T>  the type of the object
	 * @param body the object
	 * @param type an object representing {@code T}
	 * @return the representation
	 * @throws SerializingException if the object cannot be transformed
	 */
	<T> String toString(T body, Class<T> type);

	/**
	 * <p>
	 * Transforms a hinted object into a {@code String} representation.
	 * </p>
	 * <p>
	 * Call this method if {@code T} is a generic type.
	 * </p>
	 * 
	 * @param <T>  the type of the object
	 * @param body the object
	 * @param hint an object representing {@code T}
	 * @return the representation
	 * @throws SerializingException if the object cannot be transformed
	 */
	<T> String toString(T body, Hint<T> hint);
}
