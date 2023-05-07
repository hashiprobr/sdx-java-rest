package br.pro.hashi.sdx.rest.reflection.mock.parser;

public class NonInstanceMethod {
	public static Object valueOf(String s) {
		return new NonInstanceMethod();
	}
}
