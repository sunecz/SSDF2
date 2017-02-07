package sune.util.ssdf2;

public interface SSDNode {
	
	boolean isObject();
	boolean isCollection();
	SSDNode getParent();
	String getFullName();
	String getName();
	String toString();
	String toString(boolean compress);
	String toString(boolean compress, boolean invoke);
	String toString(int depth, boolean compress, boolean invoke);
	String toJSON();
	String toJSON(boolean compress);
	String toJSON(boolean compress, boolean invoke);
	String toJSON(int depth, boolean compress, boolean invoke);
	SSDAnnotation getAnnotation(String name);
	SSDAnnotation[] getAnnotations();
	SSDAnnotation[] getAnnotations(String name);
}