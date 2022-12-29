package br.pro.hashi.sdx.rest.transformer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockConstruction;
import static org.mockito.Mockito.mockStatic;

import java.io.InputStream;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedConstruction;
import org.mockito.MockedStatic;

import com.google.gson.Gson;

import br.pro.hashi.sdx.rest.coding.Media;
import br.pro.hashi.sdx.rest.transformer.base.Assembler;
import br.pro.hashi.sdx.rest.transformer.base.Deserializer;
import br.pro.hashi.sdx.rest.transformer.base.Disassembler;
import br.pro.hashi.sdx.rest.transformer.base.Serializer;

class TransformerTest {
	private MockedConstruction<ByteAssembler> byteAssembler;
	private MockedConstruction<ByteDisassembler> byteDisassembler;
	private MockedConstruction<TextSerializer> textSerializer;
	private MockedConstruction<GsonSerializer> gsonSerializer;
	private MockedConstruction<TextDeserializer> textDeserializer;
	private MockedConstruction<SafeGsonDeserializer> gsonDeserializer;
	private Transformer t;

	@BeforeEach
	void setUp() {
		byteAssembler = mockConstruction(ByteAssembler.class);
		byteDisassembler = mockConstruction(ByteDisassembler.class);
		textSerializer = mockConstruction(TextSerializer.class);
		gsonSerializer = mockConstruction(GsonSerializer.class);
		textDeserializer = mockConstruction(TextDeserializer.class);
		gsonDeserializer = mockConstruction(SafeGsonDeserializer.class);
		t = new Transformer(mock(Gson.class));
	}

	@AfterEach
	void tearDown() {
		gsonDeserializer.close();
		textDeserializer.close();
		gsonSerializer.close();
		textSerializer.close();
		byteDisassembler.close();
		byteAssembler.close();
	}

	@Test
	void initializesWithInputStream() {
		assertTrue(t.isBinary(InputStream.class));
	}

	@Test
	void initializesWithByteAssembler() {
		assertInstanceOf(ByteAssembler.class, t.getAssembler("application/octet-stream"));
	}

	@Test
	void initializesWithByteDisassembler() {
		assertInstanceOf(ByteDisassembler.class, t.getDisassembler("application/octet-stream"));
	}

	@Test
	void initializesWithTextSerializer() {
		assertInstanceOf(TextSerializer.class, t.getSerializer("text/plain"));
	}

	@Test
	void initializesWithGsonSerializer() {
		assertInstanceOf(GsonSerializer.class, t.getSerializer("application/json"));
	}

	@Test
	void initializesWithTextDeserializer() {
		assertInstanceOf(TextDeserializer.class, t.getDeserializer("text/plain"));
	}

	@Test
	void initializesWithGsonDeserializer() {
		assertInstanceOf(SafeGsonDeserializer.class, t.getDeserializer("application/json"));
	}

	@Test
	void addsObject() {
		assertFalse(t.isBinary(Object.class));
		t.addBinary(Object.class);
		assertTrue(t.isBinary(Object.class));
	}

	@Test
	void doesNotAddNull() {
		assertThrows(IllegalArgumentException.class, () -> {
			t.addBinary(null);
		});
	}

	@Test
	void cleansBinaryToSame() {
		String contentType = "image/png";
		assertSame(contentType, t.cleanBinary(contentType));
	}

	@Test
	void cleansBinaryToByte() {
		assertEquals("application/octet-stream", t.cleanBinary(null));
	}

	@Test
	void cleansSerializerToSame() {
		String contentType = "application/xml";
		assertSame(contentType, t.cleanSerializer(contentType, new Object()));
	}

	@Test
	void cleansSerializerToText() {
		assertEquals("text/plain", t.cleanSerializer(null, "body"));
	}

	@Test
	void cleansSerializerToJson() {
		assertEquals("application/json", t.cleanSerializer(null, new Object()));
	}

	@Test
	void cleansDeserializerToSame() {
		String contentType = "application/xml";
		assertSame(contentType, t.cleanDeserializer(contentType, Object.class));
	}

