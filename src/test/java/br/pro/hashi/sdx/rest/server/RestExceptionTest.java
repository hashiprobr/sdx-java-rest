package br.pro.hashi.sdx.rest.server;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

import br.pro.hashi.sdx.rest.transform.Hint;

class RestExceptionTest {
	private RestException e;

	@Test
	void constructsWithClientStatus() {
		Object body = new Object();
		e = new RestException(450, body);
		assertNull(e.getMessage());
		assertNull(e.getCause());
		assertEquals(450, e.getStatus());
		assertSame(body, e.getBody());
		assertEquals(Object.class, e.getType());
	}

	@Test
	void constructsWithClientStatusAndHint() {
		Object body = new Object();
		e = new RestException(450, body, new Hint<Object>() {});
		assertNull(e.getMessage());
		assertNull(e.getCause());
		assertEquals(450, e.getStatus());
		assertSame(body, e.getBody());
		assertEquals(Object.class, e.getType());
	}

	@Test
	void doesNotConstructWithClientStatusAndHint() {
		Object body = new Object();
		assertThrows(NullPointerException.class, () -> {
			new RestException(450, body, null);
		});
	}

	@Test
	void constructsWithClientStatusAndNull() {
		e = new RestException(450, null);
		assertNull(e.getMessage());
		assertNull(e.getCause());
		assertEquals(450, e.getStatus());
		assertNull(e.getBody());
		assertEquals(Object.class, e.getType());
	}

	@Test
	void constructsWithClientStatusAndNullAndHint() {
		e = new RestException(450, null, new Hint<Object>() {});
		assertNull(e.getMessage());
		assertNull(e.getCause());
		assertEquals(450, e.getStatus());
		assertNull(e.getBody());
		assertEquals(Object.class, e.getType());
	}

	@Test
	void doesNotConstructWithClientStatusAndNullAndHint() {
		assertThrows(NullPointerException.class, () -> {
			new RestException(450, null, null);
		});
	}

	@Test
	void constructsWithServerStatus() {
		Object body = new Object();
		e = new RestException(550, body);
		assertNull(e.getMessage());
		assertNull(e.getCause());
		assertEquals(550, e.getStatus());
		assertSame(body, e.getBody());
		assertEquals(Object.class, e.getType());
	}

	@Test
	void constructsWithServerStatusAndHint() {
		Object body = new Object();
		e = new RestException(550, body, new Hint<Object>() {});
		assertNull(e.getMessage());
		assertNull(e.getCause());
		assertEquals(550, e.getStatus());
		assertSame(body, e.getBody());
		assertEquals(Object.class, e.getType());
	}

	@Test
	void doesNotConstructWithServerStatusAndHint() {
		Object body = new Object();
		assertThrows(NullPointerException.class, () -> {
			new RestException(550, body, null);
		});
	}

	@Test
	void constructsWithServerStatusAndNull() {
		e = new RestException(550, null);
		assertNull(e.getMessage());
		assertNull(e.getCause());
		assertEquals(550, e.getStatus());
		assertNull(e.getBody());
		assertEquals(Object.class, e.getType());
	}

	@Test
	void constructsWithServerStatusAndNullAndHint() {
		e = new RestException(550, null, new Hint<Object>() {});
		assertNull(e.getMessage());
		assertNull(e.getCause());
		assertEquals(550, e.getStatus());
		assertNull(e.getBody());
		assertEquals(Object.class, e.getType());
	}

	@Test
	void doesNotConstructWithServerStatusAndNullAndHint() {
		assertThrows(NullPointerException.class, () -> {
			new RestException(550, null, null);
		});
	}

	@Test
	void doesNotConstructWithSmallStatus() {
		Object body = new Object();
		assertThrows(IllegalArgumentException.class, () -> {
			new RestException(350, body);
		});
	}

	@Test
	void doesNotConstructWithLargeStatus() {
		Object body = new Object();
		assertThrows(IllegalArgumentException.class, () -> {
			new RestException(650, body);
		});
	}
}
