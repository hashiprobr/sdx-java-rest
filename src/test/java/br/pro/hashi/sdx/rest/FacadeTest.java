package br.pro.hashi.sdx.rest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockConstruction;
import static org.mockito.Mockito.mockStatic;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.Reader;
import java.io.StringReader;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.MockedConstruction;
import org.mockito.MockedStatic;

import br.pro.hashi.sdx.rest.coding.Media;
import br.pro.hashi.sdx.rest.transform.Assembler;
import br.pro.hashi.sdx.rest.transform.Deserializer;
import br.pro.hashi.sdx.rest.transform.Disassembler;
import br.pro.hashi.sdx.rest.transform.Serializer;
import br.pro.hashi.sdx.rest.transform.basic.BasicAssembler;
import br.pro.hashi.sdx.rest.transform.basic.BasicDeserializer;
import br.pro.hashi.sdx.rest.transform.basic.BasicDisassembler;
import br.pro.hashi.sdx.rest.transform.basic.BasicSerializer;

class FacadeTest {
	private MockedConstruction<BasicAssembler> assemblerConstruction;
	private MockedConstruction<BasicDisassembler> disassemblerConstruction;
	private MockedConstruction<BasicSerializer> serializerConstruction;
	private MockedConstruction<BasicDeserializer> deserializerConstruction;
	private Facade f;

