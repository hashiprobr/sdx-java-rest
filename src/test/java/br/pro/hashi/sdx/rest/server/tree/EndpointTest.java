package br.pro.hashi.sdx.rest.server.tree;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.AdditionalMatchers.eq;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import br.pro.hashi.sdx.rest.reflection.Cache;
import br.pro.hashi.sdx.rest.reflection.exception.ReflectionException;
import br.pro.hashi.sdx.rest.server.RestException;
import br.pro.hashi.sdx.rest.server.RestResource;
import br.pro.hashi.sdx.rest.server.exception.BadRequestException;
import br.pro.hashi.sdx.rest.server.tree.Endpoint.DataParameter;
import br.pro.hashi.sdx.rest.server.tree.Endpoint.ItemParameter;
import br.pro.hashi.sdx.rest.server.tree.mock.endpoint.Signatures;
import br.pro.hashi.sdx.rest.transform.Hint;
import br.pro.hashi.sdx.rest.transform.exception.DisassemblingException;
import br.pro.hashi.sdx.rest.transform.facade.exception.SupportException;

class EndpointTest {
	private static final double DELTA = 0.000001;

	private Function<String, Integer> intFunction;
	private Function<String, Double> doubleFunction;
	private Cache cache;
	private Endpoint e;
	private Signatures resource;

	@BeforeEach
	void setUp() {
		intFunction = Integer::parseInt;
		doubleFunction = Double::parseDouble;
		cache = mock(Cache.class);
		when(cache.get(int.class)).thenReturn(intFunction);
		when(cache.get(double.class)).thenReturn(doubleFunction);
		resource = spy(new Signatures());
	}

	@Test
	void constructsWithReturn() {
		e = newEndpoint(0, "withReturn");
		assertEquals(Signatures.class, e.getResourceType());
		assertEquals(boolean.class, e.getReturnType());
		assertNull(e.getVarType());
		assertEquals(0, e.getItemParameters().length);
		assertTrue(e.getPartParameters().isEmpty());
		assertNull(e.getBodyParameter());
		assertArrayEquals(new Object[] {}, e.getArguments());
		assertEquals(0, e.getReach());
	}

	@ParameterizedTest
	@ValueSource(ints = { 1, 2 })
	void doesNotConstructWithReturn(int distance) {
		assertThrows(ReflectionException.class, () -> {
			newEndpoint(distance, "withReturn");
		});
	}

	@Test
	void constructsWithNothing() {
		e = newEndpoint(0, "withNothing");
		assertEquals(Signatures.class, e.getResourceType());
		assertEquals(void.class, e.getReturnType());
		assertNull(e.getVarType());
		assertEquals(0, e.getItemParameters().length);
		assertTrue(e.getPartParameters().isEmpty());
		assertNull(e.getBodyParameter());
		assertArrayEquals(new Object[] {}, e.getArguments());
		assertEquals(0, e.getReach());
	}

	@ParameterizedTest
	@ValueSource(ints = { 1, 2 })
	void doesNotConstructWithNothing(int distance) {
		assertThrows(ReflectionException.class, () -> {
			newEndpoint(distance, "withNothing");
		});
	}

	@Test
	void constructsWithVarArgs() {
		e = newEndpoint(0, "withVarArgs", int[].class);
		assertEquals(Signatures.class, e.getResourceType());
		assertEquals(void.class, e.getReturnType());
		assertEquals(int.class, e.getVarType());
		ItemParameter[] itemParameters = e.getItemParameters();
		assertEquals(1, itemParameters.length);
		assertItem(0, intFunction, "arg0", itemParameters[0]);
		assertTrue(e.getPartParameters().isEmpty());
		assertNull(e.getBodyParameter());
		assertArrayEquals(new Object[] { null }, e.getArguments());
		assertEquals(0, e.getReach());
	}

	@ParameterizedTest
	@ValueSource(ints = { 1, 2 })
	void doesNotConstructWithVarArgs(int distance) {
		assertThrows(ReflectionException.class, () -> {
			newEndpoint(distance, "withVarArgs", int[].class);
		});
	}

	@ParameterizedTest
	@ValueSource(ints = { 0, 1 })
	void constructsWithOneItem(int distance) {
		e = newEndpoint(distance, "withOneItem", int.class);
		assertEquals(Signatures.class, e.getResourceType());
		assertEquals(void.class, e.getReturnType());
		assertNull(e.getVarType());
		ItemParameter[] itemParameters = e.getItemParameters();
		assertEquals(1, itemParameters.length);
		assertItem(0, intFunction, "arg0", itemParameters[0]);
		assertTrue(e.getPartParameters().isEmpty());
		assertNull(e.getBodyParameter());
		assertArrayEquals(new Object[] { null }, e.getArguments());
		assertEquals(1 - distance, e.getReach());
	}

	@Test
	void doesNotConstructWithOneItem() {
		assertThrows(ReflectionException.class, () -> {
			newEndpoint(2, "withOneItem", int.class);
		});
	}

	@ParameterizedTest
	@ValueSource(ints = { 0, 1 })
	void constructsWithOneItemAndVarArgs(int distance) {
		e = newEndpoint(distance, "withOneItemAndVarArgs", int.class, double[].class);
		assertEquals(Signatures.class, e.getResourceType());
		assertEquals(void.class, e.getReturnType());
		assertEquals(double.class, e.getVarType());
		ItemParameter[] itemParameters = e.getItemParameters();
		assertEquals(2, itemParameters.length);
		assertItem(0, intFunction, "arg0", itemParameters[0]);
		assertItem(1, doubleFunction, "arg1", itemParameters[1]);
		assertTrue(e.getPartParameters().isEmpty());
		assertNull(e.getBodyParameter());
		assertArrayEquals(new Object[] { null, null }, e.getArguments());
		assertEquals(1 - distance, e.getReach());
	}

	@Test
	void doesNotConstructWithOneItemAndVarArgs() {
		assertThrows(ReflectionException.class, () -> {
			newEndpoint(2, "withOneItemAndVarArgs", int.class, double[].class);
		});
	}

	@ParameterizedTest
	@ValueSource(ints = { 0, 1, 2 })
	void constructsWithTwoItems(int distance) {
		e = newEndpoint(distance, "withTwoItems", int.class, double.class);
		assertEquals(Signatures.class, e.getResourceType());
		assertEquals(void.class, e.getReturnType());
		assertNull(e.getVarType());
		ItemParameter[] itemParameters = e.getItemParameters();
		assertEquals(2, itemParameters.length);
		assertItem(0, intFunction, "arg0", itemParameters[0]);
		assertItem(1, doubleFunction, "arg1", itemParameters[1]);
		assertTrue(e.getPartParameters().isEmpty());
		assertNull(e.getBodyParameter());
		assertArrayEquals(new Object[] { null, null }, e.getArguments());
		assertEquals(2 - distance, e.getReach());
	}

	@ParameterizedTest
	@ValueSource(ints = { 0, 1 })
	void constructsWithOneItemAndOnePart(int distance) {
		e = newEndpoint(distance, "withOneItemAndOnePart", int.class, Object.class);
		assertEquals(Signatures.class, e.getResourceType());
		assertEquals(void.class, e.getReturnType());
		assertNull(e.getVarType());
		ItemParameter[] itemParameters = e.getItemParameters();
		assertEquals(1, itemParameters.length);
		assertItem(0, intFunction, "arg0", itemParameters[0]);
		Map<String, DataParameter[]> partParameters = e.getPartParameters();
		assertEquals(1, partParameters.size());
		DataParameter[] partArray = partParameters.get("name");
		assertEquals(1, partArray.length);
		assertData(1, Object.class, partArray[0]);
		assertNull(e.getBodyParameter());
		assertArrayEquals(new Object[] { null, null }, e.getArguments());
		assertEquals(1 - distance, e.getReach());
	}

	@Test
	void doesNotConstructWithOneItemAndOnePart() {
		assertThrows(ReflectionException.class, () -> {
			newEndpoint(2, "withOneItemAndOnePart", int.class, Object.class);
		});
	}

