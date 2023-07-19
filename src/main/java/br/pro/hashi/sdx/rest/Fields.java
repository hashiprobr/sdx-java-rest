package br.pro.hashi.sdx.rest;

import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

import br.pro.hashi.sdx.rest.reflection.Headers;
import br.pro.hashi.sdx.rest.reflection.ParserFactory;
import br.pro.hashi.sdx.rest.reflection.PartHeaders;
import br.pro.hashi.sdx.rest.reflection.Queries;

/**
 * Base class for part headers, headers, and queries.
 */
public sealed abstract class Fields permits PartHeaders, Headers, Queries {
	private final ParserFactory factory;

	/**
	 * Internal member.
	 *
	 * @param factory the factory
	 * @hidden
	 */
	protected Fields(ParserFactory factory) {
		this.factory = factory;
	}

	/**
	 * <p>
	 * Splits the value corresponding to the specified name using the specified
	 * separator and returns the items as strings.
	 * </p>
	 * <p>
	 * If multiple values correspond to the name, the first one is considered.
	 * </p>
	 *
	 * @param name  the name
	 * @param regex the separator
	 * @return the items
	 * @throws NullPointerException     if the name is null or the separator is null
	 * @throws IllegalArgumentException if the name is invalid or does not exist or
	 *                                  if the separator is empty
	 */
	public List<String> split(String name, String regex) {
		check(name);
		return doSplit(name, regex, String.class);
	}

	/**
	 * <p>
	 * Splits the value corresponding to the specified name using the specified
	 * separator and returns the items as objects of the specified type.
	 * </p>
	 * <p>
	 * If multiple values correspond to the name, the first one is considered.
	 * </p>
	 * <p>
	 * The items are converted via {@code valueOf(String)}.
	 * </p>
	 *
	 * @param <T>   the type
	 * @param name  the name
	 * @param regex the separator
	 * @param type  a {@link Class} representing {@code T}
	 * @return the items
	 * @throws NullPointerException     if the name is null, the separator is null,
	 *                                  or the type is null
	 * @throws IllegalArgumentException if the name is invalid or does not exist or
	 *                                  if the separator is empty
	 */
	public <T> List<T> split(String name, String regex, Class<T> type) {
		check(name);
		check(type);
		return doSplit(name, regex, type);
	}

	private <T> List<T> doSplit(String name, String regex, Class<T> type) {
		if (regex == null) {
			throw new NullPointerException("Separator cannot be null");
		}
		if (regex.isEmpty()) {
			throw new IllegalArgumentException("Separator cannot be empty");
		}
		return map(Stream.of(doRequire(name, String.class).split(regex, -1)), type);
	}

	/**
	 * <p>
	 * Obtains the value corresponding to the specified name as a string or throws
	 * an exception if the name does not exist.
	 * </p>
	 * <p>
	 * If multiple values correspond to the name, the first one is considered.
	 * </p>
	 *
	 * @param name the name
	 * @return the value
	 * @throws NullPointerException     if the name is null
	 * @throws IllegalArgumentException if the name is invalid or does not exist
	 */
	public String require(String name) {
		check(name);
		return doRequire(name, String.class);
	}

	/**
	 * <p>
	 * Obtains the value corresponding to the specified name as an object of the
	 * specified type or throws an exception if the name does not exist.
	 * </p>
	 * <p>
	 * If multiple values correspond to the name, the first one is considered.
	 * </p>
	 * <p>
	 * The value is converted via {@code valueOf(String)}.
	 * </p>
	 *
	 * @param <T>  the type
	 * @param name the name
	 * @param type a {@link Class} representing {@code T}
	 * @return the value
	 * @throws NullPointerException     if the name is null or the type is null
	 * @throws IllegalArgumentException if the name is invalid or does not exist
	 */
	public <T> T require(String name, Class<T> type) {
		check(name);
		check(type);
		return doRequire(name, type);
	}

	private <T> T doRequire(String name, Class<T> type) {
		String valueString = getString(name);
		if (valueString == null) {
			throw new IllegalArgumentException("Name '%s' does not exist".formatted(name));
		}
		return factory.get(type).apply(valueString);
	}

	/**
	 * <p>
	 * Obtains the values corresponding to the specified name as strings.
	 * </p>
	 * <p>
	 * If the name does not exist, returns an empty list.
	 * </p>
	 *
	 * @param name the name
	 * @return the values
	 * @throws NullPointerException     if the name is null
	 * @throws IllegalArgumentException if the name is invalid
	 */
	public List<String> getList(String name) {
		check(name);
		return doGetList(name, String.class);
	}

