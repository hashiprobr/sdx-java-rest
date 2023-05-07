package br.pro.hashi.sdx.rest.reflection.mock.parser;

public class CheckedMethod {
	public static CheckedMethod valueOf(String s) throws Exception {
		throw new Exception();
	}
}
