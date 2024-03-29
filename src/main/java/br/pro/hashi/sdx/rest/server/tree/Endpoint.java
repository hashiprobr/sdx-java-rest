package br.pro.hashi.sdx.rest.server.tree;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.lang.invoke.MethodHandle;
import java.lang.reflect.Array;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.jetty.http.HttpStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import br.pro.hashi.sdx.rest.reflection.ParserFactory;
import br.pro.hashi.sdx.rest.reflection.Reflector;
import br.pro.hashi.sdx.rest.reflection.exception.ReflectionException;
import br.pro.hashi.sdx.rest.server.RestException;
import br.pro.hashi.sdx.rest.server.RestResource;
import br.pro.hashi.sdx.rest.server.annotation.Body;
import br.pro.hashi.sdx.rest.server.annotation.Part;
import br.pro.hashi.sdx.rest.server.exception.BadRequestException;
import br.pro.hashi.sdx.rest.transform.exception.DeserializingException;
import br.pro.hashi.sdx.rest.transform.exception.DisassemblingException;
import br.pro.hashi.sdx.rest.transform.exception.TypeException;

public class Endpoint {
	private static final Pattern METHOD_PATTERN = Pattern.compile("[A-Za-z]+");

	private final Reflector reflector;
	private final Logger logger;
	private final Class<? extends RestResource> resourceType;
	private final MethodHandle handle;
	private final Type returnType;
	private final Class<?> varType;
	private final ItemParameter[] itemParameters;
	private final Map<String, DataParameter[]> partParameters;
	private final DataParameter bodyParameter;
	private final Object[] arguments;
	private final int reach;

	Endpoint(ParserFactory factory, long maxBodySize, int distance, Class<? extends RestResource> resourceType, String typeName, Method method, String methodName) {
		Matcher matcher = METHOD_PATTERN.matcher(methodName);
		methodName = "%s.%s".formatted(typeName, methodName);
		if (!matcher.matches()) {
			throw new ReflectionException("Method name %s can only have US-ASCII letters".formatted(methodName));
		}

		Type[] types = method.getGenericParameterTypes();
		Class<?> varType = null;
		List<ItemParameter> itemList = new ArrayList<>();
		Map<String, List<DataParameter>> partMap = new HashMap<>();
		DataParameter bodyParameter = null;
		int start = method.isVarArgs() ? types.length - 1 : -1;

		int index = 0;
		for (Parameter parameter : method.getParameters()) {
			Type type = types[index];
			Part partAnnotation = parameter.getDeclaredAnnotation(Part.class);
			Body bodyAnnotation = parameter.getDeclaredAnnotation(Body.class);
			if (partAnnotation == null) {
				if (bodyAnnotation == null) {
					if (type instanceof ParameterizedType) {
						throw new ReflectionException("Parameter %d of method %s cannot be generic if neither part or body".formatted(index, methodName));
					}
					Class<?> rawType = (Class<?>) type;
					if (index == start) {
						rawType = rawType.getComponentType();
						varType = rawType;
					}
					Function<String, ?> function = factory.get(rawType);
					itemList.add(new ItemParameter(index + 1, function, parameter.getName()));
				} else {
					if (!partMap.isEmpty()) {
						throw new ReflectionException("Method %s cannot have both parts and body".formatted(methodName));
					}
					if (bodyParameter != null) {
						throw new ReflectionException("Method %s cannot have more than one body".formatted(methodName));
					}
					if (index == start) {
						throw new ReflectionException("Method %s cannot have a varargs body".formatted(methodName));
					}
					long maxSize = bodyAnnotation.value();
					if (maxSize < 1) {
						maxSize = maxBodySize;
					}
					bodyParameter = new DataParameter(index + 1, type, maxSize);
				}
			} else {
				if (bodyAnnotation != null) {
					throw new ReflectionException("Parameter %d of method %s cannot be both part and body".formatted(index, methodName));
				}
				if (bodyParameter != null) {
					throw new ReflectionException("Method %s cannot have both body and parts".formatted(methodName));
				}
				if (index == start) {
					throw new ReflectionException("Method %s cannot have a varargs part".formatted(methodName));
				}
				String name = partAnnotation.value();
				List<DataParameter> partList = partMap.get(name);
				if (partList == null) {
					partList = new ArrayList<>();
					partMap.put(name, partList);
				}
				partList.add(new DataParameter(index + 1, type, 0));
			}
			index++;
		}

		int varSize = varType == null ? 0 : 1;
		if (itemList.size() - varSize < distance) {
			throw new ReflectionException("Method %s must have at least %d parameters that are neither part or body".formatted(methodName, distance));
		}

		this.reflector = Reflector.getInstance();
		this.logger = LoggerFactory.getLogger(Endpoint.class);
		this.resourceType = resourceType;
		this.handle = reflector.unreflect(method).asFixedArity();
		this.returnType = method.getGenericReturnType();
		this.varType = varType;
		this.itemParameters = itemList.toArray(new ItemParameter[itemList.size()]);
		this.partParameters = new HashMap<>();
		for (String name : partMap.keySet()) {
			List<DataParameter> partList = partMap.get(name);
			this.partParameters.put(name, partList.toArray(new DataParameter[partList.size()]));
		}
		this.bodyParameter = bodyParameter;
		this.arguments = new Object[types.length + 1];
		this.reach = itemParameters.length - varSize - distance;
	}

