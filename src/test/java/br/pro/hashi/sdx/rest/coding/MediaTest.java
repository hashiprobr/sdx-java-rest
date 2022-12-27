package br.pro.hashi.sdx.rest.coding;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

import org.junit.jupiter.api.Test;

import br.pro.hashi.sdx.rest.coding.exception.CharsetException;

class MediaTest {
	private static final String CONTENT_TYPE = "text/plain";
	private static final String USASCII_CONTENT = "usascii";
	private static final String SPECIAL_CONTENT = "spéçíál";

	private InputStream stream(String content, Charset charset) {
		return new ByteArrayInputStream(content.getBytes(charset));
	}

	private InputStream stream(String content) {
		return stream(content, Coding.CHARSET);
	}

	private void assertReads(String expected, InputStream stream) {
		byte[] actual = new byte[expected.length()];
		int b = assertDoesNotThrow(() -> {
			stream.readNBytes(actual, 0, actual.length);
			return stream.read();
		});
		assertEquals(-1, b);
		assertEquals(expected, new String(actual));
	}

	@Test
	void stripReturnsNull() {
		assertNull(Media.strip(""));
	}

	@Test
	void stripReturnsNullWithWhitespaces() {
		assertNull(Media.strip(" \t\n"));
	}

	@Test
	void stripReturnsNullWithParameters() {
		assertNull(Media.strip(";;parameter;;name=value;"));
	}

	@Test
	void stripReturnsNullWithWhitespacesAndParameters() {
		assertNull(Media.strip(" \t\n; \t\n; \t\nparameter \t\n; \t\n; \t\nname \t\n= \t\nvalue \t\n; \t\n"));
	}

	@Test
	void stripReturnsNotNull() {
		assertEquals(CONTENT_TYPE, Media.strip("text/plain"));
	}

	@Test
	void stripReturnsNotNullWithWhitespaces() {
		assertEquals(CONTENT_TYPE, Media.strip(" \t\ntext/plain \t\n"));
	}

	@Test
	void stripReturnsNotNullWithParameters() {
		assertEquals(CONTENT_TYPE, Media.strip("text/plain;;parameter;;name=value;"));
	}

	@Test
	void stripReturnsNotNullWithWhitespacesAndParameters() {
		assertEquals(CONTENT_TYPE, Media.strip(" \t\ntext/plain \t\n; \t\n; \t\nparameter \t\n; \t\n; \t\nname \t\n= \t\nvalue \t\n; \t\n"));
	}

	@Test
	void readsCorrectly() {
		InputStream stream = stream(SPECIAL_CONTENT);
		String content = assertDoesNotThrow(() -> {
			return Media.read(stream, null);
		});
		assertEquals(SPECIAL_CONTENT, content);
	}

	@Test
	void readsCorrectlyWithWhitespaces() {
		InputStream stream = stream(SPECIAL_CONTENT);
		String content = assertDoesNotThrow(() -> {
			return Media.read(stream, " \t\n");
		});
		assertEquals(SPECIAL_CONTENT, content);
	}

	@Test
	void readsCorrectlyWithParameters() {
		InputStream stream = stream(SPECIAL_CONTENT);
		String content = assertDoesNotThrow(() -> {
			return Media.read(stream, "text/plain;;parameter;;name=value;");
		});
		assertEquals(SPECIAL_CONTENT, content);
	}

	@Test
	void readsCorrectlyWithWhitespacesAndParameters() {
		InputStream stream = stream(SPECIAL_CONTENT);
		String content = assertDoesNotThrow(() -> {
			return Media.read(stream, " \t\ntext/plain \t\n; \t\n; \t\nparameter \t\n; \t\n; \t\nname \t\n= \t\nvalue \t\n; \t\n");
		});
		assertEquals(SPECIAL_CONTENT, content);
	}

	@Test
	void readsCorrectlyUSASCII() {
		InputStream stream = stream(USASCII_CONTENT, StandardCharsets.US_ASCII);
		String content = assertDoesNotThrow(() -> {
			return Media.read(stream, ";charset=us-ascii");
		});
		assertEquals(USASCII_CONTENT, content);
	}

	@Test
	void readsCorrectlyISO88591() {
		InputStream stream = stream(SPECIAL_CONTENT, StandardCharsets.ISO_8859_1);
		String content = assertDoesNotThrow(() -> {
			return Media.read(stream, ";charset=iso-8859-1");
		});
		assertEquals(SPECIAL_CONTENT, content);
	}

	@Test
	void readsCorrectlyISO88591WithWhitespaces() {
		InputStream stream = stream(SPECIAL_CONTENT, StandardCharsets.ISO_8859_1);
		String content = assertDoesNotThrow(() -> {
			return Media.read(stream, " \t\n; \t\ncharset \t\n= \t\niso-8859-1 \t\n");
		});
		assertEquals(SPECIAL_CONTENT, content);
	}

