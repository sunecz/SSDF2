package sune.util.ssdf2.func;

import java.util.ArrayList;
import java.util.List;

import sune.util.ssdf2.SSDCollection;
import sune.util.ssdf2.SSDF;
import sune.util.ssdf2.SSDFunctionImpl;
import sune.util.ssdf2.SSDNode;
import sune.util.ssdf2.SSDObject;

public class Func_get implements SSDFunctionImpl {
	
	static final Object _value(String instance, String path) {
		if(instance == null || instance.isEmpty() ||
		   path 	== null || path	   .isEmpty())
			return null;
		SSDCollection coll = SSDF.read(instance);
		SSDNode	  	  node = coll.get(path);
		if((node.isObject())) {
			SSDObject object = (SSDObject) node;
			switch(object.getType()) {
				case NULL: 	  return null;
				case BOOLEAN: return object.booleanValue();
				case INTEGER: return object.longValue();
				case DECIMAL: return object.doubleValue();
				case STRING:  return object.stringValue();
				case UNKNOWN: return object.stringValue();
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
		String instance   = args[0].toString();
		List<Object> vals = new ArrayList<>();
		for(int i = 1, l = args.length; i < l; ++i)
			vals.add(_value(instance, args[i].toString()));
		return vals.toArray(new Object[vals.size()]);
	}
}