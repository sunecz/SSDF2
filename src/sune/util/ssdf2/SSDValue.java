package sune.util.ssdf2;

import static sune.util.ssdf2.SSDF.WORD_NULL;

public final class SSDValue {
	
	private final Object value;
	SSDValue(Object value) {
		this.value = value;
	}
	
	public boolean booleanValue() { return Boolean.valueOf(toString()); }
	public byte byteValue() 	  { return Byte	  .valueOf(toString()); }
	public short shortValue() 	  { return Short  .valueOf(toString()); }
	public int intValue() 		  { return Integer.valueOf(toString()); }
	public long longValue() 	  { return Long	  .valueOf(toString()); }
	public float floatValue() 	  { return Float  .valueOf(toString()); }
	public double doubleValue()   { return Double .valueOf(toString()); }
	public String stringValue()   { return toString(); }
	public Object value() 		  { return value; }
	
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
		return value == null ? WORD_NULL : value.toString();
	}
}