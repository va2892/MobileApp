package ru.hitsmobileapp

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun CodeBlockInterpreter() {
    val codeBlocks = remember { mutableStateListOf<CodeBlock>() }
    var output by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp)
            .padding(vertical = 40.dp)
    ) {
        Text("CodeBlock Visual Interpreter", fontSize = 24.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 16.dp))

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(onClick = { codeBlocks += CodeBlock.VariableDeclaration() }) { Text("+ Var") }
            Button(onClick = { codeBlocks += CodeBlock.ArrayDeclaration() }) { Text("+ Arr") }
        }

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(onClick = { codeBlocks += CodeBlock.IfBlock() }) { Text("+ If") }
            Button(onClick = { codeBlocks += CodeBlock.WhileBlock() }) { Text("+ While") }
            Button(onClick = { codeBlocks += CodeBlock.ForBlock() }) { Text("+ For") }
        }

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(onClick = { codeBlocks += CodeBlock.Assignment() }) { Text("Var =") }
            Button(onClick = { codeBlocks += CodeBlock.ArrayAssignment() }) { Text("arr[i] =") }
            Button(onClick = { codeBlocks += CodeBlock.ArrayFillBlock() }) { Text("arr = ") }
        }

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(onClick = { codeBlocks += CodeBlock.SwapBlock() }) { Text("Swap") }
            Button(onClick = { codeBlocks += CodeBlock.ExpressionBlock() }) { Text("+ Expr") }
        }

        Spacer(modifier = Modifier.height(16.dp))

        codeBlocks.forEach { block ->
            CodeBlockUI(block, onDelete = { codeBlocks.remove(block) })
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(onClick = {
            try {
                val context = InterpreterContext()
                codeBlocks.forEach { it.execute(context) }

                val variableLines = context.variables.entries.joinToString("\n") { "${it.key} = ${it.value}" }

                val arrayLines = context.arrays.entries.joinToString("\n") { (name, array) ->
                    "$name = [${array.joinToString(", ")}]"
                }

                val expressionLines = context.outputs.joinToString("\n")

                output = listOf(variableLines, arrayLines, expressionLines)
                    .filter { it.isNotBlank() }
                    .joinToString("\n")

                error = null
            } catch (e: Exception) {
                error = e.message
            }
        }) { Text("Run") }

        Spacer(modifier = Modifier.height(16.dp))

        error?.let {
            Text("Error: $it", color = Color.Red, fontWeight = FontWeight.Bold)
        }
        if (output.isNotBlank()) {
            Text("Output:", fontWeight = FontWeight.Bold)
            Text(output)
        }
    }
}



sealed class CodeBlock {
    abstract fun execute(context: InterpreterContext)

    class VariableDeclaration(names: String = "") : CodeBlock() {
        var names by mutableStateOf(names)

        override fun execute(context: InterpreterContext) {
            val variables = names.split(",").map { it.trim() }
            for (name in variables) {
                if (name.isEmpty() || !name.matches(Regex("[a-zA-Z_][a-zA-Z0-9_]*")))
                    throw Exception("Invalid variable name: '$name'")
                context.variables[name] = 0
            }
        }
    }

    class Assignment(variable: String = "", expression: String = "") : CodeBlock() {
        var variable by mutableStateOf(variable)
        var expression by mutableStateOf(expression)

        override fun execute(context: InterpreterContext) {
            val trimmedVar = variable.trim()
            val value = context.evaluateExpression(expression)
            if (!context.variables.containsKey(trimmedVar))
                throw Exception("Undeclared variable: $trimmedVar")
            context.variables[trimmedVar] = value
        }
    }

    class ExpressionBlock(
        expression: String = ""
    ) : CodeBlock() {
        var expression by mutableStateOf(expression)

        override fun execute(context: InterpreterContext) {
            val result = context.evaluateExpression(expression)
            context.outputs += "answer = $result"
        }
    }


