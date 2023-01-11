package br.pro.hashi.sdx.rest.transform.simple;

import java.io.IOException;
import java.io.Reader;

import br.pro.hashi.sdx.rest.coding.Media;
import br.pro.hashi.sdx.rest.transform.Deserializer;
import br.pro.hashi.sdx.rest.transform.exception.DeserializingException;

/**
 * A simple deserializer can transform non-streaming text representations back
 * into objects.
 */
public interface SimpleDeserializer extends Deserializer {
	/**
	 * Transforms a {@code String} representation back into an object if possible.
	 * 
	 * @param <T>     the type of the object
	 * @param content the representation
	 * @param type    an object representing {@code T}
	 * @return the object
	 * @throws IllegalArgumentException if the type of the object is not supported
	 * @throws DeserializingException   if it is not possible to transform the
	 *                                  representation
	 */
	<T> T fromString(String content, Class<T> type) throws DeserializingException;

	/**
	 * <p>
	 * {@inheritDoc}
	 * </p>
	 * <p>
	 * The default implementation simply transfers the input to a {@code String} and
	 * calls {@link #fromString(String, Class)}. Classes are encouraged to provide a
	 * more efficient implementation.
	 * </p>
	 * 
	 * @throws IllegalArgumentException {@inheritDoc}
	 * @throws IOException              {@inheritDoc}
	 * @throws DeserializingException   {@inheritDoc}
	 */
	@Override
	default <T> T fromReader(Reader reader, Class<T> type) throws IOException, DeserializingException {
		return fromString(Media.read(reader), type);
	}
}
