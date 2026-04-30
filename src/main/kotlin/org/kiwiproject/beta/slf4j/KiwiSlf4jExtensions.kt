package org.kiwiproject.beta.slf4j

import org.slf4j.Logger
import org.slf4j.event.Level

fun Logger.isEnabled(level: Level): Boolean = KiwiSlf4j.isEnabled(this, level)

@Deprecated(
    message = "Use SLF4J's fluent logging API: logger.atLevel(level).log(message)",
    replaceWith = ReplaceWith("this.atLevel(level).log(message)")
)
@Suppress("DEPRECATION", "removal", "kotlin:S1874", "kotlin:S1133")
fun Logger.log(level: Level, message: String) = KiwiSlf4j.log(this, level, message)

@Deprecated(
    message = "Use SLF4J's fluent logging API: logger.atLevel(level).log(format, arg)",
    replaceWith = ReplaceWith("this.atLevel(level).log(format, arg)")
)
@Suppress("DEPRECATION", "removal", "kotlin:S1874", "kotlin:S1133")
fun Logger.log(level: Level, format: String, arg: Any) =
    KiwiSlf4j.log(this, level, format, arg)

@Deprecated(
    message = "Use SLF4J's fluent logging API: logger.atLevel(level).log(format, arg1, arg2)",
    replaceWith = ReplaceWith("this.atLevel(level).log(format, arg1, arg2)")
)
@Suppress("DEPRECATION", "removal", "kotlin:S1874", "kotlin:S1133")
fun Logger.log(level: Level, format: String, arg1: Any, arg2: Any) =
    KiwiSlf4j.log(this, level, format, arg1, arg2)

// Add guard around log to avoid an array copy by the spread operator unless actually necessary.
// I don't know how else to "fix" the use of the spread operator here.
// See https://detekt.github.io/detekt/performance.html#spreadoperator for more information
@Deprecated(
    message = "Use SLF4J's fluent logging API: logger.atLevel(level).log(format, *arguments)",
    replaceWith = ReplaceWith("this.atLevel(level).log(format, *arguments)")
)
@Suppress("DEPRECATION", "removal", "SpreadOperator", "kotlin:S1874", "kotlin:S1133")
fun Logger.log(level: Level, format: String, vararg arguments: Any) {
    if (isEnabled(level)) {
        KiwiSlf4j.log(this, level, format, *arguments)
    }
}

@Deprecated(
    message = "Use SLF4J's fluent logging API: logger.atLevel(level).log(message, t)",
    replaceWith = ReplaceWith("this.atLevel(level).setCause(t).log(message)")
)
@Suppress("DEPRECATION", "removal", "kotlin:S1874", "kotlin:S1133")
fun Logger.log(level: Level, message: String, t: Throwable) =
    KiwiSlf4j.log(this, level, message, t)
