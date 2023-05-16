package br.pro.hashi.sdx.rest.coding;

import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.Charset;

public class Query {
	public static String encode(String item, Charset charset) {
		item = URLEncoder.encode(item, charset);
		return makeConsistentWithRfc3986(item);
	}

	// decode ~
	// encode *
	private static String makeConsistentWithRfc3986(String item) {
		StringBuilder builder = new StringBuilder();
		for (int i = 0; i < item.length(); i++) {
			char c = item.charAt(i);
			switch (c) {
			case '%':
				i++;
				char x = item.charAt(i);
				i++;
				char y = item.charAt(i);
				if (x == '7' && y == 'E') {
					builder.append('~');
				} else {
					builder.append(c);
					builder.append(x);
					builder.append(y);
				}
				break;
			case '*':
				builder.append("%2A");
				break;
			default:
				builder.append(c);
			}
		}
		return builder.toString();
	}

	public static String recode(String item, Charset charset) {
		item = URLDecoder.decode(item, charset);
		return encode(item, charset);
	}

	private Query() {
	}
}
