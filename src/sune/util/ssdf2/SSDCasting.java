package sune.util.ssdf2;

import static sune.util.ssdf2.SSDF.WORD_ANNOTATION_DEFAULT;

import java.lang.annotation.Annotation;
import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Parameter;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

/**
 * This is an experimental class. Use it with caution!*/
public final class SSDCasting {
	
	private static final String ANNOTATION_CAST_EACH = "CastEach";
	
	// cast an object to its true value that it represents
	static final Object _castObject(SSDObject object) {
		switch(object.getType()) {
			case BOOLEAN:
				return object.getFormattedValue().booleanValue();
			case INTEGER:
				return object.getFormattedValue().longValue();
			case DECIMAL:
				return object.getFormattedValue().doubleValue();
			case STRING:
			case STRING_VAR:
			// also the unknown type should return a value
			case UNKNOWN:
				return object.getFormattedValue().stringValue();
			case NULL:
				return null;
		}
		// this should not happen
		return null;
	}
	
	static final Object _castCollection(SSDCollection coll) {
		if((coll.getType() == SSDCollectionType.OBJECT)) {
			Map<String, Object> map = new LinkedHashMap<>();
			for(Entry<String, SSDNode> e : coll.objectMap().entrySet()) {
				String  name = e.getKey();
				SSDNode node = e.getValue();
				// node is an object
				if((node.isObject())) {
					map.put(name, _castObject((SSDObject) node));
				}
				// node is a collection
				else if((node.isCollection())) {
					map.put(name, _castCollection((SSDCollection) node));
				}
				// for other cases do not add anything to the map
			}
			// convert to an unmodifiable map
			return Collections.unmodifiableMap(map);
		} else {
			Set<Object> set = new LinkedHashSet<>();
			for(SSDNode node : coll.objectMap().values()) {
				// node is an object
				if((node.isObject())) {
					set.add(_castObject((SSDObject) node));
				}
				// node is a collection
				else if((node.isCollection())) {
					set.add(_castCollection((SSDCollection) node));
				}
				// for other cases do not add anything to the set
			}
			// convert to an unmodifiable set
			return Collections.unmodifiableSet(set);
		}
	}
	
	@SuppressWarnings("unchecked")
	public static final <T> T cast(Class<T> clazz, SSDCollection args)
			throws InstantiationException,
				   IllegalAccessException,
				   IllegalArgumentException,
				   InvocationTargetException {
		constructors:
		for(Constructor<?> con : clazz.getDeclaredConstructors()) {
			con.setAccessible(true); // make the ctor accessible
			Parameter[] params = con.getParameters();
			Object[] 	vals   = new Object[params.length];
			int 		index  = 0;
			for(Parameter param : params) {
				Annotation annName;
				if((annName = param.getAnnotation(SSDNamedArg.class)) != null) {
					String name = ((SSDNamedArg) annName).value();
					SSDNode node;
					if((node = args.get(name)) != null) {
						if((node.isObject())) {
							vals[index++] = ((SSDObject) node).getFormattedValue().value();
						} else if((node.isCollection())) {
							Object val = _castCollection((SSDCollection) node);
							// convert a set to an object array
							if((val instanceof Set)) {
								Set<?> set = (Set<?>) val;
								val = set.toArray(new Object[set.size()]);
							}
							vals[index++] = val;
						}
					} else continue constructors;
				}
			}
			if((index == params.length)) {
				return (T) con.newInstance(vals);
			}
		}
		return null;
	}
	
	public static final <T> T[] tryCast(SSDCollection coll) {
		if((coll.hasAnnotation(ANNOTATION_CAST_EACH))) {
			String clazzName = coll.getAnnotation(ANNOTATION_CAST_EACH)
								   .getString(WORD_ANNOTATION_DEFAULT);
			try {
				Class<?> 		clazz = Class.forName(clazzName);
				SSDCollection[] array = coll.toCollectionArray();
				@SuppressWarnings("unchecked")
				T[] castArray = (T[]) Array.newInstance(clazz, array.length);
				int index	  = 0;
				for(SSDCollection item : array) {	
					@SuppressWarnings("unchecked")
					T inst = (T) cast(clazz, item);
					castArray[index++] = inst;
				}
				// return the array with casted items
				return castArray;
			} catch(Exception ex) {
				// throw Class Cast Exception
				throw new ClassCastException(ex.getLocalizedMessage());
			}
		}
		// collection cannot be casted
		return null;
	}
	
	public static final <T> T tryCast(SSDObject object) {
		try {
			@SuppressWarnings("unchecked")
			T inst = (T) _castObject(object);
			return inst;
		} catch(Exception ex) {
			// throw Class Cast Exception
			throw new ClassCastException(ex.getLocalizedMessage());
		}
	}
	
	private SSDCasting() {
	}
}