	@ParameterizedTest
	@ValueSource(ints = { 0, 1 })
	void constructsWithOneItemAndOneBody(int distance) {
		e = newEndpoint(distance, "withOneItemAndOneBody", int.class, Object.class);
		assertEquals(Signatures.class, e.getResourceType());
		assertEquals(void.class, e.getReturnType());
		assertNull(e.getVarType());
		ItemParameter[] itemParameters = e.getItemParameters();
		assertEquals(1, itemParameters.length);
		assertItem(0, intFunction, "arg0", itemParameters[0]);
		assertTrue(e.getPartParameters().isEmpty());
		assertData(1, Object.class, e.getBodyParameter());
		assertArrayEquals(new Object[] { null, null }, e.getArguments());
		assertEquals(1 - distance, e.getReach());
	}

	@Test
	void doesNotConstructWithOneItemAndOneBody() {
		assertThrows(ReflectionException.class, () -> {
			newEndpoint(2, "withOneItemAndOneBody", int.class, Object.class);
		});
	}

	@Test
	void constructsWithOnePart() {
		e = newEndpoint(0, "withOnePart", Object.class);
		assertEquals(Signatures.class, e.getResourceType());
		assertEquals(void.class, e.getReturnType());
		assertNull(e.getVarType());
		assertEquals(0, e.getItemParameters().length);
		Map<String, DataParameter[]> partParameters = e.getPartParameters();
		assertEquals(1, partParameters.size());
		DataParameter[] partArray = partParameters.get("name");
		assertEquals(1, partArray.length);
		assertData(0, Object.class, partArray[0]);
		assertNull(e.getBodyParameter());
		assertArrayEquals(new Object[] { null }, e.getArguments());
		assertEquals(0, e.getReach());
	}

	@ParameterizedTest
	@ValueSource(ints = { 1, 2 })
	void doesNotConstructWithOnePart(int distance) {
		assertThrows(ReflectionException.class, () -> {
			newEndpoint(distance, "withOnePart", Object.class);
		});
	}

	@Test
	void constructsWithOnePartAndVarArgs() {
		e = newEndpoint(0, "withOnePartAndVarArgs", Object.class, int[].class);
		assertEquals(Signatures.class, e.getResourceType());
		assertEquals(void.class, e.getReturnType());
		assertEquals(int.class, e.getVarType());
		ItemParameter[] itemParameters = e.getItemParameters();
		assertEquals(1, itemParameters.length);
		assertItem(1, intFunction, "arg1", itemParameters[0]);
		Map<String, DataParameter[]> partParameters = e.getPartParameters();
		assertEquals(1, partParameters.size());
		DataParameter[] partArray = partParameters.get("name");
		assertEquals(1, partArray.length);
		assertData(0, Object.class, partArray[0]);
		assertNull(e.getBodyParameter());
		assertArrayEquals(new Object[] { null, null }, e.getArguments());
		assertEquals(0, e.getReach());
	}

	@ParameterizedTest
	@ValueSource(ints = { 1, 2 })
	void doesNotConstructWithOnePartAndVarArgs(int distance) {
		assertThrows(ReflectionException.class, () -> {
			newEndpoint(distance, "withOnePartAndVarArgs", Object.class, int[].class);
		});
	}

	@ParameterizedTest
	@ValueSource(ints = { 0, 1 })
	void constructsWithOnePartAndOneItem(int distance) {
		e = newEndpoint(distance, "withOnePartAndOneItem", Object.class, int.class);
		assertEquals(Signatures.class, e.getResourceType());
		assertEquals(void.class, e.getReturnType());
		assertNull(e.getVarType());
		ItemParameter[] itemParameters = e.getItemParameters();
		assertEquals(1, itemParameters.length);
		assertItem(1, intFunction, "arg1", itemParameters[0]);
		Map<String, DataParameter[]> partParameters = e.getPartParameters();
		assertEquals(1, partParameters.size());
		DataParameter[] partArray = partParameters.get("name");
		assertEquals(1, partArray.length);
		assertData(0, Object.class, partArray[0]);
		assertNull(e.getBodyParameter());
		assertArrayEquals(new Object[] { null, null }, e.getArguments());
		assertEquals(1 - distance, e.getReach());
	}

	@Test
	void doesNotConstructWithOnePartAndOneItem() {
		assertThrows(ReflectionException.class, () -> {
			newEndpoint(2, "withOnePartAndOneItem", Object.class, int.class);
		});
	}

	@Test
	void constructsWithTwoPartsAndOneName() {
		e = newEndpoint(0, "withTwoPartsAndOneName", Object.class, String.class);
		assertEquals(Signatures.class, e.getResourceType());
		assertEquals(void.class, e.getReturnType());
		assertNull(e.getVarType());
		assertEquals(0, e.getItemParameters().length);
		Map<String, DataParameter[]> partParameters = e.getPartParameters();
		assertEquals(1, partParameters.size());
		DataParameter[] partArray = partParameters.get("name");
		assertEquals(2, partArray.length);
		assertData(0, Object.class, partArray[0]);
		assertData(1, String.class, partArray[1]);
		assertNull(e.getBodyParameter());
		assertArrayEquals(new Object[] { null, null }, e.getArguments());
		assertEquals(0, e.getReach());
	}

	@ParameterizedTest
	@ValueSource(ints = { 1, 2 })
	void doesNotConstructWithTwoPartsAndOneName(int distance) {
		assertThrows(ReflectionException.class, () -> {
			newEndpoint(distance, "withTwoPartsAndOneName", Object.class, String.class);
		});
	}

	@Test
	void constructsWithTwoPartsAndTwoNames() {
		e = newEndpoint(0, "withTwoPartsAndTwoNames", Object.class, String.class);
		assertEquals(Signatures.class, e.getResourceType());
		assertEquals(void.class, e.getReturnType());
		assertNull(e.getVarType());
		assertEquals(0, e.getItemParameters().length);
		Map<String, DataParameter[]> partParameters = e.getPartParameters();
		assertEquals(2, partParameters.size());
		DataParameter[] partArray = partParameters.get("name0");
		assertEquals(1, partArray.length);
		assertData(0, Object.class, partArray[0]);
		partArray = partParameters.get("name1");
		assertEquals(1, partArray.length);
		assertData(1, String.class, partArray[0]);
		assertNull(e.getBodyParameter());
		assertArrayEquals(new Object[] { null, null }, e.getArguments());
		assertEquals(0, e.getReach());
	}

	@ParameterizedTest
	@ValueSource(ints = { 1, 2 })
	void doesNotConstructWithTwoPartsAndTwoNames(int distance) {
		assertThrows(ReflectionException.class, () -> {
			newEndpoint(distance, "withTwoPartsAndTwoNames", Object.class, String.class);
		});
	}

	@ParameterizedTest
	@ValueSource(ints = { 0, 1, 2 })
	void doesNotConstructWithOnePartAndOneBody(int distance) {
		assertThrows(ReflectionException.class, () -> {
			newEndpoint(distance, "withOnePartAndOneBody", Object.class, String.class);
		});
	}

	@Test
	void constructsWithOneBody() {
		e = newEndpoint(0, "withOneBody", Object.class);
		assertEquals(Signatures.class, e.getResourceType());
		assertEquals(void.class, e.getReturnType());
		assertNull(e.getVarType());
		assertEquals(0, e.getItemParameters().length);
		assertTrue(e.getPartParameters().isEmpty());
		assertData(0, Object.class, e.getBodyParameter());
		assertArrayEquals(new Object[] { null }, e.getArguments());
		assertEquals(0, e.getReach());
	}

	@ParameterizedTest
	@ValueSource(ints = { 1, 2 })
	void doesNotConstructWithOneBody(int distance) {
		assertThrows(ReflectionException.class, () -> {
			newEndpoint(distance, "withOneBody", Object.class);
		});
	}

