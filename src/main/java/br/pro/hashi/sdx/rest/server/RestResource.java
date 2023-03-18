package br.pro.hashi.sdx.rest.server;

/**
 * Stub.
 */
public abstract class RestResource {
	private final String base;

	/**
	 * Stub.
	 */
	protected RestResource() {
		this("/");
	}

	/**
	 * Stub.
	 * 
	 * @param base stub
	 */
	protected RestResource(String base) {
		this.base = base;
	}

	String getBase() {
		return base;
	}
}
