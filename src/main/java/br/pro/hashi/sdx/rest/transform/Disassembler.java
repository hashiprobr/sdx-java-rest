package br.pro.hashi.sdx.rest.transform;

import java.io.InputStream;
import java.io.UncheckedIOException;

import br.pro.hashi.sdx.rest.transform.exception.DisassemblingException;

/**
 * A disassembler can transform byte representations back into objects.
 */
public interface Disassembler {
	/**
	 * <p>
	 * Transforms an {@link InputStream} representation back into a typed object if
	 * possible.
	 * </p>
	 * <p>
	 * Do not call this method if {@code T} is a generic type. Call
	 * {@code fromStream(T, Hint<T>)} instead.
	 * </p>
	 * 
	 * @param <T>    the type of the object
	 * @param stream the representation
	 * @param type   an object representing {@code T}
	 * @return the object
	 * @throws UncheckedIOException   if the representation cannot be read
	 * @throws DisassemblingException if the representation cannot be transformed
	 */
	<T> T fromStream(InputStream stream, Class<T> type);

	/**
	 * <p>
	 * Transforms an {@link InputStream} representation back into a hinted object if
	 * possible.
	 * </p>
	 * <p>
	 * Call this method if {@code T} is a generic type.
	 * </p>
	 * 
	 * @param <T>    the type of the object
	 * @param stream the representation
	 * @param hint   an object representing {@code T}
	 * @return the object
	 * @throws UncheckedIOException   if the representation cannot be read
	 * @throws DisassemblingException if the representation cannot be transformed
	 */
	<T> T fromStream(InputStream stream, Hint<T> hint);
}
