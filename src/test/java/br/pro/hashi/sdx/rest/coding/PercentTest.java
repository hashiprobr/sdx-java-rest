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
	void recodesSlash() {
		assertEquals("/", recode("/"));
	}

	@Test
	void recodesPlus() {
		assertEquals("/%2B", recode("/+"));
	}

	@Test
	void recodesSpace() {
		assertEquals("/%20", recode("/ "));
	}

	@Test
	void recodesOneItem() {
		assertEquals("/0", recode("/0"));
	}

	@Test
	void recodesOneItemDoubled() {
		assertEquals("//00", recode("//00"));
	}

	@Test
	void recodesOneItemTripled() {
		assertEquals("///000", recode("///000"));
	}

	@Test
	void recodesTwoItems() {
		assertEquals("/0/1", recode("/0/1"));
	}

	@Test
	void recodesTwoItemsDoubled() {
		assertEquals("//00//11", recode("//00//11"));
	}

	@Test
	void recodesTwoItemsTripled() {
		assertEquals("///000///111", recode("///000///111"));
	}

	@Test
	void recodesThreeItems() {
		assertEquals("/0/1/2", recode("/0/1/2"));
	}

	@Test
	void recodesThreeItemsDoubled() {
		assertEquals("//00//11//22", recode("//00//11//22"));
	}

	@Test
	void recodesThreeItemsTripled() {
		assertEquals("///000///111///222", recode("///000///111///222"));
	}

	private String recode(String uri) {
		return Percent.recode(uri, StandardCharsets.UTF_8);
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
