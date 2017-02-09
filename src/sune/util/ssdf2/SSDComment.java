package sune.util.ssdf2;

import static sune.util.ssdf2.SSDF.CHAR_COMMENT_FIRST;
import static sune.util.ssdf2.SSDF.CHAR_COMMENT_MULTIPLE_LINES;
import static sune.util.ssdf2.SSDF.CHAR_COMMENT_ONE_LINE;

public class SSDComment {
	
	private final String  content;
	private final boolean oneLine;
	
	SSDComment(String content, boolean oneLine) {
		this.content = content;
		this.oneLine = oneLine;
	}
	
	public static SSDComment of(String content) {
		if((content == null))
			throw new IllegalArgumentException(
				"Content of a comment cannot be null!");
		// Create new SSDComment with check of onelining
		return new SSDComment(content, isOneLine(content));
	}
	
	static final boolean isOneLine(String content) {
		// Regular check for new line characters
		return content.indexOf('\n') == -1 ||
			   content.indexOf('\r') == -1;
	}
	
	public String getContent() {
		return content;
	}
	
	public boolean isOneLine() {
		return oneLine;
	}
	
	String toString(int depth, boolean compress) {
		StringBuilder buffer = new StringBuilder();
		if(!compress) {
			String tabStr = SSDCollection.tabString(depth);
			buffer.append(tabStr);
		}
		buffer.append(CHAR_COMMENT_FIRST);
		if((oneLine && !compress)) {
			buffer.append(CHAR_COMMENT_ONE_LINE);
			buffer.append(content);
		} else {
			buffer.append(CHAR_COMMENT_MULTIPLE_LINES);
			buffer.append(content);
			buffer.append(CHAR_COMMENT_MULTIPLE_LINES);
			buffer.append(CHAR_COMMENT_FIRST);
		}
		return buffer.toString();
	}
	
	@Override
	public String toString() {
		return toString(0, false);
	}
}