package sune.util.ssdf;

public interface SSDNode {
	
	public SSDNode getParent();
	public String getFullName();
	public String getName();
	public String toString(boolean compress);
}