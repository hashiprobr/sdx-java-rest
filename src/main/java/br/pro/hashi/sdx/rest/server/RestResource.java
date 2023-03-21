package br.pro.hashi.sdx.rest.server;

import java.nio.charset.Charset;
import java.util.List;
import java.util.Map;

import br.pro.hashi.sdx.rest.Fields;
import br.pro.hashi.sdx.rest.coding.Coding;

/**
 * Stub.
 */
public abstract class RestResource {
	private final String base;
	private final boolean nullBase;
	private int status;
	private boolean nullBody;
	private String contentType;
	private Charset charset;
	private boolean base64;

	/**
	 * Stub.
	 */
	protected Map<String, List<Fields>> partHeaders;

	/**
	 * Stub.
	 */
	protected Fields headers;

	/**
	 * Stub.
	 */
	protected Fields queries;

	/**
	 * Stub.
	 */
	protected RestResource() {
		this.base = null;
		this.nullBase = true;
		init();
	}

	/**
	 * Stub.
	 * 
	 * @param base stub
	 */
	protected RestResource(String base) {
		this.base = base;
		this.nullBase = false;
		init();
	}

	private void init() {
		this.status = -1;
		this.nullBody = false;
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

	boolean isNullBody() {
		return nullBody;
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
}
