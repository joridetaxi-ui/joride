package com.example.ui.admin

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.local.entity.KycDocumentEntity
import com.example.data.local.entity.PricingConfigEntity
import com.example.data.local.entity.SupportTicketEntity
import com.example.ui.components.*
import com.example.ui.theme.*
import com.example.ui.viewmodel.*

@Composable
fun AdminScreen(
    viewModel: VeloGoViewModel,
    onExitToHub: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val subScreen by viewModel.adminSubScreen.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            AdminTopBar(onExitToHub = onExitToHub)
        },
        bottomBar = {
            AdminBottomNav(
                currentScreen = subScreen,
                onSelectScreen = { viewModel.adminSubScreen.value = it }
            )
        },
        containerColor = VeloDarkSlate
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (subScreen) {
                AdminSubScreen.DASHBOARD -> AdminDashboardView(viewModel = viewModel)
                AdminSubScreen.LIVE_MAP -> AdminLiveMapView(viewModel = viewModel)
                AdminSubScreen.KYC_REVIEW -> AdminKycReviewView(viewModel = viewModel)
                AdminSubScreen.RIDES_PARCELS -> AdminRidesParcelsView(viewModel = viewModel)
                AdminSubScreen.PRICING_ZONES -> AdminPricingZonesView(viewModel = viewModel)
                AdminSubScreen.SUPPORT_TICKETS -> AdminSupportTicketsView(viewModel = viewModel)
            }
        }
    }
}

