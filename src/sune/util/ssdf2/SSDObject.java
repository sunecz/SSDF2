package sune.util.ssdf2;

import static sune.util.ssdf2.SSDF.CHAR_SPACE;
import static sune.util.ssdf2.SSDF.WORD_NULL;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class SSDObject implements SSDNode {
	
	// Protected properties
	protected final SSDProperty<SSDNode> parent;
	protected final SSDProperty<String>  name;
	
	// Private properties
	private final SSDType  type;
	private final SSDValue value;
	// Formatted value
	private final SSDValue fvalue;
	
	// Annotations
	private final List<SSDAnnotation> annotations;
	
	SSDObject(SSDNode parent, String name, String value) {
		SSDType type  = SSDType.recognize(value);
		SSDValue val  = new SSDValue(value);
		SSDValue fval = type.format(value);
		checkArgs(name, type, fval);
		this.parent = new SSDProperty<>(parent);
		this.name 	= new SSDProperty<>(name);
		this.type 	= type;
		this.value	= val;
		this.fvalue = fval;
		// Annotations
		this.annotations = new ArrayList<>();
	}
	
	SSDObject(SSDNode parent, String name, SSDType type, SSDValue value, SSDValue fvalue) {
		checkArgs(name, type, value);
		this.parent = new SSDProperty<>(parent);
		this.name 	= new SSDProperty<>(name);
		this.type 	= type;
		this.value 	= value;
		this.fvalue = fvalue;
		// Annotations
		this.annotations = new ArrayList<>();
	}
	
	static final void checkArgs(String name, SSDType type, SSDValue value) {
		if(name == null) {
			throw new IllegalArgumentException(
				"Name of an object cannot be null!");
		}
		if(type == null) {
			throw new IllegalArgumentException(
				"Type of an object cannot be null!");
		}
		if(value == null) {
			throw new IllegalArgumentException(
				"Value of an object cannot be null!");
		}
	}
	
	static final Random RANDOM;
	static final String RANDOM_CHARS;
	static {
		RANDOM		 = new Random();
		RANDOM_CHARS = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz_";
	}
	
	static final String genRandomName(int length) {
		StringBuilder sb = new StringBuilder();
		for(int i = length, l = RANDOM_CHARS.length(); --i >= 0;)
			sb.append(RANDOM_CHARS.charAt(RANDOM.nextInt(l)));
		return sb.toString();
	}
	
	public static SSDObject of(Object value) {
		return of(genRandomName(16), value);
	}
	
	public static SSDObject of(String name, Object value) {
		return new SSDObject(null, name, value != null
											? value.toString()
											: WORD_NULL);
	}
	
	void addAnnotations(List<SSDAnnotation> annotations) {
		if(annotations != null) {
			this.annotations.addAll(annotations);
		}
	}
	
	void addAnnotation(SSDAnnotation annotation) {
		if(annotation != null) {
			this.annotations.add(annotation);
		}
	}
	
	@Override
	public SSDNode getParent() {
		return parent.get();
	}
	
	@Override
	public String getName() {
		return name.get();
	}
	
	@Override
	public String getFullName() {
		SSDNode p = parent.get();
		return (p == null ? "" : (p.getFullName() + CHAR_SPACE)) +
			   (getName());
	}
	
	public SSDType getType() {
		return type;
	}
	
	SSDValue getValue() {
		return value;
	}
	
	SSDValue getFormattedValue() {
		return fvalue;
	}
	
	public boolean booleanValue() 				 { return fvalue.booleanValue(); }
	public byte byteValue() 	  				 { return fvalue.byteValue(); 	 }
	public short shortValue() 	  				 { return fvalue.shortValue();   }
	public int intValue() 		  				 { return fvalue.intValue(); 	 }
	public long longValue() 	  				 { return fvalue.longValue(); 	 }
	public float floatValue() 	  				 { return fvalue.floatValue();   }
	public double doubleValue()   				 { return fvalue.doubleValue();  }
	public String stringValue()   				 { return fvalue.stringValue();  }
	public Object value() 		 				 { return fvalue.value(); 		 }
	public <T> T value(Class<? extends T> clazz) { return fvalue.value(clazz); 	 }
	
	@Override
	public SSDAnnotation getAnnotation(String name) {
		for(SSDAnnotation ann : annotations)
			if(ann.getName().equals(name))
				return ann;
		return null;
	}
	
	@Override
	public SSDAnnotation[] getAnnotations() {
		return annotations.toArray(new SSDAnnotation[annotations.size()]);
	}
	
	@Override
	public SSDAnnotation[] getAnnotations(String name) {
		List<SSDAnnotation> list = new ArrayList<>();
		for(SSDAnnotation ann : annotations)
			if(ann.getName().equals(name))
				list.add(ann);
		return list.toArray(new SSDAnnotation[list.size()]);
	}
	
	@Override
	public boolean isObject() {
		return true;
	}
	
	@Override
	public boolean isCollection() {
		return false;
	}
	
	@Override
	public String toString() {
		return toString(false);
	}
	
	public String toString(boolean compress) {
		// No compression can be done here
		return value == null ? WORD_NULL : value.toString();
	}
}