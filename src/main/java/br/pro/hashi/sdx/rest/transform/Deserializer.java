package br.pro.hashi.sdx.rest.transform;

import java.io.Reader;
import java.io.UncheckedIOException;

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
	 * @throws UncheckedIOException   if the representation cannot be read
	 * @throws DeserializingException if the representation cannot be transformed
	 */
	<T> T fromReader(Reader reader, Class<T> type);
}
