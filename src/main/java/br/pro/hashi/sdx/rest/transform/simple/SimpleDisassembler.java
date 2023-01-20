package br.pro.hashi.sdx.rest.transform.simple;

import java.io.InputStream;
import java.io.UncheckedIOException;
import java.lang.reflect.Type;

import br.pro.hashi.sdx.rest.coding.Media;
import br.pro.hashi.sdx.rest.transform.Disassembler;
import br.pro.hashi.sdx.rest.transform.exception.DisassemblingException;

/**
 * A simple disassembler can transform non-streaming byte representations back
 * into objects.
 */
public interface SimpleDisassembler extends Disassembler {
	/**
	 * {@inheritDoc}
	 * 
	 * @implSpec The default implementation simply transfers the input to a
	 *           {@code byte[]} and calls {@link #read(byte[], Type)}. Classes are
	 *           encouraged to provide a more efficient implementation.
	 * 
	 * @throws UncheckedIOException   {@inheritDoc}
	 * @throws DisassemblingException {@inheritDoc}
	 */
	@Override
	default <T> T read(InputStream stream, Type type) {
		return read(Media.read(stream), type);
	}

	/**
	 * Transforms a {@code byte[]} representation back into an object if possible.
	 * 
	 * @implNote The implementation can assume that the type is correct.
	 * 
	 * @param <T>   the type of the object
	 * @param bytes the representation
	 * @param type  an object representing {@code T}
	 * @return the object
	 * @throws DisassemblingException if the representation cannot be transformed
	 */
	<T> T read(byte[] bytes, Type type);
}
