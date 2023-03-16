package br.pro.hashi.sdx.rest.reflection.mock.cache;

public class WithCheckedException {
	public static WithCheckedException valueOf(String s) throws Exception {
		throw new Exception();
	}
}
