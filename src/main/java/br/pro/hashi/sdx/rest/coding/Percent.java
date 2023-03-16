package br.pro.hashi.sdx.rest.coding;

import java.net.URLDecoder;
import java.nio.charset.Charset;

public final class Percent {
	public static String stripEndingSlashes(String uri) {
		int last = uri.length() - 1;
		while (last > 0 && uri.charAt(last) == '/') {
			last--;
		}
		int length = last + 1;
		if (length < uri.length()) {
			uri = uri.substring(0, length);
		}
		return uri;
	}

	public static String recode(String uri, Charset charset) {
		String[] items = uri.split("/", -1);
		for (int i = 0; i < items.length; i++) {
			String item = items[i].replace("+", "%2B");
			item = Query.recode(item, charset);
			items[i] = item.replace("+", "%20");
		}
		return String.join("/", items);
	}

	public static String[] splitAndDecode(String uri, Charset charset) {
		String[] items = uri.split("/", -1);
		for (int i = 0; i < items.length; i++) {
			String item = items[i].replace("+", "%2B");
			items[i] = URLDecoder.decode(item, charset);
		}
		return items;
	}

	private Percent() {
	}
}
