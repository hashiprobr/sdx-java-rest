package br.pro.hashi.sdx.rest.transform;

import java.io.InputStream;
import java.io.UncheckedIOException;

/**
 * An assembler can transform arbitrary objects into byte representations.
 */
public interface Assembler {
	/**
	 * <p>
	 * Transforms an arbitrary object into an {@link InputStream} representation.
	 * </p>
	 * <p>
	 * The default implementation simply calls {@code toStream(T, Class<T>)},
	 * passing {@code body.getClass()} as the second parameter. Since
	 * {@code body.getClass()} loses generic information due to erasure, this
	 * implementation might not be recommended if {@code T} is a generic type. It
	 * might be better to call {@code toStream(T, Class<T>)} or provide an
	 * alternative implementation that ensures generic information is not lost.
	 * </p>
	 * 
	 * @param <T>  the type of the object
	 * @param body the object
	 * @return the representation
	 * @throws IllegalArgumentException if the type of the object is not supported
	 * @throws UncheckedIOException     if the representation cannot be written
	 */
	@SuppressWarnings("unchecked")
	default <T> InputStream toStream(T body) {
		return toStream(body, (Class<T>) body.getClass());
	}

	/**
	 * Transforms a typed object into an {@link InputStream} representation.
	 * 
	 * @param <T>  the type of the object
	 * @param body the object
	 * @param type an object representing {@code T}
	 * @return the representation
	 * @throws IllegalArgumentException if the type of the object is not supported
	 * @throws UncheckedIOException     if the representation cannot be written
	 */
	<T> InputStream toStream(T body, Class<T> type);
}