	@Test
	void constructsWithOneBodyAndVarArgs() {
		e = newEndpoint(0, "withOneBodyAndVarArgs", Object.class, int[].class);
		assertEquals(Signatures.class, e.getResourceType());
		assertEquals(void.class, e.getReturnType());
		assertEquals(int.class, e.getVarType());
		ItemParameter[] itemParameters = e.getItemParameters();
		assertEquals(1, itemParameters.length);
		assertItem(1, intFunction, "arg1", itemParameters[0]);
		assertTrue(e.getPartParameters().isEmpty());
		assertData(0, Object.class, e.getBodyParameter());
		assertArrayEquals(new Object[] { null, null }, e.getArguments());
		assertEquals(0, e.getReach());
	}

	@ParameterizedTest
	@ValueSource(ints = { 1, 2 })
	void doesNotConstructWithOneBodyAndVarArgs(int distance) {
		assertThrows(ReflectionException.class, () -> {
			newEndpoint(distance, "withOneBodyAndVarArgs", Object.class, int[].class);
		});
	}

	@ParameterizedTest
	@ValueSource(ints = { 0, 1 })
	void constructsWithOneBodyAndOneItem(int distance) {
		e = newEndpoint(distance, "withOneBodyAndOneItem", Object.class, int.class);
		assertEquals(Signatures.class, e.getResourceType());
		assertEquals(void.class, e.getReturnType());
		assertNull(e.getVarType());
		ItemParameter[] itemParameters = e.getItemParameters();
		assertEquals(1, itemParameters.length);
		assertItem(1, intFunction, "arg1", itemParameters[0]);
		assertTrue(e.getPartParameters().isEmpty());
		assertData(0, Object.class, e.getBodyParameter());
		assertArrayEquals(new Object[] { null, null }, e.getArguments());
		assertEquals(1 - distance, e.getReach());
	}

	@Test
	void doesNotConstructWithOneBodyAndOneItem() {
		assertThrows(ReflectionException.class, () -> {
			newEndpoint(2, "withOneBodyAndOneItem", Object.class, int.class);
		});
	}

	@ParameterizedTest
	@ValueSource(ints = { 0, 1, 2 })
	void doesNotConstructWithOneBodyAndOnePart(int distance) {
		assertThrows(ReflectionException.class, () -> {
			newEndpoint(distance, "withOneBodyAndOnePart", Object.class, String.class);
		});
	}

	@ParameterizedTest
	@ValueSource(ints = { 0, 1, 2 })
	void doesNotConstructWithTwoBodies(int distance) {
		assertThrows(ReflectionException.class, () -> {
			newEndpoint(distance, "withTwoBodies", Object.class, String.class);
		});
	}

	@ParameterizedTest
	@ValueSource(ints = { 0, 1, 2 })
	void constructsWithEverythingAndTwoParts(int distance) {
		e = newEndpoint(distance, "withEverythingAndTwoParts", int.class, Object.class, double.class, String.class);
		assertEquals(Signatures.class, e.getResourceType());
		assertEquals(boolean.class, e.getReturnType());
		assertNull(e.getVarType());
		ItemParameter[] itemParameters = e.getItemParameters();
		assertEquals(2, itemParameters.length);
		assertItem(0, intFunction, "arg0", itemParameters[0]);
		assertItem(2, doubleFunction, "arg2", itemParameters[1]);
		Map<String, DataParameter[]> partParameters = e.getPartParameters();
		assertEquals(2, partParameters.size());
		DataParameter[] partArray = partParameters.get("name0");
		assertEquals(1, partArray.length);
		assertData(1, Object.class, partArray[0]);
		partArray = partParameters.get("name1");
		assertEquals(1, partArray.length);
		assertData(3, String.class, partArray[0]);
		assertNull(e.getBodyParameter());
		assertArrayEquals(new Object[] { null, null, null, null }, e.getArguments());
		assertEquals(2 - distance, e.getReach());
	}

	@ParameterizedTest
	@ValueSource(ints = { 0, 1 })
	void constructsWithEverythingAndVarArgs(int distance) {
		e = newEndpoint(distance, "withEverythingAndVarArgs", int.class, Object.class, double[].class);
		assertEquals(Signatures.class, e.getResourceType());
		assertEquals(boolean.class, e.getReturnType());
		assertEquals(double.class, e.getVarType());
		ItemParameter[] itemParameters = e.getItemParameters();
		assertEquals(2, itemParameters.length);
		assertItem(0, intFunction, "arg0", itemParameters[0]);
		assertItem(2, doubleFunction, "arg2", itemParameters[1]);
		Map<String, DataParameter[]> partParameters = e.getPartParameters();
		assertEquals(0, partParameters.size());
		assertData(1, Object.class, e.getBodyParameter());
		assertArrayEquals(new Object[] { null, null, null }, e.getArguments());
		assertEquals(1 - distance, e.getReach());
	}

	@Test
	void doesNotConstructWithEverythingAndVarArgs() {
		assertThrows(ReflectionException.class, () -> {
			newEndpoint(2, "withEverythingAndVarArgs", int.class, Object.class, double[].class);
		});
	}

	@ParameterizedTest
	@ValueSource(ints = { 0, 1, 2 })
	void constructsWithEverythingAndOneBody(int distance) {
		e = newEndpoint(distance, "withEverythingAndOneBody", int.class, Object.class, double.class);
		assertEquals(Signatures.class, e.getResourceType());
		assertEquals(boolean.class, e.getReturnType());
		assertNull(e.getVarType());
		ItemParameter[] itemParameters = e.getItemParameters();
		assertEquals(2, itemParameters.length);
		assertItem(0, intFunction, "arg0", itemParameters[0]);
		assertItem(2, doubleFunction, "arg2", itemParameters[1]);
		Map<String, DataParameter[]> partParameters = e.getPartParameters();
		assertEquals(0, partParameters.size());
		assertData(1, Object.class, e.getBodyParameter());
		assertArrayEquals(new Object[] { null, null, null }, e.getArguments());
		assertEquals(2 - distance, e.getReach());
	}

	@ParameterizedTest
	@ValueSource(ints = { 0, 1, 2 })
	void doesNotConstructWithOnePartAndBody(int distance) {
		assertThrows(ReflectionException.class, () -> {
			newEndpoint(distance, "withOnePartAndBody", Object.class);
		});
	}

	@ParameterizedTest
	@ValueSource(ints = { 0, 1, 2 })
	void doesNotConstructWithUnderscore(int distance) {
		assertThrows(ReflectionException.class, () -> {
			newEndpoint(distance, "with_Underscore");
		});
	}

	@Test
	void constructsWithGenericReturn() {
		e = newEndpoint(0, "withGenericReturn");
		assertEquals(Signatures.class, e.getResourceType());
		assertEquals(new Hint<List<Boolean>>() {}.getType(), e.getReturnType());
		assertNull(e.getVarType());
		assertEquals(0, e.getItemParameters().length);
		assertTrue(e.getPartParameters().isEmpty());
		assertArrayEquals(new Object[] {}, e.getArguments());
		assertEquals(0, e.getReach());
	}

	@ParameterizedTest
	@ValueSource(ints = { 1, 2 })
	void doesNotConstructWithGenericReturn(int distance) {
		assertThrows(ReflectionException.class, () -> {
			newEndpoint(distance, "withGenericReturn");
		});
	}

	@ParameterizedTest
	@ValueSource(ints = { 0, 1, 2 })
	void doesNotConstructWithGenericItem(int distance) {
		assertThrows(ReflectionException.class, () -> {
			newEndpoint(distance, "withGenericItem", List.class);
		});
	}

	@Test
	void constructsWithGenericPart() {
		e = newEndpoint(0, "withGenericPart", List.class);
		assertEquals(Signatures.class, e.getResourceType());
		assertEquals(void.class, e.getReturnType());
		assertNull(e.getVarType());
		assertEquals(0, e.getItemParameters().length);
		Map<String, DataParameter[]> partParameters = e.getPartParameters();
		assertEquals(1, partParameters.size());
		DataParameter[] partArray = partParameters.get("name");
		assertEquals(1, partArray.length);
		assertData(0, new Hint<List<Object>>() {}.getType(), partArray[0]);
		assertNull(e.getBodyParameter());
		assertArrayEquals(new Object[] { null }, e.getArguments());
		assertEquals(0, e.getReach());
	}

