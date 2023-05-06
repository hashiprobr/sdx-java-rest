package br.pro.hashi.sdx.rest.transform.extension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import br.pro.hashi.sdx.rest.reflection.Reflector;
import br.pro.hashi.sdx.rest.transform.extension.mock.ConcreteConverter;

class ConverterTest {
	private Reflector reflector;
	private MockedStatic<Reflector> reflectorStatic;
	private Converter<?, ?> c;

	@BeforeEach
	void setUp() {
		reflector = mock(Reflector.class);
		reflectorStatic = mockStatic(Reflector.class);
		reflectorStatic.when(() -> Reflector.getInstance()).thenReturn(reflector);
	}

	@AfterEach
	void tearDown() {
		reflectorStatic.close();
	}

	@Test
	void getsSourceType() {
		when(reflector.getSpecificType(any(), eq(Converter.class), eq(0))).thenReturn(Double.class);
		c = new ConcreteConverter();
		assertEquals(Double.class, c.getSourceType());
	}

	@Test
	void getsTargetType() {
		when(reflector.getSpecificType(any(), eq(Converter.class), eq(1))).thenReturn(Integer.class);
		c = new ConcreteConverter();
		assertEquals(Integer.class, c.getTargetType());
	}
}
