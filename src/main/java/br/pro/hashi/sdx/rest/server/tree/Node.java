package br.pro.hashi.sdx.rest.server.tree;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class Node {
	private final Map<String, Node> children;
	private final Map<String, Endpoint> endpoints;
	private final Set<String> varMethodNames;

	Node() {
		this.children = new HashMap<>();
		this.endpoints = new HashMap<>();
		this.varMethodNames = new HashSet<>();
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
		if (endpoint.getVarType() != null) {
			varMethodNames.add(methodName);
		}
	}

	public Set<String> getMethodNames() {
		return endpoints.keySet();
	}

	public Set<String> getVarMethodNames() {
		return varMethodNames;
	}

	public Endpoint getEndpoint(String methodName, int varSize) {
		if (methodName.equals("HEAD")) {
			methodName = "GET";
		}
		if (varSize > 0 && !varMethodNames.contains(methodName)) {
			return null;
		}
		return endpoints.get(methodName);
	}
}
