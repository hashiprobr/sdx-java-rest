package br.pro.hashi.sdx.rest.server;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.same;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.StringReader;
import java.io.UncheckedIOException;
import java.io.Writer;
import java.lang.reflect.Constructor;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import org.eclipse.jetty.server.Request;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.stubbing.Answer;

import br.pro.hashi.sdx.rest.reflection.Cache;
import br.pro.hashi.sdx.rest.server.exception.NotFoundException;
import br.pro.hashi.sdx.rest.server.exception.ResourceException;
import br.pro.hashi.sdx.rest.server.exception.ResponseException;
import br.pro.hashi.sdx.rest.server.mock.valid.NullableResource;
import br.pro.hashi.sdx.rest.server.mock.valid.Resource;
import br.pro.hashi.sdx.rest.server.tree.Endpoint;
import br.pro.hashi.sdx.rest.server.tree.Node;
import br.pro.hashi.sdx.rest.server.tree.Tree;
import br.pro.hashi.sdx.rest.transform.Assembler;
import br.pro.hashi.sdx.rest.transform.Serializer;
import br.pro.hashi.sdx.rest.transform.facade.Facade;
import jakarta.servlet.MultipartConfigElement;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.Part;

class HandlerTest {
	private static final String USASCII_BODY = "special";
	private static final String SPECIAL_BODY = "spéçíál";

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
	private RestResource resource;
	private ByteArrayOutputStream stream;

	@BeforeEach
	void setUp() throws NoSuchMethodException, IOException {
		cache = mock(Cache.class);
		facade = mock(Facade.class);
		tree = mock(Tree.class);
		formatter = mock(ErrorFormatter.class);
		constructors = new HashMap<>();
		constructors.put(Resource.class, Resource.class.getConstructor());
		constructors.put(NullableResource.class, NullableResource.class.getConstructor());
		element = mock(MultipartConfigElement.class);
		gatewayTypes = new HashSet<>();
		baseRequest = mock(Request.class);
		request = mock(HttpServletRequest.class);
		ServletOutputStream servletStream = mock(ServletOutputStream.class);
		response = mock(HttpServletResponse.class);
		when(response.getOutputStream()).thenReturn(servletStream);
		node = mock(Node.class);
		endpoint = mock(Endpoint.class);
		parts = new ArrayList<>();
		resource = mock(RestResource.class);
		stream = spy(new ByteArrayOutputStream());
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
		mockCall();
		mockReturnType();
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
		mockCall();
		mockReturnType();
		handle(false, mockAnswer());
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
		mockCall();
		mockReturnType();
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
		mockCall();
		mockReturnType();
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
		mockCall();
		mockReturnType();
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
		mockCall();
		mockReturnType();
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
		mockCall();
		mockReturnType();
		handle();
		verify(response).addHeader("Allow", "GET, POST");
	}