	@ParameterizedTest
	@ValueSource(ints = { 1, 2 })
	void doesNotConstructWithGenericPart(int distance) {
		assertThrows(ReflectionException.class, () -> {
			newEndpoint(distance, "withGenericPart", List.class);
		});
	}

	@Test
	void constructsWithGenericBody() {
		e = newEndpoint(0, "withGenericBody", List.class);
		assertEquals(Signatures.class, e.getResourceType());
		assertEquals(void.class, e.getReturnType());
		assertNull(e.getVarType());
		assertEquals(0, e.getItemParameters().length);
		assertTrue(e.getPartParameters().isEmpty());
		assertData(0, new Hint<List<Object>>() {}.getType(), e.getBodyParameter());
		assertArrayEquals(new Object[] { null }, e.getArguments());
		assertEquals(0, e.getReach());
	}

	@ParameterizedTest
	@ValueSource(ints = { 1, 2 })
	void doesNotConstructWithGenericBody(int distance) {
		assertThrows(ReflectionException.class, () -> {
			newEndpoint(distance, "withGenericBody", List.class);
		});
	}

	@Test
	void constructsWithUncheckedException() {
		e = newEndpoint(0, "withUncheckedException");
		assertEquals(Signatures.class, e.getResourceType());
		assertEquals(void.class, e.getReturnType());
		assertNull(e.getVarType());
		assertEquals(0, e.getItemParameters().length);
		assertTrue(e.getPartParameters().isEmpty());
		assertNull(e.getBodyParameter());
		assertArrayEquals(new Object[] {}, e.getArguments());
		assertEquals(0, e.getReach());
	}

	@ParameterizedTest
	@ValueSource(ints = { 1, 2 })
	void doesNotConstructWithUncheckedException(int distance) {
		assertThrows(ReflectionException.class, () -> {
			newEndpoint(distance, "withUncheckedException");
		});
	}

	@ParameterizedTest
	@ValueSource(ints = { 0, 1, 2 })
	void doesNotConstructWithCheckedException(int distance) {
		assertThrows(ReflectionException.class, () -> {
			newEndpoint(distance, "withCheckedException");
		});
	}

	private void assertItem(int index, Function<String, ?> function, String name, ItemParameter parameter) {
		assertEquals(index, parameter.index());
		assertSame(function, parameter.function());
		assertEquals(name, parameter.name());
	}

	private void assertData(int index, Type type, DataParameter parameter) {
		assertEquals(index, parameter.index());
		assertEquals(type, parameter.type());
	}

	@Test
	void callsWithReturn() {
		e = newEndpoint(0, "withReturn");
		assertTrue((boolean) e.call(resource, List.of(), Map.of(), null));
		assertArrayEquals(new Object[] {}, e.getArguments());
		verify(resource).withReturn();
	}

	@Test
	void callsWithNothing() {
		e = newEndpoint(0, "withNothing");
		assertNull(e.call(resource, List.of(), Map.of(), null));
		assertArrayEquals(new Object[] {}, e.getArguments());
		verify(resource).withNothing();
	}

	@Test
	void callsWithNothingAndOneExtraBody() {
		e = newEndpoint(0, "withNothing");
		Object body = new Object();
		assertNull(e.call(resource, List.of(), Map.of(), mockData(body)));
		assertArrayEquals(new Object[] {}, e.getArguments());
		verify(resource).withNothing();
	}

	@Test
	void callsWithZeroVarArgs() {
		e = newEndpoint(0, "withVarArgs", int[].class);
		assertNull(e.call(resource, List.of(), Map.of(), null));
		assertArrayEquals(new Object[] { null }, e.getArguments());
		verify(resource).withVarArgs();
	}

	@Test
	void callsWithOneVarArg() {
		e = newEndpoint(0, "withVarArgs", int[].class);
		assertNull(e.call(resource, List.of("1"), Map.of(), null));
		assertArrayEquals(new Object[] { null }, e.getArguments());
		verify(resource).withVarArgs(1);
	}

	@Test
	void callsWithTwoVarArgs() {
		e = newEndpoint(0, "withVarArgs", int[].class);
		assertNull(e.call(resource, List.of("1", "2"), Map.of(), null));
		assertArrayEquals(new Object[] { null }, e.getArguments());
		verify(resource).withVarArgs(1, 2);
	}

	@Test
	void callsWithVarArgsAndOneExtraBody() {
		e = newEndpoint(0, "withVarArgs", int[].class);
		Object body = new Object();
		assertNull(e.call(resource, List.of("1", "2"), Map.of(), mockData(body)));
		assertArrayEquals(new Object[] { null }, e.getArguments());
		verify(resource).withVarArgs(1, 2);
	}

	@Test
	void doesNotCallWithInvalidVarArgs() {
		e = newEndpoint(0, "withVarArgs", int[].class);
		assertThrows(BadRequestException.class, () -> {
			e.call(resource, List.of("1", "s"), Map.of(), null);
		});
		assertArrayEquals(new Object[] { null }, e.getArguments());
	}

	@Test
	void callsWithOneItem() {
		e = newEndpoint(0, "withOneItem", int.class);
		assertNull(e.call(resource, List.of("1"), Map.of(), null));
		assertArrayEquals(new Object[] { null }, e.getArguments());
		verify(resource).withOneItem(1);
	}

	@Test
	void callsWithOneItemAndOneExtraBody() {
		e = newEndpoint(0, "withOneItem", int.class);
		Object body = new Object();
		assertNull(e.call(resource, List.of("1"), Map.of(), mockData(body)));
		assertArrayEquals(new Object[] { null }, e.getArguments());
		verify(resource).withOneItem(1);
	}

	@Test
	void doesNotCallWithOneInvalidItem() {
		e = newEndpoint(0, "withOneItem", int.class);
		assertThrows(BadRequestException.class, () -> {
			e.call(resource, List.of("s"), Map.of(), null);
		});
		assertArrayEquals(new Object[] { null }, e.getArguments());
	}

	@Test
	void callsWithOneItemAndZeroVarArgs() {
		e = newEndpoint(0, "withOneItemAndVarArgs", int.class, double[].class);
		doAnswer((invocation) -> {
			return null;
		}).when(resource).withOneItemAndVarArgs(eq(1), any(double[].class));
		assertNull(e.call(resource, List.of("1"), Map.of(), null));
		assertArrayEquals(new Object[] { null, null }, e.getArguments());
		verify(resource).withOneItemAndVarArgs(eq(1));
	}

	@Test
	void callsWithOneItemAndOneVarArg() {
		e = newEndpoint(0, "withOneItemAndVarArgs", int.class, double[].class);
		doAnswer((invocation) -> {
			assertEquals(2.3, invocation.getArgument(1), DELTA);
			return null;
		}).when(resource).withOneItemAndVarArgs(eq(1), any(double[].class));
		assertNull(e.call(resource, List.of("1", "2.3"), Map.of(), null));
		assertArrayEquals(new Object[] { null, null }, e.getArguments());
		verify(resource).withOneItemAndVarArgs(eq(1), any(double[].class));
	}

	@Test
	void callsWithOneItemAndTwoVarArgs() {
		e = newEndpoint(0, "withOneItemAndVarArgs", int.class, double[].class);
		doAnswer((invocation) -> {
			assertEquals(2.3, invocation.getArgument(1), DELTA);
			assertEquals(4.5, invocation.getArgument(2), DELTA);
			return null;
		}).when(resource).withOneItemAndVarArgs(eq(1), any(double[].class));
		assertNull(e.call(resource, List.of("1", "2.3", "4.5"), Map.of(), null));
		assertArrayEquals(new Object[] { null, null }, e.getArguments());
		verify(resource).withOneItemAndVarArgs(eq(1), any(double[].class));
	}

