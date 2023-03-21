package br.pro.hashi.sdx.rest.server;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.lang.reflect.Method;

import org.junit.jupiter.api.Test;

class ErrorFormatterTest {
	private ErrorFormatter f;

	@Test
	void constructs() {
		f = new ErrorFormatter() {
			@Override
			public Object format(int status, String message) {
				return null;
			}
		};
		assertEquals(Object.class, f.getReturnType());
	}

	@Test
	void doesNotConstruct() {
		assertThrows(AssertionError.class, () -> {
			new ErrorFormatter() {
				@Override
				Method getMethod(Class<? extends ErrorFormatter> type) throws NoSuchMethodException {
					throw new NoSuchMethodException();
				}

				@Override
				public Object format(int status, String message) {
					return null;
				}
			};
		});
	}
}
