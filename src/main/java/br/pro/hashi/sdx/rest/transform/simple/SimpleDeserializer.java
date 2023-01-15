package br.pro.hashi.sdx.rest.transform.simple;

import java.io.Reader;
import java.io.UncheckedIOException;

import br.pro.hashi.sdx.rest.coding.Media;
import br.pro.hashi.sdx.rest.transform.Deserializer;
import br.pro.hashi.sdx.rest.transform.Hint;
import br.pro.hashi.sdx.rest.transform.exception.DeserializingException;

/**
 * A simple deserializer can transform non-streaming text representations back
 * into objects.
 */
public interface SimpleDeserializer extends Deserializer {
	/**
	 * <p>
	 * {@inheritDoc}
	 * </p>
	 * <p>
	 * The default implementation simply transfers the input to a {@code String} and
	 * calls {@code fromString(String, Class<T>)}. Classes are encouraged to provide
	 * a more efficient implementation.
	 * </p>
	 * 
	 * @throws UncheckedIOException   {@inheritDoc}
	 * @throws DeserializingException {@inheritDoc}
	 */
	@Override
	default <T> T fromReader(Reader reader, Class<T> type) {
		return fromString(fromReader(reader), type);
	}

	/**
	 * <p>
	 * {@inheritDoc}
	 * </p>
	 * <p>
	 * The default implementation simply transfers the input to a {@code String} and
	 * calls {@code fromString(String, Hint<T>)}. Classes are encouraged to provide
	 * a more efficient implementation.
	 * </p>
	 * 
	 * @throws UncheckedIOException   {@inheritDoc}
	 * @throws DeserializingException {@inheritDoc}
	 */
	@Override
	default <T> T fromReader(Reader reader, Hint<T> hint) {
		return fromString(fromReader(reader), hint);
	}

	private String fromReader(Reader reader) {
		return Media.read(reader);
	}

	/**
	 * <p>
	 * Transforms a {@code String} representation back into a typed object if
	 * possible.
	 * </p>
	 * <p>
	 * Do not call this method if {@code T} is a generic type. Call
	 * {@code fromString(T, Hint<T>)} instead.
	 * </p>
	 * 
	 * @param <T>     the type of the object
	 * @param content the representation
	 * @param type    an object representing {@code T}
	 * @return the object
	 * @throws DeserializingException if the representation cannot be transformed
	 */
	<T> T fromString(String content, Class<T> type);

	/**
	 * <p>
	 * Transforms a {@code String} representation back into a hinted object if
	 * possible.
	 * </p>
	 * <p>
	 * Call this method if {@code T} is a generic type.
	 * </p>
	 * 
	 * @param <T>     the type of the object
	 * @param content the representation
	 * @param hint    an object representing {@code T}
	 * @return the object
	 * @throws DeserializingException if the representation cannot be transformed
	 */
	<T> T fromString(String content, Hint<T> hint);
}