	@Test
	void doesNotCallWithOneInvalidItemAndVarArgs() {
		e = newEndpoint(0, "withOneItemAndVarArgs", int.class, double[].class);
		assertThrows(BadRequestException.class, () -> {
			e.call(resource, List.of("s", "2.3", "4.5"), Map.of(), null);
		});
		assertArrayEquals(new Object[] { null, null }, e.getArguments());
	}

	@Test
	void doesNotCallWithOneItemAndInvalidVarArgs() {
		e = newEndpoint(0, "withOneItemAndVarArgs", int.class, double[].class);
		assertThrows(BadRequestException.class, () -> {
			e.call(resource, List.of("1", "2.3", "s"), Map.of(), null);
		});
		assertArrayEquals(new Object[] { null, null }, e.getArguments());
	}

	@Test
	void callsWithTwoItems() {
		e = newEndpoint(0, "withTwoItems", int.class, double.class);
		assertNull(e.call(resource, List.of("1", "2.3"), Map.of(), null));
		assertArrayEquals(new Object[] { null, null }, e.getArguments());
		verify(resource).withTwoItems(eq(1), eq(2.3, DELTA));
	}

	@Test
	void callsWithTwoItemsAndOneExtraBody() {
		e = newEndpoint(0, "withTwoItems", int.class, double.class);
		Object body = new Object();
		assertNull(e.call(resource, List.of("1", "2.3"), Map.of(), mockData(body)));
		assertArrayEquals(new Object[] { null, null }, e.getArguments());
		verify(resource).withTwoItems(eq(1), eq(2.3, DELTA));
	}

	@Test
	void doesNotCallWithOneItemAndOneInvalidItem() {
		e = newEndpoint(0, "withTwoItems", int.class, double.class);
		assertThrows(BadRequestException.class, () -> {
			e.call(resource, List.of("1", "s"), Map.of(), null);
		});
		assertArrayEquals(new Object[] { null, null }, e.getArguments());
	}

	@Test
	void doesNotCallWithOneInvalidItemAndOneItem() {
		e = newEndpoint(0, "withTwoItems", int.class, double.class);
		assertThrows(BadRequestException.class, () -> {
			e.call(resource, List.of("s", "2.3"), Map.of(), null);
		});
		assertArrayEquals(new Object[] { null, null }, e.getArguments());
	}

	@Test
	void callsWithOneItemAndOnePart() {
		e = newEndpoint(0, "withOneItemAndOnePart", int.class, Object.class);
		Object body = new Object();
		assertNull(e.call(resource, List.of("1"), Map.of("name", List.of(mockData(body))), null));
		assertArrayEquals(new Object[] { null, null }, e.getArguments());
		verify(resource).withOneItemAndOnePart(1, body);
	}

	@Test
	void doesNotCallWithOneInvalidItemAndOnePart() {
		e = newEndpoint(0, "withOneItemAndOnePart", int.class, Object.class);
		Object body = new Object();
		assertThrows(BadRequestException.class, () -> {
			e.call(resource, List.of("s"), Map.of("name", List.of(mockData(body))), null);
		});
		assertArrayEquals(new Object[] { null, null }, e.getArguments());
	}

	@Test
	void doesNotCallWithOneItemAndOneExtraPart() {
		e = newEndpoint(0, "withOneItemAndOnePart", int.class, Object.class);
		Object body = new Object();
		assertThrows(BadRequestException.class, () -> {
			e.call(resource, List.of("1"), Map.of("name", List.of(mockData(body), mockStringData())), null);
		});
		assertArrayEquals(new Object[] { null, null }, e.getArguments());
	}

	@Test
	void doesNotCallWithOneItemAndOneUnsupportedPart() {
		e = newEndpoint(0, "withOneItemAndOnePart", int.class, Object.class);
		RestException exception = assertThrows(RestException.class, () -> {
			e.call(resource, List.of("1"), Map.of("name", List.of(mockUnsupportedData())), null);
		});
		assertEquals(415, exception.getStatus());
		assertArrayEquals(new Object[] { null, null }, e.getArguments());
	}

	@Test
	void doesNotCallWithOneItemAndOneInvalidPart() {
		e = newEndpoint(0, "withOneItemAndOnePart", int.class, Object.class);
		assertThrows(BadRequestException.class, () -> {
			e.call(resource, List.of("1"), Map.of("name", List.of(mockInvalidData())), null);
		});
		assertArrayEquals(new Object[] { null, null }, e.getArguments());
	}

	@Test
	void doesNotCallWithOneItemAndOneReplacedPart() {
		e = newEndpoint(0, "withOneItemAndOnePart", int.class, Object.class);
		Object body = new Object();
		assertThrows(BadRequestException.class, () -> {
			e.call(resource, List.of("1"), Map.of(), mockData(body));
		});
		assertArrayEquals(new Object[] { null, null }, e.getArguments());
	}

	@Test
	void doesNotCallWithOneItemAndOneMissingPart() {
		e = newEndpoint(0, "withOneItemAndOnePart", int.class, Object.class);
		assertThrows(BadRequestException.class, () -> {
			e.call(resource, List.of("1"), Map.of(), null);
		});
		assertArrayEquals(new Object[] { null, null }, e.getArguments());
	}

	@Test
	void callsWithOneItemAndOneBody() {
		e = newEndpoint(0, "withOneItemAndOneBody", int.class, Object.class);
		Object body = new Object();
		assertNull(e.call(resource, List.of("1"), Map.of(), mockData(body)));
		assertArrayEquals(new Object[] { null, null }, e.getArguments());
		verify(resource).withOneItemAndOneBody(1, body);
	}

	@Test
	void doesNotCallWithOneInvalidItemAndOneBody() {
		e = newEndpoint(0, "withOneItemAndOneBody", int.class, Object.class);
		Object body = new Object();
		assertThrows(BadRequestException.class, () -> {
			e.call(resource, List.of("s"), Map.of(), mockData(body));
		});
		assertArrayEquals(new Object[] { null, null }, e.getArguments());
	}

	@Test
	void doesNotCallWithOneItemAndOneUnsupportedBody() {
		e = newEndpoint(0, "withOneItemAndOneBody", int.class, Object.class);
		RestException exception = assertThrows(RestException.class, () -> {
			e.call(resource, List.of("1"), Map.of(), mockUnsupportedData());
		});
		assertEquals(415, exception.getStatus());
		assertArrayEquals(new Object[] { null, null }, e.getArguments());
	}

	@Test
	void doesNotCallWithOneItemAndOneInvalidBody() {
		e = newEndpoint(0, "withOneItemAndOneBody", int.class, Object.class);
		assertThrows(BadRequestException.class, () -> {
			e.call(resource, List.of("1"), Map.of(), mockInvalidData());
		});
		assertArrayEquals(new Object[] { null, null }, e.getArguments());
	}

	@Test
	void doesNotCallWithOneItemAndOneReplacedBody() {
		e = newEndpoint(0, "withOneItemAndOneBody", int.class, Object.class);
		Object body = new Object();
		assertThrows(BadRequestException.class, () -> {
			e.call(resource, List.of("1"), Map.of("name", List.of(mockData(body))), null);
		});
		assertArrayEquals(new Object[] { null, null }, e.getArguments());
	}

	@Test
	void doesNotCallWithOneItemAndOneMissingBody() {
		e = newEndpoint(0, "withOneItemAndOneBody", int.class, Object.class);
		assertThrows(BadRequestException.class, () -> {
			e.call(resource, List.of("1"), Map.of(), null);
		});
		assertArrayEquals(new Object[] { null, null }, e.getArguments());
	}

	@Test
	void callsWithOnePart() {
		e = newEndpoint(0, "withOnePart", Object.class);
		Object body = new Object();
		assertNull(e.call(resource, List.of(), Map.of("name", List.of(mockData(body))), null));
		assertArrayEquals(new Object[] { null }, e.getArguments());
		verify(resource).withOnePart(body);
	}

