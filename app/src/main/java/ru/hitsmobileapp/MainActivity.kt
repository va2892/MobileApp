package ru.hitsmobileapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.Alignment
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.foundation.clickable

val LightPurple = Color(0xFFE6E6FA)
val DeepPurple = Color(0xFFB39DDB)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    CodeBlockInterpreter()
                }
            }
        }
    }
}

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

//code block

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


//ui

@Composable
fun CodeBlockUI(block: CodeBlock, onDelete: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .border(1.dp, DeepPurple)
            .background(LightPurple)
            .padding(8.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Box(modifier = Modifier.fillMaxWidth()) {
                Text(
                    "✕",
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(end = 4.dp)
                        .clickable { onDelete() },
                    color = Color.DarkGray
                )
            }

            when (block) {
                is CodeBlock.VariableDeclaration -> VariableBlock(block)
                is CodeBlock.Assignment -> AssignmentBlock(block)
                is CodeBlock.IfBlock -> IfBlockUI(block)
                is CodeBlock.ExpressionBlock -> ExpressionBlockUI(block)
            }
        }
    }
}


@Composable
fun VariableBlock(block: CodeBlock.VariableDeclaration) {
    OutlinedTextField(
        value = block.names,
        onValueChange = { block.names = it },
        label = { Text("int a, b, c") },
        modifier = Modifier.fillMaxWidth().padding(4.dp),
        singleLine = true,
        colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = LightPurple,
            unfocusedContainerColor = LightPurple,
            focusedLabelColor = DeepPurple,
            unfocusedLabelColor = DeepPurple,
            focusedBorderColor = DeepPurple,
            unfocusedBorderColor = DeepPurple
        )
    )
}

@Composable
fun AssignmentBlock(block: CodeBlock.Assignment) {
    Row(modifier = Modifier.fillMaxWidth().padding(4.dp), verticalAlignment = Alignment.CenterVertically) {
        OutlinedTextField(
            value = block.variable,
            onValueChange = { block.variable = it },
            label = { Text("Variable") },
            modifier = Modifier.weight(1f),
            singleLine = true
        )
        Text(" = ", modifier = Modifier.padding(8.dp))
        OutlinedTextField(
            value = block.expression,
            onValueChange = { block.expression = it },
            label = { Text("Expression") },
            modifier = Modifier.weight(2f),
            singleLine = true
        )
    }
}

@Composable
fun IfBlockUI(block: CodeBlock.IfBlock) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(4.dp)
            .border(1.dp, Color.Gray)
            .padding(8.dp)
            .background(Color(0xFFF0F0F0))
    ) {
        Text("if (...) {", fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
        Row {
            OutlinedTextField(value = block.left, onValueChange = { block.left = it }, label = { Text("Left") }, modifier = Modifier.weight(1f))
            OutlinedTextField(value = block.op, onValueChange = { block.op = it }, label = { Text("Operator") }, modifier = Modifier.weight(1f))
            OutlinedTextField(value = block.right, onValueChange = { block.right = it }, label = { Text("Right") }, modifier = Modifier.weight(1f))
        }
        Spacer(modifier = Modifier.height(4.dp))
        block.body.forEach { inner ->
            CodeBlockUI(inner) { block.body.remove(inner) }
        }
        Button(onClick = { block.body.add(CodeBlock.Assignment()) }) {
            Text("+ Add to IF")
        }
        Text("}", fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
fun ExpressionBlockUI(block: CodeBlock.ExpressionBlock) {
    OutlinedTextField(
        value = block.expression,
        onValueChange = { block.expression = it },
        label = { Text("Expression (e.g. (a + b) * 2)") },
        modifier = Modifier
            .fillMaxWidth()
            .padding(4.dp),
        singleLine = false,
        maxLines = 3,
        colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = LightPurple,
            unfocusedContainerColor = LightPurple,
            focusedLabelColor = DeepPurple,
            unfocusedLabelColor = DeepPurple,
            focusedBorderColor = DeepPurple,
            unfocusedBorderColor = DeepPurple
        )
    )
}

// interpeter

class InterpreterContext {
    val variables = mutableMapOf<String, Int>()
    val outputs = mutableListOf<String>()

    fun evaluateExpression(expr: String): Int {
        val replaced = expr.split(Regex("(?<=[^a-zA-Z0-9_])|(?=[^a-zA-Z0-9_])"))
            .joinToString(" ") { token ->
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

