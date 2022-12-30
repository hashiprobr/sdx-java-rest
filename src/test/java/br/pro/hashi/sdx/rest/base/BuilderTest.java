package br.pro.hashi.sdx.rest.base;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockConstruction;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Set;

import org.mockito.MockedConstruction;
import org.mockito.MockedStatic;

import com.google.gson.GsonBuilder;

import br.pro.hashi.sdx.rest.converter.BaseConverter;
import br.pro.hashi.sdx.rest.transformer.Transformer;

public class BuilderTest {
	private MockedConstruction<GsonBuilder> gsonBuilderConstruction;
	private MockedConstruction<Transformer> transformerConstruction;
	private Transformer transformer;
	private Builder<?> b;

	protected void mockConstructions() {
		gsonBuilderConstruction = mockConstruction(GsonBuilder.class, (mock, context) -> {
			when(mock.disableJdkUnsafe()).thenReturn(mock);
			when(mock.serializeNulls()).thenReturn(mock);
			when(mock.setPrettyPrinting()).thenReturn(mock);
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

	protected void testPutsUncheckedSerializer() {
		try (MockedStatic<Reflection> reflection = mockStatic(Reflection.class)) {
			String packageName = "br.pro.hashi.sdx.rest.converter.mock";
			BaseConverter<?, ?> converter = mock(BaseConverter.class);
			reflection.when(() -> Reflection.getSubInstances(packageName, BaseConverter.class)).thenReturn(Set.of(converter));
			b.withSerializer(packageName);
			GsonBuilder gsonBuilder = gsonBuilderConstruction.constructed().get(1);
			verify(converter).register(gsonBuilder);
		}
	}
}
