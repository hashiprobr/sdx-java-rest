/**
 * Defines a simple REST framework based on Jetty.
 */
module br.pro.hashi.sdx.rest {
	requires org.reflections;

	requires transitive com.google.gson;

	exports br.pro.hashi.sdx.rest.converter;
	exports br.pro.hashi.sdx.rest.transformer.base;
	exports br.pro.hashi.sdx.rest.transformer.exception;
	exports br.pro.hashi.sdx.rest.client;
	exports br.pro.hashi.sdx.rest.server;
}