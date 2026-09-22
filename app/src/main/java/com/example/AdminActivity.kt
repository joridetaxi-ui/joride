package com.example

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.model.UserRole
import com.example.ui.admin.AdminScreen
import com.example.ui.theme.VeloDarkSlate
import com.example.ui.theme.VeloGoTheme
import com.example.ui.viewmodel.VeloGoViewModel

class AdminActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            VeloGoTheme {
                val viewModel: VeloGoViewModel = viewModel()
                LaunchedEffect(Unit) {
                    viewModel.setRole(UserRole.ADMIN)
                }

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(VeloDarkSlate)
                ) {
                    AdminScreen(
                        viewModel = viewModel,
                        onExitToHub = {
                            val intent = Intent(this@AdminActivity, MainActivity::class.java).apply {
                                flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
                                putExtra("SHOW_HUB", true)
                            }
                            startActivity(intent)
                        }
                    )
                }
            }
        }
    }
}
