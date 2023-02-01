package br.pro.hashi.sdx.rest.server;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mockConstruction;
import static org.mockito.Mockito.verify;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.util.ssl.SslContextFactory;
import org.junit.jupiter.api.Test;
import org.mockito.MockedConstruction;

import br.pro.hashi.sdx.rest.Builder;
import br.pro.hashi.sdx.rest.BuilderTest;

class RestServerBuilderTest extends BuilderTest {
	private RestServerBuilder b;

	@Override
	protected Builder<?> newInstance() {
		b = new RestServerBuilder();
		return b;
	}

	@Test
	void initializesWithoutFactory() {
		assertNull(b.getFactory());
	}

	@Test
	void setsKeyStore() {
		try (MockedConstruction<SslContextFactory.Server> construction = mockConstruction(SslContextFactory.Server.class)) {
			String path = "path";
			String password = "password";
			b.withKeyStore(path, password);
			SslContextFactory.Server factory = construction.constructed().get(0);
			verify(factory).setKeyStorePath(path);
			verify(factory).setKeyStorePassword(password);
			assertEquals(factory, b.getFactory());
		}
	}

	@Test
	void doesNotSetKeyStoreIfPathIsNull() {
		assertThrows(NullPointerException.class, () -> {
			b.withKeyStore(null, "password");
		});
		assertNull(b.getFactory());
	}

	@Test
	void doesNotSetKeyStoreIfPathIsEmpty() {
		assertThrows(IllegalArgumentException.class, () -> {
			b.withKeyStore("", "password");
		});
		assertNull(b.getFactory());
	}

	@Test
	void doesNotSetKeyStoreIfPasswordIsNull() {
		assertThrows(NullPointerException.class, () -> {
			b.withKeyStore("path", null);
		});
		assertNull(b.getFactory());
	}

	@Test
	void doesNotSetKeyStoreIfPasswordIsEmpty() {
		assertThrows(IllegalArgumentException.class, () -> {
			b.withKeyStore("path", "");
		});
		assertNull(b.getFactory());
	}

	@Test
	void builds() {
		RestServer server = b.build("package");
		Server jettyServer = server.getJettyServer();
		assertNotNull(jettyServer);
	}
}
