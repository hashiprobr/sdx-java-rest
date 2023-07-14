package br.pro.hashi.sdx.rest.coding;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

import java.nio.charset.StandardCharsets;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class QueryCoderTest {
	private static final String UNRESERVED_CONTENT = "0123456789BCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz-._~";
	private static final String RESERVED_CONTENT = ":/?#[]@ !$&'()*+,;=";
	private static final String RESERVED_RECODED = "%3A%2F%3F%23%5B%5D%40+%21%24%26%27%28%29%2A+%2C%3B%3D";
	private static final String RESERVED_ENCODED = "%3A%2F%3F%23%5B%5D%40+%21%24%26%27%28%29%2A%2B%2C%3B%3D";
	private static final String COMMON_CONTENT = "\"%25<>\\^`{|}";
	private static final String COMMON_RECODED = "%22%25%3C%3E%5C%5E%60%7B%7C%7D";
	private static final String COMMON_ENCODED = "%22%2525%3C%3E%5C%5E%60%7B%7C%7D";
	private static final String SPECIAL_CONTENT = "spéçìal";
	private static final String SPECIAL_USASCII = "sp%3F%3F%3Fal";
	private static final String SPECIAL_ISO88591 = "sp%E9%E7%ECal";
	private static final String SPECIAL_UTF8 = "sp%C3%A9%C3%A7%C3%ACal";

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
		assertEquals(UNRESERVED_CONTENT, c.recode(UNRESERVED_CONTENT, StandardCharsets.US_ASCII));
	}

	@Test
	void doesNotRecodeUnreservedWithISO88591() {
		assertEquals(UNRESERVED_CONTENT, c.recode(UNRESERVED_CONTENT, StandardCharsets.ISO_8859_1));
	}

	@Test
	void doesNotRecodeUnreservedWithUTF8() {
		assertEquals(UNRESERVED_CONTENT, c.recode(UNRESERVED_CONTENT, StandardCharsets.UTF_8));
	}

	@Test
	void recodesReservedWithUSASCII() {
		assertEquals(RESERVED_RECODED, c.recode(RESERVED_CONTENT, StandardCharsets.US_ASCII));
	}

	@Test
	void recodesReservedWithISO88591() {
		assertEquals(RESERVED_RECODED, c.recode(RESERVED_CONTENT, StandardCharsets.ISO_8859_1));
	}

	@Test
	void recodesReservedWithUTF8() {
		assertEquals(RESERVED_RECODED, c.recode(RESERVED_CONTENT, StandardCharsets.UTF_8));
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
		assertEquals(COMMON_RECODED, c.recode(COMMON_CONTENT, StandardCharsets.US_ASCII));
	}

	@Test
	void recodesCommonWithISO88591() {
		assertEquals(COMMON_RECODED, c.recode(COMMON_CONTENT, StandardCharsets.ISO_8859_1));
	}

	@Test
	void recodesCommonWithUTF8() {
		assertEquals(COMMON_RECODED, c.recode(COMMON_CONTENT, StandardCharsets.UTF_8));
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
		assertEquals(SPECIAL_USASCII, c.recode(SPECIAL_CONTENT, StandardCharsets.US_ASCII));
	}

	@Test
	void recodesSpecialWithISO88591() {
		assertEquals(SPECIAL_ISO88591, c.recode(SPECIAL_CONTENT, StandardCharsets.ISO_8859_1));
	}

	@Test
	void recodesSpecialWithUTF8() {
		assertEquals(SPECIAL_UTF8, c.recode(SPECIAL_CONTENT, StandardCharsets.UTF_8));
	}

	@Test
	void recodesSpecialEncodedWithUSASCII() {
		assertEquals(SPECIAL_USASCII, c.recode(SPECIAL_USASCII, StandardCharsets.US_ASCII));
	}

	@Test
	void recodesSpecialEncodedWithISO88591() {
		assertEquals(SPECIAL_ISO88591, c.recode(SPECIAL_ISO88591, StandardCharsets.ISO_8859_1));
	}

	@Test
	void recodesSpecialEncodedWithUTF8() {
		assertEquals(SPECIAL_UTF8, c.recode(SPECIAL_UTF8, StandardCharsets.UTF_8));
	}

	@Test
	void doesNotEncodeUnreservedWithUSASCII() {
		assertEquals(UNRESERVED_CONTENT, c.encode(UNRESERVED_CONTENT, StandardCharsets.US_ASCII));
	}

	@Test
	void doesNotEncodeUnreservedWithISO88591() {
		assertEquals(UNRESERVED_CONTENT, c.encode(UNRESERVED_CONTENT, StandardCharsets.ISO_8859_1));
	}

	@Test
	void doesNotEncodeUnreservedWithUTF8() {
		assertEquals(UNRESERVED_CONTENT, c.encode(UNRESERVED_CONTENT, StandardCharsets.UTF_8));
	}

	@Test
	void encodesReservedWithUSASCII() {
		assertEquals(RESERVED_ENCODED, c.encode(RESERVED_CONTENT, StandardCharsets.US_ASCII));
	}

	@Test
	void encodesReservedWithISO88591() {
		assertEquals(RESERVED_ENCODED, c.encode(RESERVED_CONTENT, StandardCharsets.ISO_8859_1));
	}

	@Test
	void encodesReservedWithUTF8() {
		assertEquals(RESERVED_ENCODED, c.encode(RESERVED_CONTENT, StandardCharsets.UTF_8));
	}

	@Test
	void encodesCommonWithUSASCII() {
		assertEquals(COMMON_ENCODED, c.encode(COMMON_CONTENT, StandardCharsets.US_ASCII));
	}

	@Test
	void encodesCommonWithISO88591() {
		assertEquals(COMMON_ENCODED, c.encode(COMMON_CONTENT, StandardCharsets.ISO_8859_1));
	}

	@Test
	void encodesCommonWithUTF8() {
		assertEquals(COMMON_ENCODED, c.encode(COMMON_CONTENT, StandardCharsets.UTF_8));
	}

	@Test
	void encodesSpecialWithUSASCII() {
		assertEquals(SPECIAL_USASCII, c.encode(SPECIAL_CONTENT, StandardCharsets.US_ASCII));
	}

	@Test
	void encodesSpecialWithISO88591() {
		assertEquals(SPECIAL_ISO88591, c.encode(SPECIAL_CONTENT, StandardCharsets.ISO_8859_1));
	}

	@Test
	void encodesSpecialWithUTF8() {
		assertEquals(SPECIAL_UTF8, c.encode(SPECIAL_CONTENT, StandardCharsets.UTF_8));
	}
}
