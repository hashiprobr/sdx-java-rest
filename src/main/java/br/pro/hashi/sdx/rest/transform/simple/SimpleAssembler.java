package br.pro.hashi.sdx.rest.transform.simple;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import br.pro.hashi.sdx.rest.transform.Assembler;

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
	 * instantiates a new {@link ByteArrayInputStream} from the {@code byte[]}
	 * representation. Classes are encouraged to provide a more efficient
	 * implementation.
	 * </p>
	 * 
	 * @throws IllegalArgumentException {@inheritDoc}
	 * @throws IOException              {@inheritDoc}
	 */
	@Override
	default <T> InputStream toStream(T body, Class<T> type) throws IOException {
		return new ByteArrayInputStream(toBytes(body, type));
	}

	/**
	 * <p>
	 * Transforms an arbitrary object into a {@code byte[]} representation.
	 * </p>
	 * <p>
	 * The default implementation simply calls {@code toBytes(T, Class<T>)}, passing
	 * {@code body.getClass()} as the second parameter. <strong>Do not use this
	 * implementation if {@code T} is a generic type.</strong> Either call
	 * {@code toBytes(T, Class<T>)} or provide an alternative implementation.
	 * </p>
	 * 
	 * @param <T>  the type of the object
	 * @param body the object
	 * @return the representation
	 * @throws IllegalArgumentException if the type of the object is not supported
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
	 * @throws IllegalArgumentException if the type of the object is not supported
	 */
	<T> byte[] toBytes(T body, Class<T> type);
}
