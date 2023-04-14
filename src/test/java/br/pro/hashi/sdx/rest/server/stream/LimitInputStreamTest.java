package br.pro.hashi.sdx.rest.server.stream;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Arrays;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import br.pro.hashi.sdx.rest.server.RestException;
import br.pro.hashi.sdx.rest.server.exception.PayloadTooLargeException;

class LimitInputStreamTest {
	private ByteArrayInputStream stream;
	private LimitInputStream s;

	@BeforeEach
	void setUp() {
		stream = new ByteArrayInputStream(newByteArray(255));
	}

	@Test
	void initializesWithZero() {
		s = newLimitInputStream();
		assertEquals(0, s.getSize());
	}

	@Test
	void readsByte() throws IOException {
		s = newLimitInputStream();
		assertEquals(255, s.read());
		assertEquals(1, s.getSize());
	}

	@Test
	void readsToAllBytes() throws IOException {
		s = newLimitInputStream();
		int total = 0;
		byte[] bytes = newByteArray(0);
		while (total < bytes.length) {
			total += s.read(bytes);
		}
		assertArrayEquals(newByteArray(255), bytes);
		assertEquals(-1, s.read(bytes));
		assertEquals(10, s.getSize());
	}

	@Test
	void readsToBytes() throws IOException {
		s = newLimitInputStream();
		int off = 0;
		int len = 10;
		byte[] bytes = newByteArray(0);
		while (len > 0) {
			int length = s.read(bytes, off, len);
			off += length;
			len -= length;
		}
		assertArrayEquals(newByteArray(255), bytes);
		assertEquals(-1, s.read(bytes, 0, 10));
		assertEquals(10, s.getSize());
	}

	@Test
	void readsAllBytes() throws IOException {
		s = newLimitInputStream();
		assertArrayEquals(newByteArray(255), s.readAllBytes());
		assertEquals(10, s.getSize());
	}

	@Test
	void readsOfAllBytes() throws IOException {
		s = newLimitInputStream();
		assertArrayEquals(newByteArray(255), s.readNBytes(10));
		assertEquals(10, s.getSize());
	}

	@Test
	void readsOfBytes() throws IOException {
		s = newLimitInputStream();
		byte[] bytes = newByteArray(0);
		assertEquals(10, s.readNBytes(bytes, 0, 10));
		assertArrayEquals(newByteArray(255), bytes);
		assertEquals(10, s.getSize());
	}

	@Test
	void skips() throws IOException {
		s = newLimitInputStream();
		int n = 10;
		while (n > 0) {
			long length = s.skip(n);
			n -= length;
		}
		assertEquals(-1, s.read());
		assertEquals(10, s.getSize());
	}

	@Test
	void skipsOfAllBytes() throws IOException {
		s = newLimitInputStream();
		s.skipNBytes(10);
		assertEquals(-1, s.read());
		assertEquals(10, s.getSize());
	}

	@Test
	void sumAllReads() throws IOException {
		s = newLimitInputStream(10);
		s.read();
		s.readNBytes(2);
		s.skipNBytes(3);
		assertEquals(6, s.getSize());
	}

	@Test
	void doesNotRead() throws IOException {
		s = newLimitInputStream(5);
		RestException exception = assertThrows(PayloadTooLargeException.class, () -> {
			s.readAllBytes();
		});
		assertEquals("Request body exceeds 5 bytes", exception.getBody());
		assertEquals(10, s.getSize());
	}

	@Test
	void forwards() throws IOException {
		s = newLimitInputStream();
		assertEquals(stream.markSupported(), s.markSupported());
		s.mark(0);
		s.reset();
		assertEquals(stream.available(), s.available());
		s.transferTo(OutputStream.nullOutputStream());
		s.close();
	}

	private byte[] newByteArray(int b) {
		byte[] bytes = new byte[10];
		Arrays.fill(bytes, (byte) b);
		return bytes;
	}

	private LimitInputStream newLimitInputStream() {
		return newLimitInputStream(10);
	}

	private LimitInputStream newLimitInputStream(long maxSize) {
		return new LimitInputStream(stream, maxSize);
	}
}
