package sune.util.ssdf2;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Deque;
import java.util.Set;

public final class SSDF {
	
	// General characters
	static final char CHAR_NEWLINE                   = '\n';
	static final char CHAR_TAB                       = '\t';
	static final char CHAR_SPACE                     = ' ';
	static final char CHAR_NAME_DELIMITER            = '.';
	static final char CHAR_ANNOTATION_DELIMITER      = ':';
	static final char CHAR_AND_DELIMITER             = '&';
	static final char CHAR_OR_DELIMITER              = '|';
	// General syntax
	static final char CHAR_OBJECT_OB                 = '{';
	static final char CHAR_OBJECT_CB                 = '}';
	static final char CHAR_ARRAY_OB                  = '[';
	static final char CHAR_ARRAY_CB                  = ']';
	static final char CHAR_NV_DELIMITER              = ':';
	static final char CHAR_ITEM_DELIMITER            = ',';
	static final char CHAR_DOUBLE_QUOTES             = '"';
	static final char CHAR_SINGLE_QUOTES             = '\'';
	static final char CHAR_ESCAPE                    = '\\';
	// Annotation syntax
	static final char CHAR_ANNOTATION_SIGN           = '@';
	static final char CHAR_ANNOTATION_OB             = '(';
	static final char CHAR_ANNOTATION_CB             = ')';
	static final char CHAR_ANNOTATION_NV_DELIMITER   = '=';
	static final char CHAR_ANNOTATION_ITEM_DELIMITER = ',';
	// Function call syntax
	static final char CHAR_FUNCCALL_OB               = '(';
	static final char CHAR_FUNCCALL_CB               = ')';
	static final char CHAR_FUNCCALL_ARGS_DELIMITER   = ',';
	// Variable syntax
	static final char CHAR_VARIABLE_SIGN             = '$';
	static final char CHAR_VARIABLE_DELIMITER        = '.';
	static final char CHAR_VARIABLE_CONCAT           = '+';
	// Comment syntax
	static final char CHAR_COMMENT_FIRST             = '/';
	static final char CHAR_COMMENT_ONE_LINE          = '/';
	static final char CHAR_COMMENT_MULTIPLE_LINES    = '*';
	// Syntax special words
	static final String WORD_NULL                    = "null";
	static final String WORD_TRUE                    = "true";
	static final String WORD_FALSE                   = "false";
	// Other special words
	static final String WORD_ANNOTATION_DEFAULT      = "value";
	static final String WORD_VARIABLE_VALUE          = "value";
	static final String WORD_VARIABLE_THIS           = "this";
	static final String WORD_VARIABLE_MAIN           = "main";
	
	// Forbid anyone to create an instance of this class
	private SSDF() {
	}
	
	static final char[] format(char[] chars) {
		if((chars == null))
			throw new IllegalArgumentException(
				"Cannot format null array of characters!");
		if((chars.length == 0)) return chars;
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
		// If annotation sign is encountered, true is added
		Deque<Boolean> sann = new ArrayDeque<>();
		// If annotation bracket and sign is encountered, true is added
		Deque<Boolean> iann = new ArrayDeque<>();
		// Whether the comment first character was encountered
		boolean cmtfirst   = false;
		boolean cmtcontent = false;
		boolean cmtoneline = false;
		StringBuilder sb = new StringBuilder();
		for(int i = 0, l = chars.length, c; i < l; ++i) {
			cadd = true;
			c 	 = chars[i];
			// Comments logic
			if((cmtcontent)) {
				// One line comment
				if((cmtoneline)) {
					if((c == CHAR_NEWLINE)) {
						cmtcontent = false;
						cmtfirst   = false;
					}
				}
				// Multiple line comment
				else {
					if((cmtfirst && c == CHAR_COMMENT_FIRST)) {
						cmtcontent = false;
						cmtfirst   = false;
					} else if((c == CHAR_COMMENT_MULTIPLE_LINES)) {
						cmtfirst = true;
					}
				}
			} else {
				// Escape logic
				if(escaped && --escape == 0) 	 escaped = false;
				if(c == CHAR_ESCAPE && !escaped) escaped = (escape = 2) > 0; else
				// Quotes logic
				if(c == CHAR_DOUBLE_QUOTES && !insq && !escaped) indq = !indq; else
				if(c == CHAR_SINGLE_QUOTES && !indq && !escaped) insq = !insq; else
				// Formatting logic
				if(!indq && !insq) {
					if((cmtfirst &&
							(c == CHAR_COMMENT_ONE_LINE ||
							 c == CHAR_COMMENT_MULTIPLE_LINES))) {
						cmtcontent = true;
						cmtoneline = c == CHAR_COMMENT_ONE_LINE;
						cmtfirst   = false;
					} else if((c == CHAR_COMMENT_FIRST)) {
						cmtfirst = true;
					} else if((c == CHAR_ANNOTATION_SIGN)) {
						sann.push(true);
					} else if((c == CHAR_ANNOTATION_OB
									&& !sann.isEmpty()
									&&  sann.peek())) {
						iann.push(true);
					} else if((c == CHAR_ANNOTATION_CB
									&& !iann.isEmpty()
									&&  iann.peek())) {
						if((iann.peek())) {
							iann.pop();
							if(!sann.isEmpty())
								sann.pop();
						}
					} else if((Character.isWhitespace(c))) {
						if((!sann.isEmpty()
								&& sann.peek()
								// The last annotation has no attributes
								&& sann.size() != iann.size())) {
							sann.pop();
							if((c != CHAR_SPACE))
								c  = CHAR_SPACE;
						} else cadd = false;
					}
				}
			}
			if(cadd) sb.append((char) c);
		}
		int    length = sb.length();
		char[] nchars = new char[length];
		sb.getChars(0, length, nchars, 0);
		return nchars;
	}
	
