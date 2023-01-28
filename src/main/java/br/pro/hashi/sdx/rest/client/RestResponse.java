package br.pro.hashi.sdx.rest.client;

import java.io.InputStream;
import java.io.Reader;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

import org.eclipse.jetty.http.HttpFields;

import br.pro.hashi.sdx.rest.coding.Media;
import br.pro.hashi.sdx.rest.transform.Deserializer;
import br.pro.hashi.sdx.rest.transform.Disassembler;
import br.pro.hashi.sdx.rest.transform.facade.Facade;

public class RestResponse {
	private final Facade facade;
	private final int status;
	private final HttpFields fields;
	private final String contentType;
	private InputStream stream;
	private String content;
	private boolean available;

	public RestResponse(Facade facade, int status, HttpFields fields, String contentType, InputStream stream) {
		this.facade = facade;
		this.status = status;
		this.fields = fields;
		this.contentType = contentType;
		this.stream = stream;
		this.content = null;
		this.available = true;
	}

	HttpFields getFields() {
		return fields;
	}

	InputStream getStream() {
		return stream;
	}

	private void validate() {
		if (!available) {
			throw new RuntimeException("Stream is not available");
		}
		available = false;
	}

	private void decode() {
		validate();
		if (contentType != null) {
			stream = Media.decode(stream, contentType);
		}
	}

	private void read() {
		if (content == null) {
			validate();
			Reader reader = Media.reader(stream, contentType);
			content = Media.read(reader);
		}
	}

	private <T> T disassemble(String responseType, Type type) {
		decode();
		// responseType = facade.cleanForDisassembling(responseType, type);
		Disassembler disassembler = facade.getDisassembler(responseType);
		return disassembler.read(stream, type);
	}

	private <T> T deserialize(String responseType, Class<T> type) {
		read();
		// responseType = facade.cleanForDeserializing(responseType, type);
		Deserializer deserializer = facade.getDeserializer(responseType);
		return null;
		// return deserializer.fromReader(stream, type);
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
			throw new IllegalArgumentException("Response header name cannot be null");
		}
		name = name.strip();
		if (name.isEmpty()) {
			throw new IllegalArgumentException("Response header name cannot be blank");
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

	public <T> T getBodyAs(Class<T> type, String forcedType) {
		if (type == null) {
			throw new IllegalArgumentException("Type cannot be null");
		}
		if (facade.isBinary(type)) {
			return disassemble(forcedType, type);
		}
		return deserialize(forcedType, type);
	}

	public <T> T getBodyAs(Class<T> type) {
		return getBodyAs(type, contentType);
	}
}
