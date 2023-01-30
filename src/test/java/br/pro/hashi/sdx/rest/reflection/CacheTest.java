package br.pro.hashi.sdx.rest.reflection;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.function.Function;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import br.pro.hashi.sdx.rest.reflection.exception.ReflectionException;
import br.pro.hashi.sdx.rest.reflection.mock.cache.WithInvalidInputType;
import br.pro.hashi.sdx.rest.reflection.mock.cache.WithInvalidMethod;
import br.pro.hashi.sdx.rest.reflection.mock.cache.WithInvalidOutputType;
import br.pro.hashi.sdx.rest.reflection.mock.cache.WithMethod;
import br.pro.hashi.sdx.rest.reflection.mock.cache.WithNonPublicMethod;
import br.pro.hashi.sdx.rest.reflection.mock.cache.WithNonStaticMethod;
import br.pro.hashi.sdx.rest.reflection.mock.cache.WithoutMethod;

class CacheTest {
	private final static double DELTA = 0.000001;

	private Cache c;

	@BeforeEach
	void setUp() {
		c = new Cache();
	}

	@Test
	void initializesWithBoolean() {
		assertTrue(c.getFunctions().containsKey(boolean.class));
		assertTrue(c.get(boolean.class).apply("true"));
		assertTrue(c.getFunctions().containsKey(boolean.class));
	}

	@Test
	void initializesWithByte() {
		assertTrue(c.getFunctions().containsKey(byte.class));
		assertEquals((byte) 1, c.get(byte.class).apply("1"));
		assertTrue(c.getFunctions().containsKey(byte.class));
	}

	@Test
	void initializesWithShort() {
		assertTrue(c.getFunctions().containsKey(short.class));
		assertEquals((short) 123, c.get(short.class).apply("123"));
		assertTrue(c.getFunctions().containsKey(short.class));
	}

	@Test
	void initializesWithInt() {
		assertTrue(c.getFunctions().containsKey(int.class));
		assertEquals(1234, c.get(int.class).apply("1234"));
		assertTrue(c.getFunctions().containsKey(int.class));
	}

	@Test
	void initializesWithLong() {
		assertTrue(c.getFunctions().containsKey(long.class));
		assertEquals(12345, c.get(long.class).apply("12345"));
		assertTrue(c.getFunctions().containsKey(long.class));
	}

	@Test
	void initializesWithFloat() {
		assertTrue(c.getFunctions().containsKey(float.class));
		assertEquals(1.2, c.get(float.class).apply("1.2"), DELTA);
		assertTrue(c.getFunctions().containsKey(float.class));
	}

	@Test
	void initializesWithDouble() {
		assertTrue(c.getFunctions().containsKey(double.class));
		assertEquals(12.34, c.get(double.class).apply("12.34"), DELTA);
		assertTrue(c.getFunctions().containsKey(double.class));
	}

	@Test
	void initializesWithBigInteger() {
		assertTrue(c.getFunctions().containsKey(BigInteger.class));
		assertEquals(BigInteger.valueOf(123456), c.get(BigInteger.class).apply("123456"));
		assertTrue(c.getFunctions().containsKey(BigInteger.class));
	}

	@Test
	void initializesWithBigDecimal() {
		assertTrue(c.getFunctions().containsKey(BigDecimal.class));
		assertEquals(BigDecimal.valueOf(123.456), c.get(BigDecimal.class).apply("123.456"));
		assertTrue(c.getFunctions().containsKey(BigDecimal.class));
	}

	@Test
	void initializesWithString() {
		assertTrue(c.getFunctions().containsKey(String.class));
		assertEquals("123456789", c.get(String.class).apply("123456789"));
		assertTrue(c.getFunctions().containsKey(String.class));
	}

	@Test
	void getsAndCalls() {
		assertFalse(c.getFunctions().containsKey(WithMethod.class));
		assertNotNull(c.get(WithMethod.class).apply("123456789"));
		assertTrue(c.getFunctions().containsKey(WithMethod.class));
	}

	@Test
	void doesNotGetWithoutMethod() {
		assertFalse(c.getFunctions().containsKey(WithoutMethod.class));
		assertThrows(ReflectionException.class, () -> {
			c.get(WithoutMethod.class);
		});
		assertFalse(c.getFunctions().containsKey(WithoutMethod.class));
	}

	@Test
	void doesNotGetWithInvalidInputType() {
		assertFalse(c.getFunctions().containsKey(WithInvalidInputType.class));
		assertThrows(ReflectionException.class, () -> {
			c.get(WithInvalidInputType.class);
		});
		assertFalse(c.getFunctions().containsKey(WithInvalidInputType.class));
	}

	@Test
	void doesNotGetWithInvalidOutputType() {
		assertFalse(c.getFunctions().containsKey(WithInvalidOutputType.class));
		assertThrows(ReflectionException.class, () -> {
			c.get(WithInvalidOutputType.class);
		});
		assertFalse(c.getFunctions().containsKey(WithInvalidOutputType.class));
	}

	@Test
	void doesNotGetWithNonStaticMethod() {
		assertFalse(c.getFunctions().containsKey(WithNonStaticMethod.class));
		assertThrows(ReflectionException.class, () -> {
			c.get(WithNonStaticMethod.class);
		});
		assertFalse(c.getFunctions().containsKey(WithNonStaticMethod.class));
	}

	@Test
	void doesNotCallIfThrows() {
		assertFalse(c.getFunctions().containsKey(WithInvalidMethod.class));
		Function<String, WithInvalidMethod> function = c.get(WithInvalidMethod.class);
		assertTrue(c.getFunctions().containsKey(WithInvalidMethod.class));
		assertThrows(ReflectionException.class, () -> {
			function.apply("123456789");
		});
	}

	@Test
	void doesNotCallIfReflectionThrows() {
		assertFalse(c.getFunctions().containsKey(WithNonPublicMethod.class));
		Function<String, WithNonPublicMethod> function = c.get(WithNonPublicMethod.class);
		assertTrue(c.getFunctions().containsKey(WithNonPublicMethod.class));
		assertThrows(ReflectionException.class, () -> {
			function.apply("123456789");
		});
	}
}
