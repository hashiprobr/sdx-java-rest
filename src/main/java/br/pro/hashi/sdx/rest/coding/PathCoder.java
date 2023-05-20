package br.pro.hashi.sdx.rest.coding;

import java.net.URLDecoder;
import java.nio.charset.Charset;

public class PathCoder {
	private static final PathCoder INSTANCE = newInstance();

	private static PathCoder newInstance() {
		QueryCoder coder = QueryCoder.getInstance();
		return new PathCoder(coder);
	}

	public static PathCoder getInstance() {
		return INSTANCE;
	}

	private final QueryCoder coder;

	PathCoder(QueryCoder coder) {
		this.coder = coder;
	}

	public String stripEndingSlashes(String path) {
		int last = path.length() - 1;
		while (last > 0 && path.charAt(last) == '/') {
			last--;
		}
		int length = last + 1;
		if (length < path.length()) {
			path = path.substring(0, length);
		}
		return path;
	}

	public String[] splitAndDecode(String path, Charset charset) {
		path = path.substring(1);
		if (path.isEmpty()) {
			return new String[] {};
		}
		String[] items = path.split("/", -1);
		for (int i = 0; i < items.length; i++) {
			String item = items[i].replace("+", "%2B");
			items[i] = URLDecoder.decode(item, charset);
		}
		return items;
	}

	public String recode(String path, Charset charset) {
		String[] items = path.split("/", -1);
		for (int i = 0; i < items.length; i++) {
			String item = items[i].replace("+", "%2B");
			item = coder.recode(item, charset);
			items[i] = item.replace("+", "%20");
		}
		return String.join("/", items);
	}
}
