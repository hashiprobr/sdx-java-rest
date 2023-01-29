package br.pro.hashi.sdx.rest.fields;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.function.Function;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import br.pro.hashi.sdx.rest.fields.exception.FieldsException;
import br.pro.hashi.sdx.rest.fields.mock.WithInvalidInputType;
import br.pro.hashi.sdx.rest.fields.mock.WithInvalidMethod;
import br.pro.hashi.sdx.rest.fields.mock.WithInvalidOutputType;
import br.pro.hashi.sdx.rest.fields.mock.WithMethod;
import br.pro.hashi.sdx.rest.fields.mock.WithNonPublicMethod;
import br.pro.hashi.sdx.rest.fields.mock.WithNonStaticMethod;
import br.pro.hashi.sdx.rest.fields.mock.WithoutMethod;

class CacheTest {
	private final static double DELTA = 0.000001;

	private Cache c;
	private int size;

	@BeforeEach
	void setUp() {
		c = new Cache();
		size = c.size();
	}

	@Test
	void initializesWithBoolean() {
		assertFalse(c.get(boolean.class).apply("false"));
		assertEquals(size, c.size());
	}

	@Test
	void initializesWithByte() {
		assertEquals((byte) 0, c.get(byte.class).apply("0"));
		assertEquals(size, c.size());
	}

	@Test
	void initializesWithShort() {
		assertEquals((short) 0, c.get(short.class).apply("0"));
		assertEquals(size, c.size());
	}

	@Test
	void initializesWithInt() {
		assertEquals(0, c.get(int.class).apply("0"));
		assertEquals(size, c.size());
	}

	@Test
	void initializesWithLong() {
		assertEquals(0, c.get(long.class).apply("0"));
		assertEquals(size, c.size());
	}

	@Test
	void initializesWithFloat() {
		assertEquals(0.0, c.get(float.class).apply("0.0"), DELTA);
		assertEquals(size, c.size());
	}

	@Test
	void initializesWithDouble() {
		assertEquals(0.0, c.get(double.class).apply("0.0"), DELTA);
		assertEquals(size, c.size());
	}

	@Test
	void initializesWithBigInteger() {
		assertEquals(BigInteger.valueOf(0), c.get(BigInteger.class).apply("0"));
		assertEquals(size, c.size());
	}

	@Test
	void initializesWithBigDecimal() {
		assertEquals(BigDecimal.valueOf(0.0), c.get(BigDecimal.class).apply("0.0"));
		assertEquals(size, c.size());
	}

	@Test
	void initializesWithString() {
		String s = "";
		assertSame(s, c.get(String.class).apply(s));
		assertEquals(size, c.size());
	}

	@Test
	void getsAndInvokesTwice() {
		assertNotNull(c.get(WithMethod.class).apply(""));
		assertEquals(size + 1, c.size());
		assertNotNull(c.get(WithMethod.class).apply(""));
		assertEquals(size + 1, c.size());
	}

	@Test
	void doesNotGetWithoutMethod() {
		assertThrows(FieldsException.class, () -> {
			c.get(WithoutMethod.class);
		});
		assertEquals(size, c.size());
	}

	@Test
	void doesNotGetWithInvalidInputType() {
		assertThrows(FieldsException.class, () -> {
			c.get(WithInvalidInputType.class);
		});
		assertEquals(size, c.size());
	}

	@Test
	void doesNotGetWithInvalidOutputType() {
		assertThrows(FieldsException.class, () -> {
			c.get(WithInvalidOutputType.class);
		});
		assertEquals(size, c.size());
	}

	@Test
	void doesNotGetWithNonPublicMethod() {
		assertThrows(FieldsException.class, () -> {
			c.get(WithNonPublicMethod.class);
		});
		assertEquals(size, c.size());
	}

	@Test
	void doesNotGetWithNonStaticMethod() {
		assertThrows(FieldsException.class, () -> {
			c.get(WithNonStaticMethod.class);
		});
		assertEquals(size, c.size());
	}

	@Test
	void doesNotInvokeWithInvalidMethod() {
		Function<String, WithInvalidMethod> function = c.get(WithInvalidMethod.class);
		assertThrows(FieldsException.class, () -> {
			function.apply("");
		});
		assertEquals(size + 1, c.size());
	}
}
