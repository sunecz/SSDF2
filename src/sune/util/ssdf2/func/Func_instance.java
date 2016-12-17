package sune.util.ssdf2.func;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.List;
import java.util.Map;

import sune.util.ssdf2.SSDAnnotation;
import sune.util.ssdf2.SSDCollection;
import sune.util.ssdf2.SSDFunctionImpl;

public class Func_instance implements SSDFunctionImpl {
	
	static final Object _value(String path, Object... args) {
		if(path == null || path.isEmpty())
			return null;
		try {
			Class<?>   clazz = Class.forName(path);
			Class<?>[] cargs = FuncUtils.recognizeClasses(args);
			Constructor<?> c = clazz.getDeclaredConstructor(cargs);
			if(!c.isAccessible()) c.setAccessible(true);
			// Add annotation for information
			SSDCollection coll = _toCollection(clazz, c.newInstance(args));
			SSDCollection annd = SSDCollection.empty();
			annd.add("class", path);
			if((args.length > 0))
				annd.add("args", SSDFunctionImpl.toArray(args));
			// Add the Instance annotation
			coll.addAnnotation(SSDAnnotation.of("Instance", annd));
			return coll;
		} catch(Exception ex) {
		}
		return null;
	}
	
	static final SSDCollection _toCollection(Class<?> clazz, Object instance) {
		SSDCollection coll = SSDCollection.empty();
		for(Field field : clazz.getDeclaredFields()) {
			try {
				if(!field.isAccessible())
					field.setAccessible(true);
				if((Modifier.isStatic(field.getModifiers())))
					continue;
				String name  = field.getName();
				Object value = field.get(instance);
				if((value.getClass().isArray())) {
					value = SSDFunctionImpl.toArray(SSDFunctionImpl.toObjectArray(value));
					coll.add(name, (SSDCollection) value);
				} else if(value instanceof List) {
					value = SSDFunctionImpl.toArray((List<?>) value);
					coll.add(name, (SSDCollection) value);
				} else if(value instanceof Map) {
					value = SSDFunctionImpl.toArray((Map<?, ?>) value);
					coll.add(name, (SSDCollection) value);
				} else {
					coll.add(name, SSDFunctionImpl.object(name, value));
				}
			} catch(Exception ex) {
			}
		}
		return coll;
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
		String 	 path 	= args[0].toString();
		int 	 length = args.length-1;
		Object[] args0  = new Object[length];
		System.arraycopy(args, 1, args0, 0, length);
		return new Object[] { _value(path, args0) };
	}
}