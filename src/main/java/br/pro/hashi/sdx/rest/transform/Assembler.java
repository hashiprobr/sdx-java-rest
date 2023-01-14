package br.pro.hashi.sdx.rest.transform;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.UncheckedIOException;

import br.pro.hashi.sdx.rest.transform.exception.AssemblingException;

/**
 * An assembler can transform arbitrary objects into byte representations.
 */
public interface Assembler {
	/**
	 * <p>
	 * Writes the representation of an arbitrary object to an {@link OutputStream}.
	 * </p>
	 * <p>
	 * The default implementation simply calls
	 * {@code write(T, Class<T>, OutputStream)}, passing {@code body.getClass()} as
	 * the second parameter. Since {@code body.getClass()} loses generic information
	 * due to erasure, this implementation might not be recommended if {@code T} is
	 * a generic type. It might be better to call
	 * {@code write(T, Class<T>, OutputStream)} or provide an alternative
	 * implementation that ensures generic information is not lost.
	 * </p>
	 * 
	 * @param <T>    the type of the object
	 * @param body   the object
	 * @param stream the output
	 * @throws UncheckedIOException if the representation cannot be written
	 * @throws AssemblingException  if the object cannot be transformed
	 */
	@SuppressWarnings("unchecked")
	default <T> void write(T body, OutputStream stream) {
		write(body, (Class<T>) body.getClass(), stream);
	}

	/**
	 * Writes the representation of an arbitrary object to an {@link OutputStream}.
	 * 
	 * @param <T>    the type of the object
	 * @param body   the object
	 * @param type   an object representing {@code T}
	 * @param stream the output
	 * @throws UncheckedIOException if the representation cannot be written
	 * @throws AssemblingException  if the object cannot be transformed
	 */
	<T> void write(T body, Class<T> type, OutputStream stream);

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
	 * @throws UncheckedIOException if the representation cannot be written
	 * @throws AssemblingException  if the object cannot be transformed
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
	 * @throws UncheckedIOException if the representation cannot be written
	 * @throws AssemblingException  if the object cannot be transformed
	 */
	<T> InputStream toStream(T body, Class<T> type);
}
