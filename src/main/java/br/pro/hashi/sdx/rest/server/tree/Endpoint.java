package br.pro.hashi.sdx.rest.server.tree;

import java.lang.reflect.InvocationTargetException;
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

import br.pro.hashi.sdx.rest.reflection.Cache;
import br.pro.hashi.sdx.rest.reflection.exception.ReflectionException;
import br.pro.hashi.sdx.rest.server.RestResource;
import br.pro.hashi.sdx.rest.server.annotation.Body;
import br.pro.hashi.sdx.rest.server.annotation.Part;
import br.pro.hashi.sdx.rest.server.exception.BadRequestException;
import br.pro.hashi.sdx.rest.server.exception.ResponseException;
import br.pro.hashi.sdx.rest.transform.exception.DeserializingException;
import br.pro.hashi.sdx.rest.transform.exception.DisassemblingException;
import br.pro.hashi.sdx.rest.transform.facade.exception.SupportException;

public class Endpoint {
	private static final Pattern METHOD_PATTERN = Pattern.compile("[A-Za-z]+");

	private final Logger logger;
	private final Class<? extends RestResource> resourceType;
	private final Method method;
	private final Type returnType;
	private final ItemParameter[] itemParameters;
	private final Map<String, DataParameter[]> partParameters;
	private final DataParameter bodyParameter;
	private final Object[] arguments;
	private final int reach;

	Endpoint(Cache cache, int distance, Class<? extends RestResource> resourceType, String typeName, Method method, String methodName) {
		Matcher matcher = METHOD_PATTERN.matcher(methodName);
		methodName = "%s.%s".formatted(typeName, methodName);
		if (!matcher.matches()) {
			throw new ReflectionException("Method name %s must have only US-ASCII letters".formatted(methodName));
		}

		Type[] types = method.getGenericParameterTypes();
		List<ItemParameter> itemList = new ArrayList<>();
		Map<String, List<DataParameter>> partMap = new HashMap<>();
		DataParameter bodyParameter = null;

		for (Class<?> exceptionType : method.getExceptionTypes()) {
			if (!RuntimeException.class.isAssignableFrom(exceptionType)) {
				throw new ReflectionException("Method %s can only throw unchecked exceptions".formatted(methodName));
			}
		}

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
					Function<String, ?> function = cache.get((Class<?>) type);
					itemList.add(new ItemParameter(index, function, parameter.getName()));
				} else {
					if (!partMap.isEmpty()) {
						throw new ReflectionException("Method %s cannot have both parts and body".formatted(methodName));
					}
					if (bodyParameter != null) {
						throw new ReflectionException("Method %s cannot have more than one body".formatted(methodName));
					}
					bodyParameter = new DataParameter(index, type);
				}
			} else {
				if (bodyAnnotation != null) {
					throw new ReflectionException("Parameter %d of method %s cannot be both part and body".formatted(index, methodName));
				}
				if (bodyParameter != null) {
					throw new ReflectionException("Method %s cannot have both body and parts".formatted(methodName));
				}
				String name = partAnnotation.value();
				List<DataParameter> partList = partMap.get(name);
				if (partList == null) {
					partList = new ArrayList<>();
					partMap.put(name, partList);
				}
				partList.add(new DataParameter(index, type));
			}
			index++;
		}
		if (itemList.size() < distance) {
			throw new ReflectionException("Method %s must have at least %d parameters that are neither part or body".formatted(methodName, distance));
		}

		this.logger = LoggerFactory.getLogger(Endpoint.class);
		this.resourceType = resourceType;
		this.method = method;
		this.returnType = method.getGenericReturnType();
		this.itemParameters = itemList.toArray(new ItemParameter[itemList.size()]);
		this.partParameters = new HashMap<>();
		for (String name : partMap.keySet()) {
			List<DataParameter> partList = partMap.get(name);
			this.partParameters.put(name, partList.toArray(new DataParameter[partList.size()]));
		}
		this.bodyParameter = bodyParameter;
		this.arguments = new Object[types.length];
		this.reach = itemParameters.length - distance;
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

	public Object call(RestResource resource, List<String> items, Map<String, List<Data>> partMap, Data body) {
		Object result;
		try {
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
				if (bodyParameter != null) {
					if (body == null) {
						throw new BadRequestException("Endpoint expects a body");
					}
					Type type = bodyParameter.type();
					String description = "Body";
					arguments[bodyParameter.index()] = getBody(body, type, description);
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
						String description = "Part %d with name '%s'".formatted(index, name);
						arguments[parameter.index()] = getBody(part, type, description);
						index++;
					}
				}
			}
			result = invoke(method, resource, arguments);
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
			String message = "Argument '%s' could not be understood".formatted(item);
			logger.error(message, error);
			throw new BadRequestException(message);
		}
		return argument;
	}

	private Object getBody(Data data, Type type, String description) {
		Object argument;
		try {
			argument = data.getBody(type);
		} catch (SupportException error) {
			String message = "%s does not have a supported content type".formatted(description);
			logger.error(message, error);
			throw new ResponseException(HttpStatus.UNSUPPORTED_MEDIA_TYPE_415, message);
		} catch (DisassemblingException | DeserializingException error) {
			String message = "%s could not be understood".formatted(description);
			logger.error(message, error);
			throw new BadRequestException(message);
		}
		return argument;
	}

	Object invoke(Method method, RestResource resource, Object... arguments) {
		Object result;
		try {
			result = method.invoke(resource, arguments);
		} catch (InvocationTargetException exception) {
			Throwable cause = exception.getCause();
			if (cause instanceof RuntimeException) {
				throw (RuntimeException) cause;
			}
			throw new AssertionError(cause);
		} catch (IllegalAccessException exception) {
			throw new AssertionError(exception);
		}
		return result;
	}

	record ItemParameter(int index, Function<String, ?> function, String name) {
	}

	record DataParameter(int index, Type type) {
	}
}
