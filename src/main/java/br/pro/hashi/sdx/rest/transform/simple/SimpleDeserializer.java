package br.pro.hashi.sdx.rest.transform.simple;

import java.io.Reader;
import java.io.UncheckedIOException;
import java.lang.reflect.Type;

import br.pro.hashi.sdx.rest.coding.Media;
import br.pro.hashi.sdx.rest.transform.Deserializer;
import br.pro.hashi.sdx.rest.transform.exception.DeserializingException;

/**
 * A simple deserializer can transform non-streaming text representations back
 * into objects.
 */
public interface SimpleDeserializer extends Deserializer {
	/**
	 * {@inheritDoc}
	 * 
	 * @implSpec The default implementation simply transfers the input to a
	 *           {@code String} and calls {@link #read(String, Type)}. Classes are
	 *           encouraged to provide a more efficient implementation.
	 * 
	 * @throws UncheckedIOException   {@inheritDoc}
	 * @throws DeserializingException {@inheritDoc}
	 */
	@Override
	default <T> T read(Reader reader, Type type) {
		return read(Media.read(reader), type);
	}

	/**
	 * Transforms a {@code String} representation back into an object if possible.
	 * 
	 * @implNote The implementation can assume that the type is correct.
	 * 
	 * @param <T>     the type of the object
	 * @param content the representation
	 * @param type    an object representing {@code T}
	 * @return the object
	 * @throws DeserializingException if the representation cannot be transformed
	 */
	<T> T read(String content, Type type);
}
