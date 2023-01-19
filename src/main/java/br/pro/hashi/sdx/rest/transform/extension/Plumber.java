package br.pro.hashi.sdx.rest.transform.extension;

import java.io.IOException;
import java.io.PipedReader;
import java.io.PipedWriter;
import java.io.Reader;
import java.io.Writer;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.FutureTask;
import java.util.function.Consumer;

/**
 * A plumber can pipe a {@link Writer} output to a {@link Reader} input.
 */
public class Plumber {
	private final Executor executor;

	/**
	 * Constructs a new plumber.
	 */
	public Plumber() {
		this.executor = Executors.newCachedThreadPool();
	}

	/**
	 * <p>
	 * Pipes a writer consumer to a new reader. Chars written by the consumer can be
	 * read from the reader.
	 * </p>
	 * <p>
	 * If the consumer throws an exception, any subsequent attempt to read the
	 * reader throws the same exception wrapped in an {@link IOException}.
	 * </p>
	 * 
	 * @param consumer the consumer
	 * @return the reader
	 * @throws IOException if the reader cannot be connected to the writer
	 */
	public Reader connect(Consumer<Writer> consumer) throws IOException {
		PipedWriter writer = new PipedWriter();
		FutureTask<Void> task = new FutureTask<>(() -> {
			consumer.accept(writer);
			return null;
		});
		Reader reader = new CheckedPipedReader(writer, task);
		executor.execute(task);
		return reader;
	}

	static class CheckedPipedReader extends PipedReader {
		private FutureTask<Void> task;

		private CheckedPipedReader(PipedWriter writer, FutureTask<Void> task) throws IOException {
			super(writer);
			this.task = task;
		}

		FutureTask<Void> getTask() {
			return task;
		}

		void setTask(FutureTask<Void> task) {
			this.task = task;
		}

		public int read() throws IOException {
			int b = super.read();
			if (b == -1) {
				check();
			}
			return b;
		}

		public int read(char[] cbuf, int off, int len) throws IOException {
			int length = super.read(cbuf, off, len);
			if (length == -1) {
				check();
			}
			return length;
		}

		private void check() throws IOException {
			try {
				task.get();
			} catch (ExecutionException exception) {
				Throwable cause = exception.getCause();
				if (cause instanceof Exception) {
					cause = cause.getCause();
				}
				throw new IOException(cause);
			} catch (InterruptedException exception) {
				throw new AssertionError(exception);
			}
		}
	}

	/**
	 * Thrown to indicate that the consumer threw a checked exception.
	 */
	public static class Exception extends RuntimeException {
		private static final long serialVersionUID = 2014049919915961004L;

		/**
		 * Constructs a new exception with the specified cause and a detail message of
		 * {@code (cause == null ? null : cause.toString())}.
		 * 
		 * @param cause the cause
		 */
		public Exception(Throwable cause) {
			super(cause);
		}
	}
}
