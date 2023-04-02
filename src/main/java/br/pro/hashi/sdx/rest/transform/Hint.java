package br.pro.hashi.sdx.rest.transform;

import java.lang.reflect.Type;

import br.pro.hashi.sdx.rest.reflection.Reflection;

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
		this.type = Reflection.getSpecificType(Hint.class, 0, this);
	}

	/**
	 * Obtains the hinted type.
	 * 
	 * @return an object representing {@code T}
	 */
	public final Type getType() {
		return type;
	}
}
