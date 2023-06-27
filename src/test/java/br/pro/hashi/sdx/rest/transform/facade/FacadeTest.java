package br.pro.hashi.sdx.rest.transform.facade;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockConstruction;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.StringReader;
import java.io.Writer;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.MockedConstruction;
import org.mockito.MockedStatic;

import br.pro.hashi.sdx.rest.coding.MediaCoder;
import br.pro.hashi.sdx.rest.reflection.ParserFactory;
import br.pro.hashi.sdx.rest.transform.Assembler;
import br.pro.hashi.sdx.rest.transform.Deserializer;
import br.pro.hashi.sdx.rest.transform.Disassembler;
import br.pro.hashi.sdx.rest.transform.Hint;
import br.pro.hashi.sdx.rest.transform.Serializer;
import br.pro.hashi.sdx.rest.transform.exception.TypeException;

class FacadeTest {
	private MediaCoder coder;
	private MockedConstruction<DefaultAssembler> assemblerConstruction;
	private MockedConstruction<DefaultDisassembler> disassemblerConstruction;
	private MockedConstruction<DefaultSerializer> serializerConstruction;
	private MockedConstruction<DefaultDeserializer> deserializerConstruction;
	private Facade f;

	@BeforeEach
	void setUp() {
		coder = mock(MediaCoder.class);
		assemblerConstruction = mockConstruction(DefaultAssembler.class);
		disassemblerConstruction = mockConstruction(DefaultDisassembler.class);
		serializerConstruction = mockConstruction(DefaultSerializer.class);
		deserializerConstruction = mockConstruction(DefaultDeserializer.class);
		f = new Facade(ParserFactory.getInstance());
	}

	@AfterEach
	void tearDown() {
		deserializerConstruction.close();
		serializerConstruction.close();
		disassemblerConstruction.close();
		assemblerConstruction.close();
	}

	@Test
	void initializesWithByteArrayAsBinary() {
		assertTrue(f.isBinary(byte[].class));
	}

	@Test
	void initializesWithByteArrayAsBinaryWithHint() {
		assertTrue(f.isBinary(new Hint<byte[]>() {}.getType()));
	}

	@Test
	void initializesWithInputStreamAsBinary() {
		assertTrue(f.isBinary(InputStream.class));
	}

	@Test
	void initializesWithInputStreamAsBinaryWithHint() {
		assertTrue(f.isBinary(new Hint<InputStream>() {}.getType()));
	}

	@Test
	void initializesWithByteArrayInputStreamAsBinary() {
		assertTrue(f.isBinary(ByteArrayInputStream.class));
	}

	@Test
	void initializesWithByteArrayInputStreamAsBinaryWithHint() {
		assertTrue(f.isBinary(new Hint<ByteArrayInputStream>() {}.getType()));
	}

	@Test
	void initializesWithoutObjectAsBinary() {
		assertFalse(f.isBinary(Object.class));
	}

	@Test
	void initializesWithoutObjectAsBinaryWithHint() {
		assertFalse(f.isBinary(new Hint<Object>() {}.getType()));
	}

	@Test
	void initializesWithoutParameterizedTypeAsBinaryWithHint() {
		assertFalse(f.isBinary(new Hint<List<Map<Integer, Double>>>() {}.getType()));
	}

	@Test
	void initializesWithoutNullAsBinary() {
		assertFalse(f.isBinary(null));
	}

	@Test
	void initializesWithTxtExtension() {
		assertEquals("text/plain", f.getExtensionType("txt"));
	}

	@Test
	void initializesWithoutXmlExtension() {
		assertNull(f.getExtensionType("xml"));
	}

	@Test
	void initializesWithoutPngExtension() {
		assertNull(f.getExtensionType("png"));
	}

	@Test
	void initializesWithoutNullExtension() {
		assertNull(f.getExtensionType(null));
	}

	@Test
	void initializesWithOctetAssembler() {
		assertInstanceOf(DefaultAssembler.class, f.getAssembler("application/octet-stream"));
	}

	@Test
	void initializesWithoutPngAssembler() {
		assertThrows(TypeException.class, () -> {
			f.getAssembler("image/png");
		});
	}

	@Test
	void initializesWithoutNullAssembler() {
		assertThrows(TypeException.class, () -> {
			f.getAssembler(null);
		});
	}

	@Test
	void initializesWithOctetDisassembler() {
		assertInstanceOf(DefaultDisassembler.class, f.getDisassembler("application/octet-stream"));
	}

