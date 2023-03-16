package br.pro.hashi.sdx.rest.transform.facade;

import java.io.InputStream;
import java.io.Reader;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import br.pro.hashi.sdx.rest.coding.Media;
import br.pro.hashi.sdx.rest.transform.Assembler;
import br.pro.hashi.sdx.rest.transform.Deserializer;
import br.pro.hashi.sdx.rest.transform.Disassembler;
import br.pro.hashi.sdx.rest.transform.Hint;
import br.pro.hashi.sdx.rest.transform.Serializer;
import br.pro.hashi.sdx.rest.transform.facade.exception.SupportException;

public class Facade {
	private static final String OCTET_TYPE = "application/octet-stream";
	private static final String PLAIN_TYPE = "text/plain";

	private final Set<Class<?>> binaryClasses;
	private final Set<ParameterizedType> binaryParameterizedTypes;
	private final Map<String, Assembler> assemblers;
	private final Map<String, Disassembler> disassemblers;
	private final Map<String, Serializer> serializers;
	private final Map<String, Deserializer> deserializers;
	private String fallbackByteType;
	private String fallbackTextType;

	public Facade() {
		this.binaryClasses = new HashSet<>();
		this.binaryClasses.addAll(Set.of(byte[].class, InputStream.class));

		this.binaryParameterizedTypes = new HashSet<>();

		this.assemblers = new HashMap<>();
		this.assemblers.put(OCTET_TYPE, new OctetAssembler());

		this.disassemblers = new HashMap<>();
		this.disassemblers.put(OCTET_TYPE, new OctetDisassembler());

		this.serializers = new HashMap<>();
		this.serializers.put(PLAIN_TYPE, new PlainSerializer());

		this.deserializers = new HashMap<>();
		this.deserializers.put(PLAIN_TYPE, new PlainDeserializer());

		this.fallbackByteType = null;
		this.fallbackTextType = null;
	}

	public boolean isBinary(Type type) {
		if (type == null) {
			return false;
		}
		if (type instanceof ParameterizedType) {
			return binaryParameterizedTypes.contains(type);
		}
		for (Class<?> superType : binaryClasses) {
			if (superType.isAssignableFrom((Class<?>) type)) {
				return true;
			}
		}
		return false;
	}

	public void addBinary(Class<?> type) {
		if (type == null) {
			throw new NullPointerException("Type cannot be null");
		}
		binaryClasses.add(type);
	}

	public void addBinary(Hint<?> hint) {
		if (hint == null) {
			throw new NullPointerException("Hint cannot be null");
		}
		Type type = hint.getType();
		if (type instanceof ParameterizedType) {
			binaryParameterizedTypes.add((ParameterizedType) type);
		} else {
			binaryClasses.add((Class<?>) type);
		}
	}

	public String cleanForAssembling(String contentType, Object body) {
		if (contentType == null) {
			if (body instanceof byte[] || body instanceof InputStream) {
				contentType = OCTET_TYPE;
			} else {
				if (fallbackByteType == null) {
					throw new IllegalArgumentException("Content type is null, body is not instance of byte[] or InputStream, and no fallback byte type was specified");
				}
				contentType = fallbackByteType;
			}
		}
		return contentType;
	}

	public String cleanForDisassembling(String contentType, Type type) {
		if (contentType == null) {
			if (type.equals(byte[].class) || type.equals(InputStream.class)) {
				contentType = OCTET_TYPE;
			} else {
				if (fallbackByteType == null) {
					throw new IllegalArgumentException("Content type is null, type is not equal to byte[] or InputStream, and no fallback byte type was specified");
				}
				contentType = fallbackByteType;
			}
		}
		return contentType;
	}

	public String cleanForSerializing(String contentType, Object body) {
		if (contentType == null) {
			if (body instanceof String || body instanceof Reader) {
				contentType = PLAIN_TYPE;
			} else {
				if (fallbackTextType == null) {
					throw new IllegalArgumentException("Content type is null, body is not instance of String or Reader, and no fallback text type was specified");
				}
				contentType = fallbackTextType;
			}
		}
		return contentType;
	}

