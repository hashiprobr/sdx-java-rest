package br.pro.hashi.sdx.rest.server;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
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
			String body = invocation.getArgument(0);
			Writer writer = invocation.getArgument(2);
			writer.write(body);
			writer.close();
			return null;
		}).when(serializer).write(eq(BODY), eq(String.class), any());
		facade = mock(Facade.class);
		when(facade.cleanForSerializing("type/subtype", BODY)).thenReturn("type/subtype");
		when(facade.getSerializer("type/subtype")).thenReturn(serializer);
		formatter = mock(ErrorFormatter.class);
		when(formatter.getReturnType()).thenReturn(String.class);
		when(formatter.format(400, BODY)).thenReturn(BODY);
		fields = mock(HttpFields.Mutable.class);
		baseRequest = mock(Request.class);
		baseResponse = mock(Response.class);
		when(baseResponse.getReason()).thenReturn(BODY);
		when(baseRequest.getResponse()).thenReturn(baseResponse);
		request = mock(HttpServletRequest.class);
		response = mock(HttpServletResponse.class);
		when(response.getStatus()).thenReturn(400);
	}

	@Test
	void getBadMessageError() {
		h = newConcreteHandler();
		ByteBuffer buffer = h.badMessageError(400, BODY, fields);
		assertEqualsBuffer(buffer, StandardCharsets.UTF_8, false);
		verify(fields).add("Content-Type", "type/subtype;charset=UTF-8");
	}

	@Test
	void getBadMessageErrorInISO88591() {
		h = newConcreteHandler(StandardCharsets.ISO_8859_1, false);
		ByteBuffer buffer = h.badMessageError(400, BODY, fields);
		assertEqualsBuffer(buffer, StandardCharsets.ISO_8859_1, false);
		verify(fields).add("Content-Type", "type/subtype;charset=ISO-8859-1");
	}

	@Test
	void getBadMessageErrorInBase64() {
		h = newConcreteHandler(StandardCharsets.UTF_8, true);
		ByteBuffer buffer = h.badMessageError(400, BODY, fields);
		assertEqualsBuffer(buffer, StandardCharsets.UTF_8, true);
		verify(fields).add("Content-Type", "type/subtype;charset=UTF-8;base64");
	}

	private void assertEqualsBuffer(ByteBuffer buffer, Charset charset, boolean base64) {
		assertEqualsBytes(buffer.array(), charset, base64);
	}

	@Test
	void doesNotGetBadMessageError() {
		h = newConcreteHandler();
		when(fields.add("Content-Type", "type/subtype;charset=UTF-8")).thenThrow(RuntimeException.class);
		assertNull(h.badMessageError(400, BODY, fields));
	}

	private ConcreteHandler newConcreteHandler() {
		return newConcreteHandler(StandardCharsets.UTF_8, false);
	}

	@Test
	void handles() {
		mockMessage();
		handle();
		verify(response).setContentType("type/subtype;charset=UTF-8");
	}

	@Test
	void handlesInISO88591() {
		mockMessage();
		handle(StandardCharsets.ISO_8859_1, false);
		verify(response).setContentType("type/subtype;charset=ISO-8859-1");
	}

	@Test
	void handlesInBase64() {
		mockMessage();
		handle(StandardCharsets.UTF_8, true);
		verify(response).setContentType("type/subtype;charset=UTF-8;base64");
	}

	private void mockMessage() {
		when(request.getAttribute(Dispatcher.ERROR_MESSAGE)).thenReturn(BODY);
	}

	@Test
	void handlesWithoutMessage() {
		when(request.getAttribute(Dispatcher.ERROR_MESSAGE)).thenReturn(null);
		handle();
		verify(response).setContentType("type/subtype;charset=UTF-8");
	}

	private void handle() {
		handle(StandardCharsets.UTF_8, false);
	}

	private void handle(Charset charset, boolean base64) {
		h = newConcreteHandler(charset, base64);
		ByteArrayOutputStream output = new ByteArrayOutputStream();
		ServletOutputStream stream = mock(ServletOutputStream.class);
		try {
			doAnswer((invocation) -> {
				int b = invocation.getArgument(0);
				output.write(b);
				return null;
			}).when(stream).write(any(int.class));
			doAnswer((invocation) -> {
				byte[] b = invocation.getArgument(0);
				output.write(b);
				return null;
			}).when(stream).write(any(byte[].class));
			doAnswer((invocation) -> {
				byte[] b = invocation.getArgument(0);
				int off = invocation.getArgument(1);
				int len = invocation.getArgument(2);
				output.write(b, off, len);
				return null;
			}).when(stream).write(any(byte[].class), any(int.class), any(int.class));
			when(response.getOutputStream()).thenReturn(stream);
			h.handle("target", baseRequest, request, response);
		} catch (IOException exception) {
			throw new AssertionError(exception);
		}
		assertEqualsBytes(output.toByteArray(), charset, base64);
	}

	private void assertEqualsBytes(byte[] bytes, Charset charset, boolean base64) {
		if (base64) {
			bytes = Base64.getDecoder().decode(new String(bytes, StandardCharsets.US_ASCII));
		}
		assertEquals(BODY, new String(bytes, charset));
	}

	private ConcreteHandler newConcreteHandler(Charset charset, boolean base64) {
		return new ConcreteHandler(facade, formatter, "type/subtype", charset, base64);
	}
}
