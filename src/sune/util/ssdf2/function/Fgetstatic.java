package sune.util.ssdf2.function;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import sune.util.ssdf2.SSDFunctionImpl;

public class Fgetstatic implements SSDFunctionImpl {
	
	final Object _value(Object path) {
		if((path == null
				|| !(path instanceof String)
				|| ((String) path).isEmpty()))
			return null;
		try {
			String pathStr = (String) path;
			int dotIndex   = pathStr.lastIndexOf('.');
			if((dotIndex) == -1) return null;
			String 	 pathClass  = pathStr.substring(0, dotIndex);
			String   pathObject = pathStr.substring(dotIndex+1);
			Class<?> clazz	    = Class.forName(pathClass);
			Field	 field		= clazz.getDeclaredField(pathObject);
			field.setAccessible(true); // make the field accessible
			if(!Modifier.isStatic(field.getModifiers()))
				return null;
			Object value = field.get(null);
			if((value.getClass().isArray())) {
				return SSDFunctionImpl.toArray(SSDFunctionImpl.toObjectArray(value));
			} else if(value instanceof List) {
				return SSDFunctionImpl.toArray((List<?>) value);
			} else if(value instanceof Map) {
				return SSDFunctionImpl.toArray((Map<?, ?>) value);
			} else {
				return SSDFunctionImpl.object("_static", value);
			}
		} catch(Exception ex) {
		}
		return null;
	}
	
	@Override
	public Object[] invoke(Object... args) {
		if(args == null || args.length == 0) {
			throw new IllegalArgumentException(
				"Function[getstatic]: Cannot get no objects!");
		}
		return Arrays.stream(args, 0, args.length)
					 .map(this::_value)
					 .toArray(Object[]::new);
	}
}