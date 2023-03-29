package br.pro.hashi.sdx.rest.server.tree;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockConstruction;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.MockedConstruction;

import br.pro.hashi.sdx.rest.coding.Coding;
import br.pro.hashi.sdx.rest.reflection.Cache;
import br.pro.hashi.sdx.rest.reflection.exception.ReflectionException;
import br.pro.hashi.sdx.rest.server.RestResource;
import br.pro.hashi.sdx.rest.server.annotation.Nested;
import br.pro.hashi.sdx.rest.server.exception.NotFoundException;
import br.pro.hashi.sdx.rest.server.tree.Tree.Leaf;
import br.pro.hashi.sdx.rest.server.tree.mock.cyclic.Acyclic;
import br.pro.hashi.sdx.rest.server.tree.mock.cyclic.AcyclicInAcyclic;
import br.pro.hashi.sdx.rest.server.tree.mock.cyclic.AcyclicInAcyclicInAcyclic;
import br.pro.hashi.sdx.rest.server.tree.mock.cyclic.AcyclicInAcyclicInFirstOfThree;
import br.pro.hashi.sdx.rest.server.tree.mock.cyclic.AcyclicInAcyclicInFirstOfTwo;
import br.pro.hashi.sdx.rest.server.tree.mock.cyclic.AcyclicInAcyclicInSecondOfThree;
import br.pro.hashi.sdx.rest.server.tree.mock.cyclic.AcyclicInAcyclicInSecondOfTwo;
import br.pro.hashi.sdx.rest.server.tree.mock.cyclic.AcyclicInAcyclicInSelfLoop;
import br.pro.hashi.sdx.rest.server.tree.mock.cyclic.AcyclicInAcyclicInThirdOfThree;
import br.pro.hashi.sdx.rest.server.tree.mock.cyclic.AcyclicInFirstOfThree;
import br.pro.hashi.sdx.rest.server.tree.mock.cyclic.AcyclicInFirstOfTwo;
import br.pro.hashi.sdx.rest.server.tree.mock.cyclic.AcyclicInSecondOfThree;
import br.pro.hashi.sdx.rest.server.tree.mock.cyclic.AcyclicInSecondOfTwo;
import br.pro.hashi.sdx.rest.server.tree.mock.cyclic.AcyclicInSelfLoop;
import br.pro.hashi.sdx.rest.server.tree.mock.cyclic.AcyclicInThirdOfThree;
import br.pro.hashi.sdx.rest.server.tree.mock.cyclic.FirstOfThree;
import br.pro.hashi.sdx.rest.server.tree.mock.cyclic.FirstOfTwo;
import br.pro.hashi.sdx.rest.server.tree.mock.cyclic.SecondOfThree;
import br.pro.hashi.sdx.rest.server.tree.mock.cyclic.SecondOfTwo;
import br.pro.hashi.sdx.rest.server.tree.mock.cyclic.SelfLoop;
import br.pro.hashi.sdx.rest.server.tree.mock.cyclic.ThirdOfThree;
import br.pro.hashi.sdx.rest.server.tree.mock.endpoint.ChildReaches;
import br.pro.hashi.sdx.rest.server.tree.mock.endpoint.Empty;
import br.pro.hashi.sdx.rest.server.tree.mock.endpoint.GetPost;
import br.pro.hashi.sdx.rest.server.tree.mock.endpoint.Head;
import br.pro.hashi.sdx.rest.server.tree.mock.endpoint.Multiple;
import br.pro.hashi.sdx.rest.server.tree.mock.endpoint.NestedReaches;
import br.pro.hashi.sdx.rest.server.tree.mock.endpoint.OneInfiniteReaches;
import br.pro.hashi.sdx.rest.server.tree.mock.endpoint.Options;
import br.pro.hashi.sdx.rest.server.tree.mock.endpoint.PostPut;
import br.pro.hashi.sdx.rest.server.tree.mock.endpoint.PutPatch;
import br.pro.hashi.sdx.rest.server.tree.mock.endpoint.Reaches;
import br.pro.hashi.sdx.rest.server.tree.mock.endpoint.Same;
import br.pro.hashi.sdx.rest.server.tree.mock.endpoint.SameOneItem;
import br.pro.hashi.sdx.rest.server.tree.mock.endpoint.SameVarArgs;
import br.pro.hashi.sdx.rest.server.tree.mock.endpoint.ShadowOneItem;
import br.pro.hashi.sdx.rest.server.tree.mock.endpoint.ShadowVarArgs;
import br.pro.hashi.sdx.rest.server.tree.mock.endpoint.TwoInfiniteReaches;
import br.pro.hashi.sdx.rest.server.tree.mock.endpoint.ZeroInfiniteReaches;
import br.pro.hashi.sdx.rest.server.tree.mock.multiple.*;
import br.pro.hashi.sdx.rest.server.tree.mock.node.Enclosing;
import br.pro.hashi.sdx.rest.server.tree.mock.node.One;
import br.pro.hashi.sdx.rest.server.tree.mock.node.OneInOne;
import br.pro.hashi.sdx.rest.server.tree.mock.node.OneInTwo;
import br.pro.hashi.sdx.rest.server.tree.mock.node.OneInZero;
import br.pro.hashi.sdx.rest.server.tree.mock.node.Two;
import br.pro.hashi.sdx.rest.server.tree.mock.node.TwoInOne;
import br.pro.hashi.sdx.rest.server.tree.mock.node.TwoInTwo;
import br.pro.hashi.sdx.rest.server.tree.mock.node.TwoInZero;
import br.pro.hashi.sdx.rest.server.tree.mock.node.Zero;
import br.pro.hashi.sdx.rest.server.tree.mock.node.ZeroInOne;
import br.pro.hashi.sdx.rest.server.tree.mock.node.ZeroInTwo;
import br.pro.hashi.sdx.rest.server.tree.mock.node.ZeroInZero;

