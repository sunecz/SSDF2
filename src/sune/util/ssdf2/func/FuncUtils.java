package sune.util.ssdf2.func;

public class FuncUtils {
	
	public static final Class<?>[] recognizeClasses(Object... arguments) {
		int length 		   = arguments.length;
		Class<?>[] classes = new Class<?>[length];
		for(int i = 0; i < length; ++i) {
			Class<?> clazz = arguments[i].getClass();
			classes[i] 	   = toPrimitive(clazz);
		}
		return classes;
	}
	
	public static final Class<?> toPrimitive(Class<?> clazz) {
		if(clazz == Boolean.class) 	 return boolean.class;
        if(clazz == Byte.class) 	 return byte.class;
        if(clazz == Character.class) return char.class;
        if(clazz == Short.class) 	 return short.class;
        if(clazz == Integer.class) 	 return int.class;
        if(clazz == Long.class) 	 return long.class;
        if(clazz == Float.class) 	 return float.class;
        if(clazz == Double.class) 	 return double.class;
        if(clazz == Void.class) 	 return void.class;
		return clazz;
	}
}