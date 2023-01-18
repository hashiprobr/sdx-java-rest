package br.pro.hashi.sdx.rest.client;

import java.nio.charset.Charset;

import org.eclipse.jetty.client.HttpClient;

import br.pro.hashi.sdx.rest.transform.facade.Facade;

/**
 * Stub.
 */
public final class RestClient {
	/**
	 * Instantiates a REST client using a specified URL prefix.
	 * 
	 * @param urlPrefix the URL prefix
	 * @return the client
	 */
	public static RestClient to(String urlPrefix) {
		return builder().build(urlPrefix);
	}

	/**
	 * Convenience method for instantiating a REST client builder.
	 * 
	 * @return the client builder
	 */
	public static RestClientBuilder builder() {
		return new RestClientBuilder();
	}

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
