package br.pro.hashi.sdx.rest.server.tree;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.doReturn;
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
		assertNull(n.getChild(null));
		assertNull(n.getChild("item"));
	}

	@Test
	void initializesWithoutEndpoints() {
		assertTrue(n.getEndpoints().isEmpty());
		assertNull(n.getEndpoint("GET"));
		assertNull(n.getEndpoint("HEAD"));
		assertNull(n.getEndpoint("POST"));
		assertTrue(n.getMethodNames().isEmpty());
		assertTrue(n.getVarMethodNames().isEmpty());
	}

	@Test
	void requiresChild() {
		Node getNullChild = n.requireChild("GET");
		assertEquals(1, n.getChildren().size());
		assertSame(getNullChild, n.requireChild("GET"));
		assertEquals(1, n.getChildren().size());
		assertSame(getNullChild, n.getChild("GET"));
		Node postItemChild = n.requireChild("POST");
		assertEquals(2, n.getChildren().size());
		assertSame(postItemChild, n.requireChild("POST"));
		assertEquals(2, n.getChildren().size());
		assertSame(postItemChild, n.getChild("POST"));
	}

	@Test
	void putsEndpoints() {
		Endpoint getEndpoint = mock(Endpoint.class);
		doReturn(null).when(getEndpoint).getVarType();
		n.putEndpoint("GET", getEndpoint);
		assertEquals(1, n.getEndpoints().size());
		assertSame(getEndpoint, n.getEndpoint("GET"));
		assertSame(getEndpoint, n.getEndpoint("HEAD"));
		assertNull(n.getEndpoint("POST"));
		assertEquals(Set.of("GET"), n.getMethodNames());
		assertTrue(n.getVarMethodNames().isEmpty());
		Endpoint postEndpoint = mock(Endpoint.class);
		doReturn(Object.class).when(postEndpoint).getVarType();
		n.putEndpoint("POST", postEndpoint);
		assertEquals(2, n.getEndpoints().size());
		assertSame(getEndpoint, n.getEndpoint("GET"));
		assertSame(getEndpoint, n.getEndpoint("HEAD"));
		assertSame(postEndpoint, n.getEndpoint("POST"));
		assertEquals(Set.of("GET", "POST"), n.getMethodNames());
		assertEquals(Set.of("POST"), n.getVarMethodNames());
	}
}
