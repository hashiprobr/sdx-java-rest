package br.pro.hashi.sdx.rest.transform.manager;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

import br.pro.hashi.sdx.rest.Hint;
import br.pro.hashi.sdx.rest.coding.MediaCoder;
import br.pro.hashi.sdx.rest.constant.Types;
import br.pro.hashi.sdx.rest.transform.Assembler;
import br.pro.hashi.sdx.rest.transform.Deserializer;
import br.pro.hashi.sdx.rest.transform.Disassembler;
import br.pro.hashi.sdx.rest.transform.Serializer;
import br.pro.hashi.sdx.rest.transform.exception.TypeException;

public class TransformManager {
	private static final String OCTET_TYPE = "application/octet-stream";
	private static final String PLAIN_TYPE = "text/plain";

	public static TransformManager newInstance() {
		MediaCoder coder = MediaCoder.getInstance();
		return new TransformManager(coder);
	}

	private final MediaCoder coder;
	private final Map<String, Assembler> assemblers;
	private final Map<String, Disassembler> disassemblers;
	private final Map<String, Serializer> serializers;
	private final Map<String, Deserializer> deserializers;
	private final Map<String, String> extensions;
	private final Set<Class<?>> binaryRawTypes;
	private final Set<Type> binaryGenericTypes;
	private String binaryFallbackType;
	private String fallbackType;

	public TransformManager(MediaCoder coder) {
		this.coder = coder;

		this.assemblers = new HashMap<>();
		this.assemblers.put(OCTET_TYPE, DefaultAssembler.getInstance());

		this.disassemblers = new HashMap<>();
		this.disassemblers.put(OCTET_TYPE, DefaultDisassembler.getInstance());

		this.serializers = new HashMap<>();
		this.serializers.put(PLAIN_TYPE, DefaultSerializer.getInstance());

		this.deserializers = new HashMap<>();
		this.deserializers.put(PLAIN_TYPE, DefaultDeserializer.getInstance());

		this.extensions = new HashMap<>();
		this.extensions.put("txt", PLAIN_TYPE);

		this.binaryRawTypes = new HashSet<>();
		this.binaryRawTypes.add(byte[].class);
		this.binaryRawTypes.add(InputStream.class);

		this.binaryGenericTypes = new HashSet<>();
		this.binaryGenericTypes.add(new Hint<Consumer<OutputStream>>() {}.getType());

		this.binaryFallbackType = null;
		this.fallbackType = null;
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
		assemblers.put(contentType, DefaultAssembler.getInstance());
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
		contentType = coder.strip(contentType);
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
		disassemblers.put(contentType, DefaultDisassembler.getInstance());
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
		contentType = coder.strip(contentType);
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
		serializers.put(contentType, DefaultSerializer.getInstance());
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
		contentType = coder.strip(contentType);
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
		deserializers.put(contentType, DefaultDeserializer.getInstance());
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
		contentType = coder.strip(contentType);
		if (contentType == null) {
			throw new IllegalArgumentException("Deserializer type cannot be blank");
		}
		return contentType;
	}

	public String getExtensionType(String extension) {
		return extensions.get(extension);
	}

	public void putExtensionType(String extension, String contentType) {
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
		contentType = contentType.strip();
		if (contentType.isEmpty()) {
			throw new IllegalArgumentException("Extension type cannot be blank");
		}
		if (!(assemblers.containsKey(contentType) || serializers.containsKey(contentType))) {
			throw new IllegalArgumentException("No assembler or serializer associated to %s".formatted(contentType));
		}
		extensions.put(extension, contentType);
	}

	public boolean isBinary(Type type) {
		if (type instanceof ParameterizedType) {
			return binaryGenericTypes.contains(type);
		}
		for (Class<?> rawType : binaryRawTypes) {
			if (rawType.isAssignableFrom((Class<?>) type)) {
				return true;
			}
		}
		return false;
	}

	public void addBinary(Type type) {
		if (type instanceof ParameterizedType) {
			binaryGenericTypes.add(type);
		} else {
			binaryRawTypes.add((Class<?>) type);
		}
	}

	public void setBinaryFallbackType(String binaryFallbackType) {
		if (binaryFallbackType == null) {
			throw new NullPointerException("Binary fallback type cannot be null");
		}
		binaryFallbackType = coder.strip(binaryFallbackType);
		if (binaryFallbackType == null) {
			throw new IllegalArgumentException("Binary fallback type cannot be blank");
		}
		this.binaryFallbackType = binaryFallbackType;
	}

	public void setFallbackType(String fallbackType) {
		if (fallbackType == null) {
			throw new NullPointerException("Fallback type cannot be null");
		}
		fallbackType = coder.strip(fallbackType);
		if (fallbackType == null) {
			throw new IllegalArgumentException("Fallback type cannot be blank");
		}
		this.fallbackType = fallbackType;
	}

	public String getAssemblerType(String contentType, Object body, Type type) {
		if (contentType == null) {
			if (body instanceof byte[] || body instanceof InputStream || Types.instanceOfStreamConsumer(body, type)) {
				return OCTET_TYPE;
			}
			if (binaryFallbackType == null) {
				throw new IllegalStateException("Content type is null, body is not an instance of byte[], InputStream, or Consumer<OutputStream>, and no binary fallback type was specified");
			}
			return binaryFallbackType;
		}
		return contentType;
	}

	public String getDisassemblerType(String contentType, Type type) {
		if (contentType == null) {
			if (type.equals(byte[].class) || type.equals(InputStream.class)) {
				return OCTET_TYPE;
			}
			if (binaryFallbackType == null) {
				throw new IllegalStateException("Content type is null, type is not equal to byte[] or InputStream, and no binary fallback type was specified");
			}
			return binaryFallbackType;
		}
		return contentType;
	}

	public String getSerializerType(String contentType, Object body, Type type) {
		if (contentType == null) {
			if (Types.instanceOfSimple(body, type) || body instanceof Reader || Types.instanceOfWriterConsumer(body, type)) {
				return PLAIN_TYPE;
			}
			if (fallbackType == null) {
				throw new IllegalStateException("Content type is null, body is not a primitive, a big number, or an instance of String, Reader, or Consumer<Writer>, and no fallback type was specified");
			}
			return fallbackType;
		}
		return contentType;
	}

	public String getDeserializerType(String contentType, Type type) {
		if (contentType == null) {
			if (Types.equalsSimple(type) || type.equals(Reader.class)) {
				return PLAIN_TYPE;
			}
			if (fallbackType == null) {
				throw new IllegalStateException("Content type is null, type is not primitive or equal to BigInteger, BigDecimal, String, or Reader, and no fallback type was specified");
			}
			return fallbackType;
		}
		return contentType;
	}
}
