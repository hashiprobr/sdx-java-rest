package br.pro.hashi.sdx.rest.transform.manager;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.Writer;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;
import java.util.function.Consumer;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.MockitoAnnotations;

import br.pro.hashi.sdx.rest.Hint;
import br.pro.hashi.sdx.rest.coding.MediaCoder;
import br.pro.hashi.sdx.rest.transform.Assembler;
import br.pro.hashi.sdx.rest.transform.Deserializer;
import br.pro.hashi.sdx.rest.transform.Disassembler;
import br.pro.hashi.sdx.rest.transform.Serializer;
import br.pro.hashi.sdx.rest.transform.exception.TypeException;

class TransformManagerTest {
	private AutoCloseable mocks;
	private @Mock MediaCoder coder;
	private @Mock DefaultAssembler defaultAssembler;
	private @Mock DefaultDisassembler defaultDisassembler;
	private @Mock DefaultSerializer defaultSerializer;
	private @Mock DefaultDeserializer defaultDeserializer;
	private MockedStatic<DefaultAssembler> assemblerStatic;
	private MockedStatic<DefaultDisassembler> disassemblerStatic;
	private MockedStatic<DefaultSerializer> serializerStatic;
	private MockedStatic<DefaultDeserializer> deserializerStatic;
	private TransformManager m;

	@BeforeEach
	void setUp() {
		mocks = MockitoAnnotations.openMocks(this);

		when(coder.strip(any(String.class))).thenAnswer((invocation) -> {
			String contentType = invocation.getArgument(0);
			if (contentType.isEmpty()) {
				return null;
			}
			int index = contentType.indexOf(';');
			return contentType.substring(0, index);
		});

		assemblerStatic = mockStatic(DefaultAssembler.class);
		assemblerStatic.when(() -> DefaultAssembler.getInstance()).thenReturn(defaultAssembler);

		disassemblerStatic = mockStatic(DefaultDisassembler.class);
		disassemblerStatic.when(() -> DefaultDisassembler.getInstance()).thenReturn(defaultDisassembler);

		serializerStatic = mockStatic(DefaultSerializer.class);
		serializerStatic.when(() -> DefaultSerializer.getInstance()).thenReturn(defaultSerializer);

		deserializerStatic = mockStatic(DefaultDeserializer.class);
		deserializerStatic.when(() -> DefaultDeserializer.getInstance()).thenReturn(defaultDeserializer);

		m = new TransformManager(coder);
	}

	@AfterEach
	void tearDown() {
		deserializerStatic.close();
		serializerStatic.close();
		disassemblerStatic.close();
		assemblerStatic.close();
		assertDoesNotThrow(() -> {
			mocks.close();
		});
	}

	@Test
	void getsBase() {
		assertInstanceOf(TransformManager.class, TransformManager.newInstance());
	}

	@Test
	void getsCopy() {
		TransformManager manager = TransformManager.newInstance(m);
		assertSame(m.getCoder(), manager.getCoder());
		assertNotSame(m.getAssemblers(), manager.getAssemblers());
		assertEquals(m.getAssemblers(), manager.getAssemblers());
		assertNotSame(m.getDisassemblers(), manager.getDisassemblers());
		assertEquals(m.getDisassemblers(), manager.getDisassemblers());
		assertNotSame(m.getSerializers(), manager.getSerializers());
		assertEquals(m.getSerializers(), manager.getSerializers());
		assertNotSame(m.getDeserializers(), manager.getDeserializers());
		assertEquals(m.getDeserializers(), manager.getDeserializers());
		assertNotSame(m.getExtensions(), manager.getExtensions());
		assertEquals(m.getExtensions(), manager.getExtensions());
		assertNotSame(m.getBinaryRawTypes(), manager.getBinaryRawTypes());
		assertEquals(m.getBinaryRawTypes(), manager.getBinaryRawTypes());
		assertNotSame(m.getBinaryGenericTypes(), manager.getBinaryGenericTypes());
		assertEquals(m.getBinaryGenericTypes(), manager.getBinaryGenericTypes());
		assertEquals(m.getBinaryFallbackType(), manager.getBinaryFallbackType());
		assertEquals(m.getFallbackType(), manager.getFallbackType());
	}

	@Test
	void getsAssembler() {
		assertSame(defaultAssembler, m.getAssembler("application/octet-stream"));
	}

	@Test
	void doesNotGetMissingAssembler() {
		assertThrows(TypeException.class, () -> {
			m.getAssembler("image/png");
		});
	}

