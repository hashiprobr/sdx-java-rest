package br.pro.hashi.sdx.rest.reflection.mock.handle;

public class Methods {
	public void legal() {
		illegalPrivate();
	}

	protected void illegalProtected() {
	}

	void illegalPackage() {
	}

	private void illegalPrivate() {
	}
}
