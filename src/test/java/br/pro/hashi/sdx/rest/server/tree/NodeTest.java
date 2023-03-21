package br.pro.hashi.sdx.rest.server.tree;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class NodeTest {
	private Node n;

	@BeforeEach
	void setUp() {
		n = new Node();
	}

	@Test
	void initializesWithoutChildren() {
		assertTrue(n.getChildren().isEmpty());
		assertNull(n.getChild("item"));
	}

	@Test
	void initializesWithoutEndpoint() {
		assertTrue(n.getEndpoints().isEmpty());
		assertNull(n.getEndpoint("GET"));
		assertNull(n.getEndpoint("POST"));
		assertTrue(n.getMethodNames().isEmpty());
	}

	@Test
	void requiresChild() {
		Node child = n.requireChild("item");
		assertEquals(1, n.getChildren().size());
		assertSame(child, n.requireChild("item"));
		assertEquals(1, n.getChildren().size());
		assertSame(child, n.getChild("item"));
	}

	@Test
	void putsEndpoint() {
		Endpoint getEndpoint = mock(Endpoint.class);
		n.putEndpoint("GET", getEndpoint);
		assertEquals(1, n.getEndpoints().size());
		assertSame(getEndpoint, n.getEndpoint("GET"));
		assertEquals(Set.of("GET"), n.getMethodNames());
		Endpoint postEndpoint = mock(Endpoint.class);
		n.putEndpoint("POST", postEndpoint);
		assertEquals(2, n.getEndpoints().size());
		assertSame(postEndpoint, n.getEndpoint("POST"));
		assertEquals(Set.of("GET", "POST"), n.getMethodNames());
	}
}