	@Test
	void cleansDeserializerToText() {
		assertEquals("text/plain", t.cleanDeserializer(null, String.class));
	}

	@Test
	void cleansDeserializerToJson() {
		assertEquals("application/json", t.cleanDeserializer(null, Object.class));
	}

	@Test
	void putsAssembler() {
		try (MockedStatic<Media> media = mockStatic(Media.class)) {
			String contentType = "image/png";
			media.when(() -> Media.strip(contentType)).thenReturn(contentType);
			assertThrows(IllegalArgumentException.class, () -> {
				t.getAssembler(contentType);
			});
			Assembler assembler = mock(Assembler.class);
			t.putAssembler(contentType, assembler);
			assertSame(assembler, t.getAssembler(contentType));
		}
	}

	@Test
	void doesNotPutAssemblerIfStripReturnsNull() {
		try (MockedStatic<Media> media = mockStatic(Media.class)) {
			String contentType = "image/png";
			media.when(() -> Media.strip(contentType)).thenReturn(null);
			Assembler assembler = mock(Assembler.class);
			assertThrows(IllegalArgumentException.class, () -> {
				t.putAssembler(contentType, assembler);
			});
		}
	}

	@Test
	void doesNotPutAssemblerIfTypeIsNull() {
		Assembler assembler = mock(Assembler.class);
		assertThrows(IllegalArgumentException.class, () -> {
			t.putAssembler(null, assembler);
		});
	}

	@Test
	void doesNotPutAssemblerIfItIsNull() {
		try (MockedStatic<Media> media = mockStatic(Media.class)) {
			String contentType = "image/png";
			media.when(() -> Media.strip(contentType)).thenReturn(contentType);
			assertThrows(IllegalArgumentException.class, () -> {
				t.putAssembler(contentType, null);
			});
		}
	}

	@Test
	void removesAssembler() {
		try (MockedStatic<Media> media = mockStatic(Media.class)) {
			String contentType = "application/octet-stream";
			media.when(() -> Media.strip(contentType)).thenReturn(contentType);
			t.removeAssembler(contentType);
			assertThrows(IllegalArgumentException.class, () -> {
				t.getAssembler(contentType);
			});
		}
	}

	@Test
	void doesNotRemoveAssemblerIfStripReturnsNull() {
		try (MockedStatic<Media> media = mockStatic(Media.class)) {
			String contentType = "application/octet-stream";
			media.when(() -> Media.strip(contentType)).thenReturn(null);
			assertThrows(IllegalArgumentException.class, () -> {
				t.removeAssembler(contentType);
			});
		}
	}

	@Test
	void doesNotRemoveAssemblerIfTypeIsNull() {
		assertThrows(IllegalArgumentException.class, () -> {
			t.removeAssembler(null);
		});
	}

	@Test
	void putsDisassembler() {
		try (MockedStatic<Media> media = mockStatic(Media.class)) {
			String contentType = "image/png";
			media.when(() -> Media.strip(contentType)).thenReturn(contentType);
			assertThrows(IllegalArgumentException.class, () -> {
				t.getDisassembler(contentType);
			});
			Disassembler disassembler = mock(Disassembler.class);
			t.putDisassembler(contentType, disassembler);
			assertSame(disassembler, t.getDisassembler(contentType));
		}
	}

	@Test
	void doesNotPutDisassemblerIfStripReturnsNull() {
		try (MockedStatic<Media> media = mockStatic(Media.class)) {
			String contentType = "image/png";
			media.when(() -> Media.strip(contentType)).thenReturn(null);
			Disassembler disassembler = mock(Disassembler.class);
			assertThrows(IllegalArgumentException.class, () -> {
				t.putDisassembler(contentType, disassembler);
			});
		}
	}

	@Test
	void doesNotPutDisassemblerIfTypeIsNull() {
		Disassembler disassembler = mock(Disassembler.class);
		assertThrows(IllegalArgumentException.class, () -> {
			t.putDisassembler(null, disassembler);
		});
	}

