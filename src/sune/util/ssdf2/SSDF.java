package sune.util.ssdf2;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.nio.charset.Charset;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Map.Entry;

import sune.util.ssdf2.SSDCollection.SSDCollectionType;

public final class SSDF {
	
	// General characters
	static final char CHAR_NEWLINE		  		= '\n';
	static final char CHAR_TAB			  		= '\t';
	static final char CHAR_SPACE		  		= ' ';
	static final char CHAR_NAME_DELIMITER 		= '.';
	static final char CHAR_ANNOTATION_DELIMITER = ':';
	// General syntax
	static final char CHAR_OBJECT_OB 	  = '{';
	static final char CHAR_OBJECT_CB 	  = '}';
	static final char CHAR_ARRAY_OB 	  = '[';
	static final char CHAR_ARRAY_CB 	  = ']';
	static final char CHAR_NV_DELIMITER	  = ':';
	static final char CHAR_ITEM_DELIMITER = ',';
	static final char CHAR_DOUBLE_QUOTES  = '"';
	static final char CHAR_SINGLE_QUOTES  = '\'';
	static final char CHAR_ESCAPE		  = '\\';
	// Annotation syntax
	static final char CHAR_ANNOTATION_SIGN 			 = '@';
	static final char CHAR_ANNOTATION_OB   			 = '(';
	static final char CHAR_ANNOTATION_CB   			 = ')';
	static final char CHAR_ANNOTATION_NV_DELIMITER 	 = '=';
	static final char CHAR_ANNOTATION_ITEM_DELIMITER = ',';
	// Function call syntax
	static final char CHAR_FUNCCALL_OB 			   = '(';
	static final char CHAR_FUNCCALL_CB 			   = ')';
	static final char CHAR_FUNCCALL_ARGS_DELIMITER = ',';
	// Variable syntax
	static final char CHAR_VARIABLE_SIGN   	  = '$';
	static final char CHAR_VARIABLE_DELIMITER = '.';
	static final char CHAR_VARIABLE_CONCAT 	  = '+';
	// Comment syntax
	static final char CHAR_COMMENT_FIRST 		  = '/';
	static final char CHAR_COMMENT_ONE_LINE 	  = '/';
	static final char CHAR_COMMENT_MULTIPLE_LINES = '*';
	// Syntax special words
	static final String WORD_NULL  = "null";
	static final String WORD_TRUE  = "true";
	static final String WORD_FALSE = "false";
	// Other special words
	static final String WORD_ANNOTATION_DEFAULT = "value";
	static final String WORD_VARIABLE_VALUE		= "value";
	static final String WORD_VARIABLE_THIS		= "this";
	static final String WORD_VARIABLE_MAIN		= "main";
	
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
			// Escape logic
			if(escaped && --escape == 0) 	 escaped = false;
			if(c == CHAR_ESCAPE && !escaped) escaped = (escape = 2) > 0; else
			// Quotes logic
			if(c == CHAR_DOUBLE_QUOTES && !insq && !escaped) indq = !indq; else
			if(c == CHAR_SINGLE_QUOTES && !indq && !escaped) insq = !insq; else
			// Formatting logic
			if(!indq && !insq) {
				if((cmtcontent)) {
					if((cmtoneline)) {
						// One line comment
						if((c == CHAR_NEWLINE)) {
							cmtcontent = false;
							cmtfirst   = false;
						}
					} else {
						// Multiple line comment
						if((cmtfirst && c == CHAR_COMMENT_FIRST)) {
							cmtcontent = false;
							cmtfirst   = false;
						} else if((c == CHAR_COMMENT_MULTIPLE_LINES)) {
							cmtfirst = true;
						}
					}
				} else {
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
			throw new IllegalArgumentException(
				"Cannot read null array of characters!");
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
		// The last content of item when entering a comment
		String lastContent = null;
		// Read the characters and construct the map
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
					if((cmtcontent)) {
						cadd = true;
						if((cmtoneline)) {
							// One line comment
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
								cmtoneline = false;
								cadd 	   = false;
							}
						} else {
							// Multiple line comment
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
								cmtoneline = false;
								cadd 	   = false;
							} else if((c == CHAR_COMMENT_MULTIPLE_LINES)) {
								cmtfirst = true;
							}
						}
					} else {
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
										if((tempName == null)) {
											if((array || function)) {
												tempName = Integer.toString(counter++);
											} else if((lobjName != null)) {
												tempName = lobjName;
												lobjName = null; // Only one use
											} else if((annsval.peek())) {
												tempName = WORD_ANNOTATION_DEFAULT;
											} else {
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
										// Set the current parent
										parent 	  = fc;
										array 	  = true;
										function  = true;
										isfsimple = func_isContentSimple(funcName, anns);
										if(isfsimple) ctfsimple = 1;
									}
									// Current object is an array or an object
									else {
										if((tempName == null)) {
											if((array || function)) {
												tempName = Integer.toString(counter++);
											} else if((lobjName != null)) {
												tempName = lobjName;
												lobjName = null; // Only one use
											} else if((annsval.peek())) {
												tempName = WORD_ANNOTATION_DEFAULT;
											} else {
												tempName = lastName;
											}
										}
										boolean isarr = c == CHAR_ARRAY_OB;
										// Create a new collection with the given name
										SSDCollection arr;
										if((parents.isEmpty())) {
											// If no main object has been yet added, add one
											arr = new SSDCollection(parent, tempName);
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
											while(--ai >= 0)
												arr.addAnnotation0(anns.pop());
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
										if((tempName == null)) {
											if((array || function)) {
												tempName = Integer.toString(counter++);
											} else if((lobjName != null)) {
												tempName = lobjName;
												lobjName = null; // Only one use
											} else if((annsval.peek())) {
												tempName = WORD_ANNOTATION_DEFAULT;
											} else {
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
												while(--ai >= 0)
													obj.addAnnotation0(anns.pop());
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
										if((tempName == null)) {
											if((array || function)) {
												tempName = Integer.toString(counter++);
											} else if((lobjName != null)) {
												tempName = lobjName;
												lobjName = null; // Only one use
											} else if((annsval.peek())) {
												tempName = WORD_ANNOTATION_DEFAULT;
											} else {
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
												while(--ai >= 0)
													obj.addAnnotation0(anns.pop());
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
										else if((function)) {
											isfsimple 				 = false; // important
											SSDFunctionCall func 	 = (SSDFunctionCall) par;
											String			funcName = func.getName();
											addToParent(parents.peek(), funcName, func);
										}
									}
									// Set the current parent
									parent = parents.peek();
									array  = isParentArray(parent);
									if((function)) {
										if((c == CHAR_FUNCCALL_CB))
											function = false;
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
	
	static final SSDCollection convertJSONNames(SSDCollection sc) {
		String cname 	 = sc.getName();
		SSDCollection nc = cname == null || cname.isEmpty()
			? SSDCollection.empty()
			: new SSDCollection(
				sc.getParent(),
				cname.startsWith("\"") && cname.endsWith("\"") ?
					cname.substring(1, cname.length()-1) :
					cname,
				sc.getType() == SSDCollectionType.ARRAY
			);
		for(Entry<String, SSDNode> e : sc.objects().entrySet()) {
			String name   = e.getKey();
			SSDNode value = e.getValue();
			String fname  = name.startsWith("\"") && name.endsWith("\"") ?
								name.substring(1, name.length()-1) :
								name;
			if(value instanceof SSDCollection) {
				SSDCollection vc = (SSDCollection) value;
				SSDCollection kc = convertJSONNames(
					vc.getType() == SSDCollectionType.ARRAY ?
						new SSDCollection(vc.getParent(),
										  fname,
										  true,
										  vc.objects(),
										  vc.annotations()) :
						vc);
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
	
	static final String FUNC_CONTENT_SIMPLE = "CONTENT_SIMPLE";
	static final boolean func_isContentSimple(String funcName, Deque<SSDAnnotation> anns) {
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
				if(!field.isAccessible())
					field.setAccessible(true);
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
		return SSDCollection.empty(true);
	}
	
	public static final SSDCollection read(String content) {
		if(content == null) {
			throw new IllegalArgumentException(
				"Content cannot be null!");
		}
		char[] chars = format(content.toCharArray());
		return readObjects(chars, 0, chars.length);
	}
	
	public static final SSDCollection read(InputStream stream) {
		if(stream == null) {
			throw new IllegalArgumentException(
				"Stream cannot be null!");
		}
		return read(streamToString(stream));
	}
	
	public static final SSDCollection read(File file) {
		if(file == null) {
			throw new IllegalArgumentException(
				"File cannot be null!");
		}
		try {
			return read(streamToString(new FileInputStream(file)));
		} catch(Exception ex) {
			throw new IllegalStateException(
				"An error has occurred while trying to read the given file!");
		}
	}
	
	public static final SSDCollection readResource(String path) {
		if(path == null || path.isEmpty()) {
			throw new IllegalArgumentException(
				"Path cannot be null nor empty!");
		}
		return read(resourceStream(path));
	}
	
	public static final SSDCollection readJSONResource(String path) {
		if(path == null || path.isEmpty()) {
			throw new IllegalArgumentException(
				"Path cannot be null nor empty!");
		}
		return readJSON(resourceStream(path));
	}
	
	public static final SSDCollection readJSON(String json) {
		return convertJSONNames(read(json));
	}
	
	public static final SSDCollection readJSON(InputStream stream) {
		return convertJSONNames(read(stream));
	}
	
	public static final SSDCollection readJSON(File file) {
		return convertJSONNames(read(file));
	}
}