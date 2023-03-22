package br.pro.hashi.sdx.rest.server;

import java.io.OutputStream;

class CountOutputStream extends OutputStream {
	private long count;

	CountOutputStream() {
		this.count = 0;
	}

	long getCount() {
		return count;
	}

	@Override
	public void write(int b) {
		write(new byte[] { (byte) b });
	}

	@Override
	public void write(byte[] b) {
		write(b, 0, b.length);
	}

	@Override
	public void write(byte[] b, int off, int len) {
		if (b == null) {
			throw new NullPointerException();
		}
		if (off < 0) {
			throw new IndexOutOfBoundsException(off);
		}
		if (len < 0) {
			throw new IndexOutOfBoundsException(len);
		}
		int index = off + len;
		if (index > b.length) {
			throw new IndexOutOfBoundsException(index - 1);
		}
		count += len;
	}
}
