package br.pro.hashi.sdx.rest.transform;

import java.lang.reflect.Type;

import br.pro.hashi.sdx.rest.reflection.Reflector;

/**
 * <p>
 * Represents a type at runtime.
 * </p>
 * <p>
 * It is analogous to the {@code TypeToken} class in Gson and the
 * {@code TypeReference} class in Jackson.
 * </p>
 *
 * @param <T> the type
 */
public abstract class Hint<T> {
	private final Type type;

	/**
	 * Constructs a new hint.
	 */
	protected Hint() {
		this.type = Reflector.getInstance().getSpecificType(this, Hint.class, 0);
	}

	/**
	 * Obtains the type.
	 * 
	 * @return a {@link Type} representing {@code T}
	 */
	public final Type getType() {
		return type;
	}
}
