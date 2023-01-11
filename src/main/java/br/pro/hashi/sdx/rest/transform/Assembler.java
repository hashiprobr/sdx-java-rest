package br.pro.hashi.sdx.rest.transform;

import java.io.IOException;
import java.io.InputStream;

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
	 * passing {@code body.getClass()} as the second parameter. <strong>Do not use
	 * this implementation if {@code T} is a generic type.</strong> Either call
	 * {@code toStream(T, Class<T>)} or provide an alternative implementation.
	 * </p>
	 * 
	 * @param <T>  the type of the object
	 * @param body the object
	 * @return the representation
	 * @throws IllegalArgumentException if the type of the object is not supported
	 * @throws IOException              if it is not possible to write to the
	 *                                  representation
	 */
	@SuppressWarnings("unchecked")
	default <T> InputStream toStream(T body) throws IOException {
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
	 * @throws IOException              if it is not possible to write to the
	 *                                  representation
	 */
	<T> InputStream toStream(T body, Class<T> type) throws IOException;
}
