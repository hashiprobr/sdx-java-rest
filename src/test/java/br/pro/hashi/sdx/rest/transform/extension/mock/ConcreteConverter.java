package br.pro.hashi.sdx.rest.transform.extension.mock;

import br.pro.hashi.sdx.rest.transform.extension.Converter;

public class ConcreteConverter extends Converter<Integer, Double> {
	@Override
	public Double to(Integer source) {
		return source.doubleValue();
	}

	@Override
	public Integer from(Double target) {
		return target.intValue();
	}
}
