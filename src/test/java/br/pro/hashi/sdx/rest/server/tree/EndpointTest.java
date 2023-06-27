package br.pro.hashi.sdx.rest.server.tree;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.AdditionalMatchers.eq;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.lang.invoke.MethodHandle;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import br.pro.hashi.sdx.rest.reflection.ParserFactory;
import br.pro.hashi.sdx.rest.reflection.Reflector;
import br.pro.hashi.sdx.rest.reflection.exception.ReflectionException;
import br.pro.hashi.sdx.rest.server.RestException;
import br.pro.hashi.sdx.rest.server.RestResource;
import br.pro.hashi.sdx.rest.server.exception.BadRequestException;
import br.pro.hashi.sdx.rest.server.tree.Endpoint.DataParameter;
import br.pro.hashi.sdx.rest.server.tree.Endpoint.ItemParameter;
import br.pro.hashi.sdx.rest.server.tree.mock.endpoint.Signatures;
import br.pro.hashi.sdx.rest.transform.Hint;
import br.pro.hashi.sdx.rest.transform.exception.DisassemblingException;
import br.pro.hashi.sdx.rest.transform.exception.TypeException;

class EndpointTest {
	private static final double DELTA = 0.000001;

	private Function<String, Integer> intFunction;
	private Function<String, Double> doubleFunction;
	private ParserFactory factory;
	private Endpoint e;
	private Signatures resource;

	@BeforeEach
	void setUp() {
		intFunction = Integer::parseInt;
		doubleFunction = Double::parseDouble;
		factory = mock(ParserFactory.class);
		when(factory.get(int.class)).thenReturn(intFunction);
		when(factory.get(double.class)).thenReturn(doubleFunction);
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
		assertArrayEquals(new Object[] { null }, e.getArguments());
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
		assertArrayEquals(new Object[] { null }, e.getArguments());
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
		assertArrayEquals(new Object[] { null, null }, e.getArguments());
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
		assertArrayEquals(new Object[] { null, null }, e.getArguments());
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
		assertArrayEquals(new Object[] { null, null, null }, e.getArguments());
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
		assertArrayEquals(new Object[] { null, null, null }, e.getArguments());
		assertEquals(2 - distance, e.getReach());
	}

	@ParameterizedTest
	@ValueSource(ints = { 0, 1, 2 })
	void doesNotConstructWithOneItemAndVarPart(int distance) {
		assertThrows(ReflectionException.class, () -> {
			newEndpoint(distance, "withOneItemAndVarPart", int.class, Object[].class);
		});
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
		assertData(1, Object.class, 0, partArray[0]);
		assertNull(e.getBodyParameter());
		assertArrayEquals(new Object[] { null, null, null }, e.getArguments());
		assertEquals(1 - distance, e.getReach());
	}

	@Test
	void doesNotConstructWithOneItemAndOnePart() {
		assertThrows(ReflectionException.class, () -> {
			newEndpoint(2, "withOneItemAndOnePart", int.class, Object.class);
		});
	}

