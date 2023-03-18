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
import br.pro.hashi.sdx.rest.server.exception.ResponseException;
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
import br.pro.hashi.sdx.rest.server.tree.mock.endpoint.Multiple;
import br.pro.hashi.sdx.rest.server.tree.mock.endpoint.NestedReaches;
import br.pro.hashi.sdx.rest.server.tree.mock.endpoint.Reaches;
import br.pro.hashi.sdx.rest.server.tree.mock.endpoint.SamePath;
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
		itemMap.put(Reaches.class, new String[] {});
		itemMap.put(ChildReaches.class, new String[] {});
		itemMap.put(NestedReaches.class, new String[] {});
		itemMap.put(Multiple.class, new String[] {});
		itemMap.put(SamePath.class, new String[] {});
	}

	@Test
	void putsEndpointsFromReaches() {
		putNode(Reaches.class);
		Node node = getNode(new String[] {});
		assertNull(node.getEndpoint("GET"));
		assertNotNull(node.getEndpoint("POST"));
		assertNull(node.getEndpoint("PUT"));
		assertNull(node.getEndpoint("PATCH"));
		assertNull(node.getEndpoint("DELETE"));
		assertTrue(itemList.isEmpty());
		node = getNode(new String[] { "0" });
		assertNull(node.getEndpoint("GET"));
		assertNull(node.getEndpoint("POST"));
		assertNotNull(node.getEndpoint("PUT"));
		assertNull(node.getEndpoint("PATCH"));
		assertNull(node.getEndpoint("DELETE"));
		assertEquals(List.of("0"), itemList);
		node = getNode(new String[] { "0", "1" });
		assertNull(node.getEndpoint("GET"));
		assertNull(node.getEndpoint("POST"));
		assertNull(node.getEndpoint("PUT"));
		assertNotNull(node.getEndpoint("PATCH"));
		assertNull(node.getEndpoint("DELETE"));
		assertEquals(List.of("0", "1"), itemList);
		assertThrows(ResponseException.class, () -> {
			getNode(new String[] { "0", "1", "2" });
		});
	}

	@Test
	void putsEndpointsFromChildReaches() {
		putNode(ChildReaches.class);
		Node node = getNode(new String[] {});
		assertNull(node.getEndpoint("GET"));
		assertNotNull(node.getEndpoint("POST"));
		assertNull(node.getEndpoint("PUT"));
		assertNull(node.getEndpoint("PATCH"));
		assertNull(node.getEndpoint("DELETE"));
		assertTrue(itemList.isEmpty());
		node = getNode(new String[] { "0" });
		assertNull(node.getEndpoint("GET"));
		assertNull(node.getEndpoint("POST"));
		assertNotNull(node.getEndpoint("PUT"));
		assertNull(node.getEndpoint("PATCH"));
		assertNull(node.getEndpoint("DELETE"));
		assertEquals(List.of("0"), itemList);
		node = getNode(new String[] { "0", "1" });
		assertNull(node.getEndpoint("GET"));
		assertNull(node.getEndpoint("POST"));
		assertNull(node.getEndpoint("PUT"));
		assertNotNull(node.getEndpoint("PATCH"));
		assertNull(node.getEndpoint("DELETE"));
		assertEquals(List.of("0", "1"), itemList);
		node = getNode(new String[] { "0", "1", "2" });
		assertNull(node.getEndpoint("GET"));
		assertNull(node.getEndpoint("POST"));
		assertNull(node.getEndpoint("PUT"));
		assertNull(node.getEndpoint("PATCH"));
		assertNotNull(node.getEndpoint("DELETE"));
		assertEquals(List.of("0", "1", "2"), itemList);
	}

	@Test
	void putsEndpointsFromNestedReaches() {
		putNode(NestedReaches.class);
		Node node = getNode(new String[] { "0" });
		assertNull(node.getEndpoint("GET"));
		assertNotNull(node.getEndpoint("POST"));
		assertNull(node.getEndpoint("PUT"));
		assertNull(node.getEndpoint("PATCH"));
		assertNull(node.getEndpoint("DELETE"));
		assertEquals(List.of("0"), itemList);
		node = getNode(new String[] { "0", "1" });
		assertNull(node.getEndpoint("GET"));
		assertNull(node.getEndpoint("POST"));
		assertNotNull(node.getEndpoint("PUT"));
		assertNull(node.getEndpoint("PATCH"));
		assertNull(node.getEndpoint("DELETE"));
		assertEquals(List.of("0", "1"), itemList);
		node = getNode(new String[] { "0", "1", "2" });
		assertNull(node.getEndpoint("GET"));
		assertNull(node.getEndpoint("POST"));
		assertNull(node.getEndpoint("PUT"));
		assertNotNull(node.getEndpoint("PATCH"));
		assertNull(node.getEndpoint("DELETE"));
		assertEquals(List.of("0", "1", "2"), itemList);
	}

	@Test
	void doesNotPutEndpointsFromMultiple() {
		Exception exception = assertThrows(ReflectionException.class, () -> {
			putNode(Multiple.class);
		});
		assertEquals("br.pro.hashi.sdx.rest.server.tree.mock.endpoint.Multiple has multiple GET endpoints in the same path", exception.getMessage());
	}

	@Test
	void doesNotPutEndpointsFromSamePath() {
		putNode(NestedReaches.class);
		Exception exception = assertThrows(ReflectionException.class, () -> {
			putNode(SamePath.class);
		});
		assertEquals("br.pro.hashi.sdx.rest.server.tree.mock.endpoint.SamePath and br.pro.hashi.sdx.rest.server.tree.mock.endpoint.NestedReaches have POST endpoints in the same path", exception.getMessage());
	}

	@Test
	void putsNodeFromEnclosing() {
		try (MockedConstruction<Endpoint> construction = mockEndpointConstruction()) {
			putNode(Enclosing.class);
			assertDistance(0);
			List<Endpoint> endpoints = construction.constructed();
			assertEndpoint(endpoints.get(0), new String[] { "a", "b" });
			assertTrue(itemList.isEmpty());
		}
	}

	@Test
	void putsNodeFromZero() {
		try (MockedConstruction<Endpoint> construction = mockEndpointConstruction()) {
			putNode(Zero.class);
			assertDistance(0);
			List<Endpoint> endpoints = construction.constructed();
			assertEndpoint(endpoints.get(0), new String[] { "a", "b", "a0", "b0" });
			assertTrue(itemList.isEmpty());
		}
	}

	@Test
	void putsNodeFromOne() {
		try (MockedConstruction<Endpoint> construction = mockEndpointConstruction()) {
			putNode(One.class);
			assertDistance(1);
			List<Endpoint> endpoints = construction.constructed();
			assertEndpoint(endpoints.get(0), new String[] { "a", "b", "0", "a1", "b1" });
			assertEquals(List.of("0"), itemList);
		}
	}

	@Test
	void putsNodeFromTwo() {
		try (MockedConstruction<Endpoint> construction = mockEndpointConstruction()) {
			putNode(Two.class);
			assertDistance(2);
			List<Endpoint> endpoints = construction.constructed();
			assertEndpoint(endpoints.get(0), new String[] { "a", "b", "0", "1", "a2", "b2" });
			assertEquals(List.of("0", "1"), itemList);
		}
	}

	@Test
	void putsNodeFromAll() {
		try (MockedConstruction<Endpoint> construction = mockEndpointConstruction()) {
			putNode(Zero.class);
			assertDistance(0, 0);
			putNode(One.class);
			assertDistance(1, 1);
			putNode(Two.class);
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
			putNode(ZeroInZero.class);
			assertDistance(0);
			List<Endpoint> endpoints = construction.constructed();
			assertEndpoint(endpoints.get(0), new String[] { "a", "b", "a0", "b0", "a00", "b00" });
			assertTrue(itemList.isEmpty());
		}
	}

	@Test
	void putsNodeFromOneInZero() {
		try (MockedConstruction<Endpoint> construction = mockEndpointConstruction()) {
			putNode(OneInZero.class);
			assertDistance(1);
			List<Endpoint> endpoints = construction.constructed();
			assertEndpoint(endpoints.get(0), new String[] { "a", "b", "a0", "b0", "0", "a10", "b10" });
			assertEquals(List.of("0"), itemList);
		}
	}

	@Test
	void putsNodeFromTwoInZero() {
		try (MockedConstruction<Endpoint> construction = mockEndpointConstruction()) {
			putNode(TwoInZero.class);
			assertDistance(2);
			List<Endpoint> endpoints = construction.constructed();
			assertEndpoint(endpoints.get(0), new String[] { "a", "b", "a0", "b0", "0", "1", "a20", "b20" });
			assertEquals(List.of("0", "1"), itemList);
		}
	}

	@Test
	void putsNodeFromAllInZero() {
		try (MockedConstruction<Endpoint> construction = mockEndpointConstruction()) {
			putNode(ZeroInZero.class);
			assertDistance(0, 0);
			putNode(OneInZero.class);
			assertDistance(1, 1);
			putNode(TwoInZero.class);
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
			putNode(ZeroInOne.class);
			assertDistance(1);
			List<Endpoint> endpoints = construction.constructed();
			assertEndpoint(endpoints.get(0), new String[] { "a", "b", "0", "a1", "b1", "a01", "b01" });
			assertEquals(List.of("0"), itemList);
		}
	}

	@Test
	void putsNodeFromOneInOne() {
		try (MockedConstruction<Endpoint> construction = mockEndpointConstruction()) {
			putNode(OneInOne.class);
			assertDistance(2);
			List<Endpoint> endpoints = construction.constructed();
			assertEndpoint(endpoints.get(0), new String[] { "a", "b", "0", "a1", "b1", "1", "a11", "b11" });
			assertEquals(List.of("0", "1"), itemList);
		}
	}

	@Test
	void putsNodeFromTwoInOne() {
		try (MockedConstruction<Endpoint> construction = mockEndpointConstruction()) {
			putNode(TwoInOne.class);
			assertDistance(3);
			List<Endpoint> endpoints = construction.constructed();
			assertEndpoint(endpoints.get(0), new String[] { "a", "b", "0", "a1", "b1", "1", "2", "a21", "b21" });
			assertEquals(List.of("0", "1", "2"), itemList);
		}
	}

	@Test
	void putsNodeFromAllInOne() {
		try (MockedConstruction<Endpoint> construction = mockEndpointConstruction()) {
			putNode(ZeroInOne.class);
			assertDistance(1, 0);
			putNode(OneInOne.class);
			assertDistance(2, 1);
			putNode(TwoInOne.class);
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
			putNode(ZeroInTwo.class);
			assertDistance(2);
			List<Endpoint> endpoints = construction.constructed();
			assertEndpoint(endpoints.get(0), new String[] { "a", "b", "0", "1", "a2", "b2", "a02", "b02" });
			assertEquals(List.of("0", "1"), itemList);
		}
	}

	@Test
	void putsNodeFromOneInTwo() {
		try (MockedConstruction<Endpoint> construction = mockEndpointConstruction()) {
			putNode(OneInTwo.class);
			assertDistance(3);
			List<Endpoint> endpoints = construction.constructed();
			assertEndpoint(endpoints.get(0), new String[] { "a", "b", "0", "1", "a2", "b2", "2", "a12", "b12" });
			assertEquals(List.of("0", "1", "2"), itemList);
		}
	}

	@Test
	void putsNodeFromTwoInTwo() {
		try (MockedConstruction<Endpoint> construction = mockEndpointConstruction()) {
			putNode(TwoInTwo.class);
			assertDistance(4);
			List<Endpoint> endpoints = construction.constructed();
			assertEndpoint(endpoints.get(0), new String[] { "a", "b", "0", "1", "a2", "b2", "2", "3", "a22", "b22" });
			assertEquals(List.of("0", "1", "2", "3"), itemList);
		}
	}

	@Test
	void putsNodeFromAllInTwo() {
		try (MockedConstruction<Endpoint> construction = mockEndpointConstruction()) {
			putNode(ZeroInTwo.class);
			assertDistance(2, 0);
			putNode(OneInTwo.class);
			assertDistance(3, 1);
			putNode(TwoInTwo.class);
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

	private void putNode(Class<? extends RestResource> type) {
		t.putNodesAndEndpoints(type, type.getName(), itemMap);
	}

	private void assertDistance(int expected) {
		assertDistance(expected, 0);
	}

	private void assertDistance(int expected, int actual) {
		assertEquals(expected, distances.get(actual));
	}

	private void assertEndpoint(Endpoint expected, String[] actual) {
		Node node = getNode(actual);
		assertSame(expected, node.getEndpoint("GET"));
	}

	private Node getNode(String[] items) {
		itemList = new ArrayList<>();
		return t.getNodeAndAddItems(items, itemList);
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
