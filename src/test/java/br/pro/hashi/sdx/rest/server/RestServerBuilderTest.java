package br.pro.hashi.sdx.rest.server;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;

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
	void builds() {
		RestServer server = b.build("package");
		assertNotNull(server);
	}
}