	@Test
	void doesNotCallWithOneExtraPart() {
		e = newEndpoint(0, "withOnePart", Object.class);
		Object body = new Object();
		assertThrows(BadRequestException.class, () -> {
			e.call(resource, List.of(), Map.of("name", List.of(mockData(body), mockStringData())), null);
		});
		assertArrayEquals(new Object[] { null }, e.getArguments());
	}

	@Test
	void doesNotCallWithOneUnsupportedPart() {
		e = newEndpoint(0, "withOnePart", Object.class);
		RestException exception = assertThrows(RestException.class, () -> {
			e.call(resource, List.of(), Map.of("name", List.of(mockUnsupportedData())), null);
		});
		assertEquals(415, exception.getStatus());
		assertArrayEquals(new Object[] { null }, e.getArguments());
	}

	@Test
	void doesNotCallWithOneInvalidPart() {
		e = newEndpoint(0, "withOnePart", Object.class);
		assertThrows(BadRequestException.class, () -> {
			e.call(resource, List.of(), Map.of("name", List.of(mockInvalidData())), null);
		});
		assertArrayEquals(new Object[] { null }, e.getArguments());
	}

	@Test
	void doesNotCallWithOneReplacedPart() {
		e = newEndpoint(0, "withOnePart", Object.class);
		Object body = new Object();
		assertThrows(BadRequestException.class, () -> {
			e.call(resource, List.of(), Map.of(), mockData(body));
		});
		assertArrayEquals(new Object[] { null }, e.getArguments());
	}

	@Test
	void doesNotCallWithOneMissingPart() {
		e = newEndpoint(0, "withOnePart", Object.class);
		assertThrows(BadRequestException.class, () -> {
			e.call(resource, List.of(), Map.of(), null);
		});
		assertArrayEquals(new Object[] { null }, e.getArguments());
	}

	@Test
	void callsWithOnePartAndZeroVarArgs() {
		e = newEndpoint(0, "withOnePartAndVarArgs", Object.class, int[].class);
		Object body = new Object();
		assertNull(e.call(resource, List.of(), Map.of("name", List.of(mockData(body))), null));
		assertArrayEquals(new Object[] { null, null }, e.getArguments());
		verify(resource).withOnePartAndVarArgs(body);
	}

	@Test
	void callsWithOnePartAndOneVarArg() {
		e = newEndpoint(0, "withOnePartAndVarArgs", Object.class, int[].class);
		Object body = new Object();
		assertNull(e.call(resource, List.of("1"), Map.of("name", List.of(mockData(body))), null));
		assertArrayEquals(new Object[] { null, null }, e.getArguments());
		verify(resource).withOnePartAndVarArgs(body, 1);
	}

	@Test
	void callsWithOnePartAndTwoVarArgs() {
		e = newEndpoint(0, "withOnePartAndVarArgs", Object.class, int[].class);
		Object body = new Object();
		assertNull(e.call(resource, List.of("1", "2"), Map.of("name", List.of(mockData(body))), null));
		assertArrayEquals(new Object[] { null, null }, e.getArguments());
		verify(resource).withOnePartAndVarArgs(body, 1, 2);
	}

	@Test
	void doesNotCallWithOneExtraPartAndVarArgs() {
		e = newEndpoint(0, "withOnePartAndVarArgs", Object.class, int[].class);
		Object body = new Object();
		assertThrows(BadRequestException.class, () -> {
			e.call(resource, List.of("1", "2"), Map.of("name", List.of(mockData(body), mockStringData())), null);
		});
		assertArrayEquals(new Object[] { null, null }, e.getArguments());
	}

	@Test
	void doesNotCallWithOneUnsupportedPartAndVarArgs() {
		e = newEndpoint(0, "withOnePartAndVarArgs", Object.class, int[].class);
		RestException exception = assertThrows(RestException.class, () -> {
			e.call(resource, List.of("1", "2"), Map.of("name", List.of(mockUnsupportedData())), null);
		});
		assertEquals(415, exception.getStatus());
		assertArrayEquals(new Object[] { null, null }, e.getArguments());
	}

	@Test
	void doesNotCallWithOneInvalidPartAndVarArgs() {
		e = newEndpoint(0, "withOnePartAndVarArgs", Object.class, int[].class);
		assertThrows(BadRequestException.class, () -> {
			e.call(resource, List.of("1", "2"), Map.of("name", List.of(mockInvalidData())), null);
		});
		assertArrayEquals(new Object[] { null, null }, e.getArguments());
	}

	@Test
	void doesNotCallWithOneReplacedPartAndVarArgs() {
		e = newEndpoint(0, "withOnePartAndVarArgs", Object.class, int[].class);
		Object body = new Object();
		assertThrows(BadRequestException.class, () -> {
			e.call(resource, List.of("1", "2"), Map.of(), mockData(body));
		});
		assertArrayEquals(new Object[] { null, null }, e.getArguments());
	}

	@Test
	void doesNotCallWithOneMissingPartAndVarArgs() {
		e = newEndpoint(0, "withOnePartAndVarArgs", Object.class, int[].class);
		assertThrows(BadRequestException.class, () -> {
			e.call(resource, List.of("1"), Map.of(), null);
		});
		assertArrayEquals(new Object[] { null, null }, e.getArguments());
	}

	@Test
	void doesNotCallWithOnePartAndInvalidVarArgs() {
		e = newEndpoint(0, "withOnePartAndVarArgs", Object.class, int[].class);
		Object body = new Object();
		assertThrows(BadRequestException.class, () -> {
			e.call(resource, List.of("1", "s"), Map.of("name", List.of(mockData(body))), null);
		});
		assertArrayEquals(new Object[] { null, null }, e.getArguments());
	}

	@Test
	void callsWithOnePartAndOneItem() {
		e = newEndpoint(0, "withOnePartAndOneItem", Object.class, int.class);
		Object body = new Object();
		assertNull(e.call(resource, List.of("1"), Map.of("name", List.of(mockData(body))), null));
		assertArrayEquals(new Object[] { null, null }, e.getArguments());
		verify(resource).withOnePartAndOneItem(body, 1);
	}

	@Test
	void doesNotCallWithOneExtraPartAndOneItem() {
		e = newEndpoint(0, "withOnePartAndOneItem", Object.class, int.class);
		Object body = new Object();
		assertThrows(BadRequestException.class, () -> {
			e.call(resource, List.of("1"), Map.of("name", List.of(mockData(body), mockStringData())), null);
		});
		assertArrayEquals(new Object[] { null, null }, e.getArguments());
	}

	@Test
	void doesNotCallWithOneUnsupportedPartAndOneItem() {
		e = newEndpoint(0, "withOnePartAndOneItem", Object.class, int.class);
		RestException exception = assertThrows(RestException.class, () -> {
			e.call(resource, List.of("1"), Map.of("name", List.of(mockUnsupportedData())), null);
		});
		assertEquals(415, exception.getStatus());
		assertArrayEquals(new Object[] { null, null }, e.getArguments());
	}

	@Test
	void doesNotCallWithOneInvalidPartAndOneItem() {
		e = newEndpoint(0, "withOnePartAndOneItem", Object.class, int.class);
		assertThrows(BadRequestException.class, () -> {
			e.call(resource, List.of("1"), Map.of("name", List.of(mockInvalidData())), null);
		});
		assertArrayEquals(new Object[] { null, null }, e.getArguments());
	}

	@Test
	void doesNotCallWithOneReplacedPartAndOneItem() {
		e = newEndpoint(0, "withOnePartAndOneItem", Object.class, int.class);
		Object body = new Object();
		assertThrows(BadRequestException.class, () -> {
			e.call(resource, List.of("1"), Map.of(), mockData(body));
		});
		assertArrayEquals(new Object[] { null, null }, e.getArguments());
	}

	@Test
	void doesNotCallWithOneMissingPartAndOneItem() {
		e = newEndpoint(0, "withOnePartAndOneItem", Object.class, int.class);
		assertThrows(BadRequestException.class, () -> {
			e.call(resource, List.of("1"), Map.of(), null);
		});
		assertArrayEquals(new Object[] { null, null }, e.getArguments());
	}

