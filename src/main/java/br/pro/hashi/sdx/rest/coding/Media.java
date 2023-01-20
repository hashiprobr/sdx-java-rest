package br.pro.hashi.sdx.rest.coding;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;
import java.io.UncheckedIOException;
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

	public static Reader reader(InputStream stream, String contentType) {
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

	public static String read(Reader reader) {
		StringBuilder builder = new StringBuilder();
		int length;
		char[] buffer = new char[8192];
		try {
			while ((length = reader.read(buffer, 0, buffer.length)) != -1) {
				builder.append(buffer, 0, length);
			}
			reader.close();
		} catch (IOException exception) {
			throw new UncheckedIOException(exception);
		}
		return builder.toString();
	}

	public static byte[] read(InputStream stream) {
		byte[] bytes;
		try {
			bytes = stream.readAllBytes();
		} catch (IOException exception) {
			throw new UncheckedIOException(exception);
		}
		return bytes;
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

	public static OutputStream encode(OutputStream stream) {
		return BASE64_ENCODER.wrap(stream);
	}

	public static InputStream encode(InputStream stream) {
		return new EncInputStream(stream);
	}

	private static class EncInputStream extends InputStream {
		private final InputStream stream;
		private final byte[] decoded;
		private final byte[] encoded;
		private int offset;
		private int remaining;

		private EncInputStream(InputStream stream) {
			this.stream = stream;
			this.decoded = new byte[6144];
			this.encoded = new byte[8192];
			this.offset = 0;
			this.remaining = 0;
		}

		@Override
		public int available() throws IOException {
			int length = stream.available();
			return length * 4 / 3 + remaining;
		}

		@Override
		public void close() throws IOException {
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
			if (remaining == 0) {
				length = stream.readNBytes(decoded, 0, decoded.length);
				if (length == 0) {
					return -1;
				}
				offset = 0;
				if (length == decoded.length) {
					BASE64_ENCODER.encode(decoded, encoded);
					remaining = encoded.length;
				} else {
					ByteBuffer decodedBuffer = ByteBuffer.wrap(decoded, 0, length);
					ByteBuffer encodedBuffer = BASE64_ENCODER.encode(decodedBuffer);
					remaining = encodedBuffer.remaining();
					encodedBuffer.get(encoded, 0, remaining);
				}
			}
			length = Math.min(len, remaining);
			int i = off;
			int j = offset;
			while (i < off + length) {
				b[i] = encoded[j];
				i++;
				j++;
			}
			offset += length;
			remaining -= length;
			return length;
		}
	}

	private Media() {
	}
}
