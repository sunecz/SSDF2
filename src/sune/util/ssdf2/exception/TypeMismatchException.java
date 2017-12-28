package sune.util.ssdf2.exception;

public class TypeMismatchException extends RuntimeException {
	
	private static final long serialVersionUID = -2133375803371739493L;
	
	public TypeMismatchException() {
	}
	
	public TypeMismatchException(String message) {
		super(message);
	}
}