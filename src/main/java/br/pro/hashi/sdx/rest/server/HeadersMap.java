package br.pro.hashi.sdx.rest.server;

import java.util.List;
import java.util.Map;
import java.util.Set;

import br.pro.hashi.sdx.rest.Fields;

/**
 * Maps each part name to its headers. If the request is not multipart, this map
 * is empty.
 */
public final class HeadersMap {
	private Map<String, List<Fields>> map;

	HeadersMap(Map<String, List<Fields>> map) {
		this.map = map;
	}

	/**
	 * <p>
	 * Obtains the headers associated to a specified name or throws an exception if
	 * the name is not available.
	 * </p>
	 * <p>
	 * If there are multiple headers associated to the name, the first one is
	 * considered.
	 * </p>
	 *
	 * @param name the name
	 * @return the headers
	 * @throws NullPointerException     if the name is null
	 * @throws IllegalArgumentException if the name is not available
	 */
	public Fields get(String name) {
		List<Fields> headersList = doGetList(name);
		if (headersList == null) {
			throw new IllegalArgumentException("Name '%s' is not available".formatted(name));
		}
		return headersList.get(0);
	}

	/**
	 * <p>
	 * Obtains the headers associated to a specified name.
	 * </p>
	 * <p>
	 * If there are no headers associated to the name, an empty list is returned.
	 * </p>
	 *
	 * @param name the name
	 * @return a list with the headers
	 * @throws NullPointerException if the name is null
	 */
	public List<Fields> getList(String name) {
		List<Fields> headersList = doGetList(name);
		if (headersList == null) {
			return List.of();
		}
		return headersList;
	}

	/**
	 * <p>
	 * Obtains the available names.
	 * </p>
	 * <p>
	 * If there are no names available, an empty set is returned.
	 * </p>
	 *
	 * @return a set with the names
	 */
	public Set<String> names() {
		return map.keySet();
	}

	private List<Fields> doGetList(String name) {
		if (name == null) {
			throw new NullPointerException("Name cannot be null");
		}
		return map.get(name);
	}
}