	@Test
	void initializesWithoutPngDisassembler() {
		assertThrows(TypeException.class, () -> {
			f.getDisassembler("image/png");
		});
	}

	@Test
	void initializesWithoutNullDisassembler() {
		assertThrows(TypeException.class, () -> {
			f.getDisassembler(null);
		});
	}

	@Test
	void initializesWithPlainSerializer() {
		assertInstanceOf(DefaultSerializer.class, f.getSerializer("text/plain"));
	}

	@Test
	void initializesWithoutXmlSerializer() {
		assertThrows(TypeException.class, () -> {
			f.getSerializer("application/xml");
		});
	}

	@Test
	void initializesWithoutNullSerializer() {
		assertThrows(TypeException.class, () -> {
			f.getSerializer(null);
		});
	}

	@Test
	void initializesWithPlainDeserializer() {
		assertInstanceOf(DefaultDeserializer.class, f.getDeserializer("text/plain"));
	}

	@Test
	void initializesWithoutXmlDeserializer() {
		assertThrows(TypeException.class, () -> {
			f.getDeserializer("application/xml");
		});
	}

	@Test
	void initializesWithoutNullDeserializer() {
		assertThrows(TypeException.class, () -> {
			f.getDeserializer(null);
		});
	}

	@Test
	void addsObjectAsBinary() {
		f.addBinary(Object.class);
		assertTrue(f.isBinary(Object.class));
	}

	@Test
	void addsObjectAsBinaryWithHint() {
		f.addBinary(new Hint<Object>() {});
		assertTrue(f.isBinary(new Hint<Object>() {}.getType()));
	}

	@Test
	void addsParameterizedTypeAsBinary() {
		f.addBinary(new Hint<List<Map<Integer, Double>>>() {});
		assertTrue(f.isBinary(new Hint<List<Map<Integer, Double>>>() {}.getType()));
	}

	@Test
	void doesNotAddNullAsBinary() {
		assertThrows(NullPointerException.class, () -> {
			f.addBinary((Class<?>) null);
		});
		assertFalse(f.isBinary(null));
	}

	@Test
	void doesNotAddNullAsBinaryWithHint() {
		assertThrows(NullPointerException.class, () -> {
			f.addBinary((Hint<?>) null);
		});
		assertFalse(f.isBinary(null));
	}

	@Test
	void addsXmlExtension() {
		try (MockedStatic<MediaCoder> media = mockStatic(MediaCoder.class)) {
			String contentType = "text/plain";
			when(coder.strip(contentType)).thenReturn(contentType);
			media.when(() -> MediaCoder.getInstance()).thenReturn(coder);
			f.putExtension("xml", contentType);
			assertEquals(contentType, f.getExtensionType("xml"));
		}
	}

	@Test
	void doesNotAddXmlExtensionIfItIsNull() {
		assertThrows(NullPointerException.class, () -> {
			f.putExtension(null, "text/plain");
		});
		assertNull(f.getExtensionType(null));
	}

	@Test
	void doesNotAddXmlExtensionIfItIsBlank() {
		assertThrows(IllegalArgumentException.class, () -> {
			f.putExtension(" \t\n", "text/plain");
		});
		assertNull(f.getExtensionType("xml"));
	}

	@Test
	void doesNotAddXmlExtensionIfTypeIsNull() {
		assertThrows(NullPointerException.class, () -> {
			f.putExtension("xml", null);
		});
		assertNull(f.getExtensionType("xml"));
	}

	@Test
	void doesNotAddXmlExtensionIfStripReturnsNull() {
		try (MockedStatic<MediaCoder> media = mockStatic(MediaCoder.class)) {
			String contentType = "text/plain";
			when(coder.strip(contentType)).thenReturn(null);
			media.when(() -> MediaCoder.getInstance()).thenReturn(coder);
			assertThrows(IllegalArgumentException.class, () -> {
				f.putExtension("xml", contentType);
			});
			assertNull(f.getExtensionType("xml"));
		}
	}

	@Test
	void doesNotAddXmlExtensionIfTypeIsInvalid() {
		try (MockedStatic<MediaCoder> media = mockStatic(MediaCoder.class)) {
			String contentType = "application/xml";
			when(coder.strip(contentType)).thenReturn(contentType);
			media.when(() -> MediaCoder.getInstance()).thenReturn(coder);
			assertThrows(IllegalArgumentException.class, () -> {
				f.putExtension("xml", contentType);
			});
			assertNull(f.getExtensionType("xml"));
		}
	}