class TreeTest {
	private Cache cache;
	private Tree t;
	private Map<Class<? extends RestResource>, String[]> itemMap;
	private List<String> itemList;
	private List<Integer> distances;

	@BeforeEach
	void setUp() {
		cache = mock(Cache.class);
		t = new Tree(cache, Coding.LOCALE);
		itemMap = new HashMap<>();
		itemMap.put(Enclosing.class, new String[] { "a", "b" });
		itemMap.put(Zero.class, new String[] { "a0", "b0" });
		itemMap.put(ZeroInZero.class, new String[] { "a00", "b00" });
		itemMap.put(OneInZero.class, new String[] { "a10", "b10" });
		itemMap.put(TwoInZero.class, new String[] { "a20", "b20" });
		itemMap.put(One.class, new String[] { "a1", "b1" });
		itemMap.put(ZeroInOne.class, new String[] { "a01", "b01" });
		itemMap.put(OneInOne.class, new String[] { "a11", "b11" });
		itemMap.put(TwoInOne.class, new String[] { "a21", "b21" });
		itemMap.put(Two.class, new String[] { "a2", "b2" });
		itemMap.put(ZeroInTwo.class, new String[] { "a02", "b02" });
		itemMap.put(OneInTwo.class, new String[] { "a12", "b12" });
		itemMap.put(TwoInTwo.class, new String[] { "a22", "b22" });
		itemMap.put(Empty.class, new String[] {});
		itemMap.put(GetPost.class, new String[] { "a" });
		itemMap.put(PostPut.class, new String[] { "b" });
		itemMap.put(PutPatch.class, new String[] { "a", "b" });
		itemMap.put(Reaches.class, new String[] {});
		itemMap.put(ChildReaches.class, new String[] {});
		itemMap.put(NestedReaches.class, new String[] {});
		itemMap.put(ZeroInfiniteReaches.class, new String[] {});
		itemMap.put(OneInfiniteReaches.class, new String[] {});
		itemMap.put(TwoInfiniteReaches.class, new String[] {});
		itemMap.put(Options.class, new String[] {});
		itemMap.put(Head.class, new String[] {});
		itemMap.put(Multiple.class, new String[] {});
		itemMap.put(ShadowOneItem.class, new String[] {});
		itemMap.put(ShadowVarArgs.class, new String[] {});
		itemMap.put(Same.class, new String[] {});
		itemMap.put(SameOneItem.class, new String[] {});
		itemMap.put(SameVarArgs.class, new String[] {});
	}

	@Test
	void putsNothing() {
		putNodesAndEndpoints(Empty.class);
		assertTrue(t.getMethodNames().isEmpty());
	}

	@Test
	void putsGetPost() {
		putNodesAndEndpoints(Empty.class);
		putNodesAndEndpoints(GetPost.class);
		assertEquals(Set.of("GET", "HEAD", "POST", "OPTIONS"), t.getMethodNames());
	}

	@Test
	void putsGetPostPut() {
		putNodesAndEndpoints(Empty.class);
		putNodesAndEndpoints(GetPost.class);
		putNodesAndEndpoints(PostPut.class);
		assertEquals(Set.of("GET", "HEAD", "POST", "PUT", "OPTIONS"), t.getMethodNames());
	}

	@Test
	void putsGetPostPutPatch() {
		putNodesAndEndpoints(Empty.class);
		putNodesAndEndpoints(GetPost.class);
		putNodesAndEndpoints(PostPut.class);
		putNodesAndEndpoints(PutPatch.class);
		assertEquals(Set.of("GET", "HEAD", "POST", "PUT", "PATCH", "OPTIONS"), t.getMethodNames());
	}

	@Test
	void putsGetPostPutPatchWithoutOverlap() {
		putNodesAndEndpoints(GetPost.class);
		putNodesAndEndpoints(PutPatch.class);
		assertEquals(Set.of("GET", "HEAD", "POST", "PUT", "PATCH", "OPTIONS"), t.getMethodNames());
	}

	@Test
	void putsPostPut() {
		putNodesAndEndpoints(Empty.class);
		putNodesAndEndpoints(PostPut.class);
		assertEquals(Set.of("POST", "PUT", "OPTIONS"), t.getMethodNames());
	}

	@Test
	void putsPostPutPatch() {
		putNodesAndEndpoints(Empty.class);
		putNodesAndEndpoints(PostPut.class);
		putNodesAndEndpoints(PutPatch.class);
		assertEquals(Set.of("POST", "PUT", "PATCH", "OPTIONS"), t.getMethodNames());
	}

	@Test
	void putsPutPatch() {
		putNodesAndEndpoints(Empty.class);
		putNodesAndEndpoints(PutPatch.class);
		assertEquals(Set.of("PUT", "PATCH", "OPTIONS"), t.getMethodNames());
	}

	@Test
	void putsEndpointsFromReaches() {
		putNodesAndEndpoints(Reaches.class);
		Node node = getNodeAndAddItems(new String[] {});
		assertNullEndpoint(node, "GET");
		assertNotNullEndpoint(node, "POST");
		assertNullEndpoint(node, "PUT");
		assertNullEndpoint(node, "PATCH");
		assertNullEndpoint(node, "DELETE");
		assertEquals(Set.of("POST"), node.getMethodNames());
		assertTrue(node.getVarMethodNames().isEmpty());
		assertTrue(itemList.isEmpty());
		node = getNodeAndAddItems(new String[] { "0" });
		assertNullEndpoint(node, "GET");
		assertNullEndpoint(node, "POST");
		assertNotNullEndpoint(node, "PUT");
		assertNullEndpoint(node, "PATCH");
		assertNullEndpoint(node, "DELETE");
		assertEquals(Set.of("PUT"), node.getMethodNames());
		assertTrue(node.getVarMethodNames().isEmpty());
		assertEquals(List.of("0"), itemList);
		node = getNodeAndAddItems(new String[] { "0", "1" });
		assertNullEndpoint(node, "GET");
		assertNullEndpoint(node, "POST");
		assertNullEndpoint(node, "PUT");
		assertNotNullEndpoint(node, "PATCH");
		assertNullEndpoint(node, "DELETE");
		assertEquals(Set.of("PATCH"), node.getMethodNames());
		assertTrue(node.getVarMethodNames().isEmpty());
		assertEquals(List.of("0", "1"), itemList);
		assertThrows(NotFoundException.class, () -> {
			getNodeAndAddItems(new String[] { "0", "1", "2" });
		});
	}

