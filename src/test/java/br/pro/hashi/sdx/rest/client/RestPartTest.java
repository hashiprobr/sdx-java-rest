package br.pro.hashi.sdx.rest.client;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import br.pro.hashi.sdx.rest.Hint;
import br.pro.hashi.sdx.rest.client.RestClient.Proxy.Entry;

class RestPartTest extends RestBodyTest {
	private static final String SPECIAL_CONTENT = "spéçìal";

	private RestPart p;

	@Test
	void gets() {
		p = RestPart.of(actual);
		assertSame(actual, p.getActual());
		assertEquals(Object.class, p.getType());
	}

	@Test
	void doesNotGetFromNull() {
		assertThrows(NullPointerException.class, () -> {
			RestPart.of(null);
		});
	}

	@Test
	void getsFromHint() {
		p = RestPart.of(actual, new Hint<Object>() {});
		assertSame(actual, p.getActual());
		assertEquals(new Hint<Object>() {}.getType(), p.getType());
	}

	@Test
	void doesNotGetFromNullHint() {
		assertThrows(NullPointerException.class, () -> {
			RestPart.of(actual, null);
		});
	}

	@Test
	void initializesWithoutHeaders() {
		p = newInstance();
		assertTrue(p.getHeaders().isEmpty());
	}

	@Test
	void initializesWithoutName() {
		p = newInstance();
		assertNull(p.getName());
	}

	@Test
	void addsHeader() {
		p = newInstance();
		assertSame(p, p.h(" \t\nname \t\n", 0));
		assertEquals(1, p.getHeaders().size());
		Entry entry = p.getHeaders().get(0);
		assertEquals("name", entry.name());
		assertEquals("0", entry.valueString());
	}

	@Test
	void doesNotAddHeaderWithNullName() {
		p = newInstance();
		assertThrows(NullPointerException.class, () -> {
			p.h(null, 0);
		});
	}

	@Test
	void doesNotAddHeaderWithBlankName() {
		p = newInstance();
		assertThrows(IllegalArgumentException.class, () -> {
			p.h(" \t\n", 0);
		});
	}

	@Test
	void doesNotAddHeaderWithSpecialName() {
		p = newInstance();
		assertThrows(IllegalArgumentException.class, () -> {
			p.h(SPECIAL_CONTENT, 0);
		});
	}

	@Test
	void doesNotAddHeaderWithNullValue() {
		p = newInstance();
		assertThrows(NullPointerException.class, () -> {
			p.h("name", null);
		});
	}

	@Test
	void doesNotAddHeaderWithNullStringValue() {
		p = newInstance();
		Object value = new Object() {
			@Override
			public String toString() {
				return null;
			}
		};
		assertThrows(NullPointerException.class, () -> {
			p.h("name", value);
		});
	}

	@Test
	void doesNotAddHeaderWithSpecialValue() {
		p = newInstance();
		assertThrows(IllegalArgumentException.class, () -> {
			p.h("name", SPECIAL_CONTENT);
		});
	}

	@Test
	void setsName() {
		p = newInstance();
		String name = "name";
		p.setName(name);
		assertEquals(name, p.getName());
	}

	@Override
	protected RestPart newInstance() {
		return new RestPart(coder, actual, Object.class);
	}
}