	@Test
	void addsPngExtension() {
		try (MockedStatic<MediaCoder> media = mockStatic(MediaCoder.class)) {
			String contentType = "application/octet-stream";
			when(coder.strip(contentType)).thenReturn(contentType);
			media.when(() -> MediaCoder.getInstance()).thenReturn(coder);
			f.putExtension("png", contentType);
			assertEquals(contentType, f.getExtensionType("png"));
		}
	}

	@Test
	void doesNotAddPngExtensionIfItIsNull() {
		assertThrows(NullPointerException.class, () -> {
			f.putExtension(null, "application/octet-stream");
		});
		assertNull(f.getExtensionType(null));
	}

	@Test
	void doesNotAddPngExtensionIfItIsBlank() {
		assertThrows(IllegalArgumentException.class, () -> {
			f.putExtension(" \t\n", "application/octet-stream");
		});
		assertNull(f.getExtensionType("png"));
	}

	@Test
	void doesNotAddPngExtensionIfTypeIsNull() {
		assertThrows(NullPointerException.class, () -> {
			f.putExtension("png", null);
		});
		assertNull(f.getExtensionType("png"));
	}

	@Test
	void doesNotAddPngExtensionIfStripReturnsNull() {
		try (MockedStatic<MediaCoder> media = mockStatic(MediaCoder.class)) {
			String contentType = "application/octet-stream";
			when(coder.strip(contentType)).thenReturn(null);
			media.when(() -> MediaCoder.getInstance()).thenReturn(coder);
			assertThrows(IllegalArgumentException.class, () -> {
				f.putExtension("png", contentType);
			});
			assertNull(f.getExtensionType("png"));
		}
	}

	@Test
	void doesNotAddPngExtensionIfTypeIsInvalid() {
		try (MockedStatic<MediaCoder> media = mockStatic(MediaCoder.class)) {
			String contentType = "image/png";
			when(coder.strip(contentType)).thenReturn(contentType);
			media.when(() -> MediaCoder.getInstance()).thenReturn(coder);
			assertThrows(IllegalArgumentException.class, () -> {
				f.putExtension("png", contentType);
			});
			assertNull(f.getExtensionType("png"));
		}
	}

	@Test
	void getsAssemblerType() {
		String contentType = "image/png";
		assertEquals(contentType, f.getAssemblerType(contentType, null, Object.class));
	}

	@Test
	void getsAssemblerTypeIfBodyIsByteArray() {
		assertEquals("application/octet-stream", getAssemblerType(null, new byte[] {}));
	}

	@Test
	void getsAssemblerTypeIfBodyIsInputStream() {
		assertEquals("application/octet-stream", getAssemblerType(null, new ByteArrayInputStream(new byte[] {})));
	}

	@Test
	void getsAssemblerTypeIfBodyIsConsumer() {
		Consumer<OutputStream> consumer = (stream) -> {};
		assertEquals("application/octet-stream", f.getAssemblerType(null, consumer, new Hint<Consumer<OutputStream>>() {}.getType()));
	}

	@Test
	void getsAssemblerTypeIfBodyIsObject() {
		try (MockedStatic<MediaCoder> media = mockStatic(MediaCoder.class)) {
			String fallbackByteType = "image/png";
			when(coder.strip(fallbackByteType)).thenReturn(fallbackByteType);
			media.when(() -> MediaCoder.getInstance()).thenReturn(coder);
			f.setFallbackByteType(fallbackByteType);
			assertEquals(fallbackByteType, getAssemblerType(null, new Object()));
		}
	}

	@Test
	void doesNotCleanForAssembling() {
		Object body = new Object();
		assertThrows(IllegalArgumentException.class, () -> {
			getAssemblerType(null, body);
		});
	}

	private String getAssemblerType(String contentType, Object body) {
		return f.getAssemblerType(contentType, body, body.getClass());
	}

	@Test
	void getsDisassemblerType() {
		String contentType = "image/png";
		assertEquals(contentType, f.getDisassemblerType(contentType, null));
	}

	@Test
	void getsDisassemblerTypeIfTypeEqualsByteArray() {
		assertEquals("application/octet-stream", f.getDisassemblerType(null, byte[].class));
	}

	@Test
	void getsDisassemblerTypeIfTypeEqualsInputStream() {
		assertEquals("application/octet-stream", f.getDisassemblerType(null, InputStream.class));
	}