	@Test
	void putsDefaultAssembler() {
		m.putDefaultAssembler("image/png;parameter");
		assertSame(defaultAssembler, m.getAssembler("image/png"));
	}

	@Test
	void putsAssembler() {
		Assembler assembler = mock(Assembler.class);
		m.putAssembler("image/png;parameter", assembler);
		assertSame(assembler, m.getAssembler("image/png"));
	}

	@Test
	void doesNotPutNullAssembler() {
		assertThrows(NullPointerException.class, () -> {
			m.putAssembler("image/png;parameter", null);
		});
	}

	@Test
	void doesNotPutAssemblerWithNullType() {
		assertThrows(NullPointerException.class, () -> {
			m.putDefaultAssembler(null);
		});
	}

	@Test
	void doesNotPutAssemblerWithBlankType() {
		assertThrows(IllegalArgumentException.class, () -> {
			m.putDefaultAssembler("");
		});
	}

	@Test
	void removesAssembler() {
		m.removeAssembler("application/octet-stream;parameter");
		assertThrows(TypeException.class, () -> {
			m.getAssembler("application/octet-stream");
		});
	}

	@Test
	void removesAssemblerWithNullType() {
		assertDoesNotThrow(() -> {
			m.removeAssembler(null);
		});
	}

	@Test
	void removesAssemblerWithBlankType() {
		assertDoesNotThrow(() -> {
			m.removeAssembler("");
		});
	}

	@Test
	void getsDisassembler() {
		assertSame(defaultDisassembler, m.getDisassembler("application/octet-stream"));
	}

	@Test
	void doesNotGetMissingDisassembler() {
		assertThrows(TypeException.class, () -> {
			m.getDisassembler("image/png");
		});
	}

	@Test
	void putsDefaultDisassembler() {
		m.putDefaultDisassembler("image/png;parameter");
		assertSame(defaultDisassembler, m.getDisassembler("image/png"));
	}

	@Test
	void putsDisassembler() {
		Disassembler disassembler = mock(Disassembler.class);
		m.putDisassembler("image/png;parameter", disassembler);
		assertSame(disassembler, m.getDisassembler("image/png"));
	}

	@Test
	void doesNotPutNullDisassembler() {
		assertThrows(NullPointerException.class, () -> {
			m.putDisassembler("image/png;parameter", null);
		});
	}

	@Test
	void doesNotPutDisassemblerWithNullType() {
		assertThrows(NullPointerException.class, () -> {
			m.putDefaultDisassembler(null);
		});
	}

	@Test
	void doesNotPutDisassemblerWithBlankType() {
		assertThrows(IllegalArgumentException.class, () -> {
			m.putDefaultDisassembler("");
		});
	}

	@Test
	void removesDisassembler() {
		m.removeDisassembler("application/octet-stream;parameter");
		assertThrows(TypeException.class, () -> {
			m.getDisassembler("application/octet-stream");
		});
	}

	@Test
	void removesDisassemblerWithNullType() {
		assertDoesNotThrow(() -> {
			m.removeDisassembler(null);
		});
	}

	@Test
	void removesDisassemblerWithBlankType() {
		assertDoesNotThrow(() -> {
			m.removeDisassembler("");
		});
	}

	@Test
	void getsSerializer() {
		assertSame(defaultSerializer, m.getSerializer("text/plain"));
	}

	@Test
	void doesNotGetMissingSerializer() {
		assertThrows(TypeException.class, () -> {
			m.getSerializer("application/xml");
		});
	}

	@Test
	void putsDefaultSerializer() {
		m.putDefaultSerializer("application/xml;parameter");
		assertSame(defaultSerializer, m.getSerializer("application/xml"));
	}

	@Test
	void putsSerializer() {
		Serializer serializer = mock(Serializer.class);
		m.putSerializer("application/xml;parameter", serializer);
		assertSame(serializer, m.getSerializer("application/xml"));
	}

	@Test
	void doesNotPutNullSerializer() {
		assertThrows(NullPointerException.class, () -> {
			m.putSerializer("application/xml;parameter", null);
		});
	}

	@Test
	void doesNotPutSerializerWithNullType() {
		assertThrows(NullPointerException.class, () -> {
			m.putDefaultSerializer(null);
		});
	}

	@Test
	void doesNotPutSerializerWithBlankType() {
		assertThrows(IllegalArgumentException.class, () -> {
			m.putDefaultSerializer("");
		});
	}

