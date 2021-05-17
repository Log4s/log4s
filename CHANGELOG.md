# Log4s Changelog

Only changes affecting users of the library will be noted here. For internal
changes to how it's coded or built, see the Git history.

## 1.2

* Improve naming of package loggers.
  * Previously, package loggers were named by the package name, followed by
    `.package`. Now, the trailing `.package` is no longer present.
  * I believe this is the desired behavior for most people, but if this change
    causes headaches for you, please file a ticket on GitHub. We can make the
    behavior optional with a compile-time switch.
* Support for Scala 2.12.0-M1 and 2.12.0-M2.

### 1.2.1

* Fix an [issue](https://github.com/Log4s/log4s/issues/10) where
  duplicate MDC entries could lead to MDC value leaks, reported by
  [David Ross](https://github.com/dyross).
* Support for Scala 2.12.0-M3.

## 1.3

* Add support for Scala 2.12.0-M4.

### 1.3.1

* Modernize build for SBT 1.0.
* Add support for Scala 2.12.0-M5 and 2.12.0-RC1.

### 1.3.2

* Add support for Scala 2.12.0-RC2.

### 1.3.3

* Build for Scala 2.12.0.
* Drop build support for 2.12 milestones
* Update to SBT 0.13.13

### 1.3.4

* Bump to Scala 2.12.1.
* Further drop 2.12 milestones from build

### 1.3.5

* Minor version updates.

### 1.3.6

* Add support for Scala 2.13.0-M1.

## 1.4

* Update to Scala 2.12.3.
* Update to SBT 1.0.2.
* Fix [#18](https://github.com/Log4s/log4s/issues/18), which prevented
  `getLogger` from correctly getting the logger for a top-level class with a
  higher-kinded type parameter.

## 1.5

* Update to Scala 2.12.4.
* Update to SBT 1.1.1.
* Expose a new `log4s-testing`. This is experimental at this time.

## 1.6

* **Experimental** support for Scala.JS
  * This is functional, but any JavaScript-specific APIs may change.

### 1.6.1

* Update Scala to 2.12.5
  * The string-interpolation performance improvements of Scala 2.12.5 will
    not benefit Log4s's runtime performance because it doesn't make use of
    string interpolation. (It won't hurt performance either.) Compile-time
    performance may be slightly better.

## 1.7

**NOTE** This version breaks binary compatibility under Scala.JS

* Add Scala support for 2.13.0-M5
* Update Scala.js to 0.6.26
* Reduce top-level exports in Scala.js
  * To reduce the likelihood of conflicts in combined apps, `getLogger` &
    `Log4s` are now the only top-level exports. If you want to access objects
    like `Info`, you should now call `Log4s.Info`.
  * Log4s support is still considered experimental.

## 1.8

* Add support for Scala 2.13.0-RC1 and 2.13.0-RC2 (thanks @rossabaker)
* Update Scala.js to 0.6.27

### 1.8.1

* Add support for Scala 2.13.0-RC3 (thanks @rossabaker)
* Update Scala.js to 0.6.28

### 1.8.2

* Add support for Scala 2.13.0

## 1.9

* Set internal flag to prevent second initialization
* Use fully-qualified `classOf` from `scala.Predef`
* Add support for Scala.js 1.0
* Drops support for Scala 2.10
* Drops support for Scala.js 0.6
* Built against Scala 2.13.3 and 2.12.12

## 1.10

### 1.10.0-M1

* **Experimental** support for Scala 3.0.0-M1. Currently JVM only.

### 1.10.0-M2

* Remove scala-reflect, scalatest, and scalacheck from compile scope.

### 1.10.0-M3

* Add support for Scala 3.0.0-M2.

### 1.10.0-M4

* Add support for Scala 3.0.0-M3.
* Drop support for Scala 3.0.0-M1.
* 2.13.x version built against Scala 2.13.4.

### 1.10.0-M5

* Add support for Scala 3.0.0-RC1.
* Drop support for Scala 3.0.0-M2.
* Upgrade to slf4j-1.7.30

### 1.10.0-M6

* Add support for Scala 3.0.0-RC2.
* Drop support for Scala 3.0.0-M3.
* Upgrade to slf4j-1.7.30
* 2.12.x version built against 2.12.13
* 2.13.x version built against 2.13.5

### 1.10.0-M7

* Add support for Scala 3.0.0-RC3
* Drop support for Scala 3.0.0-RC1

### 1.10.0-RC1

* Add support for Scala 3.0.0
* Drop support for Scala 3.0.0-RC2 and 3.0.0-RC3
* Add **experimental** support for Scala.js on Scala 3.

### 1.10.0

* Release 1.10.0-RC1 as final.
