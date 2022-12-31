package br.pro.hashi.sdx.rest.client;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

class TypeCache {
	final Map<Class<?>, Class<? extends List<?>>> listTypes;
	final Map<Class<?>, Class<? extends Map<String, ?>>> mapTypes;

	TypeCache() {
		this.listTypes = new HashMap<>();
		this.mapTypes = new HashMap<>();
	}

	@SuppressWarnings("unchecked")
	synchronized <T> Class<? extends List<T>> getListTypeOf(Class<T> type) {
		Class<? extends List<T>> listType = (Class<? extends List<T>>) listTypes.get(type);
		if (listType == null) {
			List<T> list = new ArrayList<>();
			listType = (Class<? extends List<T>>) list.getClass();
			listTypes.put(type, listType);
		}
		return listType;
	}

	@SuppressWarnings("unchecked")
	synchronized <T> Class<? extends Map<String, T>> getMapTypeOf(Class<T> type) {
		Class<? extends Map<String, T>> mapType = (Class<? extends Map<String, T>>) mapTypes.get(type);
		if (mapType == null) {
			Map<String, T> map = new LinkedHashMap<>();
			mapType = (Class<? extends Map<String, T>>) map.getClass();
			mapTypes.put(type, mapType);
		}
		return mapType;
	}
}
