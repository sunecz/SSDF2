package sune.util.ssdf2;

import static sune.util.ssdf2.SSDF.CHAR_DOUBLE_QUOTES;
import static sune.util.ssdf2.SSDF.CHAR_ESCAPE;
import static sune.util.ssdf2.SSDF.CHAR_NAME_DELIMITER;
import static sune.util.ssdf2.SSDF.CHAR_SINGLE_QUOTES;
import static sune.util.ssdf2.SSDF.CHAR_SPACE;
import static sune.util.ssdf2.SSDF.CHAR_VARIABLE_CONCAT;
import static sune.util.ssdf2.SSDF.CHAR_VARIABLE_DELIMITER;
import static sune.util.ssdf2.SSDF.CHAR_VARIABLE_SIGN;
import static sune.util.ssdf2.SSDF.WORD_NULL;
import static sune.util.ssdf2.SSDF.WORD_VARIABLE_MAIN;
import static sune.util.ssdf2.SSDF.WORD_VARIABLE_THIS;
import static sune.util.ssdf2.SSDF.WORD_VARIABLE_VALUE;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.regex.Pattern;

public class SSDObject implements SSDNode {
	
	// Protected properties
	protected final SSDProperty<SSDNode> parent;
	protected final SSDProperty<String>  name;
	
	// Private properties
	private final SSDType  type;
	private final SSDValue value;
	// Formatted value
	private final SSDValue fvalue;
	
	// Annotations
	private final Set<SSDAnnotation> annotations;
	
	// Comments
	private final Set<SSDComment> comments;
	
	SSDObject(SSDNode parent, String name, String value) {
		SSDType type  = SSDType.recognize(value);
		SSDValue val  = new SSDValue(compress(value));
		SSDValue fval = type.format(value);
		checkArgs(name, type, fval);
		this.parent = new SSDProperty<>(parent);
		this.name 	= new SSDProperty<>(name);
		this.type 	= type;
		this.value	= val;
		this.fvalue = fval;
		// Annotations
		this.annotations = new LinkedHashSet<>();
		// Comments
		this.comments = new LinkedHashSet<>();
	}
	
	SSDObject(SSDNode parent, String name, SSDType type, SSDValue value, SSDValue fvalue) {
		checkArgs(name, type, value);
		this.parent = new SSDProperty<>(parent);
		this.name 	= new SSDProperty<>(name);
		this.type 	= type;
		this.value 	= value;
		this.fvalue = fvalue;
		// Annotations
		this.annotations = new LinkedHashSet<>();
		// Comments
		this.comments = new LinkedHashSet<>();
	}
	
	static final void checkArgs(String name, SSDType type, SSDValue value) {
		if(name == null) {
			throw new IllegalArgumentException(
				"Name of an object cannot be null!");
		}
		if(type == null) {
			throw new IllegalArgumentException(
				"Type of an object cannot be null!");
		}
		if(value == null) {
			throw new IllegalArgumentException(
				"Value of an object cannot be null!");
		}
	}
	
	static final Random RANDOM;
	static final String RANDOM_CHARS;
	static {
		RANDOM		 = new Random();
		RANDOM_CHARS = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz_";
	}
	
	static final String genRandomName(int length) {
		StringBuilder sb = new StringBuilder();
		for(int i = length, l = RANDOM_CHARS.length(); --i >= 0;)
			sb.append(RANDOM_CHARS.charAt(RANDOM.nextInt(l)));
		return sb.toString();
	}
	
	static final String compress(String value) {
		return new String(SSDF.format(value.toCharArray()));
	}
	
	public static SSDObject of(Object value) {
		return of(genRandomName(16), value);
	}
	
	public static SSDObject of(String name, Object value) {
		return new SSDObject(null, name, value != null
											? value instanceof String
												? CHAR_DOUBLE_QUOTES
													+ (String) value
													+ CHAR_DOUBLE_QUOTES
												: value.toString()
											: WORD_NULL);
	}
	
	public static SSDObject ofRaw(String value) {
		return ofRaw(genRandomName(16), value);
	}
	
	public static SSDObject ofRaw(String name, String value) {
		return new SSDObject(null, name, value);
	}
	
