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
	 * Transforms the specified typed object into a byte representation and writes
	 * it to the specified {@link OutputStream} if possible.
	 *
	 * @implNote The implementation can assume that the type is correct and cannot
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
