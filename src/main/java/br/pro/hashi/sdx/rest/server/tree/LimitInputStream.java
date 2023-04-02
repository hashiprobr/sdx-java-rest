package br.pro.hashi.sdx.rest.server.tree;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import br.pro.hashi.sdx.rest.server.exception.PayloadTooLargeException;

class LimitInputStream extends InputStream {
	private final InputStream stream;
	private final long maxSize;
	private long size;

	LimitInputStream(InputStream stream, long maxSize) {
		this.stream = stream;
		this.maxSize = maxSize;
		this.size = 0;
	}

	long getSize() {
		return size;
	}

	@Override
	public int read() throws IOException {
		int b = stream.read();
		if (b != -1) {
			add(1);
		}
		return b;
	}

	@Override
	public int read(byte[] b) throws IOException {
		int length = stream.read(b);
		if (length != -1) {
			add(length);
		}
		return length;
	}

	@Override
	public int read(byte[] b, int off, int len) throws IOException {
		int length = stream.read(b, off, len);
		if (length != -1) {
			add(length);
		}
		return length;
	}

	@Override
	public byte[] readAllBytes() throws IOException {
		byte[] b = stream.readAllBytes();
		add(b.length);
		return b;
	}

	@Override
	public byte[] readNBytes(int len) throws IOException {
		byte[] b = stream.readNBytes(len);
		add(b.length);
		return b;
	}

	@Override
	public int readNBytes(byte[] b, int off, int len) throws IOException {
		int length = stream.readNBytes(b, off, len);
		add(length);
		return length;
	}

	@Override
	public long skip(long n) throws IOException {
		long length = stream.skip(n);
		add(length);
		return length;
	}

	@Override
	public void skipNBytes(long n) throws IOException {
		stream.skipNBytes(n);
		add(n);
	}

	private void add(long length) {
		size += length;
		if (size > maxSize) {
			throw new PayloadTooLargeException("Request body exceeds %d bytes".formatted(maxSize));
		}
	}

	@Override
	public int available() throws IOException {
		return stream.available();
	}

	@Override
	public void close() throws IOException {
		stream.close();
	}

	@Override
	public void mark(int readlimit) {
		stream.mark(readlimit);
	}

	@Override
	public void reset() throws IOException {
		stream.reset();
	}

	@Override
	public boolean markSupported() {
		return stream.markSupported();
	}

	@Override
	public long transferTo(OutputStream out) throws IOException {
		return stream.transferTo(out);
	}
}