	static final SSDCollection readObjects(char[] chars, int off, int len) {
		if((chars == null))
			throw new IllegalArgumentException("Cannot read null array of characters");
		// Do some checking before the actual reading
		int length = chars.length;
		if((length == 0 || off < 0 || off >= length || len < 0 || off+len > length))
			return null;
		// In double quotes
		boolean indq 	   = false;
		// In single quotes
		boolean insq 	   = false;
		// Temporary objects for names and values
		StringBuilder temp = new StringBuilder();
		String tempName    = null;
		String lastName	   = null;
		String lobjName	   = null;
		// Counter for array objects
		int counter 	   = 0;
		// Determining if a character can be added
		boolean cadd 	   = false;
		// Determining if a character is escaped
		boolean escaped	   = false;
		// Current escape strength
		int	escape		   = 0;
		// Whether the annotation sign was encountered
		boolean bann 	   = false;
		// Whether the value should be defined now
		boolean isval 	   = false;
		// Annotations for current object/collection
		Deque<SSDAnnotation> anns = new ArrayDeque<>();
		// Holds all parents, the first element represents the current parent
		// which will be later saved into the 'parent' variable
		Deque<SSDNode> parents = new ArrayDeque<>();
		// Objects for handling information about parent
		SSDNode parent 	   = null;
		boolean array 	   = false;
		boolean function   = false;
		boolean isfsimple  = false;
		int		ctfsimple  = 0;
		Deque<Boolean> annsval = new ArrayDeque<>();
		Deque<Integer> annscnt = new ArrayDeque<>();
		annsval.push(false);
		annscnt.push(0);
		// Whether the comment first character was encountered
		boolean cmtfirst   = false;
		boolean cmtcontent = false;
		boolean cmtoneline = false;
		Deque<SSDComment> comments = new ArrayDeque<>();
		Deque<SSDAnnotation> annsh = new ArrayDeque<>();
		// The last content of item when entering a comment
		String lastContent = null;
		// Read the characters and construct the objects
		for(int i = off, l = off + len, c; i < l; ++i) {
			cadd = true;
			c 	 = chars[i];
			// Comments logic
			if((cmtcontent)) {
				// One line comment
				if((cmtoneline)) {
					if((c == CHAR_NEWLINE)) {
						cmtcontent = false;
						cmtfirst   = false;
						// Add comment to the collection
						String content = temp.substring(0, temp.length()-1)
											 .toString();
						comments.push(new SSDComment(content, cmtoneline));
						temp.setLength(0);
						if((lastContent != null
								&& !lastContent.isEmpty()))
							temp.append(lastContent);
						cmtoneline  = false;
						cadd 	    = false;
						lastContent = null;
					}
				}
				// Multiple line comment
				else {
					if((cmtfirst && c == CHAR_COMMENT_FIRST)) {
						cmtcontent = false;
						cmtfirst   = false;
						// Add comment to the collection
						String content = temp.substring(0, temp.length()-1)
											 .toString();
						comments.push(new SSDComment(content, cmtoneline));
						temp.setLength(0);
						if((lastContent != null
								&& !lastContent.isEmpty()))
							temp.append(lastContent);
						cmtoneline  = false;
						cadd 	    = false;
						lastContent = null;
					} else if((c == CHAR_COMMENT_MULTIPLE_LINES)) {
						cmtfirst = true;
					}
				}
			} else {
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
						if((cmtfirst &&
								(c == CHAR_COMMENT_ONE_LINE ||
								 c == CHAR_COMMENT_MULTIPLE_LINES))) {
							cmtcontent  = true;
							cmtoneline  = c == CHAR_COMMENT_ONE_LINE;
							cmtfirst    = false;
							lastContent = temp.toString();
							temp.setLength(0);
						} else if((c == CHAR_COMMENT_FIRST)) {
							cmtfirst = true;
						} else {
							// Special loop for content-simple functions
							if((isfsimple)) {
								if(c == CHAR_FUNCCALL_OB) ++ctfsimple; else
								if(c == CHAR_FUNCCALL_CB) --ctfsimple;
								if(ctfsimple > 0) cadd 		= true;
								else 			  isfsimple = false;
							}
							if(!isfsimple) {
								if(// Objects and arrays
								   (c == CHAR_OBJECT_OB ||
								    c == CHAR_ARRAY_OB) ||
								   // Annotations
								   ((!isval && c == CHAR_ANNOTATION_OB) ||
									(bann   && c == CHAR_SPACE)) 		||
								   // Function call
								   (isval && (c == CHAR_FUNCCALL_OB))) {
									// Current object is an annotation
									if(((c == CHAR_ANNOTATION_OB && !isval) || bann)) {
										// Get the annotation name
										tempName = temp.toString();
										// Clear the temporary string
										temp.setLength(0);
										// Create the annoation object first
										SSDAnnotation annObj = new SSDAnnotation(tempName);
										if(!comments.isEmpty()) {
											// Add all the gotten comments
											while(!comments.isEmpty()) {
												annObj.addComment(comments.pollLast());
											}
										}
										// Add the annotation object
										anns.push(annObj);
										// If the annotation has some items
										if(!(bann && c == CHAR_SPACE)) {
											// Add the annotation object to the parents
											parents.push(annObj);
											// Set the current parent
											parent = annObj;
											array  = false;
											annsval.push(true);
											annscnt.push(anns.size());
										}
										bann 	 = false;
										tempName = null;
									}
									// Current object is a function call
									else if((c == CHAR_FUNCCALL_OB && isval)) {
										// Check the function call's name first
										// Set unspecified temporary name if possible
										if((tempName == null)) {
											// Annotation (has to be first)
											if((annsval.peek())) {
												tempName = WORD_ANNOTATION_DEFAULT;
											}
											// Array or function
											else if((array || function)) {
												tempName = Integer.toString(counter++);
											}
											// Annotation specified after item name
											else if((lobjName != null)) {
												tempName = lobjName;
												lobjName = null; // Only one use
											}
											// Other cases
											else {
												tempName = lastName;
											}
										}
										// Get the function call's name
										String funcName = temp.toString();
										// Clear the temporary string
										temp.setLength(0);
										// Create a function call
										SSDFunctionCall fc = new SSDFunctionCall(parent, tempName, funcName);
										// Add the function call to the parents
										parents.push(fc);
										if(!anns.isEmpty()) {
											// Add all the gotten annotations
											int ai = annsval.peek()
														? anns.size() - annscnt.peek()
														: anns.size();
											annsh.clear(); // Clear the helper deque
											while(--ai >= 0) annsh.push(anns.pop());
											// Add the gotten annotations to the object correctly
											// since annotations are gotten in reversed order
											for(SSDAnnotation a : annsh)
												fc.addAnnotation0(a);
										}
										// Set the current parent
										parent 	  = fc;
										array 	  = true;
										function  = true;
										isfsimple = func_isContentSimple(funcName, fc.annotations());
										if(isfsimple) ctfsimple = 1;
									}
									// Current object is an array or an object
									else {
										// Set unspecified temporary name if possible
										if((tempName == null)) {
											// Annotation (has to be first)
											if((annsval.peek())) {
												tempName = WORD_ANNOTATION_DEFAULT;
											}
											// Array or function
											else if((array || function)) {
												tempName = Integer.toString(counter++);
											}
											// Annotation specified after item name
											else if((lobjName != null)) {
												tempName = lobjName;
												lobjName = null; // Only one use
											}
											// Other cases
											else {
												tempName = lastName;
											}
										}
										boolean isarr = c == CHAR_ARRAY_OB;
										// Create a new collection with the given name
										SSDCollection arr;
										if((parents.isEmpty())) {
											// If no main object has been yet added, add one
											arr = new SSDCollection(parent, isarr);
										} else {
											// Add a regular array or object
											arr = new SSDCollection(parent, tempName, isarr);
										}
										// Add the object to the parents
										parents.push(arr);
										if(!anns.isEmpty()) {
											// Add all the gotten annotations
											int ai = annsval.peek()
														? anns.size() - annscnt.peek()
														: anns.size();
											annsh.clear(); // Clear the helper deque
											while(--ai >= 0) annsh.push(anns.pop());
											// Add the gotten annotations to the object correctly
											// since annotations are gotten in reversed order
											for(SSDAnnotation a : annsh)
												arr.addAnnotation0(a);
										}
										if(!comments.isEmpty()) {
											// Add all the gotten comments
											while(!comments.isEmpty()) {
												arr.addComment(comments.pollLast());
											}
										}
										addToParent(parent, tempName, arr);
										// Set the current parent
										parent 	 = arr;
										array 	 = isarr;
										// Reset the temporary name
										tempName = null;
									}
								}
								// Item name delimiter or annotation sign
								else if((c == CHAR_NV_DELIMITER    ||
										 c == CHAR_ANNOTATION_SIGN ||
										 (annsval.peek() &&
											c == CHAR_ANNOTATION_NV_DELIMITER))) {
									if((c == CHAR_ANNOTATION_SIGN && isval))
										lobjName = tempName;
									tempName = temp.toString();
									bann  	 = c == CHAR_ANNOTATION_SIGN;
									isval 	 = c != CHAR_ANNOTATION_SIGN || array;
									if(!annsval.peek()
											&& !tempName.isEmpty())
										lastName = tempName;
									temp.setLength(0);
								}
								// Item delimiter
								else if((c == CHAR_ITEM_DELIMITER ||
										(annsval.peek() &&
											c == CHAR_ANNOTATION_ITEM_DELIMITER) ||
										(function &&
											c == CHAR_FUNCCALL_ARGS_DELIMITER))) {
									String value;
									if(!(value = temp.toString()).isEmpty()) {
										// Set unspecified temporary name if possible
										if((tempName == null)) {
											// Annotation (has to be first)
											if((annsval.peek())) {
												tempName = WORD_ANNOTATION_DEFAULT;
											}
											// Array or function
											else if((array || function)) {
												tempName = Integer.toString(counter++);
											}
											// Annotation specified after item name
											else if((lobjName != null)) {
												tempName = lobjName;
												lobjName = null; // Only one use
											}
											// Other cases
											else {
												tempName = lastName;
											}
										}
										if((tempName != null)) {
											SSDObject obj = new SSDObject(parent, tempName, value);
											if(!anns.isEmpty()) {
												// Add all the gotten annotations
												int ai = annsval.peek()
															? anns.size() - annscnt.peek()
															: anns.size();
												annsh.clear(); // Clear the helper deque
												while(--ai >= 0) annsh.push(anns.pop());
												// Add the gotten annotations to the object correctly
												// since annotations are gotten in reversed order
												for(SSDAnnotation a : annsh)
													obj.addAnnotation0(a);
											}
											if(!comments.isEmpty()) {
												// Add all the gotten comments
												while(!comments.isEmpty()) {
													obj.addComment(comments.pollLast());
												}
											}
											addToParent(parent, tempName, obj);
											temp.setLength(0);
											tempName = null;
										}
									}
									isval = array;
								}
								// Closing brackets
								else if(c == CHAR_OBJECT_CB 	||
										c == CHAR_ARRAY_CB  	||
										c == CHAR_ANNOTATION_CB ||
										c == CHAR_FUNCCALL_CB) {
									// Add last item in an object, or an array, if needed
									String value;
									if(!(value = temp.toString()).isEmpty()) {
										// Set unspecified temporary name if possible
										if((tempName == null)) {
											// Annotation (has to be first)
											if((annsval.peek())) {
												tempName = WORD_ANNOTATION_DEFAULT;
											}
											// Array or function
											else if((array || function)) {
												tempName = Integer.toString(counter++);
											}
											// Annotation specified after item name
											else if((lobjName != null)) {
												tempName = lobjName;
												lobjName = null; // Only one use
											}
											// Other cases
											else {
												tempName = lastName;
											}
										}
										if((tempName != null)) {
											SSDObject obj = new SSDObject(parent, tempName, value);
											if(!anns.isEmpty()) {
												// Add all the gotten annotations
												int ai = annsval.peek()
															? anns.size() - annscnt.peek()
															: anns.size();
												annsh.clear(); // Clear the helper deque
												while(--ai >= 0) annsh.push(anns.pop());
												// Add the gotten annotations to the object correctly
												// since annotations are gotten in reversed order
												for(SSDAnnotation a : annsh)
													obj.addAnnotation0(a);
											}
											if(!comments.isEmpty()) {
												// Add all the gotten comments
												while(!comments.isEmpty()) {
													obj.addComment(comments.pollLast());
												}
											}
											addToParent(parent, tempName, obj);
											temp.setLength(0);
											tempName = null;
											// Function call special condition
										}
									}
									isval = array;
									// Do not add this character
									cadd = false;
									// Remove the currently used parent
									SSDNode par = parents.pop();
									if((par != null)) {
										// The main object was removed
										if((parents.isEmpty()
												&& par.getName() == null)) {
											// Return the main object
											return (SSDCollection) par;
										}
										// Add the constructed function
										else if((function
													// Cannot be argument's annotation
													&& !annsval.peek())) {
											isfsimple 				 = false; // important
											SSDFunctionCall func 	 = (SSDFunctionCall) par;
											String			funcName = func.getName();
											addToParent(parents.peek(), funcName, func);
										}
									}
									// Set the current parent
									parent = parents.peek();
									array  = isParentArray(parent);
									if((function
											// Cannot be argument's annotation
											&& !annsval.peek())) {
										if((c == CHAR_FUNCCALL_CB))
											function = parent instanceof SSDFunctionCall;
									} else {
										if((annsval.peek() && c == CHAR_ANNOTATION_CB)) {
											annsval.pop();
											annscnt.pop();
										}
									}
								}
								// All other characters should be added
								else cadd = true;
							}
						}
					}
				}
			}
			// Add the current character if it can be added
			if(cadd) temp.append((char) c);
		}
		// Error, bad formatting, or whatever else happened
		return null;
	}
	
	static final void addToParent(SSDNode parent, String name, SSDNode object) {
		if((parent instanceof SSDCollection)) {
			SSDCollection coll = (SSDCollection) parent;
			if((coll.getType() == SSDCollectionType.ARRAY)) {
				coll.addObject(object);
			} else {
				coll.addObject(name, object);
			}
		} else if((parent instanceof SSDFunctionCall)) {
			SSDFunctionCall call = (SSDFunctionCall) parent;
			call.addArg(object);
		}
	}
	
	static final boolean isParentArray(SSDNode parent) {
		if((parent != null)) {
			if((parent instanceof SSDCollection)) {
				return ((SSDCollection) parent).getType()
							== SSDCollectionType.ARRAY;
			}
		}
		return false;
	}
	
	private static final int DEFAULT_BUFFER_SIZE = 8192;
	private static final int MAX_BUFFER_SIZE     = Integer.MAX_VALUE - 8;
	
	/**
	 * Reads all bytes in the stream. This method is directly ported from Java 10.*/
	static final byte[] streamToByteArray(InputStream stream) throws IOException {
		if((stream == null))
			throw new IllegalArgumentException("Stream cannot be null!");
        byte[] buf = new byte[DEFAULT_BUFFER_SIZE];
        int capacity = buf.length;
        int nread = 0;
        int n;
        for(;;) {
            // read to EOF which may read more or less than initial buffer size
            while((n = stream.read(buf, nread, capacity - nread)) > 0)
                nread += n;
            // if the last call to read returned -1, then we're done
            if((n < 0))
                break;
            // need to allocate a larger buffer
            if((capacity <= MAX_BUFFER_SIZE - capacity)) {
                capacity = capacity << 1;
            } else {
                if((capacity == MAX_BUFFER_SIZE))
                    throw new OutOfMemoryError("Required array size too large");
                capacity = MAX_BUFFER_SIZE;
            }
            buf = Arrays.copyOf(buf, capacity);
        }
        return (capacity == nread) ? buf : Arrays.copyOf(buf, nread);
    }
	
	static final Charset CHARSET = StandardCharsets.UTF_8;
	static final String streamToString(InputStream stream) {
		try {
			return new String(streamToByteArray(stream), CHARSET);
		} catch(IOException ex) {
		}
		return null;
	}
	
	static final InputStream resourceStream(String path) {
		return SSDF.class.getResourceAsStream(path);
	}
	
	static final String FUNC_CONTENT_SIMPLE = "CONTENT_SIMPLE";
	static final boolean func_isContentSimple(String funcName, Set<SSDAnnotation> anns) {
		try {
			String namespace;
			String[] split = funcName.split("\\" + CHAR_NAME_DELIMITER);
			if((split.length == 1)) {
				namespace 		 = SSDFunctionCall.FUNCTION_PREFIX;
				String ann_dname = SSDFunctionCall.ANNOTATION_NAMESPACE;
				if((anns != null)) {
					for(SSDAnnotation ann : anns) {
						String name = ann.getName();
						if((name.equalsIgnoreCase(ann_dname))) {
							namespace = ann.getString("value");
							break;
						}
					}
				}
			} else {
				namespace = ""; // Namespace defined in funcName
			}
			String className = namespace + funcName;
			Class<?> clazz = Class.forName(className);
			if((SSDFunctionCall.FUNCTION_IMPL_CLASS.isAssignableFrom(clazz))) {
				Field field = clazz.getDeclaredField(FUNC_CONTENT_SIMPLE);
				field.setAccessible(true); // make the field accessible
				if((Modifier.isStatic(field.getModifiers()))) {
					if((field.getType() == boolean.class ||
						field.getType() == Boolean.class)) {
						return (boolean) field.get(null);
					}
				}
			}
		} catch(Exception ex) {
		}
		return false;
	}
	
	public static final SSDCollection empty() {
		return SSDCollection.empty();
	}
	
	public static final SSDCollection emptyArray() {
		return SSDCollection.emptyArray();
	}
	
	public static final SSDCollection read(String content) {
		if((content == null)) {
			throw new IllegalArgumentException("Content cannot be null");
		}
		char[] chars = format(content.toCharArray());
		return readObjects(chars, 0, chars.length);
	}
	
	public static final SSDCollection read(InputStream stream) {
		if((stream == null)) {
			throw new IllegalArgumentException("Stream cannot be null");
		}
		return read(streamToString(stream));
	}
	
	public static final SSDCollection read(File file) {
		if((file == null)) {
			throw new IllegalArgumentException("File cannot be null");
		}
		try {
			return read(new FileInputStream(file));
		} catch(Exception ex) {
			throw new IllegalStateException("An error has occurred while trying to read the given file");
		}
	}
	
	public static final SSDCollection readResource(String path) {
		if((path == null || path.isEmpty())) {
			throw new IllegalArgumentException("Path cannot be null or empty");
		}
		return read(resourceStream(path));
	}
	
	public static final SSDCollection readJSON(String json) {
		return JSON.read(json);
	}
	
	public static final SSDCollection readJSON(InputStream stream) {
		return JSON.read(stream);
	}
	
	public static final SSDCollection readJSON(File file) {
		return JSON.read(file);
	}
	
	public static final SSDCollection readJSONResource(String path) {
		return JSON.readResource(path);
	}
	
	// class for reading JSON strings, streams, files, and resources
	private static final class JSON {
		
		public static final SSDCollection read(String content) {
			if((content == null)) {
				throw new IllegalArgumentException("Content cannot be null");
			}
			return read(content, 0, content.length());
		}
		
		public static final SSDCollection read(InputStream stream) {
			if((stream == null)) {
				throw new IllegalArgumentException("Stream cannot be null");
			}
			return read(streamToString(stream));
		}
		
		public static final SSDCollection read(File file) {
			if((file == null)) {
				throw new IllegalArgumentException("File cannot be null");
			}
			try {
				return read(new FileInputStream(file));
			} catch(Exception ex) {
				throw new IllegalStateException("An error has occurred while trying to read the given file");
			}
		}
		
		public static final SSDCollection readResource(String path) {
			if((path == null || path.isEmpty())) {
				throw new IllegalArgumentException("Path cannot be null or empty");
			}
			return read(resourceStream(path));
		}
		
		private static final SSDCollection read(String content, int off, int len) {
			// determining if a character is in double quotes
			boolean              indq     = false;
			// determining if a character is in single quotes
			boolean              insq     = false;
			// temporary objects for names and values
			StringBuilder        temp     = new StringBuilder();
			String               lastTemp = null;
			// determining if a character can be added
			boolean              cadd     = false;
			// determining if a character is escaped
			boolean              escaped  = false;
			// current escape strength
			int	                 escape   = 0;
			// objects for handling information about parents
			Deque<SSDCollection> parents  = new ArrayDeque<>();
			SSDCollection        parent   = null;
			boolean              isArray  = false;
			// read the characters and construct the objects
			read: for(int i = off, l = off + len, c; i < l; ++i) {
				cadd = true;
				c 	 = content.charAt(i);
				// escape logic
				if((escaped && --escape == 0))     escaped = false;
				if((c == CHAR_ESCAPE && !escaped)) escaped = (escape = 2) > 0; else
				// quotes logic
				if((c == CHAR_DOUBLE_QUOTES && !insq && !escaped)) indq = !indq; else
				if((c == CHAR_SINGLE_QUOTES && !indq && !escaped)) insq = !insq;
				// reading logic
				else {
					// check if not in quotes
					if(!indq && !insq) {
						cadd = false;
						// remove useless whitespace characters
						if((Character.isWhitespace(c))) {
							do {
								if((i + 1 >= len))
									// end of the string
									break read;
								c = content.charAt(++i);
							}
							// loop while the character is whitespace
							while(Character.isWhitespace(c));
							// go back one character, and continue the read loop
							// to process the next non-whitespace character
							--i; continue;
						}
						// object or array begin definition
						if((c == CHAR_OBJECT_OB || c == CHAR_ARRAY_OB)) {
							boolean array = c == CHAR_ARRAY_OB;
							SSDCollection object;
							if((parent != null)) {
								object = SSDCollection.empty(array);
								if((isArray && lastTemp == null)) {
									lastTemp = Integer.toString(parent.length());
								}
								parent.setDirect(lastTemp, object);
								lastTemp = null;
							} else {
								// the main object
								object = new SSDCollection(null, array);
							}
							parents.push(object);
							parent  = object;
							isArray = array;
						}
						// object or array end definition
						else if((c == CHAR_OBJECT_CB || c == CHAR_ARRAY_CB)) {
							if((temp.length() > 0)) {
								if((isArray && lastTemp == null)) {
									lastTemp = Integer.toString(parent.length());
								}
								SSDObject object = SSDObject.ofRaw(lastTemp, temp.toString());
								parent.setDirect(lastTemp, object);
								lastTemp = null;
								temp.setLength(0);
							}
							parent = parents.pop();
							if(!parents.isEmpty())
								parent = parents.peek();
							isArray = parent.getType() == SSDCollectionType.ARRAY;
						}
						// item end definition
						else if((c == CHAR_ITEM_DELIMITER)) {
							if((temp.length() > 0)) {
								if((isArray && lastTemp == null)) {
									lastTemp = Integer.toString(parent.length());
								}
								SSDObject object = SSDObject.ofRaw(lastTemp, temp.toString());
								parent.setDirect(lastTemp, object);
								lastTemp = null;
								temp.setLength(0);
							}
						}
						// item name definition
						else if((c == CHAR_NV_DELIMITER)) {
							lastTemp = temp.toString();
							lastTemp = lastTemp.substring(1, lastTemp.length() - 1);
							temp.setLength(0);
						}
						// other characters
						else cadd = true;
					}
				}
				// add the current character, if possible
				if((cadd)) temp.append((char) c);
			}
			return parent;
		}
	}
}