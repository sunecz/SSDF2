package sune.util.ssdf2;

public class SSDObject implements SSDNode {
	
	// Protected properties
	protected final SSDProperty<SSDNode> parent;
	protected final SSDProperty<String>  name;
	
	// Private properties
	private final SSDType  type;
	private final SSDValue value;
	// Formatted value
	private final SSDValue fvalue;
	
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
	}
	
	SSDObject(SSDNode parent, String name, SSDType type, SSDValue value, SSDValue fvalue) {
		checkArgs(name, type, value);
		this.parent = new SSDProperty<>(parent);
		this.name 	= new SSDProperty<>(name);
		this.type 	= type;
		this.value 	= value;
		this.fvalue = fvalue;
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
		return (p == null ? "" : (p.getFullName() + ".")) +
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
		return value == null ? "null" : value.toString();
	}
}