	@Test
	void getsDisassemblerTypeIfTypeEqualsObject() {
		try (MockedStatic<MediaCoder> media = mockStatic(MediaCoder.class)) {
			String fallbackByteType = "image/png";
			when(coder.strip(fallbackByteType)).thenReturn(fallbackByteType);
			media.when(() -> MediaCoder.getInstance()).thenReturn(coder);
			f.setFallbackByteType(fallbackByteType);
			assertEquals(fallbackByteType, f.getDisassemblerType(null, Object.class));
		}
	}

	@Test
	void doesNotCleanForDisassembling() {
		assertThrows(IllegalArgumentException.class, () -> {
			f.getDisassemblerType(null, Object.class);
		});
	}

	@Test
	void getsSerializerType() {
		String contentType = "application/xml";
		assertEquals(contentType, f.getSerializerType(contentType, null, Object.class));
	}

	@Test
	void getsSerializerTypeIfBodyIsBoolean() {
		assertEquals("text/plain", getSerializerType(null, false));
	}

	@Test
	void getsSerializerTypeIfBodyIsByte() {
		assertEquals("text/plain", getSerializerType(null, (byte) 0));
	}

	@Test
	void getsSerializerTypeIfBodyIsShort() {
		assertEquals("text/plain", getSerializerType(null, (short) 1));
	}

	@Test
	void getsSerializerTypeIfBodyIsInteger() {
		assertEquals("text/plain", getSerializerType(null, (int) 2));
	}

	@Test
	void getsSerializerTypeIfBodyIsLong() {
		assertEquals("text/plain", getSerializerType(null, 3L));
	}

	@Test
	void getsSerializerTypeIfBodyIsFloat() {
		assertEquals("text/plain", getSerializerType(null, 4.5F));
	}

	@Test
	void getsSerializerTypeIfBodyIsDouble() {
		assertEquals("text/plain", getSerializerType(null, 6.7));
	}

	@Test
	void getsSerializerTypeIfBodyIsString() {
		assertEquals("text/plain", getSerializerType(null, ""));
	}

	@Test
	void getsSerializerTypeIfBodyIsReader() {
		assertEquals("text/plain", getSerializerType(null, new StringReader("")));
	}

	@Test
	void getsSerializerTypeIfBodyIsConsumer() {
		Consumer<Writer> consumer = (writer) -> {};
		assertEquals("text/plain", f.getSerializerType(null, consumer, new Hint<Consumer<Writer>>() {}.getType()));
	}

	@Test
	void getsSerializerTypeIfBodyIsObject() {
		try (MockedStatic<MediaCoder> media = mockStatic(MediaCoder.class)) {
			String fallbackTextType = "application/xml";
			when(coder.strip(fallbackTextType)).thenReturn(fallbackTextType);
			media.when(() -> MediaCoder.getInstance()).thenReturn(coder);
			f.setFallbackTextType(fallbackTextType);
			assertEquals(fallbackTextType, getSerializerType(null, new Object()));
		}
	}

	@Test
	void doesNotCleanForSerializingIfBodyIsNeither() {
		Object body = new Object();
		assertThrows(IllegalArgumentException.class, () -> {
			getSerializerType(null, body);
		});
	}

	@Test
	void doesNotCleanForSerializingIfBodyIsNull() {
		assertThrows(IllegalArgumentException.class, () -> {
			f.getSerializerType(null, null, Object.class);
		});
	}

	private String getSerializerType(String contentType, Object body) {
		return f.getSerializerType(contentType, body, body.getClass());
	}

	@Test
	void getsDeserializerType() {
		String contentType = "application/xml";
		assertEquals(contentType, f.getDeserializerType(contentType, null));
	}

	@Test
	void getsDeserializerTypeIfTypeEqualsBoolean() {
		assertEquals("text/plain", f.getDeserializerType(null, boolean.class));
	}

	@Test
	void getsDeserializerTypeIfTypeEqualsBooleanWithHint() {
		assertEquals("text/plain", f.getDeserializerType(null, new Hint<Boolean>() {}.getType()));
	}

	@Test
	void getsDeserializerTypeIfTypeEqualsByte() {
		assertEquals("text/plain", f.getDeserializerType(null, byte.class));
	}

	@Test
	void getsDeserializerTypeIfTypeEqualsByteWithHint() {
		assertEquals("text/plain", f.getDeserializerType(null, new Hint<Byte>() {}.getType()));
	}

