package sune.util.ssdf;

import java.util.regex.Pattern;

public enum SSDType {
	
	NULL("^(null)$") {
		
		@Override
		String fixValue(String value) {
			return "null";
		}
	},
	BOOLEAN("^(true|false)$") {
		
		@Override
		String fixValue(String value) {
			return "false";
		}
	},
	INTEGER("^(\\+|-)?(\\d+)$") {
		
		@Override
		String fixValue(String value) {
			StringBuilder sb = new StringBuilder();
			char[] chars 	 = value.toCharArray();
			if(value.indexOf('-') > -1) sb.append('-');
			for(int i = 0, l = chars.length, c; i < l; ++i) {
				c = chars[i];
				if(Character.digit(c, 10) != -1)
					sb.append((char) c);
			}
			return sb.toString();
		}
	},
	DECIMAL("^(\\+|-)?(\\d+\\.(?:\\d+)?|\\.\\d+)$") {
		@Override
		String fixValue(String value) {
			StringBuilder sb = new StringBuilder();
			char[] chars 	 = value.toCharArray();
			boolean wasDot	 = false;
			if(value.indexOf('-') > -1) sb.append('-');
			for(int i = 0, l = chars.length, c; i < l; ++i) {
				c = chars[i];
				if(c == '.' && !wasDot) {
					sb.append('.');
					wasDot = true;
				} else if(Character.digit(c, 10) != -1)
					sb.append((char) c);
			}
			return sb.toString();
		}
	},
	STRING("^[\"|'](.*?)[\"|']$") {
		
		@Override
		public SSDValue format(String value) {
			if(value == null || value.length() < 2)
				return super.format(value);
			// Remove quotes at the beginning and at the end
			value = value.trim().substring(1, value.length()-1);
			// Unescape all the escaped characters
			StringBuilder sb = new StringBuilder();
			char[] chars	 = value.toCharArray();
			boolean escaped  = false;
			for(int i = 0, l = chars.length, c; i < l; ++i) {
				c = chars[i];
				if(!escaped && c == '\\') escaped = true;
				else 					  escaped = false;
				if(!escaped) sb.append((char) c);
			}
			return super.format(sb.toString());
		}
		
		@Override
		String fixValue(String value) {
			value = value.trim();
			// Fix left quote
			if(value.indexOf('"')  != 0 &&
			   value.indexOf('\'') != 0) {
				value = '"' + value;
			}
			// Fix right quote
			int len1 = value.length()-1;
			if(value.indexOf('"')  != len1 &&
			   value.indexOf('\'') != len1) {
				value = value + '"';
			}
			// Escape all the inner characters
			StringBuilder sb = new StringBuilder();
			char[] chars	 = value.toCharArray();
			for(int i = 0, l = chars.length, c; i < l; ++i) {
				c = chars[i];
				if(c == '"' && i != 0 && i != l-1) {
					sb.append('\\');
					sb.append('"');
				} else sb.append((char) c);
			}
			return sb.toString();
		}
	},
	UNKNOWN(null);
	
	private final String regex;
	private SSDType(String regex) {
		this.regex = regex;
	}
	
	public static final SSDType recognize(String value) {
		for(SSDType type : values()) {
			if(type == SSDType.UNKNOWN) continue;
			if(Pattern.matches(type.regex, value))
				return type;
		}
		return SSDType.UNKNOWN;
	}
	
	SSDObject createObject(SSDNode parent, String name, String value) {
		if(!Pattern.matches(regex, value)) value = fixValue(value);
		return new SSDObject(parent, name, this, new SSDValue(value), format(value));
	}
	
	String fixValue(String value) {
		return value;
	}
	
	public SSDValue format(String value) {
		return new SSDValue(value);
	}
}