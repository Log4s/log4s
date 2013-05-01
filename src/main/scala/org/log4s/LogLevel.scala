package org.log4s

sealed trait LogLevel
final object Error extends LogLevel
final object Warn extends LogLevel
final object Info extends LogLevel
final object Debug extends LogLevel
final object Trace extends LogLevel