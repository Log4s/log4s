# Log4s Changelog

Only changes affecting users of the library will be noted here. For internal
changes to how it's coded or built, see the Git history.

### 1.2.0
   * Improve naming of package loggers.
      * Previously, package loggers were named by the package name, followed
        by `.package`. Now, the trailing `.package` is no longer present.
      * I believe this is the desired behavior for most people, but if this
        change causes headaches for you, please file a ticket on GitHub. We
        can make the behavior optional with a compile-time switch.
   * Support for Scala 2.12.0-M1 and 2.12.0-M2.

#### 1.2.1

   * Fix an [issue](https://github.com/Log4s/log4s/issues/10) where
     duplicate MDC entries could lead to MDC value leaks, reported by
     [David Ross](https://github.com/dyross).
   * Support for Scala 2.12.0-M3.

### 1.3.0
   * Add support for Scala 2.12.0-M4.
