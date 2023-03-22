package br.pro.hashi.sdx.rest.reflection;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mockConstruction;
import static org.mockito.Mockito.when;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.MockedConstruction;
import org.reflections.Reflections;

import br.pro.hashi.sdx.rest.reflection.exception.ReflectionException;
import br.pro.hashi.sdx.rest.reflection.mock.concrete.AbstractChild;
import br.pro.hashi.sdx.rest.reflection.mock.concrete.Child;
import br.pro.hashi.sdx.rest.reflection.mock.concrete.Parent;
import br.pro.hashi.sdx.rest.reflection.mock.noargs.WithAbstractConstructor;
import br.pro.hashi.sdx.rest.reflection.mock.noargs.WithInvalidConstructor;
import br.pro.hashi.sdx.rest.reflection.mock.noargs.WithPackageConstructor;
import br.pro.hashi.sdx.rest.reflection.mock.noargs.WithPrivateConstructor;
import br.pro.hashi.sdx.rest.reflection.mock.noargs.WithProtectedConstructor;
import br.pro.hashi.sdx.rest.reflection.mock.noargs.WithPublicConstructor;
import br.pro.hashi.sdx.rest.reflection.mock.noargs.WithoutConstructor;
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

class ReflectionTest {
	@ParameterizedTest
	@ValueSource(classes = {
			WithPublicConstructor.class,
			WithProtectedConstructor.class,
			WithPackageConstructor.class,
			WithPrivateConstructor.class })
	void getsAndCallsNoArgsConstructor(Class<?> type) {
		Constructor<?> constructor = assertDoesNotThrow(() -> {
			return Reflection.getNoArgsConstructor(type);
		});
		assertDoesNotThrow(() -> {
			Reflection.newNoArgsInstance(constructor);
		});
	}

	@Test
	void doesNotGetNoArgsConstructor() {
		assertThrows(ReflectionException.class, () -> {
			Reflection.getNoArgsConstructor(WithoutConstructor.class);
		});
	}

	@Test
	void doesNotCallNoArgsConstructorIfConstructorThrows() {
		Constructor<WithInvalidConstructor> constructor = assertDoesNotThrow(() -> {
			return Reflection.getNoArgsConstructor(WithInvalidConstructor.class);
		});
		assertThrows(ReflectionException.class, () -> {
			Reflection.newNoArgsInstance(constructor);
		});
	}

	@Test
	void doesNotCallNoArgsConstructorIfConstructorIsInaccessible() throws NoSuchMethodException {
		Constructor<WithPrivateConstructor> constructor = WithPrivateConstructor.class.getDeclaredConstructor();
		assertThrows(ReflectionException.class, () -> {
			Reflection.newNoArgsInstance(constructor);
		});
	}

	@Test
	void doesNotCallNoArgsConstructorIfConstructorIsAbstract() {
		Constructor<WithAbstractConstructor> constructor = assertDoesNotThrow(() -> {
			return Reflection.getNoArgsConstructor(WithAbstractConstructor.class);
		});
		assertThrows(ReflectionException.class, () -> {
			Reflection.newNoArgsInstance(constructor);
		});
	}

	@Test
	void getsConcreteSubTypes() {
		try (MockedConstruction<Reflections> construction = mockReflectionsConstruction()) {
			List<Class<? extends Parent>> subTypes = new ArrayList<>();
			for (Class<? extends Parent> subType : Reflection.getConcreteSubTypes("package", Parent.class)) {
				subTypes.add(subType);
			}
			assertEquals(1, subTypes.size());
			assertEquals(Child.class, subTypes.get(0));
		}
	}

	private MockedConstruction<Reflections> mockReflectionsConstruction() {
		return mockConstruction(Reflections.class, (reflections, context) -> {
			when(reflections.getSubTypesOf(Parent.class)).thenReturn(Set.of(Child.class, AbstractChild.class));
		});
	}

	@Test
	void getsBothSpecificTypesFromFinalChildWithBoth() {
		assertBothSpecificTypesCorrect(GenericParent.class, new FinalChildWithBoth());
	}