	void addAnnotations0(Set<SSDAnnotation> anns) {
		if((anns != null)) {
			for(SSDAnnotation a : anns) {
				a.parent.set(this);
				annotations.add(a);
			}
		}
	}
	
	void addAnnotation0(SSDAnnotation ann) {
		if((ann != null)) {
			ann.parent.set(this);
			annotations.add(ann);
		}
	}
	
	// Annotations
	Set<SSDAnnotation> annotations() {
		return annotations;
	}
	
	@Override
	public SSDNode getParent() {
		return parent.get();
	}
	
	@Override
	public String getName() {
		return name.get();
	}
	
	@Override
	public String getFullName() {
		SSDNode p = getParent();
		return (p == null || p.getName() == null
					? ""
					: p.getFullName() + CHAR_NAME_DELIMITER)
				+ getName();
	}
	
	public SSDType getType() {
		return type;
	}
	
	public SSDValue getValue() {
		return value;
	}
	
	public SSDValue getFormattedValue() {
		return fvalue;
	}
	
	public boolean booleanValue() { return fvalue.booleanValue(); }
	public byte byteValue()       { return fvalue.byteValue();    }
	public short shortValue()     { return fvalue.shortValue();   }
	public int intValue()         { return fvalue.intValue();     }
	public long longValue()       { return fvalue.longValue();    }
	public float floatValue()     { return fvalue.floatValue();   }
	public double doubleValue()   { return fvalue.doubleValue();  }
	public String stringValue()   { return fvalue.stringValue();  }
	public Object value()         { return fvalue.value();        }
	
	@Override
	public void addAnnotation(SSDAnnotation annotation) {
		// Call the internal method
		addAnnotation0(annotation);
	}
	
	@Override
	public boolean hasAnnotation(String name) {
		return getAnnotation(name) != null;
	}
	
	@Override
	public void removeAnnotation(String name) {
		for(Iterator<SSDAnnotation> i = annotations.iterator();
				i.hasNext();) {
			SSDAnnotation ann = i.next();
			if(ann.getName()	.equals(name) ||
			   ann.getFullName().equals(name))
				i.remove();
		}
	}
	
	@Override
	public void removeAnnotation(SSDAnnotation annotation) {
		// Just remove the annotation
		annotations.remove(annotation);
	}
	
	void removeAnnotationEq(SSDAnnotation annotation) {
		for(SSDAnnotation ann : annotations) {
			if((ann.equals(annotation))) {
				removeAnnotation(ann);
				return; // Should be only one
			}
		}
	}
	
	@Override
	public SSDAnnotation getAnnotation(String name) {
		for(SSDAnnotation ann : annotations)
			if(ann.getName().equals(name))
				return ann;
		return null;
	}
	
	@Override
	public SSDAnnotation[] getAnnotations() {
		return annotations.toArray(new SSDAnnotation[annotations.size()]);
	}
	
	@Override
	public SSDAnnotation[] getAnnotations(String name) {
		List<SSDAnnotation> list = new ArrayList<>();
		for(SSDAnnotation ann : annotations)
			if(ann.getName().equals(name))
				list.add(ann);
		return list.toArray(new SSDAnnotation[list.size()]);
	}
	
	@Override
	public void addComment(SSDComment comment) {
		comments.add(comment);
	}
	
	@Override
	public void removeComment(SSDComment comment) {
		comments.remove(comment);
	}
	
	@Override
	public SSDComment[] getComments() {
		return comments.toArray(new SSDComment[comments.size()]);
	}
	
	@Override
	public SSDObject copy() {
		return new SSDObject(getParent(),
		                     getName(),
		                     getType(),
		                     new SSDValue(value .value()),
		                     new SSDValue(fvalue.value()));
	}
	
	@Override
	public boolean equals(Object obj) {
		if((obj == null
				|| !(obj instanceof SSDObject)))
			return false;
		SSDObject o = (SSDObject) obj;
		if(!o.name.get().equals(name.get()))
			return false;
		if((o.type != type))
			return false;
		if(!o.value .value().equals(value .value()))
			return false;
		if(!o.fvalue.value().equals(fvalue.value()))
			return false;
		// All good, both objects contains the same stuff
		return true;
	}
	
