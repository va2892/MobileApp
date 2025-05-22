package ru.hitsmobileapp

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

class InterpreterContext {
    val variables = mutableMapOf<String, Int>()
    val outputs = mutableListOf<String>()
    val arrays = mutableMapOf<String, IntArray>()

    fun evaluateExpression(expr: String): Int {
        val trimmed = expr.trim()

        val arrayAccessRegex = Regex("""([a-zA-Z_][a-zA-Z0-9_]*)\[(.+)]""")
        val match = arrayAccessRegex.matchEntire(trimmed)
        if (match != null) {
            val arrayName = match.groupValues[1]
            val indexExpr = match.groupValues[2]
            val index = evaluateExpression(indexExpr)
            val array = arrays[arrayName] ?: throw Exception("Array '$arrayName' not found")
            if (index !in array.indices) throw Exception("Index $index out of bounds")
            return array[index]
        }

        val evaluator = SimpleMathEvaluator(variables)
        return evaluator.eval(trimmed)
    }

    class SimpleMathEvaluator(private val variables: Map<String, Int> = emptyMap()) {

        fun eval(expression: String): Int {
            val replaced = replaceVariables(expression)
            val tokens = tokenize(replaced)
            val postfix = infixToPostfix(tokens)
            return evaluatePostfix(postfix)
        }

        private fun replaceVariables(expression: String): String {
            var result = expression
            for ((key, value) in variables) {
                result = result.replace("\\b$key\\b".toRegex(), value.toString())
            }
            return result
        }

        private fun tokenize(expr: String): List<String> {
            val regex = Regex("""\d+|[()+\-*/]""")
            return regex.findAll(expr.replace(" ", "")).map { it.value }.toList()
        }

        private fun infixToPostfix(tokens: List<String>): List<String> {
            val precedence = mapOf("+" to 1, "-" to 1, "*" to 2, "/" to 2)
            val output = mutableListOf<String>()
            val stack = ArrayDeque<String>()

            for (token in tokens) {
                when {
                    token.matches(Regex("""\d+""")) -> output.add(token)
                    token in "+-*/" -> {
                        while (stack.isNotEmpty() && precedence[token]!! <= precedence[stack.last()] ?: 0) {
                            output.add(stack.removeLast())
                        }
                        stack.addLast(token)
                    }
                    token == "(" -> stack.addLast(token)
                    token == ")" -> {
                        while (stack.isNotEmpty() && stack.last() != "(") {
                            output.add(stack.removeLast())
                        }
                        if (stack.isNotEmpty() && stack.last() == "(") stack.removeLast()
                    }
                }
            }
            while (stack.isNotEmpty()) {
                output.add(stack.removeLast())
            }
            return output
        }

        private fun evaluatePostfix(postfix: List<String>): Int {
            val stack = ArrayDeque<Int>()
            for (token in postfix) {
                when {
                    token.matches(Regex("""\d+""")) -> stack.addLast(token.toInt())
                    token in "+-*/" -> {
                        val b = stack.removeLast()
                        val a = stack.removeLast()
                        val res = when (token) {
                            "+" -> a + b
                            "-" -> a - b
                            "*" -> a * b
                            "/" -> if (b != 0) a / b else throw Exception("Division by zero")
                            else -> throw Exception("Invalid operator: $token")
                        }
                        stack.addLast(res)
                    }
                }
            }
            return stack.last()
        }
    }

    fun setArrayValue(name: String, index: Int, value: Int) {
        val array = arrays[name] ?: throw Exception("Array '$name' not declared")
        if (index !in array.indices) throw Exception("Index $index out of bounds")
        array[index] = value
    }
}

