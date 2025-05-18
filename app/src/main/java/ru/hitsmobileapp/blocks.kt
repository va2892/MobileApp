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
    ) {
        Text("CodeBlock Visual Interpreter", fontSize = 24.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 16.dp))

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(onClick = { codeBlocks += CodeBlock.VariableDeclaration() }) { Text("+ Variable") }
            Button(onClick = { codeBlocks += CodeBlock.Assignment() }) { Text("+ Assignment") }
            Button(onClick = { codeBlocks += CodeBlock.IfBlock() }) { Text("+ If") }
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
                val expressionLines = context.outputs.joinToString("\n")
                output = listOf(variableLines, expressionLines).filter { it.isNotBlank() }.joinToString("\n")
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

    class IfBlock(
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
                for (cmd in body) cmd.execute(context)
            }
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
}
