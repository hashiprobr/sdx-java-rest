package br.pro.hashi.sdx.rest.transformer;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.google.gson.Gson;

import br.pro.hashi.sdx.rest.transformer.base.Serializer;

class GsonSerializerTest {
	private Gson gson;
	private Serializer s;

	@BeforeEach
	void setUp() {
		gson = mock(Gson.class);
		s = new GsonSerializer(gson);
	}

	@Test
	void gsonReturns() {
		Object body = new Object();
		String expected = "";
		when(gson.toJson(body)).thenReturn(expected);
		String actual = s.serialize(body);
		verify(gson).toJson(body);
		assertSame(expected, actual);
	}
}
