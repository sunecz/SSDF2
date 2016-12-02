package sune.util.ssdf2.func;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import sune.util.ssdf2.SSDFunctionImpl;

public class Func_getstatic implements SSDFunctionImpl {
	
	static final Object _value(String path) {
		if(path == null || path.isEmpty())
			return null;
		try {
			int dotIndex = path.lastIndexOf('.');
			if((dotIndex) == -1) return null;
			String 	 pathClass  = path.substring(0, dotIndex);
			String   pathObject = path.substring(dotIndex+1);
			Class<?> clazz	    = Class.forName(pathClass);
			Field	 field		= clazz.getDeclaredField(pathObject);
			if(!field.isAccessible())
				field.setAccessible(true);
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
		List<Object> vals = new ArrayList<>();
		for(Object arg : args)
			vals.add(_value(arg.toString()));
		return vals.toArray(new Object[vals.size()]);
	}
}