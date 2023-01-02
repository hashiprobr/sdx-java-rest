/**
 * Defines a simple REST framework based on Jetty.
 */
module br.pro.hashi.sdx.rest {
	requires transitive org.eclipse.jetty.client;
	requires transitive com.google.gson;

	requires org.eclipse.jetty.http2.client;
	requires org.eclipse.jetty.http2.http.client.transport;
	requires org.eclipse.jetty.http3.client;
	requires org.eclipse.jetty.http3.http.client.transport;
	requires org.slf4j;
	requires org.reflections;

	exports br.pro.hashi.sdx.rest.client;
	exports br.pro.hashi.sdx.rest.server;
	exports br.pro.hashi.sdx.rest.transformer.base;
	exports br.pro.hashi.sdx.rest.transformer.exception;
	exports br.pro.hashi.sdx.rest.converter;
}