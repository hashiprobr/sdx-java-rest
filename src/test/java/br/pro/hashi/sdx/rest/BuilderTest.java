package br.pro.hashi.sdx.rest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockConstruction;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;

import java.nio.charset.StandardCharsets;
import java.util.Locale;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedConstruction;
import org.mockito.MockedStatic;

import br.pro.hashi.sdx.rest.constant.Defaults;
import br.pro.hashi.sdx.rest.reflection.ParserFactory;
import br.pro.hashi.sdx.rest.transform.Assembler;
import br.pro.hashi.sdx.rest.transform.Deserializer;
import br.pro.hashi.sdx.rest.transform.Disassembler;
import br.pro.hashi.sdx.rest.transform.Hint;
import br.pro.hashi.sdx.rest.transform.Serializer;
import br.pro.hashi.sdx.rest.transform.manager.TransformManager;

public abstract class BuilderTest {
	private MockedStatic<ParserFactory> cacheStatic;
	private MockedConstruction<TransformManager> managerConstruction;
	private Builder<?> b;
	private ParserFactory factory;
	private TransformManager manager;

	@BeforeEach
	void setUp() {
		factory = mock(ParserFactory.class);
		cacheStatic = mockStatic(ParserFactory.class);
		cacheStatic.when(() -> ParserFactory.getInstance()).thenReturn(factory);
		managerConstruction = mockConstruction(TransformManager.class);
		b = newInstance();
		manager = managerConstruction.constructed().get(0);
	}

	@AfterEach
	void tearDown() {
		managerConstruction.close();
		cacheStatic.close();
	}

	@Test
	void initializesWithCache() {
		assertEquals(factory, b.parserFactory);
	}

	@Test
	void initializesWithManager() {
		assertEquals(manager, b.manager);
	}

	@Test
	void initializesWithUTF8() {
		assertEquals(StandardCharsets.UTF_8, b.urlCharset);
	}

	@Test
	void initializesWithDefaultLocale() {
		assertEquals(Defaults.LOCALE, b.locale);
	}

	@Test
	void initializesWithRedirection() {
		assertTrue(b.redirection);
	}

	@Test
	void initializesWithCompression() {
		assertTrue(b.compression);
	}

	@Test
	void addsBinary() {
		assertSame(b, b.withBinary(Object.class));
		verify(manager).addBinary(Object.class);
	}

	@Test
	void addsBinaryWithHint() {
		Hint<Object> hint = new Hint<Object>() {};
		assertSame(b, b.withBinary(hint));
		verify(manager).addBinary(hint);
	}

	@Test
	void putsDefaultAssembler() {
		String contentType = "image/png";
		assertSame(b, b.withDefaultAssembler(contentType));
		verify(manager).putDefaultAssembler(contentType);
	}

	@Test
	void putsAssembler() {
		String contentType = "image/png";
		Assembler assembler = mock(Assembler.class);
		assertSame(b, b.withAssembler(contentType, assembler));
		verify(manager).putAssembler(contentType, assembler);
	}

	@Test
	void putsDefaultDisassembler() {
		String contentType = "image/png";
		assertSame(b, b.withDefaultDisassembler(contentType));
		verify(manager).putDefaultDisassembler(contentType);
	}

	@Test
	void putsDisassembler() {
		String contentType = "image/png";
		Disassembler disassembler = mock(Disassembler.class);
		assertSame(b, b.withDisassembler(contentType, disassembler));
		verify(manager).putDisassembler(contentType, disassembler);
	}

	@Test
	void putsDefaultSerializer() {
		String contentType = "application/xml";
		assertSame(b, b.withDefaultSerializer(contentType));
		verify(manager).putDefaultSerializer(contentType);
	}

	@Test
	void putsSerializer() {
		String contentType = "application/xml";
		Serializer serializer = mock(Serializer.class);
		assertSame(b, b.withSerializer(contentType, serializer));
		verify(manager).putSerializer(contentType, serializer);
	}

	@Test
	void putsDefaultDeserializer() {
		String contentType = "application/xml";
		assertSame(b, b.withDefaultDeserializer(contentType));
		verify(manager).putDefaultDeserializer(contentType);
	}

	@Test
	void putsDeserializer() {
		String contentType = "application/xml";
		Deserializer deserializer = mock(Deserializer.class);
		assertSame(b, b.withDeserializer(contentType, deserializer));
		verify(manager).putDeserializer(contentType, deserializer);
	}

	@Test
	void setsFallbackByteType() {
		String contentType = "image/png";
		assertSame(b, b.withFallbackByteType(contentType));
		verify(manager).setFallbackByteType(contentType);
	}

	@Test
	void setsFallbackTextType() {
		String contentType = "application/xml";
		assertSame(b, b.withFallbackTextType(contentType));
		verify(manager).setFallbackTextType(contentType);
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
		if (Defaults.LOCALE.equals(Locale.US)) {
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
		assertEquals(Defaults.LOCALE, b.locale);
	}

	@Test
	void setsRedirection() {
		assertSame(b, b.withoutRedirection());
		assertFalse(b.redirection);
	}

	@Test
	void setsCompression() {
		assertSame(b, b.withoutCompression());
		assertFalse(b.compression);
	}

	protected abstract Builder<?> newInstance();
}
