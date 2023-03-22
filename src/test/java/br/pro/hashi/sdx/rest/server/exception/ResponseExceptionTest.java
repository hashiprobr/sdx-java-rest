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
	void constructsWithClientStatus() {
		Object body = new Object();
		e = new ResponseException(450, body);
		assertNull(e.getMessage());
		assertNull(e.getCause());
		assertEquals(450, e.getStatus());
		assertSame(body, e.getBody());
		assertEquals(Object.class, e.getType());
	}

	@Test
	void constructsWithClientStatusAndHint() {
		Object body = new Object();
		e = new ResponseException(450, body, new Hint<Object>() {});
		assertNull(e.getMessage());
		assertNull(e.getCause());
		assertEquals(450, e.getStatus());
		assertSame(body, e.getBody());
		assertEquals(Object.class, e.getType());
	}

	@Test
	void constructsWithClientStatusAndNull() {
		e = new ResponseException(450, null);
		assertNull(e.getMessage());
		assertNull(e.getCause());
		assertEquals(450, e.getStatus());
		assertNull(e.getBody());
		assertEquals(Object.class, e.getType());
	}

	@Test
	void constructsWithClientStatusAndNullAndHint() {
		e = new ResponseException(450, null, new Hint<Object>() {});
		assertNull(e.getMessage());
		assertNull(e.getCause());
		assertEquals(450, e.getStatus());
		assertNull(e.getBody());
		assertEquals(Object.class, e.getType());
	}

	@Test
	void constructsWithServerStatus() {
		Object body = new Object();
		e = new ResponseException(550, body);
		assertNull(e.getMessage());
		assertNull(e.getCause());
		assertEquals(550, e.getStatus());
		assertSame(body, e.getBody());
		assertEquals(Object.class, e.getType());
	}

	@Test
	void constructsWithServerStatusAndHint() {
		Object body = new Object();
		e = new ResponseException(550, body, new Hint<Object>() {});
		assertNull(e.getMessage());
		assertNull(e.getCause());
		assertEquals(550, e.getStatus());
		assertSame(body, e.getBody());
		assertEquals(Object.class, e.getType());
	}

	@Test
	void constructsWithServerStatusAndNull() {
		e = new ResponseException(550, null);
		assertNull(e.getMessage());
		assertNull(e.getCause());
		assertEquals(550, e.getStatus());
		assertNull(e.getBody());
		assertEquals(Object.class, e.getType());
	}

	@Test
	void constructsWithServerStatusAndNullAndHint() {
		e = new ResponseException(550, null, new Hint<Object>() {});
		assertNull(e.getMessage());
		assertNull(e.getCause());
		assertEquals(550, e.getStatus());
		assertNull(e.getBody());
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