	@Test
	void putsEndpointsFromChildReaches() {
		putNodesAndEndpoints(ChildReaches.class);
		Node node = getNodeAndAddItems(new String[] {});
		assertNullEndpoint(node, "GET");
		assertNotNullEndpoint(node, "POST");
		assertNullEndpoint(node, "PUT");
		assertNullEndpoint(node, "PATCH");
		assertNullEndpoint(node, "DELETE");
		assertEquals(Set.of("POST"), node.getMethodNames());
		assertTrue(node.getVarMethodNames().isEmpty());
		assertTrue(itemList.isEmpty());
		node = getNodeAndAddItems(new String[] { "0" });
		assertNullEndpoint(node, "GET");
		assertNullEndpoint(node, "POST");
		assertNotNullEndpoint(node, "PUT");
		assertNullEndpoint(node, "PATCH");
		assertNullEndpoint(node, "DELETE");
		assertEquals(Set.of("PUT"), node.getMethodNames());
		assertTrue(node.getVarMethodNames().isEmpty());
		assertEquals(List.of("0"), itemList);
		node = getNodeAndAddItems(new String[] { "0", "1" });
		assertNullEndpoint(node, "GET");
		assertNullEndpoint(node, "POST");
		assertNullEndpoint(node, "PUT");
		assertNotNullEndpoint(node, "PATCH");
		assertNullEndpoint(node, "DELETE");
		assertEquals(Set.of("PATCH"), node.getMethodNames());
		assertTrue(node.getVarMethodNames().isEmpty());
		assertEquals(List.of("0", "1"), itemList);
		node = getNodeAndAddItems(new String[] { "0", "1", "2" });
		assertNullEndpoint(node, "GET");
		assertNullEndpoint(node, "POST");
		assertNullEndpoint(node, "PUT");
		assertNullEndpoint(node, "PATCH");
		assertNotNullEndpoint(node, "DELETE");
		assertEquals(Set.of("DELETE"), node.getMethodNames());
		assertTrue(node.getVarMethodNames().isEmpty());
		assertEquals(List.of("0", "1", "2"), itemList);
	}

	@Test
	void putsEndpointsFromNestedReaches() {
		putNodesAndEndpoints(NestedReaches.class);
		assertThrows(NotFoundException.class, () -> {
			getNodeAndAddItems(new String[] {});
		});
		Node node = getNodeAndAddItems(new String[] { "0" });
		assertNullEndpoint(node, "GET");
		assertNotNullEndpoint(node, "POST");
		assertNullEndpoint(node, "PUT");
		assertNullEndpoint(node, "PATCH");
		assertNullEndpoint(node, "DELETE");
		assertEquals(Set.of("POST"), node.getMethodNames());
		assertTrue(node.getVarMethodNames().isEmpty());
		assertEquals(List.of("0"), itemList);
		node = getNodeAndAddItems(new String[] { "0", "1" });
		assertNullEndpoint(node, "GET");
		assertNullEndpoint(node, "POST");
		assertNotNullEndpoint(node, "PUT");
		assertNullEndpoint(node, "PATCH");
		assertNullEndpoint(node, "DELETE");
		assertEquals(Set.of("PUT"), node.getMethodNames());
		assertTrue(node.getVarMethodNames().isEmpty());
		assertEquals(List.of("0", "1"), itemList);
		node = getNodeAndAddItems(new String[] { "0", "1", "2" });
		assertNullEndpoint(node, "GET");
		assertNullEndpoint(node, "POST");
		assertNullEndpoint(node, "PUT");
		assertNotNullEndpoint(node, "PATCH");
		assertNullEndpoint(node, "DELETE");
		assertEquals(Set.of("PATCH"), node.getMethodNames());
		assertTrue(node.getVarMethodNames().isEmpty());
		assertEquals(List.of("0", "1", "2"), itemList);
	}

	@Test
	void putsEndpointsFromZeroInfiniteReaches() {
		putNodesAndEndpoints(ZeroInfiniteReaches.class);
		Node node = getNodeAndAddItems(new String[] {});
		assertNullEndpoint(node, "GET");
		assertAllNotNullEndpoint(node, "POST");
		assertNotNullEndpoint(node, "PUT");
		assertNullEndpoint(node, "PATCH");
		assertNullEndpoint(node, "DELETE");
		assertEquals(Set.of("POST", "PUT"), node.getMethodNames());
		assertEquals(Set.of("POST"), node.getVarMethodNames());
		assertTrue(itemList.isEmpty());
		node = getNodeAndAddItems(1, new String[] { "0" });
		assertNullEndpoint(node, "GET");
		assertAllNotNullEndpoint(node, "POST");
		assertNotNullEndpoint(node, "PUT");
		assertNullEndpoint(node, "PATCH");
		assertNullEndpoint(node, "DELETE");
		assertEquals(Set.of("POST", "PUT"), node.getMethodNames());
		assertEquals(Set.of("POST"), node.getVarMethodNames());
		assertEquals(List.of("0"), itemList);
		node = getNodeAndAddItems(2, new String[] { "0", "1" });
		assertNullEndpoint(node, "GET");
		assertAllNotNullEndpoint(node, "POST");
		assertNotNullEndpoint(node, "PUT");
		assertNullEndpoint(node, "PATCH");
		assertNullEndpoint(node, "DELETE");
		assertEquals(Set.of("POST", "PUT"), node.getMethodNames());
		assertEquals(Set.of("POST"), node.getVarMethodNames());
		assertEquals(List.of("0", "1"), itemList);
		node = getNodeAndAddItems(3, new String[] { "0", "1", "2" });
		assertNullEndpoint(node, "GET");
		assertAllNotNullEndpoint(node, "POST");
		assertNotNullEndpoint(node, "PUT");
		assertNullEndpoint(node, "PATCH");
		assertNullEndpoint(node, "DELETE");
		assertEquals(Set.of("POST", "PUT"), node.getMethodNames());
		assertEquals(Set.of("POST"), node.getVarMethodNames());
		assertEquals(List.of("0", "1", "2"), itemList);
	}