	@Test
	void readsCorrectlyISO88591WithParameters() {
		InputStream stream = stream(SPECIAL_CONTENT, StandardCharsets.ISO_8859_1);
		String content = assertDoesNotThrow(() -> {
			return Media.read(stream, "text/plain;;parameter;;charset=iso-8859-1;name=value;");
		});
		assertEquals(SPECIAL_CONTENT, content);
	}

	@Test
	void readsCorrectlyISO88591WithWhitespacesAndParameters() {
		InputStream stream = stream(SPECIAL_CONTENT, StandardCharsets.ISO_8859_1);
		String content = assertDoesNotThrow(() -> {
			return Media.read(stream, " \t\ntext/plain \t\n; \t\n; \t\nparameter \t\n; \t\n; \t\ncharset \t\n= \t\niso-8859-1 \t\n; \t\nname \t\n= \t\nvalue \t\n; \t\n");
		});
		assertEquals(SPECIAL_CONTENT, content);
	}

	@Test
	void readsCorrectlyUTF8() {
		InputStream stream = stream(SPECIAL_CONTENT, StandardCharsets.UTF_8);
		String content = assertDoesNotThrow(() -> {
			return Media.read(stream, ";charset=utf-8");
		});
		assertEquals(SPECIAL_CONTENT, content);
	}

	@Test
	void readsCorrectlyUSASCIIFromISO88591() {
		InputStream stream = stream(USASCII_CONTENT, StandardCharsets.ISO_8859_1);
		String content = assertDoesNotThrow(() -> {
			return Media.read(stream, "text/plain;charset=us-ascii");
		});
		assertEquals(USASCII_CONTENT, content);
	}

	@Test
	void readsCorrectlyUSASCIIFromUTF8() {
		InputStream stream = stream(USASCII_CONTENT, StandardCharsets.UTF_8);
		String content = assertDoesNotThrow(() -> {
			return Media.read(stream, "text/plain;charset=us-ascii");
		});
		assertEquals(USASCII_CONTENT, content);
	}

	@Test
	void readsCorrectlyISO88591FromUSASCII() {
		InputStream stream = stream(USASCII_CONTENT, StandardCharsets.US_ASCII);
		String content = assertDoesNotThrow(() -> {
			return Media.read(stream, "text/plain;charset=iso-8859-1");
		});
		assertEquals(USASCII_CONTENT, content);
	}

	@Test
	void readsCorrectlyISO88591FromUTF8() {
		InputStream stream = stream(USASCII_CONTENT, StandardCharsets.UTF_8);
		String content = assertDoesNotThrow(() -> {
			return Media.read(stream, "text/plain;charset=iso-8859-1");
		});
		assertEquals(USASCII_CONTENT, content);
	}

	@Test
	void readsCorrectlyUTF8FromUSASCII() {
		InputStream stream = stream(USASCII_CONTENT, StandardCharsets.US_ASCII);
		String content = assertDoesNotThrow(() -> {
			return Media.read(stream, "text/plain;charset=utf-8");
		});
		assertEquals(USASCII_CONTENT, content);
	}

	@Test
	void readsCorrectlyUTF8FromISO88591() {
		InputStream stream = stream(USASCII_CONTENT, StandardCharsets.ISO_8859_1);
		String content = assertDoesNotThrow(() -> {
			return Media.read(stream, "text/plain;charset=utf-8");
		});
		assertEquals(USASCII_CONTENT, content);
	}

	@Test
	void readsIncorrectlyUSASCIIFromISO88591() {
		InputStream stream = stream(SPECIAL_CONTENT, StandardCharsets.ISO_8859_1);
		String content = assertDoesNotThrow(() -> {
			return Media.read(stream, "text/plain;charset=us-ascii");
		});
		assertNotEquals(SPECIAL_CONTENT, content);
	}

	@Test
	void readsIncorrectlyUSASCIIFromUTF8() {
		InputStream stream = stream(SPECIAL_CONTENT, StandardCharsets.UTF_8);
		String content = assertDoesNotThrow(() -> {
			return Media.read(stream, "text/plain;charset=us-ascii");
		});
		assertNotEquals(SPECIAL_CONTENT, content);
	}

	@Test
	void readsIncorrectlyISO88591FromUSASCII() {
		InputStream stream = stream(SPECIAL_CONTENT, StandardCharsets.US_ASCII);
		String content = assertDoesNotThrow(() -> {
			return Media.read(stream, "text/plain;charset=iso-8859-1");
		});
		assertNotEquals(SPECIAL_CONTENT, content);
	}

	@Test
	void readsIncorrectlyISO88591FromUTF8() {
		InputStream stream = stream(SPECIAL_CONTENT, StandardCharsets.UTF_8);
		String content = assertDoesNotThrow(() -> {
			return Media.read(stream, "text/plain;charset=iso-8859-1");
		});
		assertNotEquals(SPECIAL_CONTENT, content);
	}

