package br.pro.hashi.sdx.rest.coding;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import java.nio.charset.StandardCharsets;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

class PathCoderTest {
	private AutoCloseable mocks;
	private @Mock QueryCoder coder;
	private PathCoder c;

	@BeforeEach
	void setUp() {
		mocks = MockitoAnnotations.openMocks(this);

		when(coder.recode(any(String.class), eq(StandardCharsets.UTF_8))).thenAnswer((invocation) -> {
			String item = invocation.getArgument(0);
			return item.replace(' ', '+');
		});

		c = new PathCoder(coder);
	}

	@AfterEach
	void tearDown() {
		assertDoesNotThrow(() -> {
			mocks.close();
		});
	}

	@Test
	void getsInstance() {
		assertInstanceOf(PathCoder.class, PathCoder.getInstance());
	}

	@ParameterizedTest
	@CsvSource({
			"a,                  a",
			"a,                  a/",
			"a,                  a//",
			"a,                  a///",
			"aa,                 aa",
			"aa,                 aa/",
			"aa,                 aa//",
			"aa,                 aa///",
			"aaa,                aaa",
			"aaa,                aaa/",
			"aaa,                aaa//",
			"aaa,                aaa///",
			"a/b,                a/b",
			"a/b,                a/b/",
			"a/b,                a/b//",
			"a/b,                a/b///",
			"aa//bb,             aa//bb",
			"aa//bb,             aa//bb/",
			"aa//bb,             aa//bb//",
			"aa//bb,             aa//bb///",
			"aaa///bbb,          aaa///bbb",
			"aaa///bbb,          aaa///bbb/",
			"aaa///bbb,          aaa///bbb//",
			"aaa///bbb,          aaa///bbb///",
			"a/b/c,              a/b/c",
			"a/b/c,              a/b/c/",
			"a/b/c,              a/b/c//",
			"a/b/c,              a/b/c///",
			"aa//bb//cc,         aa//bb//cc",
			"aa//bb//cc,         aa//bb//cc/",
			"aa//bb//cc,         aa//bb//cc//",
			"aa//bb//cc,         aa//bb//cc///",
			"aaa///bbb///ccc,    aaa///bbb///ccc",
			"aaa///bbb///ccc,    aaa///bbb///ccc/",
			"aaa///bbb///ccc,    aaa///bbb///ccc//",
			"aaa///bbb///ccc,    aaa///bbb///ccc///",
			"/,                  /",
			"/,                  //",
			"/,                  ///",
			"/a,                 /a",
			"/a,                 /a/",
			"/a,                 /a//",
			"/a,                 /a///",
			"/aa,                /aa",
			"/aa,                /aa/",
			"/aa,                /aa//",
			"/aa,                /aa///",
			"/aaa,               /aaa",
			"/aaa,               /aaa/",
			"/aaa,               /aaa//",
			"/aaa,               /aaa///",
			"/a/b,               /a/b",
			"/a/b,               /a/b/",
			"/a/b,               /a/b//",
			"/a/b,               /a/b///",
			"//aa//bb,           //aa//bb",
			"//aa//bb,           //aa//bb/",
			"//aa//bb,           //aa//bb//",
			"//aa//bb,           //aa//bb///",
			"///aaa///bbb,       ///aaa///bbb",
			"///aaa///bbb,       ///aaa///bbb/",
			"///aaa///bbb,       ///aaa///bbb//",
			"///aaa///bbb,       ///aaa///bbb///",
			"/a/b/c,             /a/b/c",
			"/a/b/c,             /a/b/c/",
			"/a/b/c,             /a/b/c//",
			"/a/b/c,             /a/b/c///",
			"//aa//bb//cc,       //aa//bb//cc",
			"//aa//bb//cc,       //aa//bb//cc/",
			"//aa//bb//cc,       //aa//bb//cc//",
			"//aa//bb//cc,       //aa//bb//cc///",
			"///aaa///bbb///ccc, ///aaa///bbb///ccc",
			"///aaa///bbb///ccc, ///aaa///bbb///ccc/",
			"///aaa///bbb///ccc, ///aaa///bbb///ccc//",
			"///aaa///bbb///ccc, ///aaa///bbb///ccc///" })
	void stripsEndingSlashes(String expected, String path) {
		assertEquals(expected, c.stripEndingSlashes(path));
	}

	@Test
	void splitsAndDecodesSlash() {
		assertArrayEquals(new String[] {}, splitAndDecode("/"));
	}

	@Test
	void splitsAndDecodesPlus() {
		assertArrayEquals(new String[] { "+" }, splitAndDecode("/+"));
	}

	@Test
	void splitsAndDecodesOneItem() {
		assertArrayEquals(new String[] { "a" }, splitAndDecode("/a"));
	}

	@Test
	void splitsAndDecodesOneItemDoubled() {
		assertArrayEquals(new String[] { "", "aa" }, splitAndDecode("//aa"));
	}

	@Test
	void splitsAndDecodesOneItemTripled() {
		assertArrayEquals(new String[] { "", "", "aaa" }, splitAndDecode("///aaa"));
	}

	@Test
	void splitsAndDecodesTwoItems() {
		assertArrayEquals(new String[] { "a", "b" }, splitAndDecode("/a/b"));
	}

	@Test
	void splitsAndDecodesTwoItemsDoubled() {
		assertArrayEquals(new String[] { "", "aa", "", "bb" }, splitAndDecode("//aa//bb"));
	}

	@Test
	void splitsAndDecodesTwoItemsTripled() {
		assertArrayEquals(new String[] { "", "", "aaa", "", "", "bbb" }, splitAndDecode("///aaa///bbb"));
	}

	@Test
	void splitsAndDecodesThreeItems() {
		assertArrayEquals(new String[] { "a", "b", "c" }, splitAndDecode("/a/b/c"));
	}

	@Test
	void splitsAndDecodesThreeItemsDoubled() {
		assertArrayEquals(new String[] { "", "aa", "", "bb", "", "cc" }, splitAndDecode("//aa//bb//cc"));
	}

	@Test
	void splitsAndDecodesThreeItemsTripled() {
		assertArrayEquals(new String[] { "", "", "aaa", "", "", "bbb", "", "", "ccc" }, splitAndDecode("///aaa///bbb///ccc"));
	}

	private String[] splitAndDecode(String path) {
		return c.splitAndDecode(path, StandardCharsets.UTF_8);
	}

	@Test
	void recodesPlus() {
		assertEquals("/%2B", recode("/+"));
	}

	@Test
	void recodesSpace() {
		assertEquals("/%20", recode("/ "));
	}

	@ParameterizedTest
	@CsvSource({
			"/,                  /",
			"/a,                 /a",
			"/aa,                /aa",
			"/aaa,               /aaa",
			"/a/b,               /a/b",
			"//aa//bb,           //aa//bb",
			"///aaa///bbb,       ///aaa///bbb",
			"/a/b/c,             /a/b/c",
			"//aa//bb//cc,       //aa//bb//cc",
			"///aaa///bbb///ccc, ///aaa///bbb///ccc" })
	void recodes(String expected, String path) {
		assertEquals(expected, recode(path));
	}

	private String recode(String path) {
		return c.recode(path, StandardCharsets.UTF_8);
	}
}
