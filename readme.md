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
When reading a SSDF Syntax there are multiple ways to read the data. They can be read from a string, file, stream, or even a resource.
```java
// From a string
SSDCollection data = SSDF.read(string);
// From a file
SSDCollection data = SSDF.read(file);
// From a stream
SSDCollection data = SSDF.read(stream);
// From a resource
SSDCollection data = SSDF.readResource(path);
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

## Getting length of an array with indexes
```java
int length = array.length();
```

## Getting parent of a collection or an object
If the gotten parent is `null` the node is located in the main object.
```java
SSDNode parent = node.getParent();
```

## Getting name of a collection or an object
```java
// To get the name of a collection or an object in its parent
String name = node.getName();
// To get the full name of a collection or an object
String name = node.getFullName();
```

## Copying a collection
When copying a collection a new collection is created with the same name and same objects.
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

Compressed version is just a string without unnecessary spaces and whitespace characters.

## Creating a SSDObject
To create a new SSDObject it possible to do as follows:
```java
// With a given name
SSDObject.of("name", "value");
// With a randomly generated name
SSDObject.of("value");
```

The created object can be then added to a collection:
```java
// With a new name
collection.add("newname", object);
// With the earlier set name
collection.add(object);
```

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

### Getting annotations of a collection or an object
```java
// To get all annotations
SSDAnnotation[] annotations = node.getAnnotations();
// To get all annotations with a name
SSDAnnotation[] annotations = node.getAnnotations(name);
// To get the first annotation with a name
SSDAnnotation annotation = node.getAnnotation(name);
```

### Annotations with only one value
It is possible to specify an annotation like this:
```
@Annotation("a value")
```
which is identical with this syntax:
```
@Annotation(value="a value")
```

### Creation of an annotation
```java
// Annotation with some data
SSDCollection collection = ...;
SSDAnnotation annotation = SSDAnnotation.of(name, data);
// Annotation with no data
SSDAnnotation annotation = SSDAnnotation.of(name);
```

### Manipulation with annotations
```java
// Check whether an annotation exists
boolean exists = node.hasAnnotation(name);
// Add an annotation
node.addAnnotation(annotation);
// Remove an annotation
node.removeAnnotation(annotation);
// Set an annotation
node.setAnnotation(name, annotation);
```

### Easier manipulation with annotations
Methods - `get`, `set`, `add`, `remove` - can be used to manipulate with annotations. When an annotation at a node is needed to be accessed, instead of collection delimiter (`.`) the annotation delimiter (`:`) is used.

```java
// Get an annotation at a collection
SSDAnnotation annotation = (SSDAnnotation) node.get("collection:Annotation");
// Get an annotation at an object
SSDAnnotation annotation = (SSDAnnotation) node.get("collection.object:Annotation");
// Get an annotation's value
SSDNode value = node.get("collection.object:Annotation.value");
```

## Functions
Functions allows executing a code in Java and returning an output that is put into an invoked SSD File. To invoke a SSD File and get the content, it is needed to call function `SSDCollection.toString(compress, true)`, where `true` states for invocation; additional compression can be used.

### Syntax
```java
{
    value: functionName(arg1, arg2, ..., argN)
}
```

### Available functions
- `get(instance, name)` - Gets a non-static value defined by its name from the given instance
- `getstatic(class, name)` - Gets a static value defined by its name from the given class
- `instance(class)` - Creates a new instance of the given class
- `calc(expression)` - Calculates the given mathematical expression

### Examples

#### Example 1
Having a class `test.FunctionTest` with content:
```java
package test;

import java.io.File;

import sune.util.ssdf2.SSDCollection;
import sune.util.ssdf2.SSDF;

public class FunctionTest {

    int value = 4;

