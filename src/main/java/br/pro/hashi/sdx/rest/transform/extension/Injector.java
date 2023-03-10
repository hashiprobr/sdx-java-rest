package br.pro.hashi.sdx.rest.transform.extension;

import java.lang.reflect.Constructor;
import java.util.Iterator;

import br.pro.hashi.sdx.rest.client.RestClientBuilder;
import br.pro.hashi.sdx.rest.reflection.Reflection;
import br.pro.hashi.sdx.rest.server.RestServerBuilder;

/**
 * <p>
 * An injector can manipulate an object of type {@link RestClientBuilder} or
 * {@link RestServerBuilder}.
 * </p>
 * <p>
 * The idea is that it can be used to configure and/or extend transform support
 * (possibly via a third-party library).
 * </p>
 */
public abstract class Injector {
	/**
	 * Constructs a new injector.
	 */
	protected Injector() {
	}

	/**
	 * <p>
	 * Convenience method that instantiates all concrete converters in a specified
	 * package (including subpackages) and iterates over the instances.
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
