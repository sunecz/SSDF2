package sune.util.ssdf2;

import static sune.util.ssdf2.SSDF.WORD_NULL;

import java.lang.reflect.Array;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

public interface SSDFunctionImpl {
	
	static SSDValue value(Object value) {
		// Just create a new instance of SSDValue
		return new SSDValue(value);
	}
	
	static SSDObject object(String name, Object value) {
		// Check the value first
		String string;
		if(value != null) {
			string 		 = value.toString();
			SSDType type = SSDType.recognize(string);
			if((type  == SSDType.UNKNOWN))
				// Fix the value as a string
				string = SSDType.STRING.fixValue(string);
		} else string = WORD_NULL;
		// Just create a new instance of SSDObject
		return new SSDObject(null, name, string);
	}
	
	static Object[] toObjectArray(Object array) {
		int 	 length  = Array.getLength(array);
		Object[] objects = new Object[length];
		for(int i = 0; i < length; ++i)
			objects[i] = Array.get(array, i);
		return objects;
	}
	
	// Array conversion
	static SSDCollection toArray(Object[] array) {
		SSDCollection coll = SSDCollection.emptyArray();
		for(Object object : array) {
			if(object != null) {
				if(object.getClass().isArray()) {
					coll.add(toArray(toObjectArray(object)));
				} else if(object instanceof List) {
					coll.add(toArray((List<?>) object));
				} else if(object instanceof Map) {
					coll.add(toArray((Map<?, ?>) object));
				} else if(object instanceof Set) {
					coll.add(toArray((Set<?>) object));
				} else {
					coll.add(object("", object));
				}
			} else coll.addNull();
		}
		return coll;
	}
	
	// List conversion
	static SSDCollection toArray(List<?> list) {
		SSDCollection coll = SSDCollection.emptyArray();
		for(Object object : list) {
			if(object != null) {
				if(object.getClass().isArray()) {
					coll.add(toArray(toObjectArray(object)));
				} else if(object instanceof List) {
					coll.add(toArray((List<?>) object));
				} else if(object instanceof Map) {
					coll.add(toArray((Map<?, ?>) object));
				} else if(object instanceof Set) {
					coll.add(toArray((Set<?>) object));
				} else {
					coll.add(object("", object));
				}
			} else coll.addNull();
		}
		return coll;
	}
	
	// Map conversion
	static SSDCollection toArray(Map<?, ?> map) {
		SSDCollection coll = SSDCollection.empty();
		for(Entry<?, ?> entry : map.entrySet()) {
			Object name  = entry.getKey();
			Object value = entry.getValue();
			String sname = name == null ? WORD_NULL : name.toString();
			if(value != null) {
				if(value.getClass().isArray()) {
					coll.set(sname, toArray(toObjectArray(value)));
				} else if(value instanceof List) {
					coll.set(sname, toArray((List<?>) value));
				} else if(value instanceof Map) {
					coll.set(sname, toArray((Map<?, ?>) value));
				} else if(value instanceof Set) {
					coll.set(sname, toArray((Set<?>) value));
				} else {
					coll.set(sname, object("", value));
				}
			} else coll.setNull(sname);
		}
		return coll;
	}
	
	// Set conversion
	static SSDCollection toArray(Set<?> list) {
		SSDCollection coll = SSDCollection.emptyArray();
		for(Object object : list) {
			if(object != null) {
				if(object.getClass().isArray()) {
					coll.add(toArray(toObjectArray(object)));
				} else if(object instanceof List) {
					coll.add(toArray((List<?>) object));
				} else if(object instanceof Map) {
					coll.add(toArray((Map<?, ?>) object));
				} else if(object instanceof Set) {
					coll.add(toArray((Set<?>) object));
				} else {
					coll.add(object("", object));
				}
			} else coll.addNull();
		}
		return coll;
	}
	
	Object[] invoke(Object... args);
}