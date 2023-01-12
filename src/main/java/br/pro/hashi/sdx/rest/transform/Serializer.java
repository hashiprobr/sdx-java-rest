package br.pro.hashi.sdx.rest.transform;

import java.io.IOException;
import java.io.Reader;

/**
 * A serializer can transform arbitrary objects into text representations.
 */
public interface Serializer {
	/**
	 * <p>
	 * Transforms an arbitrary object into a {@link Reader} representation.
	 * </p>
	 * <p>
	 * The default implementation simply calls {@code toReader(T, Class<T>)},
	 * passing {@code body.getClass()} as the second parameter. Since
	 * {@code body.getClass()} loses generic information due to type erasure, this
	 * implementation might not be recommended if {@code T} is a generic type. It
	 * might be better to call {@code toReader(T, Class<T>)} or provide an
	 * alternative implementation that ensures generic information is not lost.
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
	default <T> Reader toReader(T body) throws IOException {
		return toReader(body, (Class<T>) body.getClass());
	}

	/**
	 * Transforms a typed object into a {@link Reader} representation.
	 * 
	 * @param <T>  the type of the object
	 * @param body the object
	 * @param type an object representing {@code T}
	 * @return the representation
	 * @throws IllegalArgumentException if the type of the object is not supported
	 * @throws IOException              if it is not possible to write to the
	 *                                  representation
	 */
	<T> Reader toReader(T body, Class<T> type) throws IOException;
}
