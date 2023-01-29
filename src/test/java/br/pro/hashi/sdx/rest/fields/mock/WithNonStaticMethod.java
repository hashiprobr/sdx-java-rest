package br.pro.hashi.sdx.rest.fields.mock;

public class WithNonStaticMethod {
	public WithNonStaticMethod valueOf(String s) {
		return new WithNonStaticMethod();
	}
}
