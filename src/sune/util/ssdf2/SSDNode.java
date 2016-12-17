package sune.util.ssdf2;

public interface SSDNode {
	
	boolean isObject();
	boolean isCollection();
	SSDNode getParent();
	String getFullName();
	String getName();
	String toString(boolean compress);
	String toString(boolean compress, boolean invoke);
	SSDAnnotation getAnnotation(String name);
	SSDAnnotation[] getAnnotations();
	SSDAnnotation[] getAnnotations(String name);
}