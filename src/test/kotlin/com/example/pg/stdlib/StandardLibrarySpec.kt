package com.example.pg.stdlib

import org.example.pg.Grammar
import org.example.pg.Keyword.END
import org.example.pg.NoMatchForInput
import org.example.pg.Program
import org.example.pg.compose
import org.example.pg.stdlib.Lib.OneOrMore
import org.example.pg.stdlib.Lib.Possible
import org.example.pg.stdlib.Lib.Repeating
import org.example.pg.stdlib.Lib.ZeroOrMore
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.assertDoesNotThrow
import org.junit.jupiter.api.assertThrows
import kotlin.test.Test

class StandardLibrarySpec {

    val F = "F"
    val Heart = "Heart"
    val beat = "beat"
    val a = "a"
    val b = "b"
    val c = "c"

    @Test
    fun `standard library defines helper functions which can be used in DSL`() {
        val grammar = Grammar.compose {
            Heart {
                Repeating(beat)
            }
        }
        // sanity check that it works as expected
        val prog = Program.from(grammar)
        prog(Heart)(beat)(beat)(beat)
        assertThrows<NoMatchForInput> { prog("beeeet") }
    }

    @Nested
    inner class Functions {
        @Test
        fun `Possible behaves as expected`() {
            val grammar = Grammar.compose {
                F {
                    a then Possible(b) then c
                }
            }
            assertDoesNotThrow { Program.from(grammar)(F)(a)(b)(c)(END) }
            assertDoesNotThrow { Program.from(grammar)(F)(a)(c)(END) }
        }

        @Test
        fun `Repeating and OneOrMore behave as expected`() {
            val repeatingGrammar = Grammar.compose {
                F {
                    Repeating(b)
                }
            }
            val oneOrMoreGrammar = Grammar.compose {
                F {
                    OneOrMore(b)
                }
            }
            val prog1 = Program.from(repeatingGrammar)
            val prog2 = Program.from(oneOrMoreGrammar)
            assertDoesNotThrow { prog1(F)(b)(b)(b)(b) }
            assertDoesNotThrow {
                prog2(F)
                prog2(b)
                prog2(b)
                prog2(b)(b) }
            assertThrows<NoMatchForInput> { prog1(END) }
            assertDoesNotThrow { prog2(END) }

            assertThrows<NoMatchForInput> {
                Program.from(oneOrMoreGrammar)(F)(END)
            }
        }

        @Test
        fun `zeroOrMore behaves as expected`() {
            val g = Grammar.compose {
                F {
                    a then ZeroOrMore(b) then c
                }

                "G" {
                    ZeroOrMore("x")
                }
            }
            assertDoesNotThrow { Program.from(g)(F)(a)(c) }
            assertDoesNotThrow { Program.from(g)(F)(a)(b)(c) }
            assertDoesNotThrow { Program.from(g)(F)(a)(b)(b)(c) }

            assertDoesNotThrow { Program.from(g)("G")("END") }
            assertDoesNotThrow { Program.from(g)("G")("x")("END") }
            assertDoesNotThrow { Program.from(g)("G")("x")("x")("END") }
        }
    }
}
