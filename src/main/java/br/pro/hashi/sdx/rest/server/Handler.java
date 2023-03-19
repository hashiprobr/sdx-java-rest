package br.pro.hashi.sdx.rest.server;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.nio.charset.Charset;
import java.util.Map;
import java.util.Set;

import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.handler.AbstractHandler;

import br.pro.hashi.sdx.rest.reflection.Cache;
import br.pro.hashi.sdx.rest.server.tree.Tree;
import br.pro.hashi.sdx.rest.transform.facade.Facade;
import jakarta.servlet.MultipartConfigElement;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

class Handler extends AbstractHandler {
	private final Cache cache;
	private final Facade facade;
	private final Tree tree;
	private final ErrorFormatter formatter;
	private final Map<Class<? extends RestResource>, Constructor<? extends RestResource>> constructors;
	private final MultipartConfigElement element;
	private final Set<Class<? extends RuntimeException>> gatewayTypes;
	private final Charset urlCharset;
	private final boolean cors;
	private final boolean logging;

	Handler(Cache cache, Facade facade, Tree tree, ErrorFormatter formatter, Map<Class<? extends RestResource>, Constructor<? extends RestResource>> constructors, MultipartConfigElement element, Set<Class<? extends RuntimeException>> gatewayTypes, Charset urlCharset, boolean cors, boolean logging) {
		this.cache = cache;
		this.facade = facade;
		this.tree = tree;
		this.formatter = formatter;
		this.constructors = constructors;
		this.element = element;
		this.gatewayTypes = gatewayTypes;
		this.urlCharset = urlCharset;
		this.cors = cors;
		this.logging = logging;
	}

	Cache getCache() {
		return cache;
	}

	Facade getFacade() {
		return facade;
	}

	Tree getTree() {
		return tree;
	}

	ErrorFormatter getFormatter() {
		return formatter;
	}

	MultipartConfigElement getElement() {
		return element;
	}

	Set<Class<? extends RuntimeException>> getGatewayTypes() {
		return gatewayTypes;
	}

	Charset getUrlCharset() {
		return urlCharset;
	}

	boolean isCors() {
		return cors;
	}

	boolean isLogging() {
		return logging;
	}

	@Override
	public void handle(String target, Request baseRequest, HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
	}
}
