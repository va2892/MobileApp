package ru.hitsmobileapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ru.hitsmobileapp.ui.theme.HITsMobileAppTheme
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
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

@Composable
fun AppWithMenu() {
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            MainMenu {
                scope.launch {
                    drawerState.close()
                }
            }
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
                contentAlignment = Alignment.Center
            ) {
                Text("Goblin Gang", fontSize = 20.sp)
            }
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
fun MainMenu(onClose: () -> Unit = {}) {
    Column(
        modifier = Modifier
            .width(350.dp)
            .fillMaxHeight()
            .background(color = Color(229, 229, 229))
            .padding(16.dp, vertical = 100.dp)
    ) {

        Text(
            text = "Добавление",
            style = MaterialTheme.typography.headlineSmall,
            modifier = Modifier.padding(bottom = 24.dp)
        )

        NavigationDrawerItem(
            label = { Text("Переменная") },
            selected = false,
            onClick = onClose
        )

        NavigationDrawerItem(
            label = { Text("Операция") },
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