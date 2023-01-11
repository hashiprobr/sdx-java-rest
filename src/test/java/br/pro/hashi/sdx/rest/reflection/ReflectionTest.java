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
import br.pro.hashi.sdx.rest.reflection.mock.AbstractChild;
import br.pro.hashi.sdx.rest.reflection.mock.Child;
import br.pro.hashi.sdx.rest.reflection.mock.ChildWithBoth;
import br.pro.hashi.sdx.rest.reflection.mock.ChildWithLeft;
import br.pro.hashi.sdx.rest.reflection.mock.ChildWithNeither;
import br.pro.hashi.sdx.rest.reflection.mock.ChildWithRight;
import br.pro.hashi.sdx.rest.reflection.mock.FinalChildWithBoth;
import br.pro.hashi.sdx.rest.reflection.mock.FinalChildWithLeft;
import br.pro.hashi.sdx.rest.reflection.mock.FinalChildWithNeither;
import br.pro.hashi.sdx.rest.reflection.mock.FinalChildWithRight;
import br.pro.hashi.sdx.rest.reflection.mock.GenericParent;
import br.pro.hashi.sdx.rest.reflection.mock.Parent;
import br.pro.hashi.sdx.rest.reflection.mock.WithInvalidNoArgsConstructor;
import br.pro.hashi.sdx.rest.reflection.mock.WithPackageNoArgsConstructor;
import br.pro.hashi.sdx.rest.reflection.mock.WithPrivateNoArgsConstructor;
import br.pro.hashi.sdx.rest.reflection.mock.WithProtectedNoArgsConstructor;
import br.pro.hashi.sdx.rest.reflection.mock.WithPublicNoArgsConstructor;
import br.pro.hashi.sdx.rest.reflection.mock.WithoutNoArgsConstructor;

class ReflectionTest {
	@ParameterizedTest
	@ValueSource(classes = {
			WithPublicNoArgsConstructor.class,
			WithProtectedNoArgsConstructor.class,
			WithPackageNoArgsConstructor.class,
			WithPrivateNoArgsConstructor.class })
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
			Reflection.getNoArgsConstructor(WithoutNoArgsConstructor.class);
		});
	}

	@Test
	void doesNotCallNoArgsConstructor() {
		Constructor<?> constructor = assertDoesNotThrow(() -> {
			return Reflection.getNoArgsConstructor(WithInvalidNoArgsConstructor.class);
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
		assertBothSpecificTypesCorrect(new FinalChildWithBoth());
	}

	@Test
	void getsBothSpecificTypesFromFinalChildWithLeft() {
		assertBothSpecificTypesCorrect(new FinalChildWithLeft());
	}

	@Test
	void getsBothSpecificTypesFromFinalChildWithRight() {
		assertBothSpecificTypesCorrect(new FinalChildWithRight());
	}

	@Test
	void getsBothSpecificTypesFromFinalChildWithNeither() {
		assertBothSpecificTypesCorrect(new FinalChildWithNeither());
	}

	@Test
	void getsBothSpecificTypesFromChildWithBoth() {
		assertBothSpecificTypesCorrect(new ChildWithBoth());
	}

	private <S extends GenericParent<?, ?>> void assertBothSpecificTypesCorrect(S child) {
		assertLeftSpecificTypeCorrect(child);
		assertRightSpecificTypeCorrect(child);
	}

	@Test
	void getsLeftSpecificTypeFromChildWithLeft() {
		ChildWithLeft<Double> child = new ChildWithLeft<>();
		assertLeftSpecificTypeCorrect(child);
		assertRightSpecificTypeInvalid(child);
	}

	@Test
	void getsRightSpecificTypeFromChildWithRight() {
		ChildWithRight<Integer> child = new ChildWithRight<>();
		assertLeftSpecificTypeInvalid(child);
		assertRightSpecificTypeCorrect(child);
	}

	@Test
	void getsNeitherSpecificTypeFromChildWithNeither() {
		ChildWithNeither<Integer, Double> child = new ChildWithNeither<>();
		assertLeftSpecificTypeInvalid(child);
		assertRightSpecificTypeInvalid(child);
	}

	private <S extends GenericParent<?, ?>> void assertLeftSpecificTypeCorrect(S child) {
		assertSpecificTypeEquals(Integer.class, child, 0);
	}

	private <S extends GenericParent<?, ?>> void assertRightSpecificTypeCorrect(S child) {
		assertSpecificTypeEquals(Double.class, child, 1);
	}

	private <S extends GenericParent<?, ?>> void assertLeftSpecificTypeInvalid(S child) {
		assertSpecificTypeThrows(child, 0);
	}

	private <S extends GenericParent<?, ?>> void assertRightSpecificTypeInvalid(S child) {
		assertSpecificTypeThrows(child, 1);
	}

	private <S extends GenericParent<?, ?>> void assertSpecificTypeEquals(Class<?> type, S child, int i) {
		assertEquals(type, Reflection.getSpecificType(GenericParent.class, child, i));
	}

	private <S extends GenericParent<?, ?>> void assertSpecificTypeThrows(S child, int i) {
		assertThrows(ReflectionException.class, () -> {
			Reflection.getSpecificType(GenericParent.class, child, i);
		});
	}
}
