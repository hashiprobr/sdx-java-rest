package br.pro.hashi.sdx.rest.client;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;

import java.io.InputStream;

import org.eclipse.jetty.http.HttpFields;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import br.pro.hashi.sdx.rest.transform.facade.Facade;

class RestResponseTest {
	private Facade facade;
	private HttpFields fields;
	private RestResponse r;

	@BeforeEach
	void setUp() {
		facade = mock(Facade.class);
		fields = mock(HttpFields.class);
		r = new RestResponse(facade, 600, fields, "type/subtype", InputStream.nullInputStream());
	}

	@Test
	void stub() {
		assertNotNull(r);
	}
}
