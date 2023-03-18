package br.pro.hashi.sdx.rest.server;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.util.Map;

import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.handler.AbstractHandler;

import br.pro.hashi.sdx.rest.server.tree.Tree;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

class RestHandler extends AbstractHandler {
	private final Map<Class<? extends RestResource>, Constructor<? extends RestResource>> constructors;
	private final Tree tree;

	RestHandler(Map<Class<? extends RestResource>, Constructor<? extends RestResource>> constructors, Tree tree) {
		this.constructors = constructors;
		this.tree = tree;
	}

	Tree getTree() {
		return tree;
	}

	@Override
	public void handle(String target, Request baseRequest, HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
	}
}
