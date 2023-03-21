package br.pro.hashi.sdx.rest.server;

import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.jetty.server.Request;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import br.pro.hashi.sdx.rest.reflection.Cache;
import br.pro.hashi.sdx.rest.server.exception.NotFoundException;
import br.pro.hashi.sdx.rest.server.mock.valid.ResourceWithMethods;
import br.pro.hashi.sdx.rest.server.tree.Endpoint;
import br.pro.hashi.sdx.rest.server.tree.Node;
import br.pro.hashi.sdx.rest.server.tree.Tree;
import br.pro.hashi.sdx.rest.transform.facade.Facade;
import jakarta.servlet.MultipartConfigElement;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.Part;

class HandlerTest {
	private Cache cache;
	private Facade facade;
	private Tree tree;
	private ErrorFormatter formatter;
	private Map<Class<? extends RestResource>, Constructor<? extends RestResource>> constructors;
	private MultipartConfigElement element;
	private Set<Class<? extends RuntimeException>> gatewayTypes;
	private Request baseRequest;
	private HttpServletRequest request;
	private HttpServletResponse response;
	private Node node;
	private Endpoint endpoint;
	private List<Part> parts;

	@BeforeEach
	void setUp() {
		cache = mock(Cache.class);
		facade = mock(Facade.class);
		tree = mock(Tree.class);
		formatter = mock(ErrorFormatter.class);
		constructors = new HashMap<>();
		Class<ResourceWithMethods> type = ResourceWithMethods.class;
		Constructor<ResourceWithMethods> constructor;
		try {
			constructor = type.getConstructor();
		} catch (NoSuchMethodException exception) {
			throw new AssertionError(exception);
		}
		constructors.put(type, constructor);
		element = mock(MultipartConfigElement.class);
		gatewayTypes = new HashSet<>();
		baseRequest = mock(Request.class);
		request = mock(HttpServletRequest.class);
		response = mock(HttpServletResponse.class);
		node = mock(Node.class);
		endpoint = mock(Endpoint.class);
		parts = new ArrayList<>();
	}

	@Test
	void handles() {
		mockRequestUri();
		mockNode();
		mockMethodNames();
		mockMethod();
		mockEndpoint();
		mockContentType();
		mockResourceType();
		handle();
	}

	@Test
	void handlesWithoutCors() {
		mockRequestUri();
		mockNode();
		mockMethodNames();
		mockMethod();
		mockEndpoint();
		mockContentType();
		mockResourceType();
		handle(false);
		verify(response).addHeader("Access-Control-Allow-Origin", "*");
		verify(response).addHeader("Access-Control-Allow-Methods", "*");
		verify(response).addHeader("Access-Control-Allow-Headers", "*");
		verify(response).addHeader("Access-Control-Allow-Credentials", "true");
	}

	@Test
	void handlesWithoutRequestUri() {
		when(request.getRequestURI()).thenReturn("/%b");
		mockNode();
		mockMethodNames();
		mockMethod();
		mockEndpoint();
		mockContentType();
		mockResourceType();
		handle();
	}

	@Test
	void handlesWithoutNode() {
		mockRequestUri();
		when(tree.getNodeAndAddItems(new String[] { "b" }, List.of())).thenThrow(NotFoundException.class);
		mockMethodNames();
		mockMethod();
		mockEndpoint();
		mockContentType();
		mockResourceType();
		handle();
	}

	@Test
	void handlesWithoutMethodNames() {
		mockRequestUri();
		mockNode();
		when(node.getMethodNames()).thenReturn(Set.of());
		mockMethod();
		mockEndpoint();
		mockContentType();
		mockResourceType();
		handle();
	}

	@Test
	void handlesWithoutEndpoint() {
		mockRequestUri();
		mockNode();
		mockMethodNames();
		mockMethod();
		mockNullEndpoint();
		mockContentType();
		mockResourceType();
		handle();
	}

	@Test
	void handlesWithoutEndpointWithOptions() {
		mockRequestUri();
		mockNode();
		mockMethodNames();
		when(request.getMethod()).thenReturn("OPTIONS");
		mockNullEndpoint();
		mockContentType();
		mockResourceType();
		handle();
		verify(response).addHeader("Allow", "GET, POST");
	}

	@Test
	void handlesWithNullContentType() {
		mockRequestUri();
		mockNode();
		mockMethodNames();
		mockMethod();
		mockEndpoint();
		when(request.getContentType()).thenReturn(null);
		mockResourceType();
		handle();
	}

	@Test
	void handlesWithMultipartContentType() {
		mockRequestUri();
		mockNode();
		mockMethodNames();
		mockMethod();
		mockEndpoint();
		mockMultipartContentType();
		mockParts();
		mockResourceType();
		handle();
	}