	@Override
	public boolean isObject() {
		return true;
	}
	
	@Override
	public boolean isCollection() {
		return false;
	}
	
	SSDCollection getRoot() {
		SSDNode p = this, n;
		do {
			n = p;
			p = n.getParent();
		} while(p != null);
		return (SSDCollection) n;
	}
	
	SSDNode getNonNullParent(SSDNode p, int d) {
		SSDNode n;
		do {
			n = p;
			p = n.getParent();
		} while(p != null && d-- >= 0);
		return n;
	}
	
	static final Pattern PATTERN_NUMBER = Pattern.compile("\\d+");
	SSDNode getNodeByVarName(String name, boolean compress) {
		String   _root  = WORD_VARIABLE_THIS;
		int      _depth = 0;
		String[] split  = name.split("\\" + CHAR_VARIABLE_DELIMITER);
		int    length = split.length, index = 0;
		String first  = split[0];
		if((first.equals(WORD_VARIABLE_THIS) ||
			first.equals(WORD_VARIABLE_MAIN))) {
			_root = first;
			index = 1;
		}
		if((length > 2)) {
			String second = split[index];
			// Only numbers can be parsed
			if((PATTERN_NUMBER.matcher(second).matches())) {
				_depth = Integer.parseInt(second);
				index  = 2;
			}
		}
		StringBuilder sb = new StringBuilder();
		boolean		  pf = true;
		for(; index < length; ++index) {
			if((pf)) {
				pf = false;
			} else {
				sb.append(CHAR_VARIABLE_DELIMITER);
			}
			sb.append(split[index]);
		}
		boolean main = false;
		SSDNode node = null;
		switch(_root) {
			case WORD_VARIABLE_THIS: node = this; 	                break;
			case WORD_VARIABLE_MAIN: node = getRoot(); main = true; break;
			default:
				// Just do nothing
				break;
		}
		if((node != null)) {
			String  path = sb.toString();
			SSDNode epar = getNonNullParent(node, _depth-1);
			SSDNode npar = main ? epar : epar.getParent();
			if((npar != null)) {
				if((epar instanceof SSDAnnotation
						&& path.equals(WORD_VARIABLE_VALUE))) {
					// Do not return the original node, make a copy
					npar = npar.copy();
					// Remove the annotation from the parent since this
					// would cause recursion
					SSDAnnotation ann = (SSDAnnotation) epar;
					// Collections and Annotations
					if((npar instanceof SSDCollection)) {
						((SSDCollection) npar).removeAnnotationEq(ann);
					}
					// Objects and Function calls
					else if((npar instanceof SSDObject)) {
						((SSDObject) npar).removeAnnotationEq(ann);
					}
					// Return the edited parent
					return npar;
				}
				if((npar instanceof SSDCollection)) {
					SSDCollection parent = (SSDCollection) npar;
					// Do not return the original node, make a copy
					return parent.get(path).copy();
				}
			}
		}
		return null;
	}
	
	static final String substr(String str, int start, int end) {
		if((str == null)) return null;
		if((start < 0 || (end >= 0 && end < start)))
			return str;
		if((end < 0))
			end += str.length();
		return str.substring(start, end);
	}
	
	static final String fixValue(SSDNode node, String value) {
		if((node.isCollection())) return value;
		SSDObject object = (SSDObject) node;
		if((object.getType() == SSDType.STRING)) {
			int start = 0;
			int end   = value.length();
			int last  = value.length() - 1;
			if((value.indexOf(CHAR_DOUBLE_QUOTES) == 0 ||
				value.indexOf(CHAR_SINGLE_QUOTES) == 0))
				start = 1;
			if((value.lastIndexOf(CHAR_DOUBLE_QUOTES) == last ||
				value.lastIndexOf(CHAR_SINGLE_QUOTES) == last))
				end = -1;
			value = substr(value, start, end);
		}
		return value;
	}
	