	@BeforeEach
	void setUp() {
		assemblerConstruction = mockConstruction(BasicAssembler.class);
		disassemblerConstruction = mockConstruction(BasicDisassembler.class);
		serializerConstruction = mockConstruction(BasicSerializer.class);
		deserializerConstruction = mockConstruction(BasicDeserializer.class);
		f = new Facade();
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
	void initializesWithInputStreamAsBinary() {
		assertTrue(f.isBinary(InputStream.class));
	}

	@Test
	void initializesWithoutObjectAsBinary() {
		assertFalse(f.isBinary(Object.class));
	}

	@Test
	void initializesWithoutNullAsBinary() {
		assertFalse(f.isBinary(null));
	}

	@Test
	void initializesWithBasicAssembler() {
		assertInstanceOf(BasicAssembler.class, f.getAssembler("application/octet-stream"));
	}

	@Test
	void initializesWithoutPNGAssembler() {
		assertThrows(IllegalArgumentException.class, () -> {
			f.getAssembler("image/png");
		});
	}

	@Test
	void initializesWithoutNullAssembler() {
		assertThrows(IllegalArgumentException.class, () -> {
			f.getAssembler(null);
		});
	}

	@Test
	void initializesWithBasicDisassembler() {
		assertInstanceOf(BasicDisassembler.class, f.getDisassembler("application/octet-stream"));
	}

	@Test
	void initializesWithoutPNGDisassembler() {
		assertThrows(IllegalArgumentException.class, () -> {
			f.getDisassembler("image/png");
		});
	}

	@Test
	void initializesWithoutNullDisassembler() {
		assertThrows(IllegalArgumentException.class, () -> {
			f.getDisassembler(null);
		});
	}

	@Test
	void initializesWithBasicSerializer() {
		assertInstanceOf(BasicSerializer.class, f.getSerializer("text/plain"));
	}

	@Test
	void initializesWithoutXMLSerializer() {
		assertThrows(IllegalArgumentException.class, () -> {
			f.getSerializer("application/xml");
		});
	}

	@Test
	void initializesWithoutNullSerializer() {
		assertThrows(IllegalArgumentException.class, () -> {
			f.getSerializer(null);
		});
	}

	@Test
	void initializesWithBasicDeserializer() {
		assertInstanceOf(BasicDeserializer.class, f.getDeserializer("text/plain"));
	}

	@Test
	void initializesWithoutXMLDeserializer() {
		assertThrows(IllegalArgumentException.class, () -> {
			f.getDeserializer("application/xml");
		});
	}

	@Test
	void initializesWithoutNullDeserializer() {
		assertThrows(IllegalArgumentException.class, () -> {
			f.getDeserializer(null);
		});
	}

	@Test
	void addsObjectAsBinary() {
		f.addBinary(Object.class);
		assertTrue(f.isBinary(Object.class));
	}

	@Test
	void doesNotAddNullAsBinary() {
		assertThrows(NullPointerException.class, () -> {
			f.addBinary(null);
		});
		assertFalse(f.isBinary(null));
	}

	@Test
	void cleansForAssembling() {
		String contentType = "image/png";
		assertSame(contentType, f.cleanForAssembling(contentType, null));
	}

	@Test
	void cleansForAssemblingIfBodyIsByteArray() {
		assertEquals("application/octet-stream", f.cleanForAssembling(null, new byte[] {}));
	}

	@Test
	void cleansForAssemblingIfBodyIsInputStream() {
		assertEquals("application/octet-stream", f.cleanForAssembling(null, new ByteArrayInputStream(new byte[] {})));
	}

	@Test
	void cleansForAssemblingIfBodyIsObject() {
		try (MockedStatic<Media> media = mockStatic(Media.class)) {
			String fallbackByteType = "image/png";
			media.when(() -> Media.strip(fallbackByteType)).thenReturn(fallbackByteType);
			f.setFallbackByteType(fallbackByteType);
			assertSame(fallbackByteType, f.cleanForAssembling(null, new Object()));
		}
	}

	@Test
	void doesNotCleanForAssembling() {
		assertThrows(IllegalArgumentException.class, () -> {
			f.cleanForAssembling(null, new Object());
		});
	}

	@Test
	void cleansForDisassembling() {
		String contentType = "image/png";
		assertSame(contentType, f.cleanForDisassembling(contentType, null));
	}

	@Test
	void cleansForDisassemblingIfTypeEqualsByteArray() {
		assertEquals("application/octet-stream", f.cleanForDisassembling(null, byte[].class));
	}

	@Test
	void cleansForDisassemblingIfTypeEqualsInputStream() {
		assertEquals("application/octet-stream", f.cleanForDisassembling(null, InputStream.class));
	}

	@Test
	void cleansForDisassemblingIfTypeEqualsObject() {
		try (MockedStatic<Media> media = mockStatic(Media.class)) {
			String fallbackByteType = "image/png";
			media.when(() -> Media.strip(fallbackByteType)).thenReturn(fallbackByteType);
			f.setFallbackByteType(fallbackByteType);
			assertSame(fallbackByteType, f.cleanForDisassembling(null, Object.class));
		}
	}

	@Test
	void doesNotCleanForDisassembling() {
		assertThrows(IllegalArgumentException.class, () -> {
			f.cleanForDisassembling(null, Object.class);
		});
	}

	@Test
	void cleansForSerializing() {
		String contentType = "application/xml";
		assertSame(contentType, f.cleanForSerializing(contentType, null));
	}

	@Test
	void cleansForSerializingIfBodyIsString() {
		assertEquals("text/plain", f.cleanForSerializing(null, ""));
	}

	@Test
	void cleansForSerializingIfBodyIsReader() {
		assertEquals("text/plain", f.cleanForSerializing(null, new StringReader("")));
	}

	@Test
	void cleansForSerializingIfBodyIsObject() {
		try (MockedStatic<Media> media = mockStatic(Media.class)) {
			String fallbackTextType = "application/xml";
			media.when(() -> Media.strip(fallbackTextType)).thenReturn(fallbackTextType);
			f.setFallbackTextType(fallbackTextType);
			assertSame(fallbackTextType, f.cleanForSerializing(null, new Object()));
		}
	}

	@Test
	void doesNotCleanForSerializing() {
		assertThrows(IllegalArgumentException.class, () -> {
			f.cleanForSerializing(null, new Object());
		});
	}

	@Test
	void cleansForDeserializing() {
		String contentType = "application/xml";
		assertSame(contentType, f.cleanForDeserializing(contentType, null));
	}

	@Test
	void cleansForDeserializingIfTypeEqualsString() {
		assertEquals("text/plain", f.cleanForDeserializing(null, String.class));
	}

	@Test
	void cleansForDeserializingIfTypeEqualsReader() {
		assertEquals("text/plain", f.cleanForDeserializing(null, Reader.class));
	}

	@Test
	void cleansForDeserializingIfTypeEqualsObject() {
		try (MockedStatic<Media> media = mockStatic(Media.class)) {
			String fallbackTextType = "application/xml";
			media.when(() -> Media.strip(fallbackTextType)).thenReturn(fallbackTextType);
			f.setFallbackTextType(fallbackTextType);
			assertSame(fallbackTextType, f.cleanForDeserializing(null, Object.class));
		}
	}

	@Test
	void doesNotCleanForDeserializing() {
		assertThrows(IllegalArgumentException.class, () -> {
			f.cleanForDeserializing(null, Object.class);
		});
	}

	@ParameterizedTest
	@ValueSource(strings = {
			"application/octet-stream",
			"image/png" })
	void putsAssembler(String contentType) {
		try (MockedStatic<Media> media = mockStatic(Media.class)) {
			media.when(() -> Media.strip(contentType)).thenReturn(contentType);
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
		assertThrows(IllegalArgumentException.class, () -> {
			f.getAssembler(null);
		});
	}

	@Test
	void doesNotPutAssemblerIfStripReturnsNull() {
		try (MockedStatic<Media> media = mockStatic(Media.class)) {
			String contentType = "image/png";
			media.when(() -> Media.strip(contentType)).thenReturn(null);
			Assembler assembler = mock(Assembler.class);
			assertThrows(IllegalArgumentException.class, () -> {
				f.putAssembler(contentType, assembler);
			});
			assertThrows(IllegalArgumentException.class, () -> {
				f.getAssembler(contentType);
			});
		}
	}

	@Test
	void doesNotPutAssemblerIfItIsNull() {
		try (MockedStatic<Media> media = mockStatic(Media.class)) {
			String contentType = "image/png";
			media.when(() -> Media.strip(contentType)).thenReturn(contentType);
			assertThrows(NullPointerException.class, () -> {
				f.putAssembler(contentType, null);
			});
			assertThrows(IllegalArgumentException.class, () -> {
				f.getAssembler(contentType);
			});
		}
	}

	@ParameterizedTest
	@ValueSource(strings = {
			"application/octet-stream",
			"image/png" })
	void putsDisassembler(String contentType) {
		try (MockedStatic<Media> media = mockStatic(Media.class)) {
			media.when(() -> Media.strip(contentType)).thenReturn(contentType);
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
		assertThrows(IllegalArgumentException.class, () -> {
			f.getDisassembler(null);
		});
	}

	@Test
	void doesNotPutDisassemblerIfStripReturnsNull() {
		try (MockedStatic<Media> media = mockStatic(Media.class)) {
			String contentType = "image/png";
			media.when(() -> Media.strip(contentType)).thenReturn(null);
			Disassembler disassembler = mock(Disassembler.class);
			assertThrows(IllegalArgumentException.class, () -> {
				f.putDisassembler(contentType, disassembler);
			});
			assertThrows(IllegalArgumentException.class, () -> {
				f.getDisassembler(contentType);
			});
		}
	}

	@Test
	void doesNotPutDisassemblerIfItIsNull() {
		try (MockedStatic<Media> media = mockStatic(Media.class)) {
			String contentType = "image/png";
			media.when(() -> Media.strip(contentType)).thenReturn(contentType);
			assertThrows(NullPointerException.class, () -> {
				f.putDisassembler(contentType, null);
			});
			assertThrows(IllegalArgumentException.class, () -> {
				f.getDisassembler(contentType);
			});
		}
	}

	@ParameterizedTest
	@ValueSource(strings = {
			"text/plain",
			"application/xml" })
	void putsSerializer(String contentType) {
		try (MockedStatic<Media> media = mockStatic(Media.class)) {
			media.when(() -> Media.strip(contentType)).thenReturn(contentType);
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
		assertThrows(IllegalArgumentException.class, () -> {
			f.getSerializer(null);
		});
	}

	@Test
	void doesNotPutSerializerIfStripReturnsNull() {
		try (MockedStatic<Media> media = mockStatic(Media.class)) {
			String contentType = "application/xml";
			media.when(() -> Media.strip(contentType)).thenReturn(null);
			Serializer serializer = mock(Serializer.class);
			assertThrows(IllegalArgumentException.class, () -> {
				f.putSerializer(contentType, serializer);
			});
			assertThrows(IllegalArgumentException.class, () -> {
				f.getSerializer(contentType);
			});
		}
	}

	@Test
	void doesNotPutSerializerIfItIsNull() {
		try (MockedStatic<Media> media = mockStatic(Media.class)) {
			String contentType = "application/xml";
			media.when(() -> Media.strip(contentType)).thenReturn(contentType);
			assertThrows(NullPointerException.class, () -> {
				f.putSerializer(contentType, null);
			});
			assertThrows(IllegalArgumentException.class, () -> {
				f.getSerializer(contentType);
			});
		}
	}

	@ParameterizedTest
	@ValueSource(strings = {
			"text/plain",
			"application/xml" })
	void putsDeserializer(String contentType) {
		try (MockedStatic<Media> media = mockStatic(Media.class)) {
			media.when(() -> Media.strip(contentType)).thenReturn(contentType);
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
		assertThrows(IllegalArgumentException.class, () -> {
			f.getDeserializer(null);
		});
	}

	@Test
	void doesNotPutDeserializerIfStripReturnsNull() {
		try (MockedStatic<Media> media = mockStatic(Media.class)) {
			String contentType = "application/xml";
			media.when(() -> Media.strip(contentType)).thenReturn(null);
			Deserializer deserializer = mock(Deserializer.class);
			assertThrows(IllegalArgumentException.class, () -> {
				f.putDeserializer(contentType, deserializer);
			});
			assertThrows(IllegalArgumentException.class, () -> {
				f.getDeserializer(contentType);
			});
		}
	}

	@Test
	void doesNotPutDeserializerIfItIsNull() {
		try (MockedStatic<Media> media = mockStatic(Media.class)) {
			String contentType = "application/xml";
			media.when(() -> Media.strip(contentType)).thenReturn(contentType);
			assertThrows(NullPointerException.class, () -> {
				f.putDeserializer(contentType, null);
			});
			assertThrows(IllegalArgumentException.class, () -> {
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
		try (MockedStatic<Media> media = mockStatic(Media.class)) {
			String contentType = "application/octet-stream";
			media.when(() -> Media.strip(contentType)).thenReturn(null);
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
		try (MockedStatic<Media> media = mockStatic(Media.class)) {
			String contentType = "text/plain";
			media.when(() -> Media.strip(contentType)).thenReturn(null);
			assertThrows(IllegalArgumentException.class, () -> {
				f.setFallbackTextType(contentType);
			});
		}
	}
}
