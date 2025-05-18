package ru.hitsmobileapp

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

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