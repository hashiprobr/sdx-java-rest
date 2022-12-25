package br.pro.hashi.sdx.rest.transformer;

import com.google.gson.Gson;

import br.pro.hashi.sdx.rest.transformer.base.Serializer;

class GsonSerializer implements Serializer {
	private final Gson gson;

	public GsonSerializer(Gson gson) {
		this.gson = gson;
	}

	@Override
	public String serialize(Object body) {
		return gson.toJson(body);
	}
}