	@Test
	void doesNotPutDisassemblerIfItIsNull() {
		try (MockedStatic<Media> media = mockStatic(Media.class)) {
			String contentType = "image/png";
			media.when(() -> Media.strip(contentType)).thenReturn(contentType);
			assertThrows(IllegalArgumentException.class, () -> {
				t.putDisassembler(contentType, null);
			});
		}
	}

	@Test
	void removesDisassembler() {
		try (MockedStatic<Media> media = mockStatic(Media.class)) {
			String contentType = "application/octet-stream";
			media.when(() -> Media.strip(contentType)).thenReturn(contentType);
			t.removeDisassembler(contentType);
			assertThrows(IllegalArgumentException.class, () -> {
				t.getDisassembler(contentType);
			});
		}
	}

	@Test
	void doesNotRemoveDisssemblerIfStripReturnsNull() {
		try (MockedStatic<Media> media = mockStatic(Media.class)) {
			String contentType = "application/octet-stream";
			media.when(() -> Media.strip(contentType)).thenReturn(null);
			assertThrows(IllegalArgumentException.class, () -> {
				t.removeDisassembler(contentType);
			});
		}
	}

	@Test
	void doesNotRemoveDisssemblerIfTypeIsNull() {
		assertThrows(IllegalArgumentException.class, () -> {
			t.removeDisassembler(null);
		});
	}

	@Test
	void putsSerializer() {
		try (MockedStatic<Media> media = mockStatic(Media.class)) {
			String contentType = "application/xml";
			media.when(() -> Media.strip(contentType)).thenReturn(contentType);
			assertThrows(IllegalArgumentException.class, () -> {
				t.getSerializer(contentType);
			});
			Serializer serializer = mock(Serializer.class);
			t.putSerializer(contentType, serializer);
			assertSame(serializer, t.getSerializer(contentType));
		}
	}

	@Test
	void doesNotPutSerializerIfStripReturnsNull() {
		try (MockedStatic<Media> media = mockStatic(Media.class)) {
			String contentType = "application/xml";
			media.when(() -> Media.strip(contentType)).thenReturn(null);
			Serializer serializer = mock(Serializer.class);
			assertThrows(IllegalArgumentException.class, () -> {
				t.putSerializer(contentType, serializer);
			});
		}
	}

	@Test
	void doesNotPutSerializerIfTypeIsNull() {
		Serializer serializer = mock(Serializer.class);
		assertThrows(IllegalArgumentException.class, () -> {
			t.putSerializer(null, serializer);
		});
	}

	@Test
	void doesNotPutSerializerIfItIsNull() {
		try (MockedStatic<Media> media = mockStatic(Media.class)) {
			String contentType = "application/xml";
			media.when(() -> Media.strip(contentType)).thenReturn(contentType);
			assertThrows(IllegalArgumentException.class, () -> {
				t.putSerializer(contentType, null);
			});
		}
	}

	@Test
	void putsGsonSerializer() {
		String contentType = "application/json";
		Serializer serializer = t.getSerializer(contentType);
		t.putSerializer(mock(Gson.class));
		Serializer gsonSerializer = t.getSerializer(contentType);
		assertInstanceOf(GsonSerializer.class, gsonSerializer);
		assertNotSame(serializer, gsonSerializer);
	}

	@Test
	void doesNotPutGsonSerializer() {
		assertThrows(IllegalArgumentException.class, () -> {
			t.putSerializer(null);
		});
	}

	@Test
	void removesSerializer() {
		try (MockedStatic<Media> media = mockStatic(Media.class)) {
			String contentType = "text/plain";
			media.when(() -> Media.strip(contentType)).thenReturn(contentType);
			t.removeSerializer(contentType);
			assertThrows(IllegalArgumentException.class, () -> {
				t.getSerializer(contentType);
			});
		}
	}

