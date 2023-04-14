package br.pro.hashi.sdx.rest.server;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.UncheckedIOException;
import java.io.Writer;
import java.lang.invoke.MethodHandle;
import java.lang.reflect.Type;
import java.nio.charset.Charset;
import java.nio.charset.CharsetEncoder;
import java.nio.charset.StandardCharsets;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import br.pro.hashi.sdx.rest.Fields;
import br.pro.hashi.sdx.rest.coding.Media;
import br.pro.hashi.sdx.rest.coding.Percent;
import br.pro.hashi.sdx.rest.reflection.Cache;
import br.pro.hashi.sdx.rest.reflection.Headers;
import br.pro.hashi.sdx.rest.reflection.PartHeaders;
import br.pro.hashi.sdx.rest.reflection.Queries;
import br.pro.hashi.sdx.rest.server.exception.BadRequestException;
import br.pro.hashi.sdx.rest.server.exception.MessageRestException;
import br.pro.hashi.sdx.rest.server.exception.NotAcceptableException;
import br.pro.hashi.sdx.rest.server.exception.PayloadTooLargeException;
import br.pro.hashi.sdx.rest.server.stream.CountOutputStream;
import br.pro.hashi.sdx.rest.server.tree.Data;
import br.pro.hashi.sdx.rest.server.tree.Endpoint;
import br.pro.hashi.sdx.rest.server.tree.Node;
import br.pro.hashi.sdx.rest.server.tree.Tree;
import br.pro.hashi.sdx.rest.server.tree.Tree.Leaf;
import br.pro.hashi.sdx.rest.transform.Assembler;
import br.pro.hashi.sdx.rest.transform.Hint;
import br.pro.hashi.sdx.rest.transform.Serializer;
import br.pro.hashi.sdx.rest.transform.exception.UnsupportedException;
import br.pro.hashi.sdx.rest.transform.facade.Facade;
import jakarta.servlet.MultipartConfigElement;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.Part;

class Handler extends AbstractHandler {
	private final Type streamConsumerType;
	private final Type writerConsumerType;
	private final Logger logger;
	private final Cache cache;
	private final Facade facade;
	private final Tree tree;
	private final ErrorFormatter formatter;
	private final Map<Class<? extends RestResource>, MethodHandle> handles;
	private final MultipartConfigElement element;
	private final Set<Class<? extends RuntimeException>> gatewayTypes;
	private final Charset urlCharset;
	private final boolean cors;

