package br.pro.hashi.sdx.rest.server.tree;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class Node {
	private final Map<String, Node> children;
	private final Map<String, Endpoint> endpoints;

	Node() {
		this.children = new HashMap<>();
		this.endpoints = new HashMap<>();
	}

	Map<String, Node> getChildren() {
		return children;
	}

	Map<String, Endpoint> getEndpoints() {
		return endpoints;
	}

	Node requireChild(String item) {
		Node child = children.get(item);
		if (child == null) {
			child = new Node();
			children.put(item, child);
		}
		return child;
	}

	Node getChild(String item) {
		return children.get(item);
	}

	void putEndpoint(String methodName, Endpoint endpoint) {
		endpoints.put(methodName, endpoint);
	}

	public Endpoint getEndpoint(String methodName) {
		return endpoints.get(methodName);
	}

	public Set<String> allowedMethods() {
		return endpoints.keySet();
	}
}
