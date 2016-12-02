package sune.util.ssdf2;

import static sune.util.ssdf2.SSDF.CHAR_ARRAY_CB;
import static sune.util.ssdf2.SSDF.CHAR_ARRAY_OB;
import static sune.util.ssdf2.SSDF.CHAR_DOUBLE_QUOTES;
import static sune.util.ssdf2.SSDF.CHAR_ITEM_DELIMITER;
import static sune.util.ssdf2.SSDF.CHAR_NAME_DELIMITER;
import static sune.util.ssdf2.SSDF.CHAR_NEWLINE;
import static sune.util.ssdf2.SSDF.CHAR_NV_DELIMITER;
import static sune.util.ssdf2.SSDF.CHAR_OBJECT_CB;
import static sune.util.ssdf2.SSDF.CHAR_OBJECT_OB;
import static sune.util.ssdf2.SSDF.CHAR_SPACE;
import static sune.util.ssdf2.SSDF.CHAR_TAB;
import static sune.util.ssdf2.SSDF.WORD_NULL;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class SSDCollection implements SSDNode, Iterable<SSDNode> {
	
	public static enum SSDCollectionType {
		ARRAY, OBJECT;
	}
	
	private final SSDProperty<SSDNode> parent;
	private final SSDProperty<String>  name;
	private final Map<String, SSDNode> objects;
	private final boolean 			   isArray;
	
	// Annotations
	private final List<SSDAnnotation> annotations;
	
	SSDCollection(SSDNode parent, String name, boolean isArray) {
		this(parent, name, isArray, new LinkedHashMap<>(), new ArrayList<>());
	}
	
	SSDCollection(SSDNode parent, String name, boolean isArray,
			Map<String, SSDNode> objects,
			List<SSDAnnotation> annotations) {
		checkArgs(parent, name, objects);
		this.parent  = new SSDProperty<>(parent);
		this.name 	 = new SSDProperty<>(name);
		this.isArray = isArray;
		this.objects = objects;
		// Annotations
		this.annotations = annotations;
	}
	
	static final void checkArgs(SSDNode parent, String name, Map<String, SSDNode> objects) {
		if(name == null && parent != null) {
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
			throw new RuntimeException(
				"SSDCollection is not an array!");
		}
	}
	
	void checkIfObject() {
		if(isArray) {
			throw new RuntimeException(
				"SSDCollection is not an object!");
		}
	}
	
	void checkName(String name) {
		if(name == null || name.isEmpty()) {
			throw new IllegalArgumentException(
				"Name cannot be null nor empty!");
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
	
	Map<String, SSDNode> objects() {
		return objects;
	}
	
	void addAnnotations(List<SSDAnnotation> annotations) {
		if(annotations != null) {
			this.annotations.addAll(annotations);
		}
	}
	
	void addAnnotation(SSDAnnotation annotation) {
		if(annotation != null) {
			this.annotations.add(annotation);
		}
	}
	
	// Annotations
	List<SSDAnnotation> annotations() {
		return annotations;
	}
	
	int nextIndex() {
		return objects.size();
	}
	
	public static final SSDCollection empty() {
		return empty(false);
	}
	
	public static final SSDCollection empty(boolean isArray) {
		return new SSDCollection(null, "", isArray);
	}
	
	private final SSDNode get0(String name, boolean checkObject, boolean checkCollection) {
		checkName(name);
		// Name is a complex name, that means that the object
		// is not in this array but somewhere in its arrays
		int index;
		if((index = name.indexOf(CHAR_NAME_DELIMITER)) > -1) {
			// Get name of the array
			String aname = name.substring(0, index);
			String rname = name.substring(index+1);
			return getCollection(aname).get(rname);
		}
		// Name is a simple name
		else {
			SSDNode node = objects.get(name);
			if(checkObject && node instanceof SSDCollection) {
				throw new RuntimeException(
					"Object " + name + " is not a SSDObject!");
			}
			if(checkCollection && node instanceof SSDObject) {
				throw new RuntimeException(
					"Object " + name + " is not a SSDCollection!");
			}
			return node;
		}
	}
	
	public SSDNode get(String name) {
		checkIfObject();
		return get0(name, false, false);
	}
	
	public SSDObject getObject(String name) {
		checkIfObject();
		return (SSDObject) get0(name, true, false);
	}
	
	public SSDCollection getCollection(String name) {
		checkIfObject();
		return (SSDCollection) get0(name, false, true);
	}
	
	public SSDFunctionCall getFunctionCall(String name) {
		checkIfObject();
		return (SSDFunctionCall) get0(name, false, true);
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
		return get0(Integer.toString(index), false, false);
	}
	
	public SSDObject getObject(int index) {
		checkIfArray();
		return (SSDObject) get0(Integer.toString(index), true, false);
	}
	
	public SSDCollection getCollection(int index) {
		checkIfArray();
		return (SSDCollection) get0(Integer.toString(index), false, true);
	}
	
	public SSDFunctionCall getFunctionCall(int index) {
		checkIfArray();
		return (SSDFunctionCall) get0(Integer.toString(index), false, true);
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
	
	private final void remove(String name, boolean checkObject, boolean checkCollection,
				boolean checkFunctionCall) {
		checkName(name);
		// Name is a complex name, that means that the object
		// is not in this array but somewhere in its arrays
		int index;
		if((index = name.indexOf(CHAR_NAME_DELIMITER)) > -1) {
			// Get name of the array
			String aname = name.substring(0, index);
			String rname = name.substring(index+1);
			getCollection(aname).remove(rname, checkObject, checkCollection,
			                            checkFunctionCall);
		}
		// Name is a simple name
		else {
			SSDNode node = objects.get(name);
			if(checkObject && !(node instanceof SSDObject)) {
				throw new RuntimeException(
					"Object " + name + " is not a SSDObject!");
			}
			if(checkCollection && !(node instanceof SSDCollection)) {
				throw new RuntimeException(
					"Object " + name + " is not a SSDCollection!");
			}
			if(checkFunctionCall && !(node instanceof SSDFunctionCall)) {
				throw new RuntimeException(
					"Object " + name + " is not a SSDFunctionCall!");
			}
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
	
	public void remove(String name) {
		checkIfObject();
		remove(name, false, false, false);
	}
	
	public void removeObject(String name) {
		checkIfObject();
		remove(name, true, false, false);
	}
	
	public void removeCollection(String name) {
		checkIfObject();
		remove(name, false, true, false);
	}
	
	public void removeFunctionCall(String name) {
		checkIfObject();
		remove(name, false, false, true);
	}
	
	public void remove(int index) {
		checkIfArray();
		remove(Integer.toString(index), false, false, false);
	}
	
	public void removeObject(int index) {
		checkIfArray();
		remove(Integer.toString(index), true, false, false);
	}
	
	public void removeCollection(int index) {
		checkIfArray();
		remove(Integer.toString(index), false, true, false);
	}
	
	public void removeFunctionCall(int index) {
		checkIfArray();
		remove(Integer.toString(index), false, false, true);
	}
	
	// Only internal method
	private final boolean has0(String name) {
		return objects.containsKey(name);
	}
	
	private final boolean has(String name, boolean checkObject, boolean checkCollection,
				boolean checkFunctionCall) {
		checkName(name);
		SSDCollection node = this;
		while(!name.isEmpty()) {
			// Name is a complex name, that means that the object
			// is not in this array but somewhere in its arrays
			int index;
			if((index = name.indexOf(CHAR_NAME_DELIMITER)) > -1) {
				String aname = name.substring(0, index);
				if(!node.has0(aname)) return false;
				node = node.getCollection(aname);
				name = name.substring(index+1);
			}
			// Name is a simple name
			else {
				if(node.has0(name)) {
					SSDNode node0 = node.get(name);
					if(checkObject && !(node0 instanceof SSDObject)) {
						throw new RuntimeException(
							"Object " + name + " is not a SSDObject!");
					}
					if(checkCollection && !(node0 instanceof SSDCollection)) {
						throw new RuntimeException(
							"Object " + name + " is not a SSDCollection!");
					}
					if(checkFunctionCall && !(node0 instanceof SSDFunctionCall)) {
						throw new RuntimeException(
							"Object " + name + " is not a SSDFunctionCall!");
					}
					return true;
				}
				return false;
			}
		}
		return false;
	}
	
	public boolean has(String name) {
		checkIfObject();
		return has(name, false, false, false);
	}
	
	public boolean hasObject(String name) {
		checkIfObject();
		return has(name, true, false, false);
	}
	
	public boolean hasCollection(String name) {
		checkIfObject();
		return has(name, false, true, false);
	}
	
	public boolean hasFunctionCall(String name) {
		checkIfObject();
		return has(name, false, false, true);
	}
	
	public boolean hasNull(String name) {
		SSDObject obj = getObject(name);
		return obj != null &&
			   obj.getType() == SSDType.NULL;
	}
	
	public boolean hasBoolean(String name) {
		SSDObject obj = getObject(name);
		return obj != null &&
			   obj.getType() == SSDType.BOOLEAN;
	}
	
	public boolean hasInteger(String name) {
		SSDObject obj = getObject(name);
		return obj != null &&
			   obj.getType() == SSDType.INTEGER;
	}
	
	public boolean hasDecimal(String name) {
		SSDObject obj = getObject(name);
		return obj != null &&
			   obj.getType() == SSDType.DECIMAL;
	}
	
	public boolean hasString(String name) {
		SSDObject obj = getObject(name);
		return obj != null &&
			   obj.getType() == SSDType.STRING;
	}
	
	public boolean has(int index) {
		checkIfArray();
		return has(Integer.toString(index), false, false, false);
	}
	
	public boolean hasObject(int index) {
		checkIfArray();
		return has(Integer.toString(index), true, false, false);
	}
	
	public boolean hasCollection(int index) {
		checkIfArray();
		return has(Integer.toString(index), false, true, false);
	}
	
	public boolean hasFunctionCall(int index) {
		checkIfArray();
		return has(Integer.toString(index), false, true, false);
	}
	
	public boolean hasNull(int index) {
		SSDObject obj = getObject(index);
		return obj != null &&
			   obj.getType() == SSDType.NULL;
	}
	
	public boolean hasBoolean(int index) {
		SSDObject obj = getObject(index);
		return obj != null &&
			   obj.getType() == SSDType.BOOLEAN;
	}
	
	public boolean hasInteger(int index) {
		SSDObject obj = getObject(index);
		return obj != null &&
			   obj.getType() == SSDType.INTEGER;
	}
	
	public boolean hasDecimal(int index) {
		SSDObject obj = getObject(index);
		return obj != null &&
			   obj.getType() == SSDType.DECIMAL;
	}
	
	public boolean hasString(int index) {
		SSDObject obj = getObject(index);
		return obj != null &&
			   obj.getType() == SSDType.STRING;
	}
	
	private final void set(String name, SSDType type, Object value, boolean add) {
		checkName(name);
		SSDCollection node = this;
		while(!name.isEmpty()) {
			// Name is a complex name, that means that the object
			// is not in this array but somewhere in its arrays
			int index;
			if((index = name.indexOf(CHAR_NAME_DELIMITER)) > -1) {
				String 		  aname = name.substring(0, index);
				SSDCollection coll  = node.getCollection(aname);
				// If the collection does not exist, create one
				// with the needed name
				if((coll == null)) {
					// Get the next object name
					String nextName = name.substring(index+1);
					// Format the name if needed
					int index0;
					if((index0 = nextName.indexOf(CHAR_NAME_DELIMITER)) > -1) {
						// Trim the name so it is only one part
						// (between two dots)
						nextName = nextName.substring(0, index0);
					}
					// Decide whether the new collection should be array
					boolean array = nextName.matches("\\d+");
					// Create a new collection with all needed data
					coll = new SSDCollection(node, aname, array);
					// Add the created collection
					node.set(aname, coll);
				}
				// Set the collection for the next loop
				node = coll;
				name = name.substring(index+1);
			}
			// Name is a simple name
			else {
				// Create a new node of the desired type
				SSDNode newNode = null;
				if(value instanceof SSDCollection) {
					SSDCollection coll = (SSDCollection) value;
					// Change properties of the collection
					coll.parent.set(node);
					coll.name  .set(name);
					// Change value of the new node
					newNode = coll;
				} else if(value instanceof SSDObject) {
					SSDObject obj = (SSDObject) value;
					// Change properties of the object
					obj.parent.set(node);
					obj.name  .set(name);
					// Change value of the new node
					newNode = obj;
				} else if(value instanceof String) {
					newNode = type.createObject(node, name, (String) value);
				}
				// Add the created node to the objects
				if(newNode != null) {
					node.objects.put(name, newNode);
					break;
				}
			}
		}
	}
	
	public void setNull(String name) {
		checkIfObject();
		set(name, SSDType.NULL, WORD_NULL, false);
	}
	
	public void set(String name, boolean value) {
		checkIfObject();
		set(name, SSDType.BOOLEAN, Boolean.toString(value), false);
	}
	
	public void set(String name, byte value) {
		checkIfObject();
		set(name, SSDType.INTEGER, Byte.toString(value), false);
	}
	
	public void set(String name, short value) {
		checkIfObject();
		set(name, SSDType.INTEGER, Short.toString(value), false);
	}
	
	public void set(String name, int value) {
		checkIfObject();
		set(name, SSDType.INTEGER, Integer.toString(value), false);
	}
	
	public void set(String name, long value) {
		checkIfObject();
		set(name, SSDType.INTEGER, Long.toString(value), false);
	}
	
	public void set(String name, float value) {
		checkIfObject();
		set(name, SSDType.DECIMAL, Float.toString(value), false);
	}
	
	public void set(String name, double value) {
		checkIfObject();
		set(name, SSDType.DECIMAL, Double.toString(value), false);
	}
	
	public void set(String name, String value) {
		checkIfObject();
		set(name, SSDType.STRING, value, false);
	}
	
	public void set(String name, SSDObject object) {
		checkIfObject();
		set(name, SSDType.UNKNOWN, object, false);
	}
	
	public void set(String name, SSDCollection collection) {
		checkIfObject();
		set(name, SSDType.UNKNOWN, collection, false);
	}
	
	public void set(String name, SSDFunctionCall funcCall) {
		checkIfObject();
		set(name, SSDType.UNKNOWN, funcCall, false);
	}
	
	public void setNull(int index) {
		checkIfArray();
		set(Integer.toString(index), SSDType.NULL, WORD_NULL, false);
	}
	
	public void set(int index, boolean value) {
		checkIfArray();
		set(Integer.toString(index), SSDType.BOOLEAN, Boolean.toString(value), false);
	}
	
	public void set(int index, byte value) {
		checkIfArray();
		set(Integer.toString(index), SSDType.INTEGER, Byte.toString(value), false);
	}
	
	public void set(int index, short value) {
		checkIfArray();
		set(Integer.toString(index), SSDType.INTEGER, Short.toString(value), false);
	}
	
	public void set(int index, int value) {
		checkIfArray();
		set(Integer.toString(index), SSDType.INTEGER, Integer.toString(value), false);
	}
	
	public void set(int index, long value) {
		checkIfArray();
		set(Integer.toString(index), SSDType.INTEGER, Long.toString(value), false);
	}
	
	public void set(int index, float value) {
		checkIfArray();
		set(Integer.toString(index), SSDType.DECIMAL, Float.toString(value), false);
	}
	
	public void set(int index, double value) {
		checkIfArray();
		set(Integer.toString(index), SSDType.DECIMAL, Double.toString(value), false);
	}
	
	public void set(int index, String value) {
		checkIfArray();
		set(Integer.toString(index), SSDType.STRING, value, false);
	}
	
	public void set(int index, SSDObject object) {
		checkIfArray();
		set(Integer.toString(index), SSDType.UNKNOWN, object, false);
	}
	
	public void set(int index, SSDCollection collection) {
		checkIfArray();
		set(Integer.toString(index), SSDType.UNKNOWN, collection, false);
	}
	
	public void set(int index, SSDFunctionCall funcCall) {
		checkIfArray();
		set(Integer.toString(index), SSDType.UNKNOWN, funcCall, false);
	}
	
	public void addNull(String name) {
		checkIfObject();
		set(name, SSDType.NULL, WORD_NULL, true);
	}
	
	public void add(String name, boolean value) {
		checkIfObject();
		set(name, SSDType.BOOLEAN, Boolean.toString(value), true);
	}
	
	public void add(String name, byte value) {
		checkIfObject();
		set(name, SSDType.INTEGER, Byte.toString(value), true);
	}
	
	public void add(String name, short value) {
		checkIfObject();
		set(name, SSDType.INTEGER, Short.toString(value), true);
	}
	
	public void add(String name, int value) {
		checkIfObject();
		set(name, SSDType.INTEGER, Integer.toString(value), true);
	}
	
	public void add(String name, long value) {
		checkIfObject();
		set(name, SSDType.INTEGER, Long.toString(value), true);
	}
	
	public void add(String name, float value) {
		checkIfObject();
		set(name, SSDType.DECIMAL, Float.toString(value), true);
	}
	
	public void add(String name, double value) {
		checkIfObject();
		set(name, SSDType.DECIMAL, Double.toString(value), true);
	}
	
	public void add(String name, String value) {
		checkIfObject();
		set(name, SSDType.STRING, value, true);
	}
	
	public void add(String name, SSDObject object) {
		checkIfObject();
		set(name, SSDType.UNKNOWN, object, true);
	}
	
	public void add(String name, SSDCollection collection) {
		checkIfObject();
		set(name, SSDType.UNKNOWN, collection, true);
	}
	
	public void add(String name, SSDFunctionCall funcCall) {
		checkIfObject();
		set(name, SSDType.UNKNOWN, funcCall, true);
	}
	
	public void addNull() {
		checkIfArray();
		set(Integer.toString(nextIndex()), SSDType.NULL, WORD_NULL, true);
	}
	
	public void add(boolean value) {
		checkIfArray();
		set(Integer.toString(nextIndex()), SSDType.BOOLEAN, Boolean.toString(value), true);
	}
	
	public void add(byte value) {
		checkIfArray();
		set(Integer.toString(nextIndex()), SSDType.INTEGER, Byte.toString(value), true);
	}
	
	public void add(short value) {
		checkIfArray();
		set(Integer.toString(nextIndex()), SSDType.INTEGER, Short.toString(value), true);
	}
	
	public void add(int value) {
		checkIfArray();
		set(Integer.toString(nextIndex()), SSDType.INTEGER, Integer.toString(value), true);
	}
	
	public void add(long value) {
		checkIfArray();
		set(Integer.toString(nextIndex()), SSDType.INTEGER, Long.toString(value), true);
	}
	
	public void add(float value) {
		checkIfArray();
		set(Integer.toString(nextIndex()), SSDType.DECIMAL, Float.toString(value), true);
	}
	
	public void add(double value) {
		checkIfArray();
		set(Integer.toString(nextIndex()), SSDType.DECIMAL, Double.toString(value), true);
	}
	
	public void add(String value) {
		checkIfArray();
		set(Integer.toString(nextIndex()), SSDType.STRING, value, true);
	}
	
	public void add(SSDObject object) {
		set(isArray ? Integer.toString(nextIndex())
		            : object != null
		            	? object.getName()
		            	: Integer.toString(nextIndex()),
		    SSDType.UNKNOWN, object, true);
	}
	
	public void add(SSDCollection collection) {
		set(isArray ? Integer.toString(nextIndex())
		            : collection != null
		            	? collection.getName()
		            	: Integer.toString(nextIndex()),
		    SSDType.UNKNOWN, collection, true);
	}
	
	public void add(SSDFunctionCall funcCall) {
		set(isArray ? Integer.toString(nextIndex())
		            : funcCall != null
		            	? funcCall.getName()
		            	: Integer.toString(nextIndex()),
		    SSDType.UNKNOWN, funcCall, true);
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
		return isArray ?
				SSDCollectionType.ARRAY :
				SSDCollectionType.OBJECT;
	}
	
	public SSDNode[] getNodes() {
		Collection<SSDNode> c = objects.values();
		return c.toArray(new SSDNode[c.size()]);
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
		SSDNode p = parent.get();
		return (p == null ? "" : (p.getFullName() + CHAR_NAME_DELIMITER)) +
			   (getName());
	}
	
	public void addAnnotation(String name, SSDCollection data) {
		annotations.add(new SSDAnnotation(name, data.objects));
	}
	
	public boolean hasAnnotation(String name) {
		return getAnnotation(name) != null;
	}
	
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
	
	public SSDCollection copy() {
		return new SSDCollection(getParent(), getName(), isArray,
			new LinkedHashMap<>(objects), new ArrayList<>(annotations));
	}
	
	public SSDCollection filter(SSDFilter filter) {
		SSDCollection cl = empty();
		for(SSDNode node : this) {
			// Filter each node
			if(filter.accept(node)) {
				String name = node.getName();
				if(node.isCollection()) cl.set(name, (SSDCollection) node);
				else 					cl.set(name, (SSDObject) 	   node);
			}
		}
		return cl;
	}
	
	String tabString(int level) {
		StringBuilder sb = new StringBuilder();
		for(int i = 0; i < level; ++i)
			sb.append(CHAR_TAB);
		return sb.toString();
	}
	
	String toString(int depth, boolean compress, boolean json, boolean invoke) {
		String tab0 	 = tabString(depth-1);
		String tab1 	 = tab0 + CHAR_TAB;
		StringBuilder sb = new StringBuilder();
		// Append all annotations
		if(depth == 1 && !annotations.isEmpty() && !json) {
			boolean annf = true;
			for(SSDAnnotation ann : annotations) {
				if(annf) 	  annf = false; else
				if(!compress) sb.append(CHAR_NEWLINE);
				sb.append(ann.toString(compress));
			}
			if(!compress) sb.append(CHAR_NEWLINE);
		}
		sb.append(isArray ? CHAR_ARRAY_OB : CHAR_OBJECT_OB);
		boolean first = true;
		for(SSDNode node : objects.values()) {
			if((node instanceof SSDObject)
					&& ((SSDObject) node).getType() == SSDType.UNKNOWN
					&& json
					&& !invoke)
				continue;
			if((first)) {
				if(!compress) {
					sb.append(CHAR_NEWLINE);
				}
				first = false;
			} else {
				sb.append(CHAR_ITEM_DELIMITER);
				if(!compress) {
					sb.append(CHAR_NEWLINE);
				}
			}
			// Append all node's annotations
			if(!json) {
				SSDAnnotation[] anns;
				if((anns = node.getAnnotations()).length > 0) {
					boolean annf = true;
					for(SSDAnnotation ann : anns) {
						if(annf) 	  annf = false; else
						if(!compress) sb.append(CHAR_NEWLINE);
						if(!compress) sb.append(tab1);
						sb.append(ann.toString(compress));
					}
					if(!compress) sb.append(CHAR_NEWLINE);
				}
			}
			if(!compress) sb.append(tab1);
			if(!isArray) {
				if(json) sb.append(CHAR_DOUBLE_QUOTES);
				sb.append(node.getName());
				if(json) sb.append(CHAR_DOUBLE_QUOTES);
				sb.append(CHAR_NV_DELIMITER);
				if(!compress) {
					sb.append(CHAR_SPACE);
				}
			}
			if(node instanceof SSDObject) {
				if(invoke && node instanceof SSDFunctionCall) {
					SSDFunctionCall func  = (SSDFunctionCall) node;
					SSDValue 		value = compress ? func.getValue()
					         		                 : func.getFormattedValue(depth);
					String			sval  = value != null ? value.toString()
					      			                      : WORD_NULL;
					if(compress) {
						// There is no compress option, use SSDF compress function
						sval = new String(SSDF.formatContent(sval.toCharArray()));
					}
					// Add the compressed function value
					sb.append(sval);
				} else sb.append(node.toString(compress));
			} else {
				sb.append(((SSDCollection) node)
				          		.toString(depth+1,
				          		          compress,
				          		          json,
				          		          invoke));
			}
		}
		if(!compress) {
			sb.append(CHAR_NEWLINE);
			sb.append(tab0);
		}
		sb.append(isArray ? CHAR_ARRAY_CB : CHAR_OBJECT_CB);
		return sb.toString();
	}
	
	@Override
	public String toString() {
		return toString(false);
	}
	
	public String toString(boolean compress) {
		return toString(1, compress, false, false);
	}
	
	public String toString(boolean compress, boolean invoke) {
		return toString(1, compress, false, invoke);
	}
	
	public String toJSON() {
		return toJSON(false);
	}
	
	public String toJSON(boolean compress) {
		return toString(1, compress, true, false);
	}
	
	public String toJSON(boolean compress, boolean invoke) {
		return toString(1, compress, true, invoke);
	}

	@Override
	public Iterator<SSDNode> iterator() {
		return new SSDCollectionIterator(this);
	}
	
	private static final class SSDCollectionIterator
			implements Iterator<SSDNode> {
		
		final Iterator<SSDNode> iterator;
		SSDCollectionIterator(SSDCollection collection) {
			iterator = collection.objects.values().iterator();
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
}