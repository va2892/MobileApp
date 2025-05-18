package ru.hitsmobileapp

class InterpreterContext {
    val variables = mutableMapOf<String, Int>()
    val outputs = mutableListOf<String>()

    fun evaluateExpression(expr: String): Int {
        val tokens = expr.split(Regex("(?<=[^a-zA-Z0-9_])|(?=[^a-zA-Z0-9_])"))
        val replaced = tokens.joinToString(" ") { token ->
            val trimmed = token.trim()
            when {
                trimmed.matches(Regex("[a-zA-Z_][a-zA-Z0-9_]*")) ->
                    variables[trimmed]?.toString() ?: throw Exception("Unknown variable: $trimmed")
                trimmed.matches(Regex("\\d+")) -> trimmed
                trimmed in listOf("+", "-", "*", "/", "%", "(", ")") -> trimmed
                trimmed.isBlank() -> ""
                else -> throw Exception("Invalid token: $trimmed")
            }
        }
        return ExpressionEvaluator().eval(replaced)
    }
}

class ExpressionEvaluator {
    fun eval(expression: String): Int {
        try {
            return object {
                var pos = -1
                var ch = 0
                fun nextChar() {
                    ch = if (++pos < expression.length) expression[pos].code else -1
                }
                fun eat(charToEat: Int): Boolean {
                    while (ch == ' '.code) nextChar()
                    if (ch == charToEat) {
                        nextChar()
                        return true
                    }
                    return false
                }
                fun parse(): Int {
                    nextChar()
                    val x = parseExpression()
                    if (pos < expression.length) throw RuntimeException("Unexpected: ${expression[pos]}")
                    return x
                }
                fun parseExpression(): Int {
                    var x = parseTerm()
                    while (true) {
                        x = when {
                            eat('+'.code) -> x + parseTerm()
                            eat('-'.code) -> x - parseTerm()
                            else -> return x
                        }
                    }
                }
                fun parseTerm(): Int {
                    var x = parseFactor()
                    while (true) {
                        x = when {
                            eat('*'.code) -> x * parseFactor()
                            eat('/'.code) -> x / parseFactor()
                            eat('%'.code) -> x % parseFactor()
                            else -> return x
                        }
                    }
                }
                fun parseFactor(): Int {
                    if (eat('+'.code)) return parseFactor()
                    if (eat('-'.code)) return -parseFactor()
                    var x: Int
                    val startPos = pos
                    if (eat('('.code)) {
                        x = parseExpression()
                        eat(')'.code)
                    } else if (ch in '0'.code..'9'.code) {
                        while (ch in '0'.code..'9'.code) nextChar()
                        x = expression.substring(startPos, pos).toInt()
                    } else {
                        throw RuntimeException("Unexpected: ${ch.toChar()}")
                    }
                    return x
                }
            }.parse()
        } catch (e: Exception) {
            throw Exception("Ошибка в выражении: ${e.message}")
        }
    }
}