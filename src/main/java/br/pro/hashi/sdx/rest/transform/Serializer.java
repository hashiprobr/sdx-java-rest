package br.pro.hashi.sdx.rest.transform;

import java.io.Reader;
import java.io.UncheckedIOException;
import java.io.Writer;

import br.pro.hashi.sdx.rest.transform.exception.SerializingException;

/**
 * A serializer can transform objects into text representations.
 */
public interface Serializer {
	/**
	 * <p>
	 * Writes the representation of an arbitrary object to a {@link Writer}.
	 * </p>
	 * <p>
	 * The default implementation simply calls {@code write(T, Class<T>, Writer)},
	 * passing {@code body.getClass()} as the second parameter. Since
	 * {@code body.getClass()} loses generic information due to type erasure, this
	 * implementation might not be recommended if {@code T} is a generic type. It
	 * might be better to call {@code write(T, Hint<T>, Writer)} or provide an
	 * alternative implementation that ensures generic information is not lost.
	 * </p>
	 * 
	 * @param <T>    the type of the object
	 * @param body   the object
	 * @param writer the output
	 * @throws UncheckedIOException if the representation cannot be written
	 * @throws SerializingException if the object cannot be transformed
	 */
	@SuppressWarnings("unchecked")
	default <T> void write(T body, Writer writer) {
		write(body, (Class<T>) body.getClass(), writer);
	}

	/**
	 * <p>
	 * Writes the representation of a typed object to a {@link Writer}.
	 * </p>
	 * <p>
	 * Do not call this method if {@code T} is a generic type. Call
	 * {@code write(T, Hint<T>, Writer)} instead.
	 * </p>
	 * 
	 * @param <T>    the type of the object
	 * @param body   the object
	 * @param type   an object representing {@code T}
	 * @param writer the output
	 * @throws UncheckedIOException if the representation cannot be written
	 * @throws SerializingException if the object cannot be transformed
	 */
	<T> void write(T body, Class<T> type, Writer writer);

	/**
	 * <p>
	 * Writes the representation of a hinted object to a {@link Writer}.
	 * </p>
	 * <p>
	 * Call this method if {@code T} is a generic type.
	 * </p>
	 * 
	 * @param <T>    the type of the object
	 * @param body   the object
	 * @param hint   an object representing {@code T}
	 * @param writer the output
	 * @throws UncheckedIOException if the representation cannot be written
	 * @throws SerializingException if the object cannot be transformed
	 */
	<T> void write(T body, Hint<T> hint, Writer writer);

	/**
	 * <p>
	 * Transforms an arbitrary object into a {@link Reader} representation.
	 * </p>
	 * <p>
	 * The default implementation simply calls {@code toReader(T, Class<T>)},
	 * passing {@code body.getClass()} as the second parameter. Since
	 * {@code body.getClass()} loses generic information due to type erasure, this
	 * implementation might not be recommended if {@code T} is a generic type. It
	 * might be better to call {@code toReader(T, Hint<T>)} or provide an
	 * alternative implementation that ensures generic information is not lost.
	 * </p>
	 * 
	 * @param <T>  the type of the object
	 * @param body the object
	 * @return the representation
	 * @throws UncheckedIOException if the representation cannot be written
	 * @throws SerializingException if the object cannot be transformed
	 */
	@SuppressWarnings("unchecked")
	default <T> Reader toReader(T body) {
		return toReader(body, (Class<T>) body.getClass());
	}

	/**
	 * <p>
	 * Transforms a typed object into a {@link Reader} representation.
	 * </p>
	 * <p>
	 * Do not call this method if {@code T} is a generic type. Call
	 * {@code toReader(T, Hint<T>)} instead.
	 * </p>
	 * 
	 * @param <T>  the type of the object
	 * @param body the object
	 * @param type an object representing {@code T}
	 * @return the representation
	 * @throws UncheckedIOException if the representation cannot be written
	 * @throws SerializingException if the object cannot be transformed
	 */
	<T> Reader toReader(T body, Class<T> type);

	/**
	 * <p>
	 * Transforms a hinted object into a {@link Reader} representation.
	 * </p>
	 * <p>
	 * Call this method if {@code T} is a generic type.
	 * </p>
	 * 
	 * @param <T>  the type of the object
	 * @param body the object
	 * @param hint an object representing {@code T}
	 * @return the representation
	 * @throws UncheckedIOException if the representation cannot be written
	 * @throws SerializingException if the object cannot be transformed
	 */
	<T> Reader toReader(T body, Hint<T> hint);
}
