package br.pro.hashi.sdx.rest.reflection;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockConstruction;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.lang.invoke.MethodHandle;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.MockedConstruction;
import org.mockito.MockedConstruction.MockInitializer;
import org.reflections.Reflections;

import br.pro.hashi.sdx.rest.reflection.exception.ReflectionException;
import br.pro.hashi.sdx.rest.reflection.mock.concrete.AbstractChild;
import br.pro.hashi.sdx.rest.reflection.mock.concrete.Child;
import br.pro.hashi.sdx.rest.reflection.mock.concrete.Parent;
import br.pro.hashi.sdx.rest.reflection.mock.handle.ArgsConstructor;
import br.pro.hashi.sdx.rest.reflection.mock.handle.ImplicitConstructor;
import br.pro.hashi.sdx.rest.reflection.mock.handle.Methods;
import br.pro.hashi.sdx.rest.reflection.mock.handle.PackageConstructor;
import br.pro.hashi.sdx.rest.reflection.mock.handle.PrivateConstructor;
import br.pro.hashi.sdx.rest.reflection.mock.handle.ProtectedConstructor;
import br.pro.hashi.sdx.rest.reflection.mock.handle.PublicConstructor;
import br.pro.hashi.sdx.rest.reflection.mock.handle.ThrowerConstructor;
import br.pro.hashi.sdx.rest.reflection.mock.specific.ChildWithBoth;
import br.pro.hashi.sdx.rest.reflection.mock.specific.ChildWithLeft;
import br.pro.hashi.sdx.rest.reflection.mock.specific.ChildWithNeither;
import br.pro.hashi.sdx.rest.reflection.mock.specific.ChildWithRight;
import br.pro.hashi.sdx.rest.reflection.mock.specific.FinalChildWithBoth;
import br.pro.hashi.sdx.rest.reflection.mock.specific.FinalChildWithLeft;
import br.pro.hashi.sdx.rest.reflection.mock.specific.FinalChildWithNeither;
import br.pro.hashi.sdx.rest.reflection.mock.specific.FinalChildWithRight;
import br.pro.hashi.sdx.rest.reflection.mock.specific.FinalImplementationWithBoth;
import br.pro.hashi.sdx.rest.reflection.mock.specific.FinalImplementationWithDiamond;
import br.pro.hashi.sdx.rest.reflection.mock.specific.FinalImplementationWithLeft;
import br.pro.hashi.sdx.rest.reflection.mock.specific.FinalImplementationWithNeither;
import br.pro.hashi.sdx.rest.reflection.mock.specific.FinalImplementationWithRight;
import br.pro.hashi.sdx.rest.reflection.mock.specific.FinalMixedWithBoth;
import br.pro.hashi.sdx.rest.reflection.mock.specific.FinalMixedWithLeft;
import br.pro.hashi.sdx.rest.reflection.mock.specific.FinalMixedWithNeither;
import br.pro.hashi.sdx.rest.reflection.mock.specific.FinalMixedWithRight;
import br.pro.hashi.sdx.rest.reflection.mock.specific.GenericInterface;
import br.pro.hashi.sdx.rest.reflection.mock.specific.GenericParent;
import br.pro.hashi.sdx.rest.reflection.mock.specific.ImplementationWithBoth;
import br.pro.hashi.sdx.rest.reflection.mock.specific.ImplementationWithDiamond;
import br.pro.hashi.sdx.rest.reflection.mock.specific.ImplementationWithLeft;
import br.pro.hashi.sdx.rest.reflection.mock.specific.ImplementationWithNeither;
import br.pro.hashi.sdx.rest.reflection.mock.specific.ImplementationWithRight;
import br.pro.hashi.sdx.rest.reflection.mock.specific.MixedWithBoth;
import br.pro.hashi.sdx.rest.reflection.mock.specific.MixedWithLeft;
import br.pro.hashi.sdx.rest.reflection.mock.specific.MixedWithNeither;
import br.pro.hashi.sdx.rest.reflection.mock.specific.MixedWithRight;
import br.pro.hashi.sdx.rest.reflection.mock.specific.PartialGenericInterface;
import br.pro.hashi.sdx.rest.reflection.mock.specific.PartialGenericParent;

