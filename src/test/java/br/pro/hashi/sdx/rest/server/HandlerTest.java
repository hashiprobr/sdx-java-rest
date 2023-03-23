package br.pro.hashi.sdx.rest.server;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
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
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
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
		ServletOutputStream output = mock(ServletOutputStream.class);
		response = mock(HttpServletResponse.class);
		when(response.getOutputStream()).thenReturn(output);
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
		doAnswer(answer).when(h).write(eq(response), any(), any(), any(), any());
		h.handle("target", baseRequest, request, response);
		verify(baseRequest).setHandled(true);
	}

	@Test
	void writes() {
		mockCharset();
		mockWithoutBase64();
		mockWithoutCommitted();
		assertTrue(write());
		verify(response).setContentType("type/subtype;charset=UTF-8");
		assertEqualsBytes();
	}

	@Test
	void writesWithISO88591() {
		when(resource.getCharset()).thenReturn(StandardCharsets.ISO_8859_1);
		mockWithoutBase64();
		mockWithoutCommitted();
		assertTrue(write());
		verify(response).setContentType("type/subtype;charset=ISO-8859-1");
		assertEqualsBytes(StandardCharsets.ISO_8859_1, false);
	}

	@Test
	void writesWithBase64() {
		mockCharset();
		mockWithBase64();
		mockWithoutCommitted();
		assertTrue(write());
		verify(response).setContentType("type/subtype;charset=UTF-8;base64");
		assertEqualsBytes(StandardCharsets.UTF_8, true);
	}

	private boolean write() {
		return serialize(SPECIAL_BODY, String.class);
	}

	@Test
	void writesLarge() {
		mockCharset();
		mockWithoutBase64();
		mockWithoutCommitted();
		assertFalse(writeLarge());
		verify(response).setContentType("type/subtype;charset=UTF-8");
		assertEqualsBytes();
		verifyFlush();
	}

	@Test
	void writesLargeWithFlushException() {
		mockCharset();
		mockWithoutBase64();
		mockWithoutCommitted();
		Throwable cause = mockFlushException();
		Exception exception = assertThrows(UncheckedIOException.class, () -> {
			writeLarge();
		});
		assertSame(cause, exception.getCause());
		verify(response).setContentType("type/subtype;charset=UTF-8");
		verifyClose();
	}

	@Test
	void writesLargeWithCommittedFlushException() {
		mockCharset();
		mockWithoutBase64();
		mockWithCommitted();
		mockFlushException();
		assertFalse(writeLarge());
		verify(response).setContentType("type/subtype;charset=UTF-8");
		assertEmptyBytes();
	}

	@Test
	void writesLargeWithCloseAndFlushException() {
		mockCharset();
		mockWithoutBase64();
		mockWithCommitted();
		mockCloseException();
		mockFlushException();
		assertFalse(writeLarge());
		verify(response).setContentType("type/subtype;charset=UTF-8");
		assertEmptyBytes();
	}

	private boolean writeLarge() {
		return serialize(new StringReader(SPECIAL_BODY), Reader.class);
	}

	private boolean serialize(Object actual, Type type) {
		mockSerializer(actual);
		return write(actual, type);
	}

	@Test
	void writesDirectly() {
		mockCharset();
		mockWithoutBase64();
		mockWithoutCommitted();
		assertTrue(writeDirectly());
		verify(response).setContentType("type/subtype;charset=UTF-8");
		assertEqualsBytes();
	}

	@Test
	void writesDirectlyWithISO88591() {
		when(resource.getCharset()).thenReturn(StandardCharsets.ISO_8859_1);
		mockWithoutBase64();
		mockWithoutCommitted();
		assertTrue(writeDirectly());
		verify(response).setContentType("type/subtype;charset=ISO-8859-1");
		assertEqualsBytes(StandardCharsets.ISO_8859_1, false);
	}

	@Test
	void writesDirectlyWithBase64() {
		mockCharset();
		mockWithBase64();
		mockWithoutCommitted();
		assertTrue(writeDirectly());
		verify(response).setContentType("type/subtype;charset=UTF-8;base64");
		assertEqualsBytes(StandardCharsets.UTF_8, true);
	}

	private boolean writeDirectly() {
		return serializeDirectly(SPECIAL_BODY, String.class);
	}

	@Test
	void writesDirectlyLarge() {
		mockCharset();
		mockWithoutBase64();
		mockWithoutCommitted();
		assertFalse(writeDirectlyLarge());
		verify(response).setContentType("type/subtype;charset=UTF-8");
		assertEqualsBytes();
	}

	private boolean writeDirectlyLarge() {
		return serializeDirectly(new StringReader(SPECIAL_BODY), Reader.class);
	}

	private boolean serializeDirectly(Object actual, Type type) {
		mockSerializer(actual);
		return writeDirectly(actual, type);
	}

	private void mockSerializer(Object actual) {
		Serializer serializer = mock(Serializer.class);
		doAnswer((invocation) -> {
			String str = invocation.getArgument(0);
			Writer writer = invocation.getArgument(2);
			writer.write(str);
			writer.close();
			return null;
		}).when(serializer).write(eq(actual), eq(String.class), any());
		doAnswer((invocation) -> {
			StringReader reader = invocation.getArgument(0);
			Writer writer = invocation.getArgument(2);
			reader.transferTo(writer);
			writer.close();
			return null;
		}).when(serializer).write(eq(actual), eq(Reader.class), any());
		when(resource.getContentType()).thenReturn(null);
		when(facade.isBinary(any())).thenReturn(false);
		when(facade.cleanForSerializing(null, actual)).thenReturn("type/subtype");
		when(facade.getSerializer("type/subtype")).thenReturn(serializer);
	}

	private void assertEqualsBytes() {
		assertEqualsBytes(StandardCharsets.UTF_8, false);
	}

	private void assertEqualsBytes(Charset charset, boolean base64) {
		assertEqualsBytes(SPECIAL_BODY, charset, base64);
	}

	@Test
	void writesBinary() {
		mockCharset();
		mockWithoutBase64();
		mockWithoutCommitted();
		assertTrue(writeBinary());
		verify(response).setContentType("type/subtype");
		assertBinaryEqualsBytes();
	}

	@Test
	void writesBinaryWithBase64() {
		mockCharset();
		mockWithBase64();
		mockWithoutCommitted();
		assertTrue(writeBinary());
		verify(response).setContentType("type/subtype;base64");
		assertBinaryEqualsBytes(StandardCharsets.US_ASCII, true);
	}

	private boolean writeBinary() {
		return assemble(USASCII_BODY, String.class);
	}

	@Test
	void writesLargeBinary() {
		mockCharset();
		mockWithoutBase64();
		mockWithoutCommitted();
		assertFalse(writeLargeBinary());
		verify(response).setContentType("type/subtype");
		assertBinaryEqualsBytes();
		verifyFlush();
	}

	@Test
	void writesLargeBinaryWithFlushException() {
		mockCharset();
		mockWithoutBase64();
		mockWithoutCommitted();
		Throwable cause = mockFlushException();
		Exception exception = assertThrows(UncheckedIOException.class, () -> {
			writeLargeBinary();
		});
		assertSame(cause, exception.getCause());
		verify(response).setContentType("type/subtype");
		verifyClose();
	}

	@Test
	void writesLargeBinaryCommittedFlushException() {
		mockCharset();
		mockWithoutBase64();
		mockWithCommitted();
		mockFlushException();
		assertFalse(writeLargeBinary());
		verify(response).setContentType("type/subtype");
		assertEmptyBytes();
	}

	@Test
	void writesLargeBinaryCloseAndFlushException() {
		mockCharset();
		mockWithoutBase64();
		mockWithCommitted();
		mockCloseException();
		mockFlushException();
		assertFalse(writeLargeBinary());
		verify(response).setContentType("type/subtype");
		assertEmptyBytes();
	}

	private boolean writeLargeBinary() {
		return assemble(new ByteArrayInputStream(USASCII_BODY.getBytes(StandardCharsets.US_ASCII)), InputStream.class);
	}

	private boolean assemble(Object actual, Type type) {
		mockAssembler(actual);
		return write(actual, type);
	}

	@Test
	void writesDirectlyBinary() {
		mockCharset();
		mockWithoutBase64();
		mockWithoutCommitted();
		assertTrue(writeDirectlyBinary());
		verify(response).setContentType("type/subtype");
		assertBinaryEqualsBytes();
	}

	@Test
	void writesDirectlyBinaryWithBase64() {
		mockCharset();
		mockWithBase64();
		mockWithoutCommitted();
		assertTrue(writeDirectlyBinary());
		verify(response).setContentType("type/subtype;base64");
		assertBinaryEqualsBytes(StandardCharsets.US_ASCII, true);
	}

	private boolean writeDirectlyBinary() {
		return assembleDirectly(USASCII_BODY, String.class);
	}

	@Test
	void writesDirectlyLargeBinary() {
		mockCharset();
		mockWithoutBase64();
		mockWithoutCommitted();
		assertFalse(writeDirectlyLargeBinary());
		verify(response).setContentType("type/subtype");
		assertBinaryEqualsBytes();
	}

	private boolean writeDirectlyLargeBinary() {
		return assembleDirectly(new ByteArrayInputStream(USASCII_BODY.getBytes(StandardCharsets.US_ASCII)), InputStream.class);
	}

	private boolean assembleDirectly(Object actual, Type type) {
		mockAssembler(actual);
		return writeDirectly(actual, type);
	}

	private void mockAssembler(Object actual) {
		Assembler assembler = mock(Assembler.class);
		doAnswer((invocation) -> {
			String str = invocation.getArgument(0);
			OutputStream output = invocation.getArgument(2);
			output.write(str.getBytes(StandardCharsets.US_ASCII));
			output.close();
			return null;
		}).when(assembler).write(eq(actual), eq(String.class), any());
		doAnswer((invocation) -> {
			InputStream input = invocation.getArgument(0);
			OutputStream output = invocation.getArgument(2);
			input.transferTo(output);
			output.close();
			return null;
		}).when(assembler).write(eq(actual), eq(InputStream.class), any());
		when(resource.getContentType()).thenReturn(null);
		when(facade.isBinary(any())).thenReturn(true);
		when(facade.cleanForAssembling(null, actual)).thenReturn("type/subtype");
		when(facade.getAssembler("type/subtype")).thenReturn(assembler);
	}

	private void assertBinaryEqualsBytes() {
		assertBinaryEqualsBytes(StandardCharsets.US_ASCII, false);
	}

	private void assertBinaryEqualsBytes(Charset charset, boolean base64) {
		assertEqualsBytes(USASCII_BODY, charset, base64);
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

	private void mockWithoutCommitted() {
		when(response.isCommitted()).thenReturn(false);
	}

	private void mockWithCommitted() {
		when(response.isCommitted()).thenReturn(true);
	}

	private void mockCloseException() {
		try {
			doThrow(IOException.class).when(stream).close();
		} catch (IOException exception) {
			throw new AssertionError();
		}
	}

	private Throwable mockFlushException() {
		Throwable cause = new IOException();
		try {
			doThrow(cause).when(response).flushBuffer();
		} catch (IOException exception) {
			throw new AssertionError();
		}
		return cause;
	}

	private boolean write(Object actual, Type type) {
		ServletOutputStream output;
		try {
			output = response.getOutputStream();
			doAnswer((invocation) -> {
				stream.close();
				return null;
			}).when(output).close();
			doAnswer((invocation) -> {
				int b = invocation.getArgument(0);
				stream.write(b);
				return null;
			}).when(output).write(any(int.class));
			doAnswer((invocation) -> {
				byte[] b = invocation.getArgument(0);
				stream.write(b);
				return null;
			}).when(output).write(any(byte[].class));
			doAnswer((invocation) -> {
				byte[] b = invocation.getArgument(0);
				int off = invocation.getArgument(1);
				int len = invocation.getArgument(2);
				stream.write(b, off, len);
				return null;
			}).when(output).write(any(byte[].class), any(int.class), any(int.class));
		} catch (IOException exception) {
			throw new AssertionError(exception);
		}
		return write(actual, type, output);
	}

	private boolean writeDirectly(Object actual, Type type) {
		return write(actual, type, stream);
	}

	private boolean write(Object actual, Type type, OutputStream output) {
		Handler h = new Handler(cache, facade, tree, formatter, constructors, element, gatewayTypes, StandardCharsets.UTF_8, false);
		return h.write(response, resource, actual, type, output);
	}

	private void assertEmptyBytes() {
		assertEqualsBytes("", StandardCharsets.US_ASCII, false);
	}

	private void assertEqualsBytes(String expected, Charset charset, boolean base64) {
		try {
			verify(stream, times(1)).close();
		} catch (IOException exception) {
			throw new AssertionError(exception);
		}
		byte[] bytes = stream.toByteArray();
		if (base64) {
			bytes = Base64.getDecoder().decode(new String(bytes, StandardCharsets.US_ASCII));
		}
		assertEquals(expected, new String(bytes, charset));
	}

	private void verifyClose() {
		try {
			verify(stream, times(0)).close();
		} catch (IOException exception) {
			throw new AssertionError(exception);
		}
	}

	private void verifyFlush() {
		try {
			verify(response).flushBuffer();
		} catch (IOException exception) {
			throw new AssertionError(exception);
		}
	}
}
