package br.pro.hashi.sdx.rest.transform.manager;

import java.io.Reader;
import java.lang.reflect.Type;

import br.pro.hashi.sdx.rest.coding.MediaCoder;
import br.pro.hashi.sdx.rest.constant.Types;
import br.pro.hashi.sdx.rest.reflection.ParserFactory;
import br.pro.hashi.sdx.rest.transform.Deserializer;
import br.pro.hashi.sdx.rest.transform.exception.TypeException;

class DefaultDeserializer implements Deserializer {
	private static final DefaultDeserializer INSTANCE = newInstance();

	private static DefaultDeserializer newInstance() {
		ParserFactory factory = ParserFactory.getInstance();
		MediaCoder coder = MediaCoder.getInstance();
		return new DefaultDeserializer(factory, coder);
	}

	public static DefaultDeserializer getInstance() {
		return INSTANCE;
	}

	private final ParserFactory factory;
	private final MediaCoder coder;

	DefaultDeserializer(ParserFactory factory, MediaCoder coder) {
		this.factory = factory;
		this.coder = coder;
	}

	@Override
	public <T> T read(Reader reader, Type type) {
		if (Types.equalsSimple(type)) {
			@SuppressWarnings("unchecked")
			T body = (T) factory.get((Class<?>) type).apply(coder.read(reader));
			return body;
		}
		if (type.equals(Reader.class)) {
			@SuppressWarnings("unchecked")
			T body = (T) reader;
			return body;
		}
		throw new TypeException("Type must be primitive or equal to BigInteger, BigDecimal, String, or Reader");
	}
}
