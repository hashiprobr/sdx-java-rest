package br.pro.hashi.sdx.rest.client;

import java.io.InputStream;
import java.io.Reader;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Set;

import br.pro.hashi.sdx.rest.client.exception.ClientException;
import br.pro.hashi.sdx.rest.coding.Media;
import br.pro.hashi.sdx.rest.reflection.Headers;
import br.pro.hashi.sdx.rest.transform.Deserializer;
import br.pro.hashi.sdx.rest.transform.Disassembler;
import br.pro.hashi.sdx.rest.transform.Hint;
import br.pro.hashi.sdx.rest.transform.facade.Facade;

public class RestResponse {
	private final Facade facade;
	private final int status;
	private final Headers headers;
	private final String contentType;
	private InputStream stream;
	private boolean available;

	RestResponse(Facade facade, int status, Headers headers, String contentType, InputStream stream) {
		this.facade = facade;
		this.status = status;
		this.headers = headers;
		this.contentType = contentType;
		this.stream = stream;
		this.available = true;
	}

	Facade getFacade() {
		return facade;
	}

	Headers getHeaders() {
		return headers;
	}

	InputStream getStream() {
		return stream;
	}

	public int getStatus() {
		return status;
	}

	public List<String> splitHeader(String name, String regex) {
		return headers.split(name, regex);
	}

	public <T> List<T> splitHeader(String name, String regex, Class<T> type) {
		return headers.split(name, regex, type);
	}

	public String requireHeader(String name) {
		return headers.require(name);
	}

	public <T> T requireHeader(String name, Class<T> type) {
		return headers.require(name, type);
	}

	public List<String> getHeaderList(String name) {
		return headers.getList(name);
	}

	public <T> List<T> getHeaderList(String name, Class<T> type) {
		return headers.getList(name, type);
	}

	public String getHeader(String name) {
		return headers.get(name);
	}

	public String getHeader(String name, String defaultValue) {
		return headers.get(name, defaultValue);
	}

	public <T> T getHeader(String name, Class<T> type) {
		return headers.get(name, type);
	}

	public <T> T getHeader(String name, Class<T> type, T defaultValue) {
		return headers.get(name, type, defaultValue);
	}

	public Set<String> headerNames() {
		return headers.names();
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
		synchronized (this) {
			if (!available) {
				throw new ClientException("Stream is not available");
			}
			available = false;
		}
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
