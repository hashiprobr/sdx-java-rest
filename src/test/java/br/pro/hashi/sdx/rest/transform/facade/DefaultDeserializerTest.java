package br.pro.hashi.sdx.rest.transform.facade;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.Reader;
import java.io.StringReader;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import br.pro.hashi.sdx.rest.reflection.ParserFactory;
import br.pro.hashi.sdx.rest.transform.Deserializer;
import br.pro.hashi.sdx.rest.transform.Hint;
import br.pro.hashi.sdx.rest.transform.exception.UnsupportedException;

class DefaultDeserializerTest {
	private Reader reader;
	private Deserializer d;

	@BeforeEach
	void setUp() {
		reader = new StringReader(newString());
		d = new DefaultDeserializer(ParserFactory.getInstance());
	}

	@Test
	void readsIfTypeEqualsBoolean() {
		assertEquals(false, d.read(new StringReader("false"), boolean.class));
	}

	@Test
	void readsIfTypeEqualsBooleanWithHint() {
		assertEquals(false, d.read(new StringReader("false"), new Hint<Boolean>() {}.getType()));
	}

	@Test
	void readsIfTypeEqualsByte() {
		assertEquals(0, (byte) d.read(new StringReader("0"), byte.class));
	}

	@Test
	void readsIfTypeEqualsByteWithHint() {
		assertEquals(0, (byte) d.read(new StringReader("0"), new Hint<Byte>() {}.getType()));
	}

	@Test
	void readsIfTypeEqualsShort() {
		assertEquals(1, (short) d.read(new StringReader("1"), short.class));
	}

	@Test
	void readsIfTypeEqualsShortWithHint() {
		assertEquals(1, (short) d.read(new StringReader("1"), new Hint<Short>() {}.getType()));
	}

	@Test
	void readsIfTypeEqualsInteger() {
		assertEquals(2, (int) d.read(new StringReader("2"), int.class));
	}

	@Test
	void readsIfTypeEqualsIntegerWithHint() {
		assertEquals(2, (int) d.read(new StringReader("2"), new Hint<Integer>() {}.getType()));
	}

	@Test
	void readsIfTypeEqualsLong() {
		assertEquals(3L, (long) d.read(new StringReader("3"), long.class));
	}

	@Test
	void readsIfTypeEqualsLongWithHint() {
		assertEquals(3L, (long) d.read(new StringReader("3"), new Hint<Long>() {}.getType()));
	}

	@Test
	void readsIfTypeEqualsFloat() {
		assertEquals(4.5F, d.read(new StringReader("4.5"), float.class), 0.000001F);
	}

	@Test
	void readsIfTypeEqualsFloatWithHint() {
		assertEquals(4.5F, d.read(new StringReader("4.5"), new Hint<Float>() {}.getType()), 0.000001F);
	}

	@Test
	void readsIfTypeEqualsDouble() {
		assertEquals(6.7, d.read(new StringReader("6.7"), double.class), 0.000001);
	}

	@Test
	void readsIfTypeEqualsDoubleWithHint() {
		assertEquals(6.7, d.read(new StringReader("6.7"), new Hint<Double>() {}.getType()), 0.000001);
	}

	@Test
	void readsIfTypeEqualsString() {
		assertEquals(newString(), d.read(reader, String.class));
	}

	@Test
	void readsIfTypeEqualsStringWithHint() {
		assertEquals(newString(), d.read(reader, new Hint<String>() {}.getType()));
	}

	@Test
	void readsIfTypeEqualsReader() {
		assertSame(reader, d.read(reader, Reader.class));
	}

	@Test
	void readsIfTypeEqualsReaderWithHint() {
		assertSame(reader, d.read(reader, new Hint<Reader>() {}.getType()));
	}

	@Test
	void doesNotReadIfTypeEqualsStringReader() {
		assertThrows(UnsupportedException.class, () -> {
			d.read(reader, StringReader.class);
		});
	}

	@Test
	void doesNotReadIfTypeEqualsStringReaderWithHint() {
		assertThrows(UnsupportedException.class, () -> {
			d.read(reader, new Hint<StringReader>() {}.getType());
		});
	}

	private String newString() {
		return "content";
	}
}