	@Test
	void getsDeserializerTypeIfTypeEqualsShort() {
		assertEquals("text/plain", f.getDeserializerType(null, short.class));
	}

	@Test
	void getsDeserializerTypeIfTypeEqualsShortWithHint() {
		assertEquals("text/plain", f.getDeserializerType(null, new Hint<Short>() {}.getType()));
	}

	@Test
	void getsDeserializerTypeIfTypeEqualsInteger() {
		assertEquals("text/plain", f.getDeserializerType(null, int.class));
	}

	@Test
	void getsDeserializerTypeIfTypeEqualsIntegerWithHint() {
		assertEquals("text/plain", f.getDeserializerType(null, new Hint<Integer>() {}.getType()));
	}

	@Test
	void getsDeserializerTypeIfTypeEqualsLong() {
		assertEquals("text/plain", f.getDeserializerType(null, long.class));
	}

	@Test
	void getsDeserializerTypeIfTypeEqualsLongWithHint() {
		assertEquals("text/plain", f.getDeserializerType(null, new Hint<Long>() {}.getType()));
	}

	@Test
	void getsDeserializerTypeIfTypeEqualsFloat() {
		assertEquals("text/plain", f.getDeserializerType(null, float.class));
	}

	@Test
	void getsDeserializerTypeIfTypeEqualsFloatWithHint() {
		assertEquals("text/plain", f.getDeserializerType(null, new Hint<Float>() {}.getType()));
	}

	@Test
	void getsDeserializerTypeIfTypeEqualsDouble() {
		assertEquals("text/plain", f.getDeserializerType(null, double.class));
	}

	@Test
	void getsDeserializerTypeIfTypeEqualsDoubleWithHint() {
		assertEquals("text/plain", f.getDeserializerType(null, new Hint<Double>() {}.getType()));
	}

	@Test
	void getsDeserializerTypeIfTypeEqualsString() {
		assertEquals("text/plain", f.getDeserializerType(null, String.class));
	}

	@Test
	void getsDeserializerTypeIfTypeEqualsReader() {
		assertEquals("text/plain", f.getDeserializerType(null, Reader.class));
	}

	@Test
	void getsDeserializerTypeIfTypeEqualsObject() {
		try (MockedStatic<MediaCoder> media = mockStatic(MediaCoder.class)) {
			String fallbackTextType = "application/xml";
			when(coder.strip(fallbackTextType)).thenReturn(fallbackTextType);
			media.when(() -> MediaCoder.getInstance()).thenReturn(coder);
			f.setFallbackTextType(fallbackTextType);
			assertEquals(fallbackTextType, f.getDeserializerType(null, Object.class));
		}
	}

	@Test
	void doesNotCleanForDeserializing() {
		assertThrows(IllegalArgumentException.class, () -> {
			f.getDeserializerType(null, Object.class);
		});
	}

	@Test
	void putsDefaultAssembler() {
		try (MockedStatic<MediaCoder> media = mockStatic(MediaCoder.class)) {
			String contentType = "image/png";
			when(coder.strip(contentType)).thenReturn(contentType);
			media.when(() -> MediaCoder.getInstance()).thenReturn(coder);
			Assembler assembler = f.getAssembler("application/octet-stream");
			f.putDefaultAssembler(contentType);
			assertSame(assembler, f.getAssembler(contentType));
		}
	}

	@Test
	void doesNotPutDefaultAssemblerIfTypeIsNull() {
		assertThrows(NullPointerException.class, () -> {
			f.putDefaultAssembler(null);
		});
		assertThrows(TypeException.class, () -> {
			f.getAssembler(null);
		});
	}

	@Test
	void doesNotPutDefaultAssemblerIfStripReturnsNull() {
		try (MockedStatic<MediaCoder> media = mockStatic(MediaCoder.class)) {
			String contentType = "image/png";
			when(coder.strip(contentType)).thenReturn(null);
			media.when(() -> MediaCoder.getInstance()).thenReturn(coder);
			assertThrows(IllegalArgumentException.class, () -> {
				f.putDefaultAssembler(contentType);
			});
			assertThrows(TypeException.class, () -> {
				f.getAssembler(contentType);
			});
		}
	}

