/**
 * Defines a simple REST framework based on Jetty.
 */
module br.pro.hashi.sdx.rest {
	requires com.google.gson;
	requires org.reflections;

	exports br.pro.hashi.sdx.rest.transformer.base;
	exports br.pro.hashi.sdx.rest.transformer.exception;
}