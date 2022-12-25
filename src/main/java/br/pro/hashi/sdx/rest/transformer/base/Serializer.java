package br.pro.hashi.sdx.rest.transformer.base;

/**
 * A serializer can transform objects into text representations.
 */
public interface Serializer {
	/**
	 * Transforms an object into a text representation.
	 * 
	 * @param body the object.
	 * @return a {@link String} representing the object.
	 */
	public String serialize(Object body);
}
