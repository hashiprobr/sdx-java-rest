package br.pro.hashi.sdx.rest.reflection;

import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodHandles.Lookup;
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
	public static final Lookup LOOKUP = MethodHandles.lookup();

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
		} catch (InvocationTargetException exception) {
			throw new ReflectionException(exception.getCause());
		} catch (IllegalAccessException | InstantiationException exception) {
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

	public static <T, S extends T> Type getSpecificType(Class<T> rootType, int rootIndex, S object) {
		Class<?> type = object.getClass();

		Stack<Node> stack = new Stack<>();
		stack.push(new Node(null, type));

		Type specificType;
		TypeVariable<?>[] typeVariables;

		while (!stack.isEmpty()) {
			Node node = stack.peek();

			if (node.hasNext()) {
				Class<?> superType = node.getSuperType();

				if (superType != null) {
					if (superType.equals(rootType)) {
						int index = rootIndex;

						while (node != null) {
							ParameterizedType genericSuperType = node.getGenericSuperType();
							Type[] types = genericSuperType.getActualTypeArguments();
							specificType = types[index];

							if (specificType instanceof TypeVariable) {
								typeVariables = node.getTypeParameters();
								index = 0;
								while (!specificType.equals(typeVariables[index])) {
									index++;
								}
							} else {
								return specificType;
							}

							node = node.getParent();
						}
					} else {
						stack.push(new Node(node, superType));
					}
				}
			} else {
				stack.pop();
			}
		}

		typeVariables = rootType.getTypeParameters();
		String typeVariableName = typeVariables[rootIndex].getName();
		throw new ReflectionException("Class %s must specify type %s".formatted(type.getName(), typeVariableName));
	}

	private static class Node {
		private final Node parent;
		private final Class<?> type;
		private final Class<?>[] interfaces;
		private final Type[] genericInterfaces;
		private int index;

		private Node(Node parent, Class<?> type) {
			this.parent = parent;
			this.type = type;
			this.interfaces = type.getInterfaces();
			this.genericInterfaces = type.getGenericInterfaces();
			this.index = -2;
			// superclass == -1
			// interface >= 0
		}

		private Node getParent() {
			return parent;
		}

		private TypeVariable<?>[] getTypeParameters() {
			return type.getTypeParameters();
		}

		private boolean hasNext() {
			index++;
			return index < interfaces.length;
		}

		private Class<?> getSuperType() {
			Class<?> superType;
			if (index == -1) {
				superType = type.getSuperclass();
			} else {
				superType = interfaces[index];
			}
			return superType;
		}

		private ParameterizedType getGenericSuperType() {
			Type genericType;
			if (index == -1) {
				genericType = type.getGenericSuperclass();
			} else {
				genericType = genericInterfaces[index];
			}
			return (ParameterizedType) genericType;
		}
	}

	private Reflection() {
	}
}
