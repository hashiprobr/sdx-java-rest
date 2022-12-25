package br.pro.hashi.sdx.rest.transformer;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

import br.pro.hashi.sdx.rest.transformer.base.Deserializer;
import br.pro.hashi.sdx.rest.transformer.exception.DeserializingException;

class GsonDeserializerTest {
	private Gson gson;
	private Deserializer d;
	private String content;
	private Class<Object> type;

	@BeforeEach
	void setUp() {
		gson = mock(Gson.class);
		d = new GsonDeserializer(gson);
		content = "";
		type = Object.class;
	}

	@Test
	void gsonDoesNotThrow() {
		Object expected = new Object();
		when(gson.fromJson(content, type)).thenReturn(expected);
		Object actual = assertDoesNotThrow(() -> {
			return d.deserialize(content, type);
		});
		verify(gson).fromJson(content, type);
		assertSame(expected, actual);
	}

	@Test
	void gsonThrows() {
		when(gson.fromJson(content, type)).thenThrow(JsonSyntaxException.class);
		assertThrows(DeserializingException.class, () -> {
			d.deserialize(content, type);
		});
		verify(gson).fromJson(content, type);
	}
}
