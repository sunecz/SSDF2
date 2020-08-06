package sune.util.ssdf2;

import static sune.util.ssdf2.SSDF.CHAR_AND_DELIMITER;
import static sune.util.ssdf2.SSDF.CHAR_ANNOTATION_DELIMITER;
import static sune.util.ssdf2.SSDF.CHAR_ARRAY_CB;
import static sune.util.ssdf2.SSDF.CHAR_ARRAY_OB;
import static sune.util.ssdf2.SSDF.CHAR_DOUBLE_QUOTES;
import static sune.util.ssdf2.SSDF.CHAR_ITEM_DELIMITER;
import static sune.util.ssdf2.SSDF.CHAR_NAME_DELIMITER;
import static sune.util.ssdf2.SSDF.CHAR_NEWLINE;
import static sune.util.ssdf2.SSDF.CHAR_NV_DELIMITER;
import static sune.util.ssdf2.SSDF.CHAR_OBJECT_CB;
import static sune.util.ssdf2.SSDF.CHAR_OBJECT_OB;
import static sune.util.ssdf2.SSDF.CHAR_OR_DELIMITER;
import static sune.util.ssdf2.SSDF.CHAR_SPACE;
import static sune.util.ssdf2.SSDF.CHAR_TAB;
import static sune.util.ssdf2.SSDF.WORD_NULL;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.function.Function;

import sune.util.ssdf2.exception.NotFoundException;
import sune.util.ssdf2.exception.TypeMismatchException;

public class SSDCollection implements SSDNode, Iterable<SSDNode> {
	
	// Protected properties
	protected final SSDProperty<SSDNode> parent;
	protected final SSDProperty<String>  name;
	
	// Private properties
	private final Map<String, SSDNode> objects;
	private final boolean 			   isArray;
	
	// Annotations
	private final Set<SSDAnnotation> annotations;
	
	// Comments
	private final Set<SSDComment> comments;
	
	SSDCollection(SSDNode parent, String name, boolean isArray) {
		this(parent, name, isArray, new LinkedHashMap<>(), null, null);
	}
	
	SSDCollection(SSDNode parent, String name, boolean isArray,
			Map<String, SSDNode> objects,
			Set<SSDAnnotation> annotations,
			Set<SSDComment> comments) {
		checkArgs(parent, name, objects, false);
		this.parent  = new SSDProperty<>(parent);
		this.name 	 = new SSDProperty<>(name);
		this.isArray = isArray;
		this.objects = objects;
		// Annotations
		if((annotations == null))
			annotations = new LinkedHashSet<>();
		this.annotations = annotations;
		// Comments
		if((comments == null))
			comments = new LinkedHashSet<>();
		this.comments = comments;
	}
	
	// Method for creating the main parent (object) of the data structure
	SSDCollection(SSDNode parent, boolean isArray) {
		Map<String, SSDNode> objects = new LinkedHashMap<>();
  		checkArgs(parent, null, objects, true);
  		this.parent  = new SSDProperty<>(parent);
  		this.name 	 = new SSDProperty<>(null); // main object has no name
  		this.isArray = isArray; // main object can also be an array
  		this.objects = objects;
  		// Annotations
  		this.annotations = new LinkedHashSet<>();
  		// Comments
  		this.comments = new LinkedHashSet<>();
  	}
	
	static final void checkArgs(SSDNode parent, String name, Map<String, SSDNode> objects,
			boolean isMainObject) { // Needs to be checked
		if(name == null && parent != null && !isMainObject) {
			throw new IllegalArgumentException(
				"Name of a collection cannot be null!");
		}
		if(objects == null) {
			throw new IllegalArgumentException(
				"Objects Map of a collection cannot be null!");
		}
	}
	
	void checkIfArray() {
		if(!isArray) {
			throw new UnsupportedOperationException("SSDCollection is not an array");
		}
	}
	
	void checkName(String name) {
		if((name == null || name.isEmpty())) {
			throw new IllegalArgumentException("Name cannot be null or empty");
		}
	}
	
	void addObjects(Map<String, SSDNode> objects) {
		if(objects != null) {
			this.objects.putAll(objects);
		}
	}
	
	void addObject(String name, SSDNode object) {
		checkName(name);
		if(object == null) {
			throw new IllegalArgumentException(
				"Object cannot be null!");
		}
		this.objects.put(name, object);
	}
	
	void addObject(SSDNode object) {
		checkIfArray();
		String name = Integer.toString(nextIndex());
		addObject(name, object);
	}
	
