package br.pro.hashi.sdx.rest.client;

import java.nio.charset.Charset;

import org.eclipse.jetty.client.HttpClient;

import br.pro.hashi.sdx.rest.transform.facade.Facade;

/**
 * Stub.
 */
public class RestClient {
	private final Facade facade;
	private final HttpClient jettyClient;
	private final Charset urlCharset;
	private final String none;
	private final String urlPrefix;

	RestClient(Facade facade, HttpClient jettyClient, Charset urlCharset, String none, String urlPrefix) {
		this.facade = facade;
		this.jettyClient = jettyClient;
		this.urlCharset = urlCharset;
		this.none = none;
		this.urlPrefix = urlPrefix;
	}

	Facade getFacade() {
		return facade;
	}

	Charset getUrlCharset() {
		return urlCharset;
	}

	String getNone() {
		return none;
	}

	String getUrlPrefix() {
		return urlPrefix;
	}

	/**
	 * Stub.
	 * 
	 * @return stub
	 */
	public HttpClient getJettyClient() {
		return jettyClient;
	}
}
