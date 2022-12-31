package br.pro.hashi.sdx.rest.client;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import br.pro.hashi.sdx.rest.base.BuilderTest;

public class RESTClientBuilderTest extends BuilderTest {
	private RESTClientBuilder b;

	@BeforeEach
	void setUp() {
		mockConstructions();
		b = new RESTClientBuilder();
		getMocks();
		setBuilder(b);
	}

	@AfterEach
	void tearDown() {
		closeConstructions();
	}

	@Test
	void addsBinary() {
		testAddsBinary();
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
}
