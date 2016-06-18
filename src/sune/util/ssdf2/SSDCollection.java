package sune.util.ssdf2;

import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

public class SSDCollection implements SSDNode, Iterable<SSDNode> {
	
	public static enum SSDCollectionType {
		ARRAY, OBJECT;
	}
	
	private final SSDNode parent;
	private final String name;
	private final boolean isArray;
	private final Map<String, SSDNode> objects;
	
	SSDCollection(SSDNode parent, String name, boolean isArray) {
		this(parent, name, isArray, new LinkedHashMap<>());
	}
	
	SSDCollection(SSDNode parent, String name, boolean isArray,
			Map<String, SSDNode> objects) {
		checkArgs(name, objects);
		this.parent  = parent;
		this.name 	 = name;
		this.isArray = isArray;
		this.objects = objects;
	}
	
	static final void checkArgs(String name, Map<String, SSDNode> objects) {
		if(name == null) {
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
		if((index = name.indexOf('.')) > -1) {
			// Get name of the array
			String aname = name.substring(0, index);
			String rname = name.substring(index+1);
			return getCollection(aname).get(rname);
		}
		// Name is a simple name
		else {
			SSDNode node = objects.get(name);
			if(node == null) {
				throw new RuntimeException(
					"Object " + name + " does not exist!");
			}
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
	
	private final void remove(String name, boolean checkObject, boolean checkCollection) {
		checkName(name);
		// Name is a complex name, that means that the object
		// is not in this array but somewhere in its arrays
		int index;
		if((index = name.indexOf('.')) > -1) {
			// Get name of the array
			String aname = name.substring(0, index);
			String rname = name.substring(index+1);
			getCollection(aname).remove(rname, checkObject, checkCollection);
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
		remove(name, false, false);
	}
	
	public void removeObject(String name) {
		checkIfObject();
		remove(name, true, false);
	}
	
	public void removeCollection(String name) {
		checkIfObject();
		remove(name, false, true);
	}
	
	public void remove(int index) {
		checkIfArray();
		remove(Integer.toString(index), false, false);
	}
	
	public void removeObject(int index) {
		checkIfArray();
		remove(Integer.toString(index), true, false);
	}
	
	public void removeCollection(int index) {
		checkIfArray();
		remove(Integer.toString(index), false, true);
	}
	
	// Only internal method
	private final boolean has0(String name) {
		return objects.containsKey(name);
	}
	
	private final boolean has(String name, boolean checkObject, boolean checkCollection) {
		checkName(name);
		SSDCollection node = this;
		while(!name.isEmpty()) {
			// Name is a complex name, that means that the object
			// is not in this array but somewhere in its arrays
			int index;
			if((index = name.indexOf('.')) > -1) {
				String aname = name.substring(0, index);
				if(!node.has0(aname)) return false;
				node = node.getCollection(aname);
				name = name.substring(index+1);
			}
			// Name is a simple name
			else {
				if(node.has0(name)) {
					SSDNode node0 = node.get(name);
					if(checkObject && node0 instanceof SSDCollection) {
						throw new RuntimeException(
							"Object " + name + " is not a SSDObject!");
					}
					if(checkCollection && node0 instanceof SSDObject) {
						throw new RuntimeException(
							"Object " + name + " is not a SSDCollection!");
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
		return has(name, false, false);
	}
	
	public boolean hasObject(String name) {
		checkIfObject();
		return has(name, true, false);
	}
	
	public boolean hasCollection(String name) {
		checkIfObject();
		return has(name, false, true);
	}
	
	public boolean has(int index) {
		checkIfArray();
		return has(Integer.toString(index), false, false);
	}
	
	public boolean hasObject(int index) {
		checkIfArray();
		return has(Integer.toString(index), true, false);
	}
	
	public boolean hasCollection(int index) {
		checkIfArray();
		return has(Integer.toString(index), false, true);
	}
	
	private final void set(String name, SSDType type, Object value, boolean add) {
		checkName(name);
		SSDCollection node = this;
		while(!name.isEmpty()) {
			// Name is a complex name, that means that the object
			// is not in this array but somewhere in its arrays
			int index;
			if((index = name.indexOf('.')) > -1) {
				String aname = name.substring(0, index);
				if(!node.has0(aname)) {
					throw new RuntimeException(
						"Object " + node.getFullName() + " does not contain " +
						aname + "!");
				}
				node = node.getCollection(aname);
				name = name.substring(index+1);
			}
			// Name is a simple name
			else {
				if(!add && !node.has0(name)) {
					throw new RuntimeException(
						"Object " + node.getFullName() + " does not contain " +
						name + "!");
				}
				// Create a new node of the desired type
				SSDNode newNode = null;
				if(value instanceof SSDCollection) {
					SSDCollection oldc = (SSDCollection) value;
					SSDCollection newc = new SSDCollection(node, name, oldc.isArray);
					newc.addObjects(oldc.objects);
					newNode = newc;
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
		set(name, SSDType.NULL, "null", false);
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
	
	public void set(String name, SSDCollection collection) {
		checkIfObject();
		set(name, SSDType.UNKNOWN, collection, false);
	}
	
	public void setNull(int index) {
		checkIfArray();
		set(Integer.toString(index), SSDType.NULL, "null", false);
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
	
	public void set(int index, SSDCollection collection) {
		checkIfArray();
		set(Integer.toString(index), SSDType.UNKNOWN, collection, false);
	}
	
	public void addNull(String name) {
		checkIfObject();
		set(name, SSDType.NULL, "null", true);
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
	
	public void add(String name, SSDCollection collection) {
		checkIfObject();
		set(name, SSDType.UNKNOWN, collection, true);
	}
	
	public void addNull() {
		checkIfArray();
		set(Integer.toString(nextIndex()), SSDType.NULL, "null", true);
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
	
	public void add(SSDCollection collection) {
		checkIfArray();
		set(Integer.toString(nextIndex()), SSDType.UNKNOWN, collection, true);
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
		return parent;
	}
	
	@Override
	public String getName() {
		return name;
	}
	
	@Override
	public String getFullName() {
		return (parent == null ? "" : (parent.getFullName() + ".")) +
			   (getName());
	}
	
	String tabString(int level) {
		StringBuilder sb = new StringBuilder();
		for(int i = 0; i < level; ++i)
			sb.append('\t');
		return sb.toString();
	}
	
	String toString(int depth, boolean compress, boolean json) {
		String tab0 	 = tabString(depth-1);
		String tab1 	 = tab0 + '\t';
		StringBuilder sb = new StringBuilder();
		sb.append(isArray ? '[' : '{');
		if(!compress && !objects.isEmpty())
			sb.append('\n');
		boolean first = true;
		for(SSDNode node : objects.values()) {
			if(first) {
				first = false;
			} else {
				sb.append(',');
				if(!compress) {
					sb.append('\n');
				}
			}
			if(!compress) sb.append(tab1);
			if(!isArray) {
				if(json) sb.append('"');
				sb.append(node.getName());
				if(json) sb.append('"');
				sb.append(':');
				if(!compress) {
					sb.append(' ');
				}
			}
			sb.append(node instanceof SSDCollection ?
				((SSDCollection) node).toString(depth+1, compress, json) :
				(node.toString(compress)));
		}
		if(!compress) {
			sb.append('\n');
			sb.append(tab0);
		}
		sb.append(isArray ? ']' : '}');
		return sb.toString();
	}
	
	@Override
	public String toString() {
		return toString(false);
	}
	
	public String toString(boolean compress) {
		return toString(1, compress, false);
	}
	
	public String toJSON() {
		return toJSON(false);
	}
	
	public String toJSON(boolean compress) {
		return toString(1, compress, true);
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