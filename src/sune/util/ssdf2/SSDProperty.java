package sune.util.ssdf2;

final class SSDProperty<T> {
	
	// The value of this property
	private T value;
	
	public SSDProperty(T value) {
		this.value = value;
	}
	
	public void set(T newValue) {
		value = newValue;
	}
	
	public T get() {
		return value;
	}
}