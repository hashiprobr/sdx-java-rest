package br.pro.hashi.sdx.rest.transform.simple;

import java.io.InputStream;
import java.io.UncheckedIOException;

import br.pro.hashi.sdx.rest.coding.Media;
import br.pro.hashi.sdx.rest.transform.Disassembler;
import br.pro.hashi.sdx.rest.transform.Hint;
import br.pro.hashi.sdx.rest.transform.exception.DisassemblingException;

/**
 * A simple disassembler can transform non-streaming byte representations back
 * into objects.
 */
public interface SimpleDisassembler extends Disassembler {
	/**
	 * <p>
	 * {@inheritDoc}
	 * </p>
	 * <p>
	 * The default implementation simply transfers the input to a {@code byte[]} and
	 * calls {@code fromBytes(byte[], Class<T>)}. Classes are encouraged to provide
	 * a more efficient implementation.
	 * </p>
	 * 
	 * @throws UncheckedIOException   {@inheritDoc}
	 * @throws DisassemblingException {@inheritDoc}
	 */
	@Override
	default <T> T fromStream(InputStream stream, Class<T> type) {
		return fromBytes(fromStream(stream), type);
	}

	/**
	 * <p>
	 * {@inheritDoc}
	 * </p>
	 * <p>
	 * The default implementation simply transfers the input to a {@code byte[]} and
	 * calls {@code fromBytes(byte[], Hint<T>)}. Classes are encouraged to provide a
	 * more efficient implementation.
	 * </p>
	 * 
	 * @throws UncheckedIOException   {@inheritDoc}
	 * @throws DisassemblingException {@inheritDoc}
	 */
	@Override
	default <T> T fromStream(InputStream stream, Hint<T> hint) {
		return fromBytes(fromStream(stream), hint);
	}

	private byte[] fromStream(InputStream stream) {
		return Media.read(stream);
	}

	/**
	 * <p>
	 * Transforms a {@code byte[]} representation back into a typed object if
	 * possible.
	 * </p>
	 * <p>
	 * Do not call this method if {@code T} is a generic type. Call
	 * {@code fromBytes(byte[], Hint<T>)} instead.
	 * </p>
	 * 
	 * @param <T>   the type of the object
	 * @param bytes the representation
	 * @param type  an object representing {@code T}
	 * @return the object
	 * @throws DisassemblingException if the representation cannot be transformed
	 */
	<T> T fromBytes(byte[] bytes, Class<T> type);

	/**
	 * <p>
	 * Transforms a {@code byte[]} representation back into a hinted object if
	 * possible.
	 * </p>
	 * <p>
	 * Call this method if {@code T} is a generic type.
	 * </p>
	 * 
	 * @param <T>   the type of the object
	 * @param bytes the representation
	 * @param hint  an object representing {@code T}
	 * @return the object
	 * @throws DisassemblingException if the representation cannot be transformed
	 */
	<T> T fromBytes(byte[] bytes, Hint<T> hint);
}
