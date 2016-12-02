package sune.util.ssdf2;

import static sune.util.ssdf2.SSDF.CHAR_ANNOTATION_CB;
import static sune.util.ssdf2.SSDF.CHAR_ANNOTATION_ITEM_DELIMITER;
import static sune.util.ssdf2.SSDF.CHAR_ANNOTATION_NV_DELIMITER;
import static sune.util.ssdf2.SSDF.CHAR_ANNOTATION_OB;
import static sune.util.ssdf2.SSDF.CHAR_ANNOTATION_SIGN;
import static sune.util.ssdf2.SSDF.CHAR_SPACE;
import static sune.util.ssdf2.SSDF.WORD_ANNOTATION_DEFAULT;

import java.util.Collection;
import java.util.Map;

public class SSDAnnotation extends SSDCollection {
	
	SSDAnnotation(String name, Map<String, SSDNode> data) {
		super(null, name, false, data, null);
	}
	
	@Override
	public String toString() {
		return toString(false);
	}
	
	public String toString(boolean compress) {
		StringBuilder buffer = new StringBuilder();
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
				String nodeName;
				if(!(nodeName = node.getName())
							.equals(WORD_ANNOTATION_DEFAULT)
						|| nodes.size() > 1) {
					buffer.append(nodeName);
					if(!compress) buffer.append(CHAR_SPACE);
					buffer.append(CHAR_ANNOTATION_NV_DELIMITER);
					if(!compress) buffer.append(CHAR_SPACE);
				}
				buffer.append(node.toString(compress));
			}
			buffer.append(CHAR_ANNOTATION_CB);
		} else if(compress) buffer.append(CHAR_SPACE);
		return buffer.toString();
	}
}