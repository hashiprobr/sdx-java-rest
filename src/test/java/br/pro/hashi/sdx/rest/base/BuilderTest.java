package br.pro.hashi.sdx.rest.base;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockConstruction;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.nio.charset.StandardCharsets;
import java.util.Objects;
import java.util.Set;

import org.mockito.MockedConstruction;
import org.mockito.MockedStatic;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import br.pro.hashi.sdx.rest.converter.BaseConverter;
import br.pro.hashi.sdx.rest.transformer.Transformer;
import br.pro.hashi.sdx.rest.transformer.base.Assembler;
import br.pro.hashi.sdx.rest.transformer.base.Deserializer;
import br.pro.hashi.sdx.rest.transformer.base.Disassembler;
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

	protected void testInitializesWithURLCharsetUTF8() {
		b.urlCharset.equals(StandardCharsets.UTF_8);
	}

	protected void testInitializesWithoutNullBody() {
		Objects.equals(b.none, null);
	}

	protected void testInitializesWithoutRedirection() {
		assertFalse(b.redirection);
	}

	protected void testInitializesWithCompression() {
		assertTrue(b.compression);
	}

	protected void testAddsBinary() {
		b.withBinary(Object.class);
		verify(transformer).addBinary(Object.class);
	}

	protected void testPutsAssembler() {
		String contentType = "image/png";
		Assembler assembler = mock(Assembler.class);
		b.withAssembler(contentType, assembler);
		verify(transformer).putAssembler(contentType, assembler);
	}

	protected void testRemovesAssembler() {
		String contentType = "image/png";
		b.withoutAssembler(contentType);
		verify(transformer).removeAssembler(contentType);
	}

	protected void testPutsDisassembler() {
		String contentType = "image/png";
		Disassembler disassembler = mock(Disassembler.class);
		b.withDisassembler(contentType, disassembler);
		verify(transformer).putDisassembler(contentType, disassembler);
	}

	protected void testRemovesDisassembler() {
		String contentType = "image/png";
		b.withoutDisassembler(contentType);
		verify(transformer).removeDisassembler(contentType);
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
		verify(transformer, times(0)).putUncheckedSerializer(any());
	}

	protected void testDoesNotPutUncheckedSerializerIfBlank() {
		assertThrows(IllegalArgumentException.class, () -> {
			b.withSerializer(" \t\n");
		});
		verify(transformer, times(0)).putUncheckedSerializer(any());
	}

	protected void testRemovesSerializer() {
		String contentType = "application/xml";
		b.withoutSerializer(contentType);
		verify(transformer).removeSerializer(contentType);
	}

	protected void testPutsDeserializer() {
		String contentType = "application/xml";
		Deserializer deserializer = mock(Deserializer.class);
		b.withDeserializer(contentType, deserializer);
		verify(transformer).putDeserializer(contentType, deserializer);
	}

	protected void testPutsGsonDeserializer() {
		Gson gson = mock(Gson.class);
		b.withDeserializer(gson);
		verify(transformer).putDeserializer(gson);
	}

	protected void testPutsSafeDeserializer() {
		try (MockedStatic<Reflection> reflection = mockStatic(Reflection.class)) {
			String packageName = "br.pro.hashi.sdx.rest.converter.mock";
			BaseConverter<?, ?> converter = mock(BaseConverter.class);
			reflection.when(() -> Reflection.getSubInstances(packageName, BaseConverter.class)).thenReturn(Set.of(converter));
			b.withDeserializer(packageName);
			GsonBuilder gsonBuilder = gsonBuilderConstruction.constructed().get(1);
			verify(converter).register(gsonBuilder);
			verify(transformer).putSafeDeserializer(defaultGson);
		}
	}

	protected void testDoesNotPutSafeDeserializerIfNull() {
		assertThrows(IllegalArgumentException.class, () -> {
			b.withDeserializer((String) null);
		});
		verify(transformer, times(0)).putSafeDeserializer(any());
	}

	protected void testDoesNotPutSafeDeserializerIfBlank() {
		assertThrows(IllegalArgumentException.class, () -> {
			b.withDeserializer(" \t\n");
		});
		verify(transformer, times(0)).putSafeDeserializer(any());
	}

	protected void testRemovesDeserializer() {
		String contentType = "application/xml";
		b.withoutDeserializer(contentType);
		verify(transformer).removeDeserializer(contentType);
	}

	protected void testSetsURLCharset() {
		b.withURLCharset(StandardCharsets.ISO_8859_1);
		b.urlCharset.equals(StandardCharsets.ISO_8859_1);
	}

	protected void testDoesNotSetURLCharset() {
		assertThrows(IllegalArgumentException.class, () -> {
			b.withURLCharset(null);
		});
		b.urlCharset.equals(StandardCharsets.UTF_8);
	}

	protected void testSetsNullBody() {
		b.withNullBody();
		Objects.equals(b.none, "");
	}

	protected void testSetsRedirection() {
		b.withRedirection();
		assertTrue(b.redirection);
	}

	protected void testSetsCompression() {
		b.withoutCompression();
		assertFalse(b.compression);
	}
}