	@ParameterizedTest
	@ValueSource(ints = { 0, 1, 2 })
	void doesNotConstructWithOneItemAndVarBody(int distance) {
		assertThrows(ReflectionException.class, () -> {
			newEndpoint(distance, "withOneItemAndVarBody", int.class, Object[].class);
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
		assertData(1, Object.class, 200000, e.getBodyParameter());
		assertArrayEquals(new Object[] { null, null, null }, e.getArguments());
		assertEquals(1 - distance, e.getReach());
	}

	@Test
	void doesNotConstructWithOneItemAndOneBody() {
		assertThrows(ReflectionException.class, () -> {
			newEndpoint(2, "withOneItemAndOneBody", int.class, Object.class);
		});
	}

	@ParameterizedTest
	@ValueSource(ints = { 0, 1, 2 })
	void doesNotConstructWithVarPart(int distance) {
		assertThrows(ReflectionException.class, () -> {
			newEndpoint(distance, "withVarPart", Object[].class);
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
		assertData(0, Object.class, 0, partArray[0]);
		assertNull(e.getBodyParameter());
		assertArrayEquals(new Object[] { null, null }, e.getArguments());
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
		assertData(0, Object.class, 0, partArray[0]);
		assertNull(e.getBodyParameter());
		assertArrayEquals(new Object[] { null, null, null }, e.getArguments());
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
		assertData(0, Object.class, 0, partArray[0]);
		assertNull(e.getBodyParameter());
		assertArrayEquals(new Object[] { null, null, null }, e.getArguments());
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
		assertData(0, Object.class, 0, partArray[0]);
		assertData(1, String.class, 0, partArray[1]);
		assertNull(e.getBodyParameter());
		assertArrayEquals(new Object[] { null, null, null }, e.getArguments());
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
		assertData(0, Object.class, 0, partArray[0]);
		partArray = partParameters.get("name1");
		assertEquals(1, partArray.length);
		assertData(1, String.class, 0, partArray[0]);
		assertNull(e.getBodyParameter());
		assertArrayEquals(new Object[] { null, null, null }, e.getArguments());
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
	void doesNotConstructWithOnePartAndVarBody(int distance) {
		assertThrows(ReflectionException.class, () -> {
			newEndpoint(distance, "withOnePartAndVarBody", Object.class, String[].class);
		});
	}

	@ParameterizedTest
	@ValueSource(ints = { 0, 1, 2 })
	void doesNotConstructWithOnePartAndOneBody(int distance) {
		assertThrows(ReflectionException.class, () -> {
			newEndpoint(distance, "withOnePartAndOneBody", Object.class, String.class);
		});
	}

	@ParameterizedTest
	@ValueSource(ints = { 0, 1, 2 })
	void doesNotConstructWithVarBody(int distance) {
		assertThrows(ReflectionException.class, () -> {
			newEndpoint(distance, "withVarBody", Object[].class);
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
		assertData(0, Object.class, 200000, e.getBodyParameter());
		assertArrayEquals(new Object[] { null, null }, e.getArguments());
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
		assertData(0, Object.class, 200000, e.getBodyParameter());
		assertArrayEquals(new Object[] { null, null, null }, e.getArguments());
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
		assertData(0, Object.class, 200000, e.getBodyParameter());
		assertArrayEquals(new Object[] { null, null, null }, e.getArguments());
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
	void doesNotConstructWithOneBodyAndVarPart(int distance) {
		assertThrows(ReflectionException.class, () -> {
			newEndpoint(distance, "withOneBodyAndVarPart", Object.class, String[].class);
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
	void doesNotConstructWithOneBodyAndVarBody(int distance) {
		assertThrows(ReflectionException.class, () -> {
			newEndpoint(distance, "withOneBodyAndVarBody", Object.class, String[].class);
		});
	}

	@ParameterizedTest
	@ValueSource(ints = { 0, 1, 2 })
	void doesNotConstructWithTwoBodies(int distance) {
		assertThrows(ReflectionException.class, () -> {
			newEndpoint(distance, "withTwoBodies", Object.class, String.class);
		});
	}

	@Test
	void constructsWithLargeBody() {
		e = newEndpoint(0, "withLargeBody", Object.class);
		assertEquals(Signatures.class, e.getResourceType());
		assertEquals(void.class, e.getReturnType());
		assertNull(e.getVarType());
		assertEquals(0, e.getItemParameters().length);
		assertTrue(e.getPartParameters().isEmpty());
		assertData(0, Object.class, 100000, e.getBodyParameter());
		assertArrayEquals(new Object[] { null, null }, e.getArguments());
		assertEquals(0, e.getReach());
	}

	@ParameterizedTest
	@ValueSource(ints = { 1, 2 })
	void doesNotConstructWithLargeBody(int distance) {
		assertThrows(ReflectionException.class, () -> {
			newEndpoint(distance, "withLargeBody", Object.class);
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
		assertData(1, Object.class, 0, partArray[0]);
		partArray = partParameters.get("name1");
		assertEquals(1, partArray.length);
		assertData(3, String.class, 0, partArray[0]);
		assertNull(e.getBodyParameter());
		assertArrayEquals(new Object[] { null, null, null, null, null }, e.getArguments());
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
		assertData(1, Object.class, 200000, e.getBodyParameter());
		assertArrayEquals(new Object[] { null, null, null, null }, e.getArguments());
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
		assertData(1, Object.class, 200000, e.getBodyParameter());
		assertArrayEquals(new Object[] { null, null, null, null }, e.getArguments());
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
		assertArrayEquals(new Object[] { null }, e.getArguments());
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
		assertData(0, new Hint<List<Object>>() {}.getType(), 0, partArray[0]);
		assertNull(e.getBodyParameter());
		assertArrayEquals(new Object[] { null, null }, e.getArguments());
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
		assertData(0, new Hint<List<Object>>() {}.getType(), 200000, e.getBodyParameter());
		assertArrayEquals(new Object[] { null, null }, e.getArguments());
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
		assertArrayEquals(new Object[] { null }, e.getArguments());
		assertEquals(0, e.getReach());
	}

	@ParameterizedTest
	@ValueSource(ints = { 1, 2 })
	void doesNotConstructWithUncheckedException(int distance) {
		assertThrows(ReflectionException.class, () -> {
			newEndpoint(distance, "withUncheckedException");
		});
	}

	@Test
	void constructsWithCheckedException() {
		e = newEndpoint(0, "withCheckedException");
		assertEquals(Signatures.class, e.getResourceType());
		assertEquals(void.class, e.getReturnType());
		assertNull(e.getVarType());
		assertEquals(0, e.getItemParameters().length);
		assertTrue(e.getPartParameters().isEmpty());
		assertNull(e.getBodyParameter());
		assertArrayEquals(new Object[] { null }, e.getArguments());
		assertEquals(0, e.getReach());
	}

	@ParameterizedTest
	@ValueSource(ints = { 1, 2 })
	void doesNotConstructWithCheckedException(int distance) {
		assertThrows(ReflectionException.class, () -> {
			newEndpoint(distance, "withCheckedException");
		});
	}

	private void assertItem(int index, Function<String, ?> function, String name, ItemParameter parameter) {
		assertEquals(index, parameter.index() - 1);
		assertSame(function, parameter.function());
		assertEquals(name, parameter.name());
	}

	private void assertData(int index, Type type, long maxSize, DataParameter parameter) {
		assertEquals(index, parameter.index() - 1);
		assertEquals(type, parameter.type());
		assertEquals(maxSize, parameter.maxSize());
	}

	@Test
	void callsWithReturn() {
		e = newEndpoint(0, "withReturn");
		assertDoesNotThrow(() -> {
			assertTrue((boolean) e.call(resource, List.of(), Map.of(), null));
		});
		assertArrayEquals(new Object[] { null }, e.getArguments());
		verify(resource).withReturn();
	}

	@Test
	void callsWithNothing() {
		e = newEndpoint(0, "withNothing");
		assertDoesNotThrow(() -> {
			assertNull(e.call(resource, List.of(), Map.of(), null));
		});
		assertArrayEquals(new Object[] { null }, e.getArguments());
		verify(resource).withNothing();
	}

	@Test
	void doesNotCallWithNothingAndOneExtraBody() {
		e = newEndpoint(0, "withNothing");
		Data data = mockExtraData();
		assertThrows(BadRequestException.class, () -> {
			e.call(resource, List.of(), Map.of(), data);
		});
	}

	@Test
	void doesNotCallWithNothingAndOneInvalidExtraBody() {
		e = newEndpoint(0, "withNothing");
		Data data = mockInvalidExtraData();
		assertThrows(UncheckedIOException.class, () -> {
			e.call(resource, List.of(), Map.of(), data);
		});
	}

	@Test
	void callsWithNothingAndOneEmptyExtraBody() {
		e = newEndpoint(0, "withNothing");
		Object body = new Object();
		Data data = mockEmptyExtraData(body);
		assertDoesNotThrow(() -> {
			assertNull(e.call(resource, List.of(), Map.of(), data));
		});
		assertArrayEquals(new Object[] { null }, e.getArguments());
		verify(resource).withNothing();
	}

	@Test
	void doesNotCallWithNothingAndOneInvalidEmptyExtraBody() {
		e = newEndpoint(0, "withNothing");
		IOException cause = new IOException();
		Data data = mockInvalidEmptyExtraData(cause);
		Exception exception = assertThrows(UncheckedIOException.class, () -> {
			e.call(resource, List.of(), Map.of(), data);
		});
		assertSame(cause, exception.getCause());
	}

	@Test
	void callsWithZeroVarArgs() {
		e = newEndpoint(0, "withVarArgs", int[].class);
		assertDoesNotThrow(() -> {
			assertNull(e.call(resource, List.of(), Map.of(), null));
		});
		assertArrayEquals(new Object[] { null, null }, e.getArguments());
		verify(resource).withVarArgs();
	}

	@Test
	void callsWithOneVarArg() {
		e = newEndpoint(0, "withVarArgs", int[].class);
		assertDoesNotThrow(() -> {
			assertNull(e.call(resource, List.of("1"), Map.of(), null));
		});
		assertArrayEquals(new Object[] { null, null }, e.getArguments());
		verify(resource).withVarArgs(1);
	}

	@Test
	void callsWithTwoVarArgs() {
		e = newEndpoint(0, "withVarArgs", int[].class);
		assertDoesNotThrow(() -> {
			assertNull(e.call(resource, List.of("1", "2"), Map.of(), null));
		});
		assertArrayEquals(new Object[] { null, null }, e.getArguments());
		verify(resource).withVarArgs(1, 2);
	}

	@Test
	void doesNotCallWithVarArgsAndOneExtraBody() {
		e = newEndpoint(0, "withVarArgs", int[].class);
		Data data = mockExtraData();
		assertThrows(BadRequestException.class, () -> {
			e.call(resource, List.of("1", "2"), Map.of(), data);
		});
	}

	@Test
	void doesNotCallWithVarArgsAndOneInvalidExtraBody() {
		e = newEndpoint(0, "withVarArgs", int[].class);
		Data data = mockInvalidExtraData();
		assertThrows(UncheckedIOException.class, () -> {
			e.call(resource, List.of("1", "2"), Map.of(), data);
		});
	}

	@Test
	void doesNotCallWithVarArgsAndOneInvalidEmptyExtraBody() {
		e = newEndpoint(0, "withVarArgs", int[].class);
		IOException cause = new IOException();
		Data data = mockInvalidEmptyExtraData(cause);
		Exception exception = assertThrows(UncheckedIOException.class, () -> {
			e.call(resource, List.of("1", "2"), Map.of(), data);
		});
		assertSame(cause, exception.getCause());
	}

	@Test
	void doesNotCallWithInvalidVarArgs() {
		e = newEndpoint(0, "withVarArgs", int[].class);
		assertThrows(BadRequestException.class, () -> {
			e.call(resource, List.of("1", "s"), Map.of(), null);
		});
		assertArrayEquals(new Object[] { null, null }, e.getArguments());
	}

	@Test
	void callsWithOneItem() {
		e = newEndpoint(0, "withOneItem", int.class);
		assertDoesNotThrow(() -> {
			assertNull(e.call(resource, List.of("1"), Map.of(), null));
		});
		assertArrayEquals(new Object[] { null, null }, e.getArguments());
		verify(resource).withOneItem(1);
	}

	@Test
	void doesNotCallWithOneItemAndOneExtraBody() {
		e = newEndpoint(0, "withOneItem", int.class);
		Data data = mockExtraData();
		assertThrows(BadRequestException.class, () -> {
			e.call(resource, List.of("1"), Map.of(), data);
		});
	}

	@Test
	void doesNotCallWithOneItemAndOneInvalidExtraBody() {
		e = newEndpoint(0, "withOneItem", int.class);
		Data data = mockInvalidExtraData();
		assertThrows(UncheckedIOException.class, () -> {
			e.call(resource, List.of("1"), Map.of(), data);
		});
	}

	@Test
	void doesNotCallWithOneItemAndOneInvalidEmptyExtraBody() {
		e = newEndpoint(0, "withOneItem", int.class);
		IOException cause = new IOException();
		Data data = mockInvalidEmptyExtraData(cause);
		Exception exception = assertThrows(UncheckedIOException.class, () -> {
			e.call(resource, List.of("1"), Map.of(), data);
		});
		assertSame(cause, exception.getCause());
	}

	@Test
	void doesNotCallWithOneInvalidItem() {
		e = newEndpoint(0, "withOneItem", int.class);
		assertThrows(BadRequestException.class, () -> {
			e.call(resource, List.of("s"), Map.of(), null);
		});
		assertArrayEquals(new Object[] { null, null }, e.getArguments());
	}

	@Test
	void callsWithOneItemAndZeroVarArgs() {
		e = newEndpoint(0, "withOneItemAndVarArgs", int.class, double[].class);
		assertDoesNotThrow(() -> {
			assertNull(e.call(resource, List.of("1"), Map.of(), null));
		});
		assertArrayEquals(new Object[] { null, null, null }, e.getArguments());
		verify(resource).withOneItemAndVarArgs(1);
	}

	@Test
	void callsWithOneItemAndOneVarArg() {
		e = newEndpoint(0, "withOneItemAndVarArgs", int.class, double[].class);
		assertDoesNotThrow(() -> {
			assertNull(e.call(resource, List.of("1", "2.3"), Map.of(), null));
		});
		assertArrayEquals(new Object[] { null, null, null }, e.getArguments());
		verify(resource).withOneItemAndVarArgs(eq(1), eq(2.3, DELTA));
	}

	@Test
	void callsWithOneItemAndTwoVarArgs() {
		e = newEndpoint(0, "withOneItemAndVarArgs", int.class, double[].class);
		assertDoesNotThrow(() -> {
			assertNull(e.call(resource, List.of("1", "2.3", "4.5"), Map.of(), null));
		});
		assertArrayEquals(new Object[] { null, null, null }, e.getArguments());
		verify(resource).withOneItemAndVarArgs(eq(1), eq(2.3, DELTA), eq(4.5, DELTA));
	}

	@Test
	void doesNotCallWithOneInvalidItemAndVarArgs() {
		e = newEndpoint(0, "withOneItemAndVarArgs", int.class, double[].class);
		assertThrows(BadRequestException.class, () -> {
			e.call(resource, List.of("s", "2.3", "4.5"), Map.of(), null);
		});
		assertArrayEquals(new Object[] { null, null, null }, e.getArguments());
	}

	@Test
	void doesNotCallWithOneItemAndInvalidVarArgs() {
		e = newEndpoint(0, "withOneItemAndVarArgs", int.class, double[].class);
		assertThrows(BadRequestException.class, () -> {
			e.call(resource, List.of("1", "2.3", "s"), Map.of(), null);
		});
		assertArrayEquals(new Object[] { null, null, null }, e.getArguments());
	}

	@Test
	void callsWithTwoItems() {
		e = newEndpoint(0, "withTwoItems", int.class, double.class);
		assertDoesNotThrow(() -> {
			assertNull(e.call(resource, List.of("1", "2.3"), Map.of(), null));
		});
		assertArrayEquals(new Object[] { null, null, null }, e.getArguments());
		verify(resource).withTwoItems(eq(1), eq(2.3, DELTA));
	}

	@Test
	void doesNotCallWithTwoItemsAndOneExtraBody() {
		e = newEndpoint(0, "withTwoItems", int.class, double.class);
		Data data = mockExtraData();
		assertThrows(BadRequestException.class, () -> {
			e.call(resource, List.of("1", "2.3"), Map.of(), data);
		});
	}

	@Test
	void doesNotCallWithTwoItemsAndOneInvalidExtraBody() {
		e = newEndpoint(0, "withTwoItems", int.class, double.class);
		Data data = mockInvalidExtraData();
		assertThrows(UncheckedIOException.class, () -> {
			e.call(resource, List.of("1", "2.3"), Map.of(), data);
		});
	}

	@Test
	void doesNotCallWithTwoItemsAndOneInvalidEmptyExtraBody() {
		e = newEndpoint(0, "withTwoItems", int.class, double.class);
		IOException cause = new IOException();
		Data data = mockInvalidEmptyExtraData(cause);
		Exception exception = assertThrows(UncheckedIOException.class, () -> {
			e.call(resource, List.of("1", "2.3"), Map.of(), data);
		});
		assertSame(cause, exception.getCause());
	}

	@Test
	void doesNotCallWithOneItemAndOneInvalidItem() {
		e = newEndpoint(0, "withTwoItems", int.class, double.class);
		assertThrows(BadRequestException.class, () -> {
			e.call(resource, List.of("1", "s"), Map.of(), null);
		});
		assertArrayEquals(new Object[] { null, null, null }, e.getArguments());
	}

	@Test
	void doesNotCallWithOneInvalidItemAndOneItem() {
		e = newEndpoint(0, "withTwoItems", int.class, double.class);
		assertThrows(BadRequestException.class, () -> {
			e.call(resource, List.of("s", "2.3"), Map.of(), null);
		});
		assertArrayEquals(new Object[] { null, null, null }, e.getArguments());
	}

	@Test
	void callsWithOneItemAndOnePart() {
		e = newEndpoint(0, "withOneItemAndOnePart", int.class, Object.class);
		Object body = new Object();
		Data data = mockData(body);
		assertDoesNotThrow(() -> {
			assertNull(e.call(resource, List.of("1"), Map.of("name", List.of(data)), null));
		});
		assertArrayEquals(new Object[] { null, null, null }, e.getArguments());
		verify(resource).withOneItemAndOnePart(1, body);
		verify(data).getBody(Object.class, 0);
	}

	@Test
	void doesNotCallWithOneInvalidItemAndOnePart() {
		e = newEndpoint(0, "withOneItemAndOnePart", int.class, Object.class);
		Data data = mockData(new Object());
		assertThrows(BadRequestException.class, () -> {
			e.call(resource, List.of("s"), Map.of("name", List.of(data)), null);
		});
		assertArrayEquals(new Object[] { null, null, null }, e.getArguments());
		verify(data, times(0)).getBody(eq(Object.class), any(long.class));
	}

	@Test
	void doesNotCallWithOneItemAndOneExtraPart() {
		e = newEndpoint(0, "withOneItemAndOnePart", int.class, Object.class);
		Data data = mockData(new Object());
		Data stringData = mockStringData();
		assertThrows(BadRequestException.class, () -> {
			e.call(resource, List.of("1"), Map.of("name", List.of(data, stringData)), null);
		});
		assertArrayEquals(new Object[] { null, null, null }, e.getArguments());
		verify(data, times(0)).getBody(eq(Object.class), any(long.class));
		verify(stringData, times(0)).getBody(eq(String.class), any(long.class));
	}

	@Test
	void doesNotCallWithOneItemAndOneUnsupportedPart() {
		e = newEndpoint(0, "withOneItemAndOnePart", int.class, Object.class);
		RestException exception = assertThrows(RestException.class, () -> {
			e.call(resource, List.of("1"), Map.of("name", List.of(mockUnsupportedData())), null);
		});
		assertEquals(415, exception.getStatus());
		assertArrayEquals(new Object[] { null, null, null }, e.getArguments());
	}

	@Test
	void doesNotCallWithOneItemAndOneInvalidPart() {
		e = newEndpoint(0, "withOneItemAndOnePart", int.class, Object.class);
		assertThrows(BadRequestException.class, () -> {
			e.call(resource, List.of("1"), Map.of("name", List.of(mockInvalidData())), null);
		});
		assertArrayEquals(new Object[] { null, null, null }, e.getArguments());
	}

	@Test
	void doesNotCallWithOneItemAndOneReplacedPart() {
		e = newEndpoint(0, "withOneItemAndOnePart", int.class, Object.class);
		Data data = mockData(new Object());
		assertThrows(BadRequestException.class, () -> {
			e.call(resource, List.of("1"), Map.of(), data);
		});
		assertArrayEquals(new Object[] { null, null, null }, e.getArguments());
		verify(data, times(0)).getBody(eq(Object.class), any(long.class));
	}

	@Test
	void doesNotCallWithOneItemAndOneMissingPart() {
		e = newEndpoint(0, "withOneItemAndOnePart", int.class, Object.class);
		assertThrows(BadRequestException.class, () -> {
			e.call(resource, List.of("1"), Map.of(), null);
		});
		assertArrayEquals(new Object[] { null, null, null }, e.getArguments());
	}

	@Test
	void callsWithOneItemAndOneBody() {
		e = newEndpoint(0, "withOneItemAndOneBody", int.class, Object.class);
		Object body = new Object();
		Data data = mockData(body);
		assertDoesNotThrow(() -> {
			assertNull(e.call(resource, List.of("1"), Map.of(), data));
		});
		assertArrayEquals(new Object[] { null, null, null }, e.getArguments());
		verify(resource).withOneItemAndOneBody(1, body);
		verify(data).getBody(Object.class, 200000);
	}

	@Test
	void doesNotCallWithOneInvalidItemAndOneBody() {
		e = newEndpoint(0, "withOneItemAndOneBody", int.class, Object.class);
		Data data = mockData(new Object());
		assertThrows(BadRequestException.class, () -> {
			e.call(resource, List.of("s"), Map.of(), data);
		});
		assertArrayEquals(new Object[] { null, null, null }, e.getArguments());
		verify(data, times(0)).getBody(eq(Object.class), any(long.class));
	}

	@Test
	void doesNotCallWithOneItemAndOneUnsupportedBody() {
		e = newEndpoint(0, "withOneItemAndOneBody", int.class, Object.class);
		RestException exception = assertThrows(RestException.class, () -> {
			e.call(resource, List.of("1"), Map.of(), mockUnsupportedData());
		});
		assertEquals(415, exception.getStatus());
		assertArrayEquals(new Object[] { null, null, null }, e.getArguments());
	}

	@Test
	void doesNotCallWithOneItemAndOneInvalidBody() {
		e = newEndpoint(0, "withOneItemAndOneBody", int.class, Object.class);
		assertThrows(BadRequestException.class, () -> {
			e.call(resource, List.of("1"), Map.of(), mockInvalidData());
		});
		assertArrayEquals(new Object[] { null, null, null }, e.getArguments());
	}

	@Test
	void doesNotCallWithOneItemAndOneReplacedBody() {
		e = newEndpoint(0, "withOneItemAndOneBody", int.class, Object.class);
		Data data = mockData(new Object());
		assertThrows(BadRequestException.class, () -> {
			e.call(resource, List.of("1"), Map.of("name", List.of(data)), null);
		});
		assertArrayEquals(new Object[] { null, null, null }, e.getArguments());
		verify(data, times(0)).getBody(eq(Object.class), any(long.class));
	}

	@Test
	void doesNotCallWithOneItemAndOneMissingBody() {
		e = newEndpoint(0, "withOneItemAndOneBody", int.class, Object.class);
		assertThrows(BadRequestException.class, () -> {
			e.call(resource, List.of("1"), Map.of(), null);
		});
		assertArrayEquals(new Object[] { null, null, null }, e.getArguments());
	}

	@Test
	void callsWithOnePart() {
		e = newEndpoint(0, "withOnePart", Object.class);
		Object body = new Object();
		Data data = mockData(body);
		assertDoesNotThrow(() -> {
			assertNull(e.call(resource, List.of(), Map.of("name", List.of(data)), null));
		});
		assertArrayEquals(new Object[] { null, null }, e.getArguments());
		verify(resource).withOnePart(body);
		verify(data).getBody(Object.class, 0);
	}

	@Test
	void doesNotCallWithOneExtraPart() {
		e = newEndpoint(0, "withOnePart", Object.class);
		Object body = new Object();
		Data data = mockData(body);
		Data stringData = mockStringData();
		assertThrows(BadRequestException.class, () -> {
			e.call(resource, List.of(), Map.of("name", List.of(data, stringData)), null);
		});
		assertArrayEquals(new Object[] { null, null }, e.getArguments());
		verify(data, times(0)).getBody(eq(Object.class), any(long.class));
		verify(stringData, times(0)).getBody(eq(String.class), any(long.class));
	}

	@Test
	void doesNotCallWithOneUnsupportedPart() {
		e = newEndpoint(0, "withOnePart", Object.class);
		RestException exception = assertThrows(RestException.class, () -> {
			e.call(resource, List.of(), Map.of("name", List.of(mockUnsupportedData())), null);
		});
		assertEquals(415, exception.getStatus());
		assertArrayEquals(new Object[] { null, null }, e.getArguments());
	}

	@Test
	void doesNotCallWithOneInvalidPart() {
		e = newEndpoint(0, "withOnePart", Object.class);
		assertThrows(BadRequestException.class, () -> {
			e.call(resource, List.of(), Map.of("name", List.of(mockInvalidData())), null);
		});
		assertArrayEquals(new Object[] { null, null }, e.getArguments());
	}

	@Test
	void doesNotCallWithOneReplacedPart() {
		e = newEndpoint(0, "withOnePart", Object.class);
		Data data = mockData(new Object());
		assertThrows(BadRequestException.class, () -> {
			e.call(resource, List.of(), Map.of(), data);
		});
		assertArrayEquals(new Object[] { null, null }, e.getArguments());
		verify(data, times(0)).getBody(eq(Object.class), any(long.class));
	}

	@Test
	void doesNotCallWithOneMissingPart() {
		e = newEndpoint(0, "withOnePart", Object.class);
		assertThrows(BadRequestException.class, () -> {
			e.call(resource, List.of(), Map.of(), null);
		});
		assertArrayEquals(new Object[] { null, null }, e.getArguments());
	}

	@Test
	void callsWithOnePartAndZeroVarArgs() {
		e = newEndpoint(0, "withOnePartAndVarArgs", Object.class, int[].class);
		Object body = new Object();
		Data data = mockData(body);
		assertDoesNotThrow(() -> {
			assertNull(e.call(resource, List.of(), Map.of("name", List.of(data)), null));
		});
		assertArrayEquals(new Object[] { null, null, null }, e.getArguments());
		verify(resource).withOnePartAndVarArgs(body);
		verify(data).getBody(Object.class, 0);
	}

	@Test
	void callsWithOnePartAndOneVarArg() {
		e = newEndpoint(0, "withOnePartAndVarArgs", Object.class, int[].class);
		Object body = new Object();
		Data data = mockData(body);
		assertDoesNotThrow(() -> {
			assertNull(e.call(resource, List.of("1"), Map.of("name", List.of(data)), null));
		});
		assertArrayEquals(new Object[] { null, null, null }, e.getArguments());
		verify(resource).withOnePartAndVarArgs(body, 1);
		verify(data).getBody(Object.class, 0);
	}

	@Test
	void callsWithOnePartAndTwoVarArgs() {
		e = newEndpoint(0, "withOnePartAndVarArgs", Object.class, int[].class);
		Object body = new Object();
		Data data = mockData(body);
		assertDoesNotThrow(() -> {
			assertNull(e.call(resource, List.of("1", "2"), Map.of("name", List.of(data)), null));
		});
		assertArrayEquals(new Object[] { null, null, null }, e.getArguments());
		verify(resource).withOnePartAndVarArgs(body, 1, 2);
		verify(data).getBody(Object.class, 0);
	}

	@Test
	void doesNotCallWithOneExtraPartAndVarArgs() {
		e = newEndpoint(0, "withOnePartAndVarArgs", Object.class, int[].class);
		Data data = mockData(new Object());
		Data stringData = mockStringData();
		assertThrows(BadRequestException.class, () -> {
			e.call(resource, List.of("1", "2"), Map.of("name", List.of(data, stringData)), null);
		});
		assertArrayEquals(new Object[] { null, null, null }, e.getArguments());
		verify(data, times(0)).getBody(eq(Object.class), any(long.class));
		verify(stringData, times(0)).getBody(eq(String.class), any(long.class));
	}

	@Test
	void doesNotCallWithOneUnsupportedPartAndVarArgs() {
		e = newEndpoint(0, "withOnePartAndVarArgs", Object.class, int[].class);
		RestException exception = assertThrows(RestException.class, () -> {
			e.call(resource, List.of("1", "2"), Map.of("name", List.of(mockUnsupportedData())), null);
		});
		assertEquals(415, exception.getStatus());
		assertArrayEquals(new Object[] { null, null, null }, e.getArguments());
	}

	@Test
	void doesNotCallWithOneInvalidPartAndVarArgs() {
		e = newEndpoint(0, "withOnePartAndVarArgs", Object.class, int[].class);
		assertThrows(BadRequestException.class, () -> {
			e.call(resource, List.of("1", "2"), Map.of("name", List.of(mockInvalidData())), null);
		});
		assertArrayEquals(new Object[] { null, null, null }, e.getArguments());
	}

	@Test
	void doesNotCallWithOneReplacedPartAndVarArgs() {
		e = newEndpoint(0, "withOnePartAndVarArgs", Object.class, int[].class);
		Data data = mockData(new Object());
		assertThrows(BadRequestException.class, () -> {
			e.call(resource, List.of("1", "2"), Map.of(), data);
		});
		assertArrayEquals(new Object[] { null, null, null }, e.getArguments());
		verify(data, times(0)).getBody(eq(Object.class), any(long.class));
	}

	@Test
	void doesNotCallWithOneMissingPartAndVarArgs() {
		e = newEndpoint(0, "withOnePartAndVarArgs", Object.class, int[].class);
		assertThrows(BadRequestException.class, () -> {
			e.call(resource, List.of("1", "2"), Map.of(), null);
		});
		assertArrayEquals(new Object[] { null, null, null }, e.getArguments());
	}

	@Test
	void doesNotCallWithOnePartAndInvalidVarArgs() {
		e = newEndpoint(0, "withOnePartAndVarArgs", Object.class, int[].class);
		Data data = mockData(new Object());
		assertThrows(BadRequestException.class, () -> {
			e.call(resource, List.of("1", "s"), Map.of("name", List.of(data)), null);
		});
		assertArrayEquals(new Object[] { null, null, null }, e.getArguments());
		verify(data, times(0)).getBody(eq(Object.class), any(long.class));
	}

	@Test
	void callsWithOnePartAndOneItem() {
		e = newEndpoint(0, "withOnePartAndOneItem", Object.class, int.class);
		Object body = new Object();
		Data data = mockData(body);
		assertDoesNotThrow(() -> {
			assertNull(e.call(resource, List.of("1"), Map.of("name", List.of(data)), null));
		});
		assertArrayEquals(new Object[] { null, null, null }, e.getArguments());
		verify(resource).withOnePartAndOneItem(body, 1);
		verify(data).getBody(Object.class, 0);
	}

	@Test
	void doesNotCallWithOneExtraPartAndOneItem() {
		e = newEndpoint(0, "withOnePartAndOneItem", Object.class, int.class);
		Data data = mockData(new Object());
		Data stringData = mockStringData();
		assertThrows(BadRequestException.class, () -> {
			e.call(resource, List.of("1"), Map.of("name", List.of(data, stringData)), null);
		});
		assertArrayEquals(new Object[] { null, null, null }, e.getArguments());
		verify(data, times(0)).getBody(eq(Object.class), any(long.class));
		verify(stringData, times(0)).getBody(eq(String.class), any(long.class));
	}

	@Test
	void doesNotCallWithOneUnsupportedPartAndOneItem() {
		e = newEndpoint(0, "withOnePartAndOneItem", Object.class, int.class);
		RestException exception = assertThrows(RestException.class, () -> {
			e.call(resource, List.of("1"), Map.of("name", List.of(mockUnsupportedData())), null);
		});
		assertEquals(415, exception.getStatus());
		assertArrayEquals(new Object[] { null, null, null }, e.getArguments());
	}

	@Test
	void doesNotCallWithOneInvalidPartAndOneItem() {
		e = newEndpoint(0, "withOnePartAndOneItem", Object.class, int.class);
		assertThrows(BadRequestException.class, () -> {
			e.call(resource, List.of("1"), Map.of("name", List.of(mockInvalidData())), null);
		});
		assertArrayEquals(new Object[] { null, null, null }, e.getArguments());
	}

	@Test
	void doesNotCallWithOneReplacedPartAndOneItem() {
		e = newEndpoint(0, "withOnePartAndOneItem", Object.class, int.class);
		Data data = mockData(new Object());
		assertThrows(BadRequestException.class, () -> {
			e.call(resource, List.of("1"), Map.of(), data);
		});
		assertArrayEquals(new Object[] { null, null, null }, e.getArguments());
		verify(data, times(0)).getBody(eq(Object.class), any(long.class));
	}

	@Test
	void doesNotCallWithOneMissingPartAndOneItem() {
		e = newEndpoint(0, "withOnePartAndOneItem", Object.class, int.class);
		assertThrows(BadRequestException.class, () -> {
			e.call(resource, List.of("1"), Map.of(), null);
		});
		assertArrayEquals(new Object[] { null, null, null }, e.getArguments());
	}

	@Test
	void doesNotCallWithOnePartAndOneInvalidItem() {
		e = newEndpoint(0, "withOnePartAndOneItem", Object.class, int.class);
		Data data = mockData(new Object());
		assertThrows(BadRequestException.class, () -> {
			e.call(resource, List.of("s"), Map.of("name", List.of(data)), null);
		});
		assertArrayEquals(new Object[] { null, null, null }, e.getArguments());
		verify(data, times(0)).getBody(eq(Object.class), any(long.class));
	}

	@Test
	void callsWithTwoPartsAndOneName() {
		e = newEndpoint(0, "withTwoPartsAndOneName", Object.class, String.class);
		Object body = new Object();
		Data data = mockData(body);
		Data stringData = mockStringData();
		assertDoesNotThrow(() -> {
			assertNull(e.call(resource, List.of(), Map.of("name", List.of(data, stringData)), null));
		});
		assertArrayEquals(new Object[] { null, null, null }, e.getArguments());
		verify(resource).withTwoPartsAndOneName(body, "body");
		verify(data).getBody(Object.class, 0);
		verify(stringData).getBody(String.class, 0);
	}

	@Test
	void doesNotCallWithTwoPartsAndOneName() {
		e = newEndpoint(0, "withTwoPartsAndOneName", Object.class, String.class);
		Data data = mockData(new Object());
		assertThrows(BadRequestException.class, () -> {
			e.call(resource, List.of(), Map.of("name", List.of(data)), null);
		});
		assertArrayEquals(new Object[] { null, null, null }, e.getArguments());
		verify(data, times(0)).getBody(eq(Object.class), any(long.class));
	}

	@Test
	void callsWithTwoPartsAndTwoNames() {
		e = newEndpoint(0, "withTwoPartsAndTwoNames", Object.class, String.class);
		Object body = new Object();
		Data data = mockData(body);
		Data stringData = mockStringData();
		assertDoesNotThrow(() -> {
			assertNull(e.call(resource, List.of(), Map.of("name0", List.of(data), "name1", List.of(stringData)), null));
		});
		assertArrayEquals(new Object[] { null, null, null }, e.getArguments());
		verify(resource).withTwoPartsAndTwoNames(body, "body");
		verify(data).getBody(Object.class, 0);
		verify(stringData).getBody(String.class, 0);
	}

	@Test
	void doesNotCallWithTwoPartsAndTwoNames() {
		e = newEndpoint(0, "withTwoPartsAndTwoNames", Object.class, String.class);
		Data data = mockData(new Object());
		assertThrows(BadRequestException.class, () -> {
			e.call(resource, List.of(), Map.of("name0", List.of(data)), null);
		});
		assertArrayEquals(new Object[] { null, null, null }, e.getArguments());
		verify(data, times(0)).getBody(eq(Object.class), any(long.class));
	}

	@Test
	void callsWithOneBody() {
		e = newEndpoint(0, "withOneBody", Object.class);
		Object body = new Object();
		Data data = mockData(body);
		assertDoesNotThrow(() -> {
			assertNull(e.call(resource, List.of(), Map.of(), data));
		});
		assertArrayEquals(new Object[] { null, null }, e.getArguments());
		verify(resource).withOneBody(body);
		verify(data).getBody(Object.class, 200000);
	}

	@Test
	void doesNotCallWithOneUnsupportedBody() {
		e = newEndpoint(0, "withOneBody", Object.class);
		RestException exception = assertThrows(RestException.class, () -> {
			e.call(resource, List.of(), Map.of(), mockUnsupportedData());
		});
		assertEquals(415, exception.getStatus());
		assertArrayEquals(new Object[] { null, null }, e.getArguments());
	}

	@Test
	void doesNotCallWithOneInvalidBody() {
		e = newEndpoint(0, "withOneBody", Object.class);
		assertThrows(BadRequestException.class, () -> {
			e.call(resource, List.of(), Map.of(), mockInvalidData());
		});
		assertArrayEquals(new Object[] { null, null }, e.getArguments());
	}

	@Test
	void doesNotCallWithOneReplacedBody() {
		e = newEndpoint(0, "withOneBody", Object.class);
		Data data = mockData(new Object());
		assertThrows(BadRequestException.class, () -> {
			e.call(resource, List.of(), Map.of("name", List.of(data)), null);
		});
		assertArrayEquals(new Object[] { null, null }, e.getArguments());
		verify(data, times(0)).getBody(eq(Object.class), any(long.class));
	}

	@Test
	void doesNotCallWithOneMissingBody() {
		e = newEndpoint(0, "withOneBody", Object.class);
		assertThrows(BadRequestException.class, () -> {
			e.call(resource, List.of(), Map.of(), null);
		});
		assertArrayEquals(new Object[] { null, null }, e.getArguments());
	}

	@Test
	void callsWithOneBodyAndZeroVarArgs() {
		e = newEndpoint(0, "withOneBodyAndVarArgs", Object.class, int[].class);
		Object body = new Object();
		Data data = mockData(body);
		assertDoesNotThrow(() -> {
			assertNull(e.call(resource, List.of(), Map.of(), data));
		});
		assertArrayEquals(new Object[] { null, null, null }, e.getArguments());
		verify(resource).withOneBodyAndVarArgs(body);
		verify(data).getBody(Object.class, 200000);
	}

	@Test
	void callsWithOneBodyAndOneVarArg() {
		e = newEndpoint(0, "withOneBodyAndVarArgs", Object.class, int[].class);
		Object body = new Object();
		Data data = mockData(body);
		assertDoesNotThrow(() -> {
			assertNull(e.call(resource, List.of("1"), Map.of(), data));
		});
		assertArrayEquals(new Object[] { null, null, null }, e.getArguments());
		verify(resource).withOneBodyAndVarArgs(body, 1);
		verify(data).getBody(Object.class, 200000);
	}

	@Test
	void callsWithOneBodyAndTwoVarArgs() {
		e = newEndpoint(0, "withOneBodyAndVarArgs", Object.class, int[].class);
		Object body = new Object();
		Data data = mockData(body);
		assertDoesNotThrow(() -> {
			assertNull(e.call(resource, List.of("1", "2"), Map.of(), data));
		});
		assertArrayEquals(new Object[] { null, null, null }, e.getArguments());
		verify(resource).withOneBodyAndVarArgs(body, 1, 2);
		verify(data).getBody(Object.class, 200000);
	}

	@Test
	void doesNotCallWithOneUnsupportedBodyAndVarArgs() {
		e = newEndpoint(0, "withOneBodyAndVarArgs", Object.class, int[].class);
		RestException exception = assertThrows(RestException.class, () -> {
			e.call(resource, List.of("1", "2"), Map.of(), mockUnsupportedData());
		});
		assertEquals(415, exception.getStatus());
		assertArrayEquals(new Object[] { null, null, null }, e.getArguments());
	}

	@Test
	void doesNotCallWithOneInvalidBodyAndVarArgs() {
		e = newEndpoint(0, "withOneBodyAndVarArgs", Object.class, int[].class);
		assertThrows(BadRequestException.class, () -> {
			e.call(resource, List.of("1", "2"), Map.of(), mockInvalidData());
		});
		assertArrayEquals(new Object[] { null, null, null }, e.getArguments());
	}

	@Test
	void doesNotCallWithOneReplacedBodyAndVarArgs() {
		e = newEndpoint(0, "withOneBodyAndVarArgs", Object.class, int[].class);
		Data data = mockData(new Object());
		assertThrows(BadRequestException.class, () -> {
			e.call(resource, List.of("1", "2"), Map.of("name", List.of(data)), null);
		});
		assertArrayEquals(new Object[] { null, null, null }, e.getArguments());
		verify(data, times(0)).getBody(eq(Object.class), any(long.class));
	}

	@Test
	void doesNotCallWithOneMissingBodyAndVarArgs() {
		e = newEndpoint(0, "withOneBodyAndVarArgs", Object.class, int[].class);
		assertThrows(BadRequestException.class, () -> {
			e.call(resource, List.of("1", "2"), Map.of(), mockInvalidData());
		});
		assertArrayEquals(new Object[] { null, null, null }, e.getArguments());
	}

	@Test
	void doesNotCallWithOneBodyAndInvalidVarArgs() {
		e = newEndpoint(0, "withOneBodyAndVarArgs", Object.class, int[].class);
		Data data = mockData(new Object());
		assertThrows(BadRequestException.class, () -> {
			e.call(resource, List.of("1", "s"), Map.of(), data);
		});
		assertArrayEquals(new Object[] { null, null, null }, e.getArguments());
		verify(data, times(0)).getBody(eq(Object.class), any(long.class));
	}

	@Test
	void callsWithOneBodyAndOneItem() {
		e = newEndpoint(0, "withOneBodyAndOneItem", Object.class, int.class);
		Object body = new Object();
		Data data = mockData(body);
		assertDoesNotThrow(() -> {
			assertNull(e.call(resource, List.of("1"), Map.of(), data));
		});
		assertArrayEquals(new Object[] { null, null, null }, e.getArguments());
		verify(resource).withOneBodyAndOneItem(body, 1);
		verify(data).getBody(Object.class, 200000);
	}

	@Test
	void doesNotCallWithOneUnsupportedBodyAndOneItem() {
		e = newEndpoint(0, "withOneBodyAndOneItem", Object.class, int.class);
		RestException exception = assertThrows(RestException.class, () -> {
			e.call(resource, List.of("1"), Map.of(), mockUnsupportedData());
		});
		assertEquals(415, exception.getStatus());
		assertArrayEquals(new Object[] { null, null, null }, e.getArguments());
	}

	@Test
	void doesNotCallWithOneInvalidBodyAndOneItem() {
		e = newEndpoint(0, "withOneBodyAndOneItem", Object.class, int.class);
		assertThrows(BadRequestException.class, () -> {
			e.call(resource, List.of("1"), Map.of(), mockInvalidData());
		});
		assertArrayEquals(new Object[] { null, null, null }, e.getArguments());
	}

	@Test
	void doesNotCallWithOneReplacedBodyAndOneItem() {
		e = newEndpoint(0, "withOneBodyAndOneItem", Object.class, int.class);
		Data data = mockData(new Object());
		assertThrows(BadRequestException.class, () -> {
			e.call(resource, List.of("1"), Map.of("name", List.of(data)), null);
		});
		assertArrayEquals(new Object[] { null, null, null }, e.getArguments());
		verify(data, times(0)).getBody(eq(Object.class), any(long.class));
	}

	@Test
	void doesNotCallWithOneMissingBodyAndOneItem() {
		e = newEndpoint(0, "withOneBodyAndOneItem", Object.class, int.class);
		assertThrows(BadRequestException.class, () -> {
			e.call(resource, List.of("1"), Map.of(), null);
		});
		assertArrayEquals(new Object[] { null, null, null }, e.getArguments());
	}

	@Test
	void doesNotCallWithOneBodyAndOneInvalidItem() {
		e = newEndpoint(0, "withOneBodyAndOneItem", Object.class, int.class);
		Data data = mockData(new Object());
		assertThrows(BadRequestException.class, () -> {
			e.call(resource, List.of("s"), Map.of(), data);
		});
		assertArrayEquals(new Object[] { null, null, null }, e.getArguments());
		verify(data, times(0)).getBody(eq(Object.class), any(long.class));
	}

	@Test
	void callsWithLargeBody() {
		e = newEndpoint(0, "withLargeBody", Object.class);
		Object body = new Object();
		assertDoesNotThrow(() -> {
			assertNull(e.call(resource, List.of(), Map.of(), mockData(body)));
		});
		assertArrayEquals(new Object[] { null, null }, e.getArguments());
		verify(resource).withLargeBody(body);
	}

	@Test
	void callsWithEverythingAndTwoParts() {
		e = newEndpoint(0, "withEverythingAndTwoParts", int.class, Object.class, double.class, String.class);
		Object body = new Object();
		Data data = mockData(body);
		Data stringData = mockStringData();
		assertDoesNotThrow(() -> {
			assertTrue((boolean) e.call(resource, List.of("1", "2.3"), Map.of("name0", List.of(data), "name1", List.of(stringData)), null));
		});
		assertArrayEquals(new Object[] { null, null, null, null, null }, e.getArguments());
		verify(resource).withEverythingAndTwoParts(eq(1), eq(body), eq(2.3, DELTA), eq("body"));
		verify(data).getBody(Object.class, 0);
		verify(stringData).getBody(String.class, 0);
	}

	@Test
	void callsWithEverythingAndZeroVarArgs() {
		e = newEndpoint(0, "withEverythingAndVarArgs", int.class, Object.class, double[].class);
		Object body = new Object();
		Data data = mockData(body);
		assertDoesNotThrow(() -> {
			assertTrue((boolean) e.call(resource, List.of("1"), Map.of(), data));
		});
		assertArrayEquals(new Object[] { null, null, null, null }, e.getArguments());
		verify(resource).withEverythingAndVarArgs(1, body);
		verify(data).getBody(Object.class, 200000);
	}

	@Test
	void callsWithEverythingAndOneVarArg() {
		e = newEndpoint(0, "withEverythingAndVarArgs", int.class, Object.class, double[].class);
		Object body = new Object();
		Data data = mockData(body);
		assertDoesNotThrow(() -> {
			assertTrue((boolean) e.call(resource, List.of("1", "2.3"), Map.of(), data));
		});
		assertArrayEquals(new Object[] { null, null, null, null }, e.getArguments());
		verify(resource).withEverythingAndVarArgs(eq(1), eq(body), eq(2.3, DELTA));
		verify(data).getBody(Object.class, 200000);
	}

	@Test
	void callsWithEverythingAndTwoVarArgs() {
		e = newEndpoint(0, "withEverythingAndVarArgs", int.class, Object.class, double[].class);
		Object body = new Object();
		Data data = mockData(body);
		assertDoesNotThrow(() -> {
			assertTrue((boolean) e.call(resource, List.of("1", "2.3", "4.5"), Map.of(), data));
		});
		assertArrayEquals(new Object[] { null, null, null, null }, e.getArguments());
		verify(resource).withEverythingAndVarArgs(eq(1), eq(body), eq(2.3, DELTA), eq(4.5, DELTA));
		verify(data).getBody(Object.class, 200000);
	}

	@Test
	void callsWithEverythingAndOneBody() {
		e = newEndpoint(0, "withEverythingAndOneBody", int.class, Object.class, double.class);
		Object body = new Object();
		Data data = mockData(body);
		assertDoesNotThrow(() -> {
			assertTrue((boolean) e.call(resource, List.of("1", "2.3"), Map.of(), data));
		});
		assertArrayEquals(new Object[] { null, null, null, null }, e.getArguments());
		verify(resource).withEverythingAndOneBody(eq(1), eq(body), eq(2.3, DELTA));
		verify(data).getBody(Object.class, 200000);
	}

	private Data mockData(Object body) {
		Data data = mock(Data.class);
		when(data.getBody(eq(Object.class), any(long.class))).thenReturn(body);
		return data;
	}

	private Data mockExtraData() {
		Data data = mock(Data.class);
		when(data.getStream()).thenReturn(new ByteArrayInputStream(new byte[] { 0 }));
		return data;
	}

	private Data mockInvalidExtraData() {
		Data data = mock(Data.class);
		InputStream stream = InputStream.nullInputStream();
		try {
			stream.close();
		} catch (IOException exception) {
			throw new AssertionError(exception);
		}
		when(data.getStream()).thenReturn(stream);
		return data;
	}

	private Data mockEmptyExtraData(Object body) {
		Data data = mock(Data.class);
		when(data.getStream()).thenReturn(InputStream.nullInputStream());
		when(data.getBody(eq(Object.class), any(long.class))).thenReturn(body);
		return data;
	}

	private Data mockInvalidEmptyExtraData(Throwable cause) {
		Data data = mock(Data.class);
		InputStream stream = spy(InputStream.nullInputStream());
		try {
			doThrow(cause).when(stream).close();
		} catch (IOException exception) {
			throw new AssertionError(exception);
		}
		when(data.getStream()).thenReturn(stream);
		return data;
	}

	private Data mockStringData() {
		Data data = mock(Data.class);
		when(data.getBody(eq(String.class), any(long.class))).thenReturn("body");
		return data;
	}

	private Data mockUnsupportedData() {
		Data data = mock(Data.class);
		when(data.getBody(eq(Object.class), any(long.class))).thenThrow(TypeException.class);
		return data;
	}

	private Data mockInvalidData() {
		Data data = mock(Data.class);
		when(data.getBody(eq(Object.class), any(long.class))).thenThrow(DisassemblingException.class);
		return data;
	}

	@Test
	void callsWithGenericReturn() {
		e = newEndpoint(0, "withGenericReturn");
		assertDoesNotThrow(() -> {
			assertEquals(List.of(false, true), e.call(resource, List.of(), Map.of(), null));
		});
		assertArrayEquals(new Object[] { null }, e.getArguments());
		verify(resource).withGenericReturn();
	}

	@Test
	void callsWithGenericPart() {
		e = newEndpoint(0, "withGenericPart", List.class);
		List<Object> body = List.of(new Object());
		assertDoesNotThrow(() -> {
			assertNull(e.call(resource, List.of(), Map.of("name", List.of(mockGenericData(body))), null));
		});
		assertArrayEquals(new Object[] { null, null }, e.getArguments());
		verify(resource).withGenericPart(body);
	}

	@Test
	void callsWithGenericBody() {
		e = newEndpoint(0, "withGenericBody", List.class);
		List<Object> body = List.of(new Object());
		assertDoesNotThrow(() -> {
			assertNull(e.call(resource, List.of(), Map.of(), mockGenericData(body)));
		});
		assertArrayEquals(new Object[] { null, null }, e.getArguments());
		verify(resource).withGenericBody(body);
	}

	private Data mockGenericData(List<Object> body) {
		Data data = mock(Data.class);
		when(data.getBody(eq(new Hint<List<Object>>() {}.getType()), any(long.class))).thenReturn(body);
		return data;
	}

	@Test
	void doesNotCallWithUncheckedException() {
		e = newEndpoint(0, "withUncheckedException");
		assertThrows(RuntimeException.class, () -> {
			e.call(resource, List.of(), Map.of(), null);
		});
		assertArrayEquals(new Object[] { null }, e.getArguments());
	}

	@Test
	void doesNotCallWithCheckedException() {
		e = newEndpoint(0, "withCheckedException");
		assertThrows(Exception.class, () -> {
			e.call(resource, List.of(), Map.of(), null);
		});
		assertArrayEquals(new Object[] { null }, e.getArguments());
	}

	@Test
	void doesNotUnreflectWithoutPublic() {
		e = newEndpoint(0, "withNothing");
		Method method = getDeclaredMethod(Signatures.class, "withoutPublic");
		assertThrows(AssertionError.class, () -> {
			Reflector.getInstance().unreflect(method);
		});
	}

	@Test
	void doesNotInvokeWithError() {
		e = newEndpoint(0, "withNothing");
		Method method = getDeclaredMethod(Signatures.class, "withError");
		MethodHandle handle = Reflector.getInstance().unreflect(method);
		Object[] arguments = new Object[] { resource };
		assertThrows(AssertionError.class, () -> {
			e.invoke(handle, arguments);
		});
		assertArrayEquals(new Object[] { null }, e.getArguments());
	}

	private Endpoint newEndpoint(int distance, String methodName, Class<?>... types) {
		Class<? extends RestResource> subType = Signatures.class;
		Method method = getDeclaredMethod(subType, methodName, types);
		return new Endpoint(factory, 200000, distance, subType, subType.getName(), method, methodName);
	}

	private Method getDeclaredMethod(Class<?> subType, String methodName, Class<?>... types) {
		Method method;
		try {
			method = subType.getDeclaredMethod(methodName, types);
		} catch (NoSuchMethodException exception) {
			throw new AssertionError(exception);
		}
		return method;
	}
}
