package br.pro.hashi.sdx.rest.base;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.util.Iterator;

import org.reflections.Reflections;

import br.pro.hashi.sdx.rest.base.exception.ReflectionException;

public final class Reflection {
	public static <T> Constructor<? extends T> getNoArgsConstructor(Class<? extends T> type, String name) {
		Constructor<? extends T> constructor;
		try {
			constructor = type.getDeclaredConstructor();
		} catch (NoSuchMethodException exception) {
			throw new ReflectionException("Class %s must have a no-args constructor (but not necessarily public)".formatted(name));
		}
		if (!Modifier.isPublic(constructor.getModifiers())) {
			constructor.setAccessible(true);
		}
		return constructor;
	}

	public static <T> Constructor<? extends T> getNoArgsConstructor(Class<? extends T> type) {
		return getNoArgsConstructor(type, type.getName());
	}

	public static <T> T newNoArgsInstance(Constructor<? extends T> constructor) {
		T instance;
		try {
			instance = constructor.newInstance();
		} catch (InvocationTargetException | IllegalAccessException | InstantiationException exception) {
			throw new ReflectionException(exception);
		}
		return instance;
	}

	public static <T> Iterable<Class<? extends T>> getSubTypes(String packageName, Class<T> type) {
		Reflections reflections = new Reflections(packageName);

		return new Iterable<>() {
			@Override
			public Iterator<Class<? extends T>> iterator() {
				return reflections.getSubTypesOf(type).stream()
						.filter((subType) -> !Modifier.isAbstract(subType.getModifiers()))
						.iterator();
			}
		};
	}

	public static <T> Iterable<T> getSubInstances(String packageName, Class<T> type) {
		return new Iterable<>() {
			@Override
			public Iterator<T> iterator() {
				Iterator<Class<? extends T>> iterator = getSubTypes(packageName, type).iterator();

				return new Iterator<>() {
					@Override
					public boolean hasNext() {
						return iterator.hasNext();
					}

					@Override
					public T next() {
						Class<? extends T> subType = iterator.next();
						Constructor<? extends T> constructor = getNoArgsConstructor(subType);
						return newNoArgsInstance(constructor);
					}
				};
			}
		};
	}

	private Reflection() {
	}
}
