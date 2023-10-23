package br.pro.hashi.sdx.rest.server;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
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
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodHandles.Lookup;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
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
import java.util.function.Consumer;

import org.eclipse.jetty.http.HttpFields;
import org.eclipse.jetty.server.Request;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import br.pro.hashi.sdx.rest.Fields;
import br.pro.hashi.sdx.rest.Hint;
import br.pro.hashi.sdx.rest.reflection.Headers;
import br.pro.hashi.sdx.rest.reflection.PartHeaders;
import br.pro.hashi.sdx.rest.reflection.Queries;
import br.pro.hashi.sdx.rest.server.exception.NotAcceptableException;
import br.pro.hashi.sdx.rest.server.exception.NotFoundException;
import br.pro.hashi.sdx.rest.server.mock.invalid.ResourceWithException;
import br.pro.hashi.sdx.rest.server.mock.valid.ConcreteResource;
import br.pro.hashi.sdx.rest.server.mock.valid.ConcreteResourceWithoutBlank;
import br.pro.hashi.sdx.rest.server.mock.valid.ConcreteResourceWithoutEmpty;
import br.pro.hashi.sdx.rest.server.mock.valid.ConcreteResourceWithoutPlain;
import br.pro.hashi.sdx.rest.server.mock.valid.NullableResource;
import br.pro.hashi.sdx.rest.server.stream.CountOutputStream;
import br.pro.hashi.sdx.rest.server.tree.Data;
import br.pro.hashi.sdx.rest.server.tree.Endpoint;
import br.pro.hashi.sdx.rest.server.tree.Node;
import br.pro.hashi.sdx.rest.server.tree.Tree;
import br.pro.hashi.sdx.rest.server.tree.Tree.Leaf;
import br.pro.hashi.sdx.rest.transform.Assembler;
import br.pro.hashi.sdx.rest.transform.Serializer;
import br.pro.hashi.sdx.rest.transform.exception.TypeException;
import br.pro.hashi.sdx.rest.transform.manager.TransformManager;
import jakarta.servlet.MultipartConfigElement;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletInputStream;
import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.Part;

class HandlerTest {
	private static final String REGULAR_CONTENT = "regular";
	private static final String SPECIAL_CONTENT = "spéçìal";

	private TransformManager manager;
	private Tree tree;
	private ErrorFormatter formatter;
	private Map<Class<? extends RestResource>, MethodHandle> handles;
	private MultipartConfigElement element;
	private Set<Class<? extends RuntimeException>> gatewayTypes;
	private Handler h;
	private HttpFields fields;
	private Request baseRequest;
	private Map<String, String[]> map;
	private HttpServletRequest request;
	private HttpServletResponse response;
	private Node node;
	private Endpoint endpoint;
	private List<Part> parts;
	private RestResource callResource;
	private List<String> callItemList;
	private Map<String, List<Data>> callPartMap;
	private Data callBody;
	private RestResource resource;
	private ByteArrayOutputStream stream;

	@BeforeEach
	void setUp() throws NoSuchMethodException, IOException {
		manager = mock(TransformManager.class);
		when(manager.getExtensionType("txt")).thenReturn("text/plain");
		tree = mock(Tree.class);
		formatter = mock(ErrorFormatter.class);
		handles = new HashMap<>();
		Lookup lookup = MethodHandles.lookup();
		putHandle(lookup, ConcreteResource.class);
		putHandle(lookup, ConcreteResourceWithoutEmpty.class);
		putHandle(lookup, ConcreteResourceWithoutBlank.class);
		putHandle(lookup, ConcreteResourceWithoutPlain.class);
		putHandle(lookup, NullableResource.class);
		element = mock(MultipartConfigElement.class);
		gatewayTypes = new HashSet<>();
		fields = mock(HttpFields.class);
		baseRequest = mock(Request.class);
		when(baseRequest.getHttpFields()).thenReturn(fields);
		map = new HashMap<>();
		request = mock(HttpServletRequest.class);
		when(request.getParameterMap()).thenReturn(map);
		ServletOutputStream output = mock(ServletOutputStream.class);
		response = mock(HttpServletResponse.class);
		when(response.getOutputStream()).thenReturn(output);
		node = mock(Node.class);
		endpoint = mock(Endpoint.class);
		parts = new ArrayList<>();
		resource = mock(RestResource.class);
		stream = spy(new ByteArrayOutputStream());
	}

	private void putHandle(Lookup lookup, Class<? extends RestResource> type) {
		Constructor<? extends RestResource> constructor;
		try {
			constructor = type.getDeclaredConstructor();
		} catch (NoSuchMethodException exception) {
			throw new AssertionError(exception);
		}
		MethodHandle handle;
		try {
			handle = lookup.unreflectConstructor(constructor);
		} catch (IllegalAccessException exception) {
			throw new AssertionError(exception);
		}
		handles.put(type, handle);
	}

	@Test
	void handles() {
		mockMethod();
		mockRequestUri();
		mockNode();
		mockMethodNames();
		mockEndpoint();
		mockContentType();
		ServletInputStream stream = mockInputStream();
		mockResourceType();
		mockCall();
		mockReturnType();
		handle();
		assertItemList();
		assertBody(stream);
		assertOk();
	}

	@Test
	void handlesWithoutCors() {
		mockMethod();
		mockRequestUri();
		mockNode();
		mockMethodNames();
		mockEndpoint();
		mockContentType();
		ServletInputStream stream = mockInputStream();
		mockResourceType();
		mockCall();
		mockReturnType();
		handle(false, mockAnswer());
		assertItemList();
		assertBody(stream);
		verify(response).addHeader("Access-Control-Allow-Origin", "*");
		verify(response).addHeader("Access-Control-Allow-Methods", "*");
		verify(response).addHeader("Access-Control-Allow-Headers", "*");
		verify(response).addHeader("Access-Control-Allow-Credentials", "true");
		assertResponse(200);
	}

	@Test
	void handlesWithoutMethod() {
		when(request.getMethod()).thenReturn("GET");
		when(tree.getMethodNames()).thenReturn(Set.of());
		mockRequestUri();
		mockNode();
		mockMethodNames();
		mockEndpoint();
		mockContentType();
		mockResourceType();
		mockCall();
		mockReturnType();
		handle();
		assertMessageResponse(501, "GET not implemented");
	}

	@Test
	void handlesWithExtensionAndNullNotAcceptable() {
		mockMethod();
		mockRequestUriWithExtension();
		mockNode();
		mockMethodNames();
		mockEndpoint();
		mockContentType();
		ServletInputStream stream = mockInputStream();
		mockResourceType();
		mockCall();
		mockReturnType();
		handle();
		assertItemList();
		assertBody(stream);
		assertOkWithExtension();
	}

	@Test
	void handlesWithExtensionAndEmptyNotAcceptable() {
		mockMethod();
		mockRequestUriWithExtension();
		mockNode();
		mockMethodNames();
		mockEndpoint();
		mockContentType();
		ServletInputStream stream = mockInputStream();
		mockResourceType(ConcreteResourceWithoutEmpty.class);
		mockCall();
		mockReturnType();
		handle();
		assertItemList();
		assertBody(stream);
		assertOkWithExtension();
	}

	private void assertOkWithExtension() {
		assertHeaders();
		assertFields();
		verify(response).setStatus(200);
		verifyNotLength();
		verifyServletWrite("text/plain");
		verifyNoCountWrite();
		verifyNoResponseClose();
		verifyNoError();
	}

