package br.pro.hashi.sdx.rest.reflection.mock.noargs;

public class WithInvalidConstructor {
	public WithInvalidConstructor() {
		throw new RuntimeException();
	}
}