	@Test
	void putsEndpointsFromOneInfiniteReaches() {
		putNodesAndEndpoints(OneInfiniteReaches.class);
		Node node = getNodeAndAddItems(new String[] {});
		assertNullEndpoint(node, "GET");
		assertNullEndpoint(node, "POST");
		assertNullEndpoint(node, "PUT");
		assertNotNullEndpoint(node, "PATCH");
		assertNullEndpoint(node, "DELETE");
		assertEquals(Set.of("PATCH"), node.getMethodNames());
		assertTrue(node.getVarMethodNames().isEmpty());
		assertTrue(itemList.isEmpty());
		node = getNodeAndAddItems(new String[] { "0" });
		assertNullEndpoint(node, "GET");
		assertAllNotNullEndpoint(node, "POST");
		assertNotNullEndpoint(node, "PUT");
		assertNullEndpoint(node, "PATCH");
		assertNullEndpoint(node, "DELETE");
		assertEquals(Set.of("POST", "PUT"), node.getMethodNames());
		assertEquals(Set.of("POST"), node.getVarMethodNames());
		assertEquals(List.of("0"), itemList);
		node = getNodeAndAddItems(1, new String[] { "0", "1" });
		assertNullEndpoint(node, "GET");
		assertAllNotNullEndpoint(node, "POST");
		assertNotNullEndpoint(node, "PUT");
		assertNullEndpoint(node, "PATCH");
		assertNullEndpoint(node, "DELETE");
		assertEquals(Set.of("POST", "PUT"), node.getMethodNames());
		assertEquals(Set.of("POST"), node.getVarMethodNames());
		assertEquals(List.of("0", "1"), itemList);
		node = getNodeAndAddItems(2, new String[] { "0", "1", "2" });
		assertNullEndpoint(node, "GET");
		assertAllNotNullEndpoint(node, "POST");
		assertNotNullEndpoint(node, "PUT");
		assertNullEndpoint(node, "PATCH");
		assertNullEndpoint(node, "DELETE");
		assertEquals(Set.of("POST", "PUT"), node.getMethodNames());
		assertEquals(Set.of("POST"), node.getVarMethodNames());
		assertEquals(List.of("0", "1", "2"), itemList);
	}

	@Test
	void putsEndpointsFromTwoInfiniteReaches() {
		putNodesAndEndpoints(TwoInfiniteReaches.class);
		Node node = getNodeAndAddItems(new String[] {});
		assertNullEndpoint(node, "GET");
		assertNullEndpoint(node, "POST");
		assertNullEndpoint(node, "PUT");
		assertNullEndpoint(node, "PATCH");
		assertNotNullEndpoint(node, "DELETE");
		assertEquals(Set.of("DELETE"), node.getMethodNames());
		assertTrue(node.getVarMethodNames().isEmpty());
		assertTrue(itemList.isEmpty());
		node = getNodeAndAddItems(new String[] { "0" });
		assertNullEndpoint(node, "GET");
		assertNullEndpoint(node, "POST");
		assertNullEndpoint(node, "PUT");
		assertNotNullEndpoint(node, "PATCH");
		assertNullEndpoint(node, "DELETE");
		assertEquals(Set.of("PATCH"), node.getMethodNames());
		assertTrue(node.getVarMethodNames().isEmpty());
		assertEquals(List.of("0"), itemList);
		node = getNodeAndAddItems(new String[] { "0", "1" });
		assertNullEndpoint(node, "GET");
		assertAllNotNullEndpoint(node, "POST");
		assertNotNullEndpoint(node, "PUT");
		assertNullEndpoint(node, "PATCH");
		assertNullEndpoint(node, "DELETE");
		assertEquals(Set.of("POST", "PUT"), node.getMethodNames());
		assertEquals(Set.of("POST"), node.getVarMethodNames());
		assertEquals(List.of("0", "1"), itemList);
		node = getNodeAndAddItems(1, new String[] { "0", "1", "2" });
		assertNullEndpoint(node, "GET");
		assertAllNotNullEndpoint(node, "POST");
		assertNotNullEndpoint(node, "PUT");
		assertNullEndpoint(node, "PATCH");
		assertNullEndpoint(node, "DELETE");
		assertEquals(Set.of("POST", "PUT"), node.getMethodNames());
		assertEquals(Set.of("POST"), node.getVarMethodNames());
		assertEquals(List.of("0", "1", "2"), itemList);
	}

	private void assertNullEndpoint(Node node, String methodName) {
		assertNull(node.getEndpoint(methodName, 0));
		assertNull(node.getEndpoint(methodName, 1));
		assertNull(node.getEndpoint(methodName, 2));
	}

	private void assertNotNullEndpoint(Node node, String methodName) {
		assertNotNull(node.getEndpoint(methodName, 0));
		assertNull(node.getEndpoint(methodName, 1));
		assertNull(node.getEndpoint(methodName, 2));
	}

	private void assertAllNotNullEndpoint(Node node, String methodName) {
		assertNotNull(node.getEndpoint(methodName, 0));
		assertNotNull(node.getEndpoint(methodName, 1));
		assertNotNull(node.getEndpoint(methodName, 2));
	}

