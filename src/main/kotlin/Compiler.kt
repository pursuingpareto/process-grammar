package org.example.pg

object Expanders {
    val equality: Expander = {
        ("$this" == it) || throw NoMatchForInput(it)}
}

/**
 * Compiles a [Grammar] into a runnable [Program].
 *
 * Compilation produces a [Program] by converting each [Function] process into a
 * single function of type [OnWord].
 *
 * Each of these functions can be called by passing it a word
 */
@Suppress("UNCHECKED_CAST")
class Compiler(private val expander: Expander = Expanders.equality)  {

    private val namespace: MutableNamespace = mutableMapOf()

    fun compile(grammar: Grammar): Program {
        namespace.clear()
        grammar.processes.forEach { functionalize(it) }
        return Program(namespace)
    }

    private fun expanding(obj: Name.Expanding): OnWord = {
        word -> expander(obj, word) }

    private fun optional(process: OnWord): OnWord = {
        word -> try { process(word) }
                catch (e: UnrunnableProcess) { null } }

    private fun decision(a: OnWord, b: OnWord) = { word: Word ->
        val attempts = listOf(a, b).map {
            try { it(word) }
            catch (e: ProcessException) { false } }
        if (attempts.all  { it == false }) throw NoMatchForInput(word)
        if (attempts.none { it == false }) throw AmbiguousBranching()
        if (attempts.first() == false) attempts.last() else attempts.first() }

    private fun ref(name: Name.Defined) = { word: Word ->
        this.namespace[name]?.invoke(word) ?: throw NoMatchForInput(word) }

    private fun sequence(x: OnWord, y: OnWord): OnWord = { word: Word ->
        when (val xw = x(word)) {
            null -> y(word)
            true -> { w: Word -> y(w) }
            else -> sequence(xw as OnWord, y) } }

    private fun named(onWord: OnWord, name: Name.Defined): OnWord {
        this.namespace[name] = onWord
        return { word: Word -> onWord(word) } }

    private fun functionalize(process: Process): OnWord = when (process) {
        is Dimension.Time   -> sequence(functionalize(process.tick), functionalize(process.tock))
        is Dimension.Choice -> decision(functionalize(process.left), functionalize(process.right))
        is Optional         -> optional(functionalize(process.process))
        is Expanding        -> expanding(process.obj)
        is Function         -> named(functionalize(process.process), process.name)
        is Reference        -> ref(process.referencedName)
        else                -> throw UnrunnableProcess("not supported ${process}!") }
}