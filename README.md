# jvm-constexpr

[![Build status](https://img.shields.io/azure-devops/build/sipkab/4a34014e-9c3d-4f32-bf69-4bc36095dc9d/8/master)](https://dev.azure.com/sipkab/jvm-constexpr/_build)

Java library that performs constant optimizations on JVM bytecode. The tool examines the input bytecode, executes the instructions which only have constant inputs, and replaces them with the result. \
jvm-constexpr can work with custom types and lets you perform computation during build time.

The project uses [ASM](https://asm.ow2.io/) to perform bytecode manipulation.

  * [Examples](#examples)
    + [Input string with prefix](#input-string-with-prefix)
    + [Build time](#build-time)
    + [Various minor optimizations](#various-minor-optimizations)
    + [`StringBuilder`](#stringbuilder)
    + [Array data creation](#array-data-creation)
    + [Custom types](#custom-types)
  * [How it works](#how-it-works)
    + [`@ConstantExpression`](#constantexpression)
    + [`@Deconstructor`](#deconstructor)
  * [Usage](#usage)
  * [Building the project](#building-the-project)
  * [Repository structure](#repository-structure)
  * [License](#license)

## Examples

See below for some basic examples of the library capabilities.

### Input string with prefix

Something we've encountered a few times in our life.

```java
public static final String PREFIX = "prefix: ";
if (input.startsWith(PREFIX)) {
    String value = input.substring(PREFIX.length());
}
// is replaced with:
public static final String PREFIX = "prefix: ";
if (input.startsWith(PREFIX)) {
    String value = input.substring(8);
}
```

### Build time

Including info about the build in the classes directly.

```java
@ConstantExpression
private static final long BUILD_TIME = System.currentTimeMillis();
public static long getBuildTime() {
    return BUILD_TIME;
}
// is replaced with:
@ConstantExpression
private static final long BUILD_TIME = 1672596127848L; // value is an example, depends on when you run
public static long getBuildTime() {
    return 1672596127848L;
}
```

### Various minor optimizations

Functions are evaluated if their result are deterministic and have constant inputs. If they don't result in a primitive value, their instantiation can also replaced with a more efficient version (like in the case of `UUID`).

```java
public static final UUID MY_UUID = UUID.fromString("f3d07547-bb76-4d25-9c23-d1ce6b6f4ab5");
public static final long MILLIS_IN_A_DAY = TimeUnit.DAYS.toMillis(1);
public static final String MESSAGE = "There are " + MILLIS_IN_A_DAY + " milliseconds in a day.";
public static final String SIMPLE_NAME = MyClass.class.getSimpleName();
public static final LocalTime LUNCHTIME = LocalTime.of(12, 0);
// is replaced with:
public static final UUID MY_UUID = new UUID(-878072976389026523L, -7195677095111996747L);
public static final long MILLIS_IN_A_DAY = 86400000L;
public static final String MESSAGE = "There are 86400000 milliseconds in a day.";
public static final String SIMPLE_NAME = "MyClass";
public static final LocalTime LUNCHTIME = LocalTime.NOON;
```

### `StringBuilder`

When you concatenate strings like `"Value=" + val + ", type=" + type`, then the following code is generated by the Java compiler. (At least on Java 8)

```java
new StringBuilder().append("Value=").apppend(val).append(", type=").append(type)
```

The tool also optimizes the constructor of `StringBuilder` where possible, by inlining the first `append` call:

```java
new StringBuilder("Value=").apppend(val).append(", type=").append(type)
```

### Array data creation

Sometimes you create arrays with an initializer:
```java
new int[] { 0, 0, 1, 5, 99, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, }
```

However, the Java compiler basically generates the following code for the above (pseudocode):

```java
array = new int[26]
array[0] = 0;
array[1] = 0;
array[2] = 1;
array[3] = 5;
array[4] = 99;
array[5] = 0;
array[6] = 0;
array[7] = 0;
... 18 more zero assignments
```

These zero assignments are unnecessary, as the arrays are default initialized to zero values. jvm-constexpr will remove these zero assignments, therefore reducing the bytecode size:

```java
array = new int[26]
array[2] = 1;
array[3] = 5;
array[4] = 99;
```

### Custom types

The library can evaluate custom types if they're annotated as such.

```java
@ConstantExpression
class MyType {
    private int value;
	
    public MyType(int value) { this.value = value * 99; }
    public int getValue() { return this.value; }
}

public static final int MY_VALUE = new MyType(10).getValue();
// is replaced with:
public static final int MY_VALUE = 990;
```


## How it works

The library takes the class files as an input, and examines the instructions in the method bodies of the class files. If an instruction only has constant value inputs, then it will be executed and the instruction(s) are replaced with the computed value. \
The value of `static final` fields are also inlined when possible. This is done in multiple rounds on the class files, until there are no more optimizable instructions found.

The tool is configured by specifying which types can be used as constants, which methods can be used to reproduce the objects, and how the computed values should be written back in the method body.

This configuration is done with the annotations below. \
(Also via programmatic interface, see [`InlinerOptions`](tool/src/sipka/jvm/constexpr/tool/options/InlinerOptions.java).

### `@ConstantExpression`

Use this annotation to specify which methods can be used by the tool to reconstruct an object. \

You can place this annotation on:

* Types: The type will be marked as a constant type, meaning that all constructors, non-static fields, and non-static methods are allowed to be called by the inliner tool. The only exception to this is the `hashCode()` method, which needs to be annotated in addition to this. (This is because the tool cannot check if the hash code is stable between different JVM executions.)
* Methods: The annotated method will be callable by the inliner tool. If it is an instance method, then it will be called only if an appropriate instance can also be reconstructed by the tool.\
If the method is `static`, then the tool will call it if all arguments can also be reconstructed.
* Constructors: The annotated constructor can be used to recreate the instance during optimization.
* Fields: Non-static fields will be accessable by the tool.\
For `static final` fields, their initializers will be evaluated and inlined.

You should take care when annotating methods with `@ConstantExpression` and check if the methods implementation access any environment dependent data. E.g. `String.format(String, Object...)` cannot be annotated, as it uses the default `Locale` of the JVM, therefore it may give different results when executed with a different default `Locale`. The same applies to system properties, environment variables, and others.

### `@Deconstructor`

This annotation is relevant when you have your own custom types that take part of constant evaluation. It specifies how an instance of the type should be written back in the method body.

**Constructors** can be annotated with this. E.g. for the class `UUID`, the `UUID(long, long)` constructor is the *deconstructor*, because it can be used to reconstruct the `UUID` instance during runtime based on the most and least significant bits of the `UUID` calculated by the constant inliner.\
If the `UUID.fromString("f3d07547-bb76-4d25-9c23-d1ce6b6f4ab5")` expression is used to construct an `UUID`, then this is evaluated by the inliner tool, and will look for a way to write it back, thus optimizing this `fromString` call. The constructor above is used, and `new UUID(-878072976389026523L, -7195677095111996747L)` will be used to replace the `fromString` call.

**Static methods** can also be annotated with `@Deconstructor`, in which case that will be used. An example for this is `Integer.valueOf(int)`. E.g. If the tool encounters a call to `Integer.valueOf(String, int)`, then it will be evaluated, and the result written back with `Integer.valueOf(int)`:
```java
Integer i = Integer.valueOf("2abc", 16)
// is replaced with:
Integer i = Integer.valueOf(10940)
```

**Static fields** can also be annotated with `@Deconstructor` in which case the value of this field will be checked for equality against the object being deconstructed, and if they equal, a reference to the `static` field will be written, instead of a constructor or method call. Like in the example above, with `LocalTime`:

```java
LocalTime LUNCHTIME = LocalTime.of(12, 0);
// is replaced with:
LocalTime LUNCHTIME = LocalTime.NOON;
```

**Note** that currently only a single method or constructor can be annotated with `@Deconstructor` for a given type. Multiple static fields are allowed, they will be checked for equality in sequence.

When determining the arguments for a deconstructor function, the inliner tool will look for a method based on the parameter name. It will search for a no-arg method that has the same return type as the parameter type. The method name should be lowercase-match the lowercase parameter name, or the parameter name prepended with `get`. \
E.g. For a parameter `CharSequence myMessage`:

* `CharSequence myMessage()`: **matches**
* `Object myMessage()`: doesn't match
* `String myMessage()`: doesn't match
* `CharSequence mymessage()`: **matches**
* `CharSequence getmymessage(`): **matches**
* `CharSequence getMyMessage(`): **matches**
* `CharSequence GETMYMESSAGE()`: **matches**
* `CharSequence getMyMessage(int index)`: doesn't match

More fine grained configuration is planned to be implemented.

## Usage

The releases contain multiple JARs with each component separately, as well as a fat JAR with all dependencies included. The fat JAR can be used as a simple command line application.

Examples:

```
# Optimize JAR and overwrite it
java -jar sipka.jvm.constexpr-fat.jar -input classes.jar -overwrite

# Dry-run the optimization on the input, only display the optimizations, but don't write anything
java -jar sipka.jvm.constexpr-fat.jar -input classes.jar

# Write the output to an output JAR
java -jar sipka.jvm.constexpr-fat.jar -input classes.jar -output classes_optimized.jar

# Write the output to a directory
java -jar sipka.jvm.constexpr-fat.jar -input classes.jar -output output_dir

# Take a class file as the input, overwrite it
java -jar sipka.jvm.constexpr-fat.jar -input build/classes/com/pack/MyClass.class -overwrite

# Take a class directory as the input, overwrite it
java -jar sipka.jvm.constexpr-fat.jar -input build/classes -overwrite

# Optimize classes.jar, but also load lib1.jar, lib2.jar, lib3.jar, which are not optimized. Overwrite the input classes.jar
java -jar sipka.jvm.constexpr-fat.jar -classpath lib1.jar;lib2.jar -classpath lib3.jar -input classes.jar -overwrite

# Display help
java -jar sipka.jvm.constexpr-fat.jar help

# Display help for the run subcommand
java -jar sipka.jvm.constexpr-fat.jar help run
```

`-input` and `-classpath` parameters can be specified multiple times, or with a semicolon (`;`) path separator.

## Building the project

The project uses the [saker.build system](https://github.com/sakerbuild/saker.build) for building.

Use the following command, or build it inside an IDE with the plugin:

```plaintext
java -jar saker.build.jar -build-directory build export
```

See the [build script](/saker.build) for the executable build targets.

## Repository structure

* `annotations`: Contains the annotations that can be used for configuration
* `tool`: Sources and resources for the inliner tool implementation
* `main`: Code for the command line interface, and the main entry point for non-programmatic usage
* `test`: Tests

## License

The source code for the project with the exception of the *annotations* bundle is licensed under *GNU General Public License v3.0 only*. \
The *annotations* sources and release JAR is licensed under ##TBD##, which allows adding the annotations to your project (and running the optimizations) without the need of adhering to the rest of the project's GPL-3.0 license.

Short identifier: [`GPL-3.0-only`](https://spdx.org/licenses/GPL-3.0-only.html).

Official releases of the project (and parts of it) may be licensed under different terms. See the particular releases for more information.
