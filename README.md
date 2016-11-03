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

    libraryDependencies += "org.log4s" %% "log4s" % "1.3.3"

Scala 2.10, 2.11, and 2.12 are fully supported, and enabling macro paradise
in the 2.10 compiler is not required: You can just add it like any other
dependency.

Scala 2.10 support will be maintained at least until at least June 2017.

## Examples ##

- [Getting a logger](#getting-a-logger)
- [Logging messages](#logging-messages)
- [Exception logging](#exception-logging)
- [Diagnostic contexts](#diagnostic-contexts)
- [Unsupported features](#unsupported-features)

### Getting a logger ###

Most of the time, you simply want to define a logger with a name that matches
the enclosing class or module.  Log4s makes this common case as easy as
possible, transparently deriving the name for you with zero overhead at runtime.

```scala
package com.example.project
import org.log4s._

class DemoClass {
  // Retrieves a logger for "com.example.project.DemoClass"
  private[this] val logger = getLogger
  ???
}
```

There is no requirement that you mark your loggers with `private[this]`, but
the compiler may bypass accessors and generate direct field access if you do.

It is not required that you import `org.log4s._` into your classes: calling
`org.log4s.getLogger` will also have the same effect. I generally recommend
importing the entire `log4s` package: it doesn't have many symbols that are
likely to conflict, and importing the package makes it easy to access other
logging features if needed.

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

#### Custom Logger Names ####

There are situations where you may want to use a custom logger name. E.g., you
may want to have a special category for some kind of high-level events, or you
may want to consolidate the logging of two related classes.

To accomplish this, you can simply pass a name directly to `getLogger`.

```scala
import org.log4s._

object CustomNamed {
  private[this] val queryLogger = getLogger("queries")
}
```

Although this is fully supported, I recommend that you use the automatic
class-named loggers most of the time. Class-named loggers provide useful
debugging information and usually align well with the decisions you'll make
about which logging statements you want to enable in which situations. By
letting the compiler provide the name for you, you also reduce the chance of
errors as you refactor your code.

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
that the string interpolations are not even performed unless the logger is
enabled. It does this by inspecting the structure of the argument that you pass
into the logger.

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
with `isTraceEnabled`, you will still incur the cost of stepping through the
linked list to find element 1000 even if trace-level logging is disabled.
Without manual intervention, SLF4J only avoids the cost of string
concatenations.

However, Log4s can do better. Its macros discover at compile time that you are
constructing a dynamic log statement and automatically wrap the entire
calculation with `isTraceEnabled`.

The string interpolation syntax is not required for this detection, but it
is usually the easiest and best-performing approach.

You can also use message nesting with entire code blocks. If logging is not
enabled at the provided level, the block is skipped entirely.

```scala
class ComplexTrace {
  ...
  logger trace {
    def helper(s: String) = ???
    val x = ...
    val y = helper(...)
    s"Combined trace message for $x: $y"
  }
}
```

You can, of course, accomplish the same thing using
`if (logger.isTraceEnabled) ...`. If the logger is disabled, they will have
identical performance. However, the explicit test may perform slightly better
than a block in the case where the logger is enabled as a closure may be
required to compile the block. (In most situations, these differences are
_completely_ negligible, but designing for zero overhead and documenting any
usage patterns that do add overhead is a major goal of Log4s.)

### Exception logging

When logging an exception, it's always the best practice to send the actual
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

Like regular message logging, dynamic arguments are only evaluated if the
provided logger is turned on. This includes both the `Throwable` and the
message itself.

This means you could use the following pattern to see who is calling a method,
and if you were to disable trace logging you would only pay for the call to
`isTraceEnabled`, which has a cost of only a few nanoseconds (according to the
[SLF4J FAQ](http://www.slf4j.org/faq.html#trace)).

```scala
object MyObject {
  def xyz() {
    logger.trace(new RuntimeException())("Got call into xyz")
    ???
  }
}
```

(This is more an illustration of the possibilities of dynamic message
processing than a suggestion that this is the best way to get caller
information. However, sometimes a low-tech solution like this can be a good
complement to more complex solutions like profilers and debuggers.)

### Diagnostic contexts

Mapped diagnostic contexts (MDCs) are a great way to put share common
contextual information across all log statements. Frameworks like Logback
have the ability to not just output them, but also use them in filtering to
select the type of logging to perform or even persist certain information in
databases.

MDCs in Log4s have the same semantics as those of standard MDCs in SLF4J.
In keeping with the design goal of making SLF4J idiomatic to Scala,
Log4s's MDCs implement the standard interface for a
[Scala mutable map](http://www.scala-lang.org/api/current/index.html#scala.collection.mutable.Map).

**Though I cover the map-style API first, see the [MDC convenience and
safety](#mdc-convenience-and-safety) section below for the simpler idiom that
I recommend for most situations.**

#### MDC Map-style API

The direct way to manipulate MDCs is through the `org.log4s.MDC` object.

```scala
import org.log4s._

object DiagnosticExample {
  private[this] val logger = getLogger

  def doRequest(user: String) {
    val requestId = java.util.UUID.randomUUID

    // Empty out the MDC for this thread
    MDC.clear

    /* *************************** */
    /* Set some context in the MDC */
    /* *************************** */

    // Set a single value
    MDC("request-id") = requestId.toString
    // Set multiple values
    MDC += ("request-user" -> user, "request-time" -> System.currentTimeMillis)

    // Note that Log4s requires the caller to do string conversion. This helps
    // ensure that it's really the implementation that you want.

    /* *************** */
    /* Use our context */
    /* *************** */

    // No need to put the request ID in the message: it's in the context
    logger.debug("Processing request")

    /* ************************ */
    /* Remove context variables */
    /* ************************ */

    // Remove a single value
    MDC -= "request-id"
    // Remove multiple values
    MDC -= ("request-user", "request-time")
  }
}
```

These are a few common examples, but all the mutator methods of a mutable
map will work. It's also possible to intermix calls to SLF4J's MDC methods
directly: the Log4s map is backed by the actual SLF4J MDC.

#### MDC convenience and safety

Note that the example above has a common bug: if some exception happens during
request processing, the MDC will not get cleaned up and it will leak to other
operations. Because of this common situation, there's a convenience method
that does cleanup in a finalizer block. I recommend using this approach for
most common settings.

```scala
import org.log4s._

object BlockExample {
  def doRequest(user: String) {
    val requestId = java.util.UUID.randomUUID

    // This context operates only for the block, then cleans itself up
    MDC.withCtx ("request-id" -> requestId.toString, "request-user" -> user) {
      logger.debug("Processing request")
    }
  }
}
```

Nesting context blocks is permitted. The inner context block retains the
values of the outer context. If there are conflicts, the inner block wins, but
the outer value is restored when the inner block is completed.

This ability to restore previous values on block exit does require their
storage in a map which adds slight memory overhead. If you are in a tight loop
with nested contexts, you may have better performance if you add and remove
values directly. These performance costs apply only to the block-based API,
not the map-style API.

### Unsupported features

The following potential or suggested features are not implemented. If some
missing feature is particularly valuable to you, feel free to reach out with
your requests or suggestions. I'm also—of course—open to pull requests,
but please drop me an email first if there are significant new APIs or
features so we can agree on the general design.

  * A `scalac` compiler flag or environment variable to automatically disable
    all logging below a certain level.
  * Marker support.


### Contributors

Here are all the contributors (chronologically). Thanks to all!

  * [Sarah Gerweck](https://github.com/sarahgerweck/) (primary author)
  * [Bryce Anderson](https://github.com/bryce-anderson)
  * [David Ross](https://github.com/dyross)