	@Test
	void removesSerializer() {
		m.removeSerializer("text/plain;parameter");
		assertThrows(TypeException.class, () -> {
			m.getSerializer("text/plain");
		});
	}

	@Test
	void removesSerializerWithNullType() {
		assertDoesNotThrow(() -> {
			m.removeSerializer(null);
		});
	}

	@Test
	void removesSerializerWithBlankType() {
		assertDoesNotThrow(() -> {
			m.removeSerializer("");
		});
	}

	@Test
	void getsDeserializer() {
		assertSame(defaultDeserializer, m.getDeserializer("text/plain"));
	}

	@Test
	void doesNotGetMissingDeserializer() {
		assertThrows(TypeException.class, () -> {
			m.getDeserializer("application/xml");
		});
	}

	@Test
	void putsDefaultDeserializer() {
		m.putDefaultDeserializer("application/xml;parameter");
		assertSame(defaultDeserializer, m.getDeserializer("application/xml"));
	}

	@Test
	void putsDeserializer() {
		Deserializer deserializer = mock(Deserializer.class);
		m.putDeserializer("application/xml;parameter", deserializer);
		assertSame(deserializer, m.getDeserializer("application/xml"));
	}

	@Test
	void doesNotPutNullDeserializer() {
		assertThrows(NullPointerException.class, () -> {
			m.putDeserializer("application/xml;parameter", null);
		});
	}

	@Test
	void doesNotPutDeserializerWithNullType() {
		assertThrows(NullPointerException.class, () -> {
			m.putDefaultDeserializer(null);
		});
	}

	@Test
	void doesNotPutDeserializerWithBlankType() {
		assertThrows(IllegalArgumentException.class, () -> {
			m.putDefaultDeserializer("");
		});
	}

	@Test
	void removesDeserializer() {
		m.removeDeserializer("text/plain;parameter");
		assertThrows(TypeException.class, () -> {
			m.getDeserializer("text/plain");
		});
	}

	@Test
	void removesDeserializerWithNullType() {
		assertDoesNotThrow(() -> {
			m.removeDeserializer(null);
		});
	}

	@Test
	void removesDeserializerWithBlankType() {
		assertDoesNotThrow(() -> {
			m.removeDeserializer("");
		});
	}

	@Test
	void getsExtensionType() {
		assertEquals("text/plain", m.getExtensionType("txt"));
	}

	@Test
	void doesNotGetMissingBinaryExtensionType() {
		assertNull(m.getExtensionType("png"));
	}

	@Test
	void doesNotGetMissingExtensionType() {
		assertNull(m.getExtensionType("xml"));
	}

	@Test
	void putsBinaryExtensionType() {
		String contentType = "image/png;parameter";
		m.putDefaultAssembler(contentType);
		m.putExtensionType(" \t\npng \t\n", contentType);
		assertEquals("image/png", m.getExtensionType("png"));
	}

	@Test
	void putsExtensionType() {
		String contentType = "application/xml;parameter";
		m.putDefaultSerializer(contentType);
		m.putExtensionType(" \t\nxml \t\n", contentType);
		assertEquals("application/xml", m.getExtensionType("xml"));
	}

	@Test
	void doesNotPutBinaryExtensionTypeWithNullExtension() {
		assertThrows(NullPointerException.class, () -> {
			m.putExtensionType(null, "image/png;parameter");
		});
	}

	@Test
	void doesNotPutExtensionTypeWithNullExtension() {
		assertThrows(NullPointerException.class, () -> {
			m.putExtensionType(null, "application/xml;parameter");
		});
	}

	@Test
	void doesNotPutBinaryExtensionTypeWithBlankExtension() {
		assertThrows(IllegalArgumentException.class, () -> {
			m.putExtensionType(" \t\n", "image/png;parameter");
		});
	}

	@Test
	void doesNotPutExtensionTypeWithBlankExtension() {
		assertThrows(IllegalArgumentException.class, () -> {
			m.putExtensionType(" \t\n", "application/xml;parameter");
		});
	}

	@Test
	void doesNotPutNullBinaryExtensionType() {
		assertThrows(NullPointerException.class, () -> {
			m.putExtensionType("png", null);
		});
	}

	@Test
	void doesNotPutNullExtensionType() {
		assertThrows(NullPointerException.class, () -> {
			m.putExtensionType("xml", null);
		});
	}

	@Test
	void doesNotPutBlankBinaryExtensionType() {
		assertThrows(IllegalArgumentException.class, () -> {
			m.putExtensionType("png", "");
		});
	}