	@ParameterizedTest
	@ValueSource(strings = {
			"application/octet-stream",
			"image/png" })
	void putsAssembler(String contentType) {
		try (MockedStatic<MediaCoder> media = mockStatic(MediaCoder.class)) {
			when(coder.strip(contentType)).thenReturn(contentType);
			media.when(() -> MediaCoder.getInstance()).thenReturn(coder);
			Assembler assembler = mock(Assembler.class);
			f.putAssembler(contentType, assembler);
			assertSame(assembler, f.getAssembler(contentType));
		}
	}

	@Test
	void doesNotPutAssemblerIfTypeIsNull() {
		Assembler assembler = mock(Assembler.class);
		assertThrows(NullPointerException.class, () -> {
			f.putAssembler(null, assembler);
		});
		assertThrows(TypeException.class, () -> {
			f.getAssembler(null);
		});
	}

	@Test
	void doesNotPutAssemblerIfStripReturnsNull() {
		try (MockedStatic<MediaCoder> media = mockStatic(MediaCoder.class)) {
			String contentType = "image/png";
			when(coder.strip(contentType)).thenReturn(null);
			media.when(() -> MediaCoder.getInstance()).thenReturn(coder);
			Assembler assembler = mock(Assembler.class);
			assertThrows(IllegalArgumentException.class, () -> {
				f.putAssembler(contentType, assembler);
			});
			assertThrows(TypeException.class, () -> {
				f.getAssembler(contentType);
			});
		}
	}

	@Test
	void doesNotPutAssemblerIfItIsNull() {
		try (MockedStatic<MediaCoder> media = mockStatic(MediaCoder.class)) {
			String contentType = "image/png";
			when(coder.strip(contentType)).thenReturn(contentType);
			media.when(() -> MediaCoder.getInstance()).thenReturn(coder);
			assertThrows(NullPointerException.class, () -> {
				f.putAssembler(contentType, null);
			});
			assertThrows(TypeException.class, () -> {
				f.getAssembler(contentType);
			});
		}
	}

	@Test
	void putsDefaultDisassembler() {
		try (MockedStatic<MediaCoder> media = mockStatic(MediaCoder.class)) {
			String contentType = "image/png";
			when(coder.strip(contentType)).thenReturn(contentType);
			media.when(() -> MediaCoder.getInstance()).thenReturn(coder);
			Disassembler disassembler = f.getDisassembler("application/octet-stream");
			f.putDefaultDisassembler(contentType);
			assertSame(disassembler, f.getDisassembler(contentType));
		}
	}

	@Test
	void doesNotPutDefaultDisassemblerIfTypeIsNull() {
		assertThrows(NullPointerException.class, () -> {
			f.putDefaultDisassembler(null);
		});
		assertThrows(TypeException.class, () -> {
			f.getDisassembler(null);
		});
	}

	@Test
	void doesNotPutDefaultDisassemblerIfStripReturnsNull() {
		try (MockedStatic<MediaCoder> media = mockStatic(MediaCoder.class)) {
			String contentType = "image/png";
			when(coder.strip(contentType)).thenReturn(null);
			media.when(() -> MediaCoder.getInstance()).thenReturn(coder);
			assertThrows(IllegalArgumentException.class, () -> {
				f.putDefaultDisassembler(contentType);
			});
			assertThrows(TypeException.class, () -> {
				f.getDisassembler(contentType);
			});
		}
	}

	@ParameterizedTest
	@ValueSource(strings = {
			"application/octet-stream",
			"image/png" })
	void putsDisassembler(String contentType) {
		try (MockedStatic<MediaCoder> media = mockStatic(MediaCoder.class)) {
			when(coder.strip(contentType)).thenReturn(contentType);
			media.when(() -> MediaCoder.getInstance()).thenReturn(coder);
			Disassembler disassembler = mock(Disassembler.class);
			f.putDisassembler(contentType, disassembler);
			assertSame(disassembler, f.getDisassembler(contentType));
		}
	}

	@Test
	void doesNotPutDisassemblerIfTypeIsNull() {
		Disassembler disassembler = mock(Disassembler.class);
		assertThrows(NullPointerException.class, () -> {
			f.putDisassembler(null, disassembler);
		});
		assertThrows(TypeException.class, () -> {
			f.getDisassembler(null);
		});
	}

	@Test
	void doesNotPutDisassemblerIfStripReturnsNull() {
		try (MockedStatic<MediaCoder> media = mockStatic(MediaCoder.class)) {
			String contentType = "image/png";
			when(coder.strip(contentType)).thenReturn(null);
			media.when(() -> MediaCoder.getInstance()).thenReturn(coder);
			Disassembler disassembler = mock(Disassembler.class);
			assertThrows(IllegalArgumentException.class, () -> {
				f.putDisassembler(contentType, disassembler);
			});
			assertThrows(TypeException.class, () -> {
				f.getDisassembler(contentType);
			});
		}
	}