    public static void main(String[] args) {
        SSDCollection coll = SSDF.read(new File("res:/test.ssdf"));
        System.out.println(coll.toString());
    }
}
```

and `test.ssdf` with content:
```java
{
    value: get(instance(test.FunctionTest), "value")
}
```

The output will be as supposed:
```
{
    value: 4
}
```

For static fields it is needed to use `getstatic(test.FunctionTest, "value")`.

#### Example 2
To get a stringified instance of a Java class, it is possible to do as follows - content of `test.ssdf` file:
```
{
    value: instance(test.FunctionTest)
}
```

and content of `test.FunctionTest` class:
```java
package test;

import java.io.File;
import java.util.LinkedHashMap;
import java.util.Map;

import sune.util.ssdf2.SSDCollection;
import sune.util.ssdf2.SSDF;

public class FunctionTest {
    
    String[] greetings = { "Hello", "Hi", "Hey", "Yo" };
    float[] factors = { 0.7f, 0.3f, 0.8f };
    
    double[][] reals = { { 0.33, 1.4893 }, { 7.83, 8.11 } };
    Map<String, String> people = new LinkedHashMap<>();
    
    public FunctionTest() {
        people.put("Bob", "Weird man");
        people.put("Katya", "Beautiful woman");
        people.put("Marci", "Brave woman");
        people.put("Julius", "Shy man");
    }
    
    public static void main(String[] args) {
        SSDCollection coll = SSDF.read(new File("res:/test.ssdf"));
        System.out.println(coll.toString(false, true));
    }
}
```

Output:
```
{
    value: @Instance(class = "test.FunctionTest")
    {
        greetings: [
            "Hello",
            "Hi",
            "Hey",
            "Yo"
        ],
        factors: [
            0.7,
            0.3,
            0.8
        ],
        reals: [
            [
                0.33,
                1.4893
            ],
            [
                7.83,
                8.11
            ]
        ],
        people: {
            Bob: "Weird man",
            Katya: "Beautiful woman",
            Marci: "Brave woman",
            Julius: "Shy man"
        }
    }
}
```

#### Example 3
To calculate a mathematical expression - content of `test.ssdf`:
```
{
    value: calc(3*5+8)
}
```

and content of `test.FunctionTest` class:
```java
package test;

import java.io.File;

import sune.util.ssdf2.SSDCollection;
import sune.util.ssdf2.SSDF;

public class FunctionTest {
    
    public static void main(String[] args) {
        SSDCollection coll = SSDF.read(new File("res:/test.ssdf"));
        System.out.println(coll.toString(false, true));
    }
}
```

Output:
```
{
    value: 23.0
}
```

## Variables
When a value is needed to be written again at some place and everytime when this value is changed, it is needed to be changed at that place as well, variables can be used.

Variables are given as follow:
```
$[parent[.parent_count].]variable_name
```

`parent` - Relative parent where the variable is located (`this` - current object, `main` - the main object)<br>
`parent_count` - How many parents back from relative parent the variable is located<br>
`variable_name` - Name of the variable<br>

When a variable is used in an annotation, special `variable_name` can be used to obtain the object's value:
```
$this.1.value
```

`this` means: the current annotation<br>
`1` means: go back once, so the object which the annotation is bound to is the current object<br>
`value` means: value of the object<br>

### Concatenation of strings or variables
To concatenate strings or variables `+` character can be used.

### Examples
```
{
    value1: "House",
    value2: $value1,
    value3: $this.value1,
    value4: $this.0.value1
}
```

`value1`, `value2`, `value3`, and `value4` have the same value.

```
{
    value: "House",
    data: {
    	value1: $this.1.value,
	value2: $main.value
    }
}
```

`value1` and `value2` have the same value.

```
{
    @Meta(type="Bricks", height=38.2, type_name=$this.1.value)
    name: "House"
}
```

`type_name` has the value of object `name`.

```
{
    first_name: "John",
    last_name: "Smith",
    full_name: $first_name + " " + $last_name,
    greeting: "Hello " + $full_name + "!"
}
```

`greeting` has this value: `Hello John Smith!` 

## JSON
This library can be also used for reading JSON strings from files, streams, and strings.
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
