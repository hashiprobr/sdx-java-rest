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
		assertNull(n.getEndpoint("GET", 0));
		assertNull(n.getEndpoint("GET", 1));
		assertNull(n.getEndpoint("GET", 2));
		assertNull(n.getEndpoint("HEAD", 0));
		assertNull(n.getEndpoint("HEAD", 1));
		assertNull(n.getEndpoint("HEAD", 2));
		assertNull(n.getEndpoint("POST", 0));
		assertNull(n.getEndpoint("POST", 1));
		assertNull(n.getEndpoint("POST", 2));
		assertTrue(n.getMethodNames().isEmpty());
		assertTrue(n.getVarMethodNames().isEmpty());
	}

	@Test
	void requiresChild() {
		Node getNullChild = n.requireChild(null);
		assertEquals(1, n.getChildren().size());
		assertSame(getNullChild, n.requireChild(null));
		assertEquals(1, n.getChildren().size());
		assertSame(getNullChild, n.getChild(null));
		Node postItemChild = n.requireChild("item");
		assertEquals(2, n.getChildren().size());
		assertSame(postItemChild, n.requireChild("item"));
		assertEquals(2, n.getChildren().size());
		assertSame(postItemChild, n.getChild("item"));
	}

	@Test
	void putsEndpoints() {
		Endpoint getEndpoint = mock(Endpoint.class);
		doReturn(null).when(getEndpoint).getVarType();
		n.putEndpoint("GET", getEndpoint);
		assertEquals(1, n.getEndpoints().size());
		assertSame(getEndpoint, n.getEndpoint("GET", 0));
		assertNull(n.getEndpoint("GET", 1));
		assertNull(n.getEndpoint("GET", 2));
		assertSame(getEndpoint, n.getEndpoint("HEAD", 0));
		assertNull(n.getEndpoint("HEAD", 1));
		assertNull(n.getEndpoint("HEAD", 2));
		assertNull(n.getEndpoint("POST", 0));
		assertNull(n.getEndpoint("POST", 1));
		assertNull(n.getEndpoint("POST", 2));
		assertEquals(Set.of("GET"), n.getMethodNames());
		assertTrue(n.getVarMethodNames().isEmpty());
		Endpoint postEndpoint = mock(Endpoint.class);
		doReturn(Object.class).when(postEndpoint).getVarType();
		n.putEndpoint("POST", postEndpoint);
		assertEquals(2, n.getEndpoints().size());
		assertSame(getEndpoint, n.getEndpoint("GET", 0));
		assertNull(n.getEndpoint("GET", 1));
		assertNull(n.getEndpoint("GET", 2));
		assertSame(getEndpoint, n.getEndpoint("HEAD", 0));
		assertNull(n.getEndpoint("HEAD", 1));
		assertNull(n.getEndpoint("HEAD", 2));
		assertSame(postEndpoint, n.getEndpoint("POST", 0));
		assertSame(postEndpoint, n.getEndpoint("POST", 1));
		assertSame(postEndpoint, n.getEndpoint("POST", 2));
		assertEquals(Set.of("GET", "POST"), n.getMethodNames());
		assertEquals(Set.of("POST"), n.getVarMethodNames());
	}
}
