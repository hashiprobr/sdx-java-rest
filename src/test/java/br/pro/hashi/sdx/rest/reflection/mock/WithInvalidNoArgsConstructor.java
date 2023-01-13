package br.pro.hashi.sdx.rest.reflection.mock;

public class WithInvalidNoArgsConstructor {
	public WithInvalidNoArgsConstructor() {
		throw new RuntimeException();
	}
}
