package sune.util.ssdf;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

public final class SSDF {
	
	private static final char CHAR_OBJECT_OB 	  = '{';
	private static final char CHAR_OBJECT_CB 	  = '}';
	private static final char CHAR_ARRAY_OB 	  = '[';
	private static final char CHAR_ARRAY_CB 	  = ']';
	private static final char CHAR_NV_DELIMITER	  = ':';
	private static final char CHAR_ITEM_DELIMITER = ',';
	private static final char CHAR_DOUBLE_QUOTES  = '"';
	private static final char CHAR_SINGLE_QUOTES  = '\'';
	private static final char CHAR_ESCAPE		  = '\\';
	
	// Forbid anyone to create an instance of this class
	private SSDF() {
	}
	
	static final char[] formatContent(char[] chars) {
		if(chars == null) {
			throw new IllegalArgumentException(
				"Cannot format null array of characters!");
		}
		if(chars.length == 0) return chars;
		// In double quotes
		boolean indq 	= false;
		// In single quotes
		boolean insq 	= false;
		// Determining if a character can be added
		boolean cadd 	= false;
		// Determining if a character is escaped
		boolean escaped = false;
		// Current escape strength
		int	escape		= 0;
		StringBuilder sb = new StringBuilder();
		for(int i = 0, l = chars.length, c; i < l; ++i) {
			cadd = true;
			c 	 = chars[i];
			// Escape logic
			if(escaped && --escape == 0) 	 escaped = false;
			if(c == CHAR_ESCAPE && !escaped) escaped = (escape = 2) > 0; else
			// Quotes logic
			if(c == CHAR_DOUBLE_QUOTES && !insq && !escaped) indq = !indq; else
			if(c == CHAR_SINGLE_QUOTES && !indq && !escaped) insq = !insq; else
			// Formatting logic
			if(!indq && !insq && Character.isWhitespace(c))
				cadd = false;
			if(cadd) sb.append((char) c);
		}
		int    length = sb.length();
		char[] nchars = new char[length];
		sb.getChars(0, length, nchars, 0);
		return nchars;
	}
	
	static final Map<String, SSDNode> readObjects(char[] chars, int off, int len,
			SSDNode parent, boolean array) {
		if(chars == null) {
			throw new IllegalArgumentException(
				"Cannot read null array of characters!");
		}
		// Map where all the objects are stored
		Map<String, SSDNode> map = new LinkedHashMap<>();
		// Do some checking before the actual reading
		int length = chars.length;
		if(length == 0 || off < 0 || off >= length || len < 0 || off+len >= length)
			return map;
		// In double quotes
		boolean indq = false;
		// In single quotes
		boolean insq = false;
		// Temporary objects for names and values
		StringBuilder temp = new StringBuilder();
		String tempName    = null;
		// Counter for array objects
		int counter 	   = 0;
		// Determining if a character can be added
		boolean cadd 	   = false;
		// Determining if a character is escaped
		boolean escaped	   = false;
		// Current escape strength
		int	escape		   = 0;
		for(int i = off, l = off + len, c; i < l; ++i) {
			cadd = true;
			c 	 = chars[i];
			// Escape logic
			if(escaped && --escape == 0) 	 escaped = false;
			if(c == CHAR_ESCAPE && !escaped) escaped = (escape = 2) > 0; else
			// Quotes logic
			if(c == CHAR_DOUBLE_QUOTES && !insq && !escaped) indq = !indq; else
			if(c == CHAR_SINGLE_QUOTES && !indq && !escaped) insq = !insq;
			// Reading logic
			else {
				// Not in quotes
				if(!indq && !insq) {
					cadd = false;
					if(c == CHAR_OBJECT_OB || c == CHAR_ARRAY_OB) {
						boolean isarr = c == CHAR_ARRAY_OB;
						boolean dq 	  = false;
						boolean sq	  = false;
						int f 		  = 0;
						int b		  = 1;
						// Get content length of the object/array
						// NOTE: This should be avoided if the desired functionality
						// is to work in online mode, that means in mode where there
						// is no looking forward or backward.
						for(int k = i+1, h; k < l; ++k) {
							h = chars[k];
							// Quotes logic
							if(h == CHAR_DOUBLE_QUOTES && !sq) dq = !dq; else
							if(h == CHAR_SINGLE_QUOTES && !dq) sq = !sq; else
							// Reading logic
							if(!sq && !dq) {
								if(isarr) {
									if(h == CHAR_ARRAY_OB) ++b; else
									if(h == CHAR_ARRAY_CB && --b == 0) {
										f = k - i - 1; break;
									}
								} else {
									if(h == CHAR_OBJECT_OB) ++b; else
									if(h == CHAR_OBJECT_CB && --b == 0) {
										f = k - i - 1; break;
									}
								}
							}
						}
						SSDCollection arr = new SSDCollection(parent, tempName, isarr);
						arr.addObjects(readObjects(chars, i+1, f, arr, isarr));
						map.put(tempName, arr);
						tempName = null;
						i 		+= f+2;
					} else if(c == CHAR_NV_DELIMITER) {
						tempName = temp.toString();
						temp.setLength(0);
					} else if(c == CHAR_ITEM_DELIMITER) {
						if(array) tempName = Integer.toString(counter++);
						if(tempName != null) {
							map.put(tempName, new SSDObject(parent, tempName, temp.toString()));
							temp.setLength(0);
							tempName = null;
						}
					} else cadd = true;
				}
			}
			// Add the current character if it can be added
			if(cadd) temp.append((char) c);
			// Special condition for the last character, so that also the last item will be adedd!
			if(i == l-1 && !insq && !indq) {
				if(array) tempName = Integer.toString(counter++);
				if(tempName != null) {
					map.put(tempName, new SSDObject(parent, tempName, temp.toString()));
					temp.setLength(0);
					tempName = null;
				}
			}
		}
		return map;
	}
	
