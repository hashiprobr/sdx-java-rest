/**
 * Defines a simple REST framework based on Jetty.
 */
module br.pro.hashi.sdx.rest {
	requires transitive org.eclipse.jetty.client;

	requires org.eclipse.jetty.http2.http.client.transport;
	requires org.eclipse.jetty.http3.http.client.transport;
	requires org.slf4j;
	requires org.reflections;

	exports br.pro.hashi.sdx.rest.client;
	exports br.pro.hashi.sdx.rest.server;
	exports br.pro.hashi.sdx.rest.transform;
	exports br.pro.hashi.sdx.rest.transform.exception;
	exports br.pro.hashi.sdx.rest.transform.extension;
	exports br.pro.hashi.sdx.rest.transform.simple;
}