package br.pro.hashi.sdx.rest.reflection.mock;

public class WithNonPublicMethod {
	static WithNonPublicMethod valueOf(String s) {
		return new WithNonPublicMethod();
	}
}
