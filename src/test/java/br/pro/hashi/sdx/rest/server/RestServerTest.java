package br.pro.hashi.sdx.rest.server;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockConstruction;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.eclipse.jetty.server.Server;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedConstruction;

import br.pro.hashi.sdx.rest.server.exception.ServerException;

class RestServerTest {
	private Server jettyServer;
	private RestServer s;

	@BeforeEach
	void setUp() {
		jettyServer = mock(Server.class);
		s = new RestServer(jettyServer);
	}

	@Test
	void builds() {
		try (MockedConstruction<RestServerBuilder> construction = mockBuilderConstruction()) {
			assertSame(s, RestServer.from("package"));
			RestServerBuilder builder = construction.constructed().get(0);
			verify(builder).build("package");
		}
	}

	private MockedConstruction<RestServerBuilder> mockBuilderConstruction() {
		s = mock(RestServer.class);
		return mockConstruction(RestServerBuilder.class, (mock, context) -> {
			when(mock.build("package")).thenReturn(s);
		});
	}

	@Test
	void starts() throws Exception {
		when(jettyServer.isRunning()).thenReturn(false);
		s.start();
		verify(jettyServer).start();
	}

	@Test
	void doesNotStartIfJettyServerAlreadyStarted() throws Exception {
		when(jettyServer.isRunning()).thenReturn(true);
		s.start();
		verify(jettyServer, times(0)).start();
	}

	@Test
	void doesNotStartIfJettyServerThrowsException() throws Exception {
		when(jettyServer.isRunning()).thenReturn(false);
		Throwable cause = new Exception();
		doThrow(cause).when(jettyServer).start();
		Exception exception = assertThrows(ServerException.class, () -> {
			s.start();
		});
		assertSame(cause, exception.getCause());
	}

	@Test
	void stops() throws Exception {
		when(jettyServer.isRunning()).thenReturn(true);
		s.stop();
		verify(jettyServer).stop();
	}

	@Test
	void doesNotStopIfJettyServerAlreadyStopped() throws Exception {
		when(jettyServer.isRunning()).thenReturn(false);
		s.stop();
		verify(jettyServer, times(0)).stop();
	}

	@Test
	void doesNotStopIfJettyServerThrowsException() throws Exception {
		when(jettyServer.isRunning()).thenReturn(true);
		Throwable cause = new Exception();
		doThrow(cause).when(jettyServer).stop();
		Exception exception = assertThrows(ServerException.class, () -> {
			s.stop();
		});
		assertSame(cause, exception.getCause());
	}
}
