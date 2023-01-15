package br.pro.hashi.sdx.rest.transform.simple;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import br.pro.hashi.sdx.rest.transform.Hint;

class SimpleAssemblerTest {
	private SimpleAssembler a;
	private Object body;

	@BeforeEach
	void setUp() {
		a = new SimpleAssembler() {
			@Override
			public <T> byte[] toBytes(T body, Class<T> type) {
				return getBytesFrom(body);
			}

			@Override
			public <T> byte[] toBytes(T body, Hint<T> hint) {
				return getBytesFrom(body);
			}

			private <T> byte[] getBytesFrom(T body) {
				return body.toString().getBytes(StandardCharsets.US_ASCII);
			}
		};
		body = new Object() {
			@Override
			public String toString() {
				return "body";
			}
		};
	}

	@Test
	void writeCallsToBytes() {
		ByteArrayOutputStream stream = new ByteArrayOutputStream();
		a.write(body, stream);
		assertEqualsBody(stream);
	}

	@Test
	void writeCallsToBytesWithHint() {
		ByteArrayOutputStream stream = new ByteArrayOutputStream();
		a.write(body, new Hint<Object>() {}, stream);
		assertEqualsBody(stream);
	}

	private void assertEqualsBody(ByteArrayOutputStream stream) {
		assertEqualsBody(stream.toByteArray());
	}

	@Test
	void writeThrowsUncheckedIOExceptionIfStreamThrowsIOException() throws IOException {
		OutputStream stream = OutputStream.nullOutputStream();
		stream.close();
		assertThrows(UncheckedIOException.class, () -> {
			a.write(body, stream);
		});
	}

	@Test
	void toStreamCallsToBytes() throws IOException {
		InputStream stream = a.toStream(body);
		assertEqualsBody(stream);
	}

	@Test
	void toStreamCallsToBytesWithHint() throws IOException {
		InputStream stream = a.toStream(body, new Hint<Object>() {});
		assertEqualsBody(stream);
	}

	private void assertEqualsBody(InputStream stream) throws IOException {
		byte[] bytes = new byte[4];
		stream.readNBytes(bytes, 0, 4);
		assertEquals(-1, stream.read());
		assertEqualsBody(bytes);
		stream.close();
	}

	@Test
	void toBytesCallsToBytes() {
		byte[] bytes = a.toBytes(body);
		assertEqualsBody(bytes);
	}

	@Test
	void toBytesCallsToBytesWithHint() {
		byte[] bytes = a.toBytes(body, new Hint<Object>() {});
		assertEqualsBody(bytes);
	}

	private void assertEqualsBody(byte[] bytes) {
		assertEquals("body", new String(bytes, StandardCharsets.US_ASCII));
	}
}