	@Test
	void doesNotCallWithOnePartAndOneInvalidItem() {
		e = newEndpoint(0, "withOnePartAndOneItem", Object.class, int.class);
		Object body = new Object();
		assertThrows(BadRequestException.class, () -> {
			e.call(resource, List.of("s"), Map.of("name", List.of(mockData(body))), null);
		});
		assertArrayEquals(new Object[] { null, null }, e.getArguments());
	}

	@Test
	void callsWithTwoPartsAndOneName() {
		e = newEndpoint(0, "withTwoPartsAndOneName", Object.class, String.class);
		Object body = new Object();
		assertNull(e.call(resource, List.of(), Map.of("name", List.of(mockData(body), mockStringData())), null));
		assertArrayEquals(new Object[] { null, null }, e.getArguments());
		verify(resource).withTwoPartsAndOneName(body, "body");
	}

	@Test
	void doesNotCallWithTwoPartsAndOneName() {
		e = newEndpoint(0, "withTwoPartsAndOneName", Object.class, String.class);
		Object body = new Object();
		assertThrows(BadRequestException.class, () -> {
			e.call(resource, List.of(), Map.of("name", List.of(mockData(body))), null);
		});
		assertArrayEquals(new Object[] { null, null }, e.getArguments());
	}

	@Test
	void callsWithTwoPartsAndTwoNames() {
		e = newEndpoint(0, "withTwoPartsAndTwoNames", Object.class, String.class);
		Object body = new Object();
		assertNull(e.call(resource, List.of(), Map.of("name0", List.of(mockData(body)), "name1", List.of(mockStringData())), null));
		assertArrayEquals(new Object[] { null, null }, e.getArguments());
		verify(resource).withTwoPartsAndTwoNames(body, "body");
	}

	@Test
	void doesNotCallWithTwoPartsAndTwoNames() {
		e = newEndpoint(0, "withTwoPartsAndTwoNames", Object.class, String.class);
		Object body = new Object();
		assertThrows(BadRequestException.class, () -> {
			e.call(resource, List.of(), Map.of("name0", List.of(mockData(body))), null);
		});
		assertArrayEquals(new Object[] { null, null }, e.getArguments());
	}

	@Test
	void callsWithOneBody() {
		e = newEndpoint(0, "withOneBody", Object.class);
		Object body = new Object();
		assertNull(e.call(resource, List.of(), Map.of(), mockData(body)));
		assertArrayEquals(new Object[] { null }, e.getArguments());
		verify(resource).withOneBody(body);
	}

	@Test
	void doesNotCallWithOneUnsupportedBody() {
		e = newEndpoint(0, "withOneBody", Object.class);
		RestException exception = assertThrows(RestException.class, () -> {
			e.call(resource, List.of(), Map.of(), mockUnsupportedData());
		});
		assertEquals(415, exception.getStatus());
		assertArrayEquals(new Object[] { null }, e.getArguments());
	}

	@Test
	void doesNotCallWithOneInvalidBody() {
		e = newEndpoint(0, "withOneBody", Object.class);
		assertThrows(BadRequestException.class, () -> {
			e.call(resource, List.of(), Map.of(), mockInvalidData());
		});
		assertArrayEquals(new Object[] { null }, e.getArguments());
	}

	@Test
	void doesNotCallWithOneReplacedBody() {
		e = newEndpoint(0, "withOneBody", Object.class);
		Object body = new Object();
		assertThrows(BadRequestException.class, () -> {
			e.call(resource, List.of(), Map.of("name", List.of(mockData(body))), null);
		});
		assertArrayEquals(new Object[] { null }, e.getArguments());
	}

	@Test
	void doesNotCallWithOneMissingBody() {
		e = newEndpoint(0, "withOneBody", Object.class);
		assertThrows(BadRequestException.class, () -> {
			e.call(resource, List.of(), Map.of(), null);
		});
		assertArrayEquals(new Object[] { null }, e.getArguments());
	}

	@Test
	void callsWithOneBodyAndZeroVarArgs() {
		e = newEndpoint(0, "withOneBodyAndVarArgs", Object.class, int[].class);
		Object body = new Object();
		assertNull(e.call(resource, List.of(), Map.of(), mockData(body)));
		assertArrayEquals(new Object[] { null, null }, e.getArguments());
		verify(resource).withOneBodyAndVarArgs(body);
	}

	@Test
	void callsWithOneBodyAndOneVarArg() {
		e = newEndpoint(0, "withOneBodyAndVarArgs", Object.class, int[].class);
		Object body = new Object();
		assertNull(e.call(resource, List.of("1"), Map.of(), mockData(body)));
		assertArrayEquals(new Object[] { null, null }, e.getArguments());
		verify(resource).withOneBodyAndVarArgs(body, 1);
	}

	@Test
	void callsWithOneBodyAndTwoVarArgs() {
		e = newEndpoint(0, "withOneBodyAndVarArgs", Object.class, int[].class);
		Object body = new Object();
		assertNull(e.call(resource, List.of("1", "2"), Map.of(), mockData(body)));
		assertArrayEquals(new Object[] { null, null }, e.getArguments());
		verify(resource).withOneBodyAndVarArgs(body, 1, 2);
	}

	@Test
	void doesNotCallWithOneUnsupportedBodyAndVarArgs() {
		e = newEndpoint(0, "withOneBodyAndVarArgs", Object.class, int[].class);
		RestException exception = assertThrows(RestException.class, () -> {
			e.call(resource, List.of("1", "2"), Map.of(), mockUnsupportedData());
		});
		assertEquals(415, exception.getStatus());
		assertArrayEquals(new Object[] { null, null }, e.getArguments());
	}

	@Test
	void doesNotCallWithOneInvalidBodyAndVarArgs() {
		e = newEndpoint(0, "withOneBodyAndVarArgs", Object.class, int[].class);
		assertThrows(BadRequestException.class, () -> {
			e.call(resource, List.of("1", "2"), Map.of(), mockInvalidData());
		});
		assertArrayEquals(new Object[] { null, null }, e.getArguments());
	}

	@Test
	void doesNotCallWithOneReplacedBodyAndVarArgs() {
		e = newEndpoint(0, "withOneBodyAndVarArgs", Object.class, int[].class);
		Object body = new Object();
		assertThrows(BadRequestException.class, () -> {
			e.call(resource, List.of("1", "2"), Map.of("name", List.of(mockData(body))), null);
		});
		assertArrayEquals(new Object[] { null, null }, e.getArguments());
	}

	@Test
	void doesNotCallWithOneMissingBodyAndVarArgs() {
		e = newEndpoint(0, "withOneBodyAndVarArgs", Object.class, int[].class);
		assertThrows(BadRequestException.class, () -> {
			e.call(resource, List.of("1", "2"), Map.of(), mockInvalidData());
		});
		assertArrayEquals(new Object[] { null, null }, e.getArguments());
	}

	@Test
	void doesNotCallWithOneBodyAndInvalidVarArgs() {
		e = newEndpoint(0, "withOneBodyAndVarArgs", Object.class, int[].class);
		Object body = new Object();
		assertThrows(BadRequestException.class, () -> {
			e.call(resource, List.of("1", "s"), Map.of(), mockData(body));
		});
		assertArrayEquals(new Object[] { null, null }, e.getArguments());
	}

	@Test
	void callsWithOneBodyAndOneItem() {
		e = newEndpoint(0, "withOneBodyAndOneItem", Object.class, int.class);
		Object body = new Object();
		assertNull(e.call(resource, List.of("1"), Map.of(), mockData(body)));
		assertArrayEquals(new Object[] { null, null }, e.getArguments());
		verify(resource).withOneBodyAndOneItem(body, 1);
	}

