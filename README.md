# Log4s

**Note:** version 1.6 and above have *experimental* support for
[Scala.js](https://www.scala-js.org/). See the table of contents below for
documentation.

To get started quickly, you can add this dependency to your `build.sbt`

    libraryDependencies += "org.log4s" %% "log4s" % "1.8.2"

## Topics

- [Introduction](#introduction)
- [Requirements](#requirements)
- [Using Log4s](#using-log4s)
  - [Getting a logger](#getting-a-logger)
  - [Performing logging](#logging-messages)
  - [Exception logging](#exception-logging)
  - [Diagnostic contexts](#diagnostic-contexts)
- [Scala.js support](#scalajs-support)
- [Log4s Testing](#testing)
- [Unsupported features](#unsupported-features)
- [Contributors](#contributors)

## Introduction

Logging is a generally solved problem on the JVM, thanks largely to the
excellent work of Ceki Gülcü and many others. The [SLF4J](http://slf4j.org)
library solves the problem of abstracting logging over different frameworks
on the JVM, and frameworks like [Logback](https://logback.qos.ch/) and
[Log4j 2](https://logging.apache.org/log4j/2.x/) are both flexible and
powerful.

On the JVM, Log4s simply sits on top of these existing subsystems. Scala's
macro and value classes, enable Log4s provide an idiomatic Scala façade that
does not impose runtime overhead and that frequently outperforms the common
usage patterns of the JVM APIs.

Log4s also provides some additional functionality to improve the ease of
logging-related development, including the Log4s Testing framework for
facilitating the testing of logging-related code.

## Using Log4s

### Requirements

Scala 2.11, 2.12 and 2.13 are fully supported. No special settings or
compiler options are required: just add the dependency as described
above.

#### Scala 2.10

Scala 2.10 support is still present, but it is beyond its support window: it
may be removed in any future minor release if there's a reason. (It will not
be removed in a patch release.)

The macro paradise compiler extensions are not required for Scala 2.10.

### Getting a logger

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

#### Custom Logger Names

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

#### Instance or static?

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

### Logging messages

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

```scala
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
    MDC.withCtx("request-id" -> requestId.toString, "request-user" -> user) {
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

## Scala.js Support

**Scala.js support is currently experimental.** It should be stable enough to
use reliably, but there may be API changes in the future. If there are changes,
they would likely be to either the configuration system or to the JavaScript native APIs.

Many Scala.js-specific APIs are in the `org.log4s.log4sjs` package. It is not
currently recommended that you import this full package. There may be many public
APIs in here that become private later.

### Scala-defined usage

Your Scala code that targets JavaScript can retrieve and use loggers *exactly* the same way
that you would when targeting the JVM, fulfilling the basic promise of Scala.js.

#### Configuration

Unlike when targeting the JVM, standard frameworks like Logback or Log4j are
not available to do the configuration of the logging system. Instead, there is
an API that you can call to adjust logging thresholds and appenders.

Normally, you will call this API very early on during your application's
startup to set up your logging configuration. However, you can adjust the
settings at any time.

```scala
import org.log4s._

def initLogging(): Unit = {
  import Log4sConfig._

  /* Set `org.log4s.foo` and any children to log only Info or higher */
  setLoggerThreshold("org.log4s.foo", Info)

  /* Set `org.log4s` to not log anything. This will not override the specific
   * setting we already applied to `org.log4s.foo`. */
  setLoggerThreshold("org.log4s", OffThreshold)

  /* Set to log everything */
  setLoggerThreshold("", AllThreshold)

  /* Unset a previously customized threshold. *Now* this category will inherit
   * from the parent level, which we disabled. */
  resetLoggerThreshold("org.log4s.foo")

  /* Add a custom appender */
  val myAppender = { ev: log4sjs.LoggedEvent => ??? }
  /* Add a custom appender, leaving others in place */
  addLoggerAppender("org.example", myAppender)

  /* Set the specific appenders. The `additive` parameter controls whether
   * this is in addition to the appenders of the parent logger. The `false`
   * here means to *not* include any parent appenders. */
  setLoggerAppenders("org.log4s.audit", false, Seq(myAppender))

  /* The `true` here means that myAppender` from the `audit` logger will still
   * be called since it allows additive inheritance. */
  val appender2 = { ev: log4sjs.LoggedEvent => ??? }
  setLoggerAppenders("org.log4s.audit.detailed", true, Seq(appender2))

  /* Resets the logger to default settings. This also adjusts the
   * `org.log4s.audit.detailed` appenders, since that logger inherits
   * appenders from its parents */
  resetLoggerAppenders("org.log4s.audit")
}
```

### JavaScript direct usage

To JavaScript, Log4s exposes a few key methods as a module so that you can
access logging facilities from any JavaScript code you might have. You should
consult the Scala.js documentation for how to get access to your modules from
JavaScript.

Note that all the examples below will assume you have already imported the
module under the name `log4s`, which could look like this for a separately
packaged log4s.

```javascript
var log4s = require('log4s-opt.js')
```

#### Basic logging

`getLogger` is a top-level function that takes a String and gives you back a
logger object, just as you'd expect in Scala.

The methods on a logger are straightforward:

```javascript
var logger = log4s.getLogger("org.log4s")

logger.debug("testing")

if (logger.isWarnEnabled) {
  logger.warn("Something went wrong", new Error())
}
```

The names of the log levels are the same as in Scala. Note that doing
trace-level logging does not trigger the JavaScript's console trace, which
automatically dumps a stack trace. if you want this behavior, you can always
add a custom appender that inspects the level. If you have advanced needs in
this area, please file a feature request.

#### MDCs in JavaScript

The MDC is available through JavaScript just as it is in Scala. Here's an example

```javascript
/* Clear the MDC before we start */
log4s.MDC.clear()

log4s.MDC.put("user", "john.doe")
/* Do some logging */
log4s.MDC.remove("user")
/* Fetch an MDC value (usually not recommended) */
log4s.MDC.get("user")
/* Or get a copy of the entire MDC (also not usually recommended) */
log4s.MDC.getCopyOfContextMap()
```

Just as in Scala, there's a "with context" method that automatically handles
any cleanup for you. It's not quite as convenient in JavaScript as in Scala,
but it can still be a good way to ensure your MDC gets cleaned up. These are
curried functions: you pass it a context and then it gives you new function to
which you pass a zero-argument function that does the work.

```javascript
var logger = log4s.getLogger("com.example")
/* With a single MDC value */
log4s.MDC.withCtx("user", "jane.roe")(() => {
  /* Do some stuff */
  logger.debug("User did something")
})
/* With several MDC values */
log4s.MDC.withCtx({"user": "benway", "query": "1234"})(() => {
  /* Do some stuff */
  logger.debug("Use with complext context did something")
})
```

#### JavaScript Configuration Objects

The same basic methods that you would use to do Scala-defined configuration
are available through JavaScript. See the documentation above for details on
how to use them.

```javascript
log4s.Config.setLoggerThreshold("org.log4s", log4s.Info)
log4s.Config.resetLoggerThreshold("org.log4s")
log4s.Config.addLoggerAppender("org", e => console.log(e.level.name + ": " + e.message))
log4s.Config.setLoggerAppenders("org.test", false, [e => console.log(e.message)])
log4s.Config.resetLoggerAppenders("org")
```

Appenders can be created by passing in a JavaScript function.

For parameters, there are top-level threshold/level objects available:

- `AllThreshold`
- `Trace`
- `Debug`
- `Info`
- `Warn`
- `Error`
- `OffThreshold`

## Log4s-Testing

There is a Logback-specific testing library that allows you to do mock-object
style testing of your log messages if you'd like. This was built for internal
testing of Log4s, but it has been made public by request.

### Setup

This only works if you are using Logback as your logging framework, at
least during testing. (Doing this will not interfere with using a different
framework for your runtime logging if you correctly configure the two
classpaths.)

#### SBT config

    libraryDependencies += "org.log4s" %% "log4s-testing" % log4sVersion % "test"

I recommend you use a `val log4sVersion` to match the version number with the
main Log4s dependency.

#### Logback config

You'll then want to add lines like the following in your `logback-test.xml`

```xml
<appender name="TEST" class="org.log4s.TestAppender"/>
<logger name="org.log4s.abc" additivity="false" level="TRACE">
  <!-- Set additivity to `false` if you don't want this logging to actually go to the main output. -->
  <appender-ref ref="TEST" />
</logger>
```

Full documentation of this is beyond the scope of this document, but you need
to create the custom appender and register it with the appropriate categories.
Note that Logback's Groovy-based configuration is more convenient and flexible
than the XML for more complicated logging configurations, but it's less
familiar and adds an extra runtime dependency on Groovy.


### Usage

The steps are relatively simple

1. Get access to a logger that hooked up to your appender
1. Write one or more events to that logger
1. Call into the TestAppender object to dequeue the events and inspect them

Here's a simple example of code you might write:

```scala
import org.log4s._
import org.scalatest._

/* An auto-named logger would work just as well as long as that logger
 * is hooked up in your Logback configuration. */
val testLogger = getLogger("org.log4s.abc")

TestAppender.withAppender() {
  testLogger.debug("Here's a test message")
  val eventOpt = TestAppender.dequeue
  eventOpt should be ('defined)
  eventOpt foreach { e =>
    e.message should equal ("Here's a test message")
    e.throwable should not be 'defined
  }
}
```

More examples are available if you look through the various test classes
in this project.

### Should I test my logging?

Testing scope and philosophy is a complex topic far beyond the reach of this
docuemnt, but I can give some general guidance based on my personal views.

Good tests validate the behavior that callers or users should expect when
interacting with your code, but not arbitrary implementation details. Quality
tests don't fail just because you changed something—unless that thing impacts
the expected behavior of the code. My view is effectively that tests should
attempt to fully verify the black-box behavior of a piece of code while
ignoring details that do not affect black-box behavior. (However, white-box
development techniques may be useful to provide this verification, and
judgment is required in the drawing of the lines.)

I'll give two examples.

At one extreme is a trace statement that is used for developer debugging. You
probably would not benefit from crying wolf with a test failure just because
you added an additional bit of detail to a debug statement or you changed its
punctuation. In my opinion, this log statement is probably not part of the
code's specification and should not be tested.

At the other extreme would be a scenario where you're using your logging
framework to generate audit logs that track access to secured resources. This
is important functionality that may be required for regulatory compliance. It
is _strongly_ advisable to develop tests that ensure this log is written as
expected. I would probably consider a white-box unit test to ensure the messages
went to the right logger in the right format and a black-box test that inspected
the actual output log files to ensure events were being written in an end-to-end
manner.

Most real situations will lie between these two extremes, and you will need to
use judgment. In my estimation, most applications probably do not need or want
tests that verify the details of their logging, but there are many situations
where this testing is approrpiate.

## Unsupported features

The following potential or suggested features are not implemented. If some
missing feature is particularly valuable to you, feel free to reach out with
your requests or suggestions. I'm also—of course—open to pull requests,
but please drop me an email first if there are significant new APIs or
features so we can agree on the general design.

- A `scalac` compiler flag or environment variable to automatically disable
  all logging below a certain level.
- Marker support.


## Contributors

### Maintainers

- [Sarah Gerweck](https://github.com/sarahgerweck/) (creator & primary maintainer)
- [Ross A. Baker](https://github.com/rossabaker)

### Additional contributors

Here are all other contributors, listed chronologically. Thanks to all!

- [Bryce Anderson](https://github.com/bryce-anderson)
- [David Ross](https://github.com/dyross)
- [Seth Tisue](https://github.com/SethTisue)
- [Michal](https://github.com/mkows)
- [Sean Sullivan](https://github.com/sullis)
- [Olli Helenius](https://github.com/liff)
- [Raúl Piaggio](https://github.com/rpiaggio)
- [Sergey Torgashov](https://github.com/satorg)
