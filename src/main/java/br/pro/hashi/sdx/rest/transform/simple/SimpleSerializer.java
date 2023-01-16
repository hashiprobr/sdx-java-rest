package br.pro.hashi.sdx.rest.transform.simple;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.io.UncheckedIOException;
import java.io.Writer;
import java.lang.reflect.Type;

import br.pro.hashi.sdx.rest.transform.Serializer;
import br.pro.hashi.sdx.rest.transform.exception.SerializingException;

/**
 * A simple serializer can transform objects into non-streaming text
 * representations.
 */
public interface SimpleSerializer extends Serializer {
	/**
	 * {@inheritDoc}
	 * 
	 * @implSpec The default implementation simply calls
	 *           {@link #toString(Object, Type)} and writes the {@code String}
	 *           representation. Classes are encouraged to provide a more efficient
	 *           implementation.
	 * 
	 * @throws UncheckedIOException {@inheritDoc}
	 * @throws SerializingException {@inheritDoc}
	 */
	@Override
	default void write(Object body, Type type, Writer writer) {
		String content = toString(body, type);
		try {
			writer.write(content);
			writer.close();
		} catch (IOException exception) {
			throw new UncheckedIOException(exception);
		}
	}

	/**
	 * {@inheritDoc}
	 * 
	 * @implSpec The default implementation simply calls
	 *           {@link #toString(Object, Type)} and instantiates a
	 *           {@link StringReader} from the {@code String} representation.
	 *           Classes are encouraged to provide a more efficient implementation.
	 * 
	 * @throws UncheckedIOException {@inheritDoc}
	 * @throws SerializingException {@inheritDoc}
	 */
	@Override
	default Reader toReader(Object body, Type type) {
		return new StringReader(toString(body, type));
	}

	/**
	 * Transforms an arbitrary object into a {@code String} representation.
	 * 
	 * @implSpec The default implementation simply calls
	 *           {@link #toString(Object, Type)}, passing {@code body.getClass()} as
	 *           the second parameter. Since {@code body.getClass()} loses generic
	 *           information due to type erasure, this implementation might not be
	 *           recommended if the object is generic. It might be better to call
	 *           {@link #toString(Object, Type)} or provide an alternative
	 *           implementation that ensures generic information is not lost.
	 * 
	 * @param body the object
	 * @return the representation
	 * @throws SerializingException if the object cannot be transformed
	 */
	default String toString(Object body) {
		return toString(body, body.getClass());
	}

	/**
	 * Transforms a typed object into a {@code String} representation.
	 * 
	 * @implNote The implementation can assume that the type is correct.
	 * 
	 * @param body the object
	 * @param type the type of the object
	 * @return the representation
	 * @throws SerializingException if the object cannot be transformed
	 */
	String toString(Object body, Type type);
}