	@Test
	void handlesWithoutBlankExtension() {
		mockMethod();
		mockRequestUri();
		mockNode();
		mockMethodNames();
		mockEndpoint();
		mockContentType();
		mockResourceType(ConcreteResourceWithoutBlank.class);
		mockCall();
		mockReturnType();
		handle();
		assertNotAcceptable("URI must have an extension");
	}

	@Test
	void handlesWithoutPlainExtension() {
		mockMethod();
		mockRequestUriWithExtension();
		mockNode();
		mockMethodNames();
		mockEndpoint();
		mockContentType();
		mockResourceType(ConcreteResourceWithoutPlain.class);
		mockCall();
		mockReturnType();
		handle();
		assertNotAcceptable("Extension txt is not acceptable");
	}

	private void mockRequestUriWithExtension() {
		when(request.getRequestURI()).thenReturn("/b.txt");
	}

	private void assertNotAcceptable(String message) {
		assertMessageResponse(406, message);
	}

	@Test
	void handlesWithoutDotfileExtension() {
		mockMethod();
		when(request.getRequestURI()).thenReturn("/b/.txt");
		when(tree.getLeafAndAddItems(new String[] { "b", "" }, List.of())).thenThrow(new NotFoundException());
		mockMethodNames();
		mockEndpoint();
		mockContentType();
		mockResourceType();
		mockCall();
		mockReturnType();
		handle();
		assertNotFound();
	}

	@Test
	void handlesWithEncodedExtension() {
		mockMethod();
		when(request.getRequestURI()).thenReturn("/b%2Etxt");
		mockNodeWithExtension();
		mockMethodNames();
		mockEndpoint();
		mockContentType();
		ServletInputStream stream = mockInputStream();
		mockResourceType();
		mockCall();
		mockReturnType();
		handle();
		assertItemList();
		assertBody(stream);
		assertOk();
	}

	@Test
	void handlesWithSlashedExtension() {
		mockMethod();
		when(request.getRequestURI()).thenReturn("/b.txt///");
		mockNodeWithExtension();
		mockMethodNames();
		mockEndpoint();
		mockContentType();
		ServletInputStream stream = mockInputStream();
		mockResourceType();
		mockCall();
		mockReturnType();
		handle();
		assertItemList();
		assertBody(stream);
		assertOk();
	}

	private void mockNodeWithExtension() {
		when(tree.getLeafAndAddItems(new String[] { "b.txt" }, List.of())).thenAnswer((invocation) -> {
			return loadNode(invocation);
		});
	}

	@Test
	void handlesWithWrongExtension() {
		mockMethod();
		when(request.getRequestURI()).thenReturn("/b.json");
		when(tree.getLeafAndAddItems(new String[] { "b.json" }, List.of())).thenAnswer((invocation) -> {
			return loadNode(invocation);
		});
		mockMethodNames();
		mockEndpoint();
		mockContentType();
		ServletInputStream stream = mockInputStream();
		mockResourceType();
		mockCall();
		mockReturnType();
		handle();
		assertItemList();
		assertBody(stream);
		assertOk();
	}

	@Test
	void handlesWithEmptyExtension() {
		mockMethod();
		when(request.getRequestURI()).thenReturn("/b.");
		when(tree.getLeafAndAddItems(new String[] { "b." }, List.of())).thenAnswer((invocation) -> {
			return loadNode(invocation);
		});
		mockMethodNames();
		mockEndpoint();
		mockContentType();
		ServletInputStream stream = mockInputStream();
		mockResourceType();
		mockCall();
		mockReturnType();
		handle();
		assertItemList();
		assertBody(stream);
		assertOk();
	}

	@Test
	void handlesWithoutRequestUri() {
		mockMethod();
		when(request.getRequestURI()).thenReturn("/%b");
		mockNode();
		mockMethodNames();
		mockEndpoint();
		mockContentType();
		mockResourceType();
		mockCall();
		mockReturnType();
		handle();
		assertBadRequest("URI could not be decoded");
	}

	@Test
	void handlesWithoutNode() {
		mockMethod();
		mockRequestUri();
		when(tree.getLeafAndAddItems(new String[] { "b" }, List.of())).thenThrow(new NotFoundException());
		mockMethodNames();
		mockEndpoint();
		mockContentType();
		mockResourceType();
		mockCall();
		mockReturnType();
		handle();
		assertNotFound();
	}

	private void assertNotFound() {
		assertMessageResponse(404, "Endpoint not found");
	}

	@Test
	void handlesWithPost() {
		mockMethod("POST");
		mockRequestUri();
		mockNode();
		mockMethodNames();
		mockEndpoint("POST");
		mockContentType();
		ServletInputStream stream = mockInputStream();
		mockResourceType();
		mockCall();
		mockReturnType();
		handle();
		assertItemList();
		assertBody(stream);
		assertResponseWithoutHeaders(201);
	}

	@Test
	void handlesWithOptions() {
		mockMethod("OPTIONS");
		mockRequestUri();
		mockNode();
		mockMethodNames();
		mockEndpoint();
		mockContentType();
		mockResourceType();
		mockCall();
		mockReturnType();
		handle();
		assertOptions("GET, POST");
	}

	@Test
	void handlesWithOptionsAndOneVarArg() {
		String[] items = new String[] { "2" };
		mockMethod("OPTIONS");
		mockRequestUri();
		mockNode(items);
		mockVarMethodNames();
		mockEndpoint();
		mockContentType();
		mockResourceType();
		mockCall();
		mockReturnType();
		handle();
		assertOptionsWithVarArgs();
	}

	@Test
	void handlesWithOptionsAndTwoVarArgs() {
		String[] items = new String[] { "2", "3" };
		mockMethod("OPTIONS");
		mockRequestUri();
		mockNode(items);
		mockVarMethodNames();
		mockEndpoint();
		mockContentType();
		mockResourceType();
		mockCall();
		mockReturnType();
		handle();
		assertOptionsWithVarArgs();
	}

	private void assertOptionsWithVarArgs() {
		assertOptions("PUT, PATCH");
	}

	private void assertOptions(String allow) {
		verify(response).addHeader("Allow", allow);
		verify(response).setStatus(200);
		verifyNotLength();
		verifyNoServletWrite();
		verifyNoCountWrite();
		verifyResponseClose();
		verifyNoError();
	}

	@Test
	void handlesWithoutEndpoint() {
		mockMethod();
		mockRequestUri();
		mockNode();
		mockMethodNames();
		when(node.getEndpoint("GET", 0)).thenReturn(null);
		mockContentType();
		mockResourceType();
		mockCall();
		mockReturnType();
		handle();
		assertMessageResponse(405, "GET not allowed");
	}

	@Test
	void handlesWithOneVarArg() {
		String[] items = new String[] { "2" };
		mockMethod();
		mockRequestUri();
		mockNode(items);
		mockMethodNames();
		mockEndpoint(1);
		mockContentType();
		ServletInputStream stream = mockInputStream();
		mockResourceType();
		mockCall(items);
		mockReturnType();
		handle();
		assertItemList(items);
		assertBody(stream);
		assertOk();
	}

	@Test
	void handlesWithTwoVarArgs() {
		String[] items = new String[] { "2", "3" };
		mockMethod();
		mockRequestUri();
		mockNode(items);
		mockMethodNames();
		mockEndpoint(2);
		mockContentType();
		ServletInputStream stream = mockInputStream();
		mockResourceType();
		mockCall(items);
		mockReturnType();
		handle();
		assertItemList(items);
		assertBody(stream);
		assertOk();
	}

