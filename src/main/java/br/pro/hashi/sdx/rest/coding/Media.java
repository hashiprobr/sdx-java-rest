package br.pro.hashi.sdx.rest.coding;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;
import java.io.UncheckedIOException;
import java.nio.charset.Charset;
import java.nio.charset.IllegalCharsetNameException;
import java.nio.charset.UnsupportedCharsetException;
import java.util.Base64;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import br.pro.hashi.sdx.rest.coding.exception.CharsetException;
import br.pro.hashi.sdx.rest.constant.Defaults;
import br.pro.hashi.sdx.rest.constant.Sizes;

public final class Media {
	private static final Base64.Encoder BASE64_ENCODER = Base64.getEncoder();
	private static final Base64.Decoder BASE64_DECODER = Base64.getDecoder();
	private static final Pattern BASE64_PATTERN = Pattern.compile("(?:[^;]*;)+\\s*base64\\s*(?:;[^;]*)*", Pattern.CASE_INSENSITIVE);
	private static final Pattern CHARSET_PATTERN = Pattern.compile("(?:[^;]*;)+\\s*charset\\s*=\\s*([^\\s;]+)\\s*(?:;[^;]*)*", Pattern.CASE_INSENSITIVE);

	public static String strip(String contentType) {
		int index = contentType.indexOf(';');
		if (index != -1) {
			contentType = contentType.substring(0, index);
		}
		contentType = contentType.strip();
		if (contentType.isEmpty()) {
			return null;
		}
		return contentType;
	}

	public static Reader reader(InputStream stream, String contentType) {
		Charset charset;
		if (contentType == null) {
			charset = Defaults.CHARSET;
		} else {
			Matcher matcher = CHARSET_PATTERN.matcher(contentType);
			if (matcher.matches()) {
				String charsetName = matcher.group(1);
				try {
					charset = Charset.forName(charsetName);
				} catch (IllegalCharsetNameException exception) {
					throw new CharsetException("Charset name %s is not legal".formatted(charsetName));
				} catch (UnsupportedCharsetException exception) {
					throw new CharsetException("Charset %s is not supported".formatted(charsetName));
				}
			} else {
				charset = Defaults.CHARSET;
			}
		}
		return new InputStreamReader(stream, charset);
	}

	public static String read(Reader reader) {
		StringBuilder builder = new StringBuilder();
		int length;
		char[] buffer = new char[Sizes.BUFFER];
		try {
			while ((length = reader.read(buffer, 0, buffer.length)) != -1) {
				builder.append(buffer, 0, length);
			}
		} catch (IOException exception) {
			throw new UncheckedIOException(exception);
		} finally {
			try {
				reader.close();
			} catch (IOException exception) {
				throw new UncheckedIOException(exception);
			}
		}
		return builder.toString();
	}

	public static byte[] read(InputStream stream) {
		byte[] bytes;
		try {
			bytes = stream.readAllBytes();
		} catch (IOException exception) {
			throw new UncheckedIOException(exception);
		} finally {
			try {
				stream.close();
			} catch (IOException exception) {
				throw new UncheckedIOException(exception);
			}
		}
		return bytes;
	}

	public static InputStream decode(InputStream stream, String contentType) {
		if (inBase64(contentType)) {
			stream = BASE64_DECODER.wrap(stream);
		}
		return stream;
	}

	private static boolean inBase64(String contentType) {
		return contentType != null && BASE64_PATTERN.matcher(contentType).matches();
	}

	public static OutputStream encode(OutputStream stream) {
		return BASE64_ENCODER.wrap(stream);
	}

	private Media() {
	}
}
