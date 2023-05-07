package br.pro.hashi.sdx.rest.reflection.mock.parser;

public class NonPublicMethod {
	static NonPublicMethod valueOf(String s) {
		return new NonPublicMethod();
	}
}
