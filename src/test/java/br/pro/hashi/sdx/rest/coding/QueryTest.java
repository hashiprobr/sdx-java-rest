package br.pro.hashi.sdx.rest.coding;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import java.nio.charset.StandardCharsets;

import org.junit.jupiter.api.Test;

class QueryTest {
	private static final String UNRESERVED = "-.0123456789BCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz_~";
	private static final String RESERVED_DECODED = " !#$%25&'()*+,/:;=?@[]";
	private static final String RESERVED_ENCODED = "+%21%23%24%2525%26%27%28%29%2A%2B%2C%2F%3A%3B%3D%3F%40%5B%5D";
	private static final String RESERVED_RECODED = "+%21%23%24%25%26%27%28%29%2A+%2C%2F%3A%3B%3D%3F%40%5B%5D";
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
	void doesNotPreserveEncodeOfReservedWithUSASCII() {
		assertNotEquals(RESERVED_ENCODED, Query.encode(RESERVED_ENCODED, StandardCharsets.US_ASCII));
	}

	@Test
	void doesNotPreserveEncodeOfReservedWithISO88591() {
		assertNotEquals(RESERVED_ENCODED, Query.encode(RESERVED_ENCODED, StandardCharsets.ISO_8859_1));
	}

	@Test
	void doesNotPreserveEncodeOfReservedWithUTF8() {
		assertNotEquals(RESERVED_ENCODED, Query.encode(RESERVED_ENCODED, StandardCharsets.UTF_8));
	}

	@Test
	void doesNotPreserveEncodeOfCommonWithUSASCII() {
		assertNotEquals(COMMON_ENCODED, Query.encode(COMMON_ENCODED, StandardCharsets.US_ASCII));
	}

	@Test
	void doesNotPreserveEncodeOfCommonWithISO88591() {
		assertNotEquals(COMMON_ENCODED, Query.encode(COMMON_ENCODED, StandardCharsets.ISO_8859_1));
	}

	@Test
	void doesNotPreserveEncodeOfCommonWithUTF8() {
		assertNotEquals(COMMON_ENCODED, Query.encode(COMMON_ENCODED, StandardCharsets.UTF_8));
	}

	@Test
	void doesNotPreserveEncodeOfSpecialWithUSASCII() {
		assertNotEquals(SPECIAL_USASCII, Query.encode(SPECIAL_USASCII, StandardCharsets.US_ASCII));
	}

	@Test
	void doesNotPreserveEncodeOfSpecialWithISO88591() {
		assertNotEquals(SPECIAL_ISO88591, Query.encode(SPECIAL_ISO88591, StandardCharsets.ISO_8859_1));
	}

	@Test
	void doesNotPreserveEncodeOfSpecialWithUTF8() {
		assertNotEquals(SPECIAL_UTF8, Query.encode(SPECIAL_UTF8, StandardCharsets.UTF_8));
	}

	@Test
	void doesNotRecodeUnreservedWithUSASCII() {
		assertEquals(UNRESERVED, Query.recode(UNRESERVED, StandardCharsets.US_ASCII));
	}

	@Test
	void doesNotRecodeUnreservedWithISO88591() {
		assertEquals(UNRESERVED, Query.recode(UNRESERVED, StandardCharsets.ISO_8859_1));
	}

	@Test
	void doesNotRecodeUnreservedWithUTF8() {
		assertEquals(UNRESERVED, Query.recode(UNRESERVED, StandardCharsets.UTF_8));
	}

	@Test
	void recodesReservedWithUSASCII() {
		assertEquals(RESERVED_RECODED, Query.recode(RESERVED_DECODED, StandardCharsets.US_ASCII));
	}

	@Test
	void recodesReservedWithISO88591() {
		assertEquals(RESERVED_RECODED, Query.recode(RESERVED_DECODED, StandardCharsets.ISO_8859_1));
	}

	@Test
	void recodesReservedWithUTF8() {
		assertEquals(RESERVED_RECODED, Query.recode(RESERVED_DECODED, StandardCharsets.UTF_8));
	}

	@Test
	void recodesCommonWithUSASCII() {
		assertEquals(COMMON_ENCODED, Query.recode(COMMON_DECODED, StandardCharsets.US_ASCII));
	}

	@Test
	void recodesCommonWithISO88591() {
		assertEquals(COMMON_ENCODED, Query.recode(COMMON_DECODED, StandardCharsets.ISO_8859_1));
	}

	@Test
	void recodesCommonWithUTF8() {
		assertEquals(COMMON_ENCODED, Query.recode(COMMON_DECODED, StandardCharsets.UTF_8));
	}

	@Test
	void recodesSpecialWithUSASCII() {
		assertEquals(SPECIAL_USASCII, Query.recode(SPECIAL_DECODED, StandardCharsets.US_ASCII));
	}

	@Test
	void recodesSpecialWithISO88591() {
		assertEquals(SPECIAL_ISO88591, Query.recode(SPECIAL_DECODED, StandardCharsets.ISO_8859_1));
	}

	@Test
	void recodesSpecialWithUTF8() {
		assertEquals(SPECIAL_UTF8, Query.recode(SPECIAL_DECODED, StandardCharsets.UTF_8));
	}

	@Test
	void preservesRecodeOfReservedWithUSASCII() {
		assertEquals(RESERVED_RECODED, Query.recode(RESERVED_RECODED, StandardCharsets.US_ASCII));
	}

	@Test
	void preservesRecodeOfReservedWithISO88591() {
		assertEquals(RESERVED_RECODED, Query.recode(RESERVED_RECODED, StandardCharsets.ISO_8859_1));
	}

	@Test
	void preservesRecodeOfReservedWithUTF8() {
		assertEquals(RESERVED_RECODED, Query.recode(RESERVED_RECODED, StandardCharsets.UTF_8));
	}

	@Test
	void preservesRecodeOfCommonWithUSASCII() {
		assertEquals(COMMON_ENCODED, Query.recode(COMMON_ENCODED, StandardCharsets.US_ASCII));
	}

	@Test
	void preservesRecodeOfCommonWithISO88591() {
		assertEquals(COMMON_ENCODED, Query.recode(COMMON_ENCODED, StandardCharsets.ISO_8859_1));
	}

	@Test
	void preservesRecodeOfCommonWithUTF8() {
		assertEquals(COMMON_ENCODED, Query.recode(COMMON_ENCODED, StandardCharsets.UTF_8));
	}

	@Test
	void preservesRecodeOfSpecialWithUSASCII() {
		assertEquals(SPECIAL_USASCII, Query.recode(SPECIAL_USASCII, StandardCharsets.US_ASCII));
	}

	@Test
	void preservesRecodeOfSpecialWithISO88591() {
		assertEquals(SPECIAL_ISO88591, Query.recode(SPECIAL_ISO88591, StandardCharsets.ISO_8859_1));
	}

	@Test
	void preservesRecodeOfSpecialWithUTF8() {
		assertEquals(SPECIAL_UTF8, Query.recode(SPECIAL_UTF8, StandardCharsets.UTF_8));
	}
}