	@Test
	void doesNotPutBlankExtensionType() {
		assertThrows(IllegalArgumentException.class, () -> {
			m.putExtensionType("xml", "");
		});
	}

	@Test
	void doesNotPutMissingBinaryExtensionType() {
		assertThrows(IllegalArgumentException.class, () -> {
			m.putExtensionType("png", "image/png;parameter");
		});
	}

	@Test
	void doesNotPutMissingExtensionType() {
		assertThrows(IllegalArgumentException.class, () -> {
			m.putExtensionType("xml", "application/xml;parameter");
		});
	}

	@Test
	void removesExtensionType() {
		m.removeExtensionType(" \t\ntxt \t\n");
		assertNull(m.getExtensionType("txt"));
	}

	@Test
	void removesExtensionTypeWithNullExtension() {
		assertDoesNotThrow(() -> {
			m.removeExtensionType(null);
		});
	}

	@Test
	void removesExtensionTypeWithBlankExtension() {
		assertDoesNotThrow(() -> {
			m.removeExtensionType(" \t\n");
		});
	}

	@Test
	void initializesByteArrayAsBinary() {
		assertTrue(m.isBinary(byte[].class));
	}

	@Test
	void initializesInputStreamAsBinary() {
		assertTrue(m.isBinary(InputStream.class));
	}

	@Test
	void initializesOutputStreamConsumerAsBinary() {
		assertTrue(m.isBinary(new Hint<Consumer<OutputStream>>() {}.getType()));
	}

	@Test
	void initializesByteArrayInputStreamAsBinary() {
		assertTrue(m.isBinary(ByteArrayInputStream.class));
	}

	@Test
	void doesNotInitializeReadableByteChannelAsBinary() {
		assertFalse(m.isBinary(ReadableByteChannel.class));
	}

	@Test
	void doesNotInitializeWritableByteChannelConsumerAsBinary() {
		assertFalse(m.isBinary(new Hint<Consumer<WritableByteChannel>>() {}.getType()));
	}

	@Test
	void addsReadableByteChannelAsBinary() {
		m.addBinary(ReadableByteChannel.class);
		assertTrue(m.isBinary(ReadableByteChannel.class));
	}

	@Test
	void addsWritableByteChannelConsumerAsBinary() {
		m.addBinary(new Hint<Consumer<WritableByteChannel>>() {}.getType());
		assertTrue(m.isBinary(new Hint<Consumer<WritableByteChannel>>() {}.getType()));
	}

	@Test
	void removesByteArrayAsBinary() {
		m.removeBinary(byte[].class);
		assertFalse(m.isBinary(byte[].class));
	}

	@Test
	void removesInputStreamAsBinary() {
		m.removeBinary(InputStream.class);
		assertFalse(m.isBinary(InputStream.class));
	}

	@Test
	void removesOutputStreamConsumerAsBinary() {
		m.removeBinary(new Hint<Consumer<OutputStream>>() {}.getType());
		assertFalse(m.isBinary(new Hint<Consumer<OutputStream>>() {}.getType()));
	}

	@Test
	void removesByteArrayInputStreamAsBinary() {
		assertDoesNotThrow(() -> {
			m.removeBinary(ByteArrayInputStream.class);
		});
	}

	@Test
	void removesNullAsBinary() {
		assertDoesNotThrow(() -> {
			m.removeBinary(null);
		});
	}

	@Test
	void doesNotSetNullBinaryFallbackType() {
		assertThrows(NullPointerException.class, () -> {
			m.setBinaryFallbackType(null);
		});
	}

	@Test
	void doesNotSetBlankBinaryFallbackType() {
		assertThrows(IllegalArgumentException.class, () -> {
			m.setBinaryFallbackType("");
		});
	}

	@Test
	void doesNotSetNullFallbackType() {
		assertThrows(NullPointerException.class, () -> {
			m.setFallbackType(null);
		});
	}

	@Test
	void doesNotSetBlankFallbackType() {
		assertThrows(IllegalArgumentException.class, () -> {
			m.setFallbackType("");
		});
	}

	@Test
	void setsAndGetsFallbackAssemblerType() {
		Object body = new Object();
		m.setBinaryFallbackType("image/png;parameter");
		assertEquals("image/png", m.getAssemblerType(null, body, Object.class));
		m.unsetBinaryFallbackType();
		assertThrows(IllegalStateException.class, () -> {
			assertEquals("application/octet-stream", m.getAssemblerType(null, body, Object.class));
		});
	}

