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


## Examples ##

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

Automatic logger naming also works for modules (_AKA_ objects or 
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
