# Log4s #

## Introduction ##

This is a simple project to wrap the excellent [SLF4J](http://slf4j.org/)
logging façade with a Scala-friendly API that is lightweight and very 
convenient.  Using Scala 2.10's macros and value classes, it is possible to 
implement simple API that incurs zero runtime costs and outperforms typical
Java-based practices.

Logging is mostly a solved problem on the JVM, thanks largely to the excellent 
work of Ceki Gülcü and many others.  I have no goal to invent a new logging 
library or introduce yet another wrapper system.  SLF4J solves the problems 
of modularity and separation of concerns beautifully.  My only goal is to 
*enhance* the usability of SLF4J by offering an idiomatic Scala interface.

Please feel free to contact me if you have suggestions for how to enhance or 
improve this library—as long as those suggestions are compatible with the 
project's goals.

## Installation ##

To use Log4s, add the following to your SBT build:

    libraryDependencies += "org.log4s" %% "log4s" % "1.0.2"

## Examples ##

- [Getting a logger](#getting-a-logger)
- [Logging messages](#logging-messages)
- [Exception logging](#exception-logging)

### Getting a logger ###

Most of the time, you simply want to define a logger with a name that matches
the enclosing class or module.  Log4s makes this common case as easy as 
possible, transparently deriving the name for you with zero overhead at runtime. 

```scala
package com.example.project

class DemoClass {
  // Retrieves a logger for "com.example.project.DemoClass"
  private[this] val logger = org.log4s.getLogger 
  ???
} 
```

There is no requirement that you mark your loggers with `private[this]`, but 
the compiler may bypass accessors and generate direct field access if you do.

Automatic logger naming also works for modules (a.k.a. objects or 
singletons).

```scala
object DemoClass {
  // Will log against category "com.example.project.DemoClass"
  private val logger = org.log4s.getLogger
  ???
}
```

Notice that by default, Log4s does not include a _$_ at the end of logger 
categories for modules.  This is slightly different behavior from the common 
idiom `LoggerFactory.getLogger(getClass)` used to get a logger for a module, 
but this behavior is more consistent with Java practices and I suspect is what 
a majority of users will prefer.  Future enhancements may provide a mechanism 
to allow the user to choose whether to include the trailing _$_.    

#### Instance or static? ####

My recommendation is that by default you create your loggers as instance 
variables and mark them as `private[this]`.  This may be more compatible with 
some complex classloading environments, and this practice is more friendly to 
principles of encapsulation.

However, if a specific class will be instantiated very frequently, you may 
want to move its logger to the companion module and mark it `private`. 
There are some cases where greater visibility is justified, but these are 
infrequent for most applications.

The SLF4J FAQ has a good discussion of the [tradeoffs between static and 
instance loggers](http://slf4j.org/faq.html#declared_static).

### Logging messages ###

The logger interfaces are extremely simple, but they're more powerful than 
they look.  All the standard loggers take a single argument of type string. 

```scala
class MyClass(val data: Map[String,Int]) {
  private[this] val logger = org.log4s.getLogger
  
  logger.debug("Constructing new instance of MyClass")
  logger.trace(s"New instance's data set: $data")
}
```

Unlike SLF4J, there are no special methods for parameterized logging, because 
it turns out to be completely unnecessary.  Parameterized logging serves two 
primary purposes: it provides an easy way to construct complex strings, and it 
helps avoid some of the costs of building a dynamic message when the message 
is at a level that is not enabled.

As you can see from the example, Scala 2.10's 
[string interpolation](http://docs.scala-lang.org/overviews/core/string-interpolation.html) 
is a much more powerful solution to the first of these issues—and it even saves
the runtime work of parsing a format string by splitting up the string into
easily concatenated pieces at compile time.

Log4s goes even further in that it uses macros to manipulate the execution so 
that the string interpolations are not even performed unless the compiler is 
enabled.  It does this by inspecting the structure of the argument that you 
pass into the logger.  

If you pass a constant string literal, Log4s will make a direct, in-line call 
to the underlying SLF4J log method.  If you pass in any kind of more complex
expression, Log4s will wrap it in an <tt>is<i>Level</i>Enabled</tt> call
automatically.  This is what SLF4J does when you use parameterized logging, but
Log4s does it transparently and can even auto-wrap additional calculation.

Compare the following:

```java
class JavaClass {
    ...    
    logger.trace("Element 1000: {}", linkedList.get(1000));
    ...
}
```

```scala
class ScalaClass {
  ...
  logger.trace(s"Element 1000: ${linkedList(1000)}")
  ...
}
```

In the Java API, parameterized logging is not enough: unless you wrap the call
with `isDebugEnabled`, you will still incur the cost of stepping through the
linked list to find element 1000 even if trace-level logging is disabled.  
Without manual intervention, SLF4J only avoids the cost of string 
concatenations.

However, Log4s can do better. Its macros discover at compile time that you are
constructing a dynamic log statement and automatically wrap the entire
calculation with `isDebugEnabled`.

The string interpolation syntax is not required for this detection, but it 
is usually the easiest and best-performing approach. 

### Exception logging

When logging an exception, it's always the best practice to attach send actual
exception object into your logging system. This gives you flexibility in how
it's displayed, the ability to do filtering, and additional options for things
like database logging.

Log4s allows you to pass exceptions into your logger, while still maintining
the simple string-interpolation style API that makes it so convenient. To log
an exception, use the following syntax.

```
try {
  ...
} catch {
  case e: Exception => logger.error(e)("Some error message")
}
```

There is no method to log an error message without any message, because this
is generally not a good practice. You can always feed it an empty string if
you really want. It's usually not desirable to use the exception's message, as
most logging systems will output this anyway.

Like regular message logging, exception logging uses macros to improve
performance in the case where the given logger is not enabled. If the log
message is dynamic, it will not be constructed unless the logger is disabled.

The macro also detect if the exception is dynamic, making it possible to write
code like below, and pay for only an `isTraceEnabled` unless trace logging is
enabled on the given logger.

```
logger.trace(new RuntimeException())("Got call into xyz")
```
