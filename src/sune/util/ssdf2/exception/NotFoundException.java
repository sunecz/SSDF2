package sune.util.ssdf2.exception;

public class NotFoundException extends RuntimeException {
	
	private static final long serialVersionUID = 1165468453116003993L;
	
	public NotFoundException() {
	}
	
	public NotFoundException(String message) {
		super(message);
	}
}