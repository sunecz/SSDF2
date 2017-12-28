package sune.util.ssdf2;

import static sune.util.ssdf2.SSDF.CHAR_ANNOTATION_CB;
import static sune.util.ssdf2.SSDF.CHAR_ANNOTATION_ITEM_DELIMITER;
import static sune.util.ssdf2.SSDF.CHAR_ANNOTATION_NV_DELIMITER;
import static sune.util.ssdf2.SSDF.CHAR_ANNOTATION_OB;
import static sune.util.ssdf2.SSDF.CHAR_ANNOTATION_SIGN;
import static sune.util.ssdf2.SSDF.CHAR_NEWLINE;
import static sune.util.ssdf2.SSDF.CHAR_SPACE;
import static sune.util.ssdf2.SSDF.WORD_ANNOTATION_DEFAULT;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

public class SSDAnnotation extends SSDCollection {
	
	SSDAnnotation(String name) {
		this(name, new LinkedHashMap<>());
	}
	
	SSDAnnotation(String name, Map<String, SSDNode> data) {
		super(null, name, false, data, null, null);
	}
	
	public static final SSDAnnotation of(String name) {
		return of(name, SSDCollection.empty());
	}
	
	public static final SSDAnnotation of(String name, SSDCollection data) {
		return new SSDAnnotation(name, data.objects());
	}
	
	@Override
	public void addAnnotation(SSDAnnotation annotation) {
		// Do nothing
	}
	
	@Override
	public void removeAnnotation(String name) {
		// Do nothing
	}
	
	@Override
	public void removeAnnotation(SSDAnnotation annotation) {
		// Do nothing
	}
	
	@Override
	void removeAnnotationEq(SSDAnnotation annotation) {
		// Do nothing
	}
	
	@Override
	public SSDAnnotation getAnnotation(String name) {
		// Just return null
		return null;
	}
	
	@Override
	public SSDAnnotation[] getAnnotations() {
		// Just return null
		return null;
	}
	
	@Override
	public SSDAnnotation[] getAnnotations(String name) {
		// Just return null
		return null;
	}
	
	@Override
	public SSDAnnotation copy() {
		// Copy properly all the annotation's objects
		Map<String, SSDNode> copyObj = new LinkedHashMap<>();
		for(Entry<String, SSDNode> e : objects().entrySet()) {
			String  name 	 = e.getKey();
			SSDNode node 	 = e.getValue();
			SSDNode copyNode = copyNode(node);
			if((copyNode != null))
				copyObj.put(name, copyNode);
		}
		return new SSDAnnotation(getName(), copyObj);
	}
	
	@Override
	public boolean equals(Object obj) {
		if((obj == null
				|| !(obj instanceof SSDAnnotation)))
			return false;
		// Use the SSDCollection equals check
		return super.equals(obj);
	}
	
	String toString(int depth, boolean compress, boolean json, boolean invoke,
			boolean info, boolean comments) {
		if(json) return ""; // Annotations don't exist in JSON
		StringBuilder buffer = new StringBuilder();
		// Append all comments
		if((comments && !json)) {
			SSDComment[] cmts;
			if((cmts = getComments()).length > 0) {
				boolean cmtf = true;
				for(SSDComment cmt : cmts) {
					if(cmtf) cmtf = false; else
					if(!compress) buffer.append(CHAR_NEWLINE);
					buffer.append(cmt.toString(depth, compress));
				}
				if(!compress) {
					buffer.append(CHAR_NEWLINE);
				}
			}
		}
		buffer.append(CHAR_ANNOTATION_SIGN);
		buffer.append(getName());
		Collection<SSDNode> nodes;
		if(!(nodes = objects().values()).isEmpty()) {
			buffer.append(CHAR_ANNOTATION_OB);
			boolean first = true;
			for(SSDNode node : nodes) {
				if((first)) {
					first = false;
				} else {
					buffer.append(CHAR_ANNOTATION_ITEM_DELIMITER);
					if(!compress)
						buffer.append(CHAR_SPACE);
				}
				// Append all node's annotations
				if(!json && node.isObject()) {
					SSDAnnotation[] anns;
					if((anns = node.getAnnotations()).length > 0) {
						boolean annf = true;
						for(SSDAnnotation ann : anns) {
							if((annf)) {
								annf = false;
							} else {
								if((ann.objects().isEmpty()) || !compress)
									buffer.append(CHAR_SPACE);
							}
							buffer.append(json ? ann.toJSON(depth, compress, invoke)
							                   : ann.toString(depth, compress, invoke, comments));
						}
					}
				}
				String nodeName;
				if(!(nodeName = node.getName())
							.equals(WORD_ANNOTATION_DEFAULT)
						|| nodes.size() > 1) {
					buffer.append(nodeName);
					if(!compress) buffer.append(CHAR_SPACE);
					buffer.append(CHAR_ANNOTATION_NV_DELIMITER);
					if(!compress) buffer.append(CHAR_SPACE);
				}
				// No need to check for JSON
				buffer.append(node.toString(depth+1, compress, invoke, comments));
			}
			buffer.append(CHAR_ANNOTATION_CB);
		} else if(compress) buffer.append(CHAR_SPACE);
		return buffer.toString();
	}
	
	@Override
	public String toString() {
		return toString(0, false, false, false, true, true);
	}
	
	@Override
	public String toString(boolean compress) {
		return toString(0, compress, false, false, true, true);
	}
	
	@Override
	public String toString(boolean compress, boolean invoke) {
		return toString(0, compress, false, invoke, true, true);
	}
	
	@Override
	public String toString(boolean compress, boolean invoke, boolean comments) {
		return toString(0, compress, false, invoke, true, comments);
	}
	
	@Override
	public String toString(int depth, boolean compress, boolean invoke) {
		return toString(depth, compress, false, invoke, true, true);
	}
	
	@Override
	public String toString(int depth, boolean compress, boolean invoke, boolean comments) {
		return toString(depth, compress, false, invoke, true, comments);
	}
	
	@Override
	public String toJSON() {
		return toString(0, false, true, false, false, false);
	}
	
	@Override
	public String toJSON(boolean compress) {
		return toString(0, compress, true, false, false, false);
	}
	
	@Override
	public String toJSON(boolean compress, boolean invoke) {
		return toString(0, compress, true, invoke, false, false);
	}
	
	@Override
	public String toJSON(int depth, boolean compress, boolean invoke) {
		return toString(depth, compress, true, invoke, false, false);
	}
}