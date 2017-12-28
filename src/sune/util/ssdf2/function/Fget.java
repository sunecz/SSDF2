package sune.util.ssdf2.function;

import java.util.Arrays;

import sune.util.ssdf2.SSDCollection;
import sune.util.ssdf2.SSDF;
import sune.util.ssdf2.SSDFunctionImpl;
import sune.util.ssdf2.SSDNode;
import sune.util.ssdf2.SSDObject;

public class Fget implements SSDFunctionImpl {
	
	String _instance;
	final Object _value(Object path) {
		if((_instance == null
				|| _instance.isEmpty()))
			return null;
		if((path == null
				|| !(path instanceof String)
				|| ((String) path).isEmpty()))
			return null;
		SSDCollection coll = SSDF.read(_instance);
		SSDNode	  	  node = coll.get((String) path);
		if((node.isObject())) {
			SSDObject object = (SSDObject) node;
			switch(object.getType()) {
				case NULL: 	  	 return null;
				case BOOLEAN:	 return object.booleanValue();
				case INTEGER: 	 return object.longValue();
				case DECIMAL: 	 return object.doubleValue();
				case STRING:  	 return object.stringValue();
				case STRING_VAR: return object.stringValue();
				case UNKNOWN: 	 return object.stringValue();
			}
		} else if((node.isCollection())) {
			return node.toString();
		}
		return null;
	}
	
	@Override
	public Object[] invoke(Object... args) {
		if(args == null || args.length == 0) {
			throw new IllegalArgumentException(
				"Function[get]: Cannot get no objects!");
		}
		if(args[0] == null) {
			throw new IllegalArgumentException(
				"Function[get]: Instance cannot be null!");
		}
		_instance = args[0].toString();
		return Arrays.stream(args, 1, args.length)
					 .map(this::_value)
					 .toArray(Object[]::new);
	}
}