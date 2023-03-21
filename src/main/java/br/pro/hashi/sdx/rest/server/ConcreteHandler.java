package br.pro.hashi.sdx.rest.server;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.lang.reflect.Type;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;

import org.eclipse.jetty.http.HttpFields;
import org.eclipse.jetty.server.Dispatcher;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.handler.ErrorHandler;

import br.pro.hashi.sdx.rest.coding.Media;
import br.pro.hashi.sdx.rest.transform.Serializer;
import br.pro.hashi.sdx.rest.transform.facade.Facade;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

class ConcreteHandler extends ErrorHandler {
	private final Facade facade;
	private final ErrorFormatter formatter;
	private final Type type;
	private final String contentType;
	private final Charset charset;
	private final boolean base64;

	ConcreteHandler(Facade facade, ErrorFormatter formatter, String contentType, Charset charset, boolean base64) {
		this.facade = facade;
		this.formatter = formatter;
		this.type = formatter.getReturnType();
		this.contentType = contentType;
		this.charset = charset;
		this.base64 = base64;
	}

	ErrorFormatter getFormatter() {
		return formatter;
	}

	String getContentType() {
		return contentType;
	}

	Charset getCharset() {
		return charset;
	}

	boolean isBase64() {
		return base64;
	}

	@Override
	public ByteBuffer badMessageError(int status, String reason, HttpFields.Mutable fields) {
		ByteBuffer buffer;
		try {
			Object actual = formatter.format(status, reason);
			String contentType = this.contentType;

			contentType = facade.cleanForSerializing(this.contentType, actual);
			Serializer serializer = facade.getSerializer(contentType);
			ByteArrayOutputStream output = new ByteArrayOutputStream();
			contentType = "%s;charset=%s".formatted(contentType, charset.name());
			if (base64) {
				contentType = "%s;base64".formatted(contentType);
			}

			fields.add("Content-Type", contentType);
			OutputStream stream;
			if (base64) {
				stream = Media.encode(output);
			} else {
				stream = output;
			}
			OutputStreamWriter writer = new OutputStreamWriter(stream, charset);

			serializer.write(actual, type, writer);
			buffer = ByteBuffer.wrap(output.toByteArray());
		} catch (RuntimeException error) {
			error.printStackTrace();
			buffer = null;
		}
		return buffer;
	}

	@Override
	public void handle(String target, Request baseRequest, HttpServletRequest request, HttpServletResponse response) throws IOException {
		String message = (String) request.getAttribute(Dispatcher.ERROR_MESSAGE);
		if (message == null) {
			message = baseRequest.getResponse().getReason();
		}

		Object actual = formatter.format(response.getStatus(), message);
		String contentType = this.contentType;

		contentType = facade.cleanForSerializing(this.contentType, actual);
		Serializer serializer = facade.getSerializer(contentType);
		contentType = "%s;charset=%s".formatted(contentType, charset.name());
		if (base64) {
			contentType = "%s;base64".formatted(contentType);
		}

		response.setContentType(contentType);
		OutputStream stream = response.getOutputStream();
		if (base64) {
			stream = Media.encode(stream);
		}
		OutputStreamWriter writer = new OutputStreamWriter(stream, charset);

		serializer.write(actual, type, writer);
	}
}
