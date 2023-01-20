package br.pro.hashi.sdx.rest.transform.facade;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.lang.reflect.Type;

import br.pro.hashi.sdx.rest.transform.Assembler;
import br.pro.hashi.sdx.rest.transform.exception.AssemblingException;

class OctetAssembler implements Assembler {
	@Override
	public void write(Object body, Type type, OutputStream stream) {
		write(body, stream);
	}

	@Override
	public void write(Object body, OutputStream stream) {
		try {
			if (body instanceof byte[]) {
				stream.write((byte[]) body);
				return;
			}
			if (body instanceof InputStream) {
				((InputStream) body).transferTo(stream);
				return;
			}
			throw new AssemblingException("Body must be instance of byte[] or InputStream");
		} catch (IOException exception) {
			throw new UncheckedIOException(exception);
		} finally {
			try {
				stream.close();
			} catch (IOException exception) {
				throw new UncheckedIOException(exception);
			}
		}
	}
}
