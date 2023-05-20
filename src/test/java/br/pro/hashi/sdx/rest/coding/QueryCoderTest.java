package br.pro.hashi.sdx.rest.coding;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

import java.nio.charset.StandardCharsets;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class QueryCoderTest {
	private static final String UNRESERVED = "0123456789BCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz-._~";
	private static final String RESERVED = ":/?#[]@ !$&'()*+,;=";
	private static final String RESERVED_RECODED = "%3A%2F%3F%23%5B%5D%40+%21%24%26%27%28%29%2A+%2C%3B%3D";
	private static final String RESERVED_ENCODED = "%3A%2F%3F%23%5B%5D%40+%21%24%26%27%28%29%2A%2B%2C%3B%3D";
	private static final String COMMON = "\"%25<>\\^`{|}";
	private static final String COMMON_RECODED = "%22%25%3C%3E%5C%5E%60%7B%7C%7D";
	private static final String COMMON_ENCODED = "%22%2525%3C%3E%5C%5E%60%7B%7C%7D";
	private static final String SPECIAL = "spéçìal";
	private static final String SPECIAL_ENCODED_USASCII = "sp%3F%3F%3Fal";
	private static final String SPECIAL_ENCODED_ISO88591 = "sp%E9%E7%ECal";
	private static final String SPECIAL_ENCODED_UTF8 = "sp%C3%A9%C3%A7%C3%ACal";

	private QueryCoder c;

	@BeforeEach
	void setUp() {
		c = new QueryCoder();
	}

	@Test
	void getsInstance() {
		assertInstanceOf(QueryCoder.class, QueryCoder.getInstance());
	}

	@Test
	void doesNotRecodeUnreservedWithUSASCII() {
		assertEquals(UNRESERVED, c.recode(UNRESERVED, StandardCharsets.US_ASCII));
	}

	@Test
	void doesNotRecodeUnreservedWithISO88591() {
		assertEquals(UNRESERVED, c.recode(UNRESERVED, StandardCharsets.ISO_8859_1));
	}

	@Test
	void doesNotRecodeUnreservedWithUTF8() {
		assertEquals(UNRESERVED, c.recode(UNRESERVED, StandardCharsets.UTF_8));
	}

	@Test
	void recodesReservedWithUSASCII() {
		assertEquals(RESERVED_RECODED, c.recode(RESERVED, StandardCharsets.US_ASCII));
	}

	@Test
	void recodesReservedWithISO88591() {
		assertEquals(RESERVED_RECODED, c.recode(RESERVED, StandardCharsets.ISO_8859_1));
	}

	@Test
	void recodesReservedWithUTF8() {
		assertEquals(RESERVED_RECODED, c.recode(RESERVED, StandardCharsets.UTF_8));
	}

	@Test
	void recodesReservedRecodedWithUSASCII() {
		assertEquals(RESERVED_RECODED, c.recode(RESERVED_RECODED, StandardCharsets.US_ASCII));
	}

	@Test
	void recodesReservedRecodedWithISO88591() {
		assertEquals(RESERVED_RECODED, c.recode(RESERVED_RECODED, StandardCharsets.ISO_8859_1));
	}

	@Test
	void recodesReservedRecodedWithUTF8() {
		assertEquals(RESERVED_RECODED, c.recode(RESERVED_RECODED, StandardCharsets.UTF_8));
	}

	@Test
	void recodesReservedEncodedWithUSASCII() {
		assertEquals(RESERVED_ENCODED, c.recode(RESERVED_ENCODED, StandardCharsets.US_ASCII));
	}

	@Test
	void recodesReservedEncodedWithISO88591() {
		assertEquals(RESERVED_ENCODED, c.recode(RESERVED_ENCODED, StandardCharsets.ISO_8859_1));
	}

	@Test
	void recodesReservedEncodedWithUTF8() {
		assertEquals(RESERVED_ENCODED, c.recode(RESERVED_ENCODED, StandardCharsets.UTF_8));
	}

	@Test
	void recodesCommonWithUSASCII() {
		assertEquals(COMMON_RECODED, c.recode(COMMON, StandardCharsets.US_ASCII));
	}

	@Test
	void recodesCommonWithISO88591() {
		assertEquals(COMMON_RECODED, c.recode(COMMON, StandardCharsets.ISO_8859_1));
	}

	@Test
	void recodesCommonWithUTF8() {
		assertEquals(COMMON_RECODED, c.recode(COMMON, StandardCharsets.UTF_8));
	}

	@Test
	void recodesCommonRecodedWithUSASCII() {
		assertEquals(COMMON_RECODED, c.recode(COMMON_RECODED, StandardCharsets.US_ASCII));
	}

	@Test
	void recodesCommonRecodedWithISO88591() {
		assertEquals(COMMON_RECODED, c.recode(COMMON_RECODED, StandardCharsets.ISO_8859_1));
	}

	@Test
	void recodesCommonRecodedWithUTF8() {
		assertEquals(COMMON_RECODED, c.recode(COMMON_RECODED, StandardCharsets.UTF_8));
	}

	@Test
	void recodesCommonEncodedWithUSASCII() {
		assertEquals(COMMON_ENCODED, c.recode(COMMON_ENCODED, StandardCharsets.US_ASCII));
	}

	@Test
	void recodesCommonEncodedWithISO88591() {
		assertEquals(COMMON_ENCODED, c.recode(COMMON_ENCODED, StandardCharsets.ISO_8859_1));
	}

	@Test
	void recodesCommonEncodedWithUTF8() {
		assertEquals(COMMON_ENCODED, c.recode(COMMON_ENCODED, StandardCharsets.UTF_8));
	}

	@Test
	void recodesSpecialWithUSASCII() {
		assertEquals(SPECIAL_ENCODED_USASCII, c.recode(SPECIAL, StandardCharsets.US_ASCII));
	}

	@Test
	void recodesSpecialWithISO88591() {
		assertEquals(SPECIAL_ENCODED_ISO88591, c.recode(SPECIAL, StandardCharsets.ISO_8859_1));
	}

	@Test
	void recodesSpecialWithUTF8() {
		assertEquals(SPECIAL_ENCODED_UTF8, c.recode(SPECIAL, StandardCharsets.UTF_8));
	}

	@Test
	void recodesSpecialEncodedWithUSASCII() {
		assertEquals(SPECIAL_ENCODED_USASCII, c.recode(SPECIAL_ENCODED_USASCII, StandardCharsets.US_ASCII));
	}

	@Test
	void recodesSpecialEncodedWithISO88591() {
		assertEquals(SPECIAL_ENCODED_ISO88591, c.recode(SPECIAL_ENCODED_ISO88591, StandardCharsets.ISO_8859_1));
	}

	@Test
	void recodesSpecialEncodedWithUTF8() {
		assertEquals(SPECIAL_ENCODED_UTF8, c.recode(SPECIAL_ENCODED_UTF8, StandardCharsets.UTF_8));
	}

	@Test
	void doesNotEncodeUnreservedWithUSASCII() {
		assertEquals(UNRESERVED, c.encode(UNRESERVED, StandardCharsets.US_ASCII));
	}

	@Test
	void doesNotEncodeUnreservedWithISO88591() {
		assertEquals(UNRESERVED, c.encode(UNRESERVED, StandardCharsets.ISO_8859_1));
	}

	@Test
	void doesNotEncodeUnreservedWithUTF8() {
		assertEquals(UNRESERVED, c.encode(UNRESERVED, StandardCharsets.UTF_8));
	}

	@Test
	void encodesReservedWithUSASCII() {
		assertEquals(RESERVED_ENCODED, c.encode(RESERVED, StandardCharsets.US_ASCII));
	}

	@Test
	void encodesReservedWithISO88591() {
		assertEquals(RESERVED_ENCODED, c.encode(RESERVED, StandardCharsets.ISO_8859_1));
	}

	@Test
	void encodesReservedWithUTF8() {
		assertEquals(RESERVED_ENCODED, c.encode(RESERVED, StandardCharsets.UTF_8));
	}

	@Test
	void encodesCommonWithUSASCII() {
		assertEquals(COMMON_ENCODED, c.encode(COMMON, StandardCharsets.US_ASCII));
	}

	@Test
	void encodesCommonWithISO88591() {
		assertEquals(COMMON_ENCODED, c.encode(COMMON, StandardCharsets.ISO_8859_1));
	}

	@Test
	void encodesCommonWithUTF8() {
		assertEquals(COMMON_ENCODED, c.encode(COMMON, StandardCharsets.UTF_8));
	}

	@Test
	void encodesSpecialWithUSASCII() {
		assertEquals(SPECIAL_ENCODED_USASCII, c.encode(SPECIAL, StandardCharsets.US_ASCII));
	}

	@Test
	void encodesSpecialWithISO88591() {
		assertEquals(SPECIAL_ENCODED_ISO88591, c.encode(SPECIAL, StandardCharsets.ISO_8859_1));
	}

	@Test
	void encodesSpecialWithUTF8() {
		assertEquals(SPECIAL_ENCODED_UTF8, c.encode(SPECIAL, StandardCharsets.UTF_8));
	}
}
