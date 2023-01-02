package br.pro.hashi.sdx.rest.transformer.base;

import java.io.InputStream;

/**
 * An assembler can transform objects into byte representations.
 */
public interface Assembler {
	/**
	 * Transforms an object into a byte representation.
	 * 
	 * @param body the object.
	 * @return an {@link InputStream} representing the object.
	 */
	InputStream assemble(Object body);
}
