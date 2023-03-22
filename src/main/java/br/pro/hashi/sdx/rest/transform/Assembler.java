package br.pro.hashi.sdx.rest.transform;

import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.lang.reflect.Type;

import br.pro.hashi.sdx.rest.transform.exception.AssemblingException;

/**
 * An assembler can transform objects into byte representations.
 */
public interface Assembler {
	/**
	 * Writes the representation of an arbitrary object to an {@link OutputStream}
	 * if possible.
	 * 
	 * @implSpec The default implementation simply calls
	 *           {@link #write(Object, Type, OutputStream)}, passing
	 *           {@code body.getClass()} as the second parameter. Since
	 *           {@code body.getClass()} loses generic information due to type
	 *           erasure, this implementation might not be recommended if the object
	 *           is generic. It might be better to call
	 *           {@link #write(Object, Type, OutputStream)} or provide an
	 *           alternative implementation that ensures generic information is not
	 *           lost.
	 * 
	 * @param body   the object
	 * @param stream the output
	 * @throws UncheckedIOException if the representation cannot be written
	 * @throws AssemblingException  if the object cannot be transformed
	 */
	default void write(Object body, OutputStream stream) {
		write(body, body == null ? Object.class : body.getClass(), stream);
	}

	/**
	 * Writes the representation of a typed object to an {@link OutputStream} if
	 * possible.
	 * 
	 * @implNote The implementation can assume that the type is correct.
	 * 
	 * @param body   the object
	 * @param type   the type of the object
	 * @param stream the output
	 * @throws UncheckedIOException if the representation cannot be written
	 * @throws AssemblingException  if the object cannot be transformed
	 */
	void write(Object body, Type type, OutputStream stream);
}