	/**
	 * <p>
	 * Obtains the values corresponding to the specified name as objects of the
	 * specified type.
	 * </p>
	 * <p>
	 * If the name does not exist, returns an empty list.
	 * </p>
	 * <p>
	 * The values are converted via {@code valueOf(String)}.
	 * </p>
	 *
	 * @param <T>  the type
	 * @param name the name
	 * @param type a {@link Class} representing {@code T}
	 * @return the values
	 * @throws NullPointerException     if the name is null or the type is null
	 * @throws IllegalArgumentException if the name is invalid
	 */
	public <T> List<T> getList(String name, Class<T> type) {
		check(name);
		check(type);
		return doGetList(name, type);
	}

	private <T> List<T> doGetList(String name, Class<T> type) {
		return map(getStream(name), type);
	}

	/**
	 * <p>
	 * Obtains the value corresponding to the specified name as a string or
	 * {@code null} if the name does not exist.
	 * </p>
	 * <p>
	 * If multiple values correspond to the name, the first one is considered.
	 * </p>
	 *
	 * @param name the name
	 * @return the value
	 * @throws NullPointerException     if the name is null
	 * @throws IllegalArgumentException if the name is invalid
	 */
	public String get(String name) {
		check(name);
		return doGet(name, String.class, null);
	}

	/**
	 * <p>
	 * Obtains the value corresponding to the specified name as an object of the
	 * specified type or {@code null} if the name does not exist.
	 * </p>
	 * <p>
	 * If multiple values correspond to the name, the first one is considered.
	 * </p>
	 * <p>
	 * The value is converted via {@code valueOf(String)}.
	 * </p>
	 *
	 * @param <T>  the type
	 * @param name the name
	 * @param type a {@link Class} representing {@code T}
	 * @return the value
	 * @throws NullPointerException     if the name is null or the type is null
	 * @throws IllegalArgumentException if the name is invalid
	 */
	public <T> T get(String name, Class<T> type) {
		check(name);
		check(type);
		return doGet(name, type, null);
	}

	/**
	 * <p>
	 * Obtains the value corresponding to the specified name as a string or the
	 * specified default value if the name does not exist.
	 * </p>
	 * <p>
	 * If multiple values correspond to the name, the first one is considered.
	 * </p>
	 *
	 * @param name         the name
	 * @param defaultValue the default
	 * @return the value
	 * @throws NullPointerException     if the name is null
	 * @throws IllegalArgumentException if the name is invalid
	 */
	public String get(String name, String defaultValue) {
		check(name);
		return doGet(name, String.class, defaultValue);
	}

	/**
	 * <p>
	 * Obtains the value corresponding to the specified name as an object of the
	 * specified type or the specified default value if the name does not exist.
	 * </p>
	 * <p>
	 * If multiple values correspond to the name, the first one is considered.
	 * </p>
	 * <p>
	 * The value is converted via {@code valueOf(String)}.
	 * </p>
	 *
	 * @param <T>          the type
	 * @param name         the name
	 * @param type         a {@link Class} representing {@code T}
	 * @param defaultValue the default
	 * @return the value
	 * @throws NullPointerException     if the name is null or the type is null
	 * @throws IllegalArgumentException if the name is invalid
	 */
	public <T> T get(String name, Class<T> type, T defaultValue) {
		check(name);
		check(type);
		return doGet(name, type, defaultValue);
	}

	private <T> T doGet(String name, Class<T> type, T defaultValue) {
		String valueString = getString(name);
		if (valueString == null) {
			return defaultValue;
		}
		return factory.get(type).apply(valueString);
	}

	private void check(String name) {
		if (name == null) {
			throw new NullPointerException("Name cannot be null");
		}
	}

	private <T> void check(Class<T> type) {
		if (type == null) {
			throw new NullPointerException("Type cannot be null");
		}
	}

	private <T> List<T> map(Stream<String> stream, Class<T> type) {
		return stream.map(factory.get(type)).toList();
	}

	/**
	 * Obtains the names.
	 *
	 * @return the names
	 */
	public abstract Set<String> names();

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
	 * @return the string
	 * @hidden
	 */
	protected abstract String getString(String name);
}
