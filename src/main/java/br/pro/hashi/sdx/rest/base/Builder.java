package br.pro.hashi.sdx.rest.base;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import br.pro.hashi.sdx.rest.converter.BaseConverter;
import br.pro.hashi.sdx.rest.transformer.Transformer;

/**
 * @hidden
 */
public abstract class Builder<T extends Builder<T>> {
	private Transformer transformer;

	protected Builder() {
		Gson gson = newGsonBuilder().create();
		this.transformer = new Transformer(gson);
	}

	/**
	 * @hidden
	 */
	protected GsonBuilder newGsonBuilder() {
		return new GsonBuilder()
				.disableJdkUnsafe()
				.serializeNulls()
				.setPrettyPrinting();
	}

	protected abstract T self();

	/**
	 * Adds...
	 * 
	 * @param args args
	 * @return return
	 */
	public T withBinary(Class<?>... args) {
		for (Class<?> type : args) {
			transformer.addBinary(type);
		}
		return self();
	}

	/**
	 * Puts...
	 * 
	 * @param packageName packageName
	 * @return return
	 */
	public T withSerializer(String packageName) {
		GsonBuilder builder = newGsonBuilder();
		for (BaseConverter<?, ?> converter : Reflection.getSubInstances(packageName, BaseConverter.class)) {
			converter.register(builder);
		}
		Gson gson = builder.create();
		transformer.putUncheckedSerializer(gson);
		return self();
	}
}
