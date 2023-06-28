package br.pro.hashi.sdx.rest.transform.extension;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.MockitoAnnotations;

import br.pro.hashi.sdx.rest.reflection.Reflector;
import br.pro.hashi.sdx.rest.transform.extension.mock.ConcreteConverter;

class ConverterTest {
	private AutoCloseable mocks;
	private @Mock Reflector reflector;
	private MockedStatic<Reflector> reflectorStatic;
	private Converter<?, ?> c;

	@BeforeEach
	void setUp() {
		mocks = MockitoAnnotations.openMocks(this);

		reflectorStatic = mockStatic(Reflector.class);
		reflectorStatic.when(() -> Reflector.getInstance()).thenReturn(reflector);

		c = new ConcreteConverter();
	}

	@AfterEach
	void tearDown() {
		reflectorStatic.close();
		assertDoesNotThrow(() -> {
			mocks.close();
		});
	}

	@Test
	void getsSourceType() {
		when(reflector.getSpecificType(c, Converter.class, 0)).thenReturn(Integer.class);
		assertEquals(Integer.class, c.getSourceType());
	}

	@Test
	void getsTargetType() {
		when(reflector.getSpecificType(c, Converter.class, 1)).thenReturn(Double.class);
		assertEquals(Double.class, c.getTargetType());
	}
}
