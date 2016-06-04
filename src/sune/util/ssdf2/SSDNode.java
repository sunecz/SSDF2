package sune.util.ssdf2;

public interface SSDNode {
	
	public SSDNode getParent();
	public String getFullName();
	public String getName();
	public String toString(boolean compress);
}