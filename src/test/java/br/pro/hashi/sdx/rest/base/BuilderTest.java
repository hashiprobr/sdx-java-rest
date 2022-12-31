package br.pro.hashi.sdx.rest.base;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockConstruction;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Set;

import org.mockito.MockedConstruction;
import org.mockito.MockedStatic;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import br.pro.hashi.sdx.rest.converter.BaseConverter;
import br.pro.hashi.sdx.rest.transformer.Transformer;
import br.pro.hashi.sdx.rest.transformer.base.Serializer;

public class BuilderTest {
	private Gson defaultGson;
	private MockedConstruction<GsonBuilder> gsonBuilderConstruction;
	private MockedConstruction<Transformer> transformerConstruction;
	private Transformer transformer;
	private Builder<?> b;

	protected void mockConstructions() {
		defaultGson = mock(Gson.class);
		gsonBuilderConstruction = mockConstruction(GsonBuilder.class, (gsonBuilder, context) -> {
			when(gsonBuilder.disableJdkUnsafe()).thenReturn(gsonBuilder);
			when(gsonBuilder.disableHtmlEscaping()).thenReturn(gsonBuilder);
			when(gsonBuilder.serializeNulls()).thenReturn(gsonBuilder);
			when(gsonBuilder.setPrettyPrinting()).thenReturn(gsonBuilder);
			when(gsonBuilder.create()).thenReturn(defaultGson);
		});
		transformerConstruction = mockConstruction(Transformer.class);
	}

	protected void getMocks() {
		transformer = transformerConstruction.constructed().get(0);
	}

	protected void setBuilder(Builder<?> builder) {
		b = builder;
	}

	protected void closeConstructions() {
		transformerConstruction.close();
		gsonBuilderConstruction.close();
	}

	protected void testAddsBinary() {
		b.withBinary(Object.class);
		verify(transformer).addBinary(Object.class);
	}

	protected void testPutsSerializer() {
		String contentType = "application/xml";
		Serializer serializer = mock(Serializer.class);
		b.withSerializer(contentType, serializer);
		verify(transformer).putSerializer(contentType, serializer);
	}

	protected void testPutsGsonSerializer() {
		Gson gson = mock(Gson.class);
		b.withSerializer(gson);
		verify(transformer).putSerializer(gson);
	}

	protected void testPutsUncheckedSerializer() {
		try (MockedStatic<Reflection> reflection = mockStatic(Reflection.class)) {
			String packageName = "br.pro.hashi.sdx.rest.converter.mock";
			BaseConverter<?, ?> converter = mock(BaseConverter.class);
			reflection.when(() -> Reflection.getSubInstances(packageName, BaseConverter.class)).thenReturn(Set.of(converter));
			b.withSerializer(packageName);
			GsonBuilder gsonBuilder = gsonBuilderConstruction.constructed().get(1);
			verify(converter).register(gsonBuilder);
			verify(transformer).putUncheckedSerializer(defaultGson);
		}
	}

	protected void testDoesNotPutUncheckedSerializerIfNull() {
		assertThrows(IllegalArgumentException.class, () -> {
			b.withSerializer((String) null);
		});
	}

	protected void testDoesNotPutUncheckedSerializerIfBlank() {
		assertThrows(IllegalArgumentException.class, () -> {
			b.withSerializer(" \t\n");
		});
	}

	protected void testRemovesSerializer() {
		String contentType = "application/xml";
		b.withoutSerializer(contentType);
		verify(transformer).removeSerializer(contentType);
	}
}
