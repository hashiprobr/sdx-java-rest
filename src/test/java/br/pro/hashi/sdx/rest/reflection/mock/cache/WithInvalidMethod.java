package br.pro.hashi.sdx.rest.reflection.mock.cache;

public class WithInvalidMethod {
	public static WithInvalidMethod valueOf(String s) {
		throw new RuntimeException();
	}
}
