package br.pro.hashi.sdx.rest.transformer.base;

import br.pro.hashi.sdx.rest.transformer.exception.DeserializingException;

/**
 * A deserializer can transform text representations into objects.
 */
public interface Deserializer {
	/**
	 * Transforms a text representation into an object.
	 * 
	 * @param <T>     the type of the object.
	 * @param content a {@link String} representing the object.
	 * @param type    a {@link Class}{@code <T>} representing the type of the
	 *                object.
	 * @return the object.
	 * @throws DeserializingException if the representation cannot be transformed.
	 */
	<T> T deserialize(String content, Class<T> type) throws DeserializingException;
}