	@Test
	void setsAndGetsFallbackDisassemblerType() {
		m.setBinaryFallbackType("image/png;parameter");
		assertEquals("image/png", m.getDisassemblerType(null, Object.class));
		m.unsetBinaryFallbackType();
		assertThrows(IllegalStateException.class, () -> {
			assertEquals("application/octet-stream", m.getDisassemblerType(null, Object.class));
		});
	}

	@Test
	void setsAndGetsFallbackSerializerType() {
		Object body = new Object();
		m.setFallbackType("application/xml;parameter");
		assertEquals("application/xml", m.getSerializerType(null, body, Object.class));
		m.unsetFallbackType();
		assertThrows(IllegalStateException.class, () -> {
			assertEquals("text/plain", m.getSerializerType(null, body, Object.class));
		});
	}

	@Test
	void setsAndGetsFallbackDeserializerType() {
		m.setFallbackType("application/xml;parameter");
		assertEquals("application/xml", m.getDeserializerType(null, Object.class));
		m.unsetFallbackType();
		assertThrows(IllegalStateException.class, () -> {
			assertEquals("text/plain", m.getDeserializerType(null, Object.class));
		});
	}

	@Test
	void getsAssemblerType() {
		String contentType = "image/png";
		assertEquals(contentType, m.getAssemblerType(contentType, new Object(), Object.class));
	}

	@Test
	void getsByteArrayAssemblerType() {
		byte[] body = new byte[] {};
		assertEquals("application/octet-stream", m.getAssemblerType(null, body, byte[].class));
	}

	@Test
	void getsInputStreamAssemblerType() {
		InputStream body = InputStream.nullInputStream();
		assertEquals("application/octet-stream", m.getAssemblerType(null, body, InputStream.class));
	}

	@Test
	void getsOutputStreamConsumerAssemblerType() {
		Consumer<OutputStream> body = (stream) -> {};
		assertEquals("application/octet-stream", m.getAssemblerType(null, body, new Hint<Consumer<OutputStream>>() {}.getType()));
	}

	@Test
	void doesNotGetMissingAssemblerType() {
		Object body = new Object();
		assertThrows(IllegalStateException.class, () -> {
			m.getAssemblerType(null, body, Object.class);
		});
	}

	@Test
	void getsDisassemblerType() {
		String contentType = "image/png";
		assertEquals(contentType, m.getDisassemblerType(contentType, Object.class));
	}

	@Test
	void getsByteArrayDisssemblerType() {
		assertEquals("application/octet-stream", m.getDisassemblerType(null, byte[].class));
	}

	@Test
	void getsInputStreamDisssemblerType() {
		assertEquals("application/octet-stream", m.getDisassemblerType(null, InputStream.class));
	}

	@Test
	void doesNotGetMissingDisassemblerType() {
		assertThrows(IllegalStateException.class, () -> {
			m.getDisassemblerType(null, Object.class);
		});
	}

	@Test
	void getsSerializerType() {
		String contentType = "application/xml";
		assertEquals(contentType, m.getSerializerType(contentType, new Object(), Object.class));
	}

	@Test
	void getsStringSerializerType() {
		String body = "";
		assertEquals("text/plain", m.getSerializerType(null, body, String.class));
	}

	@Test
	void getsReaderSerializerType() {
		Reader body = Reader.nullReader();
		assertEquals("text/plain", m.getSerializerType(null, body, Reader.class));
	}

	@Test
	void getsWriterConsumerSerializerType() {
		Consumer<Writer> body = (writer) -> {};
		assertEquals("text/plain", m.getSerializerType(null, body, new Hint<Consumer<Writer>>() {}.getType()));
	}

	@Test
	void doesNotGetMissingSerializerType() {
		Object body = new Object();
		assertThrows(IllegalStateException.class, () -> {
			m.getSerializerType(null, body, Object.class);
		});
	}

	@Test
	void getsDeserializerType() {
		String contentType = "application/xml";
		assertEquals(contentType, m.getDeserializerType(contentType, Object.class));
	}

	@Test
	void getsStringDeserializerType() {
		assertEquals("text/plain", m.getDeserializerType(null, String.class));
	}

	@Test
	void getsReaderDeserializerType() {
		assertEquals("text/plain", m.getDeserializerType(null, Reader.class));
	}

	@Test
	void doesNotGetMissingDeserializerType() {
		assertThrows(IllegalStateException.class, () -> {
			m.getDeserializerType(null, Object.class);
		});
	}
}