	@Test
	void doesNotPutDisassemblerIfItIsNull() {
		try (MockedStatic<MediaCoder> media = mockStatic(MediaCoder.class)) {
			String contentType = "image/png";
			when(coder.strip(contentType)).thenReturn(contentType);
			media.when(() -> MediaCoder.getInstance()).thenReturn(coder);
			assertThrows(NullPointerException.class, () -> {
				f.putDisassembler(contentType, null);
			});
			assertThrows(TypeException.class, () -> {
				f.getDisassembler(contentType);
			});
		}
	}

	@Test
	void putsDefaultSerializer() {
		try (MockedStatic<MediaCoder> media = mockStatic(MediaCoder.class)) {
			String contentType = "application/xml";
			when(coder.strip(contentType)).thenReturn(contentType);
			media.when(() -> MediaCoder.getInstance()).thenReturn(coder);
			Serializer serializer = f.getSerializer("text/plain");
			f.putDefaultSerializer(contentType);
			assertSame(serializer, f.getSerializer(contentType));
		}
	}

	@Test
	void doesNotPutDefaultSerializerIfTypeIsNull() {
		assertThrows(NullPointerException.class, () -> {
			f.putDefaultSerializer(null);
		});
		assertThrows(TypeException.class, () -> {
			f.getSerializer(null);
		});
	}

	@Test
	void doesNotPutDefaultSerializerIfStripReturnsNull() {
		try (MockedStatic<MediaCoder> media = mockStatic(MediaCoder.class)) {
			String contentType = "application/xml";
			when(coder.strip(contentType)).thenReturn(null);
			media.when(() -> MediaCoder.getInstance()).thenReturn(coder);
			assertThrows(IllegalArgumentException.class, () -> {
				f.putDefaultSerializer(contentType);
			});
			assertThrows(TypeException.class, () -> {
				f.getSerializer(contentType);
			});
		}
	}

	@ParameterizedTest
	@ValueSource(strings = {
			"text/plain",
			"application/xml" })
	void putsSerializer(String contentType) {
		try (MockedStatic<MediaCoder> media = mockStatic(MediaCoder.class)) {
			when(coder.strip(contentType)).thenReturn(contentType);
			media.when(() -> MediaCoder.getInstance()).thenReturn(coder);
			Serializer serializer = mock(Serializer.class);
			f.putSerializer(contentType, serializer);
			assertSame(serializer, f.getSerializer(contentType));
		}
	}

	@Test
	void doesNotPutSerializerIfTypeIsNull() {
		Serializer serializer = mock(Serializer.class);
		assertThrows(NullPointerException.class, () -> {
			f.putSerializer(null, serializer);
		});
		assertThrows(TypeException.class, () -> {
			f.getSerializer(null);
		});
	}

	@Test
	void doesNotPutSerializerIfStripReturnsNull() {
		try (MockedStatic<MediaCoder> media = mockStatic(MediaCoder.class)) {
			String contentType = "application/xml";
			when(coder.strip(contentType)).thenReturn(null);
			media.when(() -> MediaCoder.getInstance()).thenReturn(coder);
			Serializer serializer = mock(Serializer.class);
			assertThrows(IllegalArgumentException.class, () -> {
				f.putSerializer(contentType, serializer);
			});
			assertThrows(TypeException.class, () -> {
				f.getSerializer(contentType);
			});
		}
	}

	@Test
	void doesNotPutSerializerIfItIsNull() {
		try (MockedStatic<MediaCoder> media = mockStatic(MediaCoder.class)) {
			String contentType = "application/xml";
			when(coder.strip(contentType)).thenReturn(contentType);
			media.when(() -> MediaCoder.getInstance()).thenReturn(coder);
			assertThrows(NullPointerException.class, () -> {
				f.putSerializer(contentType, null);
			});
			assertThrows(TypeException.class, () -> {
				f.getSerializer(contentType);
			});
		}
	}

	@Test
	void putsDefaultDeserializer() {
		try (MockedStatic<MediaCoder> media = mockStatic(MediaCoder.class)) {
			String contentType = "application/xml";
			when(coder.strip(contentType)).thenReturn(contentType);
			media.when(() -> MediaCoder.getInstance()).thenReturn(coder);
			Deserializer deserializer = f.getDeserializer("text/plain");
			f.putDefaultDeserializer(contentType);
			assertSame(deserializer, f.getDeserializer(contentType));
		}
	}

