package org.pareto.processGrammar

typealias Word = String

typealias RequiredArg = String

typealias Param = Process

typealias Params = List<Param>

typealias Namespace = Map<Fn.Name, OnWord>

object Keyword {
    const val END = "END"
}

/**
 * A name for a process. A [Fn.Name] is PascalCase and used for [Fn]'s.
 * An [Expanding.Name] is used for terminal [Expanding] processes.
 */
sealed class ProcessName(private val name: String) {
    override fun toString() = name

    protected fun String.isPascalCase() = "^([A-Z][a-z0-9]*)+$".toRegex().containsMatchIn(this)

    override fun hashCode() = name.hashCode()

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is ProcessName) return false
        if (name != other.name) return false
        return true
    }
}