    class IfBlock(
        left: String = "",
        op: String = "",
        right: String = "",
        val body: SnapshotStateList<CodeBlock> = mutableStateListOf(),
        val elseIfBlocks: SnapshotStateList<ElseIfBlock> = mutableStateListOf(),
        elseBlock: ElseBlock? = null
    ) : CodeBlock() {
        var left by mutableStateOf(left)
        var op by mutableStateOf(op)
        var right by mutableStateOf(right)

        var elseBlock by mutableStateOf(elseBlock)

        override fun execute(context: InterpreterContext) {
            val leftVal = context.evaluateExpression(left)
            val rightVal = context.evaluateExpression(right)

            val condition = when (op) {
                ">" -> leftVal > rightVal
                "<" -> leftVal < rightVal
                "==" -> leftVal == rightVal
                "!=" -> leftVal != rightVal
                ">=" -> leftVal >= rightVal
                "<=" -> leftVal <= rightVal
                else -> throw Exception("Invalid operator: $op")
            }

            if (condition) {
                body.forEach { it.execute(context) }
            } else {
                var executed = false
                for (elseif in elseIfBlocks) {
                    val l = context.evaluateExpression(elseif.left)
                    val r = context.evaluateExpression(elseif.right)
                    val cond = when (elseif.op) {
                        ">" -> l > r
                        "<" -> l < r
                        "==" -> l == r
                        "!=" -> l != r
                        ">=" -> l >= r
                        "<=" -> l <= r
                        else -> throw Exception("Invalid operator: ${elseif.op}")
                    }
                    if (cond) {
                        elseif.body.forEach { it.execute(context) }
                        executed = true
                        break
                    }
                }
                if (!executed) {
                    elseBlock?.body?.forEach { it.execute(context) }
                }
            }
        }
    }

    class ElseIfBlock(
        left: String = "",
        op: String = "",
        right: String = "",
        val body: SnapshotStateList<CodeBlock> = mutableStateListOf()
    ) : CodeBlock() {
        var left by mutableStateOf(left)
        var op by mutableStateOf(op)
        var right by mutableStateOf(right)

        override fun execute(context: InterpreterContext) {
            val leftVal = context.evaluateExpression(left)
            val rightVal = context.evaluateExpression(right)

            val condition = when (op) {
                ">" -> leftVal > rightVal
                "<" -> leftVal < rightVal
                "==" -> leftVal == rightVal
                "!=" -> leftVal != rightVal
                ">=" -> leftVal >= rightVal
                "<=" -> leftVal <= rightVal
                else -> throw Exception("Invalid operator: $op")
            }

            if (condition) {
                body.forEach { it.execute(context) }
            }
        }
    }

    class ElseBlock(
        val body: SnapshotStateList<CodeBlock> = mutableStateListOf()
    ) : CodeBlock() {
        override fun execute(context: InterpreterContext) {
            body.forEach { it.execute(context) }
        }
    }


    class WhileBlock(
        left: String = "",
        op: String = "",
        right: String = "",
        val body: SnapshotStateList<CodeBlock> = mutableStateListOf()
    ) : CodeBlock() {
        var left by mutableStateOf(left)
        var op by mutableStateOf(op)
        var right by mutableStateOf(right)

        override fun execute(context: InterpreterContext) {
            var iterationCount = 0

            while (true) {
                val leftVal = context.evaluateExpression(left)
                val rightVal = context.evaluateExpression(right)

                val condition = when (op) {
                    ">" -> leftVal > rightVal
                    "<" -> leftVal < rightVal
                    "==" -> leftVal == rightVal
                    "!=" -> leftVal != rightVal
                    ">=" -> leftVal >= rightVal
                    "<=" -> leftVal <= rightVal
                    else -> throw Exception("Invalid operator: $op")
                }

                if (!condition) break

                if (iterationCount++ > 1000) throw Exception("Possible infinite loop")
                body.forEach { it.execute(context) }
            }
        }
    }

