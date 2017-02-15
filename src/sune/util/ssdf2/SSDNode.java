package sune.util.ssdf2;

public interface SSDNode {
	
	SSDNode copy();
	boolean isObject();
	boolean isCollection();
	SSDNode getParent();
	String getFullName();
	String getName();
	String toString();
	String toString(boolean compress);
	String toString(boolean compress, boolean invoke);
	String toString(boolean compress, boolean invoke, boolean comments);
	String toString(int depth, boolean compress, boolean invoke);
	String toString(int depth, boolean compress, boolean invoke, boolean comments);
	String toJSON();
	String toJSON(boolean compress);
	String toJSON(boolean compress, boolean invoke);
	String toJSON(int depth, boolean compress, boolean invoke);
	// Annotations
	SSDAnnotation getAnnotation(String name);
	SSDAnnotation[] getAnnotations();
	SSDAnnotation[] getAnnotations(String name);
	// Comments
	void addComment(SSDComment comment);
	void removeComment(SSDComment comment);
	SSDComment[] getComments();
}