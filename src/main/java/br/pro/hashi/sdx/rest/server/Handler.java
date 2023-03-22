package br.pro.hashi.sdx.rest.server;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.UncheckedIOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Type;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

import org.eclipse.jetty.http.HttpStatus;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.handler.AbstractHandler;

import br.pro.hashi.sdx.rest.Fields;
import br.pro.hashi.sdx.rest.coding.Media;
import br.pro.hashi.sdx.rest.coding.Percent;
import br.pro.hashi.sdx.rest.reflection.Cache;
import br.pro.hashi.sdx.rest.reflection.Headers;
import br.pro.hashi.sdx.rest.reflection.Queries;
import br.pro.hashi.sdx.rest.reflection.Reflection;
import br.pro.hashi.sdx.rest.server.exception.BadRequestException;
import br.pro.hashi.sdx.rest.server.exception.MessageResponseException;
import br.pro.hashi.sdx.rest.server.exception.NotFoundException;
import br.pro.hashi.sdx.rest.server.exception.ResponseException;
import br.pro.hashi.sdx.rest.server.tree.Data;
import br.pro.hashi.sdx.rest.server.tree.Endpoint;
import br.pro.hashi.sdx.rest.server.tree.Node;
import br.pro.hashi.sdx.rest.server.tree.Tree;
import br.pro.hashi.sdx.rest.transform.Assembler;
import br.pro.hashi.sdx.rest.transform.Serializer;
import br.pro.hashi.sdx.rest.transform.facade.Facade;
import jakarta.servlet.MultipartConfigElement;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.Part;

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

	Handler(Cache cache, Facade facade, Tree tree, ErrorFormatter formatter, Map<Class<? extends RestResource>, Constructor<? extends RestResource>> constructors, MultipartConfigElement element, Set<Class<? extends RuntimeException>> gatewayTypes, Charset urlCharset, boolean cors) {
		this.cache = cache;
		this.facade = facade;
		this.tree = tree;
		this.formatter = formatter;
		this.constructors = constructors;
		this.element = element;
		this.gatewayTypes = gatewayTypes;
		this.urlCharset = urlCharset;
		this.cors = cors;
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

	@Override
	public void handle(String target, Request baseRequest, HttpServletRequest request, HttpServletResponse response) {
		try {
			if (!cors) {
				response.addHeader("Access-Control-Allow-Origin", "*");
				response.addHeader("Access-Control-Allow-Methods", "*");
				response.addHeader("Access-Control-Allow-Headers", "*");
				response.addHeader("Access-Control-Allow-Credentials", "true");
			}

			String uri = request.getRequestURI();
			String[] items;
			try {
				items = Percent.splitAndDecode(uri, urlCharset);
			} catch (IllegalArgumentException exception) {
				exception.printStackTrace();
				throw new BadRequestException("URI could not be decoded");
			}
			List<String> itemList = new ArrayList<>();
			Node node = tree.getNodeAndAddItems(items, itemList);
			Set<String> methodNames = node.getMethodNames();
			if (methodNames.isEmpty()) {
				throw new NotFoundException();
			}

			String methodName = request.getMethod();
			Endpoint endpoint = node.getEndpoint(methodName);
			if (endpoint == null) {
				if (!methodName.equals("OPTIONS")) {
					throw new MessageResponseException(HttpStatus.METHOD_NOT_ALLOWED_405, "%s not allowed".formatted(methodName));
				}
				response.addHeader("Allow", String.join(", ", methodNames));
				response.setStatus(HttpServletResponse.SC_OK);
				return;
			}

			String requestType = request.getContentType();
			Map<String, List<Fields>> partHeadersMap = new HashMap<>();
			Map<String, List<Data>> partMap = new HashMap<>();
			Data requestBody;
			if (requestType != null && requestType.startsWith("multipart/form-data")) {
				request.setAttribute(Request.__MULTIPART_CONFIG_ELEMENT, element);
				Collection<Part> parts;
				try {
					parts = request.getParts();
				} catch (ServletException exception) {
					exception.printStackTrace();
					throw new BadRequestException("Parts could not be parsed");
				}
				for (Part part : parts) {
					String name = part.getName();
					if (name == null) {
						name = "";
					}
					List<Fields> partHeadersList = partHeadersMap.get(name);
					if (partHeadersList == null) {
						partHeadersList = new ArrayList<>();
						partHeadersMap.put(name, partHeadersList);
					}
					List<Data> partList = partMap.get(name);
					if (partList == null) {
						partList = new ArrayList<>();
						partMap.put(name, partList);
					}
					partHeadersList.add(new Headers(cache, null));
					partList.add(new Data(facade, part.getContentType(), part.getInputStream()));
				}
				requestBody = null;
			} else {
				requestBody = new Data(facade, requestType, request.getInputStream());
			}

			Fields headers = new Headers(cache, baseRequest.getHttpFields());
			Fields queries = new Queries(cache, request.getParameterMap());

			Class<? extends RestResource> resourceType = endpoint.getResourceType();
			Constructor<? extends RestResource> constructor = constructors.get(resourceType);
			RestResource resource = Reflection.newNoArgsInstance(constructor);
			resource.setFields(partHeadersMap, headers, queries);

			Object responseBody;
			Type returnType;
			int status;
			try {
				responseBody = endpoint.call(resource, itemList, partMap, requestBody);
				returnType = endpoint.getReturnType();
				status = resource.getStatus();
			} catch (ResponseException exception) {
				responseBody = exception.getBody();
				status = exception.getStatus();
				if (responseBody instanceof String) {
					responseBody = formatter.format(status, (String) responseBody);
					returnType = formatter.getReturnType();
				} else {
					returnType = exception.getType();
				}
			}

			boolean hasContent = returnType.equals(void.class) || returnType.equals(Void.class) || (responseBody == null && !resource.isNullBody());
			if (status == -1) {
				if (hasContent) {
					response.setStatus(HttpStatus.OK_200);
				} else {
					response.setStatus(HttpStatus.NO_CONTENT_204);
				}
			} else {
				response.setStatus(status);
			}

			OutputStream responseStream = response.getOutputStream();
			if (methodName.equals("HEAD")) {
				CountOutputStream countStream = new CountOutputStream();
				if (hasContent) {
					if (write(response, resource, responseBody, returnType, countStream)) {
						response.setContentLengthLong(countStream.getCount());
					}
				}
				responseStream.close();
			} else {
				write(response, resource, responseBody, returnType, responseStream);
			}
		} catch (MessageResponseException exception) {
			int status = exception.getStatus();
			String message = exception.getBody();
			sendError(response, status, message);
		} catch (Exception exception) {
			exception.printStackTrace();
			boolean gateway = false;
			for (Class<? extends RuntimeException> type : gatewayTypes) {
				if (type.isAssignableFrom(exception.getClass())) {
					gateway = true;
					break;
				}
			}
			int status;
			if (gateway) {
				status = HttpServletResponse.SC_BAD_GATEWAY;
			} else {
				status = HttpServletResponse.SC_INTERNAL_SERVER_ERROR;
			}
			try {
				sendError(response, status);
			} catch (RuntimeException error) {
				error.printStackTrace();
			}
		} finally {
			baseRequest.setHandled(true);
		}
	}

	boolean write(HttpServletResponse response, RestResource resource, Object actual, Type type, OutputStream stream) {
		String contentType = resource.getContentType();
		boolean base64 = resource.isBase64();

		boolean writing;
		boolean consuming = false;
		try {
			Consumer<OutputStream> consumer;
			if (facade.isBinary(type)) {
				contentType = facade.cleanForAssembling(contentType, actual);
				Assembler assembler = facade.getAssembler(contentType);
				consumer = (output) -> {
					assembler.write(actual, type, output);
				};
				writing = actual instanceof InputStream;
			} else {
				contentType = facade.cleanForSerializing(contentType, actual);
				Serializer serializer = facade.getSerializer(contentType);
				Charset charset = resource.getCharset();
				consumer = (output) -> {
					OutputStreamWriter writer = new OutputStreamWriter(output, charset);
					serializer.write(actual, type, writer);
				};
				writing = actual instanceof Reader;
				contentType = "%s;charset=%s".formatted(contentType, charset.name());
			}
			if (base64) {
				contentType = "%s;base64".formatted(contentType);
			}

			response.setContentType(contentType);
			if (writing) {
				try {
					response.flushBuffer();
				} catch (IOException exception) {
					throw new UncheckedIOException(exception);
				}
			}
			if (base64) {
				stream = Media.encode(stream);
			}

			consuming = true;
			consumer.accept(stream);
		} finally {
			if (!consuming) {
				try {
					stream.close();
				} catch (IOException exception) {
					throw new UncheckedIOException(exception);
				}
			}
		}
		return !writing;
	}

	private void sendError(HttpServletResponse response, int status) {
		sendError(response, status, null);
	}

	private void sendError(HttpServletResponse response, int status, String message) {
		try {
			response.sendError(status, message);
		} catch (IOException exception) {
			throw new UncheckedIOException(exception);
		}
	}
}