	Map<String, SSDNode> objectMap() {
		return objects;
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
	
	// Comments
	Set<SSDComment> comments() {
		return comments;
	}
	
	int nextIndex() {
		return objects.size();
	}
	
	public static final SSDCollection empty() {
		return empty(false);
	}
	
	public static final SSDCollection emptyArray() {
		return empty(true);
	}
	
	static final SSDCollection empty(boolean isArray) {
		return new SSDCollection(null, "", isArray);
	}
	
	protected final SSDNode get(String name, boolean checkObject, boolean checkCollection,
				boolean checkFunctionCall) {
		checkName(name);
		int nindex = name.indexOf(CHAR_NAME_DELIMITER);
		int aindex = name.indexOf(CHAR_ANNOTATION_DELIMITER);
		int oindex = name.indexOf(CHAR_OR_DELIMITER);
		if((oindex > -1)) {
			String  name0 = name.substring(0, oindex);
			boolean hasi0 = has(name0, false, false, false);
			if((hasi0)) {
				SSDNode item0 = get(name0, checkObject, checkCollection, checkFunctionCall);
				if((item0 != null)) return item0;
			}
			String name1 = name.substring(oindex+1);
			if((name1.startsWith("*"))) {
				// Return raw value, such as null, number, etc.
				String value = name1.substring(1);
				return SSDObject.ofRaw(value);
			} else {
				boolean hasi1 = has(name1, false, false, false);
				if((hasi1)) {
					SSDNode item1 = get(name1, checkObject, checkCollection, checkFunctionCall);
					return  item1;
				}
				// throw an exception, if node is not found
				throw new NotFoundException("Node " + name + " does not exist!");
			}
		} else {
			if((nindex > -1)) {
				if((aindex > -1 && aindex < nindex)) {
					String nname = name.substring(0, aindex);
					String aname = name.substring(aindex+1, nindex);
					String kname = name.substring(nindex+1);
					return get(nname).getAnnotation(aname)
									 .get(kname, checkObject,
									      checkCollection,
									      checkFunctionCall);
				} else {
					String cname = name.substring(0, nindex);
					String oname = name.substring(nindex+1);
					return getCollection(cname).get(oname, checkObject,
					                                checkCollection,
					                                checkFunctionCall);
				}
			} else if((aindex > -1)) {
				String nname = name.substring(0, aindex);
				String aname = name.substring(aindex+1);
				if((checkObject))
					throw new NotFoundException("Object " + name + " is not a SSDObject!");
				return get(nname).getAnnotation(aname);
			} else {
				SSDNode node = objects.get(name);
				if((node == null)) {
					// throw an exception, if node is not found
					throw new NotFoundException("Node " + name + " does not exist!");
				}
				if(checkObject
						&& !(node instanceof SSDObject))
					throw new TypeMismatchException(
						"Object " + name + " is not a SSDObject!");
				if(checkCollection
						&& !(node instanceof SSDCollection))
					throw new TypeMismatchException(
						"Object " + name + " is not a SSDCollection!");
				if(checkFunctionCall
						&& !(node instanceof SSDFunctionCall))
					throw new TypeMismatchException(
						"Object " + name + " is not a SSDFunctionCall!");
				return node;
			}
		}
	}
	
	@SuppressWarnings("unchecked")
	protected final <T> T get_cast(String name, boolean checkObject, boolean checkCollection,
			boolean checkFunctionCall) {
		return (T) get(name, checkObject, checkCollection, checkFunctionCall);
	}
	
	// used especially for JSON
	protected final SSDNode getDirect(String name, boolean checkObject, boolean checkCollection,
			boolean checkFunctionCall) {
		checkName(name);
		SSDNode node = objects.get(name);
		if((node == null)) {
			// throw an exception, if node is not found
			throw new NotFoundException("Node " + name + " does not exist!");
		}
		if(checkObject
				&& !(node instanceof SSDObject))
			throw new TypeMismatchException(
				"Object " + name + " is not a SSDObject!");
		if(checkCollection
				&& !(node instanceof SSDCollection))
			throw new TypeMismatchException(
				"Object " + name + " is not a SSDCollection!");
		if(checkFunctionCall
				&& !(node instanceof SSDFunctionCall))
			throw new TypeMismatchException(
				"Object " + name + " is not a SSDFunctionCall!");
		return node;
	}
	
	@SuppressWarnings("unchecked")
	protected final <T> T getDirect_cast(String name, boolean checkObject, boolean checkCollection,
			boolean checkFunctionCall) {
		return (T) getDirect(name, checkObject, checkCollection, checkFunctionCall);
	}
	
	public SSDNode getDirect(String name) {
		return getDirect(name, false, false, false);
	}
	
	public SSDObject getDirectObject(String name) {
		return getDirect_cast(name, true, false, false);
	}
	
	public SSDCollection getDirectCollection(String name) {
		return getDirect_cast(name, false, true, false);
	}
	
	public SSDFunctionCall getDirectFunctionCall(String name) {
		return getDirect_cast(name, false, false, true);
	}
	
	public boolean getDirectBoolean(String name) {
		return getDirectObject(name).booleanValue();
	}
	
	public byte getDirectByte(String name) {
		return getDirectObject(name).byteValue();
	}
	
	public short getDirectShort(String name) {
		return getDirectObject(name).shortValue();
	}
	
	public int getDirectInt(String name) {
		return getDirectObject(name).intValue();
	}
	
	public long getDirectLong(String name) {
		return getDirectObject(name).longValue();
	}
	
	public float getDirectFloat(String name) {
		return getDirectObject(name).floatValue();
	}
	
	public double getDirectDouble(String name) {
		return getDirectObject(name).doubleValue();
	}
	
	public String getDirectString(String name) {
		return getDirectObject(name).stringValue();
	}
	
	public SSDNode get(String name) {
		return get(name, false, false, false);
	}
	
	public SSDObject getObject(String name) {
		return get_cast(name, true, false, false);
	}
	
	public SSDCollection getCollection(String name) {
		return get_cast(name, false, true, false);
	}
	
	public SSDFunctionCall getFunctionCall(String name) {
		return get_cast(name, false, false, true);
	}
	
	public boolean getBoolean(String name) {
		return getObject(name).booleanValue();
	}
	
	public byte getByte(String name) {
		return getObject(name).byteValue();
	}
	
	public short getShort(String name) {
		return getObject(name).shortValue();
	}
	
	public int getInt(String name) {
		return getObject(name).intValue();
	}
	
	public long getLong(String name) {
		return getObject(name).longValue();
	}
	
	public float getFloat(String name) {
		return getObject(name).floatValue();
	}
	
	public double getDouble(String name) {
		return getObject(name).doubleValue();
	}
	
	public String getString(String name) {
		return getObject(name).stringValue();
	}
	
	public SSDNode get(int index) {
		checkIfArray();
		return getDirect(Integer.toString(index), false, false, false);
	}
	
	public SSDObject getObject(int index) {
		checkIfArray();
		return getDirect_cast(Integer.toString(index), true, false, false);
	}
	
	public SSDCollection getCollection(int index) {
		checkIfArray();
		return getDirect_cast(Integer.toString(index), false, true, false);
	}
	
	public SSDFunctionCall getFunctionCall(int index) {
		checkIfArray();
		return getDirect_cast(Integer.toString(index), false, false, true);
	}
	
	public boolean getBoolean(int index) {
		return getObject(index).booleanValue();
	}
	
	public byte getByte(int index) {
		return getObject(index).byteValue();
	}
	
	public short getShort(int index) {
		return getObject(index).shortValue();
	}
	
	public int getInt(int index) {
		return getObject(index).intValue();
	}
	
	public long getLong(int index) {
		return getObject(index).longValue();
	}
	
	public float getFloat(int index) {
		return getObject(index).floatValue();
	}
	
	public double getDouble(int index) {
		return getObject(index).doubleValue();
	}
	
	public String getString(int index) {
		return getObject(index).stringValue();
	}
	
	@SuppressWarnings("unchecked")
	protected final <T> T getOrDefault(String name, boolean checkObject, boolean checkCollection,
				boolean checkFunctionCall, T defaultValue) {
		boolean has = has(name, false, false, false);
		return  has ? (T) get(name, checkObject, checkCollection, checkFunctionCall)
		            : defaultValue;
	}
	
	@SuppressWarnings("unchecked")
	protected final <T> T getOrDefault(int index, boolean checkObject, boolean checkCollection,
				boolean checkFunctionCall, T defaultValue) {
		String  ind = Integer.toString(index);
		boolean has = hasDirect(ind, false, false, false);
		return  has ? (T) getDirect(ind, checkObject, checkCollection, checkFunctionCall)
		            : defaultValue;
	}
	
	@SuppressWarnings("unchecked")
	protected final <T> T getDirectOrDefault(String name, boolean checkObject, boolean checkCollection,
				boolean checkFunctionCall, T defaultValue) {
		boolean has = hasDirect(name, false, false, false);
		return  has ? (T) getDirect(name, checkObject, checkCollection, checkFunctionCall)
		            : defaultValue;
	}
	
	protected final boolean getDirectObjectOrDefault(String name, boolean defaultValue) {
		boolean has = has(name, false, false, false);
		return  has ? getDirectObject(name).booleanValue()
		            : defaultValue;
	}
	
	protected final byte getDirectObjectOrDefault(String name, byte defaultValue) {
		boolean has = has(name, false, false, false);
		return  has ? getDirectObject(name).byteValue()
		            : defaultValue;
	}
	
	protected final short getDirectObjectOrDefault(String name, short defaultValue) {
		boolean has = has(name, false, false, false);
		return  has ? getDirectObject(name).shortValue()
		            : defaultValue;
	}
	
	protected final int getDirectObjectOrDefault(String name, int defaultValue) {
		boolean has = has(name, false, false, false);
		return  has ? getDirectObject(name).intValue()
		            : defaultValue;
	}
	
	protected final long getDirectObjectOrDefault(String name, long defaultValue) {
		boolean has = has(name, false, false, false);
		return  has ? getDirectObject(name).longValue()
		            : defaultValue;
	}
	
	protected final float getDirectObjectOrDefault(String name, float defaultValue) {
		boolean has = has(name, false, false, false);
		return  has ? getDirectObject(name).floatValue()
		            : defaultValue;
	}
	
	protected final double getDirectObjectOrDefault(String name, double defaultValue) {
		boolean has = has(name, false, false, false);
		return  has ? getDirectObject(name).doubleValue()
		            : defaultValue;
	}
	
	protected final String getDirectObjectOrDefault(String name, String defaultValue) {
		boolean has = has(name, false, false, false);
		return  has ? getDirectObject(name).stringValue()
		            : defaultValue;
	}
	
	protected final boolean getDirectObjectOrDefault(int index, boolean defaultValue) {
		return getDirectObjectOrDefault(Integer.toString(index), defaultValue);
	}
	
	protected final byte getDirectObjectOrDefault(int index, byte defaultValue) {
		return getDirectObjectOrDefault(Integer.toString(index), defaultValue);
	}
	
	protected final short getDirectObjectOrDefault(int index, short defaultValue) {
		return getDirectObjectOrDefault(Integer.toString(index), defaultValue);
	}
	
	protected final int getDirectObjectOrDefault(int index, int defaultValue) {
		return getDirectObjectOrDefault(Integer.toString(index), defaultValue);
	}
	
	protected final long getDirectObjectOrDefault(int index, long defaultValue) {
		return getDirectObjectOrDefault(Integer.toString(index), defaultValue);
	}
	
	protected final float getDirectObjectOrDefault(int index, float defaultValue) {
		return getDirectObjectOrDefault(Integer.toString(index), defaultValue);
	}
	
	protected final double getDirectObjectOrDefault(int index, double defaultValue) {
		return getDirectObjectOrDefault(Integer.toString(index), defaultValue);
	}
	
	protected final String getDirectObjectOrDefault(int index, String defaultValue) {
		return getDirectObjectOrDefault(Integer.toString(index), defaultValue);
	}
	
	public SSDNode getDirect(String name, SSDNode defaultValue) {
		return getDirectOrDefault(name, false, false, false, defaultValue);
	}
	
	public SSDObject getDirectObject(String name, SSDObject defaultValue) {
		return getDirectOrDefault(name, true, false, false, defaultValue);
	}
	
	public SSDCollection getDirectCollection(String name, SSDCollection defaultValue) {
		return getDirectOrDefault(name, false, true, false, defaultValue);
	}
	
	public SSDFunctionCall getDirectFunctionCall(String name, SSDFunctionCall defaultValue) {
		return getDirectOrDefault(name, false, false, true, defaultValue);
	}
	
	public boolean getDirectBoolean(String name, boolean defaultValue) {
		return getDirectObjectOrDefault(name, defaultValue);
	}
	
	public byte getDirectByte(String name, byte defaultValue) {
		return getDirectObjectOrDefault(name, defaultValue);
	}
	
	public short getDirectShort(String name, short defaultValue) {
		return getDirectObjectOrDefault(name, defaultValue);
	}
	
	public int getDirectInt(String name, int defaultValue) {
		return getDirectObjectOrDefault(name, defaultValue);
	}
	
	public long getDirectLong(String name, long defaultValue) {
		return getDirectObjectOrDefault(name, defaultValue);
	}
	
	public float getDirectFloat(String name, float defaultValue) {
		return getDirectObjectOrDefault(name, defaultValue);
	}
	
	public double getDirectDouble(String name, double defaultValue) {
		return getDirectObjectOrDefault(name, defaultValue);
	}
	
	public String getDirectString(String name, String defaultValue) {
		return getDirectObjectOrDefault(name, defaultValue);
	}
	
	protected final boolean getObjectOrDefault(String name, boolean defaultValue) {
		boolean has = has(name, false, false, false);
		return  has ? getObject(name).booleanValue()
		            : defaultValue;
	}
	
	protected final byte getObjectOrDefault(String name, byte defaultValue) {
		boolean has = has(name, false, false, false);
		return  has ? getObject(name).byteValue()
		            : defaultValue;
	}
	
	protected final short getObjectOrDefault(String name, short defaultValue) {
		boolean has = has(name, false, false, false);
		return  has ? getObject(name).shortValue()
		            : defaultValue;
	}
	
	protected final int getObjectOrDefault(String name, int defaultValue) {
		boolean has = has(name, false, false, false);
		return  has ? getObject(name).intValue()
		            : defaultValue;
	}
	
	protected final long getObjectOrDefault(String name, long defaultValue) {
		boolean has = has(name, false, false, false);
		return  has ? getObject(name).longValue()
		            : defaultValue;
	}
	
	protected final float getObjectOrDefault(String name, float defaultValue) {
		boolean has = has(name, false, false, false);
		return  has ? getObject(name).floatValue()
		            : defaultValue;
	}
	
	protected final double getObjectOrDefault(String name, double defaultValue) {
		boolean has = has(name, false, false, false);
		return  has ? getObject(name).doubleValue()
		            : defaultValue;
	}
	
	protected final String getObjectOrDefault(String name, String defaultValue) {
		boolean has = has(name, false, false, false);
		return  has ? getObject(name).stringValue()
		            : defaultValue;
	}
	
	protected final boolean getObjectOrDefault(int index, boolean defaultValue) {
		return getDirectObjectOrDefault(index, defaultValue);
	}
	
	protected final byte getObjectOrDefault(int index, byte defaultValue) {
		return getDirectObjectOrDefault(index, defaultValue);
	}
	
	protected final short getObjectOrDefault(int index, short defaultValue) {
		return getDirectObjectOrDefault(index, defaultValue);
	}
	
	protected final int getObjectOrDefault(int index, int defaultValue) {
		return getDirectObjectOrDefault(index, defaultValue);
	}
	
	protected final long getObjectOrDefault(int index, long defaultValue) {
		return getDirectObjectOrDefault(index, defaultValue);
	}
	
	protected final float getObjectOrDefault(int index, float defaultValue) {
		return getDirectObjectOrDefault(index, defaultValue);
	}
	
	protected final double getObjectOrDefault(int index, double defaultValue) {
		return getDirectObjectOrDefault(index, defaultValue);
	}
	
	protected final String getObjectOrDefault(int index, String defaultValue) {
		return getDirectObjectOrDefault(index, defaultValue);
	}
	
	public SSDNode get(String name, SSDNode defaultValue) {
		return getOrDefault(name, false, false, false, defaultValue);
	}
	
	public SSDObject getObject(String name, SSDObject defaultValue) {
		return getOrDefault(name, true, false, false, defaultValue);
	}
	
	public SSDCollection getCollection(String name, SSDCollection defaultValue) {
		return getOrDefault(name, false, true, false, defaultValue);
	}
	
	public SSDFunctionCall getFunctionCall(String name, SSDFunctionCall defaultValue) {
		return getOrDefault(name, false, false, true, defaultValue);
	}
	
	public boolean getBoolean(String name, boolean defaultValue) {
		return getObjectOrDefault(name, defaultValue);
	}
	
	public byte getByte(String name, byte defaultValue) {
		return getObjectOrDefault(name, defaultValue);
	}
	
	public short getShort(String name, short defaultValue) {
		return getObjectOrDefault(name, defaultValue);
	}
	
	public int getInt(String name, int defaultValue) {
		return getObjectOrDefault(name, defaultValue);
	}
	
	public long getLong(String name, long defaultValue) {
		return getObjectOrDefault(name, defaultValue);
	}
	
	public float getFloat(String name, float defaultValue) {
		return getObjectOrDefault(name, defaultValue);
	}
	
	public double getDouble(String name, double defaultValue) {
		return getObjectOrDefault(name, defaultValue);
	}
	
	public String getString(String name, String defaultValue) {
		return getObjectOrDefault(name, defaultValue);
	}
	
	public SSDNode get(int index, SSDNode defaultValue) {
		checkIfArray();
		return getOrDefault(index, false, false, false, defaultValue);
	}
	
	public SSDObject getObject(int index, SSDObject defaultValue) {
		checkIfArray();
		return getOrDefault(index, true, false, false, defaultValue);
	}
	
	public SSDCollection getCollection(int index, SSDCollection defaultValue) {
		checkIfArray();
		return getOrDefault(index, false, true, false, defaultValue);
	}
	
	public SSDFunctionCall getFunctionCall(int index, SSDFunctionCall defaultValue) {
		checkIfArray();
		return getOrDefault(index, false, false, true, defaultValue);
	}
	
	public boolean getBoolean(int index, boolean defaultValue) {
		return getObjectOrDefault(index, defaultValue);
	}
	
	public byte getByte(int index, byte defaultValue) {
		return getObjectOrDefault(index, defaultValue);
	}
	
	public short getShort(int index, short defaultValue) {
		return getObjectOrDefault(index, defaultValue);
	}
	
	public int getInt(int index, int defaultValue) {
		return getObjectOrDefault(index, defaultValue);
	}
	
	public long getLong(int index, long defaultValue) {
		return getObjectOrDefault(index, defaultValue);
	}
	
	public float getFloat(int index, float defaultValue) {
		return getObjectOrDefault(index, defaultValue);
	}
	
	public double getDouble(int index, double defaultValue) {
		return getObjectOrDefault(index, defaultValue);
	}
	
	public String getString(int index, String defaultValue) {
		return getObjectOrDefault(index, defaultValue);
	}
	
	protected final void remove(String name, boolean checkObject, boolean checkCollection,
				boolean checkFunctionCall) {
		checkName(name);
		int nindex = name.indexOf(CHAR_NAME_DELIMITER);
		int aindex = name.indexOf(CHAR_ANNOTATION_DELIMITER);
		int dindex = name.indexOf(CHAR_AND_DELIMITER);
		int oindex = name.indexOf(CHAR_OR_DELIMITER);
		if((dindex > -1)) {
			String  name0 = name.substring(0, dindex);
			boolean item0 = has(name0, checkObject, checkCollection, checkFunctionCall);
			if((item0)) {
				remove(name0, checkObject, checkCollection, checkFunctionCall);
			}
			String  name1 = name.substring(dindex+1);
			boolean item1 = has(name1, checkObject, checkCollection, checkFunctionCall);
			if((item1)) {
				remove(name1, checkObject, checkCollection, checkFunctionCall);
			}
		} else if((oindex > -1)) {
			String  name0 = name.substring(0, oindex);
			boolean item0 = has(name0, checkObject, checkCollection, checkFunctionCall);
			if((item0)) {
				remove(name0, checkObject, checkCollection, checkFunctionCall);
			} else {
				String  name1 = name.substring(oindex+1);
				boolean item1 = has(name1, checkObject, checkCollection, checkFunctionCall);
				if((item1)) {
					remove(name1, checkObject, checkCollection, checkFunctionCall);
				}
			}
		} else {
			if((nindex > -1)) {
				if((aindex > -1 && aindex < nindex)) {
					String nname = name.substring(0, aindex);
					String aname = name.substring(aindex+1, nindex);
					String kname = name.substring(nindex+1);
					get(nname).getAnnotation(aname)
							  .remove(kname, checkObject,
							          checkCollection,
							          checkFunctionCall);
				} else {
					String cname = name.substring(0, nindex);
					String oname = name.substring(nindex+1);
					getCollection(cname).remove(oname, checkObject,
					                            checkCollection,
					                            checkFunctionCall);
				}
			} else if((aindex > -1)) {
				String nname = name.substring(0, aindex);
				String aname = name.substring(aindex+1);
				SSDNode node = get(nname);
				if((node instanceof SSDObject)) {
					((SSDObject)     node).removeAnnotation(aname);
				} else if((node instanceof SSDCollection)) {
					((SSDCollection) node).removeAnnotation(aname);
				}
			} else {
				SSDNode node = objects.get(name);
				if(checkObject
						&& !(node instanceof SSDObject))
					throw new TypeMismatchException(
						"Object " + name + " is not a SSDObject!");
				if(checkCollection
						&& !(node instanceof SSDCollection))
					throw new TypeMismatchException(
						"Object " + name + " is not a SSDCollection!");
				if(checkFunctionCall
						&& !(node instanceof SSDFunctionCall))
					throw new TypeMismatchException(
						"Object " + name + " is not a SSDFunctionCall!");
				objects.remove(name);
				if(isArray) {
					// Recreate objects to ensure that indexes are correct
					int counter = 0;
					Map<String, SSDNode> nodes = new LinkedHashMap<>();
					for(SSDNode n : objects.values()) {
						nodes.put(Integer.toString(counter++), n);
					}
					objects.clear();
					objects.putAll(nodes);
				}
			}
		}
	}
	
	// used especially for JSON
	protected final void removeDirect(String name, boolean checkObject, boolean checkCollection,
			boolean checkFunctionCall) {
		checkName(name);
		SSDNode node = objects.get(name);
		if(checkObject
				&& !(node instanceof SSDObject))
			throw new TypeMismatchException(
				"Object " + name + " is not a SSDObject!");
		if(checkCollection
				&& !(node instanceof SSDCollection))
			throw new TypeMismatchException(
				"Object " + name + " is not a SSDCollection!");
		if(checkFunctionCall
				&& !(node instanceof SSDFunctionCall))
			throw new TypeMismatchException(
				"Object " + name + " is not a SSDFunctionCall!");
		objects.remove(name);
		if(isArray) {
			// Recreate objects to ensure that indexes are correct
			int counter = 0;
			Map<String, SSDNode> nodes = new LinkedHashMap<>();
			for(SSDNode n : objects.values()) {
				nodes.put(Integer.toString(counter++), n);
			}
			objects.clear();
			objects.putAll(nodes);
		}
	}
	
	public void removeDirect(String name) {
		removeDirect(name, false, false, false);
	}
	
	public void removeDirectObject(String name) {
		removeDirect(name, true, false, false);
	}
	
	public void removeDirectCollection(String name) {
		removeDirect(name, false, true, false);
	}
	
	public void removeDirectFunctionCall(String name) {
		removeDirect(name, false, false, true);
	}
	
	public void remove(String name) {
		remove(name, false, false, false);
	}
	
	public void removeObject(String name) {
		remove(name, true, false, false);
	}
	
	public void removeCollection(String name) {
		remove(name, false, true, false);
	}
	
	public void removeFunctionCall(String name) {
		remove(name, false, false, true);
	}
	
	public void remove(int index) {
		checkIfArray();
		removeDirect(Integer.toString(index), false, false, false);
	}
	
	public void removeObject(int index) {
		checkIfArray();
		removeDirect(Integer.toString(index), true, false, false);
	}
	
	public void removeCollection(int index) {
		checkIfArray();
		removeDirect(Integer.toString(index), false, true, false);
	}
	
	public void removeFunctionCall(int index) {
		checkIfArray();
		removeDirect(Integer.toString(index), false, false, true);
	}
	
	protected final boolean has(String name, boolean checkObject, boolean checkCollection,
				boolean checkFunctionCall) {
		checkName(name);
		int nindex = name.indexOf(CHAR_NAME_DELIMITER);
		int aindex = name.indexOf(CHAR_ANNOTATION_DELIMITER);
		int dindex = name.indexOf(CHAR_AND_DELIMITER);
		int oindex = name.indexOf(CHAR_OR_DELIMITER);
		if((dindex > -1)) {
			String  name0 = name.substring(0, dindex);
			boolean item0 = has(name0, checkObject, checkCollection, checkFunctionCall);
			String  name1 = name.substring(dindex+1);
			boolean item1 = has(name1, checkObject, checkCollection, checkFunctionCall);
			return  item0 && item1;
		} else if((oindex > -1)) {
			String  name0 = name.substring(0, oindex);
			boolean item0 = has(name0, checkObject, checkCollection, checkFunctionCall);
			if((item0)) return true;
			String  name1 = name.substring(oindex+1);
			boolean item1 = has(name1, checkObject, checkCollection, checkFunctionCall);
			return  item1;
		} else {
			if((nindex > -1)) {
				if((aindex > -1 && aindex < nindex)) {
					String nname = name.substring(0, aindex);
					String aname = name.substring(aindex+1, nindex);
					String kname = name.substring(nindex+1);
					SSDAnnotation ann;
					if(!has(nname, false, false, false) // Check first, so no error is thrown
							|| !(ann = get(nname).getAnnotation(aname)).hasAnnotation(aname))
						return false;
					// Finally check solely on the annotation itself
					return ann.has(kname, checkObject,
					               checkCollection,
					               checkFunctionCall);
				} else {
					String cname = name.substring(0, nindex);
					String oname = name.substring(nindex+1);
					return has(cname, false, false, false) // Check first, so no error is thrown
								&& getCollection(cname).has(oname, checkObject,
								                            checkCollection,
								                            checkFunctionCall);
				}
			} else if((aindex > -1)) {
				String nname = name.substring(0, aindex);
				String aname = name.substring(aindex+1);
				if((checkObject))
					throw new NotFoundException("Object " + name + " is not a SSDObject!");
				return has(nname) // Check first, so no error is thrown
							&& get(nname).hasAnnotation(aname);
			} else {
				SSDNode node = objects.get(name);
				if((node == null))
					return false;
				if((checkObject
						&& !(node instanceof SSDObject)))
					throw new TypeMismatchException(
						"Object " + name + " is not a SSDObject!");
				if((checkCollection
						&& !(node instanceof SSDCollection)))
					throw new TypeMismatchException(
						"Object " + name + " is not a SSDCollection!");
				if((checkFunctionCall
						&& !(node instanceof SSDFunctionCall)))
					throw new TypeMismatchException(
						"Object " + name + " is not a SSDFunctionCall!");
				return true;
			}
		}
	}
	
	// used especially for JSON
	protected final boolean hasDirect(String name, boolean checkObject, boolean checkCollection,
			boolean checkFunctionCall) {
		checkName(name);
		SSDNode node = objects.get(name);
		if((checkObject 	  && !(node instanceof SSDObject))     ||
		   (checkCollection   && !(node instanceof SSDCollection)) ||
		   (checkFunctionCall && !(node instanceof SSDFunctionCall)))
			return false;
		return node != null;
	}
	
	public boolean hasDirect(String name) {
		return hasDirect(name, false, false, false);
	}
	
	public boolean hasDirectObject(String name) {
		return hasDirect(name, true, false, false);
	}
	
	public boolean hasDirectCollection(String name) {
		return hasDirect(name, false, true, false);
	}
	
	public boolean hasDirectFunctionCall(String name) {
		return hasDirect(name, false, false, true);
	}
	
	public boolean hasDirectNull(String name) {
		return hasDirectObject(name)
					&& getDirectObject(name).getType() == SSDType.NULL;
	}
	
	public boolean hasDirectBoolean(String name) {
		return hasDirectObject(name)
					&& getDirectObject(name).getType() == SSDType.BOOLEAN;
	}
	
	public boolean hasDirectInteger(String name) {
		return hasDirectObject(name)
					&& getDirectObject(name).getType() == SSDType.INTEGER;
	}
	
	public boolean hasDirectDecimal(String name) {
		return hasDirectObject(name)
					&& getDirectObject(name).getType() == SSDType.DECIMAL;
	}
	
	public boolean hasDirectString(String name) {
		return hasDirectObject(name)
					&& getDirectObject(name).getType() == SSDType.STRING;
	}
	
	public boolean has(String name) {
		return has(name, false, false, false);
	}
	
	public boolean hasObject(String name) {
		return has(name, true, false, false);
	}
	
	public boolean hasCollection(String name) {
		return has(name, false, true, false);
	}
	
	public boolean hasFunctionCall(String name) {
		return has(name, false, false, true);
	}
	
	public boolean hasNull(String name) {
		return hasObject(name)
					&& getObject(name).getType() == SSDType.NULL;
	}
	
	public boolean hasBoolean(String name) {
		return hasObject(name)
					&& getObject(name).getType() == SSDType.BOOLEAN;
	}
	
	public boolean hasInteger(String name) {
		return hasObject(name)
					&& getObject(name).getType() == SSDType.INTEGER;
	}
	
	public boolean hasDecimal(String name) {
		return hasObject(name)
					&& getObject(name).getType() == SSDType.DECIMAL;
	}
	
	public boolean hasString(String name) {
		return hasObject(name)
					&& getObject(name).getType() == SSDType.STRING;
	}
	
	public boolean has(int index) {
		checkIfArray();
		return hasDirect(Integer.toString(index), false, false, false);
	}
	
	public boolean hasObject(int index) {
		checkIfArray();
		return hasDirect(Integer.toString(index), true, false, false);
	}
	
	public boolean hasCollection(int index) {
		checkIfArray();
		return hasDirect(Integer.toString(index), false, true, false);
	}
	
	public boolean hasFunctionCall(int index) {
		checkIfArray();
		return hasDirect(Integer.toString(index), false, false, true);
	}
	
	public boolean hasNull(int index) {
		return hasObject(index)
					&& getObject(index).getType() == SSDType.NULL;
	}
	
	public boolean hasBoolean(int index) {
		return hasObject(index)
					&& getObject(index).getType() == SSDType.BOOLEAN;
	}
	
	public boolean hasInteger(int index) {
		return hasObject(index)
					&& getObject(index).getType() == SSDType.INTEGER;
	}
	
	public boolean hasDecimal(int index) {
		return hasObject(index)
					&& getObject(index).getType() == SSDType.DECIMAL;
	}
	
	public boolean hasString(int index) {
		return hasObject(index)
					&& getObject(index).getType() == SSDType.STRING;
	}
	
	protected final void set(String name, SSDType type, Object value) {
		checkName(name);
		int nindex = name.indexOf(CHAR_NAME_DELIMITER);
		int aindex = name.indexOf(CHAR_ANNOTATION_DELIMITER);
		int dindex = name.indexOf(CHAR_AND_DELIMITER);
		int oindex = name.indexOf(CHAR_OR_DELIMITER);
		if((dindex > -1)) {
			String  name0 = name.substring(0, dindex);
			boolean item0 = has(name0, false, false, false);
			if((item0)) {
				set(name0, type, value);
			}
			String  name1 = name.substring(dindex+1);
			boolean item1 = has(name1, false, false, false);
			if((item1)) {
				set(name1, type, value);
			}
		} else if((oindex > -1)) {
			String  name0 = name.substring(0, oindex);
			boolean item0 = has(name0, false, false, false);
			if((item0)) {
				set(name0, type, value);
			} else {
				String  name1 = name.substring(oindex+1);
				boolean item1 = has(name1, false, false, false);
				if((item1)) {
					set(name1, type, value);
				}
			}
		} else {
			if((nindex > -1)) {
				if((aindex > -1 && aindex < nindex)) {
					String nname = name.substring(0, aindex);
					String aname = name.substring(aindex+1, nindex);
					String kname = name.substring(nindex+1);
					SSDNode node = get(nname);
					// When object (collection) does not exist, create one
					if((node == null)) {
						node = new SSDObject(this, nname, WORD_NULL);
						objects.put(nname, node);
					}
					SSDAnnotation ann = node.getAnnotation(aname);
					// When annotation does not exist, create one
					if((ann == null)) {
						ann = new SSDAnnotation(aname);
						if((node instanceof SSDObject)) {
							((SSDObject)     node).addAnnotation(ann);
						} else if((node instanceof SSDCollection)) {
							((SSDCollection) node).addAnnotation(ann);
						}
					}
					ann.set(kname, type, value);
				} else {
					String cname = name.substring(0, nindex);
					String oname = name.substring(nindex+1);
					SSDNode node = get(cname);
					// When collection does not exist, create one
					if((node == null || !(node instanceof SSDCollection))) {
						String k = oname;
						int    i = oname.indexOf(CHAR_NAME_DELIMITER);
						// Determine if the collection should be array or not
						if((i) > -1) k = k.substring(0, i);
						boolean array  = k.matches("\\d+");
						node = new SSDCollection(this, cname, array);
						objects.put(cname, node);
					}
					((SSDCollection) node).set(oname, type, value);
				}
			} else if((aindex > -1)) {
				if((value instanceof SSDAnnotation)) {
					String nname = name.substring(0, aindex);
					String aname = name.substring(aindex+1);
					SSDNode node = get(nname);
					// When object (collection) does not exist, create one
					if((node == null)) {
						node = new SSDObject(this, nname, WORD_NULL);
						objects.put(nname, node);
					}
					if((node instanceof SSDObject)) {
						SSDObject 	  n = (SSDObject)     node;
						SSDAnnotation a = (SSDAnnotation) value;
						// Remove any previous annotation
						if((n.getAnnotation   (aname) != null))
							n.removeAnnotation(aname);
						a.name  .set(aname);
						a.parent.set(this);
						n.addAnnotation(a);
					} else if((node instanceof SSDCollection)) {
						SSDCollection n = (SSDCollection) node;
						SSDAnnotation a = (SSDAnnotation) value;
						// Remove any previous annotation
						if((n.getAnnotation   (aname) != null))
							n.removeAnnotation(aname);
						a.name  .set(aname);
						a.parent.set(this);
						n.addAnnotation(a);
					}
				} else {
					throw new TypeMismatchException("Value is not a SSDAnnotation!");
				}
			} else {
				SSDNode node = null;
				if((value instanceof SSDCollection)) {
					SSDCollection n = (SSDCollection) value;
					n.name  .set(name);
					n.parent.set(this);
					node = n;
				} else if(value instanceof SSDObject) {
					SSDObject n = (SSDObject) value;
					n.name  .set(name);
					n.parent.set(this);
					node = n;
				} else if(value instanceof String) {
					node = type.createObject(this, name, (String) value);
				}
				if((node != null)) objects.put(name, node);
			}
		}
	}
	
	// used especially for JSON
	protected final void setDirect(String name, SSDType type, Object value) {
		checkName(name);
		SSDNode node = null;
		if((value instanceof SSDCollection)) {
			SSDCollection n = (SSDCollection) value;
			n.name  .set(name);
			n.parent.set(this);
			node = n;
		} else if(value instanceof SSDObject) {
			SSDObject n = (SSDObject) value;
			n.name  .set(name);
			n.parent.set(this);
			node = n;
		} else if(value instanceof String) {
			node = type.createObject(this, name, (String) value);
		}
		if((node != null)) objects.put(name, node);
	}
	
	public void setDirectNull(String name) {
		setDirect(name, SSDType.NULL, WORD_NULL);
	}
	
	public void setDirect(String name, boolean value) {
		setDirect(name, SSDType.BOOLEAN, Boolean.toString(value));
	}
	
	public void setDirect(String name, byte value) {
		setDirect(name, SSDType.INTEGER, Byte.toString(value));
	}
	
	public void setDirect(String name, short value) {
		setDirect(name, SSDType.INTEGER, Short.toString(value));
	}
	
	public void setDirect(String name, int value) {
		setDirect(name, SSDType.INTEGER, Integer.toString(value));
	}
	
	public void setDirect(String name, long value) {
		setDirect(name, SSDType.INTEGER, Long.toString(value));
	}
	
	public void setDirect(String name, float value) {
		setDirect(name, SSDType.DECIMAL, Float.toString(value));
	}
	
	public void setDirect(String name, double value) {
		setDirect(name, SSDType.DECIMAL, Double.toString(value));
	}
	
	public void setDirect(String name, String value) {
		setDirect(name, SSDType.STRING, value);
	}
	
	public void setDirect(String name, SSDObject object) {
		setDirect(name, SSDType.UNKNOWN, object);
	}
	
	public void setDirect(String name, SSDCollection collection) {
		setDirect(name, SSDType.UNKNOWN, collection);
	}
	
	public void setDirect(String name, SSDFunctionCall funcCall) {
		setDirect(name, SSDType.UNKNOWN, funcCall);
	}
	
	public void setNull(String name) {
		set(name, SSDType.NULL, WORD_NULL);
	}
	
	public void set(String name, boolean value) {
		set(name, SSDType.BOOLEAN, Boolean.toString(value));
	}
	
	public void set(String name, byte value) {
		set(name, SSDType.INTEGER, Byte.toString(value));
	}
	
	public void set(String name, short value) {
		set(name, SSDType.INTEGER, Short.toString(value));
	}
	
	public void set(String name, int value) {
		set(name, SSDType.INTEGER, Integer.toString(value));
	}
	
	public void set(String name, long value) {
		set(name, SSDType.INTEGER, Long.toString(value));
	}
	
	public void set(String name, float value) {
		set(name, SSDType.DECIMAL, Float.toString(value));
	}
	
	public void set(String name, double value) {
		set(name, SSDType.DECIMAL, Double.toString(value));
	}
	
	public void set(String name, String value) {
		set(name, SSDType.STRING, value);
	}
	
	public void set(String name, SSDObject object) {
		set(name, SSDType.UNKNOWN, object);
	}
	
	public void set(String name, SSDCollection collection) {
		set(name, SSDType.UNKNOWN, collection);
	}
	
	public void set(String name, SSDFunctionCall funcCall) {
		set(name, SSDType.UNKNOWN, funcCall);
	}
	
	public void setNull(int index) {
		checkIfArray();
		setDirect(Integer.toString(index), SSDType.NULL, WORD_NULL);
	}
	
	public void set(int index, boolean value) {
		checkIfArray();
		setDirect(Integer.toString(index), SSDType.BOOLEAN, Boolean.toString(value));
	}
	
	public void set(int index, byte value) {
		checkIfArray();
		setDirect(Integer.toString(index), SSDType.INTEGER, Byte.toString(value));
	}
	
	public void set(int index, short value) {
		checkIfArray();
		setDirect(Integer.toString(index), SSDType.INTEGER, Short.toString(value));
	}
	
	public void set(int index, int value) {
		checkIfArray();
		setDirect(Integer.toString(index), SSDType.INTEGER, Integer.toString(value));
	}
	
	public void set(int index, long value) {
		checkIfArray();
		setDirect(Integer.toString(index), SSDType.INTEGER, Long.toString(value));
	}
	
	public void set(int index, float value) {
		checkIfArray();
		setDirect(Integer.toString(index), SSDType.DECIMAL, Float.toString(value));
	}
	
	public void set(int index, double value) {
		checkIfArray();
		setDirect(Integer.toString(index), SSDType.DECIMAL, Double.toString(value));
	}
	
	public void set(int index, String value) {
		checkIfArray();
		setDirect(Integer.toString(index), SSDType.STRING, value);
	}
	
	public void set(int index, SSDObject object) {
		checkIfArray();
		setDirect(Integer.toString(index), SSDType.UNKNOWN, object);
	}
	
	public void set(int index, SSDCollection collection) {
		checkIfArray();
		setDirect(Integer.toString(index), SSDType.UNKNOWN, collection);
	}
	
	public void set(int index, SSDFunctionCall funcCall) {
		checkIfArray();
		setDirect(Integer.toString(index), SSDType.UNKNOWN, funcCall);
	}
	
	public void addNull(String name) {
		set(name, SSDType.NULL, WORD_NULL);
	}
	
	public void add(String name, boolean value) {
		set(name, SSDType.BOOLEAN, Boolean.toString(value));
	}
	
	public void add(String name, byte value) {
		set(name, SSDType.INTEGER, Byte.toString(value));
	}
	
	public void add(String name, short value) {
		set(name, SSDType.INTEGER, Short.toString(value));
	}
	
	public void add(String name, int value) {
		set(name, SSDType.INTEGER, Integer.toString(value));
	}
	
	public void add(String name, long value) {
		set(name, SSDType.INTEGER, Long.toString(value));
	}
	
	public void add(String name, float value) {
		set(name, SSDType.DECIMAL, Float.toString(value));
	}
	
	public void add(String name, double value) {
		set(name, SSDType.DECIMAL, Double.toString(value));
	}
	
	public void add(String name, String value) {
		set(name, SSDType.STRING, value);
	}
	
	public void addNull() {
		checkIfArray();
		setDirect(Integer.toString(nextIndex()), SSDType.NULL, WORD_NULL);
	}
	
	public void add(boolean value) {
		checkIfArray();
		setDirect(Integer.toString(nextIndex()), SSDType.BOOLEAN, Boolean.toString(value));
	}
	
	public void add(byte value) {
		checkIfArray();
		setDirect(Integer.toString(nextIndex()), SSDType.INTEGER, Byte.toString(value));
	}
	
	public void add(short value) {
		checkIfArray();
		setDirect(Integer.toString(nextIndex()), SSDType.INTEGER, Short.toString(value));
	}
	
	public void add(int value) {
		checkIfArray();
		setDirect(Integer.toString(nextIndex()), SSDType.INTEGER, Integer.toString(value));
	}
	
	public void add(long value) {
		checkIfArray();
		setDirect(Integer.toString(nextIndex()), SSDType.INTEGER, Long.toString(value));
	}
	
	public void add(float value) {
		checkIfArray();
		setDirect(Integer.toString(nextIndex()), SSDType.DECIMAL, Float.toString(value));
	}
	
	public void add(double value) {
		checkIfArray();
		setDirect(Integer.toString(nextIndex()), SSDType.DECIMAL, Double.toString(value));
	}
	
	public void add(String value) {
		checkIfArray();
		setDirect(Integer.toString(nextIndex()), SSDType.STRING, value);
	}
	
	public void add(SSDObject object) {
		checkIfArray();
		setDirect(isArray ? Integer.toString(nextIndex())
		                  : object != null
		                  		? object.getName()
		                  		: Integer.toString(nextIndex()),
		    SSDType.UNKNOWN, object);
	}
	
	public void add(SSDCollection collection) {
		checkIfArray();
		setDirect(isArray ? Integer.toString(nextIndex())
		                  : collection != null
		                  		? collection.getName()
		                  		: Integer.toString(nextIndex()),
		    SSDType.UNKNOWN, collection);
	}
	
	public void add(SSDFunctionCall funcCall) {
		checkIfArray();
		setDirect(isArray ? Integer.toString(nextIndex())
		                  : funcCall != null
		                  		? funcCall.getName()
		                  		: Integer.toString(nextIndex()),
		    SSDType.UNKNOWN, funcCall);
	}
	
	public int length() {
		checkIfArray();
		return objects.size();
	}
	
	@Override
	public boolean isObject() {
		return false;
	}
	
	@Override
	public boolean isCollection() {
		return true;
	}
	
	public SSDCollectionType getType() {
		return isArray ? SSDCollectionType.ARRAY
		               : SSDCollectionType.OBJECT;
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
	
	// Utility class
	static final class TypeFunctions {
		
		private static Function<SSDNode, Boolean> FUNCTION_NODES;
		private static Function<SSDNode, Boolean> FUNCTION_COLLECTIONS;
		private static Function<SSDNode, Boolean> FUNCTION_OBJECTS;
		
		public static Function<SSDNode, Boolean> nodes() {
			if((FUNCTION_NODES == null)) {
				FUNCTION_NODES = ((item) -> true);
			}
			return FUNCTION_NODES;
		}
		
		public static Function<SSDNode, Boolean> collections() {
			if((FUNCTION_COLLECTIONS == null)) {
				FUNCTION_COLLECTIONS = ((item) -> item.isCollection());
			}
			return FUNCTION_COLLECTIONS;
		}
		
		public static Function<SSDNode, Boolean> objects() {
			if((FUNCTION_OBJECTS == null)) {
				FUNCTION_OBJECTS = ((item) -> item.isObject());
			}
			return FUNCTION_OBJECTS;
		}
	}
	
	// Utility method
	@SuppressWarnings("unchecked")
	static final <T extends SSDNode> Collection<T> toTypeCollection(Collection<SSDNode> coll,
	        Class<? extends Collection<?>> clazz, Function<SSDNode, Boolean> cond) {
		Collection<T> data;
		if((clazz == Set .class)) data = new LinkedHashSet<>(); else
		if((clazz == List.class)) data = new ArrayList<>();     else
		throw new UnsupportedOperationException("Unsupported Collection: " + clazz);
		for(SSDNode item : coll)
			if(cond.apply(item)) data.add((T) item);
		return data;
	}
	
	// Utility method
	@SuppressWarnings("unchecked")
	static final <T extends SSDNode> List<T> toTypeList(Collection<SSDNode> coll,
			Function<SSDNode, Boolean> cond) {
		return (List<T>) toTypeCollection(coll, (Class<? extends Collection<?>>) List.class, cond);
	}
	
	// Utility method
	@SuppressWarnings("unchecked")
	static final <T extends SSDNode> Set<T> toTypeSet(Collection<SSDNode> coll,
			Function<SSDNode, Boolean> cond) {
		return (Set<T>) toTypeCollection(coll, (Class<? extends Collection<?>>) Set.class, cond);
	}
	
	// Utility method
	@SuppressWarnings("unchecked")
	static final <T extends SSDNode> T[] toTypeArray(Collection<SSDNode> coll,
			Class<T> clazz, Function<SSDNode, Boolean> cond) {
		Set<T> data  = toTypeSet(coll, cond);
		T[]    array = (T[]) Array.newInstance(clazz, data.size());
		return data.toArray(array); // Just return the casted array
	}
	
	public SSDNode[] toNodeArray() {
		return toTypeArray(objects.values(), SSDNode.class, TypeFunctions.nodes());
	}
	
	public SSDCollection[] toCollectionArray() {
		return toTypeArray(objects.values(), SSDCollection.class, TypeFunctions.collections());
	}
	
	public SSDObject[] toObjectArray() {
		return toTypeArray(objects.values(), SSDObject.class, TypeFunctions.objects());
	}
	
	public Set<SSDNode> toNodeSet() {
		return toTypeSet(objects.values(), TypeFunctions.nodes());
	}
	
	public Set<SSDCollection> toCollectionSet() {
		return toTypeSet(objects.values(), TypeFunctions.collections());
	}
	
	public Set<SSDObject> toObjectSet() {
		return toTypeSet(objects.values(), TypeFunctions.objects());
	}
	
	public List<SSDNode> toNodeList() {
		return toTypeList(objects.values(), TypeFunctions.nodes());
	}
	
	public List<SSDCollection> toCollectionList() {
		return toTypeList(objects.values(), TypeFunctions.collections());
	}
	
	public List<SSDObject> toObjectList() {
		return toTypeList(objects.values(), TypeFunctions.objects());
	}
	
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
	public SSDCollection copy() {
		// Copy properly all the collection's objects
		Map<String, SSDNode> copyObj = new LinkedHashMap<>();
		for(Entry<String, SSDNode> e : objects.entrySet()) {
			String  name 	 = e.getKey();
			SSDNode node 	 = e.getValue();
			SSDNode copyNode = node.copy();
			if((copyNode != null))
				copyObj.put(name, copyNode);
		}
		// Copy properly all the annotations
		Set<SSDAnnotation> copyAnn = new LinkedHashSet<>();
		for(SSDAnnotation a : annotations)
			// Use the annotation copy function
			copyAnn.add(a.copy());
		// Copy properly all the comments
		Set<SSDComment> copyCmt = new LinkedHashSet<>();
		for(SSDComment c : comments)
			copyCmt.add(c.copy());
		return new SSDCollection(getParent(), getName(), isArray, copyObj, copyAnn, copyCmt);
	}
	
	@Override
	public boolean equals(Object obj) {
		if((obj == null
				|| !(obj instanceof SSDCollection)))
			return false;
		SSDCollection coll = (SSDCollection) obj;
		if((coll.isArray != isArray))
			return false;
		if(!coll.getName().equals(name.get()))
			return false;
		Map<String, SSDNode> objs = coll.objects;
		if((objs.size() != objects.size()))
			return false;
		for(Entry<String, SSDNode> e : objs.entrySet()) {
			String  name = e.getKey();
			SSDNode node = e.getValue();
			SSDNode eqnd;
			if((eqnd = objects.get(name)) != null) {
				if(!eqnd.equals(node))
					return false;
			} else {
				// Has to contain the object
				return false;
			}
		}
		// All good, both collections contains the same stuff
		return true;
	}
	
	public SSDCollection filter(SSDFilter filter) {
		SSDCollection cl = empty(isArray);
		for(SSDNode node : this) {
			// Filter each node
			if(filter.accept(node)) {
				String name = node.getName();
				if(node.isCollection()) cl.set(name, (SSDCollection) node);
				else 					cl.set(name, (SSDObject) 	 node);
			}
		}
		return cl;
	}
	
	static final String tabString(int level) {
		StringBuilder sb = new StringBuilder();
		for(int i = 0; i < level; ++i)
			sb.append(CHAR_TAB);
		return sb.toString();
	}
	
	String toString(int depth, boolean compress, boolean json, boolean invoke,
			boolean info, boolean comments) {
		String 		  tabStr = tabString(depth);
		String		  ndtStr = tabStr + CHAR_TAB;
		StringBuilder buffer = new StringBuilder();
		// Append all annotations
		if((info && !annotations.isEmpty() && !json)) {
			boolean annf = true;
			for(SSDAnnotation ann : annotations) {
				if(annf) 	  annf = false; else
				if(!compress) buffer.append(CHAR_NEWLINE);
				buffer.append(json ? ann.toJSON(depth, compress, invoke)
				                   : ann.toString(depth, compress, invoke, comments));
			}
			if(!compress) {
				buffer.append(CHAR_NEWLINE);
				buffer.append(tabStr);
			}
		}
		// Append all comments
		if((comments && !this.comments.isEmpty() && !json
				&& getName() == null)) { // only the parent object
			boolean cmtf = true;
			for(SSDComment cmt : this.comments) {
				if(cmtf) cmtf = false; else
				if(!compress) buffer.append(CHAR_NEWLINE);
				buffer.append(cmt.toString(0, compress));
			}
			if(!compress) {
				buffer.append(CHAR_NEWLINE);
				buffer.append(tabStr);
			}
		}
		buffer.append(isArray ? CHAR_ARRAY_OB : CHAR_OBJECT_OB);
		Collection<SSDNode> values;
		if(!(values = objects.values()).isEmpty()) {
			boolean first = true;
			for(SSDNode node : values) {
				if((first)) {
					if(!compress) {
						buffer.append(CHAR_NEWLINE);
					}
					first = false;
				} else {
					buffer.append(CHAR_ITEM_DELIMITER);
					if(!compress) {
						buffer.append(CHAR_NEWLINE);
					}
				}
				// Append all node's annotations
				if(!json) {
					SSDAnnotation[] anns;
					if((anns = node.getAnnotations()).length > 0) {
						boolean annf = true;
						for(SSDAnnotation ann : anns) {
							if(annf) 	  annf = false; else
							if(!compress) buffer.append(CHAR_NEWLINE);
							if(!compress) buffer.append(ndtStr);
							buffer.append(json ? ann.toJSON(depth, compress, invoke)
							                   : ann.toString(depth, compress, invoke, comments));
						}
						if(!compress) buffer.append(CHAR_NEWLINE);
					}
				}
				// Append all node's comments
				if((comments && !json)) {
					SSDComment[] cmts;
					if((cmts = node.getComments()).length > 0) {
						boolean cmtf = true;
						for(SSDComment cmt : cmts) {
							if(cmtf) cmtf = false; else
							if(!compress) buffer.append(CHAR_NEWLINE);
							buffer.append(cmt.toString(depth+1, compress));
						}
						if(!compress) buffer.append(CHAR_NEWLINE);
					}
				}
				if(!compress) buffer.append(ndtStr);
				if(!isArray) {
					if(json) buffer.append(CHAR_DOUBLE_QUOTES);
					buffer.append(node.getName());
					if(json) buffer.append(CHAR_DOUBLE_QUOTES);
					buffer.append(CHAR_NV_DELIMITER);
					if(!compress) {
						buffer.append(CHAR_SPACE);
					}
				}
				if((node instanceof SSDCollection)) {
					SSDCollection coll = (SSDCollection) node;
					buffer.append(coll.toString(depth+1, compress, json, invoke, false, comments));
				} else {
					buffer.append(json ? node.toJSON(depth+1, compress, invoke)
					                   : node.toString(depth+1, compress, invoke, comments));
				}
			}
			if(!compress) {
				buffer.append(CHAR_NEWLINE);
				buffer.append(tabStr);
			}
		}
		buffer.append(isArray ? CHAR_ARRAY_CB : CHAR_OBJECT_CB);
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
	
	final Iterator<SSDNode> valuesIterator() {
		return objects.values().iterator();
	}
	
	@Override
	public Iterator<SSDNode> iterator() {
		return new SSDCollectionIterators.SSDNodeIterator(valuesIterator());
	}
	
	final <T> Function<SSDNode, T> genericTypeConverter() {
		return ((i) -> {
			@SuppressWarnings("unchecked")
			T a = (T) i; return a; // just cast the item
		});
	}
	
	public <T> Iterator<T> iterator(SSDFilter filter) {
		return iterator(filter, genericTypeConverter());
	}
	
	public <T> Iterator<T> iterator(SSDFilter filter, Function<SSDNode, T> convertor) {
		return new SSDCollectionIterators
						.GenericBiTypeIterator<SSDNode, T>(valuesIterator()) {
			
			@Override
			protected boolean isOfCorrectType(SSDNode node) {
				return filter.accept(node);
			}
			
			@Override
			protected T castToCorrectType(SSDNode node) {
				return convertor.apply(node);
			}
		};
	}
	
	public Iterator<SSDObject> objectsIterator() {
		return new SSDCollectionIterators.SSDObjectIterator(valuesIterator());
	}
	
	public Iterator<SSDCollection> collectionsIterator() {
		return new SSDCollectionIterators.SSDCollectionIterator(valuesIterator());
	}
	
	public <T> Iterable<T> iterable(SSDFilter filter) {
		return iterable(filter, genericTypeConverter());
	}
	
	public <T> Iterable<T> iterable(SSDFilter filter, Function<SSDNode, T> convertor) {
		return (() -> iterator(filter, convertor));
	}
	
	public Iterable<SSDObject> objectsIterable() {
		return iterable(SSDFilter.ONLY_OBJECTS);
	}
	
	public Iterable<SSDCollection> collectionsIterable() {
		return iterable(SSDFilter.ONLY_COLLECTIONS);
	}
	
	static final <T> Collection<T> iterableToCollection(Iterable<T> iterable) {
		List<T> collection = new ArrayList<>();
		iterableToCollection(collection, iterable);
		return collection;
	}
	
	static final <T> void iterableToCollection(Collection<T> collection, Iterable<T> iterable) {
		iterable.forEach(collection::add);
	}
	
	public Collection<SSDNode> nodes() {
		return Collections.unmodifiableCollection(iterableToCollection(this));
	}
	
	public Collection<SSDObject> objects() {
		return Collections.unmodifiableCollection(iterableToCollection(objectsIterable()));
	}
	
	public Collection<SSDCollection> collections() {
		return Collections.unmodifiableCollection(iterableToCollection(collectionsIterable()));
	}
	
	private static final class SSDCollectionIterators {
		
		static final class SSDNodeIterator implements Iterator<SSDNode> {
			
			final Iterator<SSDNode> iterator;
			SSDNodeIterator(Iterator<SSDNode> theIterator) {
				iterator = theIterator;
			}
			
			@Override
			public boolean hasNext() {
				return iterator.hasNext();
			}
			
			@Override
			public SSDNode next() {
				return iterator.next();
			}
		}
		
		static abstract class GenericBiTypeIterator<A, B> implements Iterator<B> {
			
			final Iterator<A> iterator;
			boolean hasNext;
			A lastItem;
			
			GenericBiTypeIterator(Iterator<A> theIterator) {
				iterator = theIterator;
			}
			
			protected abstract boolean isOfCorrectType(A node);
			protected abstract B castToCorrectType(A node);
			
			@Override
			public boolean hasNext() {
				while((hasNext = iterator.hasNext())
						&& !isOfCorrectType(lastItem = iterator.next()));
				if(!hasNext) lastItem = null; // important to reset
				return hasNext && isOfCorrectType(lastItem);
			}
			
			@Override
			public B next() {
				// happens at the beginning or when out of items
				if((lastItem == null && !hasNext)) {
					// if there are items, it happened at the beginning
					if((iterator.hasNext())) {
						// return the next item
						return castToCorrectType(lastItem = iterator.next());
					}
					// otherwise follow the documentation
					throw new NoSuchElementException();
				}
				// happens when hasNext() has been called at least once
				else {
					// return the next item
					return castToCorrectType(lastItem);
				}
			}
		}
		
		static final class SSDObjectIterator
				extends GenericBiTypeIterator<SSDNode, SSDObject> {
			
			SSDObjectIterator(Iterator<SSDNode> theIterator) {
				super(theIterator);
			}
			
			@Override
			protected final boolean isOfCorrectType(SSDNode node) {
				return node instanceof SSDObject;
			}
			
			@Override
			protected final SSDObject castToCorrectType(SSDNode node) {
				return (SSDObject) node;
			}
		}
		
		static final class SSDCollectionIterator
				extends GenericBiTypeIterator<SSDNode, SSDCollection> {
			
			SSDCollectionIterator(Iterator<SSDNode> theIterator) {
				super(theIterator);
			}
			
			@Override
			protected final boolean isOfCorrectType(SSDNode node) {
				return node instanceof SSDCollection;
			}
			
			@Override
			protected final SSDCollection castToCorrectType(SSDNode node) {
				return (SSDCollection) node;
			}
		}
	}
}