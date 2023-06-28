package br.pro.hashi.sdx.rest.transform.extension;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodHandles.Lookup;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.MockitoAnnotations;

import br.pro.hashi.sdx.rest.reflection.Reflector;
import br.pro.hashi.sdx.rest.transform.extension.mock.ConcreteConverter;
import br.pro.hashi.sdx.rest.transform.extension.mock.ConcreteInjector;

class InjectorTest {
	private final static Lookup LOOKUP = MethodHandles.lookup();

	private AutoCloseable mocks;
	private @Mock Reflector reflector;
	private MockedStatic<Reflector> reflectorStatic;
	private Injector i;

	@BeforeEach
	<T> void setUp() {
		mocks = MockitoAnnotations.openMocks(this);

		when(reflector.getCreator(any(), any(Lookup.class))).thenAnswer((invocation) -> {
			Class<T> type = invocation.getArgument(0);
			Constructor<T> constructor = type.getDeclaredConstructor();
			return LOOKUP.unreflectConstructor(constructor);
		});
		when(reflector.invokeCreator(any(MethodHandle.class))).thenAnswer((invocation) -> {
			MethodHandle creator = invocation.getArgument(0);
			return creator.invoke();
		});

		reflectorStatic = mockStatic(Reflector.class);
		reflectorStatic.when(() -> Reflector.getInstance()).thenReturn(reflector);

		i = new ConcreteInjector();
	}

	@AfterEach
	void tearDown() {
		reflectorStatic.close();
		assertDoesNotThrow(() -> {
			mocks.close();
		});
	}

	@Test
	void getsSubConverters() {
		String packageName = "package";
		when(reflector.getConcreteSubTypes(packageName, Converter.class)).thenReturn(Set.of(ConcreteConverter.class));
		List<Converter<?, ?>> converters = new ArrayList<>();
		i.getSubConverters(packageName, Converter.class, LOOKUP).forEach(converters::add);
		assertEquals(1, converters.size());
		assertInstanceOf(ConcreteConverter.class, converters.get(0));
	}
}