class ReflectorTest {
	private Reflector r;

	@BeforeEach
	void setUp() {
		r = new Reflector();
	}

	@Test
	void getsInstance() {
		assertNotNull(Reflector.getInstance());
	}

	@ParameterizedTest
	@ValueSource(classes = {
			ImplicitConstructor.class,
			PublicConstructor.class,
			ProtectedConstructor.class,
			PackageConstructor.class,
			PrivateConstructor.class })
	void getsAndCallsNoArgsConstructor(Class<?> type) {
		MethodHandle handle = assertDoesNotThrow(() -> {
			return r.getNoArgsConstructor(type);
		});
		assertDoesNotThrow(() -> {
			r.newNoArgsInstance(handle);
		});
	}

	@Test
	void doesNotGetArgsConstructor() {
		assertThrows(ReflectionException.class, () -> {
			r.getNoArgsConstructor(ArgsConstructor.class);
		});
	}

	@ParameterizedTest
	@ValueSource(classes = {
			ProtectedConstructor.class,
			PackageConstructor.class,
			PrivateConstructor.class })
	void doesNotUnreflectIllegalConstructor(Class<?> type) {
		Constructor<?> constructor = assertDoesNotThrow(() -> {
			return type.getDeclaredConstructor();
		});
		assertThrows(AssertionError.class, () -> {
			r.unreflectConstructor(constructor);
		});
	}

	@Test
	void getsButDoesNotCallThrowerConstructor() {
		MethodHandle handle = assertDoesNotThrow(() -> {
			return r.getNoArgsConstructor(ThrowerConstructor.class);
		});
		assertThrows(ReflectionException.class, () -> {
			r.newNoArgsInstance(handle);
		});
	}

	@Test
	void unreflectsMethod() {
		Method method = assertDoesNotThrow(() -> {
			return Methods.class.getDeclaredMethod("legal");
		});
		assertDoesNotThrow(() -> {
			r.unreflect(method);
		});
	}

	@ParameterizedTest
	@ValueSource(strings = {
			"illegalProtected",
			"illegalPackage",
			"illegalPrivate" })
	void doesNotUnreflectIllegalMethod(String methodName) {
		Method method = assertDoesNotThrow(() -> {
			return Methods.class.getDeclaredMethod(methodName);
		});
		assertThrows(AssertionError.class, () -> {
			r.unreflect(method);
		});
	}

	@Test
	void instantiatesAndPassesReflections() {
		MockInitializer<Reflections> initializer = (mock, context) -> {
			assertEquals("package", context.arguments().get(0));
		};
		try (MockedConstruction<Reflections> construction = mockConstruction(Reflections.class, initializer)) {
			r = spy(r);
			r.getConcreteSubTypes("package", Parent.class);
			List<Reflections> constructed = construction.constructed();
			assertEquals(1, constructed.size());
			verify(r).getConcreteSubTypes(constructed.get(0), Parent.class);
		}
	}

	@Test
	void getsConcreteSubTypes() {
		Reflections reflections = mock(Reflections.class);
		when(reflections.getSubTypesOf(Parent.class)).thenReturn(Set.of(Child.class, AbstractChild.class));
		List<Class<? extends Parent>> subTypes = new ArrayList<>();
		r.getConcreteSubTypes(reflections, Parent.class).forEach(subTypes::add);
		assertEquals(1, subTypes.size());
		assertEquals(Child.class, subTypes.get(0));
	}

	@Test
	void getsBothSpecificTypesFromFinalChildWithBoth() {
		assertBothSpecificTypesExist(GenericParent.class, new FinalChildWithBoth());
	}

	@Test
	void getsBothSpecificTypesFromFinalChildWithLeft() {
		assertBothSpecificTypesExist(GenericParent.class, new FinalChildWithLeft());
	}

