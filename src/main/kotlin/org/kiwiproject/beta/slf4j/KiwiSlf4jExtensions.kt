package org.kiwiproject.beta.slf4j

import org.slf4j.Logger
import org.slf4j.event.Level

fun Logger.isEnabled(level: Level): Boolean = KiwiSlf4j.isEnabled(this, level)

fun Logger.log(level: Level, message: String) = KiwiSlf4j.log(this, level, message)

fun Logger.log(level: Level, format: String, arg: Any) =
    KiwiSlf4j.log(this, level, format, arg)

fun Logger.log(level: Level, format: String, arg1: Any, arg2: Any) =
    KiwiSlf4j.log(this, level, format, arg1, arg2)

fun Logger.log(level: Level, format: String, vararg arguments: Any) =
    KiwiSlf4j.log(this, level, format, *arguments)

fun Logger.log(level: Level, message: String, t: Throwable) =
    KiwiSlf4j.log(this, level, message, t)
