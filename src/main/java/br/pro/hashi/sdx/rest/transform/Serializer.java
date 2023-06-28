package br.pro.hashi.sdx.rest.transform;

import java.io.UncheckedIOException;
import java.io.Writer;
import java.lang.reflect.Type;

import br.pro.hashi.sdx.rest.transform.exception.SerializingException;

/**
 * Implemented to transform objects into text representations.
 */
public interface Serializer {
	/**
	 * Transforms the specified object into a text representation and writes it to
	 * the specified {@link Writer} if possible.
	 * 
	 * @implSpec The default implementation simply calls
	 *           {@link #write(Object, Type, Writer)}, passing
	 *           {@code body.getClass()} as the second parameter. Since
	 *           {@code body.getClass()} loses generic information due to type
	 *           erasure, this implementation might not be recommended if the object
	 *           is generic. It might be better to call
	 *           {@link #write(Object, Type, Writer)} or provide an alternative
	 *           implementation that ensures generic information is not lost.
	 * 
	 * @param body   the object
	 * @param writer the writer
	 * @throws NullPointerException if the body is null
	 * @throws UncheckedIOException if the representation cannot be written
	 * @throws SerializingException if the object cannot be transformed
	 */
	default void write(Object body, Writer writer) {
		if (body == null) {
			throw new NullPointerException("Body cannot be null");
		}
		write(body, body.getClass(), writer);
	}

	/**
	 * Transforms the specified typed object into a text representation and writes
	 * it to the specified {@link Writer} if possible.
	 * 
	 * @implNote The implementation can assume that the type is correct and must not
	 *           close the writer.
	 * 
	 * @param <T>    the type
	 * @param body   the object
	 * @param type   a {@link Type} representing {@code T}
	 * @param writer the writer
	 * @throws UncheckedIOException if the representation cannot be written
	 * @throws SerializingException if the object cannot be transformed
	 */
	<T> void write(T body, Type type, Writer writer);
}
