package br.pro.hashi.sdx.rest.coding;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.charset.IllegalCharsetNameException;
import java.nio.charset.UnsupportedCharsetException;
import java.util.Base64;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import br.pro.hashi.sdx.rest.coding.exception.CharsetException;

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

	public static String read(Reader reader) throws IOException {
		StringBuilder builder = new StringBuilder();
		int length;
		char[] buffer = new char[8192];
		while ((length = reader.read(buffer, 0, buffer.length)) != -1) {
			builder.append(buffer, 0, length);
		}
		reader.close();
		return builder.toString();
	}

	public static Reader reader(InputStream stream, String contentType) throws CharsetException {
		Charset charset;
		if (contentType == null) {
			charset = Coding.CHARSET;
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
				charset = Coding.CHARSET;
			}
		}
		return new InputStreamReader(stream, charset);
	}

	public static InputStream decode(InputStream stream, String contentType) {
		if (hasBase64(contentType)) {
			stream = BASE64_DECODER.wrap(stream);
		}
		return stream;
	}

	private static boolean hasBase64(String contentType) {
		return contentType != null && BASE64_PATTERN.matcher(contentType).matches();
	}

	public static InputStream encode(InputStream stream) {
		return new EncInputStream(stream);
	}

	private static class EncInputStream extends InputStream {
		private final InputStream stream;
		private final byte[] input;
		private ByteBuffer output;

		private EncInputStream(InputStream stream) {
			this.stream = stream;
			this.input = new byte[12288];
			this.output = null;
		}

		@Override
		public int available() throws IOException {
			int length = stream.available() * 4 / 3;
			if (output != null) {
				length += output.remaining();
			}
			return length;
		}

		@Override
		public void close() throws IOException {
			output = null;
			stream.close();
		}

		@Override
		public int read() throws IOException {
			byte[] b = new byte[1];
			int length = readNBytes(b, 0, 1);
			if (length == 0) {
				return -1;
			}
			return b[0];
		}

		@Override
		public int read(byte[] b, int off, int len) throws IOException {
			int length;
			if (output == null) {
				length = stream.readNBytes(input, 0, input.length);
				if (length == 0) {
					return -1;
				}
				ByteBuffer buffer = ByteBuffer.wrap(input, 0, length);
				output = BASE64_ENCODER.encode(buffer);
			}
			length = output.remaining();
			if (len < length) {
				output.get(b, off, len);
				length = len;
			} else {
				output.get(b, off, length);
				output = null;
			}
			return length;
		}
	}

	private Media() {
	}
}