@Composable
private fun AdminTopBar(
    onExitToHub: (() -> Unit)? = null
) {
    Surface(
        color = Color(0xFF0F172A),
        shadowElevation = 4.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (onExitToHub != null) {
                    IconButton(
                        onClick = onExitToHub,
                        modifier = Modifier
                            .size(36.dp)
                            .background(Color(0xFF1E293B), CircleShape)
                    ) {
                        Icon(
                            Icons.Default.Apps,
                            contentDescription = "Switch App",
                            tint = VeloTeal,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                Icon(Icons.Default.AdminPanelSettings, contentDescription = null, tint = VeloTeal)
                Text(
                    text = "VeloGo Central Operations Hub",
                    color = Color.White,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Surface(
                shape = RoundedCornerShape(8.dp),
                color = VeloTeal.copy(alpha = 0.2f)
            ) {
                Text(
                    text = "SUPER ADMIN",
                    color = VeloTeal,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.ExtraBold,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }
        }
    }
}

@Composable
private fun AdminDashboardView(viewModel: VeloGoViewModel) {
    val allCaptains by viewModel.allCaptains.collectAsStateWithLifecycle()
    val allRides by viewModel.allRides.collectAsStateWithLifecycle()
    val allParcels by viewModel.allParcels.collectAsStateWithLifecycle()
    val pendingKyc by viewModel.pendingKycDocs.collectAsStateWithLifecycle()
    val openTickets by viewModel.supportTickets.collectAsStateWithLifecycle()

    val totalGMV = allRides.sumOf { it.totalFare } + allParcels.sumOf { it.totalFare } + 28400.0
    val platformCommission = (totalGMV * 0.20).toInt()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Text("📈 Real-Time Platform Analytics", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                AdminKpiCard("Total GMV Volume", "₹${totalGMV.toInt()}", VeloAmber, Modifier.weight(1f))
                AdminKpiCard("Platform Revenue (20%)", "₹$platformCommission", VeloEmerald, Modifier.weight(1f))
            }
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                AdminKpiCard("Active Captains Online", "${allCaptains.count { it.isOnline }} / ${allCaptains.size}", Color.White, Modifier.weight(1f))
                AdminKpiCard("Completed Trips", "${allRides.size + 48} rides", VeloTeal, Modifier.weight(1f))
            }
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                AdminKpiCard("Pending KYC Review", "${pendingKyc.size} docs", VeloAmber, Modifier.weight(1f))
                AdminKpiCard("Open Support Tickets", "${openTickets.count { it.status == "OPEN" }} open", VeloRed, Modifier.weight(1f))
            }
        }

        item {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = Color(0xFF1E293B),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("System Health & Multi-City Engine", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("• Bengaluru Dispatch Grid: 99.98% uptime", color = VeloEmerald, fontSize = 11.sp)
                        Text("Active", color = VeloEmerald, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("• Payment Gateway Webhook: Healthy (Razorpay/UPI)", color = VeloEmerald, fontSize = 11.sp)
                        Text("Active", color = VeloEmerald, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
private fun AdminKpiCard(
    title: String,
    value: String,
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = Color(0xFF1E293B),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(title, color = Color(0xFF94A3B8), fontSize = 11.sp)
            Text(value, color = accentColor, fontSize = 18.sp, fontWeight = FontWeight.ExtraBold)
        }
    }
}

@Composable
private fun AdminLiveMapView(viewModel: VeloGoViewModel) {
    val allCaptains by viewModel.allCaptains.collectAsStateWithLifecycle()
    val activeRide by viewModel.activeRide.collectAsStateWithLifecycle()

    Box(modifier = Modifier.fillMaxSize()) {
        InteractiveMapCanvas(
            modifier = Modifier.fillMaxSize(),
            pickupLat = activeRide?.pickupLat ?: 12.9784,
            pickupLng = activeRide?.pickupLng ?: 77.6408,
            dropLat = activeRide?.dropLat ?: 12.9352,
            dropLng = activeRide?.dropLng ?: 77.6245,
            nearbyCaptains = allCaptains.map { Pair(it.latitude, it.longitude) }
        )

        Surface(
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(16.dp),
            shape = RoundedCornerShape(12.dp),
            color = Color(0xFF0F172A).copy(alpha = 0.9f)
        ) {
            Text(
                text = "Live Fleet: ${allCaptains.size} Captains Monitored",
                color = VeloTeal,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
            )
        }
    }
}

@Composable
private fun AdminKycReviewView(viewModel: VeloGoViewModel) {
    val pendingDocs by viewModel.pendingKycDocs.collectAsStateWithLifecycle()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text("📋 Captain KYC Approvals Queue", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
            Text("Review and approve vehicle documents before activating captains.", color = Color(0xFF94A3B8), fontSize = 11.sp)
        }

        if (pendingDocs.isEmpty()) {
            item {
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = Color(0xFF1E293B),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "🎉 All Captain KYC documents are approved!",
                        color = VeloEmerald,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(20.dp)
                    )
                }
            }
        }

        items(pendingDocs) { doc ->
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = Color(0xFF1E293B),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(doc.documentType.replace("_", " "), color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                        StatusBadge(statusText = "PENDING REVIEW", color = VeloAmber)
                    }
                    Text("Document #: ${doc.documentNumber}", color = VeloAmber, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    Text("Captain ID: ${doc.captainId}", color = Color(0xFF94A3B8), fontSize = 11.sp)

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = { viewModel.adminReviewKyc(doc.id, approve = false, reason = "Blurry scan") },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = VeloRed)
                        ) {
                            Text("Reject", fontSize = 12.sp)
                        }
                        Button(
                            onClick = { viewModel.adminReviewKyc(doc.id, approve = true) },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = VeloEmerald, contentColor = Color.Black)
                        ) {
                            Text("Approve & Activate", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun AdminRidesParcelsView(viewModel: VeloGoViewModel) {
    val allRides by viewModel.allRides.collectAsStateWithLifecycle()
    val allParcels by viewModel.allParcels.collectAsStateWithLifecycle()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text("🏍️ Live Rides & Parcels Audit", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
        }

        items(allRides) { ride ->
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = Color(0xFF1E293B),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(ride.id, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        StatusBadge(statusText = ride.status, color = VeloAmber)
                    }
                    Text("Customer: ${ride.customerName} (${ride.customerPhone})", color = Color(0xFFCBD5E1), fontSize = 11.sp)
                    Text("Captain: ${ride.captainName ?: "Unassigned"}", color = Color(0xFF94A3B8), fontSize = 11.sp)
                    Text("Fare: ₹${ride.totalFare}  •  Platform: ₹${ride.platformFee}", color = VeloEmerald, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
private fun AdminPricingZonesView(viewModel: VeloGoViewModel) {
    val config by viewModel.pricingConfig.collectAsStateWithLifecycle()
    var baseFareInput by remember { mutableStateOf("25.0") }
    var perKmInput by remember { mutableStateOf("9.5") }
    var surgeMultiplierInput by remember { mutableStateOf("1.2") }
    var savedSuccess by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Text("⚙️ Dynamic Fare Engine & Surge Controls", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
        }

        item {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = Color(0xFF1E293B),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("City: Bengaluru (Service Active)", color = VeloEmerald, fontSize = 13.sp, fontWeight = FontWeight.Bold)

                    OutlinedTextField(
                        value = baseFareInput,
                        onValueChange = { baseFareInput = it },
                        label = { Text("Base Fare (₹)") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White)
                    )

                    OutlinedTextField(
                        value = perKmInput,
                        onValueChange = { perKmInput = it },
                        label = { Text("Per Kilometer Rate (₹)") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White)
                    )

                    OutlinedTextField(
                        value = surgeMultiplierInput,
                        onValueChange = { surgeMultiplierInput = it },
                        label = { Text("Surge Multiplier (1.0 = Normal, 1.3 = Rain/Rush)") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White)
                    )

                    VeloPrimaryButton(
                        text = "💾 Update Pricing Rules",
                        onClick = {
                            val base = baseFareInput.toDoubleOrNull() ?: 25.0
                            val perKm = perKmInput.toDoubleOrNull() ?: 9.5
                            val surge = surgeMultiplierInput.toDoubleOrNull() ?: 1.0
                            viewModel.adminUpdatePricing(base, perKm, surge)
                            savedSuccess = true
                        }
                    )

                    if (savedSuccess) {
                        Text("Pricing parameters updated across all rider apps!", color = VeloEmerald, fontSize = 11.sp)
                    }
                }
            }
        }
    }
}

@Composable
private fun AdminSupportTicketsView(viewModel: VeloGoViewModel) {
    val tickets by viewModel.supportTickets.collectAsStateWithLifecycle()
    var selectedTicket by remember { mutableStateOf<SupportTicketEntity?>(null) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text("🎧 Customer & Captain Support Desk", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
        }

        items(tickets) { tck ->
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = Color(0xFF1E293B),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("${tck.id} • ${tck.userName}", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        StatusBadge(statusText = tck.status, color = if (tck.status == "RESOLVED") VeloEmerald else VeloRed)
                    }
                    Text("Subject: ${tck.subject}", color = VeloAmber, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    Text(tck.description, color = Color(0xFF94A3B8), fontSize = 11.sp)

                    if (tck.status == "OPEN") {
                        Button(
                            onClick = { selectedTicket = tck },
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = VeloTeal, contentColor = Color.Black)
                        ) {
                            Text("Resolve & Process Refund", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }

    if (selectedTicket != null) {
        AdminResolveTicketDialog(
            ticket = selectedTicket!!,
            onResolve = { reply, refund ->
                viewModel.adminResolveTicket(selectedTicket!!.id, reply, refund)
                selectedTicket = null
            },
            onDismiss = { selectedTicket = null }
        )
    }
}

@Composable
private fun AdminResolveTicketDialog(
    ticket: SupportTicketEntity,
    onResolve: (String, Double) -> Unit,
    onDismiss: () -> Unit
) {
    var replyText by remember { mutableStateOf("We have reviewed your trip and credited a full refund to your VeloPay Wallet.") }
    var refundAmountInput by remember { mutableStateOf("50.0") }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = Color(0xFF0F172A),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text("Resolve Ticket ${ticket.id}", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)

                OutlinedTextField(
                    value = replyText,
                    onValueChange = { replyText = it },
                    label = { Text("Resolution Reply") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White)
                )

                OutlinedTextField(
                    value = refundAmountInput,
                    onValueChange = { refundAmountInput = it },
                    label = { Text("Wallet Refund Amount (₹)") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White)
                )

                VeloPrimaryButton(
                    text = "Approve Resolution & Credit Refund",
                    onClick = {
                        val refund = refundAmountInput.toDoubleOrNull() ?: 0.0
                        onResolve(replyText, refund)
                    },
                    color = VeloEmerald,
                    textColor = Color.Black
                )
            }
        }
    }
}

@Composable
private fun AdminBottomNav(
    currentScreen: AdminSubScreen,
    onSelectScreen: (AdminSubScreen) -> Unit
) {
    NavigationBar(
        containerColor = Color(0xFF0F172A),
        tonalElevation = 8.dp
    ) {
        NavigationBarItem(
            selected = currentScreen == AdminSubScreen.DASHBOARD,
            onClick = { onSelectScreen(AdminSubScreen.DASHBOARD) },
            icon = { Icon(Icons.Default.Dashboard, contentDescription = "Dashboard") },
            label = { Text("Stats", fontSize = 10.sp) },
            colors = NavigationBarItemDefaults.colors(selectedIconColor = Color.Black, indicatorColor = VeloTeal, unselectedIconColor = Color(0xFF94A3B8))
        )
        NavigationBarItem(
            selected = currentScreen == AdminSubScreen.LIVE_MAP,
            onClick = { onSelectScreen(AdminSubScreen.LIVE_MAP) },
            icon = { Icon(Icons.Default.Map, contentDescription = "Map") },
            label = { Text("Fleet", fontSize = 10.sp) },
            colors = NavigationBarItemDefaults.colors(selectedIconColor = Color.Black, indicatorColor = VeloTeal, unselectedIconColor = Color(0xFF94A3B8))
        )
        NavigationBarItem(
            selected = currentScreen == AdminSubScreen.KYC_REVIEW,
            onClick = { onSelectScreen(AdminSubScreen.KYC_REVIEW) },
            icon = { Icon(Icons.Default.VerifiedUser, contentDescription = "KYC") },
            label = { Text("KYC", fontSize = 10.sp) },
            colors = NavigationBarItemDefaults.colors(selectedIconColor = Color.Black, indicatorColor = VeloTeal, unselectedIconColor = Color(0xFF94A3B8))
        )
        NavigationBarItem(
            selected = currentScreen == AdminSubScreen.PRICING_ZONES,
            onClick = { onSelectScreen(AdminSubScreen.PRICING_ZONES) },
            icon = { Icon(Icons.Default.PriceChange, contentDescription = "Pricing") },
            label = { Text("Pricing", fontSize = 10.sp) },
            colors = NavigationBarItemDefaults.colors(selectedIconColor = Color.Black, indicatorColor = VeloTeal, unselectedIconColor = Color(0xFF94A3B8))
        )
        NavigationBarItem(
            selected = currentScreen == AdminSubScreen.SUPPORT_TICKETS,
            onClick = { onSelectScreen(AdminSubScreen.SUPPORT_TICKETS) },
            icon = { Icon(Icons.Default.HeadsetMic, contentDescription = "Support") },
            label = { Text("Tickets", fontSize = 10.sp) },
            colors = NavigationBarItemDefaults.colors(selectedIconColor = Color.Black, indicatorColor = VeloTeal, unselectedIconColor = Color(0xFF94A3B8))
        )
    }
}