	@Test
	void doesNotPutEndpointsFromOptions() {
		Exception exception = assertThrows(ReflectionException.class, () -> {
			putNodesAndEndpoints(Options.class);
		});
		assertEquals("br.pro.hashi.sdx.rest.server.tree.mock.endpoint.Options cannot have OPTIONS endpoints", exception.getMessage());
	}

	@Test
	void doesNotPutEndpointsFromHead() {
		Exception exception = assertThrows(ReflectionException.class, () -> {
			putNodesAndEndpoints(Head.class);
		});
		assertEquals("br.pro.hashi.sdx.rest.server.tree.mock.endpoint.Head cannot have HEAD endpoints", exception.getMessage());
	}

	@Test
	void doesNotPutEndpointsFromSameResource() {
		Exception exception = assertThrows(ReflectionException.class, () -> {
			putNodesAndEndpoints(Same.class);
		});
		assertEquals("br.pro.hashi.sdx.rest.server.tree.mock.endpoint.Same has multiple GET endpoints in the same path", exception.getMessage());
	}

	@Test
	void doesNotPutEndpointsFromSamePath() {
		putNodesAndEndpoints(SameOneItem.class);
		Exception exception = assertThrows(ReflectionException.class, () -> {
			putNodesAndEndpoints(SameVarArgs.class);
		});
		assertEquals("br.pro.hashi.sdx.rest.server.tree.mock.endpoint.SameOneItem and br.pro.hashi.sdx.rest.server.tree.mock.endpoint.SameVarArgs have GET endpoints in the same path", exception.getMessage());
	}

	@Test
	void doesNotPutEndpointsFromShadowing() {
		putNodesAndEndpoints(ShadowVarArgs.class);
		Exception exception = assertThrows(ReflectionException.class, () -> {
			putNodesAndEndpoints(ShadowOneItem.class);
		});
		assertEquals("br.pro.hashi.sdx.rest.server.tree.mock.endpoint.ShadowOneItem has a GET endpoint that shadows a varargs endpoint", exception.getMessage());
	}

	@Test
	void doesNotPutEndpointsFromShadowed() {
		putNodesAndEndpoints(ShadowOneItem.class);
		Exception exception = assertThrows(ReflectionException.class, () -> {
			putNodesAndEndpoints(ShadowVarArgs.class);
		});
		assertEquals("br.pro.hashi.sdx.rest.server.tree.mock.endpoint.ShadowVarArgs has a POST varargs endpoint shadowed by another endpoint", exception.getMessage());
	}

	@Test
	void putsNodeFromEnclosing() {
		try (MockedConstruction<Endpoint> construction = mockEndpointConstruction()) {
			putNodesAndEndpoints(Enclosing.class);
			assertDistance(0);
			List<Endpoint> endpoints = construction.constructed();
			assertEndpoint(endpoints.get(0), new String[] { "a", "b" });
			assertTrue(itemList.isEmpty());
		}
	}

	@Test
	void putsNodeFromZero() {
		try (MockedConstruction<Endpoint> construction = mockEndpointConstruction()) {
			putNodesAndEndpoints(Zero.class);
			assertDistance(0);
			List<Endpoint> endpoints = construction.constructed();
			assertEndpoint(endpoints.get(0), new String[] { "a", "b", "a0", "b0" });
			assertTrue(itemList.isEmpty());
		}
	}

	@Test
	void putsNodeFromOne() {
		try (MockedConstruction<Endpoint> construction = mockEndpointConstruction()) {
			putNodesAndEndpoints(One.class);
			assertDistance(1);
			List<Endpoint> endpoints = construction.constructed();
			assertEndpoint(endpoints.get(0), new String[] { "a", "b", "0", "a1", "b1" });
			assertEquals(List.of("0"), itemList);
		}
	}

	@Test
	void putsNodeFromTwo() {
		try (MockedConstruction<Endpoint> construction = mockEndpointConstruction()) {
			putNodesAndEndpoints(Two.class);
			assertDistance(2);
			List<Endpoint> endpoints = construction.constructed();
			assertEndpoint(endpoints.get(0), new String[] { "a", "b", "0", "1", "a2", "b2" });
			assertEquals(List.of("0", "1"), itemList);
		}
	}

	@Test
	void putsNodeFromAll() {
		try (MockedConstruction<Endpoint> construction = mockEndpointConstruction()) {
			putNodesAndEndpoints(Zero.class);
			assertDistance(0, 0);
			putNodesAndEndpoints(One.class);
			assertDistance(1, 1);
			putNodesAndEndpoints(Two.class);
			assertDistance(2, 2);
			List<Endpoint> endpoints = construction.constructed();
			assertEndpoint(endpoints.get(0), new String[] { "a", "b", "a0", "b0" });
			assertTrue(itemList.isEmpty());
			assertEndpoint(endpoints.get(1), new String[] { "a", "b", "0", "a1", "b1" });
			assertEquals(List.of("0"), itemList);
			assertEndpoint(endpoints.get(2), new String[] { "a", "b", "0", "1", "a2", "b2" });
			assertEquals(List.of("0", "1"), itemList);
		}
	}

	@Test
	void putsNodeFromZeroInZero() {
		try (MockedConstruction<Endpoint> construction = mockEndpointConstruction()) {
			putNodesAndEndpoints(ZeroInZero.class);
			assertDistance(0);
			List<Endpoint> endpoints = construction.constructed();
			assertEndpoint(endpoints.get(0), new String[] { "a", "b", "a0", "b0", "a00", "b00" });
			assertTrue(itemList.isEmpty());
		}
	}

	@Test
	void putsNodeFromOneInZero() {
		try (MockedConstruction<Endpoint> construction = mockEndpointConstruction()) {
			putNodesAndEndpoints(OneInZero.class);
			assertDistance(1);
			List<Endpoint> endpoints = construction.constructed();
			assertEndpoint(endpoints.get(0), new String[] { "a", "b", "a0", "b0", "0", "a10", "b10" });
			assertEquals(List.of("0"), itemList);
		}
	}

