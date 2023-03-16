package br.pro.hashi.sdx.rest.reflection.mock.cache;

public class WithUncheckedException {
	public static WithUncheckedException valueOf(String s) throws RuntimeException {
		return new WithUncheckedException();
	}
}
