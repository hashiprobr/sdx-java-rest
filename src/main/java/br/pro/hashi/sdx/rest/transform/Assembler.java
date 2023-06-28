package br.pro.hashi.sdx.rest.transform;

import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.lang.reflect.Type;

import br.pro.hashi.sdx.rest.transform.exception.AssemblingException;

/**
 * Implemented to transform objects into byte representations.
 */
public interface Assembler {
	/**
	 * Transforms the specified object into a byte representation and writes it to
	 * the specified {@link OutputStream} if possible.
	 * 
	 * @implSpec The default implementation simply calls
	 *           {@link #write(Object, Type, OutputStream)}, passing
	 *           {@code body.getClass()} as the second parameter. Since
	 *           {@code body.getClass()} loses generic information due to type
	 *           erasure, this implementation might not be recommended if the object
	 *           is generic. It might be better to call
	 *           {@link #write(Object, Type, OutputStream)} or provide an
	 *           alternative implementation that ensures generic information is not
	 *           lost.
	 * 
	 * @param body   the object
	 * @param stream the stream
	 * @throws NullPointerException if the body is null
	 * @throws UncheckedIOException if the representation cannot be written
	 * @throws AssemblingException  if the object cannot be transformed
	 */
	default void write(Object body, OutputStream stream) {
		if (body == null) {
			throw new NullPointerException("Body cannot be null");
		}
		write(body, body.getClass(), stream);
	}

	/**
	 * Transforms the specified typed object into a byte representation and writes
	 * it to the specified {@link OutputStream} if possible.
	 * 
	 * @implNote The implementation can assume that the type is correct and must not
	 *           close the stream.
	 * 
	 * @param <T>    the type
	 * @param body   the object
	 * @param type   a {@link Type} representing {@code T}
	 * @param stream the stream
	 * @throws UncheckedIOException if the representation cannot be written
	 * @throws AssemblingException  if the object cannot be transformed
	 */
	<T> void write(T body, Type type, OutputStream stream);
}
