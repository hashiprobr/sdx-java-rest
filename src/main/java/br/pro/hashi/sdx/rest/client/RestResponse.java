package br.pro.hashi.sdx.rest.client;

import java.io.InputStream;
import java.io.Reader;
import java.lang.reflect.Type;

import br.pro.hashi.sdx.rest.Fields;
import br.pro.hashi.sdx.rest.Hint;
import br.pro.hashi.sdx.rest.coding.MediaCoder;
import br.pro.hashi.sdx.rest.transform.Deserializer;
import br.pro.hashi.sdx.rest.transform.Disassembler;
import br.pro.hashi.sdx.rest.transform.manager.TransformManager;

/**
 * Represents the response to a REST request.
 */
public final class RestResponse {
	static RestResponse newInstance(TransformManager manager, int status, Fields headers, String contentType, InputStream stream) {
		MediaCoder coder = MediaCoder.getInstance();
		return new RestResponse(coder, manager, status, headers, contentType, stream);
	}

	private final MediaCoder coder;
	private final TransformManager manager;
	private final int status;
	private final Fields headers;
	private final String contentType;
	private final InputStream stream;
	private boolean available;

	RestResponse(MediaCoder coder, TransformManager manager, int status, Fields headers, String contentType, InputStream stream) {
		this.coder = coder;
		this.manager = manager;
		this.status = status;
		this.headers = headers;
		this.contentType = contentType;
		this.stream = stream;
		this.available = true;
	}

	TransformManager getManager() {
		return manager;
	}

	InputStream getStream() {
		return stream;
	}

	/**
	 * Obtains the status code of this response.
	 * 
	 * @return the code
	 */
	public int getStatus() {
		return status;
	}

	/**
	 * Obtains the headers of this response.
	 * 
	 * @return the headers
	 */
	public Fields getHeaders() {
		return headers;
	}

	/**
	 * Obtains the content type of this response, with parameters if they are
	 * present.
	 * 
	 * @return the content type
	 */
	public String getContentType() {
		return contentType;
	}

	/**
	 * <p>
	 * Obtains the body of this response as an object of the specified type,
	 * considering the content type sent by the server.
	 * </p>
	 * <p>
	 * Since {@link Class} objects lose generic information due to type erasure, do
	 * not call this method if the type is generic. Call {@code getBody(Hint<T>)}
	 * instead.
	 * </p>
	 * 
	 * @param <T>  the body type
	 * @param type a {@link Class} representing {@code T}
	 * @return the body
	 * @throws NullPointerException  if the type is null
	 * @throws IllegalStateException if the stream is not available
	 */
	public <T> T getBody(Class<T> type) {
		return getBody(type, contentType);
	}

	/**
	 * <p>
	 * Obtains the body of this response as an object of the hinted type,
	 * considering the content type sent by the server.
	 * </p>
	 * <p>
	 * Call this method if the type is generic.
	 * </p>
	 * 
	 * @param <T>  the body type
	 * @param hint a {@link Hint} representing {@code T}
	 * @return the body
	 * @throws NullPointerException  if the hint is null
	 * @throws IllegalStateException if the stream is not available
	 */
	public <T> T getBody(Hint<T> hint) {
		return getBody(hint, contentType);
	}

	/**
	 * <p>
	 * Obtains the body of this response as an object of the specified type,
	 * considering the specified content type, with parameters if they are present.
	 * The content type sent by the server is ignored.
	 * </p>
	 * <p>
	 * Since {@link Class} objects lose generic information due to type erasure, do
	 * not call this method if the type is generic. Call
	 * {@code getBody(Hint<T>, String)} instead.
	 * </p>
	 * 
	 * @param <T>         the body type
	 * @param type        a {@link Class} representing {@code T}
	 * @param contentType the content type
	 * @return the body
	 * @throws NullPointerException  if the type is null
	 * @throws IllegalStateException if the stream is not available
	 */
	public <T> T getBody(Class<T> type, String contentType) {
		if (type == null) {
			throw new NullPointerException("Type cannot be null");
		}
		return getBody((Type) type, contentType);
	}

	/**
	 * <p>
	 * Obtains the body of this response as an object of the hinted type,
	 * considering the specified content type, with parameters if they are present.
	 * The content type sent by the server is ignored.
	 * </p>
	 * <p>
	 * Call this method if the type is generic.
	 * </p>
	 * 
	 * @param <T>         the body type
	 * @param hint        a {@link Hint} representing {@code T}
	 * @param contentType the content type
	 * @return the body
	 * @throws NullPointerException  if the hint is null
	 * @throws IllegalStateException if the stream is not available
	 */
	public <T> T getBody(Hint<T> hint, String contentType) {
		if (hint == null) {
			throw new NullPointerException("Hint cannot be null");
		}
		return getBody(hint.getType(), contentType);
	}

	private <T> T getBody(Type type, String contentType) {
		T body;
		InputStream stream = decode(contentType);
		if (manager.isBinary(type)) {
			contentType = manager.getDisassemblerType(strip(contentType), type);
			Disassembler disassembler = manager.getDisassembler(contentType);
			body = disassembler.read(stream, type);
		} else {
			Reader reader = coder.reader(stream, contentType);
			contentType = manager.getDeserializerType(strip(contentType), type);
			Deserializer deserializer = manager.getDeserializer(contentType);
			body = deserializer.read(reader, type);
		}
		return body;
	}

	private InputStream decode(String contentType) {
		synchronized (this) {
			if (!available) {
				throw new IllegalStateException("Stream is not available");
			}
			available = false;
		}
		return coder.decode(stream, contentType);
	}

	private String strip(String contentType) {
		if (contentType == null) {
			return null;
		}
		return coder.strip(contentType);
	}
}
