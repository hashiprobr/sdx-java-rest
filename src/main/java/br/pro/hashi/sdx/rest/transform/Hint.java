package br.pro.hashi.sdx.rest.transform;

import java.lang.reflect.Type;

import br.pro.hashi.sdx.rest.reflection.Reflector;

/**
 * <p>
 * An instance of an anonymous subclass of this class can be passed to hint the
 * type of a generic object.
 * </p>
 * <p>
 * It is analogous to the {@code TypeToken} class in Gson and the
 * {@code TypeReference} class in Jackson.
 * </p>
 *
 * @param <T> the hinted type
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
	 * Obtains the hinted type.
	 * 
	 * @return {@link Class} representing {@code T}
	 */
	public final Type getType() {
		return type;
	}
}
