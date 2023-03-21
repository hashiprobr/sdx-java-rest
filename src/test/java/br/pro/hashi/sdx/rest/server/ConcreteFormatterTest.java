package br.pro.hashi.sdx.rest.server;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class ConcreteFormatterTest {
	private ConcreteFormatter f;

	@Test
	void formats() {
		f = new ConcreteFormatter();
		assertEquals(String.class, f.getReturnType());
		assertEquals("message", f.format(400, "message"));
	}
}
