package br.pro.hashi.sdx.rest.transform.extension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.mockStatic;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import br.pro.hashi.sdx.rest.reflection.Reflection;
import br.pro.hashi.sdx.rest.transform.extension.mock.ConcreteConverter;
import br.pro.hashi.sdx.rest.transform.extension.mock.ConcreteInjector;

class InjectorTest {
	private Injector i;

	@BeforeEach
	void setUp() {
		i = new ConcreteInjector();
	}

	@Test
	void getsSubConverters() {
		try (MockedStatic<Reflection> reflection = mockStatic(Reflection.class)) {
			Converter<?, ?> converter = new ConcreteConverter();
			reflection.when(() -> Reflection.getConcreteSubTypes("package", Converter.class)).thenReturn(Set.of(ConcreteConverter.class));
			reflection.when(() -> Reflection.getNoArgsConstructor(ConcreteConverter.class)).thenReturn(null);
			reflection.when(() -> Reflection.newNoArgsInstance(null)).thenReturn(converter);
			List<Converter<?, ?>> subConverters = new ArrayList<>();
			for (Converter<?, ?> subConverter : i.getSubConverters("package", Converter.class)) {
				subConverters.add(subConverter);
			}
			assertEquals(1, subConverters.size());
			assertSame(converter, subConverters.get(0));
		}
	}
}
