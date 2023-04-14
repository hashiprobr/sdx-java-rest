package br.pro.hashi.sdx.rest.server.stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class CountOutputStreamTest {
	private CountOutputStream s;

	@BeforeEach
	void setUp() {
		s = new CountOutputStream();
	}

	@Test
	void initializes() {
		assertEquals(0, s.getCount());
	}

	@Test
	void writes() {
		byte[] b = newBytes();
		s.write(b, 0, 0);
		s.write(b, 0, 1);
		s.write(b, 0, 2);
		s.write(b, 0, 3);
		s.write(b, 1, 0);
		s.write(b, 1, 1);
		s.write(b, 1, 2);
		s.write(b, 2, 0);
		s.write(b, 2, 1);
		s.write(b, 3, 0);
		s.write(b);
		s.write(0);
		assertEquals(14, s.getCount());
	}

	@Test
	void writesZeroAndZero() {
		assertCountEquals(0, 0, 0);
	}

	@Test
	void writesZeroAndOne() {
		assertCountEquals(1, 0, 1);
	}

	@Test
	void writesZeroAndTwo() {
		assertCountEquals(2, 0, 2);
	}

	@Test
	void writesZeroAndThree() {
		assertCountEquals(3, 0, 3);
	}

	@Test
	void writesOneAndZero() {
		assertCountEquals(0, 1, 0);
	}

	@Test
	void writesOneAndOne() {
		assertCountEquals(1, 1, 1);
	}

	@Test
	void writesOneAndTwo() {
		assertCountEquals(2, 1, 2);
	}

	@Test
	void writesTwoAndZero() {
		assertCountEquals(0, 2, 0);
	}

	@Test
	void writesTwoAndOne() {
		assertCountEquals(1, 2, 1);
	}

	@Test
	void writesThreeAndZero() {
		assertCountEquals(0, 3, 0);
	}

	private void assertCountEquals(int expected, int off, int len) {
		s.write(newBytes(), off, len);
		assertEquals(expected, s.getCount());
	}

	@Test
	void doesNotWriteNegativeAndZero() {
		assertWriteThrows(-1, -1, 0);
	}

	@Test
	void doesNotWriteNegativeAndOne() {
		assertWriteThrows(-1, -1, 1);
	}

	@Test
	void doesNotWriteNegativeAndTwo() {
		assertWriteThrows(-1, -1, 2);
	}

	@Test
	void doesNotWriteNegativeAndThree() {
		assertWriteThrows(-1, -1, 3);
	}

	@Test
	void doesNotWriteNegativeAndFour() {
		assertWriteThrows(-1, -1, 4);
	}

	@Test
	void doesNotWriteZeroAndNegative() {
		assertWriteThrows(-1, 0, -1);
	}

	@Test
	void doesNotWriteZeroAndFour() {
		assertWriteThrows(3, 0, 4);
	}

	@Test
	void doesNotWriteOneAndNegative() {
		assertWriteThrows(-1, 1, -1);
	}

	@Test
	void doesNotWriteOneAndThree() {
		assertWriteThrows(3, 1, 3);
	}

	@Test
	void doesNotWriteOneAndFour() {
		assertWriteThrows(4, 1, 4);
	}

	@Test
	void doesNotWriteTwoAndNegative() {
		assertWriteThrows(-1, 2, -1);
	}

	@Test
	void doesNotWriteTwoAndTwo() {
		assertWriteThrows(3, 2, 2);
	}

	@Test
	void doesNotWriteTwoAndThree() {
		assertWriteThrows(4, 2, 3);
	}

	@Test
	void doesNotWriteThreeAndNegative() {
		assertWriteThrows(-1, 3, -1);
	}

	@Test
	void doesNotWriteThreeAndOne() {
		assertWriteThrows(3, 3, 1);
	}

	@Test
	void doesNotWriteThreeAndTwo() {
		assertWriteThrows(4, 3, 2);
	}

	@Test
	void doesNotWriteFourAndNegative() {
		assertWriteThrows(-1, 4, -1);
	}

	@Test
	void doesNotWriteFourAndZero() {
		assertWriteThrows(3, 4, 0);
	}

	@Test
	void doesNotWriteFourAndOne() {
		assertWriteThrows(4, 4, 1);
	}

	private void assertWriteThrows(int expected, int off, int len) {
		Exception exception = assertThrows(IndexOutOfBoundsException.class, () -> {
			s.write(newBytes(), off, len);
		});
		assertEquals("Index out of range: %d".formatted(expected), exception.getMessage());
		assertEquals(0, s.getCount());
	}

	@Test
	void doesNotWrite() {
		assertThrows(NullPointerException.class, () -> {
			s.write(null, 0, 0);
		});
		assertEquals(0, s.getCount());
	}

	@Test
	void writesAll() {
		s.write(newBytes());
		assertEquals(3, s.getCount());
	}

	private byte[] newBytes() {
		return new byte[] { 0, 1, 2 };
	}

	@Test
	void writesAllOfTwo() {
		s.write(new byte[] { 0, 1 });
		assertEquals(2, s.getCount());
	}

	@Test
	void writesAllOfOne() {
		s.write(new byte[] { 0 });
		assertEquals(1, s.getCount());
	}

	@Test
	void writesAllOfZero() {
		s.write(new byte[] {});
		assertEquals(0, s.getCount());
	}

	@Test
	void doesNotWriteAll() {
		assertThrows(NullPointerException.class, () -> {
			s.write(null);
		});
		assertEquals(0, s.getCount());
	}

	@Test
	void writesSingle() {
		s.write(0);
		assertEquals(1, s.getCount());
	}
}
