package br.pro.hashi.sdx.rest.converter;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.ByteArrayInputStream;

import org.junit.jupiter.api.Test;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import br.pro.hashi.sdx.rest.converter.base.BaseConverter;
import br.pro.hashi.sdx.rest.converter.mock.ConverterToBigDecimal;
import br.pro.hashi.sdx.rest.converter.mock.ConverterToBigInteger;
import br.pro.hashi.sdx.rest.converter.mock.ConverterToBoolean;
import br.pro.hashi.sdx.rest.converter.mock.ConverterToByte;
import br.pro.hashi.sdx.rest.converter.mock.ConverterToDouble;
import br.pro.hashi.sdx.rest.converter.mock.ConverterToFloat;
import br.pro.hashi.sdx.rest.converter.mock.ConverterToInteger;
import br.pro.hashi.sdx.rest.converter.mock.ConverterToList;
import br.pro.hashi.sdx.rest.converter.mock.ConverterToLong;
import br.pro.hashi.sdx.rest.converter.mock.ConverterToMap;
import br.pro.hashi.sdx.rest.converter.mock.ConverterToShort;
import br.pro.hashi.sdx.rest.converter.mock.ConverterToString;

class ConverterTest {
	private Gson gson;

	private void create(BaseConverter<?, ?> converter) {
		GsonBuilder builder = new GsonBuilder();
		converter.register(builder);
		gson = builder.create();
	}

	private String toJson(byte[] bytes) {
		return gson.toJson(new ByteArrayInputStream(bytes));
	}

	private byte[] fromJson(String content) {
		return gson.fromJson(content, ByteArrayInputStream.class).readAllBytes();
	}

	@Test
	void convertsToBoolean() {
		create(new ConverterToBoolean());
		assertEquals("null", toJson(new byte[] {}));
		assertEquals("false", toJson(new byte[] { 0 }));
		assertEquals("false", toJson(new byte[] { 0, 0 }));
		assertEquals("false", toJson(new byte[] { 0, 1 }));
		assertEquals("true", toJson(new byte[] { 1 }));
		assertEquals("true", toJson(new byte[] { 1, 0 }));
		assertEquals("true", toJson(new byte[] { 1, 1 }));
	}

	@Test
	void convertsFromBoolean() {
		create(new ConverterToBoolean());
		assertArrayEquals(new byte[] { 0 }, fromJson("false"));
		assertArrayEquals(new byte[] { 1 }, fromJson("true"));
	}

	@Test
	void convertsToByte() {
		create(new ConverterToByte());
		assertEquals("null", toJson(new byte[] {}));
		assertEquals("10", toJson(new byte[] { 0 }));
		assertEquals("10", toJson(new byte[] { 0, 0 }));
		assertEquals("10", toJson(new byte[] { 0, 1 }));
		assertEquals("11", toJson(new byte[] { 1 }));
		assertEquals("11", toJson(new byte[] { 1, 0 }));
		assertEquals("11", toJson(new byte[] { 1, 1 }));
	}

	@Test
	void convertsFromByte() {
		create(new ConverterToByte());
		assertArrayEquals(new byte[] { 10 }, fromJson("0"));
		assertArrayEquals(new byte[] { 11 }, fromJson("1"));
	}

	@Test
	void convertsToShort() {
		create(new ConverterToShort());
		assertEquals("null", toJson(new byte[] {}));
		assertEquals("20", toJson(new byte[] { 0 }));
		assertEquals("20", toJson(new byte[] { 0, 0 }));
		assertEquals("20", toJson(new byte[] { 0, 1 }));
		assertEquals("21", toJson(new byte[] { 1 }));
		assertEquals("21", toJson(new byte[] { 1, 0 }));
		assertEquals("21", toJson(new byte[] { 1, 1 }));
	}

	@Test
	void convertsFromShort() {
		create(new ConverterToShort());
		assertArrayEquals(new byte[] { 20 }, fromJson("0"));
		assertArrayEquals(new byte[] { 21 }, fromJson("1"));
	}

