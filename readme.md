# SSDF2
SSDF2 is a Java library that helps with reading SSDF (Sune Scripting Data File) Syntax, which is a custom syntax similiar to a JSON syntax and identical with JS object definition. However, it has some new features compared to both JSON and JS object definition.

SSDF2 is an updated version of [SSDF-Reader](https://github.com/sunecz/SSDF-Reader) library.

# Syntax
As it was said, syntax is similiar to JSON Syntax and identical with JS object definition, but contains some features that none of JSON or JS object definition has.

### Example
```
{
    type: "people_list",
    meta: {
        version: "1.0",
        date: "2016-07-25",
        creator: "admin"
    },
    people: [
        {
            name: "John",
            age: 36,
            country: "US"
        },
        {
            name: "Billy",
            age: 54,
            country: "UK"
        }
    ]
}
```

# Usage
## Reading a SSDF Syntax
When reading a SSDF Syntax there are multiple ways to read the data. They can be read from a string, file, or even a stream.
```java
// From a string
SSDCollection data = SSDF.read(string);
// From a file
SSDCollection data = SSDF.read(file);
// From a stream
SSDCollection data = SSDF.read(stream);
```

There is also a simple way to read from a resource contained in a project or a JAR file.
```java
SSDCollection data = SSDF.read(new File("res:" + pathToResource));
```

## Data
There are two main types of objects in SSDF Syntax - a collection (`SSDCollection`) and an object (`SSDObject`) - implementing the `SSDNode` interface. Both of these types has its own intended functionality. `SSDCollection` represents an array with indexes or an array with names, and `SSDObject` represents all objects, such as `string`, `integer`, etc.

### Types of collections
* Array (array with indexes)
* Object (array with names)

### Types of objects
* Null (represents null values)
* Boolean (represents true or false)
* Integer (represents all integers)
* Decimal (represents all decimal numbers)
* String (represents all text)
* Unknown (represents all values that cannot be determined)

## Getting data
When reading a SSDF data using the `SSDF` methods a `SSDCollection` is returned. Then it's possible to get all the objects and collections.
```java
// To get a boolean value
boolean value = data.getBoolean(name);
// To get an integer value (there are also getByte, getShort, getLong methods)
int value = data.getInt(name);
// To get a decimal value (there is also getFloat method)
double value = data.getDouble(name);
// To get a string value
String value = data.getString(name);
// To get an object (SSDObject)
SSDObject value = data.getObject(name);
// To get a collection (SSDCollection)
SSDCollection value = data.getCollection(name);
// To get a node (SSDObject or SSDCollection)
SSDNode value = data.get(name);
```

For arrays with indexes use the same methods but with an index instead of a name.

## Data manipulation
Some basic operations can be done with the data, such as checking if a value exist, setting, adding, and removing. With these operations it's possible to create and manage basic data structures.
```java
// To check if a value exist
data.has(name);
// To set a value
data.set(name, value);
// To add a value
data.add(name, value);
// To remove a value
data.remove(name);
```

For arrays with indexes use the same methods but with an index instead of a name. In case of the add method there is no need to type an index, therefore `data.add(value)` is the right way to go.

## Name of a node
When doing any operation with a collection or an object, there are two ways of dealing with the names. The first way is calling the correct function to obtain another object or collection, and continuing this way until the final object is returned. The second way is calling the needed function once with a name chain (that is a string containing names of collections and the final object, each seperated by dots).

### Example
It is needed to get a string value of an object with name `message`, contained in a collection `startup`, contained in a collection `messages`, contained in the main object.

Visualization:
```
{
	messages: {
		startup: {
			message: "Hello there!"
		}
	}
}
```

Then it is possible to achieve that like this:
```java
// The first way
SSDCollection messages = data.getCollection("messages");
SSDCollection startup = data.getCollection("startup");
String message = startup.getString("message");

// The second way
String message = data.getString("messages.startup.message");
```

The `.` character is a delimiter between collections and objects within a name.

## Finding out the type of an array
To find out if an array is an array with names or if it is an array with indexes, method `array.getType()`, that returns `SSDCollectionType`, can be used. In case of an array with names the method returns `SSDCollectionType.OBJECT` and in case of an array with indexes the method returns `SSDCollectionType.ARRAY`.

## Finding out if a node is a collection or an object
```java
boolean isCollection = node.isCollection();
boolean isObject = node.isObject();

// Node is a collection
if(isCollection) {
    SSDCollection collection = (SSDCollection) node;
    // ... some operations
}
// Node is an object
else if(isObject) {
    SSDObject object = (SSDObject) node;
    // ... some operations
}
```

## Getting the length of an array with indexes
```java
int length = array.length();
```

## Getting the parent of a collection or an object
If the gotten parent is `null` the node is located in the main object.
```java
SSDNode parent = node.getParent();
```

## Getting the name of a collection or an object
```java
// To get the name of a collection or an object in its parent
String name = node.getName();
// To get the full name of a collection or an object
String name = node.getFullName();
```

## Copying a collection
When copying a collection a new collection is created with the same name, same objects and same other information.
```java
SSDCollection copy = collection.copy();
// ... some operations
```

## Filtering nodes
If there is a need to filter the nodes contained in a collection, method `collection.filter(SSDFilter)` can be used. There are some predefined filters in the `SSDFilter` class, such as `ONLY_COLLECTIONS`, `ONLY_OBJECTS`, `ONLY_INTEGERS`, etc.
```java
SSDCollection objects = collection.filter(SSDFilter.ONLY_OBJECTS);
for(SSDNode node : objects) {
    SSDObject object = (SSDObject) node;
    // ... some operations
}
```

## Converting a collection or an object to a string
```java
// Without compression
String string = node.toString();
// With compression
String string = node.toString(true);
```

Compressed version is just a string without unnecessary spaces and whitespaced characters.

# Features
There are not many features available at the moment. However, this library is still in development and is constantly growing.

## Annotation
Annotation is, same as in Java, some kind of additional information to the object, determing some of its intended features. It can, for example, provide a metadata for an object or define an intended purpose of an object.

### Example
```
@Meta(version="1.0", date="2016-07-25", creator="admin")
@List(type="people")
{
    data: [
        {
            name: "John",
            age: 36,
            country: "US"
        },
        {
            name: "Billy",
            age: 54,
            country: "UK"
        }
    ]
}
```

### Getting the annotations of a collection or an object
```java
// To get all annotations
SSDAnnotation[] annotations = node.getAnnotations();
// To get all annotations with a name
SSDAnnotation[] annotations = node.getAnnotations(name);
// To get the first annotation with a name
SSDAnnotation annotation = node.getAnnotation(name);
```

## JSON
This library can be used also for reading JSON strings from files, streams, and strings.
```java
SSDCollection data = SSDF.readJSON(jsonString);
// ... some operations with the json data
```

Method `data.toJSON()` can be used in order to get the JSON string back.
```java
// Without compression
String jsonString = data.toJSON();
// With compression
String jsonString = data.toJSON(true);
```

# License
MIT (https://github.com/sunecz/SSDF2/blob/master/license.md)
