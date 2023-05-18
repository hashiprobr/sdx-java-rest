package br.pro.hashi.sdx.rest.transform.facade;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.Writer;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

import br.pro.hashi.sdx.rest.coding.Media;
import br.pro.hashi.sdx.rest.reflection.ParserFactory;
import br.pro.hashi.sdx.rest.transform.Assembler;
import br.pro.hashi.sdx.rest.transform.Deserializer;
import br.pro.hashi.sdx.rest.transform.Disassembler;
import br.pro.hashi.sdx.rest.transform.Hint;
import br.pro.hashi.sdx.rest.transform.Serializer;
import br.pro.hashi.sdx.rest.transform.exception.TypeException;

public class Facade {
	private static final String OCTET_TYPE = "application/octet-stream";
	private static final String PLAIN_TYPE = "text/plain";

	static final Set<Class<?>> PRIMITIVE_TYPES = Set.of(
			boolean.class,
			byte.class,
			short.class,
			int.class,
			long.class,
			float.class,
			double.class,
			Boolean.class,
			Byte.class,
			Short.class,
			Integer.class,
			Long.class,
			Float.class,
			Double.class);

	private final Type streamConsumerType;
	private final Type writerConsumerType;
	private final Set<Class<?>> binaryClasses;
	private final Set<ParameterizedType> binaryParameterizedTypes;
	private final Map<String, String> extensions;
	private final Map<String, Assembler> assemblers;
	private final Map<String, Disassembler> disassemblers;
	private final Map<String, Serializer> serializers;
	private final Map<String, Deserializer> deserializers;
	private String fallbackByteType;
	private String fallbackTextType;

