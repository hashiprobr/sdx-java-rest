package br.pro.hashi.sdx.rest.server.tree;

import java.io.InputStream;
import java.io.Reader;
import java.lang.reflect.Type;

import br.pro.hashi.sdx.rest.coding.MediaCoder;
import br.pro.hashi.sdx.rest.server.stream.LimitInputStream;
import br.pro.hashi.sdx.rest.transform.Deserializer;
import br.pro.hashi.sdx.rest.transform.Disassembler;
import br.pro.hashi.sdx.rest.transform.facade.Facade;

public class Data {
	private final Facade facade;
	private final String contentType;
	private final InputStream stream;

	public Data(Facade facade, String contentType, InputStream stream) {
		this.facade = facade;
		this.contentType = contentType;
		this.stream = stream;
	}

	public String getContentType() {
		return contentType;
	}

	public InputStream getStream() {
		return stream;
	}

	Object getBody(Type type, long maxSize) {
		Object body;
		String contentType = this.contentType;
		InputStream stream = MediaCoder.getInstance().decode(limit(this.stream, maxSize), contentType);
		if (facade.isBinary(type)) {
			contentType = facade.getDisassemblerType(strip(contentType), type);
			Disassembler disassembler = facade.getDisassembler(contentType);
			body = disassembler.read(stream, type);
		} else {
			Reader reader = MediaCoder.getInstance().reader(stream, contentType);
			contentType = facade.getDeserializerType(strip(contentType), type);
			Deserializer deserializer = facade.getDeserializer(contentType);
			body = deserializer.read(reader, type);
		}
		return body;
	}

	private InputStream limit(InputStream stream, long maxSize) {
		if (maxSize > 0) {
			stream = new LimitInputStream(stream, maxSize);
		}
		return stream;
	}

	private String strip(String contentType) {
		if (contentType != null) {
			contentType = MediaCoder.getInstance().strip(contentType);
		}
		return contentType;
	}
}
