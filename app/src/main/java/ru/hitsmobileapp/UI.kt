package ru.hitsmobileapp

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.zIndex
import ru.hitsmobileapp.ui.theme.Purple40
import ru.hitsmobileapp.ui.theme.DeepPurple
import ru.hitsmobileapp.ui.theme.LightPurple

@Composable
fun CodeBlockUI(block: CodeBlock, onDelete: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .clip(RoundedCornerShape(8.dp))
            .border(1.dp, DeepPurple, RoundedCornerShape(8.dp))
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
                    color = Color.Black
                )
            }

            when (block) {
                is CodeBlock.VariableDeclaration -> VariableBlock(block)
                is CodeBlock.Assignment -> AssignmentBlock(block)
                is CodeBlock.IfBlock -> IfBlockUI(block)
                is CodeBlock.ExpressionBlock -> ExpressionBlockUI(block)
                is CodeBlock.ElseIfBlock -> ElseIfBlockUI(block)
                is CodeBlock.ElseBlock -> ElseBlockUI(block) { onDelete() }
                is CodeBlock.WhileBlock -> WhileBlockUI(block)
                is CodeBlock.ForBlock -> ForBlockUI(block)
                is CodeBlock.ArrayDeclaration -> ArrayDeclarationBlock(block)
                is CodeBlock.ArrayAssignment -> ArrayAssignmentBlock(block)
                is CodeBlock.SwapBlock -> SwapBlockUI(block)
                is CodeBlock.ArrayFillBlock -> ArrayFillBlockUI(block)
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
        modifier = Modifier
            .fillMaxWidth()
            .padding(4.dp)
            .clip(RoundedCornerShape(8.dp)),
        singleLine = true,
        colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = LightPurple,
            unfocusedContainerColor = LightPurple,
            focusedLabelColor = Purple40,
            unfocusedLabelColor = Purple40,
            focusedBorderColor = Purple40,
            unfocusedBorderColor = Purple40
        )
    )
}

@Composable
fun AssignmentBlock(block: CodeBlock.Assignment) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(4.dp)
            .clip(RoundedCornerShape(8.dp)),
        verticalAlignment = Alignment.CenterVertically
    ) {
        OutlinedTextField(
            value = block.variable,
            onValueChange = { block.variable = it },
            label = { Text("Variable", color = Purple40) },
            modifier = Modifier.weight(1f),
            singleLine = true
        )
        Text(" = ", modifier = Modifier.padding(8.dp))
        OutlinedTextField(
            value = block.expression,
            onValueChange = { block.expression = it },
            label = { Text("Expression", color = Purple40   ) },
            modifier = Modifier.weight(2f),
            singleLine = true
        )
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
            .padding(4.dp)
            .clip(RoundedCornerShape(8.dp)),
        singleLine = false,
        maxLines = 3,
        colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = LightPurple,
            unfocusedContainerColor = LightPurple,
            focusedLabelColor = Purple40,
            unfocusedLabelColor = Purple40,
            focusedBorderColor = Purple40,
            unfocusedBorderColor = Purple40
        )
    )
}


