package br.pro.hashi.sdx.rest.transform;

import java.io.UncheckedIOException;
import java.io.Writer;
import java.lang.reflect.Type;

import br.pro.hashi.sdx.rest.transform.exception.SerializingException;

/**
 * Implemented to transform objects into text representations.
 */
public interface Serializer {
	/**
	 * Transforms the specified typed object into a text representation and writes
	 * it to the specified {@link Writer} if possible.
	 * 
	 * @implNote The implementation can assume that the type is correct and cannot
	 *           close the writer.
	 * 
	 * @param <T>    the type
	 * @param body   the object
	 * @param type   a {@link Type} representing {@code T}
	 * @param writer the writer
	 * @throws UncheckedIOException if the representation cannot be written
	 * @throws SerializingException if the object cannot be transformed
	 */
	<T> void write(T body, Type type, Writer writer);
}
