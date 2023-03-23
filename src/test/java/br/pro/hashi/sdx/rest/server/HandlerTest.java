package br.pro.hashi.sdx.rest.server;

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

import org.eclipse.jetty.http.HttpFields;
import org.eclipse.jetty.server.Request;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import br.pro.hashi.sdx.rest.Fields;
import br.pro.hashi.sdx.rest.reflection.Cache;
import br.pro.hashi.sdx.rest.reflection.Headers;
import br.pro.hashi.sdx.rest.reflection.PartHeaders;
import br.pro.hashi.sdx.rest.reflection.Queries;
import br.pro.hashi.sdx.rest.server.exception.NotFoundException;
import br.pro.hashi.sdx.rest.server.mock.valid.NullableResource;
import br.pro.hashi.sdx.rest.server.mock.valid.Resource;
import br.pro.hashi.sdx.rest.server.tree.Data;
import br.pro.hashi.sdx.rest.server.tree.Endpoint;
import br.pro.hashi.sdx.rest.server.tree.Node;
import br.pro.hashi.sdx.rest.server.tree.Tree;
import br.pro.hashi.sdx.rest.transform.Assembler;
import br.pro.hashi.sdx.rest.transform.Serializer;
import br.pro.hashi.sdx.rest.transform.facade.Facade;
import jakarta.servlet.MultipartConfigElement;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletInputStream;
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
	private Handler h;
	private HttpFields fields;
	private Request baseRequest;
	private Map<String, String[]> map;
	private HttpServletRequest request;
	private HttpServletResponse response;
	private Node node;
	private Endpoint endpoint;
	private List<Part> parts;
	private RestResource resource;
	private ByteArrayOutputStream stream;
	private RestResource callResource;
	private List<String> callItemList;
	private Map<String, List<Data>> callPartMap;
	private Data callBody;

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
		fields = mock(HttpFields.class);
		when(baseRequest.getHttpFields()).thenReturn(fields);
		request = mock(HttpServletRequest.class);
		map = new HashMap<>();
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

	@Test
	void handles() {
		mockRequestUri();
		mockNode();
		mockMethodNames();
		mockMethod();
		mockEndpoint();
		mockContentType();
		ServletInputStream stream = mockInputStream();
		mockResourceType();
		mockCall();
		mockReturnType();
		handle();
		assertEquals(List.of("0", "1"), callItemList);
		assertBody(stream);
		assertOk();
	}

	@Test
	void handlesWithoutCors() {
		mockRequestUri();
		mockNode();
		mockMethodNames();
		mockMethod();
		mockEndpoint();
		mockContentType();
		ServletInputStream stream = mockInputStream();
		mockResourceType();
		mockCall();
		mockReturnType();
		handle(false, mockAnswer());
		assertEquals(List.of("0", "1"), callItemList);
		assertBody(stream);
		verify(response).addHeader("Access-Control-Allow-Origin", "*");
		verify(response).addHeader("Access-Control-Allow-Methods", "*");
		verify(response).addHeader("Access-Control-Allow-Headers", "*");
		verify(response).addHeader("Access-Control-Allow-Credentials", "true");
		assertResponse(200);
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
		assertBadRequest("URI could not be decoded");
	}

	@Test
	void handlesWithoutNode() {
		mockRequestUri();
		when(tree.getNodeAndAddItems(new String[] { "b" }, List.of())).thenThrow(new NotFoundException());
		mockMethodNames();
		mockMethod();
		mockEndpoint();
		mockContentType();
		mockResourceType();
		mockCall();
		mockReturnType();
		handle();
		assertNotFound();
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
		assertNotFound();
	}

	private void assertNotFound() {
		assertMessageResponse(404, "Endpoint not found");
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
		assertMessageResponse(405, "GET not allowed");
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
		verify(response).setStatus(200);
		verify(response, times(0)).setContentLengthLong(any(long.class));
		verify(h, times(0)).write(eq(response), any(), any(), eq(Object.class), any(ServletOutputStream.class));
		verify(h, times(0)).write(eq(response), any(), any(), eq(Object.class), any(CountOutputStream.class));
		try {
			verify(response.getOutputStream(), times(0)).close();
		} catch (IOException exception) {
			throw new AssertionError(exception);
		}
		verify(h, times(0)).sendError(eq(response), any(int.class), any());
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
		ServletInputStream stream = mockInputStream();
		mockResourceType();
		mockCall();
		mockReturnType();
		handle();
		assertEquals(List.of("0", "1"), callItemList);
		assertBody(null, stream);
		assertOk();
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
		assertEquals(List.of("0", "1"), callItemList);
		Map<String, List<Fields>> partHeaders = callResource.partHeaders;
		assertTrue(partHeaders.isEmpty());
		assertTrue(callPartMap.isEmpty());
		assertNull(callBody);
		assertOk();
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
		when(part.getContentType()).thenReturn("type");
		InputStream stream = mockInputStream(part);
		parts.add(part);
		mockParts();
		mockResourceType();
		mockCall();
		mockReturnType();
		handle();
		assertEquals(List.of("0", "1"), callItemList);
		Map<String, List<Fields>> partHeaders = callResource.partHeaders;
		assertEquals(1, partHeaders.size());
		PartHeaders headers = (PartHeaders) partHeaders.get("").get(0);
		assertSame(part, headers.getPart());
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
		mockRequestUri();
		mockNode();
		mockMethodNames();
		mockMethod();
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
		assertEquals(List.of("0", "1"), callItemList);
		Map<String, List<Fields>> partHeaders = callResource.partHeaders;
		assertEquals(1, partHeaders.size());
		List<Fields> headersList = partHeaders.get("");
		assertEquals(2, headersList.size());
		PartHeaders headers = (PartHeaders) headersList.get(0);
		assertSame(part0, headers.getPart());
		headers = (PartHeaders) headersList.get(1);
		assertSame(part1, headers.getPart());
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
		mockRequestUri();
		mockNode();
		mockMethodNames();
		mockMethod();
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
		assertEquals(List.of("0", "1"), callItemList);
		Map<String, List<Fields>> partHeaders = callResource.partHeaders;
		assertEquals(2, partHeaders.size());
		PartHeaders headers = (PartHeaders) partHeaders.get("").get(0);
		assertSame(part0, headers.getPart());
		headers = (PartHeaders) partHeaders.get("name").get(0);
		assertSame(part1, headers.getPart());
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
		mockRequestUri();
		mockNode();
		mockMethodNames();
		mockMethod();
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
		assertEquals(List.of("0", "1"), callItemList);
		Map<String, List<Fields>> partHeaders = callResource.partHeaders;
		assertEquals(1, partHeaders.size());
		PartHeaders headers = (PartHeaders) partHeaders.get("name").get(0);
		assertSame(part, headers.getPart());
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
		mockRequestUri();
		mockNode();
		mockMethodNames();
		mockMethod();
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
		assertEquals(List.of("0", "1"), callItemList);
		Map<String, List<Fields>> partHeaders = callResource.partHeaders;
		assertEquals(2, partHeaders.size());
		PartHeaders headers = (PartHeaders) partHeaders.get("name").get(0);
		assertSame(part0, headers.getPart());
		headers = (PartHeaders) partHeaders.get("").get(0);
		assertSame(part1, headers.getPart());
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
		mockRequestUri();
		mockNode();
		mockMethodNames();
		mockMethod();
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
		assertEquals(List.of("0", "1"), callItemList);
		Map<String, List<Fields>> partHeaders = callResource.partHeaders;
		assertEquals(2, partHeaders.size());
		PartHeaders headers = (PartHeaders) partHeaders.get("name0").get(0);
		assertSame(part0, headers.getPart());
		headers = (PartHeaders) partHeaders.get("name1").get(0);
		assertSame(part1, headers.getPart());
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
		assertBadRequest("Parts could not be parsed");
	}

	private void mockMultipartContentType() {
		when(request.getContentType()).thenReturn("multipart/form-data");
	}

	private void assertBadRequest(String message) {
		assertMessageResponse(400, message);
	}

	private void assertMessageResponse(int status, String message) {
		assertHeaders();
		verify(response, times(0)).setStatus(any(int.class));
		verify(response, times(0)).setContentLengthLong(any(long.class));
		verify(h, times(0)).write(eq(response), any(), any(), eq(Object.class), any(ServletOutputStream.class));
		verify(h, times(0)).write(eq(response), any(), any(), eq(Object.class), any(CountOutputStream.class));
		try {
			verify(response.getOutputStream(), times(0)).close();
		} catch (IOException exception) {
			throw new AssertionError(exception);
		}
		verify(h).sendError(response, status, message);
	}

	@Test
	void handlesWithRestException() {
		mockRequestUri();
		mockNode();
		mockMethodNames();
		mockMethod();
		mockEndpoint();
		mockContentType();
		ServletInputStream stream = mockInputStream();
		mockResourceType();
		mockRestException(450, new Object());
		mockReturnType();
		handle();
		assertEquals(List.of("0", "1"), callItemList);
		assertBody(stream);
		assertResponseWithoutHeaders(450);
	}

	@Test
	void handlesWithMessageRestException() {
		mockRequestUri();
		mockNode();
		mockMethodNames();
		mockMethod();
		mockEndpoint();
		mockContentType();
		ServletInputStream stream = mockInputStream();
		mockResourceType();
		mockRestException(550, "message");
		when(formatter.format(550, "message")).thenReturn(new Object());
		when(formatter.getReturnType()).thenReturn(Object.class);
		handle();
		assertEquals(List.of("0", "1"), callItemList);
		assertBody(stream);
		assertResponseWithoutHeaders(550);
	}

	private void mockRestException(int status, Object body) {
		when(endpoint.call(any(), any(), any(), any())).thenAnswer((invocation) -> {
			saveCall(invocation);
			throw new RestException(status, body);
		});
	}

	@Test
	void handlesWithVoid() {
		mockRequestUri();
		mockNode();
		mockMethodNames();
		mockMethod();
		mockEndpoint();
		mockContentType();
		ServletInputStream stream = mockInputStream();
		mockResourceType();
		mockCall();
		mockReturnType(void.class);
		handle();
		assertEquals(List.of("0", "1"), callItemList);
		assertBody(stream);
		assertNoContent();
	}

	@Test
	void handlesWithVoidWrapper() {
		mockRequestUri();
		mockNode();
		mockMethodNames();
		mockMethod();
		mockEndpoint();
		mockContentType();
		ServletInputStream stream = mockInputStream();
		mockResourceType();
		mockCall();
		mockReturnType(Void.class);
		handle();
		assertEquals(List.of("0", "1"), callItemList);
		assertBody(stream);
		assertNoContent();
	}

	@Test
	void handlesWithoutNull() {
		mockRequestUri();
		mockNode();
		mockMethodNames();
		mockMethod();
		mockEndpoint();
		mockContentType();
		ServletInputStream stream = mockInputStream();
		mockResourceType();
		mockNullCall();
		mockReturnType();
		handle();
		assertEquals(List.of("0", "1"), callItemList);
		assertBody(stream);
		assertNoContent();
	}

	private void assertNoContent() {
		assertHeaders();
		assertFields();
		verify(response).setStatus(204);
		verify(response, times(0)).setContentLengthLong(any(long.class));
		verify(h, times(0)).write(eq(response), any(), any(), eq(Object.class), any(ServletOutputStream.class));
		verify(h, times(0)).write(eq(response), any(), any(), eq(Object.class), any(CountOutputStream.class));
		try {
			verify(response.getOutputStream()).close();
		} catch (IOException exception) {
			throw new AssertionError(exception);
		}
		verify(h, times(0)).sendError(eq(response), any(int.class), any());
	}

	@Test
	void handlesWithNull() {
		mockRequestUri();
		mockNode();
		mockMethodNames();
		mockMethod();
		mockEndpoint();
		mockContentType();
		ServletInputStream stream = mockInputStream();
		doReturn(NullableResource.class).when(endpoint).getResourceType();
		mockNullCall();
		mockReturnType();
		handle();
		assertEquals(List.of("0", "1"), callItemList);
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
		verify(response, times(0)).setContentLengthLong(any(long.class));
		verify(h).write(eq(response), any(), any(), eq(Object.class), any(ServletOutputStream.class));
		verify(h, times(0)).write(eq(response), any(), any(), eq(Object.class), any(CountOutputStream.class));
		try {
			verify(response.getOutputStream(), times(0)).close();
		} catch (IOException exception) {
			throw new AssertionError(exception);
		}
		verify(h, times(0)).sendError(eq(response), any(int.class), any());
	}

	@Test
	void handlesWithHead() {
		mockRequestUri();
		mockNode();
		mockMethodNames();
		mockHead();
		mockContentType();
		ServletInputStream stream = mockInputStream();
		mockResourceType();
		mockCall();
		mockReturnType();
		handle();
		assertEquals(List.of("0", "1"), callItemList);
		assertBody(stream);
		assertHeaders();
		assertFields();
		verify(response).setStatus(200);
		verify(response).setContentLengthLong(any(long.class));
		verify(h, times(0)).write(eq(response), any(), any(), eq(Object.class), any(ServletOutputStream.class));
		verify(h).write(eq(response), any(), any(), eq(Object.class), any(CountOutputStream.class));
		try {
			verify(response.getOutputStream()).close();
		} catch (IOException exception) {
			throw new AssertionError(exception);
		}
		verify(h, times(0)).sendError(eq(response), any(int.class), any());
	}

	@Test
	void handlesWithHeadWithoutContent() {
		mockRequestUri();
		mockNode();
		mockMethodNames();
		mockHead();
		mockContentType();
		ServletInputStream stream = mockInputStream();
		mockResourceType();
		mockNullCall();
		mockReturnType();
		handle();
		assertEquals(List.of("0", "1"), callItemList);
		assertBody(stream);
		assertHeaders();
		assertFields();
		verify(response).setStatus(204);
		verify(response, times(0)).setContentLengthLong(any(long.class));
		verify(h, times(0)).write(eq(response), any(), any(), eq(Object.class), any(ServletOutputStream.class));
		verify(h, times(0)).write(eq(response), any(), any(), eq(Object.class), any(CountOutputStream.class));
		try {
			verify(response.getOutputStream()).close();
		} catch (IOException exception) {
			throw new AssertionError(exception);
		}
		verify(h, times(0)).sendError(eq(response), any(int.class), any());
	}

	private void mockNullCall() {
		when(endpoint.call(any(), any(), any(), any())).thenAnswer((invocation) -> {
			saveCall(invocation);
			return null;
		});
	}

	@Test
	void handlesWithHeadWithoutWrite() {
		mockRequestUri();
		mockNode();
		mockMethodNames();
		mockHead();
		mockContentType();
		ServletInputStream stream = mockInputStream();
		mockResourceType();
		mockCall();
		mockReturnType();
		handle((invocation) -> {
			return false;
		});
		assertEquals(List.of("0", "1"), callItemList);
		assertBody(stream);
		assertHeaders();
		assertFields();
		verify(response).setStatus(200);
		verify(response, times(0)).setContentLengthLong(any(long.class));
		verify(h, times(0)).write(eq(response), any(), any(), eq(Object.class), any(ServletOutputStream.class));
		verify(h).write(eq(response), any(), any(), eq(Object.class), any(CountOutputStream.class));
		try {
			verify(response.getOutputStream()).close();
		} catch (IOException exception) {
			throw new AssertionError(exception);
		}
		verify(h, times(0)).sendError(eq(response), any(int.class), any());
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
		ServletInputStream stream = mockInputStream();
		mockResourceType();
		mockReturnType();
		mockException(NullPointerException.class);
		handle();
		assertEquals(List.of("0", "1"), callItemList);
		assertBody(stream);
		assertException(502);
	}

	@Test
	void handlesWithSingleException() {
		mockRequestUri();
		mockNode();
		mockMethodNames();
		mockMethod();
		mockEndpoint();
		mockContentType();
		ServletInputStream stream = mockInputStream();
		mockResourceType();
		mockReturnType();
		mockException();
		handle();
		assertEquals(List.of("0", "1"), callItemList);
		assertBody(stream);
		assertException(500);
	}

	@Test
	void handlesWithDoubleException() throws IOException {
		mockRequestUri();
		mockNode();
		mockMethodNames();
		mockMethod();
		mockEndpoint();
		mockContentType();
		ServletInputStream stream = mockInputStream();
		mockResourceType();
		mockReturnType();
		mockException();
		doThrow(IOException.class).when(response).sendError(500, null);
		handle();
		assertEquals(List.of("0", "1"), callItemList);
		assertBody(stream);
		assertException(500);
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
		when(endpoint.call(any(), any(), any(), any())).thenAnswer((invocation) -> {
			saveCall(invocation);
			throw exception;
		});
	}

	private void assertBody(ServletInputStream stream) {
		assertBody("type/subtype", stream);
	}

	private void assertBody(String contentType, ServletInputStream stream) {
		assertEquals(contentType, callBody.getContentType());
		assertSame(stream, callBody.getStream());
	}

	private void assertException(int status) {
		assertHeaders();
		assertFields();
		verify(response, times(0)).setStatus(any(int.class));
		verify(response, times(0)).setContentLengthLong(any(long.class));
		verify(h, times(0)).write(eq(response), any(), any(), eq(Object.class), any(ServletOutputStream.class));
		verify(h, times(0)).write(eq(response), any(), any(), eq(Object.class), any(CountOutputStream.class));
		try {
			verify(response.getOutputStream(), times(0)).close();
		} catch (IOException exception) {
			throw new AssertionError(exception);
		}
		verify(h).sendError(eq(response), eq(status), eq(null));
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

	private void mockRequestUri() {
		when(request.getRequestURI()).thenReturn("/b");
	}

	private void mockNode() {
		when(tree.getNodeAndAddItems(new String[] { "b" }, List.of())).thenAnswer((invocation) -> {
			List<String> itemList = invocation.getArgument(1);
			itemList.add("0");
			itemList.add("1");
			return node;
		});
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
		when(endpoint.call(any(), any(), any(), any())).thenAnswer((invocation) -> {
			saveCall(invocation);
			return new Object();
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
		h = spy(new Handler(cache, facade, tree, formatter, constructors, element, gatewayTypes, StandardCharsets.UTF_8, cors));
		doAnswer(answer).when(h).write(eq(response), any(), any(), eq(Object.class), any());
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
		h = new Handler(cache, facade, tree, formatter, constructors, element, gatewayTypes, StandardCharsets.UTF_8, false);
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
