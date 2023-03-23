package br.pro.hashi.sdx.rest.server;

import java.nio.charset.Charset;
import java.util.List;
import java.util.Map;

import br.pro.hashi.sdx.rest.Fields;
import br.pro.hashi.sdx.rest.coding.Coding;

/**
 * Base class for representing REST resources.
 */
public abstract class RestResource {
	private final String base;
	private final boolean nullBase;
	private int status;
	private boolean nullable;
	private String contentType;
	private Charset charset;
	private boolean base64;

	/**
	 * Maps each part name to its headers. If the request is not multipart, this map
	 * is empty.
	 */
	protected Map<String, List<Fields>> partHeaders;

	/**
	 * The request headers.
	 */
	protected Fields headers;

	/**
	 * The query parameters.
	 */
	protected Fields queries;

	/**
	 * Constructs a new resource with a default base.
	 */
	protected RestResource() {
		this.base = null;
		this.nullBase = true;
		init();
	}

	/**
	 * Constructs a new resource with a specified base.
	 * 
	 * @param base the base
	 */
	protected RestResource(String base) {
		this.base = base;
		this.nullBase = false;
		init();
	}

	private void init() {
		this.status = -1;
		this.nullable = false;
		this.contentType = null;
		this.charset = Coding.CHARSET;
		this.base64 = false;
	}

	String getBase() {
		return base;
	}

	boolean isNullBase() {
		return nullBase;
	}

	int getStatus() {
		return status;
	}

	boolean isNullable() {
		return nullable;
	}

	String getContentType() {
		return contentType;
	}

	Charset getCharset() {
		return charset;
	}

	boolean isBase64() {
		return base64;
	}

	void setFields(Map<String, List<Fields>> partHeaders, Fields headers, Fields queries) {
		this.partHeaders = partHeaders;
		this.headers = headers;
		this.queries = queries;
	}

	/**
	 * Wrap the response body with this method to indicate that it should be
	 * serialized even if it is null.
	 * 
	 * @param <T>  the type of the response body
	 * @param body the response body
	 * @return the parameter, for wrapping
	 */
	protected <T> T nullable(T body) {
		nullable = true;
		return body;
	}
}
