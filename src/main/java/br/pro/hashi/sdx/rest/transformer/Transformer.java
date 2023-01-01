package br.pro.hashi.sdx.rest.transformer;

import java.io.InputStream;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.google.gson.Gson;

import br.pro.hashi.sdx.rest.coding.Media;
import br.pro.hashi.sdx.rest.transformer.base.Assembler;
import br.pro.hashi.sdx.rest.transformer.base.Deserializer;
import br.pro.hashi.sdx.rest.transformer.base.Disassembler;
import br.pro.hashi.sdx.rest.transformer.base.Serializer;

public class Transformer {
	private static final String BYTE_TYPE = "application/octet-stream";
	private static final String TEXT_TYPE = "text/plain";
	private static final String JSON_TYPE = "application/json";

	private final Set<Class<?>> binaryTypes;
	private final Map<String, Assembler> assemblers;
	private final Map<String, Disassembler> disassemblers;
	private final Map<String, Serializer> serializers;
	private final Map<String, Deserializer> deserializers;

	public Transformer(Gson gson) {
		this.binaryTypes = new HashSet<>(Set.of(InputStream.class));

		this.assemblers = new HashMap<>();
		this.assemblers.put(BYTE_TYPE, new ByteAssembler());

		this.disassemblers = new HashMap<>();
		this.disassemblers.put(BYTE_TYPE, new ByteDisassembler());

		this.serializers = new HashMap<>();
		this.serializers.put(TEXT_TYPE, new TextSerializer());
		this.serializers.put(JSON_TYPE, new GsonSerializer(gson));

		this.deserializers = new HashMap<>();
		this.deserializers.put(TEXT_TYPE, new TextDeserializer());
		this.deserializers.put(JSON_TYPE, new SafeGsonDeserializer(gson));
	}

	public boolean isBinary(Class<?> type) {
		return binaryTypes.contains(type);
	}

	public void addBinary(Class<?> type) {
		if (type == null) {
			throw new IllegalArgumentException("Binary type cannot be null");
		}
		binaryTypes.add(type);
	}

	public String cleanBinary(String contentType) {
		if (contentType == null) {
			contentType = BYTE_TYPE;
		}
		return contentType;
	}

	public String cleanSerializer(String contentType, Object body) {
		if (contentType == null) {
			if (body instanceof String) {
				contentType = TEXT_TYPE;
			} else {
				contentType = JSON_TYPE;
			}
		}
		return contentType;
	}

	public String cleanDeserializer(String contentType, Class<?> type) {
		if (contentType == null) {
			if (type.equals(String.class)) {
				contentType = TEXT_TYPE;
			} else {
				contentType = JSON_TYPE;
			}
		}
		return contentType;
	}

	public Assembler getAssembler(String contentType) {
		Assembler assembler = assemblers.get(contentType);
		if (assembler == null) {
			throw new IllegalArgumentException("No assembler associated to %s".formatted(contentType));
		}
		return assembler;
	}

	public void putAssembler(String contentType, Assembler assembler) {
		if (contentType != null) {
			contentType = Media.strip(contentType);
		}
		if (contentType == null) {
			throw new IllegalArgumentException("Assembler type cannot be null or blank");
		}
		if (assembler == null) {
			throw new IllegalArgumentException("Assembler cannot be null");
		}
		assemblers.put(contentType, assembler);
	}

	public void removeAssembler(String contentType) {
		if (contentType != null) {
			contentType = Media.strip(contentType);
		}
		if (contentType == null) {
			throw new IllegalArgumentException("Assembler type cannot be null or blank");
		}
		assemblers.remove(contentType);
	}

	public Disassembler getDisassembler(String contentType) {
		Disassembler disassembler = disassemblers.get(contentType);
		if (disassembler == null) {
			throw new IllegalArgumentException("No disassembler associated to %s".formatted(contentType));
		}
		return disassembler;
	}

	public void putDisassembler(String contentType, Disassembler disassembler) {
		if (contentType != null) {
			contentType = Media.strip(contentType);
		}
		if (contentType == null) {
			throw new IllegalArgumentException("Disassembler type cannot be null or blank");
		}
		if (disassembler == null) {
			throw new IllegalArgumentException("Disassembler cannot be null");
		}
		disassemblers.put(contentType, disassembler);
	}

	public void removeDisassembler(String contentType) {
		if (contentType != null) {
			contentType = Media.strip(contentType);
		}
		if (contentType == null) {
			throw new IllegalArgumentException("Disassembler type cannot be null or blank");
		}
		disassemblers.remove(contentType);
	}

	public Serializer getSerializer(String contentType) {
		Serializer serializer = serializers.get(contentType);
		if (serializer == null) {
			throw new IllegalArgumentException("No serializer associated to %s".formatted(contentType));
		}
		return serializer;
	}

	public void putSerializer(String contentType, Serializer serializer) {
		if (contentType != null) {
			contentType = Media.strip(contentType);
		}
		if (contentType == null) {
			throw new IllegalArgumentException("Serializer type cannot be null or blank");
		}
		if (serializer == null) {
			throw new IllegalArgumentException("Serializer cannot be null");
		}
		serializers.put(contentType, serializer);
	}

	public void putUncheckedSerializer(Gson gson) {
		serializers.put(JSON_TYPE, new GsonSerializer(gson));
	}

	public void putSerializer(Gson gson) {
		if (gson == null) {
			throw new IllegalArgumentException("Serializer gson cannot be null");
		}
		putUncheckedSerializer(gson);
	}

	public void removeSerializer(String contentType) {
		if (contentType != null) {
			contentType = Media.strip(contentType);
		}
		if (contentType == null) {
			throw new IllegalArgumentException("Serializer type cannot be null or blank");
		}
		serializers.remove(contentType);
	}

	public Deserializer getDeserializer(String contentType) {
		Deserializer deserializer = deserializers.get(contentType);
		if (deserializer == null) {
			throw new IllegalArgumentException("No deserializer associated to %s".formatted(contentType));
		}
		return deserializer;
	}

	public void putDeserializer(String contentType, Deserializer deserializer) {
		if (contentType != null) {
			contentType = Media.strip(contentType);
		}
		if (contentType == null) {
			throw new IllegalArgumentException("Deserializer type cannot be null or blank");
		}
		if (deserializer == null) {
			throw new IllegalArgumentException("Deserializer cannot be null");
		}
		deserializers.put(contentType, deserializer);
	}

	public void putDeserializer(Gson gson) {
		if (gson == null) {
			throw new IllegalArgumentException("Deserializer gson cannot be null");
		}
		deserializers.put(JSON_TYPE, new GsonDeserializer(gson));
	}

	public void putSafeDeserializer(Gson gson) {
		deserializers.put(JSON_TYPE, new SafeGsonDeserializer(gson));
	}

	public void removeDeserializer(String contentType) {
		if (contentType != null) {
			contentType = Media.strip(contentType);
		}
		if (contentType == null) {
			throw new IllegalArgumentException("Deserializer type cannot be null or blank");
		}
		deserializers.remove(contentType);
	}
}