	@Test
	void convertsToInteger() {
		create(new ConverterToInteger());
		assertEquals("null", toJson(new byte[] {}));
		assertEquals("30", toJson(new byte[] { 0 }));
		assertEquals("30", toJson(new byte[] { 0, 0 }));
		assertEquals("30", toJson(new byte[] { 0, 1 }));
		assertEquals("31", toJson(new byte[] { 1 }));
		assertEquals("31", toJson(new byte[] { 1, 0 }));
		assertEquals("31", toJson(new byte[] { 1, 1 }));
	}

	@Test
	void convertsFromInteger() {
		create(new ConverterToInteger());
		assertArrayEquals(new byte[] { 30 }, fromJson("0"));
		assertArrayEquals(new byte[] { 31 }, fromJson("1"));
	}

	@Test
	void convertsToLong() {
		create(new ConverterToLong());
		assertEquals("null", toJson(new byte[] {}));
		assertEquals("40", toJson(new byte[] { 0 }));
		assertEquals("40", toJson(new byte[] { 0, 0 }));
		assertEquals("40", toJson(new byte[] { 0, 1 }));
		assertEquals("41", toJson(new byte[] { 1 }));
		assertEquals("41", toJson(new byte[] { 1, 0 }));
		assertEquals("41", toJson(new byte[] { 1, 1 }));
	}

	@Test
	void convertsFromLong() {
		create(new ConverterToLong());
		assertArrayEquals(new byte[] { 40 }, fromJson("0"));
		assertArrayEquals(new byte[] { 41 }, fromJson("1"));
	}

	@Test
	void convertsToFloat() {
		create(new ConverterToFloat());
		assertEquals("null", toJson(new byte[] {}));
		assertEquals("50.0", toJson(new byte[] { 0 }));
		assertEquals("50.0", toJson(new byte[] { 0, 0 }));
		assertEquals("50.0", toJson(new byte[] { 0, 1 }));
		assertEquals("51.0", toJson(new byte[] { 1 }));
		assertEquals("51.0", toJson(new byte[] { 1, 0 }));
		assertEquals("51.0", toJson(new byte[] { 1, 1 }));
	}

	@Test
	void convertsFromFloat() {
		create(new ConverterToFloat());
		assertArrayEquals(new byte[] { 50 }, fromJson("0"));
		assertArrayEquals(new byte[] { 51 }, fromJson("1"));
	}

	@Test
	void convertsToDouble() {
		create(new ConverterToDouble());
		assertEquals("null", toJson(new byte[] {}));
		assertEquals("60.0", toJson(new byte[] { 0 }));
		assertEquals("60.0", toJson(new byte[] { 0, 0 }));
		assertEquals("60.0", toJson(new byte[] { 0, 1 }));
		assertEquals("61.0", toJson(new byte[] { 1 }));
		assertEquals("61.0", toJson(new byte[] { 1, 0 }));
		assertEquals("61.0", toJson(new byte[] { 1, 1 }));
	}

	@Test
	void convertsFromDouble() {
		create(new ConverterToDouble());
		assertArrayEquals(new byte[] { 60 }, fromJson("0"));
		assertArrayEquals(new byte[] { 61 }, fromJson("1"));
	}

	@Test
	void convertsToBigInteger() {
		create(new ConverterToBigInteger());
		assertEquals("null", toJson(new byte[] {}));
		assertEquals("70", toJson(new byte[] { 0 }));
		assertEquals("70", toJson(new byte[] { 0, 0 }));
		assertEquals("70", toJson(new byte[] { 0, 1 }));
		assertEquals("71", toJson(new byte[] { 1 }));
		assertEquals("71", toJson(new byte[] { 1, 0 }));
		assertEquals("71", toJson(new byte[] { 1, 1 }));
	}

	@Test
	void convertsFromBigInteger() {
		create(new ConverterToBigInteger());
		assertArrayEquals(new byte[] { 70 }, fromJson("0"));
		assertArrayEquals(new byte[] { 71 }, fromJson("1"));
	}

	@Test
	void convertsToBigDecimal() {
		create(new ConverterToBigDecimal());
		assertEquals("null", toJson(new byte[] {}));
		assertEquals("80", toJson(new byte[] { 0 }));
		assertEquals("80", toJson(new byte[] { 0, 0 }));
		assertEquals("80", toJson(new byte[] { 0, 1 }));
		assertEquals("81", toJson(new byte[] { 1 }));
		assertEquals("81", toJson(new byte[] { 1, 0 }));
		assertEquals("81", toJson(new byte[] { 1, 1 }));
	}