	@Test
	void handlesWithNullContentType() {
		mockMethod();
		mockRequestUri();
		mockNode();
		mockMethodNames();
		mockEndpoint();
		when(request.getContentType()).thenReturn(null);
		ServletInputStream stream = mockInputStream();
		mockResourceType();
		mockCall();
		mockReturnType();
		handle();
		assertItemList();
		assertBody(null, stream);
		assertOk();
	}

	@Test
	void handlesWithMultipartContentType() {
		mockMethod();
		mockRequestUri();
		mockNode();
		mockMethodNames();
		mockEndpoint();
		mockMultipartContentType();
		mockParts();
		mockResourceType();
		mockCall();
		mockReturnType();
		handle();
		assertItemList();
		HeadersMap headersMap = callResource.headersMap;
		assertTrue(headersMap.names().isEmpty());
		assertTrue(callPartMap.isEmpty());
		assertNull(callBody);
		assertOk();
	}

	@Test
	void handlesWithOnePart() {
		mockMethod();
		mockRequestUri();
		mockNode();
		mockMethodNames();
		mockEndpoint();
		mockMultipartContentType();
		Part part = mock(Part.class);
		when(part.getName()).thenReturn(null);
		when(part.getContentType()).thenReturn("type");
		InputStream stream = mockInputStream(part);
		parts.add(part);
		mockParts();
		mockResourceType();
		mockCall();
		mockReturnType();
		handle();
		assertItemList();
		HeadersMap headersMap = callResource.headersMap;
		assertEquals(1, headersMap.names().size());
		PartHeaders partHeaders = (PartHeaders) headersMap.get("");
		assertSame(part, partHeaders.getPart());
		assertEquals(1, callPartMap.size());
		List<Data> partList = callPartMap.get("");
		assertEquals(1, partList.size());
		Data data = partList.get(0);
		assertEquals("type", data.getContentType());
		assertSame(stream, data.getStream());
		assertNull(callBody);
		assertOk();
	}

	@Test
	void handlesWithTwoParts() {
		mockMethod();
		mockRequestUri();
		mockNode();
		mockMethodNames();
		mockEndpoint();
		mockMultipartContentType();
		Part part0 = mock(Part.class);
		when(part0.getName()).thenReturn(null);
		when(part0.getContentType()).thenReturn("type0");
		InputStream stream0 = mockInputStream(part0);
		parts.add(part0);
		Part part1 = mock(Part.class);
		when(part1.getName()).thenReturn(null);
		when(part1.getContentType()).thenReturn("type1");
		InputStream stream1 = mockInputStream(part1);
		parts.add(part1);
		mockParts();
		mockResourceType();
		mockCall();
		mockReturnType();
		handle();
		assertItemList();
		HeadersMap headersMap = callResource.headersMap;
		assertEquals(1, headersMap.names().size());
		List<Fields> headersList = headersMap.getList("");
		assertEquals(2, headersList.size());
		PartHeaders partHeaders = (PartHeaders) headersList.get(0);
		assertSame(part0, partHeaders.getPart());
		partHeaders = (PartHeaders) headersList.get(1);
		assertSame(part1, partHeaders.getPart());
		assertEquals(1, callPartMap.size());
		List<Data> partList = callPartMap.get("");
		assertEquals(2, partList.size());
		Data data = partList.get(0);
		assertEquals("type0", data.getContentType());
		assertSame(stream0, data.getStream());
		data = partList.get(1);
		assertEquals("type1", data.getContentType());
		assertSame(stream1, data.getStream());
		assertNull(callBody);
		assertOk();
	}

	@Test
	void handlesWithOnePartAndOneNamedPart() {
		mockMethod();
		mockRequestUri();
		mockNode();
		mockMethodNames();
		mockEndpoint();
		mockMultipartContentType();
		Part part0 = mock(Part.class);
		when(part0.getName()).thenReturn(null);
		when(part0.getContentType()).thenReturn("type0");
		InputStream stream0 = mockInputStream(part0);
		parts.add(part0);
		Part part1 = mock(Part.class);
		when(part1.getName()).thenReturn("name");
		when(part1.getContentType()).thenReturn("type1");
		InputStream stream1 = mockInputStream(part1);
		parts.add(part1);
		mockParts();
		mockResourceType();
		mockCall();
		mockReturnType();
		handle();
		assertItemList();
		HeadersMap headersMap = callResource.headersMap;
		assertEquals(2, headersMap.names().size());
		PartHeaders partHeaders = (PartHeaders) headersMap.get("");
		assertSame(part0, partHeaders.getPart());
		partHeaders = (PartHeaders) headersMap.get("name");
		assertSame(part1, partHeaders.getPart());
		assertEquals(2, callPartMap.size());
		List<Data> partList = callPartMap.get("");
		assertEquals(1, partList.size());
		Data data = partList.get(0);
		assertEquals("type0", data.getContentType());
		assertSame(stream0, data.getStream());
		partList = callPartMap.get("name");
		assertEquals(1, partList.size());
		data = partList.get(0);
		assertEquals("type1", data.getContentType());
		assertSame(stream1, data.getStream());
		assertNull(callBody);
		assertOk();
	}

	@Test
	void handlesWithOneNamedPart() {
		mockMethod();
		mockRequestUri();
		mockNode();
		mockMethodNames();
		mockEndpoint();
		mockMultipartContentType();
		Part part = mock(Part.class);
		when(part.getName()).thenReturn("name");
		when(part.getContentType()).thenReturn("type");
		InputStream stream = mockInputStream(part);
		parts.add(part);
		mockParts();
		mockResourceType();
		mockCall();
		mockReturnType();
		handle();
		assertItemList();
		HeadersMap headersMap = callResource.headersMap;
		assertEquals(1, headersMap.names().size());
		PartHeaders partHeaders = (PartHeaders) headersMap.get("name");
		assertSame(part, partHeaders.getPart());
		assertEquals(1, callPartMap.size());
		List<Data> partList = callPartMap.get("name");
		assertEquals(1, partList.size());
		Data data = partList.get(0);
		assertEquals("type", data.getContentType());
		assertSame(stream, data.getStream());
		assertNull(callBody);
		assertOk();
	}

	@Test
	void handlesWithOneNamedPartAndOnePart() {
		mockMethod();
		mockRequestUri();
		mockNode();
		mockMethodNames();
		mockEndpoint();
		mockMultipartContentType();
		Part part0 = mock(Part.class);
		when(part0.getName()).thenReturn("name");
		when(part0.getContentType()).thenReturn("type0");
		InputStream stream0 = mockInputStream(part0);
		parts.add(part0);
		Part part1 = mock(Part.class);
		when(part1.getName()).thenReturn(null);
		when(part1.getContentType()).thenReturn("type1");
		InputStream stream1 = mockInputStream(part1);
		parts.add(part1);
		mockParts();
		mockResourceType();
		mockCall();
		mockReturnType();
		handle();
		assertItemList();
		HeadersMap headersMap = callResource.headersMap;
		assertEquals(2, headersMap.names().size());
		PartHeaders partHeaders = (PartHeaders) headersMap.get("name");
		assertSame(part0, partHeaders.getPart());
		partHeaders = (PartHeaders) headersMap.get("");
		assertSame(part1, partHeaders.getPart());
		assertEquals(2, callPartMap.size());
		List<Data> partList = callPartMap.get("name");
		assertEquals(1, partList.size());
		Data data = partList.get(0);
		assertEquals("type0", data.getContentType());
		assertSame(stream0, data.getStream());
		partList = callPartMap.get("");
		assertEquals(1, partList.size());
		data = partList.get(0);
		assertEquals("type1", data.getContentType());
		assertSame(stream1, data.getStream());
		assertNull(callBody);
		assertOk();
	}

