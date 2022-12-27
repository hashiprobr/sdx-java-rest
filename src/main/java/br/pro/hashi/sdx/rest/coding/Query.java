package br.pro.hashi.sdx.rest.coding;

import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.Charset;

public final class Query {
	public static String encode(String item, Charset charset) {
		return URLEncoder.encode(URLDecoder.decode(item, charset), charset).replace("%7E", "~").replace("*", "%2A");
	}

	private Query() {
	}
}
