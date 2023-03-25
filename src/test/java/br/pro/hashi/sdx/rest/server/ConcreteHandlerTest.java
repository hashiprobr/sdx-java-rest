package br.pro.hashi.sdx.rest.server;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.Writer;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

import org.eclipse.jetty.http.HttpFields;
import org.eclipse.jetty.server.Dispatcher;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.Response;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import br.pro.hashi.sdx.rest.transform.Serializer;
import br.pro.hashi.sdx.rest.transform.facade.Facade;
import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

class ConcreteHandlerTest {
	private static final String BODY = "spéçíál";

	private Serializer serializer;
	private Facade facade;
	private ErrorFormatter formatter;
	private ConcreteHandler h;
	private HttpFields.Mutable fields;
	private Request baseRequest;
	private Response baseResponse;
	private HttpServletRequest request;
	private HttpServletResponse response;

	@BeforeEach
	void setUp() {
		serializer = mock(Serializer.class);
		doAnswer((invocation) -> {
			String str = invocation.getArgument(0);
			Writer writer = invocation.getArgument(2);
			writer.write(str);
			writer.close();
			return null;
		}).when(serializer).write(eq(BODY), eq(String.class), any());
		facade = mock(Facade.class);
		when(facade.cleanForSerializing(null, BODY)).thenReturn("type/subtype");
		when(facade.getSerializer("type/subtype")).thenReturn(serializer);
		formatter = mock(ErrorFormatter.class);
		when(formatter.getReturnType()).thenReturn(String.class);
		when(formatter.format(400, BODY)).thenReturn(BODY);
		fields = mock(HttpFields.Mutable.class);
		baseRequest = mock(Request.class);
		baseResponse = mock(Response.class);
		request = mock(HttpServletRequest.class);
		response = mock(HttpServletResponse.class);
	}

	@Test
	void writesBadMessageError() {
		h = newConcreteHandler();
		ByteBuffer buffer = h.badMessageError(400, BODY, fields);
		assertEqualsBuffer(buffer, StandardCharsets.UTF_8, false);
		verify(fields).add("Content-Type", "type/subtype;charset=UTF-8");
	}

	@Test
	void writesBadMessageErrorInISO88591() {
		h = newConcreteHandler(StandardCharsets.ISO_8859_1, false);
		ByteBuffer buffer = h.badMessageError(400, BODY, fields);
		assertEqualsBuffer(buffer, StandardCharsets.ISO_8859_1, false);
		verify(fields).add("Content-Type", "type/subtype;charset=ISO-8859-1");
	}

	@Test
	void writesBadMessageErrorInBase64() {
		h = newConcreteHandler(StandardCharsets.UTF_8, true);
		ByteBuffer buffer = h.badMessageError(400, BODY, fields);
		assertEqualsBuffer(buffer, StandardCharsets.UTF_8, true);
		verify(fields).add("Content-Type", "type/subtype;charset=UTF-8;base64");
	}

	private void assertEqualsBuffer(ByteBuffer buffer, Charset charset, boolean base64) {
		assertEqualsBytes(BODY, buffer.array(), charset, base64);
	}

	@Test
	void doesNotWriteBadMessageError() {
		h = newConcreteHandler();
		when(fields.add("Content-Type", "type/subtype;charset=UTF-8")).thenThrow(RuntimeException.class);
		assertNull(h.badMessageError(400, BODY, fields));
	}

	@Test
	void handles() {
		mockMessage();
		assertHandles();
		verify(response).setContentType("type/subtype;charset=UTF-8");
	}

	@Test
	void handlesInISO88591() {
		mockMessage();
		assertHandles(StandardCharsets.ISO_8859_1, false);
		verify(response).setContentType("type/subtype;charset=ISO-8859-1");
	}

	@Test
	void handlesInBase64() {
		mockMessage();
		assertHandles(StandardCharsets.UTF_8, true);
		verify(response).setContentType("type/subtype;charset=UTF-8;base64");
	}

	private void assertHandles(Charset charset, boolean base64) {
		assertHandles(BODY, charset, base64);
	}

	@Test
	void handlesWithException() {
		mockMessage();
		doThrow(RuntimeException.class).when(response).setContentType("type/subtype;charset=UTF-8");
		assertHandles("");
	}

	@Test
	void handlesWithCommittedException() {
		mockMessage();
		h = newConcreteHandler();
		when(response.getStatus()).thenReturn(400);
		ServletOutputStream output = mock(ServletOutputStream.class);
		try {
			doThrow(RuntimeException.class).when(output).write(any(int.class));
			doThrow(RuntimeException.class).when(output).write(any(byte[].class));
			doThrow(RuntimeException.class).when(output).write(any(byte[].class), any(int.class), any(int.class));
			when(response.getOutputStream()).thenReturn(output);
			handle();
			verify(output, times(0)).close();
		} catch (IOException exception) {
			throw new AssertionError(exception);
		}
	}

	private void mockMessage() {
		when(request.getAttribute(Dispatcher.ERROR_MESSAGE)).thenReturn(BODY);
	}

	@Test
	void handlesWithoutMessage() {
		when(request.getAttribute(Dispatcher.ERROR_MESSAGE)).thenReturn(null);
		when(baseRequest.getResponse()).thenReturn(baseResponse);
		when(baseResponse.getReason()).thenReturn(BODY);
		assertHandles();
		verify(response).setContentType("type/subtype;charset=UTF-8");
	}

	private void assertHandles() {
		assertHandles(BODY);
	}

	private void assertHandles(String expected) {
		assertHandles(expected, StandardCharsets.UTF_8, false);
	}

	private void assertHandles(String expected, Charset charset, boolean base64) {
		h = newConcreteHandler(charset, base64);
		when(response.getStatus()).thenReturn(400);
		ByteArrayOutputStream stream = spy(new ByteArrayOutputStream());
		ServletOutputStream output = mock(ServletOutputStream.class);
		try {
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
			when(response.getOutputStream()).thenReturn(output);
			handle();
			verify(stream, times(1)).close();
		} catch (IOException exception) {
			throw new AssertionError(exception);
		}
		assertEqualsBytes(expected, stream.toByteArray(), charset, base64);
	}

	private void handle() throws IOException {
		h.handle("target", baseRequest, request, response);
	}

	private void assertEqualsBytes(String expected, byte[] bytes, Charset charset, boolean base64) {
		if (base64) {
			bytes = Base64.getDecoder().decode(new String(bytes, StandardCharsets.US_ASCII));
		}
		assertEquals(expected, new String(bytes, charset));
	}

	private ConcreteHandler newConcreteHandler() {
		return newConcreteHandler(StandardCharsets.UTF_8, false);
	}

	private ConcreteHandler newConcreteHandler(Charset charset, boolean base64) {
		return new ConcreteHandler(facade, formatter, null, charset, base64);
	}
}