	private void mockNullEndpoint() {
		when(node.getEndpoint("GET")).thenReturn(null);
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
		mockCall();
		mockReturnType();
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
		mockCall();
		mockReturnType();
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
		mockCall();
		mockReturnType();
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
		mockCall();
		mockReturnType();
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
		mockCall();
		mockReturnType();
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
		mockCall();
		mockReturnType();
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
		mockCall();
		mockReturnType();
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
		mockCall();
		mockReturnType();
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
		} catch (ServletException | IOException exception) {
			throw new AssertionError(exception);
		}
		mockResourceType();
		mockCall();
		mockReturnType();
		handle();
	}

	private void mockMultipartContentType() {
		when(request.getContentType()).thenReturn("multipart/form-data");
	}

	@Test
	void handlesWithResponseException() {
		mockRequestUri();
		mockNode();
		mockMethodNames();
		mockMethod();
		mockEndpoint();
		mockContentType();
		mockResourceType();
		mockResponseException(new Object());
		mockReturnType();
		handle();
	}

	@Test
	void handlesWithMessageResponseException() {
		mockRequestUri();
		mockNode();
		mockMethodNames();
		mockMethod();
		mockEndpoint();
		mockContentType();
		mockResourceType();
		mockResponseException("message");
		mockReturnType();
		handle();
	}

	private void mockResponseException(Object body) {
		ResponseException exception = new ResponseException(400, body);
		when(endpoint.call(any(), eq(List.of()), eq(Map.of()), any())).thenThrow(exception);
	}

	@Test
	void handlesWithVoid() {
		mockRequestUri();
		mockNode();
		mockMethodNames();
		mockMethod();
		mockEndpoint();
		mockContentType();
		mockResourceType();
		mockCall();
		mockReturnType(void.class);
		handle();
	}

	@Test
	void handlesWithVoidWrapper() {
		mockRequestUri();
		mockNode();
		mockMethodNames();
		mockMethod();
		mockEndpoint();
		mockContentType();
		mockResourceType();
		mockCall();
		mockReturnType(Void.class);
		handle();
	}

	@Test
	void handlesWithoutNull() {
		mockRequestUri();
		mockNode();
		mockMethodNames();
		mockMethod();
		mockEndpoint();
		mockContentType();
		mockResourceType();
		mockNullCall();
		mockReturnType();
		handle();
	}

	@Test
	void handlesWithNull() {
		mockRequestUri();
		mockNode();
		mockMethodNames();
		mockMethod();
		mockEndpoint();
		mockContentType();
		doReturn(NullableResource.class).when(endpoint).getResourceType();
		mockNullCall();
		mockReturnType();
		handle();
	}

	private void mockNullCall() {
		when(endpoint.call(any(), eq(List.of()), eq(Map.of()), any())).thenReturn(null);
	}

	@Test
	void handlesWithHead() {
		mockRequestUri();
		mockNode();
		mockMethodNames();
		mockHead();
		mockContentType();
		mockResourceType();
		mockCall();
		mockReturnType();
		handle();
	}

	@Test
	void handlesWithHeadWithoutContent() {
		mockRequestUri();
		mockNode();
		mockMethodNames();
		mockHead();
		mockContentType();
		mockResourceType();
		mockNullCall();
		mockReturnType();
		handle();
	}

	@Test
	void handlesWithHeadWithoutWrite() {
		mockRequestUri();
		mockNode();
		mockMethodNames();
		mockHead();
		mockContentType();
		mockResourceType();
		mockCall();
		mockReturnType();
		handle((invocation) -> {
			return false;
		});
	}

	private void mockHead() {
		when(request.getMethod()).thenReturn("HEAD");
		when(node.getEndpoint("HEAD")).thenReturn(endpoint);
	}

	@Test
	void handlesWithGatewayException() {
		mockRequestUri();
		mockNode();
		mockMethodNames();
		mockMethod();
		mockEndpoint();
		mockContentType();
		mockResourceType();
		mockReturnType();
		mockException(ResourceException.class);
		handle();
	}

	@Test
	void handlesWithSingleException() {
		mockRequestUri();
		mockNode();
		mockMethodNames();
		mockMethod();
		mockEndpoint();
		mockContentType();
		mockResourceType();
		mockReturnType();
		mockException();
		handle();
	}

	@Test
	void handlesWithDoubleException() throws IOException {
		mockRequestUri();
		mockNode();
		mockMethodNames();
		mockMethod();
		mockEndpoint();
		mockContentType();
		mockResourceType();
		mockReturnType();
		mockException();
		doThrow(IOException.class).when(response).sendError(500, null);
		handle();
	}

	private void mockException() {
		mockException(RuntimeException.class);
	}

	private void mockException(Class<? extends RuntimeException> type) {
		gatewayTypes.add(ResourceException.class);
		when(endpoint.call(any(), eq(List.of()), eq(Map.of()), any())).thenThrow(type);
	}

	private void mockRequestUri() {
		when(request.getRequestURI()).thenReturn("/b");
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
		doReturn(Resource.class).when(endpoint).getResourceType();
	}

	private void mockCall() {
		when(endpoint.call(any(), eq(List.of()), eq(Map.of()), any())).thenReturn(new Object());
	}

	private void mockReturnType() {
		mockReturnType(Object.class);
	}

	private void mockReturnType(Type type) {
		when(endpoint.getReturnType()).thenReturn(type);
	}

	private void handle() {
		handle(mockAnswer());
	}

	private Answer<Boolean> mockAnswer() {
		return (invocation) -> {
			return true;
		};
	}

	private void handle(Answer<Boolean> answer) {
		handle(true, answer);
		verify(response, times(0)).addHeader("Access-Control-Allow-Origin", "*");
		verify(response, times(0)).addHeader("Access-Control-Allow-Methods", "*");
		verify(response, times(0)).addHeader("Access-Control-Allow-Headers", "*");
		verify(response, times(0)).addHeader("Access-Control-Allow-Credentials", "true");
	}

	private void handle(boolean cors, Answer<Boolean> answer) {
		Handler h = spy(new Handler(cache, facade, tree, formatter, constructors, element, gatewayTypes, StandardCharsets.UTF_8, cors));
		doAnswer(answer).when(h).write(same(response), any(), any(), any(), any());
		h.handle("target", baseRequest, request, response);
		verify(baseRequest).setHandled(true);
	}

	@Test
	void writes() {
		mockCharset();
		mockWithoutBase64();
		assertTrue(write());
		verify(response).setContentType("type/subtype;charset=UTF-8");
	}

	@Test
	void writesBase64() {
		mockCharset();
		mockWithBase64();
		assertTrue(write());
		verify(response).setContentType("type/subtype;charset=UTF-8;base64");
	}

	private boolean write() {
		return serialize(SPECIAL_BODY, String.class);
	}

	@Test
	void writesLarge() {
		mockCharset();
		mockWithoutBase64();
		assertFalse(writeLarge());
		verify(response).setContentType("type/subtype;charset=UTF-8");
	}

	@Test
	void writesLargeWithSingleException() {
		mockCharset();
		mockWithoutBase64();
		mockResponseException();
		assertThrows(UncheckedIOException.class, () -> {
			writeLarge();
		});
		verify(response).setContentType("type/subtype;charset=UTF-8");
	}

	@Test
	void writesLargeWithDoubleException() {
		mockCharset();
		mockWithoutBase64();
		mockResponseException();
		mockStreamException();
		assertThrows(UncheckedIOException.class, () -> {
			writeLarge();
		});
		verify(response).setContentType("type/subtype;charset=UTF-8");
	}

	private boolean writeLarge() {
		return serialize(new StringReader(SPECIAL_BODY), Reader.class);
	}

	private boolean serialize(Object actual, Type type) {
		Serializer serializer = mock(Serializer.class);
		doAnswer((invocation) -> {
			String str = Objects.toString(invocation.getArgument(0));
			Writer writer = invocation.getArgument(2);
			writer.write(str);
			writer.close();
			return null;
		}).when(serializer).write(eq(actual), any(), any());
		when(facade.isBinary(any())).thenReturn(false);
		when(facade.cleanForSerializing(null, actual)).thenReturn("type/subtype");
		when(facade.getSerializer("type/subtype")).thenReturn(serializer);
		return write(actual, type);
	}

	@Test
	void writesBinary() {
		mockCharset();
		mockWithoutBase64();
		assertTrue(writeBinary());
		verify(response).setContentType("type/subtype");
	}

	@Test
	void writesBinaryBase64() {
		mockCharset();
		mockWithBase64();
		assertTrue(writeBinary());
		verify(response).setContentType("type/subtype;base64");
	}

	private boolean writeBinary() {
		return assemble(USASCII_BODY, String.class);
	}

	@Test
	void writesLargeBinary() {
		mockCharset();
		mockWithoutBase64();
		assertFalse(writeLargeBinary());
		verify(response).setContentType("type/subtype");
	}

	@Test
	void writesLargeBinaryWithSingleException() {
		mockCharset();
		mockWithoutBase64();
		mockResponseException();
		assertThrows(UncheckedIOException.class, () -> {
			writeLargeBinary();
		});
		verify(response).setContentType("type/subtype");
	}

	@Test
	void writesLargeBinaryWithDoubleException() {
		mockCharset();
		mockWithoutBase64();
		mockResponseException();
		mockStreamException();
		assertThrows(UncheckedIOException.class, () -> {
			writeLargeBinary();
		});
		verify(response).setContentType("type/subtype");
	}

	private boolean writeLargeBinary() {
		return assemble(new ByteArrayInputStream(USASCII_BODY.getBytes(StandardCharsets.US_ASCII)), InputStream.class);
	}

	private boolean assemble(Object actual, Type type) {
		Assembler assembler = mock(Assembler.class);
		doAnswer((invocation) -> {
			String str = Objects.toString(invocation.getArgument(0));
			OutputStream stream = invocation.getArgument(2);
			stream.write(str.getBytes(StandardCharsets.US_ASCII));
			stream.close();
			return null;
		}).when(assembler).write(eq(actual), any(), eq(stream));
		when(facade.isBinary(any())).thenReturn(true);
		when(facade.cleanForAssembling(null, actual)).thenReturn("type/subtype");
		when(facade.getAssembler("type/subtype")).thenReturn(assembler);
		return write(actual, type);
	}

	private void mockCharset() {
		when(resource.getCharset()).thenReturn(StandardCharsets.UTF_8);
	}

	private void mockWithoutBase64() {
		when(resource.isBase64()).thenReturn(false);
	}

	private void mockWithBase64() {
		when(resource.isBase64()).thenReturn(true);
	}

	private void mockResponseException() {
		try {
			doThrow(IOException.class).when(response).flushBuffer();
		} catch (IOException exception) {
			throw new AssertionError();
		}
	}

	private void mockStreamException() {
		try {
			doThrow(IOException.class).when(stream).close();
		} catch (IOException exception) {
			throw new AssertionError();
		}
	}

	private boolean write(Object actual, Type type) {
		Handler h = new Handler(cache, facade, tree, formatter, constructors, element, gatewayTypes, StandardCharsets.UTF_8, false);
		return h.write(response, resource, actual, type, stream);
	}
}
