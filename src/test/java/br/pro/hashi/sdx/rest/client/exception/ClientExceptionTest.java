package br.pro.hashi.sdx.rest.client.exception;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

import org.junit.jupiter.api.Test;

class ClientExceptionTest {
	private ClientException e;

	@Test
	void constructsWithCause() {
		Throwable cause = new Throwable();
		e = new ClientException(cause);
		assertEquals(cause.toString(), e.getMessage());
		assertSame(cause, e.getCause());
	}
}
