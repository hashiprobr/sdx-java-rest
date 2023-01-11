package br.pro.hashi.sdx.rest.client;

import java.nio.charset.Charset;

import org.eclipse.jetty.client.HttpClient;

import br.pro.hashi.sdx.rest.Facade;

/**
 * Stub.
 */
public class RESTClient {
	private final Facade facade;
	private final HttpClient jettyClient;
	private final Charset urlCharset;
	private final String none;
	private final String urlPrefix;

	RESTClient(Facade facade, HttpClient jettyClient, Charset urlCharset, String none, String urlPrefix) {
		this.facade = facade;
		this.jettyClient = jettyClient;
		this.urlCharset = urlCharset;
		this.none = none;
		this.urlPrefix = urlPrefix;
	}

	Facade getFacade() {
		return facade;
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
