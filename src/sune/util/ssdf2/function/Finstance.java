package sune.util.ssdf2.function;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import sune.util.ssdf2.SSDAnnotation;
import sune.util.ssdf2.SSDCollection;
import sune.util.ssdf2.SSDFunctionImpl;
import sune.util.ssdf2.SSDFunctionUtils;

public class Finstance implements SSDFunctionImpl {
	
	static final SSDCollection _toCollection(Class<?> clazz, Object instance) {
		SSDCollection coll = SSDCollection.empty();
		for(Field field : clazz.getDeclaredFields()) {
			try {
				field.setAccessible(true); // make the field accessible
				if((Modifier.isStatic(field.getModifiers())))
					continue;
				String name  = field.getName();
				Object value = field.get(instance);
				if((value.getClass().isArray())) {
					value = SSDFunctionImpl.toArray(SSDFunctionImpl.toObjectArray(value));
					coll.set(name, (SSDCollection) value);
				} else if(value instanceof List) {
					value = SSDFunctionImpl.toArray((List<?>) value);
					coll.set(name, (SSDCollection) value);
				} else if(value instanceof Map) {
					value = SSDFunctionImpl.toArray((Map<?, ?>) value);
					coll.set(name, (SSDCollection) value);
				} else {
					coll.set(name, SSDFunctionImpl.object(name, value));
				}
			} catch(Exception ex) {
			}
		}
		return coll;
	}
	
	final Object _value(String path, Object... args) {
		if(path == null || path.isEmpty())
			return null;
		try {
			Class<?>   clazz = Class.forName(path);
			Class<?>[] cargs = SSDFunctionUtils.recognizeClasses(args);
			Constructor<?> c = clazz.getDeclaredConstructor(cargs);
			c.setAccessible(true); // make the ctor accessible
			// Add annotation for information
			SSDCollection coll = _toCollection(clazz, c.newInstance(args));
			SSDCollection annd = SSDCollection.empty();
			annd.set("class", path);
			if((args.length > 0))
				annd.set("args", SSDFunctionImpl.toArray(args));
			// Add the Instance annotation
			coll.addAnnotation(SSDAnnotation.of("Instance", annd));
			return coll;
		} catch(Exception ex) {
		}
		return null;
	}
	
	@Override
	public Object[] invoke(Object... args) {
		if(args == null || args.length == 0) {
			throw new IllegalArgumentException(
				"Function[instance]: Cannot get an instance from no objects!");
		}
		if(args[0] == null) {
			throw new IllegalArgumentException(
				"Function[instance]: Cannot get an instance from no class!");
		}
		String   path = args[0].toString();
		Object[] aarr = Arrays.stream(args, 1, args.length).toArray(Object[]::new);
		return new Object[] { _value(path, aarr) };
	}
}