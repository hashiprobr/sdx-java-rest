package br.pro.hashi.sdx.rest.client;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import br.pro.hashi.sdx.rest.Hint;
import br.pro.hashi.sdx.rest.client.RestClient.Proxy.Entry;

class RestPartTest {
	private Object actual;
	private RestPart p;

	@BeforeEach
	void setUp() {
		actual = new Object();
	}

	@Test
	void initializesActual() {
		p = newRestPart();
		assertSame(actual, p.getActual());
	}

	@Test
	void initializesActualWithNull() {
		p = new RestPart(null);
		assertNull(p.getActual());
	}

	@Test
	void initializesType() {
		p = newRestPart();
		assertEquals(Object.class, p.getType());
	}

	@Test
	void initializesTypeWithHint() {
		p = new RestPart(actual, new Hint<Object>() {});
		assertEquals(Object.class, p.getType());
	}

	@Test
	void doesNotInitializeTypeWithHint() {
		assertThrows(NullPointerException.class, () -> {
			new RestPart(actual, (Hint<Object>) null);
		});
	}

	@Test
	void initializesTypeWithNull() {
		p = new RestPart(null);
		assertEquals(Object.class, p.getType());
	}

	@Test
	void initializesTypeWithNullAndHint() {
		p = new RestPart(null, new Hint<Object>() {});
		assertEquals(Object.class, p.getType());
	}

	@Test
	void doesNotInitializeTypeWithNullAndHint() {
		assertThrows(NullPointerException.class, () -> {
			new RestPart(null, (Hint<Object>) null);
		});
	}

	@Test
	void initializesWithoutHeaders() {
		p = newRestPart();
		assertTrue(p.getHeaders().isEmpty());
	}

	@Test
	void initializesWithoutName() {
		p = newRestPart();
		assertNull(p.getName());
	}

	@Test
	void setsName() {
		p = newRestPart();
		p.setName("name");
		assertEquals("name", p.getName());
	}

	@Test
	void addsHeader() {
		p = newRestPart();
		assertSame(p, p.h(" \t\nname \t\n", 0));
		assertEquals(1, p.getHeaders().size());
		Entry entry = p.getHeaders().get(0);
		assertEquals("name", entry.name());
		assertEquals("0", entry.valueString());
	}

	@Test
	void doesNotAddHeaderIfNameIsNull() {
		p = newRestPart();
		Object value = new Object();
		assertThrows(NullPointerException.class, () -> {
			p.h(null, value);
		});
		assertTrue(p.getHeaders().isEmpty());
	}

	@Test
	void doesNotAddHeaderIfNameIsBlank() {
		p = newRestPart();
		Object value = new Object();
		assertThrows(IllegalArgumentException.class, () -> {
			p.h(" \t\n", value);
		});
		assertTrue(p.getHeaders().isEmpty());
	}

	@Test
	void doesNotAddHeaderIfNameNotInUSASCII() {
		p = newRestPart();
		Object value = new Object();
		assertThrows(IllegalArgumentException.class, () -> {
			p.h("spéçíál", value);
		});
		assertTrue(p.getHeaders().isEmpty());
	}

	@Test
	void doesNotAddHeaderIfValueIsNull() {
		p = newRestPart();
		assertThrows(NullPointerException.class, () -> {
			p.h("name", null);
		});
		assertTrue(p.getHeaders().isEmpty());
	}

	@Test
	void doesNotAddHeaderIfValueStringIsNull() {
		p = newRestPart();
		Object value = new Object() {
			@Override
			public String toString() {
				return null;
			}
		};
		assertThrows(NullPointerException.class, () -> {
			p.h("name", value);
		});
		assertTrue(p.getHeaders().isEmpty());
	}

	@Test
	void doesNotAddHeaderIfValueStringNotInUSASCII() {
		p = newRestPart();
		assertThrows(IllegalArgumentException.class, () -> {
			p.h("name", "spéçíál");
		});
		assertTrue(p.getHeaders().isEmpty());
	}

	private RestPart newRestPart() {
		return new RestPart(actual);
	}
}
