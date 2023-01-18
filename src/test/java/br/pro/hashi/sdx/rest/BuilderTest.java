package br.pro.hashi.sdx.rest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockConstruction;
import static org.mockito.Mockito.verify;

import java.nio.charset.StandardCharsets;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedConstruction;

import br.pro.hashi.sdx.rest.transform.Assembler;
import br.pro.hashi.sdx.rest.transform.Deserializer;
import br.pro.hashi.sdx.rest.transform.Disassembler;
import br.pro.hashi.sdx.rest.transform.Hint;
import br.pro.hashi.sdx.rest.transform.Serializer;
import br.pro.hashi.sdx.rest.transform.facade.Facade;

public abstract class BuilderTest {
	private MockedConstruction<Facade> construction;
	private Builder<?> b;
	private Facade facade;

	@BeforeEach
	void setUp() {
		construction = mockConstruction(Facade.class);
		b = newInstance();
		facade = construction.constructed().get(0);
	}

	@AfterEach
	void tearDown() {
		construction.close();
	}

	@Test
	void initializesWithFacade() {
		assertEquals(facade, b.facade);
	}

	@Test
	void initializesWithUtf8() {
		assertEquals(StandardCharsets.UTF_8, b.urlCharset);
	}

	@Test
	void initializesWithNull() {
		assertEquals(null, b.none);
	}

	@Test
	void initializesWithoutRedirection() {
		assertFalse(b.redirection);
	}

	@Test
	void initializesWithCompression() {
		assertTrue(b.compression);
	}

	@Test
	void addsBinary() {
		assertSame(b, b.withBinary(Object.class));
		verify(facade).addBinary(Object.class);
	}

	@Test
	void addsBinaryWithHint() {
		Hint<Object> hint = new Hint<Object>() {};
		assertSame(b, b.withBinary(hint));
		verify(facade).addBinary(hint);
	}

	@Test
	void putsAssembler() {
		String contentType = "image/png";
		Assembler assembler = mock(Assembler.class);
		assertSame(b, b.withAssembler(contentType, assembler));
		verify(facade).putAssembler(contentType, assembler);
	}

	@Test
	void putsDisassembler() {
		String contentType = "image/png";
		Disassembler disassembler = mock(Disassembler.class);
		assertSame(b, b.withDisassembler(contentType, disassembler));
		verify(facade).putDisassembler(contentType, disassembler);
	}

	@Test
	void putsSerializer() {
		String contentType = "application/xml";
		Serializer serializer = mock(Serializer.class);
		assertSame(b, b.withSerializer(contentType, serializer));
		verify(facade).putSerializer(contentType, serializer);
	}

	@Test
	void putsDeserializer() {
		String contentType = "application/xml";
		Deserializer deserializer = mock(Deserializer.class);
		assertSame(b, b.withDeserializer(contentType, deserializer));
		verify(facade).putDeserializer(contentType, deserializer);
	}

	@Test
	void setsFallbackByteType() {
		String contentType = "fallback";
		assertSame(b, b.withFallbackByteType(contentType));
		verify(facade).setFallbackByteType(contentType);
	}

	@Test
	void setsFallbackTextType() {
		String contentType = "fallback";
		assertSame(b, b.withFallbackTextType(contentType));
		verify(facade).setFallbackTextType(contentType);
	}

	@Test
	void setsUrlCharset() {
		assertSame(b, b.withUrlCharset(StandardCharsets.ISO_8859_1));
		assertEquals(StandardCharsets.ISO_8859_1, b.urlCharset);
	}

	@Test
	void doesNotSetUrlCharset() {
		assertThrows(NullPointerException.class, () -> {
			assertSame(b, b.withUrlCharset(null));
		});
		assertEquals(StandardCharsets.UTF_8, b.urlCharset);
	}

	@Test
	void setsNullBody() {
		assertSame(b, b.withNullBody());
		assertEquals("", b.none);
	}

	@Test
	void setsRedirection() {
		assertSame(b, b.withRedirection());
		assertTrue(b.redirection);
	}

	@Test
	void setsCompression() {
		assertSame(b, b.withoutCompression());
		assertFalse(b.compression);
	}

	protected abstract Builder<?> newInstance();
}