	@Test
	void doesNotRemoveSerializerfStripReturnsNull() {
		try (MockedStatic<Media> media = mockStatic(Media.class)) {
			String contentType = "text/plain";
			media.when(() -> Media.strip(contentType)).thenReturn(null);
			assertThrows(IllegalArgumentException.class, () -> {
				t.removeSerializer(contentType);
			});
		}
	}

	@Test
	void doesNotRemoveSerializerfTypeIsNull() {
		assertThrows(IllegalArgumentException.class, () -> {
			t.removeSerializer(null);
		});
	}

	@Test
	void putsDeserializer() {
		try (MockedStatic<Media> media = mockStatic(Media.class)) {
			String contentType = "application/xml";
			media.when(() -> Media.strip(contentType)).thenReturn(contentType);
			assertThrows(IllegalArgumentException.class, () -> {
				t.getDeserializer(contentType);
			});
			Deserializer deserializer = mock(Deserializer.class);
			t.putDeserializer(contentType, deserializer);
			assertSame(deserializer, t.getDeserializer(contentType));
		}
	}

	@Test
	void doesNotPutDeserializerIfStripReturnsNull() {
		try (MockedStatic<Media> media = mockStatic(Media.class)) {
			String contentType = "application/xml";
			media.when(() -> Media.strip(contentType)).thenReturn(null);
			Deserializer deserializer = mock(Deserializer.class);
			assertThrows(IllegalArgumentException.class, () -> {
				t.putDeserializer(contentType, deserializer);
			});
		}
	}

	@Test
	void doesNotPutDeserializerIfTypeIsNull() {
		Deserializer deserializer = mock(Deserializer.class);
		assertThrows(IllegalArgumentException.class, () -> {
			t.putDeserializer(null, deserializer);
		});
	}

	@Test
	void doesNotPutDeserializerIfItIsNull() {
		try (MockedStatic<Media> media = mockStatic(Media.class)) {
			String contentType = "application/xml";
			media.when(() -> Media.strip(contentType)).thenReturn(contentType);
			assertThrows(IllegalArgumentException.class, () -> {
				t.putDeserializer(contentType, null);
			});
		}
	}

	@Test
	void putsGsonDeserializer() {
		String contentType = "application/json";
		Deserializer deserializer = t.getDeserializer(contentType);
		t.putDeserializer(mock(Gson.class));
		Deserializer gsonDeserializer = t.getDeserializer(contentType);
		assertInstanceOf(GsonDeserializer.class, gsonDeserializer);
		assertNotSame(deserializer, gsonDeserializer);
	}

	@Test
	void doesNotPutGsonDeserializer() {
		assertThrows(IllegalArgumentException.class, () -> {
			t.putDeserializer(null);
		});
	}

	@Test
	void putsSafeGsonDeserializer() {
		String contentType = "application/json";
		Deserializer deserializer = t.getDeserializer(contentType);
		t.putSafeDeserializer(mock(Gson.class));
		Deserializer gsonDeserializer = t.getDeserializer(contentType);
		assertInstanceOf(SafeGsonDeserializer.class, gsonDeserializer);
		assertNotSame(deserializer, gsonDeserializer);
	}

	@Test
	void removesDeserializer() {
		try (MockedStatic<Media> media = mockStatic(Media.class)) {
			String contentType = "text/plain";
			media.when(() -> Media.strip(contentType)).thenReturn(contentType);
			t.removeDeserializer(contentType);
			assertThrows(IllegalArgumentException.class, () -> {
				t.getDeserializer(contentType);
			});
		}
	}

	@Test
	void doesNotRemoveDeserializerfStripReturnsNull() {
		try (MockedStatic<Media> media = mockStatic(Media.class)) {
			String contentType = "text/plain";
			media.when(() -> Media.strip(contentType)).thenReturn(null);
			assertThrows(IllegalArgumentException.class, () -> {
				t.removeDeserializer(contentType);
			});
		}
	}

	@Test
	void doesNotRemoveDeserializerfTypeIsNull() {
		assertThrows(IllegalArgumentException.class, () -> {
			t.removeDeserializer(null);
		});
	}
}
