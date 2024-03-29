package br.pro.hashi.sdx.rest.transform;

import java.io.InputStream;
import java.io.UncheckedIOException;
import java.lang.reflect.Type;

import br.pro.hashi.sdx.rest.transform.exception.DisassemblingException;

/**
 * Implemented to transform byte representations back into objects.
 */
public interface Disassembler {
	/**
	 * Reads a byte representation from the specified {@link InputStream} and
	 * transforms it back into an object of the specified type if possible.
	 *
	 * @implNote The implementation can assume that the type is correct and must
	 *           close the stream unless it also returns a stream.
	 *
	 * @param <T>    the type
	 * @param stream the stream
	 * @param type   a {@link Type} representing {@code T}
	 * @return the object
	 * @throws UncheckedIOException   if the representation cannot be read
	 * @throws DisassemblingException if the representation cannot be transformed
	 */
	<T> T read(InputStream stream, Type type);
}
