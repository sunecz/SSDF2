package sune.util.ssdf;

public final class SSDValue {
	
	private final Object value;
	SSDValue(Object value) {
		this.value = value;
	}
	
	public boolean booleanValue() { return (boolean) value; }
	public byte byteValue() 	  { return (byte) 	 value; }
	public short shortValue() 	  { return (short) 	 value; }
	public int intValue() 		  { return (int) 	 value; }
	public long longValue() 	  { return (long) 	 value; }
	public float floatValue() 	  { return (float) 	 value; }
	public double doubleValue()   { return (double)  value; }
	public String stringValue()   { return (String)  value; }
	public Object value() 		  { return 			 value; }
	
	@SuppressWarnings("unchecked")
	<T> T cast(Class<? extends T> clazz) {
		if((value == null)) return null;
		if(!value.getClass().isAssignableFrom(clazz)) {
			throw new ClassCastException(
				"Cannot cast value (of type " + value.getClass() + ") " +
				"to type " + clazz + "!");
		}
		return (T) value;
	}
	public <T> T value(Class<? extends T> clazz) { return cast(clazz); }
	
	@Override
	public String toString() {
		return value == null ? "null" : value.toString();
	}
}