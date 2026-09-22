package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.model.UserRole
import com.example.ui.admin.AdminScreen
import com.example.ui.captain.CaptainScreen
import com.example.ui.components.RoleSwitcherBar
import com.example.ui.customer.CustomerScreen
import com.example.ui.hub.AppLauncherHubScreen
import com.example.ui.theme.VeloDarkSlate
import com.example.ui.theme.VeloGoTheme
import com.example.ui.viewmodel.VeloGoViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            VeloGoTheme {
                val viewModel: VeloGoViewModel = viewModel()
                val currentRole by viewModel.currentRole.collectAsStateWithLifecycle()
                var showHubSelector by remember {
                    mutableStateOf(intent?.getBooleanExtra("SHOW_HUB", true) ?: true)
                }

                LaunchedEffect(intent) {
                    val targetRoleName = intent?.getStringExtra("TARGET_ROLE")
                    if (!targetRoleName.isNullOrEmpty()) {
                        try {
                            val role = UserRole.valueOf(targetRoleName)
                            viewModel.setRole(role)
                            showHubSelector = false
                        } catch (_: Exception) {}
                    }
                }

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(VeloDarkSlate)
                ) {
                    AnimatedContent(
                        targetState = if (showHubSelector) null else currentRole,
                        transitionSpec = {
                            fadeIn() togetherWith fadeOut()
                        },
                        label = "AppScreenTransition"
                    ) { roleState ->
                        if (roleState == null) {
                            AppLauncherHubScreen(
                                onLaunchApp = { selectedRole ->
                                    viewModel.setRole(selectedRole)
                                    showHubSelector = false
                                }
                            )
                        } else {
                            when (roleState) {
                                UserRole.CUSTOMER -> CustomerScreen(
                                    viewModel = viewModel,
                                    onExitToHub = { showHubSelector = true }
                                )
                                UserRole.CAPTAIN -> CaptainScreen(
                                    viewModel = viewModel,
                                    onExitToHub = { showHubSelector = true }
                                )
                                UserRole.ADMIN -> AdminScreen(
                                    viewModel = viewModel,
                                    onExitToHub = { showHubSelector = true }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}


