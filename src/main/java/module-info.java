/**
 * Defines a simple REST framework based on Jetty.
 */
module br.pro.hashi.sdx.rest {
	requires transitive com.google.gson;

	requires org.slf4j;
	requires org.reflections;

	exports br.pro.hashi.sdx.rest.converter;
	exports br.pro.hashi.sdx.rest.transformer.base;
	exports br.pro.hashi.sdx.rest.transformer.exception;
	exports br.pro.hashi.sdx.rest.client;
	exports br.pro.hashi.sdx.rest.server;
}