	public String cleanForDeserializing(String contentType, Type type) {
		if (contentType == null) {
			if (type.equals(String.class) || type.equals(Reader.class)) {
				contentType = PLAIN_TYPE;
			} else {
				if (fallbackTextType == null) {
					throw new IllegalArgumentException("Content type is null, type is not equal to String or Reader, and no fallback text type was specified");
				}
				contentType = fallbackTextType;
			}
		}
		return contentType;
	}

	public Assembler getAssembler(String contentType) {
		Assembler assembler = assemblers.get(contentType);
		if (assembler == null) {
			throw new SupportException("No assembler associated to %s".formatted(contentType));
		}
		return assembler;
	}

	public void putAssembler(String contentType, Assembler assembler) {
		if (contentType == null) {
			throw new NullPointerException("Assembler type cannot be null");
		}
		contentType = Media.strip(contentType);
		if (contentType == null) {
			throw new IllegalArgumentException("Assembler type cannot be blank");
		}
		if (assembler == null) {
			throw new NullPointerException("Assembler cannot be null");
		}
		assemblers.put(contentType, assembler);
	}

	public Disassembler getDisassembler(String contentType) {
		Disassembler disassembler = disassemblers.get(contentType);
		if (disassembler == null) {
			throw new SupportException("No disassembler associated to %s".formatted(contentType));
		}
		return disassembler;
	}

	public void putDisassembler(String contentType, Disassembler disassembler) {
		if (contentType == null) {
			throw new NullPointerException("Disassembler type cannot be null");
		}
		contentType = Media.strip(contentType);
		if (contentType == null) {
			throw new IllegalArgumentException("Disassembler type cannot be blank");
		}
		if (disassembler == null) {
			throw new NullPointerException("Disassembler cannot be null");
		}
		disassemblers.put(contentType, disassembler);
	}

	public Serializer getSerializer(String contentType) {
		Serializer serializer = serializers.get(contentType);
		if (serializer == null) {
			throw new SupportException("No serializer associated to %s".formatted(contentType));
		}
		return serializer;
	}

	public void putSerializer(String contentType, Serializer serializer) {
		if (contentType == null) {
			throw new NullPointerException("Serializer type cannot be null");
		}
		contentType = Media.strip(contentType);
		if (contentType == null) {
			throw new IllegalArgumentException("Serializer type cannot be blank");
		}
		if (serializer == null) {
			throw new NullPointerException("Serializer cannot be null");
		}
		serializers.put(contentType, serializer);
	}

	public Deserializer getDeserializer(String contentType) {
		Deserializer deserializer = deserializers.get(contentType);
		if (deserializer == null) {
			throw new SupportException("No deserializer associated to %s".formatted(contentType));
		}
		return deserializer;
	}

	public void putDeserializer(String contentType, Deserializer deserializer) {
		if (contentType == null) {
			throw new NullPointerException("Deserializer type cannot be null");
		}
		contentType = Media.strip(contentType);
		if (contentType == null) {
			throw new IllegalArgumentException("Deserializer type cannot be blank");
		}
		if (deserializer == null) {
			throw new NullPointerException("Deserializer cannot be null");
		}
		deserializers.put(contentType, deserializer);
	}

	public void setFallbackByteType(String fallbackByteType) {
		if (fallbackByteType == null) {
			throw new NullPointerException("Fallback byte type cannot be null");
		}
		fallbackByteType = Media.strip(fallbackByteType);
		if (fallbackByteType == null) {
			throw new IllegalArgumentException("Fallback byte type cannot be blank");
		}
		this.fallbackByteType = fallbackByteType;
	}

	public void setFallbackTextType(String fallbackTextType) {
		if (fallbackTextType == null) {
			throw new NullPointerException("Fallback text type cannot be null");
		}
		fallbackTextType = Media.strip(fallbackTextType);
		if (fallbackTextType == null) {
			throw new IllegalArgumentException("Fallback text type cannot be blank");
		}
		this.fallbackTextType = fallbackTextType;
	}
}
