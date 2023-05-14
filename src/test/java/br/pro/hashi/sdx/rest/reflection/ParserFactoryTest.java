package br.pro.hashi.sdx.rest.reflection;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodHandles.Lookup;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.function.Function;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

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
	private final static double DELTA = 0.000001;

	private Reflector reflector;
	private ParserFactory f;

	@BeforeEach
	void setUp() {
		reflector = mock(Reflector.class);
		when(reflector.unreflect(any(Method.class))).thenAnswer((invocation) -> {
			Method method = invocation.getArgument(0);
			return LOOKUP.unreflect(method);
		});
		f = new ParserFactory(reflector);
	}

	@Test
	void getsInstance() {
		assertInstanceOf(ParserFactory.class, ParserFactory.getInstance());
	}

	@Test
	void constructsWithDefaultReflector() {
		ParserFactory factory;
		try (MockedStatic<Reflector> reflectorStatic = mockStatic(Reflector.class)) {
			reflectorStatic.when(() -> Reflector.getInstance()).thenReturn(reflector);
			factory = new ParserFactory();
		}
		assertSame(reflector, factory.getReflector());
	}

	@Test
	void getsAndAppliesBooleanParser() {
		verify(reflector, times(0)).unreflect(any());
		assertTrue(f.get(boolean.class).apply("true"));
		verify(reflector, times(0)).unreflect(any());
	}

	@Test
	void getsAndAppliesByteParser() {
		verify(reflector, times(0)).unreflect(any());
		assertEquals(1, (byte) f.get(byte.class).apply("1"));
		verify(reflector, times(0)).unreflect(any());
	}

	@Test
	void getsAndAppliesShortParser() {
		verify(reflector, times(0)).unreflect(any());
		assertEquals(2, (short) f.get(short.class).apply("2"));
		verify(reflector, times(0)).unreflect(any());
	}

	@Test
	void getsAndAppliesIntParser() {
		verify(reflector, times(0)).unreflect(any());
		assertEquals(3, f.get(int.class).apply("3"));
		verify(reflector, times(0)).unreflect(any());
	}

	@Test
	void getsAndAppliesLongParser() {
		verify(reflector, times(0)).unreflect(any());
		assertEquals(4, f.get(long.class).apply("4"));
		verify(reflector, times(0)).unreflect(any());
	}

	@Test
	void getsAndAppliesFloatParser() {
		verify(reflector, times(0)).unreflect(any());
		assertEquals(5.5, f.get(float.class).apply("5.5"), DELTA);
		verify(reflector, times(0)).unreflect(any());
	}

	@Test
	void getsAndAppliesDoubleParser() {
		verify(reflector, times(0)).unreflect(any());
		assertEquals(6.6, f.get(double.class).apply("6.6"), DELTA);
		verify(reflector, times(0)).unreflect(any());
	}

	@Test
	void getsAndAppliesBigIntegerParser() {
		verify(reflector, times(0)).unreflect(any());
		assertEquals(BigInteger.valueOf(7), f.get(BigInteger.class).apply("7"));
		verify(reflector, times(0)).unreflect(any());
	}

	@Test
	void getsAndAppliesBigDecimalParser() {
		verify(reflector, times(0)).unreflect(any());
		assertEquals(BigDecimal.valueOf(8.8), f.get(BigDecimal.class).apply("8.8"));
		verify(reflector, times(0)).unreflect(any());
	}

	@Test
	void getsAndAppliesStringParser() {
		verify(reflector, times(0)).unreflect(any());
		assertEquals("s", f.get(String.class).apply("s"));
		verify(reflector, times(0)).unreflect(any());
	}

	@Test
	void getsAndApplies() {
		verify(reflector, times(0)).unreflect(any());

		Function<String, DefaultMethod> parser = f.get(DefaultMethod.class);
		assertInstanceOf(DefaultMethod.class, parser.apply("s"));
		Method method = getDeclaredMethod(DefaultMethod.class);
		verify(reflector).unreflect(method);

		assertSame(parser, f.get(DefaultMethod.class));
		verify(reflector, times(1)).unreflect(any());
	}

	@Test
	void doesNotInvokeCheckedHandle() {
		Method method = getDeclaredMethod(CheckedMethod.class);
		MethodHandle handle = assertDoesNotThrow(() -> {
			return LOOKUP.unreflect(method);
		});
		assertThrows(AssertionError.class, () -> {
			f.invoke(handle, "s");
		});
	}

	private <T> Method getDeclaredMethod(Class<T> type) {
		Method method;
		try {
			method = type.getDeclaredMethod("valueOf", String.class);
		} catch (NoSuchMethodException exception) {
			throw new AssertionError(exception);
		}
		return method;
	}

	@Test
	void doesNotApplyUncheckedParser() {
		Function<String, UncheckedMethod> parser = f.get(UncheckedMethod.class);
		assertThrows(RuntimeException.class, () -> {
			parser.apply("s");
		});
	}

	@Test
	void doesNotGetCheckedParser() {
		assertThrows(ReflectionException.class, () -> {
			f.get(CheckedMethod.class);
		});
	}

	@Test
	void doesNotGetNonStaticParser() {
		assertThrows(ReflectionException.class, () -> {
			f.get(NonStaticMethod.class);
		});
	}

	@Test
	void doesNotGetNonPublicParser() {
		assertThrows(ReflectionException.class, () -> {
			f.get(NonPublicMethod.class);
		});
	}

	@Test
	void doesNotGetNonInstanceParser() {
		assertThrows(ReflectionException.class, () -> {
			f.get(NonInstanceMethod.class);
		});
	}

	@Test
	void doesNotGetMissingParser() {
		assertThrows(ReflectionException.class, () -> {
			f.get(MissingMethod.class);
		});
	}
}