	String toString(int depth, boolean compress, boolean json, boolean invoke,
			boolean info, boolean comments) {
		if((value == null)) return WORD_NULL;
		String sval = value.toString();
		if(invoke && getType() == SSDType.STRING_VAR) {
			StringBuilder sb = new StringBuilder();
			StringBuilder tm = new StringBuilder();
			StringBuilder vr = new StringBuilder();
			// Quotes
			boolean indq = false;
			boolean insq = false;
			// Escaping
			boolean escaped = false;
			int 	escape  = 0;
			// Miscellaneous
			boolean var = false;
			boolean add = false;
			// If true, concatenation was present
			boolean cnt = false;
			// Replace variables with actual values
			char[] chars = sval.toCharArray();
			for(int i = 0, l = chars.length, c; i < l; ++i) {
				add = true;
				c   = chars[i];
				// Escape logic
				if(escaped && --escape == 0) 	 escaped = false;
				if(c == CHAR_ESCAPE && !escaped) escaped = (escape = 2) > 0; else
				// Quotes logic
				if(c == CHAR_DOUBLE_QUOTES && !insq && !escaped) { indq = !indq; add = false; } else
				if(c == CHAR_SINGLE_QUOTES && !indq && !escaped) { insq = !insq; add = false; } else
				// Checking logic
				if(!indq && !insq) {
					if((var)) {
						// Check if variable name is present and valid
						if((Character.isLetterOrDigit(c))
								|| c == '_'
								|| c == '.') {
							tm.append((char) c);
							add = false;
						} else {
							String  name = tm.toString();
							SSDNode vval = getNodeByVarName(name, compress);
							tm.setLength(0);
							vr.append(vval != null
										? fixValue(vval, vval.toString(depth, compress, invoke, comments))
							            : WORD_NULL);
							var = false;
						}
					}
					if((c == CHAR_VARIABLE_SIGN)) {
						var = true;
						add = false;
					} else if((c == CHAR_VARIABLE_CONCAT)) {
						sb.append(vr.toString());
						vr.setLength(0);
						add = false;
						cnt = true;
					}
				}
				if(add) sb.append((char) c);
			}
			if((tm.length() > 0)) {
				String  name = tm.toString();
				SSDNode vval = getNodeByVarName(name, compress);
				tm.setLength(0);
				vr.append(vval != null
							? fixValue(vval, vval.toString(depth, compress, invoke, comments))
				            : WORD_NULL);
			}
			if((vr.length() > 0)) {
				sb.append(vr.toString());
				vr.setLength(0);
			}
			sval = sb.toString();
			if((cnt)) {
				sval = CHAR_DOUBLE_QUOTES + sval + CHAR_DOUBLE_QUOTES;
			}
			return sval;
		}
		if(!compress) {
			StringBuilder sb = new StringBuilder();
			// Quotes
			boolean indq = false;
			boolean insq = false;
			// Escaping
			boolean escaped = false;
			int 	escape  = 0;
			// Miscellaneous
			boolean add = false;
			boolean con = false;
			boolean spc = false;
			// Beautify the object's content
			char[] chars = sval.toCharArray();
			for(int i = 0, l = chars.length, c; i < l; ++i) {
				add = true;
				c   = chars[i];
				// Escape logic
				if(escaped && --escape == 0) 	 escaped = false;
				if(c == CHAR_ESCAPE && !escaped) escaped = (escape = 2) > 0; else
				// Quotes logic
				if(c == CHAR_DOUBLE_QUOTES && !insq && !escaped) indq = !indq; else
				if(c == CHAR_SINGLE_QUOTES && !indq && !escaped) insq = !insq; else
				// Checking logic
				if(!indq && !insq) {
					if((c == CHAR_SPACE)) {
						if((spc))
							add = false;
						spc = true;
					} else if((c == CHAR_VARIABLE_CONCAT)) {
						if(!spc)
							sb.append(CHAR_SPACE);
						con = true;
						spc = false;
					}
				} else {
					spc = false;
					con = false;
				}
				if(add) sb.append((char) c);
				if((con && !spc)) {
					sb.append(CHAR_SPACE);
					con = false;
					spc = true;
				}
			}
			sval = sb.toString();
		}
		if(json && (type == SSDType.STRING_VAR ||
					type == SSDType.UNKNOWN)) {
			sval = sval.replaceAll("\"", "\\\\\"");
			sval = CHAR_DOUBLE_QUOTES + sval + CHAR_DOUBLE_QUOTES;
		}
		return sval;
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