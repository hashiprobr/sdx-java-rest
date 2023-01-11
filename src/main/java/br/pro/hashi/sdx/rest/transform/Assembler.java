package br.pro.hashi.sdx.rest.transform;

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
	 */
	InputStream toStream(Object body);
}