	Handler(Cache cache, Facade facade, Tree tree, ErrorFormatter formatter, Map<Class<? extends RestResource>, MethodHandle> handles, MultipartConfigElement element, Set<Class<? extends RuntimeException>> gatewayTypes, Charset urlCharset, boolean cors) {
		this.streamConsumerType = new Hint<Consumer<OutputStream>>() {}.getType();
		this.writerConsumerType = new Hint<Consumer<Writer>>() {}.getType();
		this.logger = LoggerFactory.getLogger(Handler.class);
		this.cache = cache;
		this.facade = facade;
		this.tree = tree;
		this.formatter = formatter;
		this.handles = handles;
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

			String methodName = request.getMethod();
			if (!tree.getMethodNames().contains(methodName)) {
				throw new MessageRestException(HttpStatus.NOT_IMPLEMENTED_501, "%s not implemented".formatted(methodName));
			}

			String uri = request.getRequestURI();
			String extension;
			String extensionType;
			int length = uri.lastIndexOf('.') + 1;
			if (length > 0 && length < uri.length() && uri.indexOf('/', length) == -1) {
				extension = uri.substring(length);
				extensionType = facade.getExtensionType(extension);
				if (extensionType == null) {
					extension = "";
				} else {
					uri = uri.substring(0, length - 1);
				}
			} else {
				uri = Percent.stripEndingSlashes(uri);
				extension = "";
				extensionType = null;
			}

			String[] items;
			try {
				items = Percent.splitAndDecode(uri, urlCharset);
			} catch (IllegalArgumentException error) {
				String message = "URI could not be decoded";
				logger.error(message, error);
				throw new BadRequestException(message);
			}

			List<String> itemList = new ArrayList<>();
			Leaf leaf = tree.getLeafAndAddItems(items, itemList);
			Node node = leaf.node();
			int varSize = leaf.varSize();

			OutputStream responseStream = response.getOutputStream();
			if (methodName.equals("OPTIONS")) {
				Set<String> methodNames;
				if (varSize == 0) {
					methodNames = node.getMethodNames();
				} else {
					methodNames = node.getVarMethodNames();
				}
				response.addHeader("Allow", String.join(", ", methodNames));
				response.setStatus(HttpServletResponse.SC_OK);
				responseStream.close();
				return;
			}

			Endpoint endpoint = node.getEndpoint(methodName, varSize);
			if (endpoint == null) {
				throw new MessageRestException(HttpStatus.METHOD_NOT_ALLOWED_405, "%s not allowed".formatted(methodName));
			}

			String requestType = request.getContentType();
			Map<String, List<Fields>> headersMap = new HashMap<>();
			Map<String, List<Data>> partMap = new HashMap<>();
			Data requestBody;
			if (requestType != null && requestType.startsWith("multipart/form-data")) {
				request.setAttribute(Request.__MULTIPART_CONFIG_ELEMENT, element);
				Collection<Part> parts;
				try {
					parts = request.getParts();
				} catch (ServletException | IOException exception) {
					String message = "Parts could not be parsed";
					logger.error(message, exception);
					throw new BadRequestException(message);
				} catch (IllegalStateException exception) {
					long maxRequestSize = element.getMaxRequestSize();
					long maxFileSize = element.getMaxFileSize();
					String message;
					if (maxRequestSize > 0) {
						if (maxFileSize > 0) {
							message = "Multipart request exceeds %d bytes or one of the parts exceeds %d bytes".formatted(maxRequestSize, maxFileSize);
						} else {
							message = "Multipart request exceeds %d bytes".formatted(maxRequestSize);
						}
					} else {
						if (maxFileSize > 0) {
							message = "One of the parts exceeds %d bytes".formatted(maxFileSize);
						} else {
							message = "Multipart request is too large or one of the parts is too large";
						}
					}
					logger.error(message, exception);
					throw new PayloadTooLargeException(message);
				}
				for (Part part : parts) {
					String name = part.getName();
					if (name == null) {
						name = "";
					}
					List<Fields> headersList = headersMap.get(name);
					if (headersList == null) {
						headersList = new ArrayList<>();
						headersMap.put(name, headersList);
					}
					List<Data> partList = partMap.get(name);
					if (partList == null) {
						partList = new ArrayList<>();
						partMap.put(name, partList);
					}
					headersList.add(new PartHeaders(cache, part));
					partList.add(new Data(facade, part.getContentType(), part.getInputStream()));
				}
				requestBody = null;
			} else {
				requestBody = new Data(facade, requestType, request.getInputStream());
			}

			Fields headers = new Headers(cache, baseRequest.getHttpFields());
			Fields queries = new Queries(cache, request.getParameterMap());
			CharsetEncoder encoder = StandardCharsets.US_ASCII.newEncoder();

			Class<? extends RestResource> resourceType = endpoint.getResourceType();
			MethodHandle handle = handles.get(resourceType);
			RestResource resource = invoke(handle);

			Set<String> notAcceptableExtensions = resource.notAcceptableExtensions();
			if (notAcceptableExtensions != null && notAcceptableExtensions.contains(extension)) {
				String message;
				if (extension.equals("")) {
					message = "URI must have an extension";
				} else {
					message = "Extension %s is not acceptable".formatted(extension);
				}
				throw new NotAcceptableException(message);
			}
			resource.setFields(headersMap, headers, queries, encoder, response);

			Object responseBody;
			Type returnType;
			int status;
			try {
				responseBody = endpoint.call(resource, itemList, partMap, requestBody);
				returnType = endpoint.getReturnType();
				status = resource.getStatus();
			} catch (RestException exception) {
				responseBody = exception.getBody();
				status = exception.getStatus();
				if (responseBody instanceof String) {
					responseBody = formatter.format(status, (String) responseBody);
					returnType = formatter.getReturnType();
				} else {
					returnType = exception.getType();
				}
			}

			boolean withContent = !(returnType.equals(void.class) || returnType.equals(Void.class) || (responseBody == null && !resource.isNullable()));
			if (status == -1) {
				if (withContent) {
					if (methodName.equals("POST")) {
						response.setStatus(HttpStatus.CREATED_201);
					} else {
						response.setStatus(HttpStatus.OK_200);
					}
				} else {
					response.setStatus(HttpStatus.NO_CONTENT_204);
				}
			} else {
				response.setStatus(status);
			}

			if (methodName.equals("HEAD")) {
				CountOutputStream countStream = new CountOutputStream();
				if (withContent) {
					if (write(response, resource, responseBody, returnType, extensionType, countStream)) {
						response.setContentLengthLong(countStream.getCount());
					}
				}
				responseStream.close();
			} else {
				if (withContent) {
					write(response, resource, responseBody, returnType, extensionType, responseStream);
				} else {
					responseStream.close();
				}
			}
		} catch (MessageRestException exception) {
			int status = exception.getStatus();
			String message = (String) exception.getBody();
			sendError(response, status, message);
		} catch (Exception exception) {
			response.reset();
			boolean gateway = false;
			for (Class<? extends RuntimeException> type : gatewayTypes) {
				if (type.isAssignableFrom(exception.getClass())) {
					gateway = true;
					break;
				}
			}
			int status;
			if (gateway) {
				logger.error("Bad gateway", exception);
				status = HttpServletResponse.SC_BAD_GATEWAY;
			} else {
				logger.error("Internal server error", exception);
				status = HttpServletResponse.SC_INTERNAL_SERVER_ERROR;
			}
			sendError(response, status, null);
		} finally {
			baseRequest.setHandled(true);
		}
	}

	RestResource invoke(MethodHandle handle) {
		RestResource resource;
		try {
			resource = (RestResource) handle.invoke();
		} catch (Throwable exception) {
			throw new AssertionError(exception);
		}
		return resource;
	}

	boolean write(HttpServletResponse response, RestResource resource, Object actual, Type type, String extensionType, OutputStream stream) {
		boolean withoutLength;
		try {
			String contentType;
			if (extensionType == null) {
				contentType = resource.getContentType();
			} else {
				contentType = extensionType;
			}
			boolean base64 = resource.isBase64();

			Consumer<OutputStream> consumer;
			if (facade.isBinary(type)) {
				contentType = facade.getAssemblerType(contentType, actual, type);
				Assembler assembler = facade.getAssembler(contentType);
				consumer = (output) -> {
					assembler.write(actual, type, output);
					try {
						output.close();
					} catch (IOException exception) {
						throw new UncheckedIOException(exception);
					}
				};
				withoutLength = actual instanceof InputStream || type.equals(streamConsumerType);
			} else {
				contentType = facade.getSerializerType(contentType, actual, type);
				Serializer serializer = facade.getSerializer(contentType);
				Charset charset = resource.getCharset();
				consumer = (output) -> {
					OutputStreamWriter writer = new OutputStreamWriter(output, charset);
					serializer.write(actual, type, writer);
					try {
						writer.close();
					} catch (IOException exception) {
						throw new UncheckedIOException(exception);
					}
				};
				withoutLength = actual instanceof Reader || type.equals(writerConsumerType);
				contentType = "%s;charset=%s".formatted(contentType, charset.name());
			}
			if (base64) {
				contentType = "%s;base64".formatted(contentType);
			}

			response.setContentType(contentType);
			if (withoutLength && stream instanceof ServletOutputStream) {
				try {
					response.flushBuffer();
				} catch (IOException exception) {
					throw new UncheckedIOException(exception);
				}
			}
			if (base64) {
				stream = Media.encode(stream);
			}

			try {
				consumer.accept(stream);
			} catch (UnsupportedException exception) {
				if (extensionType == null) {
					throw exception;
				}
				throw new NotAcceptableException("Cannot send response as %s".formatted(extensionType));
			}
		} catch (RuntimeException exception) {
			if (!response.isCommitted()) {
				throw exception;
			}
			withoutLength = true;
		}
		return !withoutLength;
	}

	void sendError(HttpServletResponse response, int status, String message) {
		try {
			response.sendError(status, message);
		} catch (Exception exception) {
			logger.warn("Could not send error message", exception);
		}
	}
}
