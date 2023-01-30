package br.pro.hashi.sdx.rest.reflection.mock.cache;

public class WithNonPublicMethod {
	static WithNonPublicMethod valueOf(String s) {
		return new WithNonPublicMethod();
	}
}