	Class<?> getVarType() {
		return varType;
	}

	ItemParameter[] getItemParameters() {
		return itemParameters;
	}

	Map<String, DataParameter[]> getPartParameters() {
		return partParameters;
	}

	DataParameter getBodyParameter() {
		return bodyParameter;
	}

	Object[] getArguments() {
		return arguments;
	}

	int getReach() {
		return reach;
	}

	public Class<? extends RestResource> getResourceType() {
		return resourceType;
	}

	public Type getReturnType() {
		return returnType;
	}

	public Object call(RestResource resource, List<String> items, Map<String, List<Data>> partMap, Data body) throws Exception {
		Object result;
		try {
			if (varType != null) {
				int start = itemParameters.length - 1;
				int end = items.size();
				ItemParameter parameter = itemParameters[start];
				Function<String, ?> function = parameter.function();
				Object varArgs = Array.newInstance(varType, end - start);
				int index = 0;
				for (String item : items.subList(start, end)) {
					Array.set(varArgs, index, apply(function, item));
					index++;
				}
				arguments[parameter.index()] = varArgs;
				items = items.subList(0, start);
			}
			int index = 0;
			for (String item : items) {
				ItemParameter parameter = itemParameters[index];
				Function<String, ?> function = parameter.function();
				arguments[parameter.index()] = apply(function, item);
				index++;
			}
			Set<String> names = partParameters.keySet();
			if (names.isEmpty()) {
				if (!partMap.isEmpty()) {
					throw new BadRequestException("Endpoint does not expect a multipart body");
				}
				if (bodyParameter == null) {
					if (body != null) {
						InputStream stream = body.getStream();
						int b;
						try {
							b = stream.read();
						} catch (IOException exception) {
							throw new UncheckedIOException(exception);
						}
						if (b != -1) {
							throw new BadRequestException("Endpoint does not expect a body");
						}
						try {
							stream.close();
						} catch (IOException exception) {
							throw new UncheckedIOException(exception);
						}
					}
				} else {
					if (body == null) {
						throw new BadRequestException("Endpoint expects a body");
					}
					Type type = bodyParameter.type();
					long maxSize = bodyParameter.maxSize();
					String description = "Body";
					arguments[bodyParameter.index()] = getBody(body, type, maxSize, description);
				}
			} else {
				if (partMap.isEmpty()) {
					throw new BadRequestException("Endpoint expects a multipart body");
				}
				for (String name : names) {
					List<Data> parts = partMap.get(name);
					if (parts == null) {
						throw new BadRequestException("Endpoint expects a body part with name '%s'".formatted(name));
					}
					DataParameter[] partArray = partParameters.get(name);
					int length = partArray.length;
					if (parts.size() < length) {
						throw new BadRequestException("Endpoint expects %d body parts with name '%s'".formatted(length, name));
					}
					if (parts.size() > length) {
						throw new BadRequestException("Endpoint expects only %d body parts with name '%s'".formatted(length, name));
					}
					index = 0;
					for (Data part : parts) {
						DataParameter parameter = partArray[index];
						Type type = parameter.type();
						long maxSize = parameter.maxSize();
						String description = "Part %d with name '%s'".formatted(index, name);
						arguments[parameter.index()] = getBody(part, type, maxSize, description);
						index++;
					}
				}
			}
			arguments[0] = resource;
			result = invoke(handle, arguments);
		} finally {
			Arrays.fill(arguments, null);
		}
		return result;
	}

	private Object apply(Function<String, ?> function, String item) {
		Object argument;
		try {
			argument = function.apply(item);
		} catch (RuntimeException error) {
			String message = "Argument '%s' is not valid".formatted(item);
			logger.error(message, error);
			throw new BadRequestException(message);
		}
		return argument;
	}

	private Object getBody(Data data, Type type, long maxSize, String description) {
		Object argument;
		try {
			argument = data.getBody(type, maxSize);
		} catch (TypeException error) {
			String message = "%s does not have a supported content type".formatted(description);
			logger.error(message, error);
			throw new RestException(HttpStatus.UNSUPPORTED_MEDIA_TYPE_415, message);
		} catch (DisassemblingException | DeserializingException error) {
			String message = "%s is not valid".formatted(description);
			logger.error(message, error);
			throw new BadRequestException(message);
		}
		return argument;
	}

	Object invoke(MethodHandle handle, Object... arguments) throws Exception {
		Object result;
		try {
			result = handle.invokeWithArguments(arguments);
		} catch (Throwable throwable) {
			if (throwable instanceof Exception) {
				throw (Exception) throwable;
			}
			throw new AssertionError(throwable);
		}
		return result;
	}

	record ItemParameter(int index, Function<String, ?> function, String name) {
	}

	record DataParameter(int index, Type type, long maxSize) {
	}
}