	@Test
	void getsBothSpecificTypesFromFinalChildWithLeft() {
		assertBothSpecificTypesCorrect(GenericParent.class, new FinalChildWithLeft());
	}

	@Test
	void getsBothSpecificTypesFromFinalChildWithRight() {
		assertBothSpecificTypesCorrect(GenericParent.class, new FinalChildWithRight());
	}

	@Test
	void getsBothSpecificTypesFromFinalChildWithNeither() {
		assertBothSpecificTypesCorrect(GenericParent.class, new FinalChildWithNeither());
	}

	@Test
	void getsBothSpecificTypesFromChildWithBoth() {
		assertBothSpecificTypesCorrect(GenericParent.class, new ChildWithBoth());
	}

	@Test
	void getsLeftSpecificTypeFromChildWithLeft() {
		ChildWithLeft<Double> child = new ChildWithLeft<>();
		assertLeftSpecificTypeCorrect(GenericParent.class, child);
		assertRightSpecificTypeInvalid(GenericParent.class, child);
	}

	@Test
	void getsRightSpecificTypeFromChildWithRight() {
		ChildWithRight<Integer> child = new ChildWithRight<>();
		assertLeftSpecificTypeInvalid(GenericParent.class, child);
		assertRightSpecificTypeCorrect(GenericParent.class, child);
	}

	@Test
	void getsNeitherSpecificTypeFromChildWithNeither() {
		ChildWithNeither<Integer, Double> child = new ChildWithNeither<>();
		assertLeftSpecificTypeInvalid(GenericParent.class, child);
		assertRightSpecificTypeInvalid(GenericParent.class, child);
	}

	@Test
	void getsBothSpecificTypesFromFinalImplementationWithBoth() {
		assertBothSpecificTypesCorrect(GenericInterface.class, new FinalImplementationWithBoth());
	}

	@Test
	void getsBothSpecificTypesFromFinalImplementationWithDiamond() {
		assertBothSpecificTypesCorrect(GenericInterface.class, new FinalImplementationWithDiamond());
	}

	@Test
	void getsBothSpecificTypesFromFinalImplementationWithLeft() {
		assertBothSpecificTypesCorrect(GenericInterface.class, new FinalImplementationWithLeft());
	}

	@Test
	void getsBothSpecificTypesFromFinalImplementationWithRight() {
		assertBothSpecificTypesCorrect(GenericInterface.class, new FinalImplementationWithRight());
	}

	@Test
	void getsBothSpecificTypesFromFinalImplementationWithNeither() {
		assertBothSpecificTypesCorrect(GenericInterface.class, new FinalImplementationWithNeither());
	}

	@Test
	void getsBothSpecificTypesFromImplementationWithBoth() {
		assertBothSpecificTypesCorrect(GenericInterface.class, new ImplementationWithBoth());
	}

	@Test
	void getsBothSpecificTypesFromImplementationWithDiamond() {
		assertBothSpecificTypesCorrect(GenericInterface.class, new ImplementationWithDiamond());
	}

	@Test
	void getsLeftSpecificTypeFromImplementationWithLeft() {
		ImplementationWithLeft<Double> implementation = new ImplementationWithLeft<>();
		assertLeftSpecificTypeCorrect(GenericInterface.class, implementation);
		assertRightSpecificTypeInvalid(GenericInterface.class, implementation);
	}

	@Test
	void getsRightSpecificTypeFromImplementationWithRight() {
		ImplementationWithRight<Integer> implementation = new ImplementationWithRight<>();
		assertLeftSpecificTypeInvalid(GenericInterface.class, implementation);
		assertRightSpecificTypeCorrect(GenericInterface.class, implementation);
	}

	@Test
	void getsNeitherSpecificTypeFromImplementationWithNeither() {
		ImplementationWithNeither<Integer, Double> implementation = new ImplementationWithNeither<>();
		assertLeftSpecificTypeInvalid(GenericInterface.class, implementation);
		assertRightSpecificTypeInvalid(GenericInterface.class, implementation);
	}

	private <T, S extends T> void assertBothSpecificTypesCorrect(Class<T> rootType, S object) {
		assertLeftSpecificTypeCorrect(rootType, object);
		assertRightSpecificTypeCorrect(rootType, object);
	}

