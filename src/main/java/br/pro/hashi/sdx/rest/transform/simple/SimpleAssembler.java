package br.pro.hashi.sdx.rest.transform.simple;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.UncheckedIOException;

import br.pro.hashi.sdx.rest.transform.Assembler;
import br.pro.hashi.sdx.rest.transform.exception.AssemblingException;

/**
 * A simple assembler can transform arbitrary objects into non-streaming byte
 * representations.
 */
public interface SimpleAssembler extends Assembler {
	/**
	 * <p>
	 * {@inheritDoc}
	 * </p>
	 * <p>
	 * The default implementation simply calls {@code toBytes(T, Class<T>)} and
	 * instantiates a {@link ByteArrayInputStream} from the {@code byte[]}
	 * representation. Classes are encouraged to provide a more efficient
	 * implementation.
	 * </p>
	 * 
	 * @throws UncheckedIOException {@inheritDoc}
	 * @throws AssemblingException  {@inheritDoc}
	 */
	@Override
	default <T> InputStream toStream(T body, Class<T> type) {
		return new ByteArrayInputStream(toBytes(body, type));
	}

	/**
	 * <p>
	 * Transforms an arbitrary object into a {@code byte[]} representation.
	 * </p>
	 * <p>
	 * The default implementation simply calls {@code toBytes(T, Class<T>)}, passing
	 * {@code body.getClass()} as the second parameter. Since
	 * {@code body.getClass()} loses generic information due to erasure, this
	 * implementation might not be recommended if {@code T} is a generic type. It
	 * might be better to call {@code toBytes(T, Class<T>)} or provide an
	 * alternative implementation that ensures generic information is not lost.
	 * </p>
	 * 
	 * @param <T>  the type of the object
	 * @param body the object
	 * @return the representation
	 * @throws AssemblingException if the object cannot be transformed
	 */
	@SuppressWarnings("unchecked")
	default <T> byte[] toBytes(T body) {
		return toBytes(body, (Class<T>) body.getClass());
	}

	/**
	 * Transforms a typed object into a {@code byte[]} representation.
	 * 
	 * @param <T>  the type of the object
	 * @param body the object
	 * @param type an object representing {@code T}
	 * @return the representation
	 * @throws AssemblingException if the object cannot be transformed
	 */
	<T> byte[] toBytes(T body, Class<T> type);
}
