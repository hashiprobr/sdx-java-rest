package br.pro.hashi.sdx.rest.transform.simple;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UncheckedIOException;

import br.pro.hashi.sdx.rest.transform.Assembler;
import br.pro.hashi.sdx.rest.transform.Hint;
import br.pro.hashi.sdx.rest.transform.exception.AssemblingException;

/**
 * A simple assembler can transform objects into non-streaming byte
 * representations.
 */
public interface SimpleAssembler extends Assembler {
	/**
	 * <p>
	 * {@inheritDoc}
	 * </p>
	 * <p>
	 * The default implementation simply calls {@code toBytes(T, Class<T>)} and
	 * writes the {@code byte[]} representation. Classes are encouraged to provide a
	 * more efficient implementation.
	 * </p>
	 * 
	 * @throws UncheckedIOException {@inheritDoc}
	 * @throws AssemblingException  {@inheritDoc}
	 */
	@Override
	default <T> void write(T body, Class<T> type, OutputStream stream) {
		write(toBytes(body, type), stream);
	}

	/**
	 * <p>
	 * {@inheritDoc}
	 * </p>
	 * <p>
	 * The default implementation simply calls {@code toBytes(T, Hint<T>)} and
	 * writes the {@code byte[]} representation. Classes are encouraged to provide a
	 * more efficient implementation.
	 * </p>
	 * 
	 * @throws UncheckedIOException {@inheritDoc}
	 * @throws AssemblingException  {@inheritDoc}
	 */
	@Override
	default <T> void write(T body, Hint<T> hint, OutputStream stream) {
		write(toBytes(body, hint), stream);
	}

	private <T> void write(byte[] bytes, OutputStream stream) {
		try {
			stream.write(bytes);
			stream.close();
		} catch (IOException exception) {
			throw new UncheckedIOException(exception);
		}
	}

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
		return toStream(toBytes(body, type));
	}

	/**
	 * <p>
	 * {@inheritDoc}
	 * </p>
	 * <p>
	 * The default implementation simply calls {@code toBytes(T, Hint<T>)} and
	 * instantiates a {@link ByteArrayInputStream} from the {@code byte[]}
	 * representation. Classes are encouraged to provide a more efficient
	 * implementation.
	 * </p>
	 * 
	 * @throws UncheckedIOException {@inheritDoc}
	 * @throws AssemblingException  {@inheritDoc}
	 */
	@Override
	default <T> InputStream toStream(T body, Hint<T> hint) {
		return toStream(toBytes(body, hint));
	}

	private <T> InputStream toStream(byte[] bytes) {
		return new ByteArrayInputStream(bytes);
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
	 * <p>
	 * Transforms a typed object into a {@code byte[]} representation.
	 * </p>
	 * <p>
	 * Do not call this method if {@code T} is a generic type. Call
	 * {@code toBytes(T, Hint<T>)} instead.
	 * </p>
	 * 
	 * @param <T>  the type of the object
	 * @param body the object
	 * @param type an object representing {@code T}
	 * @return the representation
	 * @throws AssemblingException if the object cannot be transformed
	 */
	<T> byte[] toBytes(T body, Class<T> type);

	/**
	 * <p>
	 * Transforms a hinted object into a {@code byte[]} representation.
	 * </p>
	 * <p>
	 * Call this method if {@code T} is a generic type.
	 * </p>
	 * 
	 * @param <T>  the type of the object
	 * @param body the object
	 * @param hint an object representing {@code T}
	 * @return the representation
	 * @throws AssemblingException if the object cannot be transformed
	 */
	<T> byte[] toBytes(T body, Hint<T> hint);
}
