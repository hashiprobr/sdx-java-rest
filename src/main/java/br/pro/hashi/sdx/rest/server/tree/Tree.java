package br.pro.hashi.sdx.rest.server.tree;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.Stack;

import org.eclipse.jetty.http.HttpStatus;

import br.pro.hashi.sdx.rest.reflection.Cache;
import br.pro.hashi.sdx.rest.reflection.exception.ReflectionException;
import br.pro.hashi.sdx.rest.server.RestResource;
import br.pro.hashi.sdx.rest.server.annotation.Nested;
import br.pro.hashi.sdx.rest.server.exception.ResponseException;

public class Tree {
	private final Cache cache;
	private final Locale locale;
	private final Node root;

	public Tree(Cache cache, Locale locale) {
		this.cache = cache;
		this.locale = locale;
		this.root = new Node();
	}

	public Node getNodeAndAddItems(String[] items, List<String> itemList) {
		Node node = root;
		for (String item : items) {
			Node child = node.getChild(item);
			if (child == null) {
				child = node.getChild(null);
				if (child == null) {
					throw new ResponseException(HttpStatus.NOT_FOUND_404, "Resource not found");
				}
				itemList.add(item);
			}
			node = child;
		}
		return node;
	}

	public void putNodesAndEndpoints(Class<? extends RestResource> type, String typeName, Map<Class<? extends RestResource>, String[]> itemMap) {
		Node node = root;
		int distance = 0;

		Stack<Nested> stack = getAnnotationStack(type, typeName);
		while (!stack.empty()) {
			Nested annotation = stack.pop();
			for (String item : itemMap.get(annotation.in())) {
				node = node.requireChild(item);
			}
			for (int i = 0; i < annotation.at(); i++) {
				node = node.requireChild(null);
				distance++;
			}
		}

		for (String item : itemMap.get(type)) {
			node = node.requireChild(item);
		}

		Node subRoot = node;
		for (Method method : type.getMethods()) {
			if (RestResource.class.isAssignableFrom(method.getDeclaringClass()) && !Modifier.isStatic(method.getModifiers())) {
				String methodName = method.getName();
				Endpoint endpoint = new Endpoint(cache, distance, type, typeName, method, methodName);
				methodName = methodName.toUpperCase(locale);
				node = subRoot;
				for (int i = 0; i < endpoint.getReach(); i++) {
					node = node.requireChild(null);
				}
				Endpoint existing = node.getEndpoint(methodName);
				if (existing != null) {
					Class<? extends RestResource> existingType = existing.getResourceType();
					if (type.equals(existingType)) {
						throw new ReflectionException("%s has multiple %s endpoints in the same path".formatted(typeName, methodName));
					}
					throw new ReflectionException("%s and %s have %s endpoints in the same path".formatted(typeName, existingType.getName(), methodName));
				}
				node.putEndpoint(methodName, endpoint);
			}
		}
	}

	Stack<Nested> getAnnotationStack(Class<? extends RestResource> type, String typeName) {
		Stack<Nested> stack = new Stack<>();
		Set<Class<? extends RestResource>> types = new HashSet<>();
		Nested annotation = getAnnotation(type, typeName);
		while (annotation != null) {
			types.add(type);
			type = annotation.in();
			if (types.contains(type)) {
				throw new ReflectionException("Cyclic nesting detected in class %s".formatted(typeName));
			}
			stack.push(annotation);
			annotation = getAnnotation(type, type.getName());
		}
		return stack;
	}

	Nested getAnnotation(Class<?> type, String typeName) {
		Nested existing = null;
		while (!type.equals(RestResource.class)) {
			Queue<Class<?>> queue = new LinkedList<>();
			queue.add(type);
			while (!queue.isEmpty()) {
				Class<?> node = queue.remove();
				Nested annotation = node.getDeclaredAnnotation(Nested.class);
				if (annotation != null) {
					if (existing != null) {
						throw new ReflectionException("Multiple nesting detected in class %s".formatted(typeName));
					}
					existing = annotation;
				}
				for (Class<?> child : node.getInterfaces()) {
					queue.add(child);
				}
			}
			type = type.getSuperclass();
		}
		return existing;
	}
}
