package br.pro.hashi.sdx.rest.transform.manager;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

import java.io.Reader;
import java.io.StringReader;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import br.pro.hashi.sdx.rest.coding.MediaCoder;
import br.pro.hashi.sdx.rest.reflection.ParserFactory;
import br.pro.hashi.sdx.rest.transform.Deserializer;
import br.pro.hashi.sdx.rest.transform.exception.TypeException;

class DefaultDeserializerTest {
	private AutoCloseable mocks;
	private @Mock ParserFactory factory;
	private @Mock MediaCoder coder;
	private Deserializer d;
	private Reader reader;

	@BeforeEach
	void setUp() {
		mocks = MockitoAnnotations.openMocks(this);

		d = new DefaultDeserializer(factory, coder);

		reader = new StringReader(newString());
	}

	@AfterEach
	void tearDown() {
		assertDoesNotThrow(() -> {
			mocks.close();
		});
	}

	@Test
	void getsInstance() {
		assertInstanceOf(DefaultDeserializer.class, DefaultDeserializer.getInstance());
	}

	@Test
	void readsString() {
		when(factory.get(String.class)).thenReturn((valueString) -> valueString);
		when(coder.read(reader)).thenReturn(newString());
		assertEquals(newString(), d.read(reader, String.class));
	}

	@Test
	void readsReader() {
		assertSame(reader, d.read(reader, Reader.class));
	}

	@Test
	void doesNotRead() {
		assertThrows(TypeException.class, () -> {
			d.read(reader, Object.class);
		});
	}

	private String newString() {
		return "body";
	}
}
