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
import java.util.Locale;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedConstruction;

import br.pro.hashi.sdx.rest.coding.Coding;
import br.pro.hashi.sdx.rest.reflection.Cache;
import br.pro.hashi.sdx.rest.transform.Assembler;
import br.pro.hashi.sdx.rest.transform.Deserializer;
import br.pro.hashi.sdx.rest.transform.Disassembler;
import br.pro.hashi.sdx.rest.transform.Hint;
import br.pro.hashi.sdx.rest.transform.Serializer;
import br.pro.hashi.sdx.rest.transform.facade.Facade;

public abstract class BuilderTest {
	private MockedConstruction<Cache> cacheConstruction;
	private MockedConstruction<Facade> facadeConstruction;
	private Builder<?> b;
	private Cache cache;
	private Facade facade;

	@BeforeEach
	void setUp() {
		cacheConstruction = mockConstruction(Cache.class);
		facadeConstruction = mockConstruction(Facade.class);
		b = newInstance();
		cache = cacheConstruction.constructed().get(0);
		facade = facadeConstruction.constructed().get(0);
	}

	@AfterEach
	void tearDown() {
		facadeConstruction.close();
		cacheConstruction.close();
	}

	@Test
	void initializesWithCache() {
		assertEquals(cache, b.cache);
	}

	@Test
	void initializesWithFacade() {
		assertEquals(facade, b.facade);
	}

	@Test
	void initializesWithUTF8() {
		assertEquals(StandardCharsets.UTF_8, b.urlCharset);
	}

	@Test
	void initializesWithDefaultLocale() {
		assertEquals(Coding.LOCALE, b.locale);
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
		String contentType = "image/png";
		assertSame(b, b.withFallbackByteType(contentType));
		verify(facade).setFallbackByteType(contentType);
	}

	@Test
	void setsFallbackTextType() {
		String contentType = "application/xml";
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
			b.withUrlCharset(null);
		});
		assertEquals(StandardCharsets.UTF_8, b.urlCharset);
	}

	@Test
	void setsLocale() {
		if (Coding.LOCALE.equals(Locale.US)) {
			assertSame(b, b.withLocale(Locale.UK));
			assertEquals(Locale.UK, b.locale);
		} else {
			assertSame(b, b.withLocale(Locale.US));
			assertEquals(Locale.US, b.locale);
		}
	}

	@Test
	void doesNotSetLocale() {
		assertThrows(NullPointerException.class, () -> {
			b.withLocale(null);
		});
		assertEquals(Coding.LOCALE, b.locale);
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
