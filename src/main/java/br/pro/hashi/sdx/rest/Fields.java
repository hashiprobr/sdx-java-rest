package br.pro.hashi.sdx.rest;

import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Stream;

import br.pro.hashi.sdx.rest.reflection.Headers;
import br.pro.hashi.sdx.rest.reflection.Queries;

/**
 * Base class for headers and queries.
 */
public sealed abstract class Fields permits Headers, Queries {
	/**
	 * Internal member.
	 * 
	 * @hidden
	 */
	protected Fields() {
	}

	/**
	 * <p>
	 * Splits, using a specified separator, the value associated to a specified name
	 * and returns the items as strings.
	 * </p>
	 * <p>
	 * If there are multiple values associated to the name, the first one is
	 * considered.
	 * </p>
	 * 
	 * @param name  the name
	 * @param regex the separator
	 * @return a list with the items
	 * @throws NullPointerException     if the name is null or the separator is null
	 * @throws IllegalArgumentException if the name is invalid or not available or
	 *                                  the separator is empty
	 */
	public List<String> split(String name, String regex) {
		return split(name, regex, String.class);
	}

	/**
	 * <p>
	 * Splits, using a specified separator, the value associated to a specified name
	 * and returns the items as objects of a specified type.
	 * </p>
	 * <p>
	 * If there are multiple values associated to the name, the first one is
	 * considered.
	 * </p>
	 * <p>
	 * The value is converted to {@code T} via {@code valueOf(String)}.
	 * </p>
	 * 
	 * @param <T>   the type of the value
	 * @param name  the name
	 * @param regex the separator
	 * @param type  an object representing {@code T}
	 * @return a list with the items
	 * @throws NullPointerException     if the name is null, the separator is null,
	 *                                  or the type is null
	 * @throws IllegalArgumentException if the name is invalid or not available or
	 *                                  the separator is empty
	 */
	public <T> List<T> split(String name, String regex, Class<T> type) {
		if (regex == null) {
			throw new NullPointerException("Separator cannot be null");
		}
		if (regex.isEmpty()) {
			throw new IllegalArgumentException("Separator cannot be empty");
		}
		return map(Stream.of(require(name).split(regex)), type);
	}

	/**
	 * <p>
	 * Obtains, as a string, the value associated to a specified name or throws an
	 * exception if the name is not available.
	 * </p>
	 * <p>
	 * If there are multiple values associated to the name, the first one is
	 * considered.
	 * </p>
	 * 
	 * @param name the name
	 * @return the value
	 * @throws NullPointerException     if the name is null
	 * @throws IllegalArgumentException if the name is invalid or not available
	 */
	public String require(String name) {
		return require(name, String.class);
	}

	/**
	 * <p>
	 * Obtains, as the object of a specified type, the value associated to a
	 * specified name or throws an exception if the name is not available.
	 * </p>
	 * <p>
	 * If there are multiple values associated to the name, the first one is
	 * considered.
	 * </p>
	 * <p>
	 * The value is converted to {@code T} via {@code valueOf(String)}.
	 * </p>
	 * 
	 * @param <T>  the type of the value
	 * @param name the name
	 * @param type an object representing {@code T}
	 * @return the value
	 * @throws NullPointerException     if the name is null or the type is null
	 * @throws IllegalArgumentException if the name is invalid or not available
	 */
	public <T> T require(String name, Class<T> type) {
		check(name, type);
		String valueString = doGet(name);
		if (valueString == null) {
			throw new IllegalArgumentException("Name '%s' is not available".formatted(name));
		}
		return function(type).apply(valueString);
	}

	/**
	 * <p>
	 * Obtains, as strings, the values associated to a specified name.
	 * </p>
	 * <p>
	 * If there are no values associated to the name, an empty list is returned.
	 * </p>
	 * 
	 * @param name the name
	 * @return a list with the values
	 * @throws NullPointerException     if the name is null
	 * @throws IllegalArgumentException if the name is invalid
	 */
	public List<String> getList(String name) {
		return getList(name, String.class);
	}

