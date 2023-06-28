package br.pro.hashi.sdx.rest.transform.mock;

import java.io.OutputStream;
import java.lang.reflect.Type;

import br.pro.hashi.sdx.rest.transform.Assembler;

public class ConcreteAssembler implements Assembler {
	@Override
	public <T> void write(T body, Type type, OutputStream stream) {
	}
}
