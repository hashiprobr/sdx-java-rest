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
	void putsUncheckedSerializer() {
		testPutsUncheckedSerializer();
	}
}
