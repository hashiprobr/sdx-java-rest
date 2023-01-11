package br.pro.hashi.sdx.rest.coding;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.nio.charset.StandardCharsets;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class PercentTest {
	@ParameterizedTest
	@CsvSource({
			"0,                  0",
			"0,                  0/",
			"0,                  0//",
			"0,                  0///",
			"01,                 01",
			"01,                 01/",
			"01,                 01//",
			"01,                 01///",
			"012,                012",
			"012,                012/",
			"012,                012//",
			"012,                012///",
			"0/1,                0/1",
			"0/1,                0/1/",
			"0/1,                0/1//",
			"0/1,                0/1///",
			"00//11,             00//11",
			"00//11,             00//11/",
			"00//11,             00//11//",
			"00//11,             00//11///",
			"000///111,          000///111",
			"000///111,          000///111/",
			"000///111,          000///111//",
			"000///111,          000///111///",
			"0/1/2,              0/1/2",
			"0/1/2,              0/1/2/",
			"0/1/2,              0/1/2//",
			"0/1/2,              0/1/2///",
			"00//11//22,         00//11//22",
			"00//11//22,         00//11//22/",
			"00//11//22,         00//11//22//",
			"00//11//22,         00//11//22///",
			"000///111///222,    000///111///222",
			"000///111///222,    000///111///222/",
			"000///111///222,    000///111///222//",
			"000///111///222,    000///111///222///",
			"/,                  /",
			"/,                  //",
			"/,                  ///",
			"/0,                 /0",
			"/0,                 /0/",
			"/0,                 /0//",
			"/0,                 /0///",
			"//00,               //00",
			"//00,               //00/",
			"//00,               //00//",
			"//00,               //00///",
			"///000,             ///000",
			"///000,             ///000/",
			"///000,             ///000//",
			"///000,             ///000///",
			"/0/1,               /0/1",
			"/0/1,               /0/1/",
			"/0/1,               /0/1//",
			"/0/1,               /0/1///",
			"//00//11,           //00//11",
			"//00//11,           //00//11/",
			"//00//11,           //00//11//",
			"//00//11,           //00//11///",
			"///000///111,       ///000///111",
			"///000///111,       ///000///111/",
			"///000///111,       ///000///111//",
			"///000///111,       ///000///111///",
			"/0/1/2,             /0/1/2",
			"/0/1/2,             /0/1/2/",
			"/0/1/2,             /0/1/2//",
			"/0/1/2,             /0/1/2///",
			"//00//11//22,       //00//11//22",
			"//00//11//22,       //00//11//22/",
			"//00//11//22,       //00//11//22//",
			"//00//11//22,       //00//11//22///",
			"///000///111///222, ///000///111///222",
			"///000///111///222, ///000///111///222/",
			"///000///111///222, ///000///111///222//",
			"///000///111///222, ///000///111///222///" })
	void stripsEndingSlashes(String expected, String uri) {
		assertEquals(expected, Percent.stripEndingSlashes(uri));
	}

	@Test
	void splitsAndEncodesSlash() {
		assertArrayEquals(new String[] { "", "" }, splitAndEncode("/"));
	}

	@Test
	void splitsAndEncodesPlus() {
		assertArrayEquals(new String[] { "", "%2B" }, splitAndEncode("/+"));
	}

	@Test
	void splitsAndEncodesSpace() {
		assertArrayEquals(new String[] { "", "%20" }, splitAndEncode("/ "));
	}

	@Test
	void splitsAndEncodesOneItem() {
		assertArrayEquals(new String[] { "", "0" }, splitAndEncode("/0"));
	}

	@Test
	void splitsAndEncodesOneItemDoubled() {
		assertArrayEquals(new String[] { "", "", "00" }, splitAndEncode("//00"));
	}

	@Test
	void splitsAndEncodesOneItemTripled() {
		assertArrayEquals(new String[] { "", "", "", "000" }, splitAndEncode("///000"));
	}

	@Test
	void splitsAndEncodesTwoItems() {
		assertArrayEquals(new String[] { "", "0", "1" }, splitAndEncode("/0/1"));
	}

	@Test
	void splitsAndEncodesTwoItemsDoubled() {
		assertArrayEquals(new String[] { "", "", "00", "", "11" }, splitAndEncode("//00//11"));
	}

	@Test
	void splitsAndEncodesTwoItemsTripled() {
		assertArrayEquals(new String[] { "", "", "", "000", "", "", "111" }, splitAndEncode("///000///111"));
	}

	@Test
	void splitsAndEncodesThreeItems() {
		assertArrayEquals(new String[] { "", "0", "1", "2" }, splitAndEncode("/0/1/2"));
	}

	@Test
	void splitsAndEncodesThreeItemsDoubled() {
		assertArrayEquals(new String[] { "", "", "00", "", "11", "", "22" }, splitAndEncode("//00//11//22"));
	}

	@Test
	void splitsAndEncodesThreeItemsTripled() {
		assertArrayEquals(new String[] { "", "", "", "000", "", "", "111", "", "", "222" }, splitAndEncode("///000///111///222"));
	}

	private String[] splitAndEncode(String uri) {
		return Percent.splitAndEncode(uri, StandardCharsets.UTF_8);
	}

	@Test
	void splitsAndDecodesSlash() {
		assertArrayEquals(new String[] { "", "" }, splitAndDecode("/"));
	}

	@Test
	void splitsAndDecodesPlus() {
		assertArrayEquals(new String[] { "", "+" }, splitAndDecode("/+"));
	}

	@Test
	void splitsAndDecodesOneItem() {
		assertArrayEquals(new String[] { "", "0" }, splitAndDecode("/0"));
	}

	@Test
	void splitsAndDecodesOneItemDoubled() {
		assertArrayEquals(new String[] { "", "", "00" }, splitAndDecode("//00"));
	}

	@Test
	void splitsAndDecodesOneItemTripled() {
		assertArrayEquals(new String[] { "", "", "", "000" }, splitAndDecode("///000"));
	}

	@Test
	void splitsAndDecodesTwoItems() {
		assertArrayEquals(new String[] { "", "0", "1" }, splitAndDecode("/0/1"));
	}

	@Test
	void splitsAndDecodesTwoItemsDoubled() {
		assertArrayEquals(new String[] { "", "", "00", "", "11" }, splitAndDecode("//00//11"));
	}

	@Test
	void splitsAndDecodesTwoItemsTripled() {
		assertArrayEquals(new String[] { "", "", "", "000", "", "", "111" }, splitAndDecode("///000///111"));
	}

	@Test
	void splitsAndDecodesThreeItems() {
		assertArrayEquals(new String[] { "", "0", "1", "2" }, splitAndDecode("/0/1/2"));
	}

	@Test
	void splitsAndDecodesThreeItemsDoubled() {
		assertArrayEquals(new String[] { "", "", "00", "", "11", "", "22" }, splitAndDecode("//00//11//22"));
	}

	@Test
	void splitsAndDecodesThreeItemsTripled() {
		assertArrayEquals(new String[] { "", "", "", "000", "", "", "111", "", "", "222" }, splitAndDecode("///000///111///222"));
	}

	private String[] splitAndDecode(String uri) {
		return Percent.splitAndDecode(uri, StandardCharsets.UTF_8);
	}
}
