package br.pro.hashi.sdx.rest.transform.extension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import br.pro.hashi.sdx.rest.reflection.Reflector;
import br.pro.hashi.sdx.rest.transform.extension.mock.ConcreteConverter;
import br.pro.hashi.sdx.rest.transform.extension.mock.ConcreteInjector;

class InjectorTest {
	@Test
	void getsSubConverters() {
		try (MockedStatic<Reflector> reflectorStatic = mockStatic(Reflector.class)) {
			String packageName = "package";
			Converter<?, ?> converter = new ConcreteConverter();
			Reflector reflector = mock(Reflector.class);
			when(reflector.getConcreteSubTypes(packageName, Converter.class)).thenReturn(Set.of(ConcreteConverter.class));
			when(reflector.getNoArgsConstructor(ConcreteConverter.class)).thenReturn(null);
			when(reflector.newNoArgsInstance(null)).thenReturn(converter);
			reflectorStatic.when(() -> Reflector.getInstance()).thenReturn(reflector);
			Injector i = new ConcreteInjector();
			List<Converter<?, ?>> subConverters = new ArrayList<>();
			i.getSubConverters(packageName, Converter.class).forEach(subConverters::add);
			assertEquals(1, subConverters.size());
			assertSame(converter, subConverters.get(0));
		}
	}
}
