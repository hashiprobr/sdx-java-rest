package br.pro.hashi.sdx.rest.transform.extension;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles.Lookup;
import java.util.Iterator;

import br.pro.hashi.sdx.rest.client.RestClientBuilder;
import br.pro.hashi.sdx.rest.reflection.Reflector;
import br.pro.hashi.sdx.rest.server.RestServerBuilder;

/**
 * <p>
 * Implemented to inject objects in a {@link RestClientBuilder} or a
 * {@link RestServerBuilder}.
 * </p>
 * <p>
 * The idea is that these objects can be used to modify and/or extend transform
 * support via a third-party library.
 * </p>
 */
public abstract class Injector {
	private final Reflector reflector;

	/**
	 * Constructs a new injector.
	 */
	protected Injector() {
		this.reflector = Reflector.getInstance();
	}

	/**
	 * <p>
	 * Instantiates all classes in the specified package (including subpackages)
	 * that are concrete implementations of the specified subinterface of
	 * {@link Converter}.
	 * </p>
	 * <p>
	 * The idea is that this method can be used to register the converters in a
	 * third-party library.
	 * </p>
	 * 
	 * @param <T>           the converter type
	 * @param packageName   the package name
	 * @param converterType a {@link Class} representing {@code T}
	 * @param lookup        a {@link Lookup} with full access to {@code T}
	 * @return an instance of each concrete implementation of {@code T} in the
	 *         package
	 */
	protected final <T extends Converter<?, ?>> Iterable<T> getSubConverters(String packageName, Class<T> converterType, Lookup lookup) {
		Iterator<Class<? extends T>> iterator = reflector.getConcreteSubTypes(packageName, converterType).iterator();

		return () -> new Iterator<>() {
			@Override
			public boolean hasNext() {
				return iterator.hasNext();
			}

			@Override
			public T next() {
				Class<? extends T> converterType = iterator.next();
				MethodHandle creator = reflector.getCreator(converterType, lookup);
				return reflector.invokeCreator(creator);
			}
		};
	}
}
