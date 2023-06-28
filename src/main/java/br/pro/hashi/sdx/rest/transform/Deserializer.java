package br.pro.hashi.sdx.rest.transform;

import java.io.Reader;
import java.io.UncheckedIOException;
import java.lang.reflect.Type;

import br.pro.hashi.sdx.rest.transform.exception.DeserializingException;

/**
 * Implemented to transform text representations back into objects.
 */
public interface Deserializer {
	/**
	 * Reads a text representation from the specified {@link Reader} and transforms
	 * it back into an object of the specified type if possible.
	 * 
	 * @implNote The implementation can assume that the type is correct and must
	 *           close the reader unless it also returns a reader.
	 * 
	 * @param <T>    the type
	 * @param reader the reader
	 * @param type   a {@link Type} representing {@code T}
	 * @return the object
	 * @throws UncheckedIOException   if the representation cannot be read
	 * @throws DeserializingException if the representation cannot be transformed
	 */
	<T> T read(Reader reader, Type type);
}
