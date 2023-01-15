package br.pro.hashi.sdx.rest.transform;

import java.io.Reader;
import java.io.UncheckedIOException;

import br.pro.hashi.sdx.rest.transform.exception.DeserializingException;

/**
 * A deserializer can transform text representations back into objects.
 */
public interface Deserializer {
	/**
	 * <p>
	 * Transforms a {@link Reader} representation back into a typed object if
	 * possible.
	 * </p>
	 * <p>
	 * Do not call this method if {@code T} is a generic type. Call
	 * {@code fromReader(T, Hint<T>)} instead.
	 * </p>
	 * 
	 * @param <T>    the type of the object
	 * @param reader the representation
	 * @param type   an object representing {@code T}
	 * @return the object
	 * @throws UncheckedIOException   if the representation cannot be read
	 * @throws DeserializingException if the representation cannot be transformed
	 */
	<T> T fromReader(Reader reader, Class<T> type);

	/**
	 * <p>
	 * Transforms a {@link Reader} representation back into a hinted object if
	 * possible.
	 * </p>
	 * <p>
	 * Call this method if {@code T} is a generic type.
	 * </p>
	 * 
	 * @param <T>    the type of the object
	 * @param reader the representation
	 * @param hint   an object representing {@code T}
	 * @return the object
	 * @throws UncheckedIOException   if the representation cannot be read
	 * @throws DeserializingException if the representation cannot be transformed
	 */
	<T> T fromReader(Reader reader, Hint<T> hint);
}
