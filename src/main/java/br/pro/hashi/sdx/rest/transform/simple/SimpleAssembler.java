package br.pro.hashi.sdx.rest.transform.simple;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import br.pro.hashi.sdx.rest.transform.Assembler;

/**
 * A simple assembler can transform arbitrary objects into non-streaming byte
 * representations.
 */
public interface SimpleAssembler extends Assembler {
	/**
	 * Transforms an arbitrary object into a {@code byte[]} representation.
	 * 
	 * @param body the object
	 * @return the representation
	 * @throws IllegalArgumentException if the type of the object is not supported
	 */
	byte[] toBytes(Object body);

	/**
	 * <p>
	 * {@inheritDoc}
	 * </p>
	 * <p>
	 * The default implementation simply calls {@link #toBytes(Object)} and
	 * instantiates a new {@link ByteArrayInputStream} from the {@code byte[]}
	 * representation. Classes are encouraged to provide a more efficient
	 * implementation.
	 * </p>
	 * 
	 * @throws IllegalArgumentException {@inheritDoc}
	 */
	@Override
	default InputStream toStream(Object body) {
		return new ByteArrayInputStream(toBytes(body));
	}
}
