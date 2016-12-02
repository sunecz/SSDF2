package sune.util.ssdf2;

public class SSDAccessor {
	
	// Private constructor
	private SSDAccessor() {}
	
	public static SSDValue getValue(SSDObject object) {
		if((object == null))
			throw new IllegalArgumentException(
				"Cannot get value of a null object!");
		return object.getValue();
	}
	
	public static SSDValue getFormattedValue(SSDObject object) {
		if((object == null))
			throw new IllegalArgumentException(
				"Cannot get value of a null object!");
		return object.getFormattedValue();
	}
	
	public static String fixValue(SSDType type, String value) {
		if((value == null))
			throw new IllegalArgumentException(
				"Cannot fix value of a null object!");
		return type.fixValue(value);
	}
}