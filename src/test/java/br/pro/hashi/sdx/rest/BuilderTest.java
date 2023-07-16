package br.pro.hashi.sdx.rest;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Locale;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.MockitoAnnotations;

import br.pro.hashi.sdx.rest.coding.PathCoder;
import br.pro.hashi.sdx.rest.constant.Defaults;
import br.pro.hashi.sdx.rest.transform.Assembler;
import br.pro.hashi.sdx.rest.transform.Deserializer;
import br.pro.hashi.sdx.rest.transform.Disassembler;
import br.pro.hashi.sdx.rest.transform.Serializer;
import br.pro.hashi.sdx.rest.transform.manager.TransformManager;

public abstract class BuilderTest {
	private AutoCloseable mocks;
	private @Mock PathCoder pathCoder;
	private MockedStatic<PathCoder> pathCoderStatic;
	private MockedStatic<TransformManager> managerStatic;
	private Builder<?> b;

	protected @Mock TransformManager managerBase;
	protected @Mock TransformManager managerCopy;

	@BeforeEach
	void setUp() {
		mocks = MockitoAnnotations.openMocks(this);

		when(pathCoder.stripEndingSlashes(any(String.class))).thenAnswer((invocation) -> {
			return invocation.getArgument(0);
		});
		when(pathCoder.splitAndDecode(any(String.class), any(Charset.class))).thenAnswer((invocation) -> {
			String path = invocation.getArgument(0);
			String[] items = path.substring(1).split("/", -1);
			for (int i = 0; i < items.length; i++) {
				items[i] = "pd(%s)".formatted(items[i]);
			}
			return items;
		});
		when(pathCoder.recode(any(String.class), any(Charset.class))).thenAnswer((invocation) -> {
			String path = invocation.getArgument(0);
			if (path.indexOf('%') != -1) {
				throw new IllegalArgumentException();
			}
			return "pr(%s)".formatted(path);
		});

		pathCoderStatic = mockStatic(PathCoder.class);
		pathCoderStatic.when(() -> PathCoder.getInstance()).thenReturn(pathCoder);

		managerStatic = mockStatic(TransformManager.class);
		managerStatic.when(() -> TransformManager.newInstance()).thenReturn(managerBase);
		managerStatic.when(() -> TransformManager.newInstance(managerBase)).thenReturn(managerCopy);

		b = newInstance();
	}

	@AfterEach
	void tearDown() {
		close();
		managerStatic.close();
		pathCoderStatic.close();
		assertDoesNotThrow(() -> {
			mocks.close();
		});
	}

	@Test
	void initializesWithDefaultLocale() {
		assertEquals(Defaults.LOCALE, b.locale);
	}

