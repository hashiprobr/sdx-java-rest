package br.pro.hashi.sdx.rest.server;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockConstruction;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.mockito.MockedConstruction;

class RestServerTest {
	private RestServer s;

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
}
