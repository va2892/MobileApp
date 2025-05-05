package ru.hitsmobileapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            AppWithMenu()
        }
    }
}

data class Variable(
    val name: String,
    val value: Int?
)

@Composable
fun AppWithMenu() {
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val variables = remember { mutableStateListOf<Variable>() }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            MainMenu(
                onAddVariable = { name, value ->
                    variables.add(Variable(
                        name = name.ifEmpty { "Переменная ${variables.size + 1}" },
                        value = value
                    ))

                    scope.launch {
                        drawerState.close()
                    }
                }
            )
        },
        gesturesEnabled = true
    ) {
        Scaffold(
            topBar = {
                AppBar {
                    scope.launch {
                        drawerState.open()
                    }
                }
            }
        ) { paddingValues ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.TopCenter
            ) {
                if (variables.isEmpty()) {
                    Text("Ваш код будет здесь", fontSize = 20.sp, color = Color.LightGray)
                } else {
                    VariableBlocksList(variables) { index, newValue ->
                        variables[index] = variables[index].copy(value = newValue)
                    }
                }
            }
        }
    }
}

@Composable
fun VariableBlocksList(
    variables: List<Variable>,
    onValueChanged: (Int, Int?) -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        itemsIndexed(variables) { index, variable ->
            VariableBlock(
                variable = variable,
                onValueChange = { newValue -> onValueChanged(index, newValue) }
            )
        }
    }
}

@Composable
fun VariableBlock(
    variable: Variable,
    onValueChange: (Int?) -> Unit
) {
    var editingValue by remember { mutableStateOf(variable.value?.toString() ?: "") }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(213, 138, 255))
            .padding(16.dp)
    ) {
        Text(
            text = variable.name,
            fontWeight = FontWeight.Bold,
            fontSize = 22.sp,
            color = Color.White,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = "Значение: ",
                fontSize = 16.sp,
                color = Color.White
            )

            BasicTextField(
                value = editingValue,
                onValueChange = { newText ->
                    editingValue = newText
                    onValueChange(newText.toIntOrNull())
                },
                textStyle = TextStyle(color = Color.White, fontSize = 16.sp),
                modifier = Modifier
                    .width(100.dp)
                    .background(Color(195, 87, 255))
                    .padding(8.dp),
                singleLine = true,
                keyboardOptions = KeyboardOptions.Default.copy(
                    keyboardType = KeyboardType.Number
                ),
                decorationBox = { innerTextField ->
                    if (editingValue.isEmpty()) {
                        Text(
                            "число",
                            color = Color.White.copy(alpha = 0.7f),
                            fontSize = 16.sp
                        )
                    }
                    innerTextField()
                }
            )
        }
    }
}

@Composable
fun AppBar(onMenuClick: () -> Unit) {
    Box(
        modifier = Modifier
            .padding(vertical = 70.dp)
            .height(70.dp)
            .fillMaxWidth()
            .background(color = Color(195, 87, 255)),
        contentAlignment = Alignment.CenterStart
    ) {
        IconButton(
            onClick = onMenuClick,
            modifier = Modifier.padding(start = 16.dp)
        ) {
            Icon(
                painter = painterResource(id = R.drawable.ic_menu),
                contentDescription = "Menu",
                tint = Color.White,
                modifier = Modifier.size(40.dp)
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun MainMenu(
    onAddVariable: (String, Int?) -> Unit
) {
    var variableName by remember { mutableStateOf("") }
    var variableValue by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .width(350.dp)
            .fillMaxHeight()
            .background(color = Color(229, 229, 229))
            .padding(16.dp, vertical = 100.dp)
    ) {

        Text(
            text = "Добавление",
            fontSize = 40.sp,
            style = MaterialTheme.typography.headlineSmall,
            modifier = Modifier.padding(bottom = 24.dp)
        )

        NavigationDrawerItem(
            label = { Text("Переменная", fontSize = 25.sp) },
            selected = false,
            onClick = onClose
        )

        NavigationDrawerItem(
            label = { Text("Операция", fontSize = 25.sp) },
            selected = false,
            onClick = onClose
        )
    }
}

@Preview
@Composable
fun PreviewAppWithMenu() {
    AppWithMenu()
}