	@Test
	void initializesWithDefaultUrlCharset() {
		assertEquals(StandardCharsets.UTF_8, b.urlCharset);
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
	void putsDefaultAssembler() {
		String contentType = "image/png";
		assertSame(b, b.withDefaultAssembler(contentType));
		verify(managerBase).putDefaultAssembler(contentType);
	}

	@Test
	void putsAssembler() {
		String contentType = "image/png";
		Assembler assembler = mock(Assembler.class);
		assertSame(b, b.withAssembler(contentType, assembler));
		verify(managerBase).putAssembler(contentType, assembler);
	}

	@Test
	void removesAssembler() {
		String contentType = "image/png";
		assertSame(b, b.withoutAssembler(contentType));
		verify(managerBase).removeAssembler(contentType);
	}

	@Test
	void putsDefaultDisassembler() {
		String contentType = "image/png";
		assertSame(b, b.withDefaultDisassembler(contentType));
		verify(managerBase).putDefaultDisassembler(contentType);
	}

	@Test
	void putsDisassembler() {
		String contentType = "image/png";
		Disassembler disassembler = mock(Disassembler.class);
		assertSame(b, b.withDisassembler(contentType, disassembler));
		verify(managerBase).putDisassembler(contentType, disassembler);
	}

	@Test
	void removesDisassembler() {
		String contentType = "image/png";
		assertSame(b, b.withoutDisassembler(contentType));
		verify(managerBase).removeDisassembler(contentType);
	}

	@Test
	void putsDefaultSerializer() {
		String contentType = "application/xml";
		assertSame(b, b.withDefaultSerializer(contentType));
		verify(managerBase).putDefaultSerializer(contentType);
	}

	@Test
	void putsSerializer() {
		String contentType = "application/xml";
		Serializer serializer = mock(Serializer.class);
		assertSame(b, b.withSerializer(contentType, serializer));
		verify(managerBase).putSerializer(contentType, serializer);
	}

	@Test
	void removesSerializer() {
		String contentType = "application/xml";
		assertSame(b, b.withoutSerializer(contentType));
		verify(managerBase).removeSerializer(contentType);
	}

	@Test
	void putsDefaultDeserializer() {
		String contentType = "application/xml";
		assertSame(b, b.withDefaultDeserializer(contentType));
		verify(managerBase).putDefaultDeserializer(contentType);
	}

	@Test
	void putsDeserializer() {
		String contentType = "application/xml";
		Deserializer deserializer = mock(Deserializer.class);
		assertSame(b, b.withDeserializer(contentType, deserializer));
		verify(managerBase).putDeserializer(contentType, deserializer);
	}

	@Test
	void removesDeserializer() {
		String contentType = "application/xml";
		assertSame(b, b.withoutDeserializer(contentType));
		verify(managerBase).removeDeserializer(contentType);
	}

	@Test
	void addsBinary() {
		assertSame(b, b.withBinary(Object.class));
		verify(managerBase).addBinary(Object.class);
	}

	@Test
	void addsBinaryWithHint() {
		assertSame(b, b.withBinary(new Hint<Object>() {}));
		verify(managerBase).addBinary(Object.class);
	}

	@Test
	void doesNotAddBinaryWithNullType() {
		assertThrows(NullPointerException.class, () -> {
			b.withBinary((Class<?>) null);
		});
	}

	@Test
	void doesNotAddBinaryWithNullHint() {
		assertThrows(NullPointerException.class, () -> {
			b.withBinary((Hint<?>) null);
		});
	}

	@Test
	void removesBinary() {
		assertSame(b, b.withoutBinary(Object.class));
		verify(managerBase).removeBinary(Object.class);
	}

	@Test
	void removesBinaryWithHint() {
		assertSame(b, b.withoutBinary(new Hint<Object>() {}));
		verify(managerBase).removeBinary(Object.class);
	}

	@Test
	void removesBinaryWithNullType() {
		assertSame(b, b.withoutBinary((Class<?>) null));
		verify(managerBase, times(0)).removeBinary(any());
	}

	@Test
	void removesBinaryWithNullHint() {
		assertSame(b, b.withoutBinary((Hint<?>) null));
		verify(managerBase, times(0)).removeBinary(any());
	}

	@Test
	void setsBinaryFallbackType() {
		String contentType = "image/png";
		assertSame(b, b.withBinaryFallbackType(contentType));
		verify(managerBase).setBinaryFallbackType(contentType);
	}

	@Test
	void unsetsBinaryFallbackType() {
		assertSame(b, b.withoutBinaryFallbackType());
		verify(managerBase).unsetBinaryFallbackType();
	}

	@Test
	void setsFallbackType() {
		String contentType = "application/xml";
		assertSame(b, b.withFallbackType(contentType));
		verify(managerBase).setFallbackType(contentType);
	}

	@Test
	void unsetsFallbackType() {
		assertSame(b, b.withoutFallbackType());
		verify(managerBase).unsetFallbackType();
	}

	@Test
	void setsLocale() {
		assertSame(b, b.withLocale(Locale.ROOT));
		assertEquals(Locale.ROOT, b.locale);
		assertSame(b, b.withoutLocale());
		assertEquals(Defaults.LOCALE, b.locale);
	}

	@Test
	void doesNotSetNullLocale() {
		assertThrows(NullPointerException.class, () -> {
			b.withLocale(null);
		});
	}

	@Test
	void setsUrlCharset() {
		assertSame(b, b.withUrlCharset(StandardCharsets.ISO_8859_1));
		assertEquals(StandardCharsets.ISO_8859_1, b.urlCharset);
		assertSame(b, b.withoutUrlCharset());
		assertEquals(StandardCharsets.UTF_8, b.urlCharset);
	}

	@Test
	void doesNotSetNullUrlCharset() {
		assertThrows(NullPointerException.class, () -> {
			b.withUrlCharset(null);
		});
	}

	@Test
	void setsRedirection() {
		assertSame(b, b.withoutRedirection());
		assertFalse(b.redirection);
		assertSame(b, b.withRedirection());
		assertTrue(b.redirection);
	}

	@Test
	void setsCompression() {
		assertSame(b, b.withoutCompression());
		assertFalse(b.compression);
		assertSame(b, b.withCompression());
		assertTrue(b.compression);
	}

	protected abstract Builder<?> newInstance();

	protected abstract void close();
}