	private <T, S extends T> void assertLeftSpecificTypeCorrect(Class<T> rootType, S object) {
		assertSpecificTypeEquals(Integer.class, rootType, 0, object);
	}

	private <T, S extends T> void assertRightSpecificTypeCorrect(Class<T> rootType, S object) {
		assertSpecificTypeEquals(Double.class, rootType, 1, object);
	}

	private <T, S extends T> void assertLeftSpecificTypeInvalid(Class<T> rootType, S object) {
		assertSpecificTypeThrows(rootType, 0, object);
	}

	private <T, S extends T> void assertRightSpecificTypeInvalid(Class<T> rootType, S object) {
		assertSpecificTypeThrows(rootType, 1, object);
	}

	@Test
	void getsBothSpecificTypesFromFinalMixedWithBoth() {
		assertBothSpecificTypesCorrect(new FinalMixedWithBoth());
	}

	@Test
	void getsBothSpecificTypesFromFinalMixedWithLeft() {
		assertBothSpecificTypesCorrect(new FinalMixedWithLeft());
	}

	@Test
	void getsBothSpecificTypesFromFinalMixedWithRight() {
		assertBothSpecificTypesCorrect(new FinalMixedWithRight());
	}

	@Test
	void getsBothSpecificTypesFromFinalMixedWithNeither() {
		assertBothSpecificTypesCorrect(new FinalMixedWithNeither());
	}

	@Test
	void getsBothSpecificTypesFromMixedWithBoth() {
		assertBothSpecificTypesCorrect(new MixedWithBoth());
	}

	@Test
	void getsLeftSpecificTypeFromMixedWithLeft() {
		MixedWithLeft<Integer> mixed = new MixedWithLeft<>();
		assertLeftSpecificTypeCorrect(mixed);
		assertRightSpecificTypeInvalid(mixed);
	}

	@Test
	void getsRightSpecificTypeFromMixedWithRight() {
		MixedWithRight<Integer> mixed = new MixedWithRight<>();
		assertLeftSpecificTypeInvalid(mixed);
		assertRightSpecificTypeCorrect(mixed);
	}

	@Test
	void getsNeitherSpecificTypeFromMixedWithNeither() {
		MixedWithNeither<Integer, Double> mixed = new MixedWithNeither<>();
		assertLeftSpecificTypeInvalid(mixed);
		assertRightSpecificTypeInvalid(mixed);
	}

	private <S extends PartialGenericParent<?> & PartialGenericInterface<?>> void assertBothSpecificTypesCorrect(S mixed) {
		assertLeftSpecificTypeCorrect(mixed);
		assertRightSpecificTypeCorrect(mixed);
	}

	private <S extends PartialGenericParent<?> & PartialGenericInterface<?>> void assertLeftSpecificTypeCorrect(S mixed) {
		assertSpecificTypeEquals(Integer.class, PartialGenericParent.class, 0, mixed);
	}

	private <S extends PartialGenericParent<?> & PartialGenericInterface<?>> void assertRightSpecificTypeCorrect(S mixed) {
		assertSpecificTypeEquals(Double.class, PartialGenericInterface.class, 0, mixed);
	}

	private <S extends PartialGenericParent<?> & PartialGenericInterface<?>> void assertLeftSpecificTypeInvalid(S mixed) {
		assertSpecificTypeThrows(PartialGenericParent.class, 0, mixed);
	}

	private <S extends PartialGenericParent<?> & PartialGenericInterface<?>> void assertRightSpecificTypeInvalid(S mixed) {
		assertSpecificTypeThrows(PartialGenericInterface.class, 0, mixed);
	}

	private <T, S extends T> void assertSpecificTypeEquals(Class<?> type, Class<T> rootType, int rootIndex, S object) {
		assertEquals(type, Reflection.getSpecificType(rootType, rootIndex, object));
	}

	private <T, S extends T> void assertSpecificTypeThrows(Class<T> rootType, int rootIndex, S object) {
		assertThrows(ReflectionException.class, () -> {
			Reflection.getSpecificType(rootType, rootIndex, object);
		});
	}
}
