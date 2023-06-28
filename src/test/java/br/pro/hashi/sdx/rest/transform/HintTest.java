package br.pro.hashi.sdx.rest.transform;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.MockitoAnnotations;

import br.pro.hashi.sdx.rest.reflection.Reflector;
import br.pro.hashi.sdx.rest.transform.mock.ConcreteHint;

class HintTest {
	private AutoCloseable mocks;
	private @Mock Reflector reflector;
	private MockedStatic<Reflector> reflectorStatic;
	private Hint<?> h;

	@BeforeEach
	void setUp() {
		mocks = MockitoAnnotations.openMocks(this);

		reflectorStatic = mockStatic(Reflector.class);
		reflectorStatic.when(() -> Reflector.getInstance()).thenReturn(reflector);
	}

	@AfterEach
	void tearDown() {
		reflectorStatic.close();
		assertDoesNotThrow(() -> {
			mocks.close();
		});
	}

	@Test
	void getsType() {
		when(reflector.getSpecificType(any(ConcreteHint.class), eq(Hint.class), eq(0))).thenReturn(String.class);
		h = new ConcreteHint();
		assertEquals(String.class, h.getType());
	}
}
