package br.pro.hashi.sdx.rest.server;

class ConcreteFormatter extends ErrorFormatter {
	@Override
	public String format(int status, String message) {
		return message;
	}
}
