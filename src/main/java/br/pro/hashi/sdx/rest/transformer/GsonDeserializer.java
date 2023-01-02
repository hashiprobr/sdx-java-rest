package br.pro.hashi.sdx.rest.transformer;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

import br.pro.hashi.sdx.rest.transformer.base.Deserializer;
import br.pro.hashi.sdx.rest.transformer.exception.DeserializingException;

class GsonDeserializer implements Deserializer {
	private final Gson gson;

	GsonDeserializer(Gson gson) {
		this.gson = gson;
	}

	@Override
	public <T> T deserialize(String content, Class<T> type) throws DeserializingException {
		T body;
		try {
			body = gson.fromJson(content, type);
		} catch (JsonSyntaxException exception) {
			throw new DeserializingException(exception);
		}
		return body;
	}
}
