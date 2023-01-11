package br.pro.hashi.sdx.rest.transform.simple;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import br.pro.hashi.sdx.rest.transform.Assembler;

class SimpleAssemblerTest {
	private Assembler a;

	@BeforeEach
	void setUp() {
		a = new SimpleAssembler() {
			@Override
			public byte[] toBytes(Object body) {
				return body.toString().getBytes(StandardCharsets.US_ASCII);
			}
		};
	}

	@Test
	void toStreamCallsToBytes() throws IOException {
		Object body = new Object() {
			@Override
			public String toString() {
				return "body";
			}
		};
		InputStream stream = a.toStream(body);
		byte[] bytes = new byte[4];
		stream.read(bytes, 0, 4);
		assertEquals(-1, stream.read());
		assertEquals("body", new String(bytes, StandardCharsets.US_ASCII));
	}
}
