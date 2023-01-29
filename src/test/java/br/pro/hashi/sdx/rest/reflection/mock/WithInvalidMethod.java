package br.pro.hashi.sdx.rest.reflection.mock;

public class WithInvalidMethod {
	public static WithInvalidMethod valueOf(String s) {
		throw new RuntimeException();
	}
}
