package br.pro.hashi.sdx.rest.reflection;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.Iterator;
import java.util.Stack;

import org.reflections.Reflections;

import br.pro.hashi.sdx.rest.reflection.exception.ReflectionException;

public final class Reflection {
	public static <T> Constructor<T> getNoArgsConstructor(Class<T> type) {
		return getNoArgsConstructor(type, type.getName());
	}

	public static <T> Constructor<T> getNoArgsConstructor(Class<T> type, String name) {
		Constructor<T> constructor;
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

	public static <T> T newNoArgsInstance(Constructor<T> constructor) {
		T instance;
		try {
			instance = constructor.newInstance();
		} catch (InvocationTargetException | IllegalAccessException | InstantiationException exception) {
			throw new ReflectionException(exception);
		}
		return instance;
	}

	public static <T> Iterable<Class<? extends T>> getConcreteSubTypes(String packageName, Class<T> type) {
		Reflections reflections = new Reflections(packageName);
		return new Iterable<>() {
			@Override
			public Iterator<Class<? extends T>> iterator() {
				return reflections.getSubTypesOf(type)
						.stream()
						.filter((subType) -> !Modifier.isAbstract(subType.getModifiers()))
						.iterator();
			}
		};
	}

	public static <T, S extends T> Type getSpecificType(Class<T> superType, S object, int i) {
		Stack<Class<?>> stack = new Stack<>();
		Class<?> subType;
		for (subType = object.getClass(); !subType.equals(superType); subType = subType.getSuperclass()) {
			stack.push(subType);
		}
		TypeVariable<?>[] typeVariables = superType.getTypeParameters();
		Type type;
		while (!stack.empty()) {
			subType = stack.pop();
			ParameterizedType parameterizedType = (ParameterizedType) subType.getGenericSuperclass();
			Type[] types = parameterizedType.getActualTypeArguments();
			type = types[i];
			if (type instanceof TypeVariable) {
				typeVariables = subType.getTypeParameters();
				i = 0;
				while (!typeVariables[i].equals(type)) {
					i++;
				}
			} else {
				return type;
			}
		}
		throw new ReflectionException("Class %s must specify type %s".formatted(subType.getName(), typeVariables[i].getName()));
	}

	private Reflection() {
	}
}
