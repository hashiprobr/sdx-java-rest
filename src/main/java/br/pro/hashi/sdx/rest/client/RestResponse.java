package br.pro.hashi.sdx.rest.client;

import java.io.InputStream;
import java.io.Reader;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

import org.eclipse.jetty.http.HttpFields;

import br.pro.hashi.sdx.rest.client.exception.ClientException;
import br.pro.hashi.sdx.rest.coding.Media;
import br.pro.hashi.sdx.rest.transform.Deserializer;
import br.pro.hashi.sdx.rest.transform.Disassembler;
import br.pro.hashi.sdx.rest.transform.Hint;
import br.pro.hashi.sdx.rest.transform.facade.Facade;

public class RestResponse {
	private final Facade facade;
	private final int status;
	private final HttpFields fields;
	private final String contentType;
	private InputStream stream;
	private boolean available;

	RestResponse(Facade facade, int status, HttpFields fields, String contentType, InputStream stream) {
		this.facade = facade;
		this.status = status;
		this.fields = fields;
		this.contentType = contentType;
		this.stream = stream;
		this.available = true;
	}

	HttpFields getFields() {
		return fields;
	}

	InputStream getStream() {
		return stream;
	}

	public int getStatus() {
		return status;
	}

	public List<String> getHeaderNames() {
		List<String> names = new ArrayList<>();
		Enumeration<String> enumeration = fields.getFieldNames();
		while (enumeration.hasMoreElements()) {
			names.add(enumeration.nextElement());
		}
		return names;
	}

	public List<String> getHeaders(String name) {
		if (name == null) {
			throw new IllegalArgumentException("Header name cannot be null");
		}
		name = name.strip();
		if (name.isEmpty()) {
			throw new IllegalArgumentException("Header name cannot be blank");
		}
		return fields.getValuesList(name);
	}

	public String getHeader(String name) {
		List<String> values = getHeaders(name);
		if (values.isEmpty()) {
			return null;
		}
		return values.get(0);
	}

	public String getContentType() {
		return contentType;
	}

	public <T> T getBodyAs(Class<T> type) {
		return getBodyAs(type, contentType);
	}

	public <T> T getBodyAs(Hint<T> hint) {
		return getBodyAs(hint, contentType);
	}

	public <T> T getBodyAs(Class<T> type, String contentType) {
		if (type == null) {
			throw new NullPointerException("Type cannot be null");
		}
		return getBodyAs((Type) type, contentType);
	}

	public <T> T getBodyAs(Hint<T> hint, String contentType) {
		if (hint == null) {
			throw new NullPointerException("Hint cannot be null");
		}
		return getBodyAs(hint.getType(), contentType);
	}

	private <T> T getBodyAs(Type type, String contentType) {
		if (!available) {
			throw new ClientException("Stream is not available");
		}
		available = false;
		T body;
		stream = Media.decode(stream, contentType);
		if (facade.isBinary(type)) {
			contentType = facade.cleanForDisassembling(contentType, type);
			Disassembler disassembler = facade.getDisassembler(contentType);
			body = disassembler.read(stream, type);
		} else {
			Reader reader = Media.reader(stream, contentType);
			contentType = facade.cleanForDeserializing(contentType, type);
			Deserializer deserializer = facade.getDeserializer(contentType);
			body = deserializer.read(reader, type);
		}
		return body;
	}
}
