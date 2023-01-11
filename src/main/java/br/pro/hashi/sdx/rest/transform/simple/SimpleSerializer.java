package br.pro.hashi.sdx.rest.transform.simple;

import java.io.Reader;
import java.io.StringReader;

import br.pro.hashi.sdx.rest.transform.Serializer;

/**
 * A simple serializer can transform arbitrary objects into non-streaming text
 * representations.
 */
public interface SimpleSerializer extends Serializer {
	/**
	 * Transforms an arbitrary object into a {@code String} representation.
	 * 
	 * @param body the object
	 * @return the representation
	 * @throws IllegalArgumentException if the type of the object is not supported
	 */
	String toString(Object body);

	/**
	 * <p>
	 * {@inheritDoc}
	 * </p>
	 * <p>
	 * The default implementation simply calls {@link #toString(Object)} and
	 * instantiates a new {@link StringReader} from the {@code String}
	 * representation. Classes are encouraged to provide a more efficient
	 * implementation.
	 * </p>
	 * 
	 * @throws IllegalArgumentException {@inheritDoc}
	 */
	@Override
	default Reader toReader(Object body) {
		return new StringReader(toString(body));
	}
}
