package br.pro.hashi.sdx.rest.transform;

import java.io.IOException;
import java.io.InputStream;

/**
 * An assembler can transform arbitrary objects into byte representations.
 */
public interface Assembler {
	/**
	 * Transforms an arbitrary object into an {@link InputStream} representation.
	 * 
	 * @param body the object
	 * @return the representation
	 * @throws IllegalArgumentException if the type of the object is not supported
	 * @throws IOException              if it is not possible to write to the
	 *                                  representation
	 */
	InputStream toStream(Object body) throws IOException;
}
