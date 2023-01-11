package br.pro.hashi.sdx.rest.transform;

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
	 */
	Reader toReader(Object body);
}