	@Test
	void getsBothSpecificTypesFromFinalChildWithRight() {
		assertBothSpecificTypesExist(GenericParent.class, new FinalChildWithRight());
	}

	@Test
	void getsBothSpecificTypesFromFinalChildWithNeither() {
		assertBothSpecificTypesExist(GenericParent.class, new FinalChildWithNeither());
	}

	@Test
	void getsBothSpecificTypesFromChildWithBoth() {
		assertBothSpecificTypesExist(GenericParent.class, new ChildWithBoth());
	}

	@Test
	void getsLeftSpecificTypeFromChildWithLeft() {
		ChildWithLeft<Double> child = new ChildWithLeft<>();
		assertLeftSpecificTypeExists(GenericParent.class, child);
		assertRightSpecificTypeNotExists(GenericParent.class, child);
	}

	@Test
	void getsRightSpecificTypeFromChildWithRight() {
		ChildWithRight<Integer> child = new ChildWithRight<>();
		assertLeftSpecificTypeNotExists(GenericParent.class, child);
		assertRightSpecificTypeExists(GenericParent.class, child);
	}

	@Test
	void getsNeitherSpecificTypeFromChildWithNeither() {
		ChildWithNeither<Integer, Double> child = new ChildWithNeither<>();
		assertBothSpecificTypesNotExist(GenericParent.class, child);
	}

	@Test
	void getsBothSpecificTypesFromFinalImplementationWithDiamond() {
		assertBothSpecificTypesExist(GenericInterface.class, new FinalImplementationWithDiamond());
	}

	@Test
	void getsBothSpecificTypesFromFinalImplementationWithBoth() {
		assertBothSpecificTypesExist(GenericInterface.class, new FinalImplementationWithBoth());
	}

	@Test
	void getsBothSpecificTypesFromFinalImplementationWithLeft() {
		assertBothSpecificTypesExist(GenericInterface.class, new FinalImplementationWithLeft());
	}

	@Test
	void getsBothSpecificTypesFromFinalImplementationWithRight() {
		assertBothSpecificTypesExist(GenericInterface.class, new FinalImplementationWithRight());
	}

	@Test
	void getsBothSpecificTypesFromFinalImplementationWithNeither() {
		assertBothSpecificTypesExist(GenericInterface.class, new FinalImplementationWithNeither());
	}

	@Test
	void getsBothSpecificTypesFromImplementationWithDiamond() {
		assertBothSpecificTypesExist(GenericInterface.class, new ImplementationWithDiamond());
	}

	@Test
	void getsBothSpecificTypesFromImplementationWithBoth() {
		assertBothSpecificTypesExist(GenericInterface.class, new ImplementationWithBoth());
	}

	@Test
	void getsLeftSpecificTypeFromImplementationWithLeft() {
		ImplementationWithLeft<Double> implementation = new ImplementationWithLeft<>();
		assertLeftSpecificTypeExists(GenericInterface.class, implementation);
		assertRightSpecificTypeNotExists(GenericInterface.class, implementation);
	}

	@Test
	void getsRightSpecificTypeFromImplementationWithRight() {
		ImplementationWithRight<Integer> implementation = new ImplementationWithRight<>();
		assertLeftSpecificTypeNotExists(GenericInterface.class, implementation);
		assertRightSpecificTypeExists(GenericInterface.class, implementation);
	}

	@Test
	void getsNeitherSpecificTypeFromImplementationWithNeither() {
		ImplementationWithNeither<Integer, Double> implementation = new ImplementationWithNeither<>();
		assertBothSpecificTypesNotExist(GenericInterface.class, implementation);
	}

	private <T, S extends T> void assertBothSpecificTypesExist(Class<T> rootType, S object) {
		assertLeftSpecificTypeExists(rootType, object);
		assertRightSpecificTypeExists(rootType, object);
	}

	private <T, S extends T> void assertLeftSpecificTypeExists(Class<T> rootType, S object) {
		assertSpecificTypeEquals(Integer.class, rootType, 0, object);
	}

