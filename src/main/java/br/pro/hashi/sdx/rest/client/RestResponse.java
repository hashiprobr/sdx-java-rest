package br.pro.hashi.sdx.rest.client;

import java.io.InputStream;
import java.io.Reader;
import java.lang.reflect.Type;

import br.pro.hashi.sdx.rest.Fields;
import br.pro.hashi.sdx.rest.client.exception.ClientException;
import br.pro.hashi.sdx.rest.coding.Media;
import br.pro.hashi.sdx.rest.reflection.Headers;
import br.pro.hashi.sdx.rest.transform.Deserializer;
import br.pro.hashi.sdx.rest.transform.Disassembler;
import br.pro.hashi.sdx.rest.transform.Hint;
import br.pro.hashi.sdx.rest.transform.facade.Facade;

/**
 * Represents the response to a REST request.
 */
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

	InputStream getStream() {
		return stream;
	}

	/**
	 * Obtains the status code of the response.
	 * 
	 * @return the code
	 */
	public int getStatus() {
		return status;
	}

	/**
	 * Obtains the headers of the response.
	 * 
	 * @return the headers
	 */
	public Fields getHeaders() {
		return headers;
	}

	/**
	 * Obtains the content type of the response, with parameters if they are
	 * present.
	 * 
	 * @return the content type
	 */
	public String getContentType() {
		return contentType;
	}

	/**
	 * <p>
	 * Obtains the body as an object of a specified type.
	 * </p>
	 * <p>
	 * This method considers the content type sent by the server.
	 * </p>
	 * 
	 * @param <T>  the type of the body
	 * @param type an object representing {@code T}
	 * @return the body
	 */
	public <T> T getBody(Class<T> type) {
		return getBody(type, contentType);
	}

	/**
	 * <p>
	 * Obtains the body as an object of a hinted type.
	 * </p>
	 * <p>
	 * This method considers the content type sent by the server.
	 * </p>
	 * 
	 * @param <T>  the type of the body
	 * @param hint a hint representing {@code T}
	 * @return the body
	 */
	public <T> T getBody(Hint<T> hint) {
		return getBody(hint, contentType);
	}

	/**
	 * <p>
	 * Obtains the body as an object of a specified type, transforming from a
	 * specified content type, with parameters if they are present.
	 * </p>
	 * <p>
	 * This method ignores the content type sent by the server.
	 * </p>
	 * 
	 * @param <T>         the type of the body
	 * @param type        an object representing {@code T}
	 * @param contentType the content type
	 * @return the body
	 */
	public <T> T getBody(Class<T> type, String contentType) {
		if (type == null) {
			throw new NullPointerException("Type cannot be null");
		}
		return getBody((Type) type, contentType);
	}

	/**
	 * <p>
	 * Obtains the body as an object of a hinted type, transforming from a specified
	 * content type, with parameters if they are present.
	 * </p>
	 * <p>
	 * This method ignores the content type sent by the server.
	 * </p>
	 * 
	 * @param <T>         the type of the body
	 * @param hint        a hint representing {@code T}
	 * @param contentType the content type
	 * @return the body
	 */
	public <T> T getBody(Hint<T> hint, String contentType) {
		if (hint == null) {
			throw new NullPointerException("Hint cannot be null");
		}
		return getBody(hint.getType(), contentType);
	}

	private <T> T getBody(Type type, String contentType) {
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
