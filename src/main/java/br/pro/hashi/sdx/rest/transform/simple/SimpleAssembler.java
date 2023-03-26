package br.pro.hashi.sdx.rest.transform.simple;

import java.io.IOException;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.lang.reflect.Type;

import br.pro.hashi.sdx.rest.transform.Assembler;
import br.pro.hashi.sdx.rest.transform.exception.AssemblingException;

/**
 * A simple assembler can transform objects into non-streaming byte
 * representations.
 */
public interface SimpleAssembler extends Assembler {
	/**
	 * {@inheritDoc}
	 * 
	 * @implSpec The default implementation simply calls
	 *           {@link #toBytes(Object, Type)} and writes the {@code byte[]}
	 *           representation. Classes are encouraged to provide a more efficient
	 *           implementation.
	 * 
	 * @throws UncheckedIOException {@inheritDoc}
	 * @throws AssemblingException  {@inheritDoc}
	 */
	@Override
	default void write(Object body, Type type, OutputStream stream) {
		byte[] bytes = toBytes(body, type);
		try {
			stream.write(bytes);
		} catch (IOException exception) {
			throw new UncheckedIOException(exception);
		}
	}

	/**
	 * Transforms an arbitrary object into a {@code byte[]} representation if
	 * possible.
	 * 
	 * @implSpec The default implementation simply calls
	 *           {@link #toBytes(Object, Type)}, passing {@code body.getClass()} as
	 *           the second parameter. Since {@code body.getClass()} loses generic
	 *           information due to type erasure, this implementation might not be
	 *           recommended if the object is generic. It might be better to call
	 *           {@link #toBytes(Object, Type)} or provide an alternative
	 *           implementation that ensures generic information is not lost.
	 * 
	 * @param body the object
	 * @return the representation
	 * @throws AssemblingException if the object cannot be transformed
	 */
	default byte[] toBytes(Object body) {
		return toBytes(body, body == null ? Object.class : body.getClass());
	}

	/**
	 * Transforms a typed object into a {@code byte[]} representation if possible.
	 * 
	 * @implNote The implementation can assume that the type is correct.
	 * 
	 * @param body the object
	 * @param type the type of the object
	 * @return the representation
	 * @throws AssemblingException if the object cannot be transformed
	 */
	byte[] toBytes(Object body, Type type);
}
