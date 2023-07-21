import org.example.pg.*
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import kotlin.test.assertEquals
import kotlin.test.assertIs

class FunctionSpec {

    @Nested
    inner class DSL {
        @Test
        fun `functions can require params by defining with parens`() {
            val grammar = Grammar.fromDsl {
                "Function"("a", "b", "c") {
                    "a" then "b" then "c"
                }
            }
            val function = grammar.processes.first()
            assertEquals(3, function.requiredArgs.size)
        }

        @Test
        fun `references can pass params into function with required args`() {
            val grammar = Grammar.fromDsl {
                "Function"("a") {
                    "a" then "b"
                }

                "Caller" {
                    "Function"("foo") then "bar"
                }
            }
            val function = grammar.processes.first().process
            val caller = grammar.processes.last().process
            assertIs<Sequence>(function)
            assertIs<Sequence>(caller)
            assertEquals("foo", (caller.tick as Reference).params.first().canonical())
            assertEquals("bar", (caller.tock as Expanding).canonical())
        }

        @Test
        fun `parameter passing works`() {
            val grammar = Grammar.fromDsl {
                "Function"("a") {
                    "a" then "b"
                }

                "Caller" {
                    "Function"("foo") then "bar"
                }
            }
            Program.from(grammar)("Caller")("foo")("b")("bar")("END")
        }

        @Test
        fun `required params must appear in function body`() {
            assertThrows<DSLParseException> {
                Grammar.fromDsl {
                    "Function"("a", "b", "c", "d") {
                        "a" then "b" then "c"
                    }
                }
            }
        }

        @Test
        fun `param names must be unique within definition site`() {
            assertThrows<DSLParseException> {
                Grammar.fromDsl {
                    "Function"("a", "b", "b") {
                        "a" then "b" then "c"
                    }
                }
            }
        }

        @Test
        fun `nested function calls allowed`() {
            val grammar = Grammar.fromDsl {
                "Function"("a") {
                    "a" then "b"
                }

                "Caller" {
                    "Function"("Function"("foo")) then "bar"
                }
            }
            val body = grammar.processes.last().process as Sequence
            assertEquals("bar", body.tock.canonical())
            val tick = body.tick
            assertIs<Reference>(tick)
            assertEquals("Function", tick.referencedName.toString())
            assertEquals("Function(foo)" , tick.params.first().canonical())
        }
    }
}