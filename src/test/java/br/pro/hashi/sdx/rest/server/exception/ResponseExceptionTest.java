package br.pro.hashi.sdx.rest.server.exception;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

import br.pro.hashi.sdx.rest.transform.Hint;

class ResponseExceptionTest {
	private ResponseException e;

	@Test
	void constructsWithClientError() {
		Object body = new Object();
		e = new ResponseException(450, body);
		assertNull(e.getMessage());
		assertNull(e.getCause());
		assertEquals(450, e.getStatus());
		assertSame(body, e.getBody());
		assertEquals(Object.class, e.getType());
	}

	@Test
	void constructsWithClientErrorAndHint() {
		Object body = new Object();
		e = new ResponseException(450, body, new Hint<Object>() {});
		assertNull(e.getMessage());
		assertNull(e.getCause());
		assertEquals(450, e.getStatus());
		assertSame(body, e.getBody());
		assertEquals(Object.class, e.getType());
	}

	@Test
	void constructsWithServerError() {
		Object body = new Object();
		e = new ResponseException(550, body);
		assertNull(e.getMessage());
		assertNull(e.getCause());
		assertEquals(550, e.getStatus());
		assertSame(body, e.getBody());
		assertEquals(Object.class, e.getType());
	}

	@Test
	void constructsWithServerErrorAndHint() {
		Object body = new Object();
		e = new ResponseException(550, body, new Hint<Object>() {});
		assertNull(e.getMessage());
		assertNull(e.getCause());
		assertEquals(550, e.getStatus());
		assertSame(body, e.getBody());
		assertEquals(Object.class, e.getType());
	}

	@Test
	void doesNotConstructWithSmallStatus() {
		Object body = new Object();
		assertThrows(IllegalArgumentException.class, () -> {
			new ResponseException(350, body);
		});
	}

	@Test
	void doesNotConstructWithLargeStatus() {
		Object body = new Object();
		assertThrows(IllegalArgumentException.class, () -> {
			new ResponseException(650, body);
		});
	}
}