	public Facade(ParserFactory cache) {
		this.streamConsumerType = new Hint<Consumer<OutputStream>>() {}.getType();
		this.writerConsumerType = new Hint<Consumer<Writer>>() {}.getType();

		this.binaryClasses = new HashSet<>();
		this.binaryClasses.addAll(Set.of(byte[].class, InputStream.class));

		this.binaryParameterizedTypes = new HashSet<>();

		this.extensions = new HashMap<>();
		this.extensions.put("txt", PLAIN_TYPE);

		this.assemblers = new HashMap<>();
		this.assemblers.put(OCTET_TYPE, new DefaultAssembler());

		this.disassemblers = new HashMap<>();
		this.disassemblers.put(OCTET_TYPE, new DefaultDisassembler());

		this.serializers = new HashMap<>();
		this.serializers.put(PLAIN_TYPE, new DefaultSerializer());

		this.deserializers = new HashMap<>();
		this.deserializers.put(PLAIN_TYPE, new DefaultDeserializer(cache));

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

	public String getExtensionType(String extension) {
		return extensions.get(extension);
	}

	public void putExtension(String extension, String contentType) {
		if (extension == null) {
			throw new NullPointerException("Extension cannot be null");
		}
		extension = extension.strip();
		if (extension.isEmpty()) {
			throw new IllegalArgumentException("Extension cannot be blank");
		}
		if (contentType == null) {
			throw new NullPointerException("Extension type cannot be null");
		}
		contentType = Media.strip(contentType);
		if (contentType == null) {
			throw new IllegalArgumentException("Extension type cannot be blank");
		}
		if (!(assemblers.containsKey(contentType) || serializers.containsKey(contentType))) {
			throw new IllegalArgumentException("Extension is not associated to an assembler or a serializer");
		}
		extensions.put(extension, contentType);
	}

	public String getAssemblerType(String contentType, Object body, Type type) {
		if (contentType == null) {
			if (body instanceof byte[] || body instanceof InputStream || type.equals(streamConsumerType)) {
				contentType = OCTET_TYPE;
			} else {
				if (fallbackByteType == null) {
					throw new IllegalArgumentException("Content type is null, body is not instance of byte[], InputStream or Consumer<OutputStream>, and no fallback byte type was specified");
				}
				contentType = fallbackByteType;
			}
		}
		return contentType;
	}

	public String getDisassemblerType(String contentType, Type type) {
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

	public String getSerializerType(String contentType, Object body, Type type) {
		if (contentType == null) {
			if ((body != null && Facade.PRIMITIVE_TYPES.contains(type)) || body instanceof String || body instanceof Reader || type.equals(writerConsumerType)) {
				contentType = PLAIN_TYPE;
			} else {
				if (fallbackTextType == null) {
					throw new IllegalArgumentException("Content type is null, body is not a primitive or an instance of String, Reader or Consumer<Writer>, and no fallback text type was specified");
				}
				contentType = fallbackTextType;
			}
		}
		return contentType;
	}

	public String getDeserializerType(String contentType, Type type) {
		if (contentType == null) {
			if (Facade.PRIMITIVE_TYPES.contains(type) || type.equals(String.class) || type.equals(Reader.class)) {
				contentType = PLAIN_TYPE;
			} else {
				if (fallbackTextType == null) {
					throw new IllegalArgumentException("Content type is null, type is not primitive or equal to String or Reader, and no fallback text type was specified");
				}
				contentType = fallbackTextType;
			}
		}
		return contentType;
	}

	public Assembler getAssembler(String contentType) {
		Assembler assembler = assemblers.get(contentType);
		if (assembler == null) {
			throw new TypeException("No assembler associated to %s".formatted(contentType));
		}
		return assembler;
	}

	public void putDefaultAssembler(String contentType) {
		contentType = cleanAssemblerType(contentType);
		assemblers.put(contentType, assemblers.get(OCTET_TYPE));
	}

	public void putAssembler(String contentType, Assembler assembler) {
		contentType = cleanAssemblerType(contentType);
		if (assembler == null) {
			throw new NullPointerException("Assembler cannot be null");
		}
		assemblers.put(contentType, assembler);
	}

	private String cleanAssemblerType(String contentType) {
		if (contentType == null) {
			throw new NullPointerException("Assembler type cannot be null");
		}
		contentType = Media.strip(contentType);
		if (contentType == null) {
			throw new IllegalArgumentException("Assembler type cannot be blank");
		}
		return contentType;
	}

	public Disassembler getDisassembler(String contentType) {
		Disassembler disassembler = disassemblers.get(contentType);
		if (disassembler == null) {
			throw new TypeException("No disassembler associated to %s".formatted(contentType));
		}
		return disassembler;
	}

	public void putDefaultDisassembler(String contentType) {
		contentType = cleanDisassemblerType(contentType);
		disassemblers.put(contentType, disassemblers.get(OCTET_TYPE));
	}

	public void putDisassembler(String contentType, Disassembler disassembler) {
		contentType = cleanDisassemblerType(contentType);
		if (disassembler == null) {
			throw new NullPointerException("Disassembler cannot be null");
		}
		disassemblers.put(contentType, disassembler);
	}

	private String cleanDisassemblerType(String contentType) {
		if (contentType == null) {
			throw new NullPointerException("Disassembler type cannot be null");
		}
		contentType = Media.strip(contentType);
		if (contentType == null) {
			throw new IllegalArgumentException("Disassembler type cannot be blank");
		}
		return contentType;
	}

	public Serializer getSerializer(String contentType) {
		Serializer serializer = serializers.get(contentType);
		if (serializer == null) {
			throw new TypeException("No serializer associated to %s".formatted(contentType));
		}
		return serializer;
	}

	public void putDefaultSerializer(String contentType) {
		contentType = cleanSerializerType(contentType);
		serializers.put(contentType, serializers.get(PLAIN_TYPE));
	}

	public void putSerializer(String contentType, Serializer serializer) {
		contentType = cleanSerializerType(contentType);
		if (serializer == null) {
			throw new NullPointerException("Serializer cannot be null");
		}
		serializers.put(contentType, serializer);
	}

	private String cleanSerializerType(String contentType) {
		if (contentType == null) {
			throw new NullPointerException("Serializer type cannot be null");
		}
		contentType = Media.strip(contentType);
		if (contentType == null) {
			throw new IllegalArgumentException("Serializer type cannot be blank");
		}
		return contentType;
	}

	public Deserializer getDeserializer(String contentType) {
		Deserializer deserializer = deserializers.get(contentType);
		if (deserializer == null) {
			throw new TypeException("No deserializer associated to %s".formatted(contentType));
		}
		return deserializer;
	}

	public void putDefaultDeserializer(String contentType) {
		contentType = cleanDeserializerType(contentType);
		deserializers.put(contentType, deserializers.get(PLAIN_TYPE));
	}

	public void putDeserializer(String contentType, Deserializer deserializer) {
		contentType = cleanDeserializerType(contentType);
		if (deserializer == null) {
			throw new NullPointerException("Deserializer cannot be null");
		}
		deserializers.put(contentType, deserializer);
	}

	private String cleanDeserializerType(String contentType) {
		if (contentType == null) {
			throw new NullPointerException("Deserializer type cannot be null");
		}
		contentType = Media.strip(contentType);
		if (contentType == null) {
			throw new IllegalArgumentException("Deserializer type cannot be blank");
		}
		return contentType;
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
