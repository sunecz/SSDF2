package sune.util.ssdf2;

import static sune.util.ssdf2.SSDF.CHAR_DOUBLE_QUOTES;
import static sune.util.ssdf2.SSDF.CHAR_FUNCCALL_ARGS_DELIMITER;
import static sune.util.ssdf2.SSDF.CHAR_FUNCCALL_CB;
import static sune.util.ssdf2.SSDF.CHAR_FUNCCALL_OB;
import static sune.util.ssdf2.SSDF.CHAR_NAME_DELIMITER;
import static sune.util.ssdf2.SSDF.CHAR_SPACE;
import static sune.util.ssdf2.SSDF.WORD_ANNOTATION_DEFAULT;
import static sune.util.ssdf2.SSDF.WORD_NULL;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public class SSDFunctionCall extends SSDObject {
	
	private final String 	   funcName;
	private final Set<SSDNode> funcArgs;
	
	SSDFunctionCall(SSDNode parent, String name, String funcName) {
		this(parent, name, funcName, new LinkedHashSet<>());
	}
	
	SSDFunctionCall(SSDNode parent, String name, String funcName, Set<SSDNode> funcArgs) {
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
			if((FUNCTION_IMPL_CLASS.isAssignableFrom(clazz))) {
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
	@SuppressWarnings("serial")
	static final Object funcArgConv(SSDFunctionCall call, String namespace, SSDNode node) {
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
					SSDAnnotation nsa = new SSDAnnotation(ANNOTATION_NAMESPACE,
						new LinkedHashMap<String, SSDNode>() {{
							put(WORD_ANNOTATION_DEFAULT,
							    	new SSDObject(null, WORD_ANNOTATION_DEFAULT, fn));
						}});
					// Add the namespace to the function implementation
					obj.addAnnotation0(nsa);
				} else {
					namespace = space;
				}
			}
			// Add value of the object based on its type
			switch(obj.getType()) {
				case NULL: 	  	 return null;
				case BOOLEAN: 	 return obj.booleanValue();
				case INTEGER: 	 return obj.longValue();
				case DECIMAL: 	 return obj.doubleValue();
				case STRING:  	 return obj.stringValue();
				case STRING_VAR: return obj.stringValue();
				case UNKNOWN: 	 return ((namespace == null || namespace.isEmpty())
											|| SSDF.func_isContentSimple(
											   		call.funcName,
											   		new ArrayDeque<SSDAnnotation>() {{
											   			for(SSDAnnotation a : call.getAnnotations())
											   				push(a);
											   		}}) ?
					                         "" : // Just do not add any prefix
										 	(namespace + CHAR_NAME_DELIMITER)) +
										 	(obj.stringValue());
			}
		} else if(node.isCollection()) {
			SSDCollection coll = (SSDCollection) node;
			List<Object>  list = new ArrayList<>();
			for(SSDNode item : coll)
				list.add(funcArgConv(call, namespace, item));
			return list.toArray(new Object[list.size()]);
		}
		return null;
	}
	
	static final Object[] convArgs(SSDFunctionCall call, String namespace, Set<SSDNode> nodes) {
		List<Object> list = new ArrayList<>();
		for(SSDNode arg : nodes)
			list.add(funcArgConv(call, namespace, arg));
		return list.toArray(new Object[list.size()]);
	}
	
	static final String ANNOTATION_NAMESPACE = "Namespace";
	static final String getNamespace(SSDNode node) {
		SSDAnnotation nsa;
		if((nsa = node.getAnnotation(ANNOTATION_NAMESPACE)) == null)
			return null;
		SSDObject obj = nsa.getObject(WORD_ANNOTATION_DEFAULT);
		return obj == null ? null : obj.stringValue();
	}
	
	void addArg(SSDNode node) {
		funcArgs.add(node);
	}
	
	void addArgs(SSDNode[] nodes) {
		for(SSDNode node : nodes)
			addArg(node);
	}
	
	public final Object[] invoke() {
		// Add the default namespace if there is none
		String name = funcName;
		String space;
		if((space = getNamespace(this)) == null) {
			if((name.split("\\" + CHAR_NAME_DELIMITER).length == 1))
				name = FUNCTION_PREFIX + name;
		} else  name = space + CHAR_NAME_DELIMITER + name;
		// Invoke the internal method
		return funcInvoke(name, convArgs(this, getNamespace(this), funcArgs));
	}
	
	@Override
	public SSDValue getValue() {
		Object[] vals = invoke();
		return vals != null ? new SSDValue(vals.length == 1 ?
		                                   		vals[0] :
		                                   		vals)
		                    : null;
	}
	
	@Override
	public SSDValue getFormattedValue() {
		return getValue();
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
		return funcArgs.toArray(new SSDNode[funcArgs.size()]);
	}
	
	String toString(int depth, boolean compress, boolean json, boolean invoke,
			boolean info, boolean comments) {
		StringBuilder buffer = new StringBuilder();
		if((invoke)) {
			for(Object value : invoke()) {
				if((value instanceof SSDCollection)) {
					SSDCollection coll = (SSDCollection) value;
					buffer.append(coll.toString(depth,
					                            compress,
					                            json,
					                            invoke,
					                            info,
					                            comments));
				} else {
					buffer.append(value == null ? WORD_NULL
					                            : value.toString());
				}
			}
		} else {
			if(json) {
				buffer.append(CHAR_DOUBLE_QUOTES);
			}
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
						buffer.append(ann.toString(compress, invoke, comments));
						if(!compress) buffer.append(CHAR_SPACE);
					}
				}
				buffer.append(arg.toString(compress, invoke, comments)
				              	 .replaceAll("\"", "\\\\\""));
			}
			buffer.append(CHAR_FUNCCALL_CB);
			if(json) {
				buffer.append(CHAR_DOUBLE_QUOTES);
			}
		}
		return buffer.toString();
	}
	
	@Override
	public String toString() {
		return toString(0, false, false, false, true, true);
	}
	
	@Override
	public String toString(boolean compress) {
		return toString(0, compress, false, false, true, true);
	}
	
	@Override
	public String toString(boolean compress, boolean invoke) {
		return toString(0, compress, false, invoke, true, true);
	}
	
	@Override
	public String toString(boolean compress, boolean invoke, boolean comments) {
		return toString(0, compress, false, invoke, true, comments);
	}
	
	@Override
	public String toString(int depth, boolean compress, boolean invoke) {
		return toString(depth, compress, false, invoke, true, true);
	}
	
	@Override
	public String toString(int depth, boolean compress, boolean invoke, boolean comments) {
		return toString(depth, compress, false, invoke, true, comments);
	}
	
	@Override
	public String toJSON() {
		return toString(0, false, true, false, false, false);
	}
	
	@Override
	public String toJSON(boolean compress) {
		return toString(0, compress, true, false, false, false);
	}
	
	@Override
	public String toJSON(boolean compress, boolean invoke) {
		return toString(0, compress, true, invoke, false, false);
	}
	
	@Override
	public String toJSON(int depth, boolean compress, boolean invoke) {
		return toString(depth, compress, true, invoke, false, false);
	}
}