	static final Charset CHARSET = Charset.forName("UTF-8");
	static final String streamToString(InputStream stream) {
		if(stream == null) {
			throw new IllegalArgumentException(
				"Stream cannot be null!");
		}
		try(ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
			try(BufferedInputStream bis = new BufferedInputStream(stream)) {
				int    read	  = 0;
				byte[] buffer = new byte[8192];
				while((read = bis.read(buffer)) != -1)
					baos.write(buffer, 0, read);
			}
			return new String(baos.toByteArray(), CHARSET);
		} catch(Exception ex) {
			throw new IllegalStateException(
				"Cannot convert the given stream to a string!");
		}
	}
	
	static final InputStream resourceStream(String path) {
		return SSDF.class.getResourceAsStream(path);
	}
	
	public static final SSDCollection empty() {
		return SSDCollection.empty();
	}
	
	public static final SSDCollection read(String content) {
		if(content == null) {
			throw new IllegalArgumentException(
				"Content cannot be null!");
		}
		char[] chars = formatContent(content.toCharArray());
		return new SSDCollection(null, "", false,
			readObjects(chars, 1, chars.length-2, null, false));
	}
	
	public static final SSDCollection read(InputStream stream) {
		return read(streamToString(stream));
	}
	
	public static final SSDCollection read(File file) {
		if(file == null) {
			throw new IllegalArgumentException(
				"File cannot be null!");
		}
		String path;
		if((path = file.getPath()).startsWith("res:")) {
			// Read a resource instead of a file
			return read(resourceStream(path.substring(4).replace('\\', '/')));
		}
		try {
			return read(streamToString(new FileInputStream(file)));
		} catch(Exception ex) {
			throw new IllegalStateException(
				"An error has occurred while trying to read the given file!");
		}
	}
	
	static final SSDCollection convertJSONNames(SSDCollection sc) {
		String cname 	 = sc.getName();
		SSDCollection nc = cname.isEmpty() ?
			SSDCollection.empty() :
			new SSDCollection(
				sc.getParent(),
				cname.substring(1, cname.length()-1),
				sc.isArray()
			);
		for(Entry<String, SSDNode> e : sc.objects().entrySet()) {
			String name   = e.getKey();
			SSDNode value = e.getValue();
			String fname  = name.substring(1, name.length()-1);
			if(value instanceof SSDCollection) {
				SSDCollection vc = (SSDCollection) value;
				SSDCollection kc = vc.isArray() ?
					new SSDCollection(vc.getParent(), fname, true, vc.objects()) :
					convertJSONNames(vc);
				nc.addObject(fname, kc);
			} else if(value instanceof SSDObject) {
				SSDObject oo = (SSDObject) value;
				SSDObject no = new SSDObject(oo.getParent(), fname, oo.getType(),
					oo.getValue(), oo.getFormattedValue());
				nc.addObject(fname, no);
			}
		}
		return nc;
	}
	
	public static final SSDCollection readJSON(String json) {
		return convertJSONNames(read(json));
	}
}