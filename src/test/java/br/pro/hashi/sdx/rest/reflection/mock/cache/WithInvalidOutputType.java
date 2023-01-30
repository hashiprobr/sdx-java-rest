package br.pro.hashi.sdx.rest.reflection.mock.cache;

public class WithInvalidOutputType {
	public static Object valueOf(String s) {
		return new WithInvalidOutputType();
	}
}
