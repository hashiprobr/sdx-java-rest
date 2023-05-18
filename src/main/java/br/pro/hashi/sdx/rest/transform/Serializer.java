package br.pro.hashi.sdx.rest.transform;

import java.io.UncheckedIOException;
import java.io.Writer;
import java.lang.reflect.Type;

import br.pro.hashi.sdx.rest.transform.exception.SerializingException;

/**
 * A serializer can transform objects into text representations.
 */
public interface Serializer {
	/**
	 * Writes the representation of an arbitrary object to a {@link Writer} if
	 * possible.
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
	 * @param writer the output
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
	 * Writes the representation of a typed object to a {@link Writer} if possible.
	 * 
	 * @implNote The implementation can assume that the type is correct and must not
	 *           close the writer.
	 * 
	 * @param body   the object
	 * @param type   the type of the object
	 * @param writer the output
	 * @throws UncheckedIOException if the representation cannot be written
	 * @throws SerializingException if the object cannot be transformed
	 */
	void write(Object body, Type type, Writer writer);
}
