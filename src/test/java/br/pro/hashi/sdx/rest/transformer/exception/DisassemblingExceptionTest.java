package br.pro.hashi.sdx.rest.transformer.exception;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;

import org.junit.jupiter.api.Test;

class DisassemblingExceptionTest {
	private static final String MESSAGE = "message";

	private DisassemblingException e;

	@Test
	void noArgsConstructor() {
		e = new DisassemblingException();
		assertNull(e.getMessage());
		assertNull(e.getCause());
	}

	@Test
	void messageConstructor() {
		e = new DisassemblingException(MESSAGE);
		assertEquals(MESSAGE, e.getMessage());
		assertNull(e.getCause());
	}

	@Test
	void messageAndCauseConstructor() {
		Throwable t = new Throwable();
		e = new DisassemblingException(MESSAGE, t);
		assertEquals(MESSAGE, e.getMessage());
		assertSame(t, e.getCause());
	}

	@Test
	void causeConstructor() {
		Throwable t = new Throwable();
		e = new DisassemblingException(t);
		assertEquals(t.toString(), e.getMessage());
		assertSame(t, e.getCause());
	}
}
