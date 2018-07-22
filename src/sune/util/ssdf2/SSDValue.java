package sune.util.ssdf2;

import static sune.util.ssdf2.SSDF.WORD_NULL;

public final class SSDValue {
	
	private final Object value;
	SSDValue(Object value) {
		this.value = value;
	}
	
	public final boolean booleanValue() { return Boolean.valueOf(toString()); }
	public final byte byteValue()       { return Byte   .valueOf(toString()); }
	public final short shortValue()     { return Short  .valueOf(toString()); }
	public final int intValue()         { return Integer.valueOf(toString()); }
	public final long longValue()       { return Long   .valueOf(toString()); }
	public final float floatValue()     { return Float  .valueOf(toString()); }
	public final double doubleValue()   { return Double .valueOf(toString()); }
	public final String stringValue()   { return toString();                  }
	public final Object value()         { return value;                       }
	
	@Override
	public final String toString() {
		return value == null ? WORD_NULL : value.toString();
	}
}