    class ForBlock(
        variable: String = "i",
        from: String = "",
        to: String = "",
        step: String = "1",
        val body: SnapshotStateList<CodeBlock> = mutableStateListOf()
    ) : CodeBlock() {
        var variable by mutableStateOf(variable)
        var from by mutableStateOf(from)
        var to by mutableStateOf(to)
        var step by mutableStateOf(step)

        override fun execute(context: InterpreterContext) {
            val name = variable.trim()
            var i = context.evaluateExpression(from)
            val end = context.evaluateExpression(to)
            val step = context.evaluateExpression(step)
            var iterations = 0

            if (name.isEmpty())
                throw Exception("Invalid variable name: '$name'")

            if (step == 0) throw Exception("Step cannot be zero")

            while ((step > 0 && i <= end) || (step < 0 && i >= end)) {
                if (iterations++ > 1000) throw Exception("possible infinite loop")
                context.outputs += "$name: $i"
                body.forEach { it.execute(context) }
                i += step
            }
        }
    }


    class ArrayDeclaration(name: String = "", size: String = "") : CodeBlock() {
        var name by mutableStateOf(name)
        var size by mutableStateOf(size)

        override fun execute(context: InterpreterContext) {
            val arraySize = context.evaluateExpression(size)
            if (arraySize <= 0) throw Exception("Invalid array size")
            context.arrays[name.trim()] = IntArray(arraySize)
        }
    }

    class ArrayAssignment(
        name: String = "",
        index: String = "",
        expression: String = ""
    ) : CodeBlock() {
        var name by mutableStateOf(name)
        var index by mutableStateOf(index)
        var expression by mutableStateOf(expression)

        override fun execute(context: InterpreterContext) {
            val idx = context.evaluateExpression(index)
            val value = context.evaluateExpression(expression)
            context.setArrayValue(name.trim(), idx, value)
        }
    }

    class SwapBlock(first: String = "", second: String = "") : CodeBlock() {
        var first by mutableStateOf(first)
        var second by mutableStateOf(second)

        override fun execute(context: InterpreterContext) {
            val f = first.trim()
            val s = second.trim()

            val isArrayAccess = { str: String -> str.matches(Regex("""[a-zA-Z_][a-zA-Z0-9_]*\[\d+]""")) }

            fun parseArrayIndex(expr: String): Pair<String, Int> {
                val match = Regex("""([a-zA-Z_][a-zA-Z0-9_]*)\[(\d+)]""").find(expr)
                    ?: throw Exception("Invalid array syntax: $expr")
                val name = match.groupValues[1]
                val index = match.groupValues[2].toInt()
                return name to index
            }

            if (isArrayAccess(f) && isArrayAccess(s)) {
                val (name1, index1) = parseArrayIndex(f)
                val (name2, index2) = parseArrayIndex(s)

                val arr1 = context.arrays[name1] ?: throw Exception("Unknown array: $name1")
                val arr2 = context.arrays[name2] ?: throw Exception("Unknown array: $name2")

                val temp = arr1[index1]
                arr1[index1] = arr2[index2]
                arr2[index2] = temp

            } else if (!isArrayAccess(f) && !isArrayAccess(s)) {
                if (!context.variables.containsKey(f) || !context.variables.containsKey(s)) {
                    throw Exception("Unknown variable(s): $f or $s")
                }
                val temp = context.variables[f]!!
                context.variables[f] = context.variables[s]!!
                context.variables[s] = temp

            } else {
                throw Exception("Cannot swap array element with variable")
            }
        }
    }

    class ArrayFillBlock(
        name: String = "",
        values: String = ""
    ) : CodeBlock() {
        var name by mutableStateOf(name)
        var values by mutableStateOf(values)

        override fun execute(context: InterpreterContext) {
            val arrayName = name.trim()
            if (!context.arrays.containsKey(arrayName)) {
                throw Exception("Array '$arrayName' is not declared")
            }

            val parsedValues = values.split(",").map { it.trim() }.map {
                context.evaluateExpression(it)
            }

            context.arrays[arrayName] = parsedValues.toIntArray()
        }
    }
}