package sune.util.ssdf2;

import static sune.util.ssdf2.SSDF.CHAR_DOUBLE_QUOTES;
import static sune.util.ssdf2.SSDF.CHAR_ESCAPE;
import static sune.util.ssdf2.SSDF.CHAR_SINGLE_QUOTES;
import static sune.util.ssdf2.SSDF.CHAR_VARIABLE_CONCAT;
import static sune.util.ssdf2.SSDF.CHAR_VARIABLE_SIGN;
import static sune.util.ssdf2.SSDF.WORD_FALSE;
import static sune.util.ssdf2.SSDF.WORD_NULL;
import static sune.util.ssdf2.SSDF.WORD_TRUE;

import java.util.regex.Pattern;

import sune.util.ssdf2.func.FuncUtils;

public enum SSDType {
	
	NULL("^("+WORD_NULL+")$") {
		
		@Override
		String fixValue(String value) {
			return WORD_NULL;
		}
	},
	BOOLEAN("^("+WORD_TRUE+"|"+WORD_FALSE+")$") {
		
		@Override
		String fixValue(String value) {
			return WORD_FALSE;
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
	STRING("^["+CHAR_DOUBLE_QUOTES+"|"+CHAR_SINGLE_QUOTES+"](.*?)" +
			"["+CHAR_DOUBLE_QUOTES+"|"+CHAR_SINGLE_QUOTES+"]$") {
		
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
				if(!escaped && c == CHAR_ESCAPE) escaped = true;
				else 					  		 escaped = false;
				if(!escaped) sb.append((char) c);
			}
			return super.format(sb.toString());
		}
		
		@Override
		String fixValue(String value) {
			value = value.trim();
			// Fix left quote
			if(value.indexOf(CHAR_DOUBLE_QUOTES) != 0 &&
			   value.indexOf(CHAR_SINGLE_QUOTES) != 0) {
				value = CHAR_DOUBLE_QUOTES + value;
			}
			// Fix right quote
			int len1 = value.length()-1;
			if(value.indexOf(CHAR_DOUBLE_QUOTES) != len1 &&
			   value.indexOf(CHAR_SINGLE_QUOTES) != len1) {
				value = value + CHAR_DOUBLE_QUOTES;
			}
			// Escape all the inner characters
			StringBuilder sb = new StringBuilder();
			char[] chars	 = value.toCharArray();
			for(int i = 0, l = chars.length, c; i < l; ++i) {
				c = chars[i];
				if(c == CHAR_DOUBLE_QUOTES && i != 0 && i != l-1) {
					sb.append(CHAR_ESCAPE);
					sb.append(CHAR_DOUBLE_QUOTES);
				} else sb.append((char) c);
			}
			return sb.toString();
		}
	},
	STRING_VAR(null) {
		// No formatting or fixing
	},
	UNKNOWN(null);
	
	private final String regex;
	private SSDType(String regex) {
		this.regex = regex;
	}
	
	// Type recognition using RegExp
	public static final SSDType recognize(String value) {
		boolean tpstr = false;
		String  regex;
		for(SSDType type : values()) {
			if((regex = type.regex) != null
					&& Pattern.matches(regex, value)) {
				if((type == STRING)) {
					tpstr = true;
					break;
				}
				return type;
			}
		}
		// Quotes
		boolean indq = false;
		boolean insq = false;
		// Escaping
		boolean escaped = false;
		int 	escape  = 0;
		// Miscellaneous
		boolean var = false;
		boolean was = false;
		boolean con = false;
		// Check whether the value contains a variable
		char[] chars = value.toCharArray();
		for(int i = 0, l = chars.length, c; i < l; ++i) {
			c = chars[i];
			// Escape logic
			if(escaped && --escape == 0) 	 escaped = false;
			if(c == CHAR_ESCAPE && !escaped) escaped = (escape = 2) > 0; else
			// Quotes logic
			if(c == CHAR_DOUBLE_QUOTES && !insq && !escaped) indq = !indq; else
			if(c == CHAR_SINGLE_QUOTES && !indq && !escaped) insq = !insq; else
			// Checking logic
			if(!indq && !insq) {
				if((var)) {
					// Check if variable name is present and valid?
					if((Character.isLetterOrDigit(c))
							|| c == '_'
							|| c == '.') {
						// Do nothing?
					} else if((c == CHAR_VARIABLE_CONCAT)) {
						con = true;
						var = false;
					} else {
						var = false;
					}
				} else {
					if((c == CHAR_VARIABLE_CONCAT)) {
						con = true;
						was = true;
					}
					if((c == CHAR_VARIABLE_SIGN)) {
						var = true;
						was = true;
						con = false;
					}
				}
			}
			// Additional logic
			if((indq || insq || Character.isDigit(c)))
				con = false;
		}
		return was ? (con ? UNKNOWN : STRING_VAR) : (tpstr ? STRING : UNKNOWN);
	}
	
	// Type recognition using classes
	public static final SSDType recognize(Object value) {
		if((value != null)) {
			Class<?> clazz = FuncUtils.toPrimitive(value.getClass());
			if((clazz == boolean.class))
				return BOOLEAN;
	        if((clazz == byte.class
	        		|| clazz == char.class
	        		|| clazz == short.class
	        		|| clazz == int.class
	        		|| clazz == long.class))
	        	return INTEGER;
	        if((clazz == float.class
	        		|| clazz == double.class))
	        	return DECIMAL;
	        if((clazz == String.class))
	        	return STRING;
	        // Other classes are stated as unknown
	        return UNKNOWN;
		}
		return NULL;
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