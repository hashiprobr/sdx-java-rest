package br.pro.hashi.sdx.rest.base;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mockConstruction;
import static org.mockito.Mockito.when;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.Test;
import org.mockito.MockedConstruction;
import org.reflections.Reflections;

import br.pro.hashi.sdx.rest.base.exception.ReflectionException;
import br.pro.hashi.sdx.rest.base.mock.AbstractChild;
import br.pro.hashi.sdx.rest.base.mock.Base;
import br.pro.hashi.sdx.rest.base.mock.Child;
import br.pro.hashi.sdx.rest.base.mock.ChildWithBoth;
import br.pro.hashi.sdx.rest.base.mock.ChildWithLeft;
import br.pro.hashi.sdx.rest.base.mock.ChildWithNeither;
import br.pro.hashi.sdx.rest.base.mock.ChildWithRight;
import br.pro.hashi.sdx.rest.base.mock.FinalChildWithBoth;
import br.pro.hashi.sdx.rest.base.mock.FinalChildWithLeft;
import br.pro.hashi.sdx.rest.base.mock.FinalChildWithNeither;
import br.pro.hashi.sdx.rest.base.mock.FinalChildWithRight;
import br.pro.hashi.sdx.rest.base.mock.Parent;
import br.pro.hashi.sdx.rest.base.mock.WithIllegalNoArgsConstructor;
import br.pro.hashi.sdx.rest.base.mock.WithPackageNoArgsConstructor;
import br.pro.hashi.sdx.rest.base.mock.WithPrivateNoArgsConstructor;
import br.pro.hashi.sdx.rest.base.mock.WithProtectedNoArgsConstructor;
import br.pro.hashi.sdx.rest.base.mock.WithPublicNoArgsConstructor;
import br.pro.hashi.sdx.rest.base.mock.WithoutNoArgsConstructor;

class ReflectionTest {
	private MockedConstruction<Reflections> mockReflections() {
		return mockConstruction(Reflections.class, (reflections, context) -> {
			when(reflections.getSubTypesOf(Parent.class)).thenReturn(Set.of(Child.class, AbstractChild.class));
		});
	}

	private <T extends Base<?, ?>> void assertSpecificTypeEquals(Class<?> type, T child, int i) {
		assertEquals(type, Reflection.getSpecificType(Base.class, child, i));
	}

	private <T extends Base<?, ?>> void assertSpecificTypeThrows(T child, int i) {
		assertThrows(ReflectionException.class, () -> {
			Reflection.getSpecificType(Base.class, child, i);
		});
	}

	@Test
	void getsPublicNoArgsConstructor() {
		assertDoesNotThrow(() -> {
			Reflection.getNoArgsConstructor(WithPublicNoArgsConstructor.class);
		});
	}

	@Test
	void getsProtectedNoArgsConstructor() {
		assertDoesNotThrow(() -> {
			Reflection.getNoArgsConstructor(WithProtectedNoArgsConstructor.class);
		});
	}

	@Test
	void getsPackageNoArgsConstructor() {
		assertDoesNotThrow(() -> {
			Reflection.getNoArgsConstructor(WithPackageNoArgsConstructor.class);
		});
	}

	@Test
	void getsPrivateNoArgsConstructor() {
		assertDoesNotThrow(() -> {
			Reflection.getNoArgsConstructor(WithPrivateNoArgsConstructor.class);
		});
	}

	@Test
	void doesNotGetNoArgsConstructor() {
		assertThrows(ReflectionException.class, () -> {
			Reflection.getNoArgsConstructor(WithoutNoArgsConstructor.class);
		});
	}

	@Test
	void invokesNoArgsConstructor() {
		Constructor<?> constructor = Reflection.getNoArgsConstructor(WithPublicNoArgsConstructor.class);
		assertDoesNotThrow(() -> {
			Reflection.newNoArgsInstance(constructor);
		});
	}

	@Test
	void doesNotInvokeNoArgsConstructor() {
		Constructor<?> constructor = Reflection.getNoArgsConstructor(WithIllegalNoArgsConstructor.class);
		assertThrows(ReflectionException.class, () -> {
			Reflection.newNoArgsInstance(constructor);
		});
	}

	@Test
	void getsSubTypes() {
		try (MockedConstruction<Reflections> reflections = mockReflections()) {
			List<Class<? extends Parent>> subTypes = new ArrayList<>();
			for (Class<? extends Parent> subType : Reflection.getSubTypes("br.pro.hashi.sdx.base.mock", Parent.class)) {
				subTypes.add(subType);
			}
			assertEquals(1, subTypes.size());
			assertEquals(Child.class, subTypes.get(0));
		}
	}

	@Test
	void getsSubInstances() {
		try (MockedConstruction<Reflections> reflections = mockReflections()) {
			List<Parent> subInstances = new ArrayList<>();
			for (Parent subInstance : Reflection.getSubInstances("br.pro.hashi.sdx.base.mock", Parent.class)) {
				subInstances.add(subInstance);
			}
			assertEquals(1, subInstances.size());
			assertInstanceOf(Child.class, subInstances.get(0));
		}
	}

	@Test
	void getsBothSpecificTypesFromFinalChildWithBoth() {
		FinalChildWithBoth child = new FinalChildWithBoth();
		assertSpecificTypeEquals(Integer.class, child, 0);
		assertSpecificTypeEquals(Double.class, child, 1);
	}

	@Test
	void getsBothSpecificTypesFromFinalChildWithLeft() {
		FinalChildWithLeft child = new FinalChildWithLeft();
		assertSpecificTypeEquals(Integer.class, child, 0);
		assertSpecificTypeEquals(Double.class, child, 1);
	}

	@Test
	void getsBothSpecificTypesFromFinalChildWithRight() {
		FinalChildWithRight child = new FinalChildWithRight();
		assertSpecificTypeEquals(Integer.class, child, 0);
		assertSpecificTypeEquals(Double.class, child, 1);
	}

	@Test
	void getsBothSpecificTypesFromFinalChildWithNeither() {
		FinalChildWithNeither child = new FinalChildWithNeither();
		assertSpecificTypeEquals(Integer.class, child, 0);
		assertSpecificTypeEquals(Double.class, child, 1);
	}

	@Test
	void getsBothSpecificTypesFromChildWithBoth() {
		ChildWithBoth child = new ChildWithBoth();
		assertSpecificTypeEquals(Integer.class, child, 0);
		assertSpecificTypeEquals(Double.class, child, 1);
	}

	@Test
	void getsLeftSpecificTypeFromChildWithLeft() {
		ChildWithLeft<Double> child = new ChildWithLeft<>();
		assertSpecificTypeEquals(Integer.class, child, 0);
		assertSpecificTypeThrows(child, 1);
	}

	@Test
	void getsRightSpecificTypeFromChildWithRight() {
		ChildWithRight<Integer> child = new ChildWithRight<>();
		assertSpecificTypeThrows(child, 0);
		assertSpecificTypeEquals(Double.class, child, 1);
	}

	@Test
	void getsNeitherSpecificTypeFromChildWithNeither() {
		ChildWithNeither<Integer, Double> child = new ChildWithNeither<>();
		assertSpecificTypeThrows(child, 0);
		assertSpecificTypeThrows(child, 1);
	}
}
