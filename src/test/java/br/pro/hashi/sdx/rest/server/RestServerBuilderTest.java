package br.pro.hashi.sdx.rest.server;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mockConstruction;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.eclipse.jetty.http.UriCompliance;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.util.ssl.SslContextFactory;
import org.junit.jupiter.api.Test;
import org.mockito.MockedConstruction;
import org.reflections.Reflections;

import br.pro.hashi.sdx.rest.Builder;
import br.pro.hashi.sdx.rest.BuilderTest;
import br.pro.hashi.sdx.rest.server.exception.ResourceException;
import br.pro.hashi.sdx.rest.server.mock.invalid.ResourceWithBlank;
import br.pro.hashi.sdx.rest.server.mock.invalid.ResourceWithNull;
import br.pro.hashi.sdx.rest.server.mock.invalid.ResourceWithoutCompliance;
import br.pro.hashi.sdx.rest.server.mock.invalid.ResourceWithoutDecoding;
import br.pro.hashi.sdx.rest.server.mock.invalid.ResourceWithoutSlash;
import br.pro.hashi.sdx.rest.server.tree.Tree;

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
	void initializesWithUnambiguousCompliance() {
		assertEquals(UriCompliance.RFC3986_UNAMBIGUOUS, b.getUriCompliance());
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
	void setsUriCompliance() {
		assertSame(b, b.withUriCompliance(UriCompliance.UNSAFE));
		assertEquals(UriCompliance.UNSAFE, b.getUriCompliance());
	}

	@Test
	void doesNotSetUriCompliance() {
		assertThrows(NullPointerException.class, () -> {
			b.withUriCompliance(null);
		});
		assertEquals(UriCompliance.RFC3986_UNAMBIGUOUS, b.getUriCompliance());
	}

	@Test
	void builds() {
		RestServer server = b.build("br.pro.hashi.sdx.rest.server.mock.valid");
		Server jettyServer = server.getJettyServer();
		RestHandler handler = (RestHandler) jettyServer.getHandler();
		Tree tree = handler.getTree();
		List<String> itemList = new ArrayList<>();
		assertNotNull(tree.getNodeAndAddItems(new String[] { "b" }, itemList));
		assertNotNull(tree.getNodeAndAddItems(new String[] { "c" }, itemList));
		assertNotNull(tree.getNodeAndAddItems(new String[] { "b", "c" }, itemList));
		assertNotNull(tree.getNodeAndAddItems(new String[] { "c", "d" }, itemList));
	}

	@Test
	void doesNotBuildIfBaseIsNull() {
		try (MockedConstruction<Reflections> construction = mockReflectionsConstruction(ResourceWithNull.class)) {
			assertThrows(ResourceException.class, () -> {
				b.build("package");
			});
		}
	}

	@Test
	void doesNotBuildIfBaseIsBlank() {
		try (MockedConstruction<Reflections> construction = mockReflectionsConstruction(ResourceWithBlank.class)) {
			assertThrows(ResourceException.class, () -> {
				b.build("package");
			});
		}
	}

	@Test
	void doesNotBuildIfBaseDoesNotStartWithSlash() {
		try (MockedConstruction<Reflections> construction = mockReflectionsConstruction(ResourceWithoutSlash.class)) {
			assertThrows(ResourceException.class, () -> {
				b.build("package");
			});
		}
	}

	@Test
	void doesNotBuildIfBaseDoesNotHaveDecoding() {
		try (MockedConstruction<Reflections> construction = mockReflectionsConstruction(ResourceWithoutDecoding.class)) {
			assertThrows(ResourceException.class, () -> {
				b.build("package");
			});
		}
	}

	@Test
	void doesNotBuildIfBaseDoesNotHaveCompliance() {
		try (MockedConstruction<Reflections> construction = mockReflectionsConstruction(ResourceWithoutCompliance.class)) {
			assertThrows(ResourceException.class, () -> {
				b.build("package");
			});
		}
	}

	private MockedConstruction<Reflections> mockReflectionsConstruction(Class<? extends RestResource> type) {
		return mockConstruction(Reflections.class, (mock, context) -> {
			when(mock.getSubTypesOf(RestResource.class)).thenReturn(Set.of(type));
		});
	}
}
