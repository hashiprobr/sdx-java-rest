package br.pro.hashi.sdx.rest.server;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import br.pro.hashi.sdx.rest.base.BuilderTest;

class RESTServerBuilderTest extends BuilderTest {
	private RESTServerBuilder b;

	@BeforeEach
	void setUp() {
		mockConstructions();
		b = new RESTServerBuilder();
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
	void putsUncheckedSerializer() {
		testPutsUncheckedSerializer();
	}
}
