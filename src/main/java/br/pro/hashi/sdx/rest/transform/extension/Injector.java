package br.pro.hashi.sdx.rest.transform.extension;

import java.lang.reflect.Constructor;
import java.util.Iterator;

import br.pro.hashi.sdx.rest.Builder;
import br.pro.hashi.sdx.rest.reflection.Reflection;

/**
 * Stub.
 */
public abstract class Injector {
	/**
	 * Stub.
	 */
	protected final Builder<?> builder;

	/**
	 * Stub.
	 *
	 * @param builder stub
	 */
	protected Injector(Builder<?> builder) {
		this.builder = builder;
	}

	/**
	 * Convenience method that instantiates converters in a package (including
	 * subpackages) and iterates over them. The idea is that it can be used to wrap
	 * and register the converters in a third-party library.
	 * 
	 * @param <T>         the superclass of the converters
	 * @param packageName the name of the package
	 * @param type        an object representing {@code T}
	 * @return an instance of each concrete subclass of {@code T} in the package
	 */
	public final <T extends Converter<?, ?>> Iterable<T> getSubConverters(String packageName, Class<T> type) {
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
