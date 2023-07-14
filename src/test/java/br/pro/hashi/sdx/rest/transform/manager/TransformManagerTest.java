package br.pro.hashi.sdx.rest.transform.manager;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
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
	private TransformManager f;

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

		f = new TransformManager(coder);
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
	void getsInstance() {
		assertInstanceOf(TransformManager.class, TransformManager.newInstance());
	}

	@Test
	void getsAssembler() {
		assertSame(defaultAssembler, f.getAssembler("application/octet-stream"));
	}

	@Test
	void doesNotGetMissingAssembler() {
		assertThrows(TypeException.class, () -> {
			f.getAssembler("image/png");
		});
	}

	@Test
	void putsDefaultAssembler() {
		f.putDefaultAssembler("image/png;parameter");
		assertSame(defaultAssembler, f.getAssembler("image/png"));
	}

	@Test
	void putsAssembler() {
		Assembler assembler = mock(Assembler.class);
		f.putAssembler("image/png;parameter", assembler);
		assertSame(assembler, f.getAssembler("image/png"));
	}

	@Test
	void doesNotPutNullAssembler() {
		assertThrows(NullPointerException.class, () -> {
			f.putAssembler("image/png;parameter", null);
		});
	}

	@Test
	void doesNotPutAssemblerWithNullType() {
		assertThrows(NullPointerException.class, () -> {
			f.putDefaultAssembler(null);
		});
	}

	@Test
	void doesNotPutAssemblerWithBlankType() {
		assertThrows(IllegalArgumentException.class, () -> {
			f.putDefaultAssembler("");
		});
	}

	@Test
	void getsDisassembler() {
		assertSame(defaultDisassembler, f.getDisassembler("application/octet-stream"));
	}

	@Test
	void doesNotGetMissingDisassembler() {
		assertThrows(TypeException.class, () -> {
			f.getDisassembler("image/png");
		});
	}

	@Test
	void putsDefaultDisassembler() {
		f.putDefaultDisassembler("image/png;parameter");
		assertSame(defaultDisassembler, f.getDisassembler("image/png"));
	}

	@Test
	void putsDisassembler() {
		Disassembler disassembler = mock(Disassembler.class);
		f.putDisassembler("image/png;parameter", disassembler);
		assertSame(disassembler, f.getDisassembler("image/png"));
	}

	@Test
	void doesNotPutNullDisassembler() {
		assertThrows(NullPointerException.class, () -> {
			f.putDisassembler("image/png;parameter", null);
		});
	}

	@Test
	void doesNotPutDisassemblerWithNullType() {
		assertThrows(NullPointerException.class, () -> {
			f.putDefaultDisassembler(null);
		});
	}

	@Test
	void doesNotPutDisassemblerWithBlankType() {
		assertThrows(IllegalArgumentException.class, () -> {
			f.putDefaultDisassembler("");
		});
	}

	@Test
	void getsSerializer() {
		assertSame(defaultSerializer, f.getSerializer("text/plain"));
	}

	@Test
	void doesNotGetMissingSerializer() {
		assertThrows(TypeException.class, () -> {
			f.getSerializer("application/xml");
		});
	}

	@Test
	void putsDefaultSerializer() {
		f.putDefaultSerializer("application/xml;parameter");
		assertSame(defaultSerializer, f.getSerializer("application/xml"));
	}

	@Test
	void putsSerializer() {
		Serializer serializer = mock(Serializer.class);
		f.putSerializer("application/xml;parameter", serializer);
		assertSame(serializer, f.getSerializer("application/xml"));
	}

	@Test
	void doesNotPutNullSerializer() {
		assertThrows(NullPointerException.class, () -> {
			f.putSerializer("application/xml;parameter", null);
		});
	}

	@Test
	void doesNotPutSerializerWithNullType() {
		assertThrows(NullPointerException.class, () -> {
			f.putDefaultSerializer(null);
		});
	}

	@Test
	void doesNotPutSerializerWithBlankType() {
		assertThrows(IllegalArgumentException.class, () -> {
			f.putDefaultSerializer("");
		});
	}

	@Test
	void getsDeserializer() {
		assertSame(defaultDeserializer, f.getDeserializer("text/plain"));
	}

	@Test
	void doesNotGetMissingDeserializer() {
		assertThrows(TypeException.class, () -> {
			f.getDeserializer("application/xml");
		});
	}

	@Test
	void putsDefaultDeserializer() {
		f.putDefaultDeserializer("application/xml;parameter");
		assertSame(defaultDeserializer, f.getDeserializer("application/xml"));
	}

	@Test
	void putsDeserializer() {
		Deserializer deserializer = mock(Deserializer.class);
		f.putDeserializer("application/xml;parameter", deserializer);
		assertSame(deserializer, f.getDeserializer("application/xml"));
	}

	@Test
	void doesNotPutNullDeserializer() {
		assertThrows(NullPointerException.class, () -> {
			f.putDeserializer("application/xml;parameter", null);
		});
	}

	@Test
	void doesNotPutDeserializerWithNullType() {
		assertThrows(NullPointerException.class, () -> {
			f.putDefaultDeserializer(null);
		});
	}

	@Test
	void doesNotPutDeserializerWithBlankType() {
		assertThrows(IllegalArgumentException.class, () -> {
			f.putDefaultDeserializer("");
		});
	}

	@Test
	void getsExtensionType() {
		assertEquals("text/plain", f.getExtensionType("txt"));
	}

	@Test
	void doesNotGetMissingByteExtensionType() {
		assertNull(f.getExtensionType("png"));
	}

	@Test
	void doesNotGetMissingTextExtensionType() {
		assertNull(f.getExtensionType("xml"));
	}

	@Test
	void putsByteExtensionType() {
		f.putDefaultAssembler("image/png;parameter");
		f.putExtensionType(" \t\npng \t\n", "image/png");
		assertEquals("image/png", f.getExtensionType("png"));
	}

	@Test
	void putsTextExtensionType() {
		f.putDefaultSerializer("application/xml;parameter");
		f.putExtensionType(" \t\nxml \t\n", "application/xml");
		assertEquals("application/xml", f.getExtensionType("xml"));
	}

	@Test
	void doesNotPutByteExtensionTypeWithNullExtension() {
		assertThrows(NullPointerException.class, () -> {
			f.putExtensionType(null, "image/png");
		});
	}

	@Test
	void doesNotPutTextExtensionTypeWithNullExtension() {
		assertThrows(NullPointerException.class, () -> {
			f.putExtensionType(null, "application/xml");
		});
	}

	@Test
	void doesNotPutByteExtensionTypeWithBlankExtension() {
		assertThrows(IllegalArgumentException.class, () -> {
			f.putExtensionType(" \t\n", "image/png");
		});
	}

	@Test
	void doesNotPutTextExtensionTypeWithBlankExtension() {
		assertThrows(IllegalArgumentException.class, () -> {
			f.putExtensionType(" \t\n", "application/xml");
		});
	}

	@Test
	void doesNotPutNullByteExtensionType() {
		assertThrows(NullPointerException.class, () -> {
			f.putExtensionType("png", null);
		});
	}

	@Test
	void doesNotPutNullTextExtensionType() {
		assertThrows(NullPointerException.class, () -> {
			f.putExtensionType("xml", null);
		});
	}

	@Test
	void doesNotPutBlankByteExtensionType() {
		assertThrows(IllegalArgumentException.class, () -> {
			f.putExtensionType("png", "");
		});
	}

	@Test
	void doesNotPutBlankTextExtensionType() {
		assertThrows(IllegalArgumentException.class, () -> {
			f.putExtensionType("xml", "");
		});
	}

	@Test
	void doesNotPutMissingByteExtensionType() {
		assertThrows(IllegalArgumentException.class, () -> {
			f.putExtensionType("png", "image/png");
		});
	}

	@Test
	void doesNotPutMissingTextExtensionType() {
		assertThrows(IllegalArgumentException.class, () -> {
			f.putExtensionType("xml", "application/xml");
		});
	}

	@Test
	void initializesByteArrayAsBinary() {
		assertTrue(f.isBinary(byte[].class));
	}

	@Test
	void initializesInputStreamAsBinary() {
		assertTrue(f.isBinary(InputStream.class));
	}

	@Test
	void initializesByteArrayInputStreamAsBinary() {
		assertTrue(f.isBinary(ByteArrayInputStream.class));
	}

	@Test
	void initializesOutputStreamConsumerAsBinary() {
		assertTrue(f.isBinary(new Hint<Consumer<OutputStream>>() {}.getType()));
	}

	@Test
	void doesNotInitializeReadableByteChannelAsBinary() {
		assertFalse(f.isBinary(ReadableByteChannel.class));
	}

	@Test
	void doesNotInitializeWritableByteChannelConsumerAsBinary() {
		assertFalse(f.isBinary(new Hint<Consumer<WritableByteChannel>>() {}.getType()));
	}

	@Test
	void addsReadableByteChannelAsBinary() {
		f.addBinary(ReadableByteChannel.class);
		assertTrue(f.isBinary(ReadableByteChannel.class));
	}

	@Test
	void addsWritableByteChannelConsumerAsBinary() {
		f.addBinary(new Hint<Consumer<WritableByteChannel>>() {}.getType());
		assertTrue(f.isBinary(new Hint<Consumer<WritableByteChannel>>() {}.getType()));
	}

	@Test
	void doesNotSetNullBinaryFallbackType() {
		assertThrows(NullPointerException.class, () -> {
			f.setBinaryFallbackType(null);
		});
	}

	@Test
	void doesNotSetBlankBinaryFallbackType() {
		assertThrows(IllegalArgumentException.class, () -> {
			f.setBinaryFallbackType("");
		});
	}

	@Test
	void doesNotSetNullFallbackType() {
		assertThrows(NullPointerException.class, () -> {
			f.setFallbackType(null);
		});
	}

	@Test
	void doesNotSetBlankFallbackType() {
		assertThrows(IllegalArgumentException.class, () -> {
			f.setFallbackType("");
		});
	}

	@Test
	void setsAndGetsFallbackAssemblerType() {
		Object body = new Object();
		f.setBinaryFallbackType("image/png;parameter");
		assertEquals("image/png", f.getAssemblerType(null, body, Object.class));
	}

	@Test
	void setsAndGetsFallbackDisassemblerType() {
		f.setBinaryFallbackType("image/png;parameter");
		assertEquals("image/png", f.getDisassemblerType(null, Object.class));
	}

	@Test
	void setsAndGetsFallbackSerializerType() {
		Object body = new Object();
		f.setFallbackType("application/xml;parameter");
		assertEquals("application/xml", f.getSerializerType(null, body, Object.class));
	}

	@Test
	void setsAndGetsFallbackDeserializerType() {
		f.setFallbackType("application/xml;parameter");
		assertEquals("application/xml", f.getDeserializerType(null, Object.class));
	}

	@Test
	void getsAssemblerType() {
		String contentType = "application/octet-stream";
		assertEquals(contentType, f.getAssemblerType(contentType, new Object(), Object.class));
	}

	@Test
	void getsByteArrayAssemblerType() {
		byte[] body = new byte[] {};
		assertEquals("application/octet-stream", f.getAssemblerType(null, body, byte[].class));
	}

	@Test
	void getsInputStreamAssemblerType() {
		InputStream body = InputStream.nullInputStream();
		assertEquals("application/octet-stream", f.getAssemblerType(null, body, InputStream.class));
	}

	@Test
	void getsOutputStreamConsumerAssemblerType() {
		Consumer<OutputStream> body = (stream) -> {};
		assertEquals("application/octet-stream", f.getAssemblerType(null, body, new Hint<Consumer<OutputStream>>() {}.getType()));
	}

	@Test
	void doesNotGetMissingAssemblerType() {
		Object body = new Object();
		assertThrows(IllegalStateException.class, () -> {
			f.getAssemblerType(null, body, Object.class);
		});
	}

	@Test
	void getsDisassemblerType() {
		String contentType = "application/octet-stream";
		assertEquals(contentType, f.getDisassemblerType(contentType, Object.class));
	}

	@Test
	void getsByteArrayDisssemblerType() {
		assertEquals("application/octet-stream", f.getDisassemblerType(null, byte[].class));
	}

	@Test
	void getsInputStreamDisssemblerType() {
		assertEquals("application/octet-stream", f.getDisassemblerType(null, InputStream.class));
	}

	@Test
	void doesNotGetMissingDisassemblerType() {
		assertThrows(IllegalStateException.class, () -> {
			f.getDisassemblerType(null, Object.class);
		});
	}

	@Test
	void getsSerializerType() {
		String contentType = "text/plain";
		assertEquals(contentType, f.getSerializerType(contentType, new Object(), Object.class));
	}

	@Test
	void getsStringSerializerType() {
		String body = "";
		assertEquals("text/plain", f.getSerializerType(null, body, String.class));
	}

	@Test
	void getsReaderSerializerType() {
		Reader body = Reader.nullReader();
		assertEquals("text/plain", f.getSerializerType(null, body, Reader.class));
	}

	@Test
	void getsWriterConsumerSerializerType() {
		Consumer<Writer> body = (writer) -> {};
		assertEquals("text/plain", f.getSerializerType(null, body, new Hint<Consumer<Writer>>() {}.getType()));
	}

	@Test
	void doesNotGetMissingSerializerType() {
		Object body = new Object();
		assertThrows(IllegalStateException.class, () -> {
			f.getSerializerType(null, body, Object.class);
		});
	}

	@Test
	void getsDeserializerType() {
		String contentType = "text/plain";
		assertEquals(contentType, f.getDeserializerType(contentType, Object.class));
	}

	@Test
	void getsStringDeserializerType() {
		assertEquals("text/plain", f.getDeserializerType(null, String.class));
	}

	@Test
	void getsReaderDeserializerType() {
		assertEquals("text/plain", f.getDeserializerType(null, Reader.class));
	}

	@Test
	void doesNotGetMissingDeserializerType() {
		assertThrows(IllegalStateException.class, () -> {
			f.getDeserializerType(null, Object.class);
		});
	}
}