	@Test
	void convertsFromBigDecimal() {
		create(new ConverterToBigDecimal());
		assertArrayEquals(new byte[] { 80 }, fromJson("0"));
		assertArrayEquals(new byte[] { 81 }, fromJson("1"));
	}

	@Test
	void convertsToString() {
		create(new ConverterToString());
		assertEquals("null", toJson(new byte[] {}));
		assertEquals("\"90\"", toJson(new byte[] { 0 }));
		assertEquals("\"90\"", toJson(new byte[] { 0, 0 }));
		assertEquals("\"90\"", toJson(new byte[] { 0, 1 }));
		assertEquals("\"91\"", toJson(new byte[] { 1 }));
		assertEquals("\"91\"", toJson(new byte[] { 1, 0 }));
		assertEquals("\"91\"", toJson(new byte[] { 1, 1 }));
	}

	@Test
	void convertsFromString() {
		create(new ConverterToString());
		assertArrayEquals(new byte[] { 90 }, fromJson("\"\""));
		assertArrayEquals(new byte[] { 91 }, fromJson("\" \""));
	}

	@Test
	void convertsToList() {
		create(new ConverterToList());
		assertEquals("[]", toJson(new byte[] {}));
		assertEquals("[0]", toJson(new byte[] { 0 }));
		assertEquals("[0,0]", toJson(new byte[] { 0, 0 }));
		assertEquals("[0,1]", toJson(new byte[] { 0, 1 }));
		assertEquals("[1]", toJson(new byte[] { 1 }));
		assertEquals("[1,0]", toJson(new byte[] { 1, 0 }));
		assertEquals("[1,1]", toJson(new byte[] { 1, 1 }));
	}

	@Test
	void convertsFromList() {
		create(new ConverterToList());
		assertArrayEquals(new byte[] {}, fromJson("[]"));
		assertArrayEquals(new byte[] { 0 }, fromJson("[0]"));
		assertArrayEquals(new byte[] { 0, 0 }, fromJson("[0,0]"));
		assertArrayEquals(new byte[] { 0, 1 }, fromJson("[0,1]"));
		assertArrayEquals(new byte[] { 1 }, fromJson("[1]"));
		assertArrayEquals(new byte[] { 1, 0 }, fromJson("[1,0]"));
		assertArrayEquals(new byte[] { 1, 1 }, fromJson("[1,1]"));
	}

	@Test
	void convertsToMap() {
		create(new ConverterToMap());
		assertEquals("{}", toJson(new byte[] {}));
		assertEquals("{\"0\":0}", toJson(new byte[] { 0 }));
		assertEquals("{\"0\":0,\"1\":0}", toJson(new byte[] { 0, 0 }));
		assertEquals("{\"0\":0,\"1\":1}", toJson(new byte[] { 0, 1 }));
		assertEquals("{\"0\":1}", toJson(new byte[] { 1 }));
		assertEquals("{\"0\":1,\"1\":0}", toJson(new byte[] { 1, 0 }));
		assertEquals("{\"0\":1,\"1\":1}", toJson(new byte[] { 1, 1 }));
	}

	@Test
	void convertsFromMap() {
		create(new ConverterToMap());
		assertArrayEquals(new byte[] {}, fromJson("{}"));
		assertArrayEquals(new byte[] { 0 }, fromJson("{\"0\":0}"));
		assertArrayEquals(new byte[] { 0, 0 }, fromJson("{\"0\":0,\"1\":0}"));
		assertArrayEquals(new byte[] { 0, 1 }, fromJson("{\"0\":0,\"1\":1}"));
		assertArrayEquals(new byte[] { 1 }, fromJson("{\"0\":1}"));
		assertArrayEquals(new byte[] { 1, 0 }, fromJson("{\"0\":1,\"1\":0}"));
		assertArrayEquals(new byte[] { 1, 1 }, fromJson("{\"0\":1,\"1\":1}"));
	}
}
