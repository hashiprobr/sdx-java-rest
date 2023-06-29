package br.pro.hashi.sdx.rest.transform.manager;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import br.pro.hashi.sdx.rest.coding.MediaCoder;
import br.pro.hashi.sdx.rest.transform.Disassembler;
import br.pro.hashi.sdx.rest.transform.exception.TypeException;

class DefaultDisassemblerTest {
	private AutoCloseable mocks;
	private @Mock MediaCoder coder;
	private Disassembler d;
	private InputStream stream;

	@BeforeEach
	void setUp() {
		mocks = MockitoAnnotations.openMocks(this);

		d = new DefaultDisassembler(coder);

		stream = new ByteArrayInputStream(newByteArray());
	}

	@AfterEach
	void tearDown() {
		assertDoesNotThrow(() -> {
			mocks.close();
		});
	}

	@Test
	void getsInstance() {
		assertInstanceOf(DefaultDisassembler.class, DefaultDisassembler.getInstance());
	}

	@Test
	void readsByteArray() {
		when(coder.read(stream)).thenReturn(newByteArray());
		assertArrayEquals(newByteArray(), d.read(stream, byte[].class));
	}

	@Test
	void readsInputStream() {
		assertSame(stream, d.read(stream, InputStream.class));
	}

	@Test
	void doesNotRead() {
		assertThrows(TypeException.class, () -> {
			d.read(stream, Object.class);
		});
	}

	private byte[] newByteArray() {
		return "body".getBytes(StandardCharsets.US_ASCII);
	}
}
