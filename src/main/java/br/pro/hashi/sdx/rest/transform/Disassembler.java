package br.pro.hashi.sdx.rest.transform;

import java.io.IOException;
import java.io.InputStream;

import br.pro.hashi.sdx.rest.transform.exception.DisassemblingException;

/**
 * A disassembler can transform byte representations back into objects.
 */
public interface Disassembler {
	/**
	 * Transforms an {@link InputStream} representation back into an object if
	 * possible.
	 * 
	 * @param <T>    the type of the object
	 * @param stream the representation
	 * @param type   an object representing {@code T}
	 * @return the object
	 * @throws IllegalArgumentException if the type of the object is not supported
	 * @throws IOException              if it is not possible to read the
	 *                                  representation
	 * @throws DisassemblingException   if it is not possible to transform the
	 *                                  representation
	 */
	<T> T fromStream(InputStream stream, Class<T> type) throws IOException, DisassemblingException;
}
