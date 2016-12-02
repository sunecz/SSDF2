package sune.util.ssdf2;

import static sune.util.ssdf2.SSDF.CHAR_FUNCCALL_ARGS_DELIMITER;
import static sune.util.ssdf2.SSDF.CHAR_FUNCCALL_CB;
import static sune.util.ssdf2.SSDF.CHAR_FUNCCALL_OB;
import static sune.util.ssdf2.SSDF.CHAR_NAME_DELIMITER;
import static sune.util.ssdf2.SSDF.CHAR_NEWLINE;
import static sune.util.ssdf2.SSDF.CHAR_SPACE;
import static sune.util.ssdf2.SSDF.CHAR_TAB;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

public class SSDFunctionCall extends SSDObject {
	
	private final String    funcName;
	private final SSDNode[] funcArgs;
	
	SSDFunctionCall(SSDNode parent, String name, String funcName, SSDNode[] funcArgs) {
		super(parent, name, SSDType.UNKNOWN, new SSDValue(""), new SSDValue(""));
		this.funcName = funcName;
		this.funcArgs = funcArgs;
	}
	
	static final String   FUNCTION_PREFIX 	  = "sune.util.ssdf2.func.Func_";
	static final Class<?> FUNCTION_IMPL_CLASS = SSDFunctionImpl.class;
	static final Object[] funcInvoke(String name, Object... args) {
		try {
			Class<?> clazz = Class.forName(name);
			// Check if the class is implementation of SSDFunction
			if(FUNCTION_IMPL_CLASS.isAssignableFrom(clazz)) {
				// Create a new object of function implementation
				SSDFunctionImpl impl = (SSDFunctionImpl) clazz.newInstance();
				// Invoke the function implementation
				return impl.invoke(args);
			}
		} catch(Exception ex) {
		}
		return null;
	}
	
	// Convert a SSDNode to an Object
	static final Object funcArgConv(String namespace, SSDNode node) {
		if(node.isObject()) {
			SSDObject obj = (SSDObject) node;
			// Special condition for function implementation and unknown type
			if(obj instanceof SSDFunctionCall || obj.getType() == SSDType.UNKNOWN) {
				String space;
				if((space = getNamespace(obj)) == null
						&& obj.getAnnotation(ANNOTATION_NAMESPACE) == null
						&& namespace != null) {
					final String fn = namespace;
					// Create a Namespace annotation
					@SuppressWarnings("serial")
					SSDAnnotation nsa = new SSDAnnotation(ANNOTATION_NAMESPACE,
						new LinkedHashMap<String, SSDNode>() {{
							put("value", new SSDObject(null, "value", fn));
						}});
					// Add the namespace to the function implementation
					obj.addAnnotation(nsa);
				} else {
					namespace = space;
				}
			}
			// Add value of the object based on its type
			switch(obj.getType()) {
				case NULL: 	  return null;
				case BOOLEAN: return obj.booleanValue();
				case INTEGER: return obj.longValue();
				case DECIMAL: return obj.doubleValue();
				case STRING:  return obj.stringValue();
				case UNKNOWN: return (namespace == null || namespace.isEmpty() ?
				                         "" : // Just do not add any prefix
									 	(namespace + CHAR_NAME_DELIMITER)) +
									 	(obj.stringValue());
			}
		} else if(node.isCollection()) {
			SSDCollection coll = (SSDCollection) node;
			List<Object>  list = new ArrayList<>();
			for(SSDNode item : coll)
				list.add(funcArgConv(namespace, item));
			return list.toArray(new Object[list.size()]);
		}
		return null;
	}
	
	static final Object[] convArgs(String namespace, SSDNode[] nodes) {
		List<Object> list = new ArrayList<>();
		for(SSDNode arg : nodes)
			list.add(funcArgConv(namespace, arg));
		return list.toArray(new Object[list.size()]);
	}
	
	static final String ANNOTATION_NAMESPACE = "Namespace";
	static final String getNamespace(SSDNode node) {
		SSDAnnotation nsa;
		if((nsa = node.getAnnotation(ANNOTATION_NAMESPACE)) == null)
			return null;
		SSDObject obj = nsa.getObject("value");
		return obj == null ? null : obj.stringValue();
	}
	
	public final Object[] invoke() {
		// Add the default namespace if there is none
		String name = funcName;
		String space;
		if((space = getNamespace(this)) == null) {
			if((name.split("\\.").length == 1))
				name = FUNCTION_PREFIX + name;
		} else  name = space + name;
		// Invoke the internal method
		return funcInvoke(name, convArgs(getNamespace(this), funcArgs));
	}
	
	@Override
	SSDValue getValue() {
		Object[] vals = invoke();
		return vals != null ? new SSDValue(vals.length == 1 ?
		                                   		vals[0] :
		                                   		vals)
		                    : null;
	}
	
	@Override
	SSDValue getFormattedValue() {
		return getValue();
	}
	
	// Special method for getting formatted value of function output
	// with each line prefixed with the specific amount of tabs.
	SSDValue getFormattedValue(int depth) {
		SSDValue value = getValue();
		if(value != null) {
			StringBuilder sb = new StringBuilder();
			value.stringValue().chars().forEach((c) -> {
				sb.append((char) c);
				if(c == CHAR_NEWLINE) {
					for(int i = depth; --i >= 0;)
						sb.append(CHAR_TAB);
				}
			});
			return new SSDValue(sb.toString());
		}
		return null;
	}
	
	public boolean booleanValue() 				 { return getValue().booleanValue(); }
	public byte byteValue() 	  				 { return getValue().byteValue(); 	 }
	public short shortValue() 	  				 { return getValue().shortValue();   }
	public int intValue() 		  				 { return getValue().intValue(); 	 }
	public long longValue() 	  				 { return getValue().longValue(); 	 }
	public float floatValue() 	  				 { return getValue().floatValue();   }
	public double doubleValue()   				 { return getValue().doubleValue();  }
	public String stringValue()   				 { return getValue().stringValue();  }
	public Object value() 		 				 { return getValue().value(); 		 }
	public <T> T value(Class<? extends T> clazz) { return getValue().value(clazz); 	 }
	
	public String getFunctionName() {
		return funcName;
	}
	
	public SSDNode[] getFunctionArgs() {
		return funcArgs;
	}
	
	@Override
	public String toString() {
		return toString(false);
	}
	
	@Override
	public String toString(boolean compress) {
		StringBuilder buffer = new StringBuilder();
		buffer.append(funcName);
		buffer.append(CHAR_FUNCCALL_OB);
		boolean first = true;
		for(SSDNode arg : funcArgs) {
			if((first)) {
				first = false;
			} else {
				buffer.append(CHAR_FUNCCALL_ARGS_DELIMITER);
				if(!compress)
					buffer.append(CHAR_SPACE);
			}
			// Append all argument's annotations
			SSDAnnotation[] anns;
			if((anns = arg.getAnnotations()).length > 0) {
				for(SSDAnnotation ann : anns) {					
					buffer.append(ann.toString(compress));
					if(!compress) buffer.append(CHAR_SPACE);
				}
			}
			buffer.append(arg.toString(compress));
		}
		buffer.append(CHAR_FUNCCALL_CB);
		return buffer.toString();
	}
}