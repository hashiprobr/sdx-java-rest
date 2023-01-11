package br.pro.hashi.sdx.rest.transform.extension;

import java.lang.reflect.Constructor;
import java.util.Iterator;

import br.pro.hashi.sdx.rest.Builder;
import br.pro.hashi.sdx.rest.client.RESTClientBuilder;
import br.pro.hashi.sdx.rest.reflection.Reflection;
import br.pro.hashi.sdx.rest.server.RESTServerBuilder;

/**
 * <p>
 * An injector can manipulate an object of type {@link RESTClientBuilder} or
 * {@link RESTServerBuilder}.
 * </p>
 * <p>
 * The idea is that it can be used to extend transform support (possibly via a
 * third-party library).
 * </p>
 */
public abstract class Injector {
	/**
	 * The builder.
	 */
	protected final Builder<?> builder;

	/**
	 * Constructs a new injector with the specified builder.
	 *
	 * @param builder the builder
	 */
	protected Injector(Builder<?> builder) {
		this.builder = builder;
	}

	/**
	 * <p>
	 * Convenience method that instantiates concrete converters in a package
	 * (including subpackages) and iterates over the instances.
	 * </p>
	 * <p>
	 * The idea is that it can be used to wrap and register the converters in a
	 * third-party library.
	 * </p>
	 * 
	 * @param <T>         the superclass of the converters
	 * @param packageName the name of the package
	 * @param type        an object representing {@code T}
	 * @return an instance of each concrete subclass of {@code T} in the package
	 */
	protected final <T extends Converter<?, ?>> Iterable<T> getSubConverters(String packageName, Class<T> type) {
		return new Iterable<>() {
			@Override
			public Iterator<T> iterator() {
				Iterator<Class<? extends T>> iterator = Reflection.getConcreteSubTypes(packageName, type).iterator();

				return new Iterator<>() {
					@Override
					public boolean hasNext() {
						return iterator.hasNext();
					}

					@Override
					public T next() {
						Class<? extends T> subType = iterator.next();
						Constructor<? extends T> constructor = Reflection.getNoArgsConstructor(subType);
						return Reflection.newNoArgsInstance(constructor);
					}
				};
			}
		};
	}
}
