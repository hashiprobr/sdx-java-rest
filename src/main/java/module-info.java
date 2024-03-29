/**
 * Defines a simple embedded REST framework based on Jetty.
 */
module br.pro.hashi.sdx.rest {
	requires transitive org.eclipse.jetty.client;
	requires transitive org.eclipse.jetty.server;

	requires org.eclipse.jetty.http2.http.client.transport;
	requires org.eclipse.jetty.http3.http.client.transport;
	requires org.eclipse.jetty.alpn.server;
	requires org.eclipse.jetty.http2.server;
	requires org.eclipse.jetty.http3.server;
	requires org.reflections;
	requires com.github.alexdlaird.ngrok;

	exports br.pro.hashi.sdx.rest;
	exports br.pro.hashi.sdx.rest.client;
	exports br.pro.hashi.sdx.rest.server;
	exports br.pro.hashi.sdx.rest.server.annotation;
	exports br.pro.hashi.sdx.rest.transform;
	exports br.pro.hashi.sdx.rest.transform.exception;
	exports br.pro.hashi.sdx.rest.transform.extension;
}