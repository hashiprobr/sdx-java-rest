package br.pro.hashi.sdx.rest.coding;

import java.net.URLDecoder;
import java.nio.charset.Charset;

public final class Percent {
	public static String strip(String uri) {
		int index = uri.length() - 1;
		while (index > 0 && uri.charAt(index) == '/') {
			index--;
		}
		index++;
		if (index < uri.length()) {
			uri = uri.substring(0, index);
		}
		return uri;
	}

	public static String[] encode(String uri, Charset charset) {
		String[] items = uri.split("/", -1);
		for (int i = 0; i < items.length; i++) {
			items[i] = Query.encode(items[i].replace("+", "%2B"), charset).replace("+", "%20");
		}
		return items;
	}

	public static String[] decode(String uri, Charset charset) {
		String[] items = uri.split("/", -1);
		for (int i = 0; i < items.length; i++) {
			items[i] = URLDecoder.decode(items[i].replace("+", "%2B"), charset);
		}
		return items;
	}

	private Percent() {
	}
}
