package br.pro.hashi.sdx.rest.server;

import java.lang.reflect.Method;
import java.lang.reflect.Type;

/**
 * An error formatter can wrap messages into arbitrary objects.
 */
public abstract class ErrorFormatter {
	private final Type returnType;

	/**
	 * Constructs a new formatter.
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
		return type.getDeclaredMethod("format", int.class, String.class);
	}

	Type getReturnType() {
		return returnType;
	}

	/**
	 * Wraps a status and a message into an object.
	 * 
	 * @implNote The implementation should override the return type to ensure
	 *           serialization accuracy.
	 * 
	 * @param status  the status
	 * @param message the message
	 * @return the object
	 */
	public abstract Object format(int status, String message);
}
