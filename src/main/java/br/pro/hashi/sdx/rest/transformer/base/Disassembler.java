package br.pro.hashi.sdx.rest.transformer.base;

import java.io.InputStream;

import br.pro.hashi.sdx.rest.transformer.exception.DisassemblingException;

/**
 * A disassembler can transform byte representations into objects.
 */
public interface Disassembler {
	/**
	 * Transforms a byte representation into an object.
	 * 
	 * @param <T>    the type of the object.
	 * @param stream an {@link InputStream} representing the object.
	 * @param type   a {@link Class}{@code <T>} representing the type of the object.
	 * @return the object.
	 * @throws DisassemblingException if the representation cannot be transformed.
	 */
	public <T> T disassemble(InputStream stream, Class<T> type) throws DisassemblingException;
}
