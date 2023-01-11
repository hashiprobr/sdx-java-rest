package br.pro.hashi.sdx.rest.transform;

import java.io.IOException;
import java.io.Reader;

/**
 * A serializer can transform arbitrary objects into text representations.
 */
public interface Serializer {
	/**
	 * Transforms an arbitrary object into a {@link Reader} representation.
	 * 
	 * @param body the object
	 * @return the representation
	 * @throws IllegalArgumentException if the type of the object is not supported
	 * @throws IOException              if it is not possible to write to the
	 *                                  representation
	 */
	Reader toReader(Object body) throws IOException;
}
