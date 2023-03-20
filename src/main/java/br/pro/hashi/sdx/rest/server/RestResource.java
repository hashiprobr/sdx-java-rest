package br.pro.hashi.sdx.rest.server;

/**
 * Stub.
 */
public abstract class RestResource {
	private final String base;
	private final boolean nullBase;

	/**
	 * Stub.
	 */
	protected RestResource() {
		this.base = null;
		this.nullBase = true;
	}

	/**
	 * Stub.
	 * 
	 * @param base stub
	 */
	protected RestResource(String base) {
		this.base = base;
		this.nullBase = false;
	}

	String getBase() {
		return base;
	}

	boolean isNullBase() {
		return nullBase;
	}
}
