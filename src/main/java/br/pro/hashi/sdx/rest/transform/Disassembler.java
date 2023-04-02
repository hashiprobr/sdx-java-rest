package br.pro.hashi.sdx.rest.transform;

import java.io.InputStream;
import java.io.UncheckedIOException;
import java.lang.reflect.Type;

import br.pro.hashi.sdx.rest.transform.exception.DisassemblingException;

/**
 * A disassembler can transform byte representations back into objects.
 */
public interface Disassembler {
	/**
	 * Transforms an {@link InputStream} representation back into an object if
	 * possible.
	 * 
	 * @implNote The implementation can assume that the type is correct and must
	 *           close the stream unless it also returns a stream.
	 * 
	 * @param <T>    the type of the object
	 * @param stream the representation
	 * @param type   an object representing {@code T}
	 * @return the object
	 * @throws UncheckedIOException   if the representation cannot be read
	 * @throws DisassemblingException if the representation cannot be transformed
	 */
	<T> T read(InputStream stream, Type type);
}
