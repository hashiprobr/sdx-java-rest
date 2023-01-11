package br.pro.hashi.sdx.rest.transform.simple;

import java.io.IOException;
import java.io.InputStream;

import br.pro.hashi.sdx.rest.transform.Disassembler;
import br.pro.hashi.sdx.rest.transform.exception.DisassemblingException;

/**
 * A simple disassembler can transform non-streaming byte representations back
 * into objects.
 */
public interface SimpleDisassembler extends Disassembler {
	/**
	 * Transforms a {@code byte[]} representation back into an object if possible.
	 * 
	 * @param <T>   the type of the object
	 * @param bytes the representation
	 * @param type  an object representing {@code T}
	 * @return the object
	 * @throws IllegalArgumentException if the type of the object is not supported
	 * @throws DisassemblingException   if it is not possible to transform the
	 *                                  representation
	 */
	<T> T fromBytes(byte[] bytes, Class<T> type) throws DisassemblingException;

	/**
	 * <p>
	 * {@inheritDoc}
	 * </p>
	 * <p>
	 * The default implementation simply transfers the input to a {@code byte[]} and
	 * calls {@link #fromBytes(byte[], Class)}. Classes are encouraged to provide a
	 * more efficient implementation.
	 * </p>
	 * 
	 * @throws IllegalArgumentException {@inheritDoc}
	 * @throws IOException              {@inheritDoc}
	 * @throws DisassemblingException   {@inheritDoc}
	 */
	@Override
	default <T> T fromStream(InputStream stream, Class<T> type) throws IOException, DisassemblingException {
		return fromBytes(stream.readAllBytes(), type);
	}
}
