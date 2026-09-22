package com.example.ui.components

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
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
import androidx.compose.ui.window.Dialog
import com.example.data.local.entity.RideEntity
import com.example.ui.theme.*

@Composable
fun VeloPrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    icon: ImageVector? = null,
    color: Color = VeloAmber,
    textColor: Color = Color.Black
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier
            .fillMaxWidth()
            .height(52.dp),
        shape = RoundedCornerShape(14.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = color,
            contentColor = textColor,
            disabledContainerColor = Color(0xFF334155),
            disabledContentColor = Color(0xFF64748B)
        ),
        elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            if (icon != null) {
                Icon(icon, contentDescription = null, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(8.dp))
            }
            Text(
                text = text,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun StatusBadge(
    statusText: String,
    modifier: Modifier = Modifier,
    color: Color = VeloAmber
) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = color.copy(alpha = 0.18f),
        modifier = modifier
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .background(color, CircleShape)
            )
            Text(
                text = statusText,
                color = color,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun FareBreakdownCard(
    baseFare: Double,
    distanceFare: Double,
    surgeAmount: Double,
    discountAmount: Double,
    platformFee: Double,
    taxes: Double,
    totalFare: Double,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = Color(0xFF1E293B),
        shadowElevation = 2.dp
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "Fare Breakdown",
                color = Color.White,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold
            )
            Divider(color = Color(0xFF334155), thickness = 0.8.dp)

            FareRow("Base Fare", "₹$baseFare")
            FareRow("Distance Fare", "₹$distanceFare")
            if (surgeAmount > 0.0) {
                FareRow("Surge (Rain / Peak Hour)", "+₹$surgeAmount", textColor = VeloAmber)
            }
            if (discountAmount > 0.0) {
                FareRow("Coupon Discount", "-₹$discountAmount", textColor = VeloEmerald)
            }
            FareRow("Platform Fee", "₹$platformFee")
            FareRow("Taxes & GST (5%)", "₹$taxes")

            Divider(color = Color(0xFF334155), thickness = 1.dp)

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Total Payable",
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.ExtraBold
                )
                Text(
                    text = "₹$totalFare",
                    color = VeloAmber,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.ExtraBold
                )
            }
        }
    }
}

@Composable
private fun FareRow(
    label: String,
    value: String,
    textColor: Color = Color(0xFF94A3B8)
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, color = Color(0xFF94A3B8), fontSize = 13.sp)
        Text(text = value, color = textColor, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
fun MaskedCallChatDialog(
    contactName: String,
    contactPhone: String,
    onDismiss: () -> Unit
) {
    var messageText by remember { mutableStateOf("") }
    var sentMessages by remember {
        mutableStateOf(
            listOf(
                "Captain: I am reaching in 2 minutes!",
                "Captain: Please be near the landmark."
            )
        )
    }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = Color(0xFF0F172A),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = contactName,
                            color = Color.White,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "🛡️ Masked Number: +91 80 4729 XXXX",
                            color = VeloEmerald,
                            fontSize = 12.sp
                        )
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.White)
                    }
                }

                Divider(color = Color(0xFF1E293B))

                // Quick pre-set messages
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    listOf("I'm at the gate", "Wearing black shirt", "Coming in 1 min").forEach { quickMsg ->
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = Color(0xFF1E293B),
                            modifier = Modifier.clickable {
                                sentMessages = sentMessages + "You: $quickMsg"
                            }
                        ) {
                            Text(
                                text = quickMsg,
                                color = Color(0xFFCBD5E1),
                                fontSize = 10.sp,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                }

                // Chat stream
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 140.dp)
                        .background(Color(0xFF1E293B), RoundedCornerShape(12.dp))
                        .padding(10.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    sentMessages.forEach { msg ->
                        Text(
                            text = msg,
                            color = if (msg.startsWith("You")) VeloAmber else Color.White,
                            fontSize = 12.sp
                        )
                    }
                }

                // Send bar
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = messageText,
                        onValueChange = { messageText = it },
                        placeholder = { Text("Type message...", color = Color(0xFF64748B), fontSize = 12.sp) },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = VeloAmber,
                            unfocusedBorderColor = Color(0xFF334155)
                        )
                    )
                    IconButton(
                        onClick = {
                            if (messageText.isNotBlank()) {
                                sentMessages = sentMessages + "You: $messageText"
                                messageText = ""
                            }
                        },
                        modifier = Modifier.background(VeloAmber, CircleShape)
                    ) {
                        Icon(Icons.Default.Send, contentDescription = "Send", tint = Color.Black)
                    }
                }
            }
        }
    }
}

@Composable
fun SafetySosDialog(
    tripId: String,
    onTriggerSos: () -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = Color(0xFF0F172A),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(60.dp)
                        .background(VeloRed.copy(alpha = 0.2f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.Warning,
                        contentDescription = "SOS",
                        tint = VeloRed,
                        modifier = Modifier.size(36.dp)
                    )
                }

                Text(
                    text = "Emergency SOS & Safety",
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text = "Triggering SOS will instantly alert the 24x7 VeloGo Emergency Response Team, send live GPS location link to your emergency contacts, and facilitate direct connection to Police (112).",
                    color = Color(0xFF94A3B8),
                    fontSize = 12.sp,
                    textAlign = TextAlign.Center
                )

                VeloPrimaryButton(
                    text = "🚨 TRIGGER INSTANT SOS (112)",
                    onClick = {
                        onTriggerSos()
                        onDismiss()
                    },
                    color = VeloRed,
                    textColor = Color.White
                )

                OutlinedButton(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White)
                ) {
                    Text("I'm Safe / Close")
                }
            }
        }
    }
}
