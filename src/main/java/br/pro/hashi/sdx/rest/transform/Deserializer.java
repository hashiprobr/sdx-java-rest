package br.pro.hashi.sdx.rest.transform;

import java.io.IOException;
import java.io.Reader;

import br.pro.hashi.sdx.rest.transform.exception.DeserializingException;

/**
 * A deserializer can transform text representations back into objects.
 */
public interface Deserializer {
	/**
	 * Transforms a {@link Reader} representation back into an object if possible.
	 * 
	 * @param <T>    the type of the object
	 * @param reader the representation
	 * @param type   an object representing {@code T}
	 * @return the object
	 * @throws IllegalArgumentException if the type of the object is not supported
	 * @throws IOException              if it is not possible to read the
	 *                                  representation
	 * @throws DeserializingException   if it is not possible to transform the
	 *                                  representation
	 */
	<T> T fromReader(Reader reader, Class<T> type) throws IOException, DeserializingException;
}
