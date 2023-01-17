package br.pro.hashi.sdx.rest.transform.extension;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;
import java.util.function.Consumer;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import br.pro.hashi.sdx.rest.transform.extension.Plumber.CheckedPipedReader;

class PlumberTest {
	private Plumber p;

	@BeforeEach
	void setUp() {
		p = new Plumber();
	}

	@Test
	void readsWhatConsumerWrites() throws IOException {
		Consumer<Writer> consumer = (writer) -> {
			try {
				writer.write("content");
			} catch (IOException exception) {
				throw new AssertionError(exception);
			}
		};
		Reader reader = assertDoesNotThrow(() -> {
			return p.connect(consumer);
		});
		char[] chars = new char[7];
		reader.read(chars, 0, 7);
		assertEquals(-1, reader.read(chars, 0, 1));
		assertEquals("content", new String(chars));
		reader.close();
	}

	@Test
	void throwsIOExceptionIfConsumerThrowsNonPlumberException() {
		RuntimeException cause = new RuntimeException();
		Consumer<Writer> consumer = (writer) -> {
			throw cause;
		};
		Reader reader = assertDoesNotThrow(() -> {
			return p.connect(consumer);
		});
		Exception exception = assertThrows(IOException.class, () -> {
			reader.read();
		});
		assertSame(cause, exception.getCause());
	}

	@Test
	void throwsIOExceptionIfConsumerThrowsPlumberException() {
		RuntimeException cause = new RuntimeException();
		Plumber.Exception plumberCause = new Plumber.Exception(cause);
		Consumer<Writer> consumer = (writer) -> {
			throw plumberCause;
		};
		Reader reader = assertDoesNotThrow(() -> {
			return p.connect(consumer);
		});
		Exception exception = assertThrows(IOException.class, () -> {
			reader.read();
		});
		assertSame(cause, exception.getCause());
	}

	@Test
	void throwsAssertionErrorIfTaskThrowsInterruptedException() throws ExecutionException, InterruptedException {
		Consumer<Writer> consumer = (writer) -> {
		};
		Reader reader = assertDoesNotThrow(() -> {
			return p.connect(consumer);
		});
		CheckedPipedReader checkedReader = (CheckedPipedReader) reader;
		FutureTask<Void> task = spy(checkedReader.task);
		when(task.get()).thenThrow(InterruptedException.class);
		checkedReader.task = task;
		assertThrows(AssertionError.class, () -> {
			reader.read();
		});
	}
}