	@Test
	void putsNodeFromTwoInZero() {
		try (MockedConstruction<Endpoint> construction = mockEndpointConstruction()) {
			putNodesAndEndpoints(TwoInZero.class);
			assertDistance(2);
			List<Endpoint> endpoints = construction.constructed();
			assertEndpoint(endpoints.get(0), new String[] { "a", "b", "a0", "b0", "0", "1", "a20", "b20" });
			assertEquals(List.of("0", "1"), itemList);
		}
	}

	@Test
	void putsNodeFromAllInZero() {
		try (MockedConstruction<Endpoint> construction = mockEndpointConstruction()) {
			putNodesAndEndpoints(ZeroInZero.class);
			assertDistance(0, 0);
			putNodesAndEndpoints(OneInZero.class);
			assertDistance(1, 1);
			putNodesAndEndpoints(TwoInZero.class);
			assertDistance(2, 2);
			List<Endpoint> endpoints = construction.constructed();
			assertEndpoint(endpoints.get(0), new String[] { "a", "b", "a0", "b0", "a00", "b00" });
			assertTrue(itemList.isEmpty());
			assertEndpoint(endpoints.get(1), new String[] { "a", "b", "a0", "b0", "0", "a10", "b10" });
			assertEquals(List.of("0"), itemList);
			assertEndpoint(endpoints.get(2), new String[] { "a", "b", "a0", "b0", "0", "1", "a20", "b20" });
			assertEquals(List.of("0", "1"), itemList);
		}
	}

	@Test
	void putsNodeFromZeroInOne() {
		try (MockedConstruction<Endpoint> construction = mockEndpointConstruction()) {
			putNodesAndEndpoints(ZeroInOne.class);
			assertDistance(1);
			List<Endpoint> endpoints = construction.constructed();
			assertEndpoint(endpoints.get(0), new String[] { "a", "b", "0", "a1", "b1", "a01", "b01" });
			assertEquals(List.of("0"), itemList);
		}
	}

	@Test
	void putsNodeFromOneInOne() {
		try (MockedConstruction<Endpoint> construction = mockEndpointConstruction()) {
			putNodesAndEndpoints(OneInOne.class);
			assertDistance(2);
			List<Endpoint> endpoints = construction.constructed();
			assertEndpoint(endpoints.get(0), new String[] { "a", "b", "0", "a1", "b1", "1", "a11", "b11" });
			assertEquals(List.of("0", "1"), itemList);
		}
	}

	@Test
	void putsNodeFromTwoInOne() {
		try (MockedConstruction<Endpoint> construction = mockEndpointConstruction()) {
			putNodesAndEndpoints(TwoInOne.class);
			assertDistance(3);
			List<Endpoint> endpoints = construction.constructed();
			assertEndpoint(endpoints.get(0), new String[] { "a", "b", "0", "a1", "b1", "1", "2", "a21", "b21" });
			assertEquals(List.of("0", "1", "2"), itemList);
		}
	}

	@Test
	void putsNodeFromAllInOne() {
		try (MockedConstruction<Endpoint> construction = mockEndpointConstruction()) {
			putNodesAndEndpoints(ZeroInOne.class);
			assertDistance(1, 0);
			putNodesAndEndpoints(OneInOne.class);
			assertDistance(2, 1);
			putNodesAndEndpoints(TwoInOne.class);
			assertDistance(3, 2);
			List<Endpoint> endpoints = construction.constructed();
			assertEndpoint(endpoints.get(0), new String[] { "a", "b", "0", "a1", "b1", "a01", "b01" });
			assertEquals(List.of("0"), itemList);
			assertEndpoint(endpoints.get(1), new String[] { "a", "b", "0", "a1", "b1", "1", "a11", "b11" });
			assertEquals(List.of("0", "1"), itemList);
			assertEndpoint(endpoints.get(2), new String[] { "a", "b", "0", "a1", "b1", "1", "2", "a21", "b21" });
			assertEquals(List.of("0", "1", "2"), itemList);
		}
	}

	@Test
	void putsNodeFromZeroInTwo() {
		try (MockedConstruction<Endpoint> construction = mockEndpointConstruction()) {
			putNodesAndEndpoints(ZeroInTwo.class);
			assertDistance(2);
			List<Endpoint> endpoints = construction.constructed();
			assertEndpoint(endpoints.get(0), new String[] { "a", "b", "0", "1", "a2", "b2", "a02", "b02" });
			assertEquals(List.of("0", "1"), itemList);
		}
	}

	@Test
	void putsNodeFromOneInTwo() {
		try (MockedConstruction<Endpoint> construction = mockEndpointConstruction()) {
			putNodesAndEndpoints(OneInTwo.class);
			assertDistance(3);
			List<Endpoint> endpoints = construction.constructed();
			assertEndpoint(endpoints.get(0), new String[] { "a", "b", "0", "1", "a2", "b2", "2", "a12", "b12" });
			assertEquals(List.of("0", "1", "2"), itemList);
		}
	}

	@Test
	void putsNodeFromTwoInTwo() {
		try (MockedConstruction<Endpoint> construction = mockEndpointConstruction()) {
			putNodesAndEndpoints(TwoInTwo.class);
			assertDistance(4);
			List<Endpoint> endpoints = construction.constructed();
			assertEndpoint(endpoints.get(0), new String[] { "a", "b", "0", "1", "a2", "b2", "2", "3", "a22", "b22" });
			assertEquals(List.of("0", "1", "2", "3"), itemList);
		}
	}

