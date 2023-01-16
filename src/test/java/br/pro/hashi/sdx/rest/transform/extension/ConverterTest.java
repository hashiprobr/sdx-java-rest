package br.pro.hashi.sdx.rest.transform.extension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mockStatic;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import br.pro.hashi.sdx.rest.reflection.Reflection;
import br.pro.hashi.sdx.rest.transform.extension.mock.ConcreteConverter;

class ConverterTest {
	private MockedStatic<Reflection> reflection;
	private Converter<?, ?> c;

	@BeforeEach
	void setUp() {
		reflection = mockStatic(Reflection.class);
	}

	@AfterEach
	void tearDown() {
		reflection.close();
	}

	@Test
	void getsSourceType() {
		reflection.when(() -> Reflection.getSpecificType(eq(Converter.class), eq(0), any())).thenReturn(Double.class);
		c = new ConcreteConverter();
		assertEquals(Double.class, c.getSourceType());
	}

	@Test
	void getsTargetType() {
		reflection.when(() -> Reflection.getSpecificType(eq(Converter.class), eq(1), any())).thenReturn(Integer.class);
		c = new ConcreteConverter();
		assertEquals(Integer.class, c.getTargetType());
	}
}
