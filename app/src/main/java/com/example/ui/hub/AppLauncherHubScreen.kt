package com.example.ui.hub

import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.UserRole
import com.example.ui.theme.*

@Composable
fun AppLauncherHubScreen(
    onLaunchApp: (UserRole) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF0F172A),
                        Color(0xFF090D16),
                        Color(0xFF020617)
                    )
                )
            )
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(20.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Header
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(top = 16.dp)
            ) {
                // Brand Badge
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = VeloAmber.copy(alpha = 0.15f),
                    border = BorderStroke(1.dp, VeloAmber.copy(alpha = 0.4f))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.ElectricBike,
                            contentDescription = null,
                            tint = VeloAmber,
                            modifier = Modifier.size(18.dp)
                        )
                        Text(
                            text = "VELOGO SUITE",
                            fontWeight = FontWeight.Black,
                            fontSize = 12.sp,
                            letterSpacing = 1.5.sp,
                            color = VeloAmber
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = "Select Application",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.White
                )

                Text(
                    text = "Independent Rapido-style bike taxi ecosystem",
                    fontSize = 14.sp,
                    color = Color(0xFF94A3B8),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }

            // App Cards
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                AppSuiteCard(
                    title = "VeloGo Rider App",
                    subtitle = "Book Bike Taxi & Express Parcel Deliveries",
                    tag = "USER APP",
                    tagColor = VeloAmber,
                    icon = Icons.Default.DirectionsBike,
                    gradientColors = listOf(Color(0xFF272115), Color(0xFF181510)),
                    borderColor = VeloAmber.copy(alpha = 0.4f),
                    features = listOf("Live 2D Map Tracking", "4-Digit Safety OTP", "Instant UPI Wallet"),
                    onClick = { onLaunchApp(UserRole.CUSTOMER) }
                )

                AppSuiteCard(
                    title = "VeloGo Captain App",
                    subtitle = "Driver partner duty, trip navigation & daily earnings",
                    tag = "DRIVER PARTNER",
                    tagColor = VeloEmerald,
                    icon = Icons.Default.TwoWheeler,
                    gradientColors = listOf(Color(0xFF13281E), Color(0xFF0D1B14)),
                    borderColor = VeloEmerald.copy(alpha = 0.4f),
                    features = listOf("Online Duty Radar", "OTP Ride Verification", "Instant Bank Payouts"),
                    onClick = { onLaunchApp(UserRole.CAPTAIN) }
                )

                AppSuiteCard(
                    title = "VeloGo Admin Panel",
                    subtitle = "Fleet telematics, pricing surge & KYC approvals",
                    tag = "CENTRAL OPS",
                    tagColor = VeloTeal,
                    icon = Icons.Default.AdminPanelSettings,
                    gradientColors = listOf(Color(0xFF12242B), Color(0xFF0B171D)),
                    borderColor = VeloTeal.copy(alpha = 0.4f),
                    features = listOf("Real-time GMV Analytics", "Driver KYC Verifier", "Dynamic Surge Controls"),
                    onClick = { onLaunchApp(UserRole.ADMIN) }
                )
            }

            // Footer note
            Row(
                modifier = Modifier.padding(bottom = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.SwapHoriz,
                    contentDescription = null,
                    tint = Color(0xFF64748B),
                    modifier = Modifier.size(16.dp)
                )
                Text(
                    text = "Switch or return to this app switcher at any time",
                    fontSize = 12.sp,
                    color = Color(0xFF64748B)
                )
            }
        }
    }
}

@Composable
private fun AppSuiteCard(
    title: String,
    subtitle: String,
    tag: String,
    tagColor: Color,
    icon: ImageVector,
    gradientColors: List<Color>,
    borderColor: Color,
    features: List<String>,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .clickable { onClick() },
        shape = RoundedCornerShape(20.dp),
        color = Color.Transparent,
        border = BorderStroke(1.5.dp, borderColor)
    ) {
        Box(
            modifier = Modifier
                .background(Brush.horizontalGradient(gradientColors))
                .padding(18.dp)
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = tagColor.copy(alpha = 0.2f),
                        border = BorderStroke(1.dp, tagColor.copy(alpha = 0.5f))
                    ) {
                        Text(
                            text = tag,
                            color = tagColor,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }

                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(tagColor.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = tagColor,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = title,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )

                Text(
                    text = subtitle,
                    fontSize = 12.sp,
                    color = Color(0xFF94A3B8),
                    modifier = Modifier.padding(top = 2.dp, bottom = 12.dp)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        features.take(2).forEach { feature ->
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(5.dp)
                                        .clip(CircleShape)
                                        .background(tagColor)
                                )
                                Text(
                                    text = feature,
                                    fontSize = 11.sp,
                                    color = Color(0xFFCBD5E1)
                                )
                            }
                        }
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = "Open App",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = tagColor
                        )
                        Icon(
                            imageVector = Icons.Default.ArrowForward,
                            contentDescription = null,
                            tint = tagColor,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }
    }
}
