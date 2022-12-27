package br.pro.hashi.sdx.rest.coding;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.nio.charset.StandardCharsets;

import org.junit.jupiter.api.Test;

class QueryTest {
	private static final String UNRESERVED = "-.0123456789BCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz_~";
	private static final String RESERVED_DECODED = " !#$%25&'()*+,/:;=?@[]";
	private static final String RESERVED_ENCODED = "+%21%23%24%25%26%27%28%29%2A+%2C%2F%3A%3B%3D%3F%40%5B%5D";
	private static final String COMMON_DECODED = "\"<>\\^`{|}";
	private static final String COMMON_ENCODED = "%22%3C%3E%5C%5E%60%7B%7C%7D";
	private static final String SPECIAL_DECODED = "spéçíál";
	private static final String SPECIAL_USASCII = "sp%3F%3F%3F%3Fl";
	private static final String SPECIAL_ISO88591 = "sp%E9%E7%ED%E1l";
	private static final String SPECIAL_UTF8 = "sp%C3%A9%C3%A7%C3%AD%C3%A1l";

	@Test
	void doesNotEncodeUnreservedWithUSASCII() {
		assertEquals(UNRESERVED, Query.encode(UNRESERVED, StandardCharsets.US_ASCII));
	}

	@Test
	void doesNotEncodeUnreservedWithISO88591() {
		assertEquals(UNRESERVED, Query.encode(UNRESERVED, StandardCharsets.ISO_8859_1));
	}

	@Test
	void doesNotEncodeUnreservedWithUTF8() {
		assertEquals(UNRESERVED, Query.encode(UNRESERVED, StandardCharsets.UTF_8));
	}

	@Test
	void encodesReservedWithUSASCII() {
		assertEquals(RESERVED_ENCODED, Query.encode(RESERVED_DECODED, StandardCharsets.US_ASCII));
	}

	@Test
	void encodesReservedWithISO88591() {
		assertEquals(RESERVED_ENCODED, Query.encode(RESERVED_DECODED, StandardCharsets.ISO_8859_1));
	}

	@Test
	void encodesReservedWithUTF8() {
		assertEquals(RESERVED_ENCODED, Query.encode(RESERVED_DECODED, StandardCharsets.UTF_8));
	}

	@Test
	void encodesCommonWithUSASCII() {
		assertEquals(COMMON_ENCODED, Query.encode(COMMON_DECODED, StandardCharsets.US_ASCII));
	}

	@Test
	void encodesCommonWithISO88591() {
		assertEquals(COMMON_ENCODED, Query.encode(COMMON_DECODED, StandardCharsets.ISO_8859_1));
	}

	@Test
	void encodesCommonWithUTF8() {
		assertEquals(COMMON_ENCODED, Query.encode(COMMON_DECODED, StandardCharsets.UTF_8));
	}

	@Test
	void encodesSpecialWithUSASCII() {
		assertEquals(SPECIAL_USASCII, Query.encode(SPECIAL_DECODED, StandardCharsets.US_ASCII));
	}

	@Test
	void encodesSpecialWithISO88591() {
		assertEquals(SPECIAL_ISO88591, Query.encode(SPECIAL_DECODED, StandardCharsets.ISO_8859_1));
	}

	@Test
	void encodesSpecialWithUTF8() {
		assertEquals(SPECIAL_UTF8, Query.encode(SPECIAL_DECODED, StandardCharsets.UTF_8));
	}

	@Test
	void decodesAndEncodesReservedWithUSASCII() {
		assertEquals(RESERVED_ENCODED, Query.encode(RESERVED_ENCODED, StandardCharsets.US_ASCII));
	}

	@Test
	void decodesAndEncodesReservedWithISO88591() {
		assertEquals(RESERVED_ENCODED, Query.encode(RESERVED_ENCODED, StandardCharsets.ISO_8859_1));
	}

	@Test
	void decodesAndEncodesReservedWithUTF8() {
		assertEquals(RESERVED_ENCODED, Query.encode(RESERVED_ENCODED, StandardCharsets.UTF_8));
	}

	@Test
	void decodesAndEncodesCommonWithUSASCII() {
		assertEquals(COMMON_ENCODED, Query.encode(COMMON_ENCODED, StandardCharsets.US_ASCII));
	}

	@Test
	void decodesAndEncodesCommonWithISO88591() {
		assertEquals(COMMON_ENCODED, Query.encode(COMMON_ENCODED, StandardCharsets.ISO_8859_1));
	}

	@Test
	void decodesAndEncodesCommonWithUTF8() {
		assertEquals(COMMON_ENCODED, Query.encode(COMMON_ENCODED, StandardCharsets.UTF_8));
	}

	@Test
	void decodesAndEncodesSpecialWithUSASCII() {
		assertEquals(SPECIAL_USASCII, Query.encode(SPECIAL_USASCII, StandardCharsets.US_ASCII));
	}

	@Test
	void decodesAndEncodesSpecialWithISO88591() {
		assertEquals(SPECIAL_ISO88591, Query.encode(SPECIAL_ISO88591, StandardCharsets.ISO_8859_1));
	}

	@Test
	void decodesAndEncodesSpecialWithUTF8() {
		assertEquals(SPECIAL_UTF8, Query.encode(SPECIAL_UTF8, StandardCharsets.UTF_8));
	}
}
