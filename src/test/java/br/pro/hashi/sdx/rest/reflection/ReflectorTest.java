package br.pro.hashi.sdx.rest.reflection;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockConstruction;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodHandles.Lookup;
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
import br.pro.hashi.sdx.rest.reflection.mock.handle.ArgumentConstructor;
import br.pro.hashi.sdx.rest.reflection.mock.handle.DefaultConstructor;
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
	private static final Lookup LOOKUP = MethodHandles.lookup();

	private Reflector r;

	@BeforeEach
	void setUp() {
		r = new Reflector();
	}

	@Test
	void getsInstance() {
		assertInstanceOf(Reflector.class, Reflector.getInstance());
	}

	@ParameterizedTest
	@ValueSource(classes = {
			DefaultConstructor.class,
			PublicConstructor.class,
			ProtectedConstructor.class,
			PackageConstructor.class,
			PrivateConstructor.class })
	<T> void getsAndInvokesCreator(Class<T> type) {
		MethodHandle creator = r.getCreator(type, type.getName());
		assertInstanceOf(type, r.invokeCreator(creator));
	}

	@Test
	void doesNotGetArgumentCreator() {
		assertThrows(ReflectionException.class, () -> {
			getCreator(ArgumentConstructor.class);
		});
	}

	@ParameterizedTest
	@ValueSource(classes = {
			ProtectedConstructor.class,
			PackageConstructor.class,
			PrivateConstructor.class })
	<T> void doesNotUnreflectIllegalConstructor(Class<T> type) {
		Constructor<T> constructor = assertDoesNotThrow(() -> {
			return type.getDeclaredConstructor();
		});
		assertThrows(AssertionError.class, () -> {
			r.unreflectConstructor(constructor, LOOKUP);
		});
	}

	@Test
	void doesNotInvokeThrowerCreator() {
		MethodHandle creator = getCreator(ThrowerConstructor.class);
		assertThrows(ReflectionException.class, () -> {
			r.invokeCreator(creator);
		});
	}

	private <T> MethodHandle getCreator(Class<T> type) {
		return r.getCreator(type, LOOKUP);
	}

	@Test
	void unreflects() {
		Method method = getDeclaredMethod("legal");
		assertDoesNotThrow(() -> {
			r.unreflect(method);
		});
	}

	@ParameterizedTest
	@ValueSource(strings = {
			"illegalProtected",
			"illegalPackage",
			"illegalPrivate" })
	void doesNotUnreflectIllegal(String methodName) {
		Method method = getDeclaredMethod(methodName);
		assertThrows(AssertionError.class, () -> {
			r.unreflect(method);
		});
	}

	private Method getDeclaredMethod(String methodName) {
		Method method = assertDoesNotThrow(() -> {
			return Methods.class.getDeclaredMethod(methodName);
		});
		return method;
	}

	@Test
	void createsReflections() {
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
		List<Class<? extends Parent>> types = new ArrayList<>();
		r.getConcreteSubTypes(reflections, Parent.class).forEach(types::add);
		assertEquals(1, types.size());
		assertEquals(Child.class, types.get(0));
	}

	@Test
	void getsBothSpecificTypesFromFinalChildWithBoth() {
		FinalChildWithBoth object = new FinalChildWithBoth();
		assertBothSpecificTypesExist(object, GenericParent.class);
	}

	@Test
	void getsBothSpecificTypesFromFinalChildWithLeft() {
		FinalChildWithLeft object = new FinalChildWithLeft();
		assertBothSpecificTypesExist(object, GenericParent.class);
	}

	@Test
	void getsBothSpecificTypesFromFinalChildWithRight() {
		FinalChildWithRight object = new FinalChildWithRight();
		assertBothSpecificTypesExist(object, GenericParent.class);
	}

	@Test
	void getsBothSpecificTypesFromFinalChildWithNeither() {
		FinalChildWithNeither object = new FinalChildWithNeither();
		assertBothSpecificTypesExist(object, GenericParent.class);
	}

	@Test
	void getsBothSpecificTypesFromChildWithBoth() {
		ChildWithBoth object = new ChildWithBoth();
		assertBothSpecificTypesExist(object, GenericParent.class);
	}

	@Test
	void getsLeftSpecificTypeFromChildWithLeft() {
		ChildWithLeft<Double> object = new ChildWithLeft<>();
		assertLeftSpecificTypeExists(object, GenericParent.class);
		assertRightSpecificTypeNotExists(object, GenericParent.class);
	}

	@Test
	void getsRightSpecificTypeFromChildWithRight() {
		ChildWithRight<Integer> object = new ChildWithRight<>();
		assertLeftSpecificTypeNotExists(object, GenericParent.class);
		assertRightSpecificTypeExists(object, GenericParent.class);
	}

	@Test
	void getsNeitherSpecificTypeFromChildWithNeither() {
		ChildWithNeither<Integer, Double> object = new ChildWithNeither<>();
		assertBothSpecificTypesNotExist(object, GenericParent.class);
	}

	@Test
	void getsBothSpecificTypesFromFinalImplementationWithDiamond() {
		FinalImplementationWithDiamond object = new FinalImplementationWithDiamond();
		assertBothSpecificTypesExist(object, GenericInterface.class);
	}

	@Test
	void getsBothSpecificTypesFromFinalImplementationWithBoth() {
		FinalImplementationWithBoth object = new FinalImplementationWithBoth();
		assertBothSpecificTypesExist(object, GenericInterface.class);
	}

	@Test
	void getsBothSpecificTypesFromFinalImplementationWithLeft() {
		FinalImplementationWithLeft object = new FinalImplementationWithLeft();
		assertBothSpecificTypesExist(object, GenericInterface.class);
	}

	@Test
	void getsBothSpecificTypesFromFinalImplementationWithRight() {
		FinalImplementationWithRight object = new FinalImplementationWithRight();
		assertBothSpecificTypesExist(object, GenericInterface.class);
	}

	@Test
	void getsBothSpecificTypesFromFinalImplementationWithNeither() {
		FinalImplementationWithNeither object = new FinalImplementationWithNeither();
		assertBothSpecificTypesExist(object, GenericInterface.class);
	}

	@Test
	void getsBothSpecificTypesFromImplementationWithDiamond() {
		ImplementationWithDiamond object = new ImplementationWithDiamond();
		assertBothSpecificTypesExist(object, GenericInterface.class);
	}

	@Test
	void getsBothSpecificTypesFromImplementationWithBoth() {
		ImplementationWithBoth object = new ImplementationWithBoth();
		assertBothSpecificTypesExist(object, GenericInterface.class);
	}

	@Test
	void getsLeftSpecificTypeFromImplementationWithLeft() {
		ImplementationWithLeft<Double> object = new ImplementationWithLeft<>();
		assertLeftSpecificTypeExists(object, GenericInterface.class);
		assertRightSpecificTypeNotExists(object, GenericInterface.class);
	}

	@Test
	void getsRightSpecificTypeFromImplementationWithRight() {
		ImplementationWithRight<Integer> object = new ImplementationWithRight<>();
		assertLeftSpecificTypeNotExists(object, GenericInterface.class);
		assertRightSpecificTypeExists(object, GenericInterface.class);
	}

	@Test
	void getsNeitherSpecificTypeFromImplementationWithNeither() {
		ImplementationWithNeither<Integer, Double> object = new ImplementationWithNeither<>();
		assertBothSpecificTypesNotExist(object, GenericInterface.class);
	}

	private <T, S extends T> void assertBothSpecificTypesExist(S object, Class<T> rootType) {
		assertLeftSpecificTypeExists(object, rootType);
		assertRightSpecificTypeExists(object, rootType);
	}

	private <T, S extends T> void assertLeftSpecificTypeExists(S object, Class<T> rootType) {
		assertSpecificTypeEquals(Integer.class, object, rootType, 0);
	}

	private <T, S extends T> void assertRightSpecificTypeExists(S object, Class<T> rootType) {
		assertSpecificTypeEquals(Double.class, object, rootType, 1);
	}

	private <T, S extends T> void assertBothSpecificTypesNotExist(S object, Class<T> rootType) {
		assertLeftSpecificTypeNotExists(object, rootType);
		assertRightSpecificTypeNotExists(object, rootType);
	}

	private <T, S extends T> void assertLeftSpecificTypeNotExists(S object, Class<T> rootType) {
		assertSpecificTypeThrows(object, rootType, 0);
	}

	private <T, S extends T> void assertRightSpecificTypeNotExists(S object, Class<T> rootType) {
		assertSpecificTypeThrows(object, rootType, 1);
	}

	@Test
	void getsBothSpecificTypesFromFinalMixedWithBoth() {
		FinalMixedWithBoth object = new FinalMixedWithBoth();
		assertBothSpecificTypesExist(object);
	}

	@Test
	void getsBothSpecificTypesFromFinalMixedWithLeft() {
		FinalMixedWithLeft object = new FinalMixedWithLeft();
		assertBothSpecificTypesExist(object);
	}

	@Test
	void getsBothSpecificTypesFromFinalMixedWithRight() {
		FinalMixedWithRight object = new FinalMixedWithRight();
		assertBothSpecificTypesExist(object);
	}

	@Test
	void getsBothSpecificTypesFromFinalMixedWithNeither() {
		FinalMixedWithNeither object = new FinalMixedWithNeither();
		assertBothSpecificTypesExist(object);
	}

	@Test
	void getsBothSpecificTypesFromMixedWithBoth() {
		MixedWithBoth object = new MixedWithBoth();
		assertBothSpecificTypesExist(object);
	}

	@Test
	void getsLeftSpecificTypeFromMixedWithLeft() {
		MixedWithLeft<Integer> object = new MixedWithLeft<>();
		assertLeftSpecificTypeExists(object);
		assertRightSpecificTypeNotExists(object);
	}

	@Test
	void getsRightSpecificTypeFromMixedWithRight() {
		MixedWithRight<Integer> object = new MixedWithRight<>();
		assertLeftSpecificTypeNotExists(object);
		assertRightSpecificTypeExists(object);
	}

	@Test
	void getsNeitherSpecificTypeFromMixedWithNeither() {
		MixedWithNeither<Integer, Double> object = new MixedWithNeither<>();
		assertLeftSpecificTypeNotExists(object);
		assertRightSpecificTypeNotExists(object);
	}

	private <S extends PartialGenericParent<?> & PartialGenericInterface<?>> void assertBothSpecificTypesExist(S object) {
		assertLeftSpecificTypeExists(object);
		assertRightSpecificTypeExists(object);
	}

	private <S extends PartialGenericParent<?> & PartialGenericInterface<?>> void assertLeftSpecificTypeExists(S object) {
		assertSpecificTypeEquals(Integer.class, object, PartialGenericParent.class, 0);
	}

	private <S extends PartialGenericParent<?> & PartialGenericInterface<?>> void assertRightSpecificTypeExists(S object) {
		assertSpecificTypeEquals(Double.class, object, PartialGenericInterface.class, 0);
	}

	private <S extends PartialGenericParent<?> & PartialGenericInterface<?>> void assertLeftSpecificTypeNotExists(S object) {
		assertSpecificTypeThrows(object, PartialGenericParent.class, 0);
	}

	private <S extends PartialGenericParent<?> & PartialGenericInterface<?>> void assertRightSpecificTypeNotExists(S object) {
		assertSpecificTypeThrows(object, PartialGenericInterface.class, 0);
	}

	private <T, S extends T> void assertSpecificTypeEquals(Class<?> expected, S object, Class<T> rootType, int rootIndex) {
		assertEquals(expected, r.getSpecificType(object, rootType, rootIndex));
	}

	private <T, S extends T> void assertSpecificTypeThrows(S object, Class<T> rootType, int rootIndex) {
		assertThrows(ReflectionException.class, () -> {
			r.getSpecificType(object, rootType, rootIndex);
		});
	}
}