	@Test
	void putsNodeFromAllInTwo() {
		try (MockedConstruction<Endpoint> construction = mockEndpointConstruction()) {
			putNodesAndEndpoints(ZeroInTwo.class);
			assertDistance(2, 0);
			putNodesAndEndpoints(OneInTwo.class);
			assertDistance(3, 1);
			putNodesAndEndpoints(TwoInTwo.class);
			assertDistance(4, 2);
			List<Endpoint> endpoints = construction.constructed();
			assertEndpoint(endpoints.get(0), new String[] { "a", "b", "0", "1", "a2", "b2", "a02", "b02" });
			assertEquals(List.of("0", "1"), itemList);
			assertEndpoint(endpoints.get(1), new String[] { "a", "b", "0", "1", "a2", "b2", "2", "a12", "b12" });
			assertEquals(List.of("0", "1", "2"), itemList);
			assertEndpoint(endpoints.get(2), new String[] { "a", "b", "0", "1", "a2", "b2", "2", "3", "a22", "b22" });
			assertEquals(List.of("0", "1", "2", "3"), itemList);
		}
	}

	private MockedConstruction<Endpoint> mockEndpointConstruction() {
		distances = new ArrayList<>();
		return mockConstruction(Endpoint.class, (mock, context) -> {
			distances.add((int) context.arguments().get(1));
		});
	}

	private void putNodesAndEndpoints(Class<? extends RestResource> type) {
		t.putNodesAndEndpoints(type, type.getName(), itemMap);
	}

	private void assertDistance(int expected) {
		assertDistance(expected, 0);
	}

	private void assertDistance(int expected, int actual) {
		assertEquals(expected, distances.get(actual));
	}

	private void assertEndpoint(Endpoint expected, String[] actual) {
		Node node = getNodeAndAddItems(actual);
		assertSame(expected, node.getEndpoint("GET", 0));
	}

	private Node getNodeAndAddItems(String[] items) {
		return getNodeAndAddItems(0, items);
	}

	private Node getNodeAndAddItems(int varSize, String[] items) {
		itemList = new ArrayList<>();
		Leaf leaf = t.getLeafAndAddItems(items, itemList);
		assertEquals(varSize, leaf.varSize());
		return leaf.node();
	}

	@Test
	void getsAnnotationStackFromAcyclic() {
		Stack<Nested> stack = getAnnotationStack(Acyclic.class);
		assertTrue(stack.isEmpty());
	}

	@Test
	void getsAnnotationStackFromAcyclicInAcyclic() {
		Stack<Nested> stack = getAnnotationStack(AcyclicInAcyclic.class);
		assertEquals(1, stack.size());
		assertEquals(Acyclic.class, stack.get(0).in());
	}

	@Test
	void getsAnnotationStackFromAcyclicInAcyclicInAcyclic() {
		Stack<Nested> stack = getAnnotationStack(AcyclicInAcyclicInAcyclic.class);
		assertEquals(2, stack.size());
		assertEquals(AcyclicInAcyclic.class, stack.get(0).in());
		assertEquals(Acyclic.class, stack.get(1).in());
	}

	@ParameterizedTest
	@ValueSource(classes = {
			SelfLoop.class,
			AcyclicInSelfLoop.class,
			AcyclicInAcyclicInSelfLoop.class,
			FirstOfTwo.class,
			AcyclicInFirstOfTwo.class,
			AcyclicInAcyclicInFirstOfTwo.class,
			SecondOfTwo.class,
			AcyclicInSecondOfTwo.class,
			AcyclicInAcyclicInSecondOfTwo.class,
			FirstOfThree.class,
			AcyclicInFirstOfThree.class,
			AcyclicInAcyclicInFirstOfThree.class,
			SecondOfThree.class,
			AcyclicInSecondOfThree.class,
			AcyclicInAcyclicInSecondOfThree.class,
			ThirdOfThree.class,
			AcyclicInThirdOfThree.class,
			AcyclicInAcyclicInThirdOfThree.class })
	void doesNotGetAnnotationStack(Class<? extends RestResource> type) {
		assertThrows(ReflectionException.class, () -> {
			getAnnotationStack(type);
		});
	}

	private Stack<Nested> getAnnotationStack(Class<? extends RestResource> type) {
		return t.getAnnotationStack(type, type.getName());
	}

	@ParameterizedTest
	@ValueSource(classes = {
			ResourceWithInterfaceAndNestedInterface.class,
			ResourceWithInterfaceAndChildNestedInterface.class,
			ResourceWithInterfaceAndNestedChildInterface.class,
			ResourceWithChildInterfaceAndNestedInterface.class,
			ResourceWithChildInterfaceAndChildNestedInterface.class,
			ResourceWithChildInterfaceAndNestedChildInterface.class,
			ResourceWithNestedInterface.class,
			ResourceWithNestedInterfaceAndInterface.class,
			ResourceWithNestedInterfaceAndChildInterface.class,
			ResourceWithChildNestedInterface.class,
			ResourceWithChildNestedInterfaceAndInterface.class,
			ResourceWithChildNestedInterfaceAndChildInterface.class,
			ResourceWithNestedChildInterface.class,
			ResourceWithNestedChildInterfaceAndChildInterface.class,
			ChildResourceWithInterfaceAndNestedInterface.class,
			ChildResourceWithInterfaceAndChildNestedInterface.class,
			ChildResourceWithInterfaceAndNestedChildInterface.class,
			ChildResourceWithChildInterfaceAndNestedInterface.class,
			ChildResourceWithChildInterfaceAndChildNestedInterface.class,
			ChildResourceWithChildInterfaceAndNestedChildInterface.class,
			ChildResourceWithNestedInterface.class,
			ChildResourceWithNestedInterfaceAndInterface.class,
			ChildResourceWithNestedInterfaceAndChildInterface.class,
			ChildResourceWithChildNestedInterface.class,
			ChildResourceWithChildNestedInterfaceAndInterface.class,
			ChildResourceWithChildNestedInterfaceAndChildInterface.class,
			ChildResourceWithNestedChildInterface.class,
			ChildResourceWithNestedChildInterfaceAndChildInterface.class,
			NestedResource.class,
			NestedResourceWithInterface.class,
			NestedResourceWithChildInterface.class,
			ChildNestedResource.class,
			ChildNestedResourceWithInterface.class,
			ChildNestedResourceWithChildInterface.class,
			NestedChildResource.class,
			NestedChildResourceWithInterface.class,
			NestedChildResourceWithChildInterface.class,
	})
	void getsAnnotation(Class<? extends RestResource> type) {
		assertNotNull(getAnnotation(type));
	}