	private <T, S extends T> void assertRightSpecificTypeExists(Class<T> rootType, S object) {
		assertSpecificTypeEquals(Double.class, rootType, 1, object);
	}

	private <T, S extends T> void assertBothSpecificTypesNotExist(Class<T> rootType, S object) {
		assertLeftSpecificTypeNotExists(rootType, object);
		assertRightSpecificTypeNotExists(rootType, object);
	}

	private <T, S extends T> void assertLeftSpecificTypeNotExists(Class<T> rootType, S object) {
		assertSpecificTypeThrows(rootType, 0, object);
	}

	private <T, S extends T> void assertRightSpecificTypeNotExists(Class<T> rootType, S object) {
		assertSpecificTypeThrows(rootType, 1, object);
	}

	@Test
	void getsBothSpecificTypesFromFinalMixedWithBoth() {
		assertBothSpecificTypesExist(new FinalMixedWithBoth());
	}

	@Test
	void getsBothSpecificTypesFromFinalMixedWithLeft() {
		assertBothSpecificTypesExist(new FinalMixedWithLeft());
	}

	@Test
	void getsBothSpecificTypesFromFinalMixedWithRight() {
		assertBothSpecificTypesExist(new FinalMixedWithRight());
	}

	@Test
	void getsBothSpecificTypesFromFinalMixedWithNeither() {
		assertBothSpecificTypesExist(new FinalMixedWithNeither());
	}

	@Test
	void getsBothSpecificTypesFromMixedWithBoth() {
		assertBothSpecificTypesExist(new MixedWithBoth());
	}

	@Test
	void getsLeftSpecificTypeFromMixedWithLeft() {
		MixedWithLeft<Integer> mixed = new MixedWithLeft<>();
		assertLeftSpecificTypeExists(mixed);
		assertRightSpecificTypeNotExists(mixed);
	}

	@Test
	void getsRightSpecificTypeFromMixedWithRight() {
		MixedWithRight<Integer> mixed = new MixedWithRight<>();
		assertLeftSpecificTypeNotExists(mixed);
		assertRightSpecificTypeExists(mixed);
	}

	@Test
	void getsNeitherSpecificTypeFromMixedWithNeither() {
		MixedWithNeither<Integer, Double> mixed = new MixedWithNeither<>();
		assertLeftSpecificTypeNotExists(mixed);
		assertRightSpecificTypeNotExists(mixed);
	}

	private <S extends PartialGenericParent<?> & PartialGenericInterface<?>> void assertBothSpecificTypesExist(S mixed) {
		assertLeftSpecificTypeExists(mixed);
		assertRightSpecificTypeExists(mixed);
	}

	private <S extends PartialGenericParent<?> & PartialGenericInterface<?>> void assertLeftSpecificTypeExists(S mixed) {
		assertSpecificTypeEquals(Integer.class, PartialGenericParent.class, 0, mixed);
	}

	private <S extends PartialGenericParent<?> & PartialGenericInterface<?>> void assertRightSpecificTypeExists(S mixed) {
		assertSpecificTypeEquals(Double.class, PartialGenericInterface.class, 0, mixed);
	}

	private <S extends PartialGenericParent<?> & PartialGenericInterface<?>> void assertLeftSpecificTypeNotExists(S mixed) {
		assertSpecificTypeThrows(PartialGenericParent.class, 0, mixed);
	}

	private <S extends PartialGenericParent<?> & PartialGenericInterface<?>> void assertRightSpecificTypeNotExists(S mixed) {
		assertSpecificTypeThrows(PartialGenericInterface.class, 0, mixed);
	}

	private <T, S extends T> void assertSpecificTypeEquals(Class<?> expected, Class<T> rootType, int rootIndex, S object) {
		assertEquals(expected, r.getSpecificType(rootType, rootIndex, object));
	}

	private <T, S extends T> void assertSpecificTypeThrows(Class<T> rootType, int rootIndex, S object) {
		assertThrows(ReflectionException.class, () -> {
			r.getSpecificType(rootType, rootIndex, object);
		});
	}
}
