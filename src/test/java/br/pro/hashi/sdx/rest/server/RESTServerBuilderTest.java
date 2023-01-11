package br.pro.hashi.sdx.rest.server;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;

import br.pro.hashi.sdx.rest.Builder;
import br.pro.hashi.sdx.rest.BuilderTest;

class RESTServerBuilderTest extends BuilderTest {
	private RESTServerBuilder b;

	@Override
	protected Builder<?> newInstance() {
		b = new RESTServerBuilder();
		return b;
	}

	@Test
	void builds() {
		RESTServer server = b.build();
		assertNotNull(server);
	}
}