	@Test
	void handlesWithTwoNamedParts() {
		mockMethod();
		mockRequestUri();
		mockNode();
		mockMethodNames();
		mockEndpoint();
		mockMultipartContentType();
		Part part0 = mock(Part.class);
		when(part0.getName()).thenReturn("name0");
		when(part0.getContentType()).thenReturn("type0");
		InputStream stream0 = mockInputStream(part0);
		parts.add(part0);
		Part part1 = mock(Part.class);
		when(part1.getName()).thenReturn("name1");
		when(part1.getContentType()).thenReturn("type1");
		InputStream stream1 = mockInputStream(part1);
		parts.add(part1);
		mockParts();
		mockResourceType();
		mockCall();
		mockReturnType();
		handle();
		assertItemList();
		HeadersMap headersMap = callResource.headersMap;
		assertEquals(2, headersMap.names().size());
		PartHeaders partHeaders = (PartHeaders) headersMap.get("name0");
		assertSame(part0, partHeaders.getPart());
		partHeaders = (PartHeaders) headersMap.get("name1");
		assertSame(part1, partHeaders.getPart());
		assertEquals(2, callPartMap.size());
		List<Data> partList = callPartMap.get("name0");
		assertEquals(1, partList.size());
		Data data = partList.get(0);
		assertEquals("type0", data.getContentType());
		assertSame(stream0, data.getStream());
		partList = callPartMap.get("name1");
		assertEquals(1, partList.size());
		data = partList.get(0);
		assertEquals("type1", data.getContentType());
		assertSame(stream1, data.getStream());
		assertNull(callBody);
		assertOk();
	}

	private InputStream mockInputStream(Part part) {
		InputStream stream = InputStream.nullInputStream();
		try {
			when(part.getInputStream()).thenReturn(stream);
		} catch (IOException exception) {
			throw new AssertionError(exception);
		}
		return stream;
	}

	private void mockParts() {
		try {
			when(request.getParts()).thenReturn(parts);
		} catch (ServletException | IOException exception) {
			throw new AssertionError(exception);
		}
	}

	@Test
	void handlesWithInvalidParts() {
		mockMethod();
		mockRequestUri();
		mockNode();
		mockMethodNames();
		mockEndpoint();
		mockMultipartContentType();
		mockMultipartException(ServletException.class);
		mockResourceType();
		mockCall();
		mockReturnType();
		handle();
		assertBadRequest("Parts could not be parsed");
	}

	@Test
	void handlesWithLargeBoth() {
		mockMethod();
		mockRequestUri();
		mockNode();
		mockMethodNames();
		mockEndpoint();
		mockMultipartContentType();
		mockMultipartException(10, 5);
		mockResourceType();
		mockCall();
		mockReturnType();
		handle();
		assertPayloadTooLarge("Multipart request exceeds 10 bytes or one of the parts exceeds 5 bytes");
	}

	@Test
	void handlesWithLargeRequest() {
		mockMethod();
		mockRequestUri();
		mockNode();
		mockMethodNames();
		mockEndpoint();
		mockMultipartContentType();
		mockMultipartException(10, 0);
		mockResourceType();
		mockCall();
		mockReturnType();
		handle();
		assertPayloadTooLarge("Multipart request exceeds 10 bytes");
	}

	@Test
	void handlesWithLargePart() {
		mockMethod();
		mockRequestUri();
		mockNode();
		mockMethodNames();
		mockEndpoint();
		mockMultipartContentType();
		mockMultipartException(0, 5);
		mockResourceType();
		mockCall();
		mockReturnType();
		handle();
		assertPayloadTooLarge("One of the parts exceeds 5 bytes");
	}

	@Test
	void handlesWithLargeNeither() {
		mockMethod();
		mockRequestUri();
		mockNode();
		mockMethodNames();
		mockEndpoint();
		mockMultipartContentType();
		mockMultipartException(0, 0);
		mockResourceType();
		mockCall();
		mockReturnType();
		handle();
		assertPayloadTooLarge("Multipart request is too large or one of the parts is too large");
	}

	private void mockMultipartContentType() {
		when(request.getContentType()).thenReturn("multipart/form-data");
	}

	private void mockMultipartException(long maxRequestSize, long maxFileSize) {
		when(element.getMaxRequestSize()).thenReturn(maxRequestSize);
		when(element.getMaxFileSize()).thenReturn(maxFileSize);
		mockMultipartException(IllegalStateException.class);
	}

	private void mockMultipartException(Class<? extends Exception> type) {
		try {
			when(request.getParts()).thenThrow(type);
		} catch (ServletException | IOException exception) {
			throw new AssertionError(exception);
		}
	}

	private void assertBadRequest(String message) {
		assertMessageResponse(400, message);
	}

	private void assertPayloadTooLarge(String message) {
		assertMessageResponse(413, message);
	}

	private void assertMessageResponse(int status, String message) {
		assertHeaders();
		assertNoStatus();
		verifyNotLength();
		verifyNoServletWrite();
		verifyNoCountWrite();
		verifyNoResponseClose();
		verifyError(status, message);
	}

	@Test
	void handlesWithRestException() {
		mockMethod();
		mockRequestUri();
		mockNode();
		mockMethodNames();
		mockEndpoint();
		mockContentType();
		ServletInputStream stream = mockInputStream();
		mockResourceType();
		mockRestExceptionCall(450, new Object());
		mockReturnType();
		handle();
		assertItemList();
		assertBody(stream);
		assertResponseWithoutHeaders(450);
	}

	@Test
	void handlesWithMessageRestException() {
		mockMethod();
		mockRequestUri();
		mockNode();
		mockMethodNames();
		mockEndpoint();
		mockContentType();
		ServletInputStream stream = mockInputStream();
		mockResourceType();
		mockRestExceptionCall(550, "message");
		when(formatter.format(550, "message")).thenReturn(new Object());
		when(formatter.getReturnType()).thenReturn(Object.class);
		handle();
		assertItemList();
		assertBody(stream);
		assertResponseWithoutHeaders(550);
	}

	private void mockRestExceptionCall(int status, Object body) {
		assertDoesNotThrow(() -> {
			when(endpoint.call(any(), eq(List.of("0", "1")), eq(Map.of()), any())).thenAnswer((invocation) -> {
				saveCall(invocation);
				throw new RestException(status, body);
			});
		});
	}

	@Test
	void handlesWithVoid() {
		mockMethod();
		mockRequestUri();
		mockNode();
		mockMethodNames();
		mockEndpoint();
		mockContentType();
		ServletInputStream stream = mockInputStream();
		mockResourceType();
		mockCall();
		mockReturnType(void.class);
		handle();
		assertItemList();
		assertBody(stream);
		assertNoContent();
	}

	@Test
	void handlesWithVoidWrapper() {
		mockMethod();
		mockRequestUri();
		mockNode();
		mockMethodNames();
		mockEndpoint();
		mockContentType();
		ServletInputStream stream = mockInputStream();
		mockResourceType();
		mockCall();
		mockReturnType(Void.class);
		handle();
		assertItemList();
		assertBody(stream);
		assertNoContent();
	}