	@ParameterizedTest
	@ValueSource(classes = {
			Resource.class,
			ResourceWithInterface.class,
			ResourceWithChildInterface.class,
			ChildResource.class,
			ChildResourceWithInterface.class,
			ChildResourceWithChildInterface.class })
	void getsNullAnnotation(Class<? extends RestResource> type) {
		assertNull(getAnnotation(type));
	}

	@ParameterizedTest
	@ValueSource(classes = {
			ResourceWithNestedInterfaceAndNestedChildInterface.class,
			ResourceWithChildNestedInterfaceAndNestedChildInterface.class,
			ResourceWithNestedChildInterfaceAndNestedInterface.class,
			ResourceWithNestedChildInterfaceAndChildNestedInterface.class,
			ChildResourceWithNestedInterfaceAndNestedChildInterface.class,
			ChildResourceWithChildNestedInterfaceAndNestedChildInterface.class,
			ChildResourceWithNestedChildInterfaceAndNestedInterface.class,
			ChildResourceWithNestedChildInterfaceAndChildNestedInterface.class,
			NestedResourceWithInterfaceAndNestedInterface.class,
			NestedResourceWithInterfaceAndChildNestedInterface.class,
			NestedResourceWithInterfaceAndNestedChildInterface.class,
			NestedResourceWithChildInterfaceAndNestedInterface.class,
			NestedResourceWithChildInterfaceAndChildNestedInterface.class,
			NestedResourceWithChildInterfaceAndNestedChildInterface.class,
			NestedResourceWithNestedInterface.class,
			NestedResourceWithNestedInterfaceAndInterface.class,
			NestedResourceWithNestedInterfaceAndChildInterface.class,
			NestedResourceWithNestedInterfaceAndNestedChildInterface.class,
			NestedResourceWithChildNestedInterface.class,
			NestedResourceWithChildNestedInterfaceAndInterface.class,
			NestedResourceWithChildNestedInterfaceAndChildInterface.class,
			NestedResourceWithChildNestedInterfaceAndNestedChildInterface.class,
			NestedResourceWithNestedChildInterface.class,
			NestedResourceWithNestedChildInterfaceAndChildInterface.class,
			NestedResourceWithNestedChildInterfaceAndNestedInterface.class,
			NestedResourceWithNestedChildInterfaceAndChildNestedInterface.class,
			ChildNestedResourceWithInterfaceAndNestedInterface.class,
			ChildNestedResourceWithInterfaceAndChildNestedInterface.class,
			ChildNestedResourceWithInterfaceAndNestedChildInterface.class,
			ChildNestedResourceWithChildInterfaceAndNestedInterface.class,
			ChildNestedResourceWithChildInterfaceAndChildNestedInterface.class,
			ChildNestedResourceWithChildInterfaceAndNestedChildInterface.class,
			ChildNestedResourceWithNestedInterface.class,
			ChildNestedResourceWithNestedInterfaceAndInterface.class,
			ChildNestedResourceWithNestedInterfaceAndChildInterface.class,
			ChildNestedResourceWithNestedInterfaceAndNestedChildInterface.class,
			ChildNestedResourceWithChildNestedInterface.class,
			ChildNestedResourceWithChildNestedInterfaceAndInterface.class,
			ChildNestedResourceWithChildNestedInterfaceAndChildInterface.class,
			ChildNestedResourceWithChildNestedInterfaceAndNestedChildInterface.class,
			ChildNestedResourceWithNestedChildInterface.class,
			ChildNestedResourceWithNestedChildInterfaceAndChildInterface.class,
			ChildNestedResourceWithNestedChildInterfaceAndNestedInterface.class,
			ChildNestedResourceWithNestedChildInterfaceAndChildNestedInterface.class,
			NestedChildResourceWithInterfaceAndNestedInterface.class,
			NestedChildResourceWithInterfaceAndChildNestedInterface.class,
			NestedChildResourceWithInterfaceAndNestedChildInterface.class,
			NestedChildResourceWithChildInterfaceAndNestedInterface.class,
			NestedChildResourceWithChildInterfaceAndChildNestedInterface.class,
			NestedChildResourceWithChildInterfaceAndNestedChildInterface.class,
			NestedChildResourceWithNestedInterface.class,
			NestedChildResourceWithNestedInterfaceAndInterface.class,
			NestedChildResourceWithNestedInterfaceAndChildInterface.class,
			NestedChildResourceWithNestedInterfaceAndNestedChildInterface.class,
			NestedChildResourceWithChildNestedInterface.class,
			NestedChildResourceWithChildNestedInterfaceAndInterface.class,
			NestedChildResourceWithChildNestedInterfaceAndChildInterface.class,
			NestedChildResourceWithChildNestedInterfaceAndNestedChildInterface.class,
			NestedChildResourceWithNestedChildInterface.class,
			NestedChildResourceWithNestedChildInterfaceAndChildInterface.class,
			NestedChildResourceWithNestedChildInterfaceAndNestedInterface.class,
			NestedChildResourceWithNestedChildInterfaceAndChildNestedInterface.class })
	void doesNotGetAnnotation(Class<? extends RestResource> type) {
		assertThrows(ReflectionException.class, () -> {
			getAnnotation(type);
		});
	}

	private Nested getAnnotation(Class<? extends RestResource> type) {
		return t.getAnnotation(type, type.getName());
	}
}
