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
 * This is an experimental class. Use it with caution!
 */
public class SSDCasting {
	
	private static final String ANNOTATION_CAST_EACH = "CastEach";
	
	// Cast an object to its true value that it represents
	static Object _castObject(SSDObject object) {
		switch(object.getType()) {
			case BOOLEAN:
				return object.getFormattedValue().booleanValue();
			case INTEGER:
				return object.getFormattedValue().longValue();
			case DECIMAL:
				return object.getFormattedValue().doubleValue();
			case STRING:
			case STRING_VAR:
			// Also the unknown type should return some value
			case UNKNOWN:
				return object.getFormattedValue().stringValue();
			case NULL:
				return null;
		}
		// For a special exception that should not happen
		return null;
	}
	
	static Object _castCollection(SSDCollection coll) {
		if((coll.getType() == SSDCollectionType.OBJECT)) {
			Map<String, Object> map = new LinkedHashMap<>();
			for(Entry<String, SSDNode> e : coll.objects().entrySet()) {
				String  name = e.getKey();
				SSDNode node = e.getValue();
				// Node is an object
				if((node.isObject())) {
					map.put(name, _castObject((SSDObject) node));
				}
				// Node is a collection
				else if((node.isCollection())) {
					map.put(name, _castCollection((SSDCollection) node));
				}
				// For other cases do not add anything to the map
			}
			// Convert to an unmodifiable map
			return Collections.unmodifiableMap(map);
		} else {
			Set<Object> set = new LinkedHashSet<>();
			for(SSDNode node : coll.objects().values()) {
				// Node is an object
				if((node.isObject())) {
					set.add(_castObject((SSDObject) node));
				}
				// Node is a collection
				else if((node.isCollection())) {
					set.add(_castCollection((SSDCollection) node));
				}
				// For other cases do not add anything to the set
			}
			// Convert to an unmodifiable set
			return Collections.unmodifiableSet(set);
		}
	}
	
	@SuppressWarnings("unchecked")
	public static <T> T cast(Class<T> clazz, SSDCollection args)
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
							// Convert a set to an object array
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
	
	public static <T> T[] tryCast(SSDCollection coll) {
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
					T inst = (T) SSDCasting.cast(clazz, item);
					castArray[index++] = inst;
				}
				// Return the array with casted items
				return castArray;
			} catch(Exception ex) {
				// Throw Cast Exception
				throw new ClassCastException(ex.getLocalizedMessage());
			}
		}
		// Collection cannot be casted
		return null;
	}
}