	@Test
	void doesNotPutDefaultDeserializerIfTypeIsNull() {
		assertThrows(NullPointerException.class, () -> {
			f.putDefaultDeserializer(null);
		});
		assertThrows(TypeException.class, () -> {
			f.getDeserializer(null);
		});
	}

	@Test
	void doesNotPutDefaultDeserializerIfStripReturnsNull() {
		try (MockedStatic<MediaCoder> media = mockStatic(MediaCoder.class)) {
			String contentType = "application/xml";
			when(coder.strip(contentType)).thenReturn(null);
			media.when(() -> MediaCoder.getInstance()).thenReturn(coder);
			assertThrows(IllegalArgumentException.class, () -> {
				f.putDefaultDeserializer(contentType);
			});
			assertThrows(TypeException.class, () -> {
				f.getDeserializer(contentType);
			});
		}
	}

	@ParameterizedTest
	@ValueSource(strings = {
			"text/plain",
			"application/xml" })
	void putsDeserializer(String contentType) {
		try (MockedStatic<MediaCoder> media = mockStatic(MediaCoder.class)) {
			when(coder.strip(contentType)).thenReturn(contentType);
			media.when(() -> MediaCoder.getInstance()).thenReturn(coder);
			Deserializer deserializer = mock(Deserializer.class);
			f.putDeserializer(contentType, deserializer);
			assertSame(deserializer, f.getDeserializer(contentType));
		}
	}

	@Test
	void doesNotPutDeserializerIfTypeIsNull() {
		Deserializer deserializer = mock(Deserializer.class);
		assertThrows(NullPointerException.class, () -> {
			f.putDeserializer(null, deserializer);
		});
		assertThrows(TypeException.class, () -> {
			f.getDeserializer(null);
		});
	}

	@Test
	void doesNotPutDeserializerIfStripReturnsNull() {
		try (MockedStatic<MediaCoder> media = mockStatic(MediaCoder.class)) {
			String contentType = "application/xml";
			when(coder.strip(contentType)).thenReturn(null);
			media.when(() -> MediaCoder.getInstance()).thenReturn(coder);
			Deserializer deserializer = mock(Deserializer.class);
			assertThrows(IllegalArgumentException.class, () -> {
				f.putDeserializer(contentType, deserializer);
			});
			assertThrows(TypeException.class, () -> {
				f.getDeserializer(contentType);
			});
		}
	}

	@Test
	void doesNotPutDeserializerIfItIsNull() {
		try (MockedStatic<MediaCoder> media = mockStatic(MediaCoder.class)) {
			String contentType = "application/xml";
			when(coder.strip(contentType)).thenReturn(contentType);
			media.when(() -> MediaCoder.getInstance()).thenReturn(coder);
			assertThrows(NullPointerException.class, () -> {
				f.putDeserializer(contentType, null);
			});
			assertThrows(TypeException.class, () -> {
				f.getDeserializer(contentType);
			});
		}
	}

	@Test
	void doesNotSetFallbackByteTypeIfItIsNull() {
		assertThrows(NullPointerException.class, () -> {
			f.setFallbackByteType(null);
		});
	}

	@Test
	void doesNotSetFallbackByteTypeIfStripReturnsNull() {
		try (MockedStatic<MediaCoder> media = mockStatic(MediaCoder.class)) {
			String contentType = "application/octet-stream";
			when(coder.strip(contentType)).thenReturn(null);
			media.when(() -> MediaCoder.getInstance()).thenReturn(coder);
			assertThrows(IllegalArgumentException.class, () -> {
				f.setFallbackByteType(contentType);
			});
		}
	}

	@Test
	void doesNotSetFallbackTextTypeIfItIsNull() {
		assertThrows(NullPointerException.class, () -> {
			f.setFallbackTextType(null);
		});
	}

	@Test
	void doesNotSetFallbackTextTypeIfStripReturnsNull() {
		try (MockedStatic<MediaCoder> media = mockStatic(MediaCoder.class)) {
			String contentType = "text/plain";
			when(coder.strip(contentType)).thenReturn(null);
			media.when(() -> MediaCoder.getInstance()).thenReturn(coder);
			assertThrows(IllegalArgumentException.class, () -> {
				f.setFallbackTextType(contentType);
			});
		}
	}
}
