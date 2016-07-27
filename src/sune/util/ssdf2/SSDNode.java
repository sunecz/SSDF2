package sune.util.ssdf2;

public interface SSDNode {
	
	public boolean isObject();
	public boolean isCollection();
	public SSDNode getParent();
	public String getFullName();
	public String getName();
	public String toString(boolean compress);
	public SSDAnnotation getAnnotation(String name);
	public SSDAnnotation[] getAnnotations();
	public SSDAnnotation[] getAnnotations(String name);
}