	@Test
	void handlesWithoutNull() {
		mockMethod();
		mockRequestUri();
		mockNode();
		mockMethodNames();
		mockEndpoint();
		mockContentType();
		ServletInputStream stream = mockInputStream();
		mockResourceType();
		mockNullCall();
		mockReturnType();
		handle();
		assertItemList();
		assertBody(stream);
		assertNoContent();
	}

	@Test
	void handlesWithNull() {
		mockMethod();
		mockRequestUri();
		mockNode();
		mockMethodNames();
		mockEndpoint();
		mockContentType();
		ServletInputStream stream = mockInputStream();
		doReturn(NullableResource.class).when(endpoint).getResourceType();
		mockNullCall();
		mockReturnType();
		handle();
		assertItemList();
		assertBody(stream);
		assertOk();
	}

	private void assertOk() {
		assertResponseWithoutHeaders(200);
	}

	private void assertResponseWithoutHeaders(int status) {
		assertHeaders();
		assertResponse(status);
	}

	private void assertResponse(int status) {
		assertFields();
		verify(response).setStatus(status);
		verifyNotLength();
		verifyServletWrite(null);
		verifyNoCountWrite();
		verifyNoResponseClose();
		verifyNoError();
	}

	@Test
	void handlesWithHead() {
		mockHeadMethod();
		mockRequestUri();
		mockNode();
		mockMethodNames();
		mockHeadEndpoint();
		mockContentType();
		ServletInputStream stream = mockInputStream();
		mockResourceType();
		mockCall();
		mockReturnType();
		handle();
		assertItemList();
		assertBody(stream);
		assertHeaders();
		assertFields();
		verify(response).setStatus(200);
		verify(response).setContentLengthLong(any(long.class));
		verifyNoServletWrite();
		verifyCountWrite();
		verifyResponseClose();
		verifyNoError();
	}

	@Test
	void handlesWithHeadWithoutContent() {
		mockHeadMethod();
		mockRequestUri();
		mockNode();
		mockMethodNames();
		mockHeadEndpoint();
		mockContentType();
		ServletInputStream stream = mockInputStream();
		mockResourceType();
		mockNullCall();
		mockReturnType();
		handle();
		assertItemList();
		assertBody(stream);
		assertNoContent();
	}

	private void mockNullCall() {
		assertDoesNotThrow(() -> {
			when(endpoint.call(any(), eq(List.of("0", "1")), eq(Map.of()), any())).thenAnswer((invocation) -> {
				saveCall(invocation);
				return null;
			});
		});
	}

	private void assertNoContent() {
		assertHeaders();
		assertFields();
		verify(response).setStatus(204);
		verifyNotLength();
		verifyNoServletWrite();
		verifyNoCountWrite();
		verifyResponseClose();
		verifyNoError();
	}

	@Test
	void handlesWithHeadWithoutWrite() {
		mockHeadMethod();
		mockRequestUri();
		mockNode();
		mockMethodNames();
		mockHeadEndpoint();
		mockContentType();
		ServletInputStream stream = mockInputStream();
		mockResourceType();
		mockCall();
		mockReturnType();
		handle((invocation) -> {
			return false;
		});
		assertItemList();
		assertBody(stream);
		assertHeaders();
		assertFields();
		verify(response).setStatus(200);
		verifyNotLength();
		verifyNoServletWrite();
		verifyCountWrite();
		verifyResponseClose();
		verifyNoError();
	}

	private void mockHeadMethod() {
		mockMethod("HEAD");
	}

	private void mockHeadEndpoint() {
		mockEndpoint("HEAD");
	}

	private void verifyCountWrite() {
		verify(h).write(eq(response), any(), any(), eq(Object.class), eq(null), any(CountOutputStream.class));
	}

	private void verifyResponseClose() {
		try {
			verify(response.getOutputStream()).close();
		} catch (IOException exception) {
			throw new AssertionError(exception);
		}
	}

	@Test
	void handlesWithGatewayException() {
		mockMethod();
		mockRequestUri();
		mockNode();
		mockMethodNames();
		mockEndpoint();
		mockContentType();
		ServletInputStream stream = mockInputStream();
		mockResourceType();
		mockException(NullPointerException.class);
		mockReturnType();
		handle();
		assertItemList();
		assertBody(stream);
		assertException(502);
	}

	@Test
	void handlesWithSingleException() {
		mockMethod();
		mockRequestUri();
		mockNode();
		mockMethodNames();
		mockEndpoint();
		mockContentType();
		ServletInputStream stream = mockInputStream();
		mockResourceType();
		mockException();
		mockReturnType();
		handle();
		assertItemList();
		assertBody(stream);
		assertException();
	}

	@Test
	void handlesWithDoubleException() throws IOException {
		mockMethod();
		mockRequestUri();
		mockNode();
		mockMethodNames();
		mockEndpoint();
		mockContentType();
		ServletInputStream stream = mockInputStream();
		mockResourceType();
		mockException();
		mockReturnType();
		doThrow(IOException.class).when(response).sendError(500, null);
		handle();
		assertItemList();
		assertBody(stream);
		assertException();
	}

	private void mockException() {
		mockException(RuntimeException.class);
	}

