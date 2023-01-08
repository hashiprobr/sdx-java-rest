package br.pro.hashi.sdx.rest.client;

import java.nio.charset.Charset;

import org.eclipse.jetty.client.HttpClient;

import br.pro.hashi.sdx.rest.transformer.Transformer;

/**
 * Stub.
 */
public class RESTClient {
	private final Transformer transformer;
	private final HttpClient jettyClient;
	private final Charset urlCharset;
	private final String none;
	private final String urlPrefix;

	RESTClient(Transformer transformer, HttpClient jettyClient, Charset urlCharset, String none, String urlPrefix) {
		this.transformer = transformer;
		this.jettyClient = jettyClient;
		this.urlCharset = urlCharset;
		this.none = none;
		this.urlPrefix = urlPrefix;
	}

	Transformer getTransformer() {
		return transformer;
	}

	Charset getURLCharset() {
		return urlCharset;
	}

	String getNone() {
		return none;
	}

	String getURLPrefix() {
		return urlPrefix;
	}

	/**
	 * Stub.
	 * 
	 * @return stub.
	 */
	public HttpClient getJettyClient() {
		return jettyClient;
	}
}