	@Test
	void readsIncorrectlyUTF8FromUSASCII() {
		InputStream stream = stream(SPECIAL_CONTENT, StandardCharsets.US_ASCII);
		String content = assertDoesNotThrow(() -> {
			return Media.read(stream, "text/plain;charset=utf-8");
		});
		assertNotEquals(SPECIAL_CONTENT, content);
	}

	@Test
	void readsIncorrectlyUTF8FromISO88591() {
		InputStream stream = stream(SPECIAL_CONTENT, StandardCharsets.ISO_8859_1);
		String content = assertDoesNotThrow(() -> {
			return Media.read(stream, "text/plain;charset=utf-8");
		});
		assertNotEquals(SPECIAL_CONTENT, content);
	}

	@Test
	void doesNotRead() {
		InputStream stream = InputStream.nullInputStream();
		assertDoesNotThrow(() -> {
			stream.close();
		});
		assertThrows(IOException.class, () -> {
			Media.read(stream, null);
		});
	}

	@Test
	void doesNotReadIllegal() {
		InputStream stream = stream(SPECIAL_CONTENT);
		assertThrows(CharsetException.class, () -> {
			Media.read(stream, ";charset==");
		});
	}

	@Test
	void doesNotReadUnsupported() {
		InputStream stream = stream(SPECIAL_CONTENT);
		assertThrows(CharsetException.class, () -> {
			Media.read(stream, ";charset=latin-1");
		});
	}

	@Test
	void decodesZeroChars() {
		assertReads("", Media.decode(stream(""), ";base64"));
	}

	@Test
	void decodesOneChar() {
		assertReads("0", Media.decode(stream("MA=="), ";base64"));
	}

	@Test
	void decodesTwoChars() {
		assertReads("01", Media.decode(stream("MDE="), ";base64"));
	}

	@Test
	void decodesThreeChars() {
		assertReads("012", Media.decode(stream("MDEy"), ";base64"));
	}

	@Test
	void decodesThreeCharsWithWhitespaces() {
		assertReads("012", Media.decode(stream("MDEy"), " \t\n; \t\nbase64 \t\n"));
	}

	@Test
	void decodesThreeCharsWithParameters() {
		assertReads("012", Media.decode(stream("MDEy"), "text/plain;;parameter;;base64;name=value;"));
	}

	@Test
	void decodesThreeCharsWithWhitespacesAndParameters() {
		assertReads("012", Media.decode(stream("MDEy"), " \t\ntext/plain \t\n; \t\n; \t\nparameter \t\n; \t\n; \t\nbase64 \t\n; \t\nname \t\n= \t\nvalue \t\n; \t\n"));
	}

	@Test
	void doesNotDecode() {
		assertReads("MDEy", Media.decode(stream("MDEy"), null));
	}

	@Test
	void doesNotDecodeWithWhitespaces() {
		assertReads("MDEy", Media.decode(stream("MDEy"), " \t\n"));
	}

	@Test
	void doesNotDecodeWithParameters() {
		assertReads("MDEy", Media.decode(stream("MDEy"), "text/plain;;parameter;;name=value;"));
	}

	@Test
	void doesNotDecodeWithWhitespacesAndParameters() {
		assertReads("MDEy", Media.decode(stream("MDEy"), " \t\ntext/plain \t\n; \t\n; \t\nparameter \t\n; \t\n; \t\nname \t\n= \t\nvalue \t\n; \t\n"));
	}

	@Test
	void encodesZeroChars() {
		assertReads("", Media.encode(stream("")));
	}

	@Test
	void encodesOneChar() {
		assertReads("MA==", Media.encode(stream("0")));
	}

	@Test
	void encodesTwoChars() {
		assertReads("MDE=", Media.encode(stream("01")));
	}

	@Test
	void encodesThreeChars() {
		assertReads("MDEy", Media.encode(stream("012")));
	}

	@Test
	void wraps() {
		InputStream stream = Media.encode(stream("012"));
		int length = assertDoesNotThrow(() -> {
			return stream.available();
		});
		assertEquals(4, length);
		int b = assertDoesNotThrow(() -> {
			return stream.read();
		});
		assertEquals(77, b);
		length = assertDoesNotThrow(() -> {
			return stream.available();
		});
		assertEquals(3, length);
		byte[] buffer = new byte[3];
		length = assertDoesNotThrow(() -> {
			return stream.readNBytes(buffer, 0, 3);
		});
		assertEquals(3, length);
		assertArrayEquals(new byte[] { 68, 69, 121 }, buffer);
		b = assertDoesNotThrow(() -> {
			return stream.read();
		});
		assertEquals(-1, b);
		assertDoesNotThrow(() -> {
			stream.close();
		});
	}
}
