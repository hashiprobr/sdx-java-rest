package br.pro.hashi.sdx.rest.reflection.mock.parser;

public class UncheckedMethod {
	public static UncheckedMethod valueOf(String s) throws RuntimeException {
		throw new RuntimeException();
	}
}