	/**
	 * <p>
	 * Obtains, as objects of a specified type, the values associated to a specified
	 * name.
	 * </p>
	 * <p>
	 * If there are no values associated to the name, an empty list is returned.
	 * </p>
	 * <p>
	 * The values are converted to {@code T} via {@code valueOf(String)}.
	 * </p>
	 * 
	 * @param <T>  the type of the values
	 * @param name the name
	 * @param type an object representing {@code T}
	 * @return a list with the values
	 * @throws NullPointerException     if the name is null or the type is null
	 * @throws IllegalArgumentException if the name is invalid
	 */
	public <T> List<T> getList(String name, Class<T> type) {
		check(name, type);
		return map(getStream(name), type);
	}

	private <T> List<T> map(Stream<String> stream, Class<T> type) {
		return stream.map(function(type)).toList();
	}

	/**
	 * <p>
	 * Obtains, as a string, the value associated to a specified name or
	 * {@code null} if the name is not available.
	 * </p>
	 * <p>
	 * If there are multiple values associated to the name, the first one is
	 * considered.
	 * </p>
	 * 
	 * @param name the name
	 * @return the value
	 * @throws NullPointerException     if the name is null
	 * @throws IllegalArgumentException if the name is invalid
	 */
	public String get(String name) {
		return get(name, String.class);
	}

	/**
	 * <p>
	 * Obtains, as a string, the value associated to a specified name or a default
	 * value if the name is not available.
	 * </p>
	 * <p>
	 * If there are multiple values associated to the name, the first one is
	 * considered.
	 * </p>
	 * 
	 * @param name         the name
	 * @param defaultValue the default value
	 * @return the value
	 * @throws NullPointerException     if the name is null
	 * @throws IllegalArgumentException if the name is invalid
	 */
	public String get(String name, String defaultValue) {
		return get(name, String.class, defaultValue);
	}

	/**
	 * <p>
	 * Obtains, as the object of a specified type, the value associated to a
	 * specified name or {@code null} if the name is not available.
	 * </p>
	 * <p>
	 * If there are multiple values associated to the name, the first one is
	 * considered.
	 * </p>
	 * <p>
	 * The value is converted to {@code T} via {@code valueOf(String)}.
	 * </p>
	 * 
	 * @param <T>  the type of the value
	 * @param name the name
	 * @param type an object representing {@code T}
	 * @return the value
	 * @throws NullPointerException     if the name is null or the type is null
	 * @throws IllegalArgumentException if the name is invalid
	 */
	public <T> T get(String name, Class<T> type) {
		return get(name, type, null);
	}

	/**
	 * <p>
	 * Obtains, as the object of a specified type, the value associated to a
	 * specified name or a default value if the name is not available.
	 * </p>
	 * <p>
	 * If there are multiple values associated to the name, the first one is
	 * considered.
	 * </p>
	 * <p>
	 * The value is converted to {@code T} via {@code valueOf(String)}.
	 * </p>
	 * 
	 * @param <T>          the type of the value
	 * @param name         the name
	 * @param type         an object representing {@code T}
	 * @param defaultValue the default value
	 * @return the value
	 * @throws NullPointerException     if the name is null or the type is null
	 * @throws IllegalArgumentException if the name is invalid
	 */
	public <T> T get(String name, Class<T> type, T defaultValue) {
		check(name, type);
		String valueString = doGet(name);
		if (valueString == null) {
			return defaultValue;
		}
		return function(type).apply(valueString);
	}

	/**
	 * Internal member.
	 * 
	 * @param name the name
	 * @return the stream
	 * @hidden
	 */
	protected abstract Stream<String> getStream(String name);

	/**
	 * Internal member.
	 * 
	 * @param name the name
	 * @return the value
	 * @hidden
	 */
	protected abstract String doGet(String name);

	/**
	 * Internal member.
	 * 
	 * @param <T>  the type of the value
	 * @param type an object representing {@code T}
	 * @return the function
	 * @hidden
	 */
	protected abstract <T> Function<String, T> function(Class<T> type);

	/**
	 * <p>
	 * Obtains the available names.
	 * </p>
	 * <p>
	 * If there are no names available, an empty set is returned.
	 * </p>
	 * 
	 * @return a set with the names
	 */
	public abstract Set<String> names();

	private void check(String name, Class<?> type) {
		if (name == null) {
			throw new NullPointerException("Name cannot be null");
		}
		if (type == null) {
			throw new NullPointerException("Type cannot be null");
		}
	}
}
