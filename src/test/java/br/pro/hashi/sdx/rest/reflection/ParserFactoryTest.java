package br.pro.hashi.sdx.rest.reflection;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodHandles.Lookup;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.function.Function;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import br.pro.hashi.sdx.rest.reflection.exception.ReflectionException;
import br.pro.hashi.sdx.rest.reflection.mock.parser.CheckedMethod;
import br.pro.hashi.sdx.rest.reflection.mock.parser.DefaultMethod;
import br.pro.hashi.sdx.rest.reflection.mock.parser.MissingMethod;
import br.pro.hashi.sdx.rest.reflection.mock.parser.NonInstanceMethod;
import br.pro.hashi.sdx.rest.reflection.mock.parser.NonPublicMethod;
import br.pro.hashi.sdx.rest.reflection.mock.parser.NonStaticMethod;
import br.pro.hashi.sdx.rest.reflection.mock.parser.UncheckedMethod;

class ParserFactoryTest {
	private static final Lookup LOOKUP = MethodHandles.lookup();

	private AutoCloseable mocks;
	private @Mock Reflector reflector;
	private ParserFactory f;

	@BeforeEach
	void setUp() {
		mocks = MockitoAnnotations.openMocks(this);

		when(reflector.unreflect(any(Method.class))).thenAnswer((invocation) -> {
			Method method = invocation.getArgument(0);
			return LOOKUP.unreflect(method);
		});

		f = new ParserFactory(reflector);
	}

	@AfterEach
	void tearDown() {
		assertDoesNotThrow(() -> {
			mocks.close();
		});
	}

	@Test
	void getsInstance() {
		assertInstanceOf(ParserFactory.class, ParserFactory.getInstance());
	}

	@Test
	void doesNotParseCharFromEmptyString() {
		assertThrows(IllegalArgumentException.class, () -> {
			f.parseChar("");
		});
	}

	@Test
	void doesNotParseCharFromLargeString() {
		assertThrows(IllegalArgumentException.class, () -> {
			f.parseChar("cc");
		});
	}

	@Test
	void getsAndAppliesBooleanParser() {
		assertTrue(f.get(boolean.class).apply("true"));
	}

	@Test
	void getsAndAppliesByteParser() {
		assertEquals(1, (byte) f.get(byte.class).apply("1"));
	}

	@Test
	void getsAndAppliesShortParser() {
		assertEquals(2, (short) f.get(short.class).apply("2"));
	}

	@Test
	void getsAndAppliesIntParser() {
		assertEquals(3, f.get(int.class).apply("3"));
	}

	@Test
	void getsAndAppliesLongParser() {
		assertEquals(4, f.get(long.class).apply("4"));
	}

	@Test
	void getsAndAppliesFloatParser() {
		assertEquals(5.5F, f.get(float.class).apply("5.5"));
	}

	@Test
	void getsAndAppliesDoubleParser() {
		assertEquals(6.6, f.get(double.class).apply("6.6"));
	}

	@Test
	void getsAndAppliesCharParser() {
		assertEquals('c', f.get(char.class).apply("c"));
	}

	@Test
	void getsAndAppliesCharacterParser() {
		assertEquals(Character.valueOf('c'), f.get(Character.class).apply("c"));
	}

	@Test
	void getsAndAppliesBigIntegerParser() {
		assertEquals(BigInteger.valueOf(7), f.get(BigInteger.class).apply("7"));
	}

	@Test
	void getsAndAppliesBigDecimalParser() {
		assertEquals(BigDecimal.valueOf(8.8), f.get(BigDecimal.class).apply("8.8"));
	}

	@Test
	void getsAndAppliesStringParser() {
		assertEquals("s", f.get(String.class).apply("s"));
	}

	@Test
	void getsAndApplies() {
		Function<String, DefaultMethod> parser = f.get(DefaultMethod.class);
		assertInstanceOf(DefaultMethod.class, parser.apply("s"));
		assertSame(parser, f.get(DefaultMethod.class));
	}

	@Test
	void doesNotGetMissingParser() {
		assertThrows(ReflectionException.class, () -> {
			f.get(MissingMethod.class);
		});
	}

	@Test
	void doesNotGetNonInstanceParser() {
		assertThrows(ReflectionException.class, () -> {
			f.get(NonInstanceMethod.class);
		});
	}

	@Test
	void doesNotGetNonPublicParser() {
		assertThrows(ReflectionException.class, () -> {
			f.get(NonPublicMethod.class);
		});
	}

	@Test
	void doesNotGetNonStaticParser() {
		assertThrows(ReflectionException.class, () -> {
			f.get(NonStaticMethod.class);
		});
	}

	@Test
	void doesNotGetCheckedParser() {
		assertThrows(ReflectionException.class, () -> {
			f.get(CheckedMethod.class);
		});
	}

	@Test
	void doesNotApplyUncheckedParser() {
		Function<String, UncheckedMethod> parser = f.get(UncheckedMethod.class);
		assertThrows(RuntimeException.class, () -> {
			parser.apply("s");
		});
	}

	@Test
	void doesNotInvokeCheckedHandle() {
		MethodHandle handle = assertDoesNotThrow(() -> {
			Method method = CheckedMethod.class.getDeclaredMethod("valueOf", String.class);
			return LOOKUP.unreflect(method);
		});
		assertThrows(AssertionError.class, () -> {
			f.invoke(handle, "s");
		});
	}
}