	@Test
	void handlesWithOnePart() {
		mockRequestUri();
		mockNode();
		mockMethodNames();
		mockMethod();
		mockEndpoint();
		mockMultipartContentType();
		Part part = mock(Part.class);
		when(part.getName()).thenReturn(null);
		parts.add(part);
		mockParts();
		mockResourceType();
		handle();
	}

	@Test
	void handlesWithTwoParts() {
		mockRequestUri();
		mockNode();
		mockMethodNames();
		mockMethod();
		mockEndpoint();
		mockMultipartContentType();
		Part part0 = mock(Part.class);
		when(part0.getName()).thenReturn(null);
		parts.add(part0);
		Part part1 = mock(Part.class);
		when(part1.getName()).thenReturn(null);
		parts.add(part1);
		mockParts();
		mockResourceType();
		handle();
	}

	@Test
	void handlesWithOnePartAndOneNamedPart() {
		mockRequestUri();
		mockNode();
		mockMethodNames();
		mockMethod();
		mockEndpoint();
		mockMultipartContentType();
		Part part = mock(Part.class);
		when(part.getName()).thenReturn(null);
		parts.add(part);
		Part namedPart = mock(Part.class);
		when(namedPart.getName()).thenReturn("name");
		parts.add(namedPart);
		mockParts();
		mockResourceType();
		handle();
	}

	@Test
	void handlesWithOneNamedPart() {
		mockRequestUri();
		mockNode();
		mockMethodNames();
		mockMethod();
		mockEndpoint();
		mockMultipartContentType();
		Part namedPart = mock(Part.class);
		when(namedPart.getName()).thenReturn("name");
		parts.add(namedPart);
		mockParts();
		mockResourceType();
		handle();
	}

	@Test
	void handlesWithOneNamedPartAndOnePart() {
		mockRequestUri();
		mockNode();
		mockMethodNames();
		mockMethod();
		mockEndpoint();
		mockMultipartContentType();
		Part namedPart = mock(Part.class);
		when(namedPart.getName()).thenReturn("name");
		parts.add(namedPart);
		Part part = mock(Part.class);
		when(part.getName()).thenReturn(null);
		parts.add(part);
		mockParts();
		mockResourceType();
		handle();
	}

	@Test
	void handlesWithTwoNamedParts() {
		mockRequestUri();
		mockNode();
		mockMethodNames();
		mockMethod();
		mockEndpoint();
		mockMultipartContentType();
		Part namedPart0 = mock(Part.class);
		when(namedPart0.getName()).thenReturn("name0");
		parts.add(namedPart0);
		Part namedPart1 = mock(Part.class);
		when(namedPart1.getName()).thenReturn("name1");
		parts.add(namedPart1);
		mockParts();
		mockResourceType();
		handle();
	}

	private void mockParts() {
		try {
			when(request.getParts()).thenReturn(parts);
		} catch (IOException | ServletException exception) {
			throw new AssertionError(exception);
		}
	}

	@Test
	void handlesWithInvalidParts() {
		mockRequestUri();
		mockNode();
		mockMethodNames();
		mockMethod();
		mockEndpoint();
		mockMultipartContentType();
		try {
			when(request.getParts()).thenThrow(ServletException.class);
		} catch (IOException | ServletException exception) {
			throw new AssertionError(exception);
		}
		mockResourceType();
		handle();
	}

	private void mockMultipartContentType() {
		when(request.getContentType()).thenReturn("multipart/form-data");
	}

	private void mockRequestUri() {
		when(request.getRequestURI()).thenReturn("/b");
	}

	private void mockNullEndpoint() {
		when(node.getEndpoint("GET")).thenReturn(null);
	}

	private void mockNode() {
		when(tree.getNodeAndAddItems(new String[] { "b" }, List.of())).thenReturn(node);
	}

	private void mockMethodNames() {
		when(node.getMethodNames()).thenReturn(new LinkedHashSet<>(List.of("GET", "POST")));
	}

	private void mockMethod() {
		when(request.getMethod()).thenReturn("GET");
	}

	private void mockEndpoint() {
		when(node.getEndpoint("GET")).thenReturn(endpoint);
	}

	private void mockContentType() {
		when(request.getContentType()).thenReturn("type/subtype");
	}

	private void mockResourceType() {
		doReturn(ResourceWithMethods.class).when(endpoint).getResourceType();
	}

	private void handle() {
		handle(true);
		verify(response, times(0)).addHeader("Access-Control-Allow-Origin", "*");
		verify(response, times(0)).addHeader("Access-Control-Allow-Methods", "*");
		verify(response, times(0)).addHeader("Access-Control-Allow-Headers", "*");
		verify(response, times(0)).addHeader("Access-Control-Allow-Credentials", "true");
	}

	private void handle(boolean cors) {
		Handler h = new Handler(cache, facade, tree, formatter, constructors, element, gatewayTypes, StandardCharsets.UTF_8, cors);
		h.handle("target", baseRequest, request, response);
		verify(baseRequest).setHandled(true);
	}
}
