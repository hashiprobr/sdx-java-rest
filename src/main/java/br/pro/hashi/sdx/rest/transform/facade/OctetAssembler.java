package br.pro.hashi.sdx.rest.transform.facade;

import java.io.ByteArrayInputStream;
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
		if (body instanceof byte[]) {
			try {
				stream.write((byte[]) body);
				stream.close();
			} catch (IOException exception) {
				throw new UncheckedIOException(exception);
			}
			return;
		}
		if (body instanceof InputStream) {
			try {
				((InputStream) body).transferTo(stream);
				stream.close();
			} catch (IOException exception) {
				throw new UncheckedIOException(exception);
			}
			return;
		}
		throw new AssemblingException("Body must be instance of byte[] or InputStream to be written");
	}

	@Override
	public InputStream toStream(Object body, Type type) {
		return toStream(body);
	}

	@Override
	public InputStream toStream(Object body) {
		if (body instanceof byte[]) {
			return new ByteArrayInputStream((byte[]) body);
		}
		if (body instanceof InputStream) {
			return (InputStream) body;
		}
		throw new AssemblingException("Body must be instance of byte[] or InputStream to be read");
	}
}