@Composable
fun IfBlockUI(block: CodeBlock.IfBlock) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(4.dp)
            .clip(RoundedCornerShape(8.dp))
            .border(1.dp, DeepPurple, RoundedCornerShape(8.dp))
            .padding(8.dp)
            .background(LightPurple)
    ) {
        Text("if (...) {", fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
        Row {
            OutlinedTextField(value = block.left, onValueChange = { block.left = it }, label = { Text("Left", color = Purple40) }, modifier = Modifier.weight(1f))
            OutlinedTextField(value = block.op, onValueChange = { block.op = it }, label = { Text("Operator", color = Purple40) }, modifier = Modifier.weight(1f))
            OutlinedTextField(value = block.right, onValueChange = { block.right = it }, label = { Text("Right", color = Purple40) }, modifier = Modifier.weight(1f))
        }

        Spacer(modifier = Modifier.height(4.dp))
        block.body.forEach { inner ->
            CodeBlockUI(inner) { block.body.remove(inner) }
        }

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(onClick = { block.body.add(CodeBlock.ExpressionBlock()) }) {
                Text("expr")
            }
            Button(onClick = { block.body.add(CodeBlock.Assignment()) }) {
                Text("assign")
            }
            Button(onClick = { block.body.add(CodeBlock.SwapBlock()) }) {
                Text("swap")
            }
        }

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(onClick = { block.elseIfBlocks.add(CodeBlock.ElseIfBlock()) }) {
                Text("else if")
            }
            Button(onClick = {
                if (block.elseBlock == null) {
                    block.elseBlock = CodeBlock.ElseBlock()
                }
            }) {
                Text("else")
            }
        }

        block.elseIfBlocks.forEach { elseIf ->
            CodeBlockUI(elseIf) { block.elseIfBlocks.remove(elseIf) }
        }


        block.elseBlock?.let { elseBlock ->
            ElseBlockUI(elseBlock, onDelete = { block.elseBlock = null })
        }

        Text("}", fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
fun ElseIfBlockUI(block: CodeBlock.ElseIfBlock) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(4.dp)
            .clip(RoundedCornerShape(8.dp))
            .border(1.dp, DeepPurple, RoundedCornerShape(8.dp))
            .padding(8.dp)
            .background(LightPurple)
    ) {
        Text("else if (...) {", fontWeight = FontWeight.SemiBold)

        Row {
            OutlinedTextField(
                value = block.left,
                onValueChange = { block.left = it },
                label = { Text("Left", color = Purple40) },
                modifier = Modifier.weight(1f)
            )
            OutlinedTextField(
                value = block.op,
                onValueChange = { block.op = it },
                label = { Text("Operator", color = Purple40) },
                modifier = Modifier.weight(1f)
            )
            OutlinedTextField(
                value = block.right,
                onValueChange = { block.right = it },
                label = { Text("Right", color = Purple40) },
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(4.dp))

        block.body.forEach { inner ->
            CodeBlockUI(inner) { block.body.remove(inner) }
        }

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(onClick = { block.body.add(CodeBlock.ExpressionBlock()) }) {
                Text("expr")
            }
            Button(onClick = { block.body.add(CodeBlock.Assignment()) }) {
                Text("assign")
            }
            Button(onClick = { block.body.add(CodeBlock.SwapBlock()) }) {
                Text("swap")
            }
        }

        Text("}", fontWeight = FontWeight.SemiBold)
    }
}

@Composable
fun ElseBlockUI(block: CodeBlock.ElseBlock, onDelete: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(4.dp)
            .clip(RoundedCornerShape(8.dp))
            .border(1.dp, DeepPurple, RoundedCornerShape(8.dp))
            .background(LightPurple)
            .padding(8.dp)
    ) {
        Box(Modifier.fillMaxWidth()) {
            Text(
                "✕",
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .clickable { onDelete() },
                color = Color.Black
            )
        }

        Text("else {", fontWeight = FontWeight.SemiBold)

        block.body.forEach { inner ->
            CodeBlockUI(inner) { block.body.remove(inner) }
        }

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(onClick = { block.body.add(CodeBlock.ExpressionBlock()) }) {
                Text("expr")
            }
            Button(onClick = { block.body.add(CodeBlock.Assignment()) }) {
                Text("assign")
            }
        }

        Text("}", fontWeight = FontWeight.SemiBold)
    }
}


