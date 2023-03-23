package br.pro.hashi.sdx.rest.server;

import java.lang.reflect.Method;
import java.lang.reflect.Type;

/**
 * Stub.
 */
public abstract class ErrorFormatter {
	private final Type returnType;

	/**
	 * Stub.
	 */
	protected ErrorFormatter() {
		Class<? extends ErrorFormatter> type = getClass();
		Method method;
		try {
			method = getMethod(type);
		} catch (NoSuchMethodException exception) {
			throw new AssertionError(exception);
		}
		this.returnType = method.getGenericReturnType();
	}

	Method getMethod(Class<? extends ErrorFormatter> type) throws NoSuchMethodException {
		return type.getMethod("format", int.class, String.class);
	}

	Type getReturnType() {
		return returnType;
	}

	/**
	 * Stub.
	 * 
	 * @param status  stub
	 * @param message stub
	 * @return stub
	 */
	public abstract Object format(int status, String message);
}