	@Test
	void doesNotCallWithOneUnsupportedBodyAndOneItem() {
		e = newEndpoint(0, "withOneBodyAndOneItem", Object.class, int.class);
		RestException exception = assertThrows(RestException.class, () -> {
			e.call(resource, List.of("1"), Map.of(), mockUnsupportedData());
		});
		assertEquals(415, exception.getStatus());
		assertArrayEquals(new Object[] { null, null }, e.getArguments());
	}

	@Test
	void doesNotCallWithOneInvalidBodyAndOneItem() {
		e = newEndpoint(0, "withOneBodyAndOneItem", Object.class, int.class);
		assertThrows(BadRequestException.class, () -> {
			e.call(resource, List.of("1"), Map.of(), mockInvalidData());
		});
		assertArrayEquals(new Object[] { null, null }, e.getArguments());
	}

	@Test
	void doesNotCallWithOneReplacedBodyAndOneItem() {
		e = newEndpoint(0, "withOneBodyAndOneItem", Object.class, int.class);
		Object body = new Object();
		assertThrows(BadRequestException.class, () -> {
			e.call(resource, List.of("1"), Map.of("name", List.of(mockData(body))), null);
		});
		assertArrayEquals(new Object[] { null, null }, e.getArguments());
	}

	@Test
	void doesNotCallWithOneMissingBodyAndOneItem() {
		e = newEndpoint(0, "withOneBodyAndOneItem", Object.class, int.class);
		assertThrows(BadRequestException.class, () -> {
			e.call(resource, List.of("1"), Map.of(), null);
		});
		assertArrayEquals(new Object[] { null, null }, e.getArguments());
	}

	@Test
	void doesNotCallWithOneBodyAndOneInvalidItem() {
		e = newEndpoint(0, "withOneBodyAndOneItem", Object.class, int.class);
		Object body = new Object();
		assertThrows(BadRequestException.class, () -> {
			e.call(resource, List.of("s"), Map.of(), mockData(body));
		});
		assertArrayEquals(new Object[] { null, null }, e.getArguments());
	}

	@Test
	void callsWithEverythingAndTwoParts() {
		e = newEndpoint(0, "withEverythingAndTwoParts", int.class, Object.class, double.class, String.class);
		Object body = new Object();
		assertTrue((boolean) e.call(resource, List.of("1", "2.3"), Map.of("name0", List.of(mockData(body)), "name1", List.of(mockStringData())), null));
		assertArrayEquals(new Object[] { null, null, null, null }, e.getArguments());
		verify(resource).withEverythingAndTwoParts(eq(1), eq(body), eq(2.3, DELTA), eq("body"));
	}

	@Test
	void callsWithEverythingAndZeroVarArgs() {
		e = newEndpoint(0, "withEverythingAndVarArgs", int.class, Object.class, double[].class);
		doAnswer((invocation) -> {
			assertEquals(2.3, invocation.getArgument(1), DELTA);
			return null;
		}).when(resource).withEverythingAndVarArgs(eq(1), any(double[].class));
		Object body = new Object();
		assertTrue((boolean) e.call(resource, List.of("1"), Map.of(), mockData(body)));
		assertArrayEquals(new Object[] { null, null, null }, e.getArguments());
	}

	@Test
	void callsWithEverythingAndOneVarArg() {
		e = newEndpoint(0, "withEverythingAndVarArgs", int.class, Object.class, double[].class);
		doAnswer((invocation) -> {
			assertEquals(2.3, invocation.getArgument(1), DELTA);
			return null;
		}).when(resource).withEverythingAndVarArgs(eq(1), any(double[].class));
		Object body = new Object();
		assertTrue((boolean) e.call(resource, List.of("1", "2.3"), Map.of(), mockData(body)));
		assertArrayEquals(new Object[] { null, null, null }, e.getArguments());
	}

	@Test
	void callsWithEverythingAndTwoVarArgs() {
		e = newEndpoint(0, "withEverythingAndVarArgs", int.class, Object.class, double[].class);
		doAnswer((invocation) -> {
			assertEquals(2.3, invocation.getArgument(1), DELTA);
			assertEquals(4.5, invocation.getArgument(2), DELTA);
			return null;
		}).when(resource).withEverythingAndVarArgs(eq(1), any(double[].class));
		Object body = new Object();
		assertTrue((boolean) e.call(resource, List.of("1", "2.3", "4.5"), Map.of(), mockData(body)));
		assertArrayEquals(new Object[] { null, null, null }, e.getArguments());
	}

	@Test
	void callsWithEverythingAndOneBody() {
		e = newEndpoint(0, "withEverythingAndOneBody", int.class, Object.class, double.class);
		Object body = new Object();
		assertTrue((boolean) e.call(resource, List.of("1", "2.3"), Map.of(), mockData(body)));
		assertArrayEquals(new Object[] { null, null, null }, e.getArguments());
		verify(resource).withEverythingAndOneBody(eq(1), eq(body), eq(2.3, DELTA));
	}

	private Data mockData(Object body) {
		Data data = mock(Data.class);
		when(data.getBody(Object.class)).thenReturn(body);
		return data;
	}

	private Data mockStringData() {
		Data data = mock(Data.class);
		when(data.getBody(String.class)).thenReturn("body");
		return data;
	}

	private Data mockUnsupportedData() {
		Data data = mock(Data.class);
		when(data.getBody(Object.class)).thenThrow(SupportException.class);
		return data;
	}

	private Data mockInvalidData() {
		Data data = mock(Data.class);
		when(data.getBody(Object.class)).thenThrow(DisassemblingException.class);
		return data;
	}

	@Test
	void callsWithGenericReturn() {
		e = newEndpoint(0, "withGenericReturn");
		assertEquals(List.of(false, true), e.call(resource, List.of(), Map.of(), null));
		assertArrayEquals(new Object[] {}, e.getArguments());
		verify(resource).withGenericReturn();
	}

	@Test
	void callsWithGenericPart() {
		e = newEndpoint(0, "withGenericPart", List.class);
		List<Object> body = List.of(new Object());
		assertNull(e.call(resource, List.of(), Map.of("name", List.of(mockGenericData(body))), null));
		assertArrayEquals(new Object[] { null }, e.getArguments());
		verify(resource).withGenericPart(body);
	}

	@Test
	void callsWithGenericBody() {
		e = newEndpoint(0, "withGenericBody", List.class);
		List<Object> body = List.of(new Object());
		assertNull(e.call(resource, List.of(), Map.of(), mockGenericData(body)));
		assertArrayEquals(new Object[] { null }, e.getArguments());
		verify(resource).withGenericBody(body);
	}

	private Data mockGenericData(List<Object> body) {
		Data data = mock(Data.class);
		when(data.getBody(new Hint<List<Object>>() {}.getType())).thenReturn(body);
		return data;
	}

	@Test
	void doesNotCallWithUncheckedException() {
		e = newEndpoint(0, "withUncheckedException");
		assertThrows(RuntimeException.class, () -> {
			e.call(resource, List.of(), Map.of(), null);
		});
		assertArrayEquals(new Object[] {}, e.getArguments());
	}

	@Test
	void doesNotInvokeWithCheckedException() {
		assertDoesNotInvoke("withCheckedException");
	}

	@Test
	void doesNotInvokeWithoutPublic() {
		assertDoesNotInvoke("withoutPublic");
	}

	private void assertDoesNotInvoke(String methodName) {
		e = newEndpoint(0, "withNothing");
		Method method;
		try {
			method = Signatures.class.getDeclaredMethod(methodName);
		} catch (NoSuchMethodException exception) {
			throw new AssertionError(exception);
		}
		assertThrows(AssertionError.class, () -> {
			e.invoke(method, resource);
		});
		assertArrayEquals(new Object[] {}, e.getArguments());
	}

	private Endpoint newEndpoint(int distance, String methodName, Class<?>... types) {
		Class<? extends RestResource> subType = Signatures.class;
		Method method;
		try {
			method = subType.getDeclaredMethod(methodName, types);
		} catch (NoSuchMethodException exception) {
			throw new AssertionError(exception);
		}
		return new Endpoint(cache, distance, subType, subType.getName(), method, methodName);
	}
}