@Composable
fun WhileBlockUI(block: CodeBlock.WhileBlock) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(4.dp)
            .clip(RoundedCornerShape(8.dp))
            .border(1.dp, DeepPurple, RoundedCornerShape(8.dp))
            .background(LightPurple)
            .padding(8.dp)
    ) {
        Text("while (...) {", fontSize = 16.sp, fontWeight = FontWeight.SemiBold)

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedTextField(
                value = block.left,
                onValueChange = { block.left = it },
                label = { Text("Left", color = Purple40) },
                modifier = Modifier.weight(1f)
            )
            OutlinedTextField(
                value = block.op,
                onValueChange = { block.op = it },
                label = { Text("Operator", color = Purple40) },
                modifier = Modifier.weight(1f)
            )
            OutlinedTextField(
                value = block.right,
                onValueChange = { block.right = it },
                label = { Text("Right", color = Purple40) },
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(4.dp))

        block.body.forEach { inner ->
            CodeBlockUI(inner) { block.body.remove(inner) }
        }

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(onClick = { block.body.add(CodeBlock.ExpressionBlock()) }) {
                Text("expr")
            }
            Button(onClick = { block.body.add(CodeBlock.Assignment()) }) {
                Text("assign")
            }
            Button(onClick = { block.body.add(CodeBlock.SwapBlock()) }) {
                Text("swap")
            }
        }

        Text("}", fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
fun ForBlockUI(block: CodeBlock.ForBlock) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(4.dp)
            .clip(RoundedCornerShape(8.dp))
            .border(1.dp, DeepPurple, RoundedCornerShape(8.dp))
            .background(LightPurple)
            .padding(8.dp)
    ) {
        Text(
            "for (...) {",
            fontSize = 16.sp,
            fontWeight = FontWeight.SemiBold,
            color = Color.Black
        )

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(
                value = block.variable,
                onValueChange = { block.variable = it },
                label = { Text("Variable (e.g. i)", color = Purple40) },
                modifier = Modifier.weight(1f)
            )
            OutlinedTextField(
                value = block.from,
                onValueChange = { block.from = it },
                label = { Text("From", color = Purple40) },
                modifier = Modifier.weight(1f)
            )
            OutlinedTextField(
                value = block.to,
                onValueChange = { block.to = it },
                label = { Text("To", color = Purple40) },
                modifier = Modifier.weight(1f)
            )
            OutlinedTextField(
                value = block.step,
                onValueChange = { block.step = it },
                label = { Text("Step", color = Purple40) },
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(4.dp))

        block.body.forEach { inner ->
            CodeBlockUI(inner) { block.body.remove(inner) }
        }

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(onClick = { block.body.add(CodeBlock.Assignment()) }) { Text("assign") }
            Button(onClick = { block.body.add(CodeBlock.ExpressionBlock()) }) { Text("expr") }
            Button(onClick = { block.body.add(CodeBlock.SwapBlock()) }) { Text("swap") }
        }

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(onClick = { block.body.add(CodeBlock.ForBlock()) }) { Text("for") }
            Button(onClick = { block.body.add(CodeBlock.IfBlock()) }) { Text("if") }
        }

        Text("}", fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
    }
}


@Composable
fun ArrayDeclarationBlock(block: CodeBlock.ArrayDeclaration) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(4.dp)
            .clip(RoundedCornerShape(8.dp)),
        verticalAlignment = Alignment.CenterVertically
    ) {
        OutlinedTextField(
            value = block.name,
            onValueChange = { block.name = it },
            label = { Text("Array Name", color = Purple40) },
            modifier = Modifier.weight(1f),
            singleLine = true
        )
        Spacer(modifier = Modifier.width(8.dp))
        OutlinedTextField(
            value = block.size,
            onValueChange = { block.size = it },
            label = { Text("Size (e.g. 5)", color = Purple40) },
            modifier = Modifier.weight(1f),
            singleLine = true
        )
    }
}

@Composable
fun ArrayAssignmentBlock(block: CodeBlock.ArrayAssignment) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(4.dp)
            .clip(RoundedCornerShape(8.dp)),
        verticalAlignment = Alignment.CenterVertically
    ) {
        OutlinedTextField(
            value = block.name,
            onValueChange = { block.name = it },
            label = { Text("Array Name", color = Purple40) },
            modifier = Modifier.weight(1f),
            singleLine = true
        )
        Text("[", fontSize = 18.sp)
        OutlinedTextField(
            value = block.index,
            onValueChange = { block.index = it },
            label = { Text("Index", color = Purple40) },
            modifier = Modifier.weight(1f),
            singleLine = true
        )
        Text("] = ", fontSize = 18.sp)
        OutlinedTextField(
            value = block.expression,
            onValueChange = { block.expression = it },
            label = { Text("Expression", color = Purple40) },
            modifier = Modifier.weight(2f),
            singleLine = true
        )
    }
}