	private void mockException(Class<? extends RuntimeException> type) {
		gatewayTypes.add(NullPointerException.class);
		RuntimeException exception;
		try {
			exception = type.getDeclaredConstructor().newInstance();
		} catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException | InstantiationException error) {
			throw new AssertionError(error);
		}
		assertDoesNotThrow(() -> {
			when(endpoint.call(any(), eq(List.of("0", "1")), eq(Map.of()), any())).thenAnswer((invocation) -> {
				saveCall(invocation);
				throw exception;
			});
		});
	}

	private void assertException() {
		assertException(500);
	}

	private void assertException(int status) {
		assertHeaders();
		assertFields();
		assertNoStatus();
		verifyNotLength();
		verifyNoServletWrite();
		verifyNoCountWrite();
		verifyNoResponseClose();
		verifyError(status, null);
	}

	private void verifyError(int status, String message) {
		verify(h).sendError(response, status, message);
	}

	private void mockMethod() {
		mockMethod("GET");
	}

	private void mockMethod(String methodName) {
		when(request.getMethod()).thenReturn(methodName);
		when(tree.getMethodNames()).thenReturn(Set.of(methodName));
	}

	private void mockRequestUri() {
		when(request.getRequestURI()).thenReturn("/b///");
	}

	private void mockNode() {
		mockNode(new String[] {});
	}

	private void mockNode(String[] items) {
		when(tree.getLeafAndAddItems(new String[] { "b" }, List.of())).thenAnswer((invocation) -> {
			return loadNode(invocation, items);
		});
	}

	private Leaf loadNode(InvocationOnMock invocation) {
		return loadNode(invocation, new String[] {});
	}

	private Leaf loadNode(InvocationOnMock invocation, String[] items) {
		List<String> itemList = invocation.getArgument(1);
		itemList.add("0");
		itemList.add("1");
		for (String item : items) {
			itemList.add(item);
		}
		return new Leaf(node, items.length);
	}

	private void mockMethodNames() {
		when(node.getMethodNames()).thenReturn(new LinkedHashSet<>(List.of("GET", "POST")));
	}

	private void mockVarMethodNames() {
		when(node.getVarMethodNames()).thenReturn(new LinkedHashSet<>(List.of("PUT", "PATCH")));
	}

	private void mockEndpoint() {
		mockEndpoint(0);
	}

	private void mockEndpoint(String methodName) {
		mockEndpoint(methodName, 0);
	}

	private void mockEndpoint(int varSize) {
		mockEndpoint("GET", varSize);
	}

	private void mockEndpoint(String methodName, int varSize) {
		when(node.getEndpoint(methodName, varSize)).thenReturn(endpoint);
	}

	private void mockContentType() {
		when(request.getContentType()).thenReturn("type/subtype");
	}

	private ServletInputStream mockInputStream() {
		ServletInputStream stream = mock(ServletInputStream.class);
		try {
			when(request.getInputStream()).thenReturn(stream);
		} catch (IOException exception) {
			throw new AssertionError(exception);
		}
		return stream;
	}

	private void mockResourceType() {
		mockResourceType(ConcreteResource.class);
	}

	private void mockResourceType(Class<? extends RestResource> type) {
		doReturn(type).when(endpoint).getResourceType();
	}

	private void mockCall() {
		mockCall(new String[] {});
	}

	private void mockCall(String[] items) {
		List<String> itemList = new ArrayList<>();
		itemList.add("0");
		itemList.add("1");
		for (String item : items) {
			itemList.add(item);
		}
		assertDoesNotThrow(() -> {
			when(endpoint.call(any(), eq(itemList), any(), any())).thenAnswer((invocation) -> {
				saveCall(invocation);
				return new Object();
			});
		});
	}

	private void saveCall(InvocationOnMock invocation) {
		callResource = invocation.getArgument(0);
		callItemList = invocation.getArgument(1);
		callPartMap = invocation.getArgument(2);
		callBody = invocation.getArgument(3);
	}

	private void mockReturnType() {
		mockReturnType(Object.class);
	}

	private void mockReturnType(Type type) {
		when(endpoint.getReturnType()).thenReturn(type);
	}

	private void assertItemList() {
		assertItemList(new String[] {});
	}

	private void assertItemList(String[] items) {
		List<String> itemList = new ArrayList<>();
		itemList.add("0");
		itemList.add("1");
		for (String item : items) {
			itemList.add(item);
		}
		assertEquals(itemList, callItemList);
	}

	private void assertBody(ServletInputStream stream) {
		assertBody("type/subtype", stream);
	}

	private void assertBody(String contentType, ServletInputStream stream) {
		assertEquals(contentType, callBody.getContentType());
		assertSame(stream, callBody.getStream());
	}

	private void assertHeaders() {
		verify(response, times(0)).addHeader(any(), any());
	}

	private void assertFields() {
		Headers headers = (Headers) callResource.headers;
		assertSame(fields, headers.getFields());
		Queries queries = (Queries) callResource.queries;
		assertSame(map, queries.getMap());
	}

	private void assertNoStatus() {
		verify(response, times(0)).setStatus(any(int.class));
	}

	private void verifyNotLength() {
		verify(response, times(0)).setContentLengthLong(any(long.class));
	}

	private void verifyServletWrite(String responseType) {
		verify(h).write(eq(response), any(), any(), eq(Object.class), eq(responseType), any(ServletOutputStream.class));
	}

	private void verifyNoServletWrite() {
		verify(h, times(0)).write(eq(response), any(), any(), eq(Object.class), any(), any(ServletOutputStream.class));
	}

	private void verifyNoCountWrite() {
		verify(h, times(0)).write(eq(response), any(), any(), eq(Object.class), any(), any(CountOutputStream.class));
	}

	private void verifyNoResponseClose() {
		try {
			verify(response.getOutputStream(), times(0)).close();
		} catch (IOException exception) {
			throw new AssertionError(exception);
		}
	}

	private void verifyNoError() {
		verify(h, times(0)).sendError(eq(response), any(int.class), any());
	}

	private void handle() {
		handle(mockAnswer());
	}

	private void handle(Answer<Boolean> answer) {
		handle(true, answer);
		verify(response, times(0)).addHeader("Access-Control-Allow-Origin", "*");
		verify(response, times(0)).addHeader("Access-Control-Allow-Methods", "*");
		verify(response, times(0)).addHeader("Access-Control-Allow-Headers", "*");
		verify(response, times(0)).addHeader("Access-Control-Allow-Credentials", "true");
	}

	private void handle(boolean cors, Answer<Boolean> answer) {
		h = spy(newHandler(cors));
		doAnswer(answer).when(h).write(eq(response), any(), any(), eq(Object.class), any(), any());
		h.handle("target", baseRequest, request, response);
		verify(baseRequest).setHandled(true);
	}

	private Answer<Boolean> mockAnswer() {
		return (invocation) -> {
			return true;
		};
	}

	@Test
	void doesNotInvoke() {
		h = newHandler();
		Lookup lookup = MethodHandles.lookup();
		Constructor<? extends RestResource> constructor;
		try {
			constructor = ResourceWithException.class.getDeclaredConstructor();
		} catch (NoSuchMethodException exception) {
			throw new AssertionError();
		}
		MethodHandle handle;
		try {
			handle = lookup.unreflectConstructor(constructor);
		} catch (IllegalAccessException exception) {
			throw new AssertionError();
		}
		assertThrows(AssertionError.class, () -> {
			h.invoke(handle);
		});
	}

	@Test
	void writes() {
		mockCharset();
		mockWithoutBase64();
		mockWithoutCommitted();
		assertTrue(write());
		verify(response).setContentType("type/subtype;charset=UTF-8");
		assertEqualsBytes();
		verifyNoFlush();
	}

	@Test
	void writesWithISO88591() {
		when(resource.getCharset()).thenReturn(StandardCharsets.ISO_8859_1);
		mockWithoutBase64();
		mockWithoutCommitted();
		assertTrue(write());
		verify(response).setContentType("type/subtype;charset=ISO-8859-1");
		assertEqualsBytes(StandardCharsets.ISO_8859_1, false);
		verifyNoFlush();
	}

	@Test
	void writesWithBase64() {
		mockCharset();
		mockWithBase64();
		mockWithoutCommitted();
		assertTrue(write());
		verify(response).setContentType("type/subtype;charset=UTF-8;base64");
		assertEqualsBytes(StandardCharsets.UTF_8, true);
		verifyNoFlush();
	}

	private boolean write() {
		return serialize(SPECIAL_CONTENT, String.class);
	}

	@Test
	void writesWithException() {
		mockCharset();
		mockWithoutBase64();
		mockWithoutCommitted();
		assertThrows(RuntimeException.class, () -> {
			serializeWithException();
		});
		verify(response).setContentType("type/subtype;charset=UTF-8");
		verifyNoWrite();
		verifyNoFlush();
	}

	@Test
	void writesWithCloseException() {
		mockCharset();
		mockWithoutBase64();
		mockWithoutCommitted();
		mockCloseException();
		assertTrue(write());
		verify(response).setContentType("type/subtype;charset=UTF-8");
		assertEqualsBytes();
		verifyNoFlush();
	}

	@Test
	void writesWithCommittedException() {
		mockCharset();
		mockWithoutBase64();
		mockWithCommitted();
		assertTrue(serializeWithException());
		verify(response).setContentType("type/subtype;charset=UTF-8");
		verifyNoWrite();
		verifyNoFlush();
	}

	@Test
	void writesWithSupportException() {
		mockCharset();
		mockWithoutBase64();
		mockWithoutCommitted();
		assertThrows(TypeException.class, () -> {
			serializeWithException(TypeException.class);
		});
		verify(response).setContentType("type/subtype;charset=UTF-8");
		verifyNoWrite();
		verifyNoFlush();
	}

	private boolean serializeWithException() {
		return serializeWithException(RuntimeException.class);
	}

	private boolean serializeWithException(Class<? extends RuntimeException> type) {
		Serializer serializer = mockSerializer(SPECIAL_CONTENT);
		doThrow(type).when(serializer).write(eq(SPECIAL_CONTENT), eq(String.class), any());
		return write(SPECIAL_CONTENT, String.class);
	}

	@Test
	void writesWithSupportExceptionAndExtension() {
		mockCharset();
		mockWithoutBase64();
		mockWithoutCommitted();
		Serializer serializer = mockSerializer(SPECIAL_CONTENT);
		doThrow(TypeException.class).when(serializer).write(eq(SPECIAL_CONTENT), eq(String.class), any());
		assertThrows(NotAcceptableException.class, () -> {
			write(SPECIAL_CONTENT, String.class, "text/plain");
		});
		verify(response).setContentType("text/plain;charset=UTF-8");
		verifyNoWrite();
		verifyNoFlush();
	}

	@Test
	void writesConsumer() {
		mockCharset();
		mockWithoutBase64();
		mockWithoutCommitted();
		Consumer<Writer> consumer = (output) -> {
			try {
				output.write(SPECIAL_CONTENT);
			} catch (IOException exception) {
				throw new AssertionError(exception);
			}
		};
		assertFalse(serialize(consumer, new Hint<Consumer<Writer>>() {}.getType()));
		verify(response).setContentType("type/subtype;charset=UTF-8");
		assertEqualsBytes();
		verifyFlush();
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
	void writesLargeWithCloseException() throws IOException {
		mockCharset();
		mockWithoutBase64();
		mockWithoutCommitted();
		mockCloseException();
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
		verifyNoWrite();
	}

	@Test
	void writesLargeWithCommittedFlushException() {
		mockCharset();
		mockWithoutBase64();
		mockWithCommitted();
		mockFlushException();
		assertFalse(writeLarge());
		verify(response).setContentType("type/subtype;charset=UTF-8");
		verifyNoWrite();
	}

	private boolean writeLarge() {
		return serialize(mockReader(), Reader.class);
	}

	private boolean serialize(Object actual, Type type) {
		mockSerializerWithoutException(actual);
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
		verifyNoFlush();
	}

	@Test
	void writesDirectlyWithExtension() {
		mockCharset();
		mockWithoutBase64();
		mockWithoutCommitted();
		assertTrue(serializeDirectlyWithExtension(SPECIAL_CONTENT, String.class));
		verify(response).setContentType("text/plain;charset=UTF-8");
		assertEqualsBytes();
		verifyNoFlush();
	}

	@Test
	void writesDirectlyWithISO88591() {
		when(resource.getCharset()).thenReturn(StandardCharsets.ISO_8859_1);
		mockWithoutBase64();
		mockWithoutCommitted();
		assertTrue(writeDirectly());
		verify(response).setContentType("type/subtype;charset=ISO-8859-1");
		assertEqualsBytes(StandardCharsets.ISO_8859_1, false);
		verifyNoFlush();
	}

	@Test
	void writesDirectlyWithBase64() {
		mockCharset();
		mockWithBase64();
		mockWithoutCommitted();
		assertTrue(writeDirectly());
		verify(response).setContentType("type/subtype;charset=UTF-8;base64");
		assertEqualsBytes(StandardCharsets.UTF_8, true);
		verifyNoFlush();
	}

	private boolean writeDirectly() {
		return serializeDirectly(SPECIAL_CONTENT, String.class);
	}

	@Test
	void writesDirectlyLarge() {
		mockCharset();
		mockWithoutBase64();
		mockWithoutCommitted();
		assertFalse(serializeDirectly(mockReader(), Reader.class));
		verify(response).setContentType("type/subtype;charset=UTF-8");
		assertEqualsBytes();
		verifyNoFlush();
	}

	@Test
	void writesDirectlyLargeWithExtension() {
		mockCharset();
		mockWithoutBase64();
		mockWithoutCommitted();
		assertFalse(serializeDirectlyWithExtension(mockReader(), Reader.class));
		verify(response).setContentType("text/plain;charset=UTF-8");
		assertEqualsBytes();
		verifyNoFlush();
	}

	private Object mockReader() {
		return new StringReader(SPECIAL_CONTENT);
	}

	private boolean serializeDirectly(Object actual, Type type) {
		mockSerializerWithoutException(actual);
		return writeDirectly(actual, type, null);
	}

	private boolean serializeDirectlyWithExtension(Object actual, Type type) {
		mockSerializerWithoutException(actual);
		return writeDirectly(actual, type, "text/plain");
	}

	private void mockSerializerWithoutException(Object actual) {
		Serializer serializer = mockSerializer(actual);
		doAnswer((invocation) -> {
			String str = invocation.getArgument(0);
			Writer writer = invocation.getArgument(2);
			writer.write(str);
			when(response.isCommitted()).thenReturn(true);
			return null;
		}).when(serializer).write(eq(actual), eq(String.class), any());
		doAnswer((invocation) -> {
			StringReader reader = invocation.getArgument(0);
			Writer writer = invocation.getArgument(2);
			reader.transferTo(writer);
			when(response.isCommitted()).thenReturn(true);
			return null;
		}).when(serializer).write(eq(actual), eq(Reader.class), any());
		doAnswer((invocation) -> {
			Consumer<Writer> consumer = invocation.getArgument(0);
			Writer writer = invocation.getArgument(2);
			consumer.accept(writer);
			when(response.isCommitted()).thenReturn(true);
			return null;
		}).when(serializer).write(eq(actual), eq(new Hint<Consumer<Writer>>() {}.getType()), any());
	}

	private Serializer mockSerializer(Object actual) {
		Serializer serializer = mock(Serializer.class);
		when(resource.getContentType()).thenReturn(null);
		when(manager.isBinary(any())).thenReturn(false);
		when(manager.getSerializerType(eq(null), eq(actual), any())).thenReturn("type/subtype");
		when(manager.getSerializerType(eq("text/plain"), eq(actual), any())).thenReturn("text/plain");
		when(manager.getSerializer("type/subtype")).thenReturn(serializer);
		when(manager.getSerializer("text/plain")).thenReturn(serializer);
		return serializer;
	}

	private void assertEqualsBytes() {
		assertEqualsBytes(StandardCharsets.UTF_8, false);
	}

	private void assertEqualsBytes(Charset charset, boolean base64) {
		assertEqualsBytes(SPECIAL_CONTENT, charset, base64);
	}

	@Test
	void writesBinary() {
		mockCharset();
		mockWithoutBase64();
		mockWithoutCommitted();
		assertTrue(writeBinary());
		verify(response).setContentType("type/subtype");
		assertBinaryEqualsBytes();
		verifyNoFlush();
	}

	@Test
	void writesBinaryWithBase64() {
		mockCharset();
		mockWithBase64();
		mockWithoutCommitted();
		assertTrue(writeBinary());
		verify(response).setContentType("type/subtype;base64");
		assertBinaryEqualsBytes(StandardCharsets.US_ASCII, true);
		verifyNoFlush();
	}

	private boolean writeBinary() {
		return assemble(REGULAR_CONTENT, String.class);
	}

	@Test
	void writesBinaryWithException() {
		mockCharset();
		mockWithoutBase64();
		mockWithoutCommitted();
		assertThrows(RuntimeException.class, () -> {
			assembleWithException();
		});
		verify(response).setContentType("type/subtype");
		verifyNoWrite();
		verifyNoFlush();
	}

	@Test
	void writesBinaryWithCloseException() throws IOException {
		mockCharset();
		mockWithoutBase64();
		mockWithoutCommitted();
		mockCloseException();
		assertTrue(writeBinary());
		verify(response).setContentType("type/subtype");
		assertBinaryEqualsBytes();
		verifyNoFlush();
	}

	@Test
	void writesBinaryWithCommittedException() {
		mockCharset();
		mockWithoutBase64();
		mockWithCommitted();
		assertTrue(assembleWithException());
		verify(response).setContentType("type/subtype");
		verifyNoWrite();
		verifyNoFlush();
	}

	private boolean assembleWithException() {
		Assembler assembler = mockAssembler(REGULAR_CONTENT);
		doThrow(RuntimeException.class).when(assembler).write(eq(REGULAR_CONTENT), eq(String.class), any());
		return write(REGULAR_CONTENT, String.class);
	}

	@Test
	void writesConsumerBinary() {
		mockCharset();
		mockWithoutBase64();
		mockWithoutCommitted();
		Consumer<OutputStream> consumer = (output) -> {
			try {
				output.write(mockBytes());
			} catch (IOException exception) {
				throw new AssertionError(exception);
			}
		};
		assertFalse(assemble(consumer, new Hint<Consumer<OutputStream>>() {}.getType()));
		verify(response).setContentType("type/subtype");
		assertBinaryEqualsBytes();
		verifyFlush();
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
	void writesLargeBinaryWithCloseException() throws IOException {
		mockCharset();
		mockWithoutBase64();
		mockWithoutCommitted();
		mockCloseException();
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
		verifyNoWrite();
	}

	@Test
	void writesLargeBinaryCommittedFlushException() {
		mockCharset();
		mockWithoutBase64();
		mockWithCommitted();
		mockFlushException();
		assertFalse(writeLargeBinary());
		verify(response).setContentType("type/subtype");
		verifyNoWrite();
	}

	private boolean writeLargeBinary() {
		return assemble(mockStream(), InputStream.class);
	}

	private boolean assemble(Object actual, Type type) {
		mockAssemblerWithoutException(actual);
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
		verifyNoFlush();
	}

	@Test
	void writesDirectlyBinaryWithExtension() {
		mockCharset();
		mockWithoutBase64();
		mockWithoutCommitted();
		assertTrue(assembleDirectlyWithExtension(REGULAR_CONTENT, String.class));
		verify(response).setContentType("text/plain");
		assertBinaryEqualsBytes();
		verifyNoFlush();
	}

	@Test
	void writesDirectlyBinaryWithBase64() {
		mockCharset();
		mockWithBase64();
		mockWithoutCommitted();
		assertTrue(writeDirectlyBinary());
		verify(response).setContentType("type/subtype;base64");
		assertBinaryEqualsBytes(StandardCharsets.US_ASCII, true);
		verifyNoFlush();
	}

	private boolean writeDirectlyBinary() {
		return assembleDirectly(REGULAR_CONTENT, String.class);
	}

	@Test
	void writesDirectlyLargeBinary() {
		mockCharset();
		mockWithoutBase64();
		mockWithoutCommitted();
		assertFalse(assembleDirectly(mockStream(), InputStream.class));
		verify(response).setContentType("type/subtype");
		assertBinaryEqualsBytes();
		verifyNoFlush();
	}

	@Test
	void writesDirectlyLargeBinaryWithExtension() {
		mockCharset();
		mockWithoutBase64();
		mockWithoutCommitted();
		assertFalse(assembleDirectlyWithExtension(mockStream(), InputStream.class));
		verify(response).setContentType("text/plain");
		assertBinaryEqualsBytes();
		verifyNoFlush();
	}

	private Object mockStream() {
		return new ByteArrayInputStream(mockBytes());
	}

	private byte[] mockBytes() {
		return REGULAR_CONTENT.getBytes(StandardCharsets.US_ASCII);
	}

	private boolean assembleDirectly(Object actual, Type type) {
		mockAssemblerWithoutException(actual);
		return writeDirectly(actual, type, null);
	}

	private boolean assembleDirectlyWithExtension(Object actual, Type type) {
		mockAssemblerWithoutException(actual);
		return writeDirectly(actual, type, "text/plain");
	}

	private void mockAssemblerWithoutException(Object actual) {
		Assembler assembler = mockAssembler(actual);
		doAnswer((invocation) -> {
			String str = invocation.getArgument(0);
			OutputStream output = invocation.getArgument(2);
			output.write(str.getBytes(StandardCharsets.US_ASCII));
			when(response.isCommitted()).thenReturn(true);
			return null;
		}).when(assembler).write(eq(actual), eq(String.class), any());
		doAnswer((invocation) -> {
			InputStream input = invocation.getArgument(0);
			OutputStream output = invocation.getArgument(2);
			input.transferTo(output);
			when(response.isCommitted()).thenReturn(true);
			return null;
		}).when(assembler).write(eq(actual), eq(InputStream.class), any());
		doAnswer((invocation) -> {
			Consumer<OutputStream> consumer = invocation.getArgument(0);
			OutputStream output = invocation.getArgument(2);
			consumer.accept(output);
			when(response.isCommitted()).thenReturn(true);
			return null;
		}).when(assembler).write(eq(actual), eq(new Hint<Consumer<OutputStream>>() {}.getType()), any());
	}

	private Assembler mockAssembler(Object actual) {
		Assembler assembler = mock(Assembler.class);
		when(resource.getContentType()).thenReturn(null);
		when(manager.isBinary(any())).thenReturn(true);
		when(manager.getAssemblerType(eq(null), eq(actual), any())).thenReturn("type/subtype");
		when(manager.getAssemblerType(eq("text/plain"), eq(actual), any())).thenReturn("text/plain");
		when(manager.getAssembler("type/subtype")).thenReturn(assembler);
		when(manager.getAssembler("text/plain")).thenReturn(assembler);
		return assembler;
	}

	private void assertBinaryEqualsBytes() {
		assertBinaryEqualsBytes(StandardCharsets.US_ASCII, false);
	}

	private void assertBinaryEqualsBytes(Charset charset, boolean base64) {
		assertEqualsBytes(REGULAR_CONTENT, charset, base64);
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
		Throwable cause = new IOException();
		try {
			doThrow(cause).when(stream).close();
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
		return write(actual, type, null);
	}

	private boolean write(Object actual, Type type, String extensionType) {
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
		return write(actual, type, extensionType, output);
	}

	private boolean writeDirectly(Object actual, Type type, String extensionType) {
		return write(actual, type, extensionType, stream);
	}

	private boolean write(Object actual, Type type, String extensionType, OutputStream output) {
		h = newHandler();
		return h.write(response, resource, actual, type, extensionType, output);
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

	private void verifyNoWrite() {
		try {
			verify(stream, times(0)).write(any(int.class));
			verify(stream, times(0)).write(any(byte[].class));
			verify(stream, times(0)).write(any(byte[].class), any(int.class), any(int.class));
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

	private void verifyNoFlush() {
		try {
			verify(response, times(0)).flushBuffer();
		} catch (IOException exception) {
			throw new AssertionError(exception);
		}
	}

	private Handler newHandler() {
		return newHandler(false);
	}

	private Handler newHandler(boolean cors) {
		return new Handler(manager, tree, formatter, handles, element, gatewayTypes, StandardCharsets.UTF_8, cors);
	}
}
