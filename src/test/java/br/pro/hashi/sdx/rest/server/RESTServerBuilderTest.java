package br.pro.hashi.sdx.rest.server;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.mockConstruction;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedConstruction;

import br.pro.hashi.sdx.rest.base.BuilderTest;

class RESTServerBuilderTest extends BuilderTest {
	private RESTServerBuilder b;

	@BeforeEach
	void setUp() {
		mockConstructions();
		b = new RESTServerBuilder();
		getTransformer();
		setBuilder(b);
	}

	@AfterEach
	void tearDown() {
		closeConstructions();
	}

	@Test
	void initializesWithURLCharsetUTF8() {
		testInitializesWithURLCharsetUTF8();
	}

	@Test
	void initializesWithoutNullBody() {
		testInitializesWithoutNullBody();
	}

	@Test
	void initializesWithoutRedirection() {
		testInitializesWithoutRedirection();
	}

	@Test
	void initializesWithCompression() {
		testInitializesWithCompression();
	}

	@Test
	void addsBinary() {
		testAddsBinary();
	}

	@Test
	void putsAssembler() {
		testPutsAssembler();
	}

	@Test
	void removesAssembler() {
		testRemovesAssembler();
	}

	@Test
	void putsDisassembler() {
		testPutsDisassembler();
	}

	@Test
	void removesDisassembler() {
		testRemovesDisassembler();
	}

	@Test
	void putsSerializer() {
		testPutsSerializer();
	}

	@Test
	void putsGsonSerializer() {
		testPutsGsonSerializer();
	}

	@Test
	void putsUncheckedSerializer() {
		testPutsUncheckedSerializer();
	}

	@Test
	void doesNotPutUncheckedSerializerIfNull() {
		testDoesNotPutUncheckedSerializerIfNull();
	}

	@Test
	void doesNotPutUncheckedSerializerIfBlank() {
		testDoesNotPutUncheckedSerializerIfBlank();
	}

	@Test
	void removesSerializer() {
		testRemovesSerializer();
	}

	@Test
	void putsDeserializer() {
		testPutsDeserializer();
	}

	@Test
	void putsGsonDeserializer() {
		testPutsGsonDeserializer();
	}

	@Test
	void putsSafeDeserializer() {
		testPutsSafeDeserializer();
	}

	@Test
	void doesNotPutSafeDeserializerIfNull() {
		testDoesNotPutSafeDeserializerIfNull();
	}

	@Test
	void doesNotPutSafeDeserializerIfBlank() {
		testDoesNotPutSafeDeserializerIfBlank();
	}

	@Test
	void removesDeserializer() {
		testRemovesDeserializer();
	}

	@Test
	void setsURLCharset() {
		testSetsURLCharset();
	}

	@Test
	void doesNotSetURLCharset() {
		testDoesNotSetURLCharset();
	}

	@Test
	void setsNullBody() {
		testSetsNullBody();
	}

	@Test
	void setsRedirection() {
		testSetsRedirection();
	}

	@Test
	void setsCompression() {
		testSetsCompression();
	}

	@Test
	void builds() {
		try (MockedConstruction<RESTServer> construction = mockConstruction(RESTServer.class)) {
			RESTServer server = b.build();
			assertSame(server, construction.constructed().get(0));
		}
	}
}
