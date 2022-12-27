package br.pro.hashi.sdx.rest.coding;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class PercentTest {
	private String[] encode(String uri) {
		return Percent.encode(uri, Coding.CHARSET);
	}

	private String[] decode(String uri) {
		return Percent.decode(uri, Coding.CHARSET);
	}

	@Test
	void stripsOneCharZeroSlashes() {
		assertEquals("0", Percent.strip("0"));
	}

	@Test
	void stripsOneCharOneSlash() {
		assertEquals("0", Percent.strip("0/"));
	}

	@Test
	void stripsOneCharTwoSlashes() {
		assertEquals("0", Percent.strip("0//"));
	}

	@Test
	void stripsOneCharThreeSlashes() {
		assertEquals("0", Percent.strip("0///"));
	}

	@Test
	void stripsTwoCharsZeroSlashes() {
		assertEquals("01", Percent.strip("01"));
	}

	@Test
	void stripsTwoCharsOneSlash() {
		assertEquals("01", Percent.strip("01/"));
	}

	@Test
	void stripsTwoCharsTwoSlashes() {
		assertEquals("01", Percent.strip("01//"));
	}

	@Test
	void stripsTwoCharsThreeSlashes() {
		assertEquals("01", Percent.strip("01///"));
	}

	@Test
	void stripsThreeCharsZeroSlashes() {
		assertEquals("012", Percent.strip("012"));
	}

	@Test
	void stripsThreeCharsOneSlash() {
		assertEquals("012", Percent.strip("012/"));
	}

	@Test
	void stripsThreeCharsTwoSlashes() {
		assertEquals("012", Percent.strip("012//"));
	}

	@Test
	void stripsThreeCharsThreeSlashes() {
		assertEquals("012", Percent.strip("012///"));
	}

	@Test
	void stripsOneSlash() {
		assertEquals("/", Percent.strip("/"));
	}

	@Test
	void stripsTwoSlashes() {
		assertEquals("/", Percent.strip("//"));
	}

	@Test
	void stripsThreeSlashes() {
		assertEquals("/", Percent.strip("///"));
	}

	@Test
	void stripsOneItemZeroSlashes() {
		assertEquals("/0", Percent.strip("/0"));
	}

	@Test
	void stripsOneItemOneSlash() {
		assertEquals("/0", Percent.strip("/0/"));
	}

	@Test
	void stripsOneItemTwoSlashes() {
		assertEquals("/0", Percent.strip("/0//"));
	}

	@Test
	void stripsOneItemThreeSlashes() {
		assertEquals("/0", Percent.strip("/0///"));
	}

	@Test
	void stripsTwoItemsZeroSlashes() {
		assertEquals("/0/1", Percent.strip("/0/1"));
	}

	@Test
	void stripsTwoItemsOneSlash() {
		assertEquals("/0/1", Percent.strip("/0/1/"));
	}

	@Test
	void stripsTwoItemsTwoSlashes() {
		assertEquals("/0/1", Percent.strip("/0/1//"));
	}

	@Test
	void stripsTwoItemsThreeSlashes() {
		assertEquals("/0/1", Percent.strip("/0/1///"));
	}

	@Test
	void stripsThreeItemsZeroSlashes() {
		assertEquals("/0/1/2", Percent.strip("/0/1/2"));
	}

	@Test
	void stripsThreeItemsOneSlash() {
		assertEquals("/0/1/2", Percent.strip("/0/1/2/"));
	}

	@Test
	void stripsThreeItemsTwoSlashes() {
		assertEquals("/0/1/2", Percent.strip("/0/1/2//"));
	}

	@Test
	void stripsThreeItemsThreeSlashes() {
		assertEquals("/0/1/2", Percent.strip("/0/1/2///"));
	}

	@Test
	void stripsOneCharOneItemZeroSlashes() {
		assertEquals("0/1", Percent.strip("0/1"));
	}

	@Test
	void stripsOneCharOneItemOneSlash() {
		assertEquals("0/1", Percent.strip("0/1/"));
	}

	@Test
	void stripsOneCharOneItemTwoSlashes() {
		assertEquals("0/1", Percent.strip("0/1//"));
	}

	@Test
	void stripsOneCharOneItemThreeSlashes() {
		assertEquals("0/1", Percent.strip("0/1///"));
	}

	@Test
	void stripsOneCharTwoItemsZeroSlashes() {
		assertEquals("0/1/2", Percent.strip("0/1/2"));
	}

	@Test
	void stripsOneCharTwoItemsOneSlash() {
		assertEquals("0/1/2", Percent.strip("0/1/2/"));
	}

	@Test
	void stripsOneCharTwoItemsTwoSlashes() {
		assertEquals("0/1/2", Percent.strip("0/1/2//"));
	}

	@Test
	void stripsOneCharTwoItemsThreeSlashes() {
		assertEquals("0/1/2", Percent.strip("0/1/2///"));
	}

	@Test
	void encodesPlus() {
		assertArrayEquals(new String[] { "%2B" }, encode("+"));
	}

	@Test
	void encodesSpace() {
		assertArrayEquals(new String[] { "%20" }, encode(" "));
	}

	@Test
	void encodesOneChar() {
		assertArrayEquals(new String[] { "0" }, encode("0"));
	}

	@Test
	void encodesTwoChars() {
		assertArrayEquals(new String[] { "01" }, encode("01"));
	}

	@Test
	void encodesThreeChars() {
		assertArrayEquals(new String[] { "012" }, encode("012"));
	}

	@Test
	void encodesOneSlash() {
		assertArrayEquals(new String[] { "", "" }, encode("/"));
	}

	@Test
	void encodesTwoSlashes() {
		assertArrayEquals(new String[] { "", "", "" }, encode("//"));
	}

	@Test
	void encodesThreeSlashes() {
		assertArrayEquals(new String[] { "", "", "", "" }, encode("///"));
	}

	@Test
	void encodesOneItem() {
		assertArrayEquals(new String[] { "", "0" }, encode("/0"));
	}

	@Test
	void encodesTwoItems() {
		assertArrayEquals(new String[] { "", "0", "1" }, encode("/0/1"));
	}

	@Test
	void encodesThreeItems() {
		assertArrayEquals(new String[] { "", "0", "1", "2" }, encode("/0/1/2"));
	}

	@Test
	void encodesOneCharOneItem() {
		assertArrayEquals(new String[] { "0", "1" }, encode("0/1"));
	}

	@Test
	void encodesOneCharTwoItems() {
		assertArrayEquals(new String[] { "0", "1", "2" }, encode("0/1/2"));
	}

	@Test
	void decodesPlus() {
		assertArrayEquals(new String[] { "+" }, decode("+"));
	}

	@Test
	void decodesOneChar() {
		assertArrayEquals(new String[] { "0" }, decode("0"));
	}

	@Test
	void decodesTwoChars() {
		assertArrayEquals(new String[] { "01" }, decode("01"));
	}

	@Test
	void decodesThreeChars() {
		assertArrayEquals(new String[] { "012" }, decode("012"));
	}

	@Test
	void decodesOneSlash() {
		assertArrayEquals(new String[] { "", "" }, decode("/"));
	}

	@Test
	void decodesTwoSlashes() {
		assertArrayEquals(new String[] { "", "", "" }, decode("//"));
	}

	@Test
	void decodesThreeSlashes() {
		assertArrayEquals(new String[] { "", "", "", "" }, decode("///"));
	}

	@Test
	void decodesOneItem() {
		assertArrayEquals(new String[] { "", "0" }, decode("/0"));
	}

	@Test
	void decodesTwoItems() {
		assertArrayEquals(new String[] { "", "0", "1" }, decode("/0/1"));
	}

	@Test
	void decodesThreeItems() {
		assertArrayEquals(new String[] { "", "0", "1", "2" }, decode("/0/1/2"));
	}

	@Test
	void decodesOneCharOneItem() {
		assertArrayEquals(new String[] { "0", "1" }, decode("0/1"));
	}

	@Test
	void decodesOneCharTwoItems() {
		assertArrayEquals(new String[] { "0", "1", "2" }, decode("0/1/2"));
	}
}