@Composable
fun SwapBlockUI(block: CodeBlock.SwapBlock) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(4.dp)
            .clip(RoundedCornerShape(8.dp)),
        verticalAlignment = Alignment.CenterVertically
    ) {
        OutlinedTextField(
            value = block.first,
            onValueChange = { block.first = it },
            label = { Text("First (a or arr[0])", color = Purple40) },
            modifier = Modifier.weight(1f),
            singleLine = true
        )
        Text("<->", modifier = Modifier.padding(horizontal = 8.dp))
        OutlinedTextField(
            value = block.second,
            onValueChange = { block.second = it },
            label = { Text("Second (b or arr[1])", color = Purple40) },
            modifier = Modifier.weight(1f),
            singleLine = true
        )
    }
}

@Composable
fun ArrayFillBlockUI(block: CodeBlock.ArrayFillBlock) {
    Column(modifier = Modifier
        .fillMaxWidth()
        .padding(4.dp)
        .clip(RoundedCornerShape(8.dp))
    ) {
        OutlinedTextField(
            value = block.name,
            onValueChange = { block.name = it },
            label = { Text("Array name (e.g. arr)", color = Purple40) },
            modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
            value = block.values,
            onValueChange = { block.values = it },
            label = { Text("Values (e.g. 5, 3, 2)", color = Purple40) },
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
fun DraggableCodeBlocks(codeBlocks: SnapshotStateList<CodeBlock>) {
    var draggingIndex by remember { mutableStateOf<Int?>(null) }
    var dragOffset by remember { mutableFloatStateOf(0f) }

    val itemHeightPx = with(LocalDensity.current) { 120.dp.toPx() }

    Column(modifier = Modifier.fillMaxWidth()) {
        codeBlocks.mapIndexed { index, block ->
            val isDragging = index == draggingIndex

            Box(
                modifier = Modifier
                    .zIndex(if (isDragging) 1f else 0f)
                    .graphicsLayer {
                        translationY = if (isDragging) dragOffset else 0f
                    }
                    .fillMaxWidth()
                    .background(
                        if (isDragging) MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)
                        else MaterialTheme.colorScheme.surface,
                        RoundedCornerShape(8.dp)
                    )
                    .pointerInput(Unit) {
                        detectDragGestures(
                            onDragStart = {
                                draggingIndex = index
                                dragOffset = 0f
                            },
                            onDragEnd = {
                                draggingIndex = null
                                dragOffset = 0f
                            },
                            onDragCancel = {
                                draggingIndex = null
                                dragOffset = 0f
                            },
                            onDrag = { change, dragAmount ->
                                change.consume()
                                dragOffset += dragAmount.y

                                val currentIndex = draggingIndex ?: return@detectDragGestures
                                val targetIndex = ((currentIndex + dragOffset / itemHeightPx).toInt())
                                    .coerceIn(0, codeBlocks.lastIndex)

                                if (targetIndex != currentIndex) {
                                    codeBlocks.swap(currentIndex, targetIndex)
                                    draggingIndex = targetIndex
                                    dragOffset -= (targetIndex - currentIndex) * itemHeightPx
                                }
                            }
                        )
                    }
                    .padding(8.dp)
            ) {
                CodeBlockUI(block, onDelete = { codeBlocks.remove(block) })
            }
        }
    }
}

fun <T> SnapshotStateList<T>.swap(i: Int, j: Int) {
    if (i == j) return
    val temp = this[i]
    this[i] = this[j]
    this[j] = temp
}

