package br.pro.hashi.sdx.rest.transformer;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.times;

import java.math.BigInteger;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import com.google.gson.Gson;

import br.pro.hashi.sdx.rest.base.Reflection;
import br.pro.hashi.sdx.rest.transformer.base.Deserializer;

class SafeGsonDeserializerTest {
	private MockedStatic<Reflection> reflection;
	private Deserializer d;
	private String content;

	@BeforeEach
	void setUp() {
		reflection = mockStatic(Reflection.class);
		d = new SafeGsonDeserializer(mock(Gson.class));
		content = "content";
	}

	@AfterEach
	void tearDown() {
		reflection.close();
	}

	@Test
	void addsObjectOnlyOnce() {
		assertDoesNotThrow(() -> {
			d.deserialize(content, Object.class);
			d.deserialize(content, Object.class);
		});
		reflection.verify(() -> Reflection.getNoArgsConstructor(Object.class), times(1));
	}

	@Test
	void doesNotAddPrimitive() {
		assertDoesNotThrow(() -> {
			d.deserialize(content, int.class);
		});
		reflection.verify(() -> Reflection.getNoArgsConstructor(int.class), times(0));
	}

	@Test
	void doesNotAddWrapper() {
		assertDoesNotThrow(() -> {
			d.deserialize(content, Integer.class);
		});
		reflection.verify(() -> Reflection.getNoArgsConstructor(Integer.class), times(0));
	}

	@Test
	void doesNotAddBig() {
		assertDoesNotThrow(() -> {
			d.deserialize(content, BigInteger.class);
		});
		reflection.verify(() -> Reflection.getNoArgsConstructor(BigInteger.class), times(0));
	}
}
