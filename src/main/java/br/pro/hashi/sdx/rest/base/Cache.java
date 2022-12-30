package br.pro.hashi.sdx.rest.base;

import java.util.HashMap;
import java.util.Map;

public abstract class Cache<K, V> {
	private final Map<K, V> map;

	protected Cache() {
		this.map = new HashMap<>();
	}

	protected abstract V create(K key);

	public synchronized V get(K key) {
		V value = map.get(key);
		if (value == null) {
			value = create(key);
			map.put(key, value);
		}
		return value;
	}
}
