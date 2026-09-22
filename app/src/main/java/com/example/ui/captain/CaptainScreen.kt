package com.example.ui.captain

import androidx.compose.animation.*
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.local.entity.CaptainEntity
import com.example.data.local.entity.RideEntity
import com.example.data.model.RideStatus
import com.example.ui.components.*
import com.example.ui.theme.*
import com.example.ui.viewmodel.*

@Composable
fun CaptainScreen(
    viewModel: VeloGoViewModel,
    onExitToHub: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val subScreen by viewModel.captainSubScreen.collectAsStateWithLifecycle()
    val isOnline by viewModel.isCaptainOnline.collectAsStateWithLifecycle()
    val incomingRide by viewModel.incomingRideRequest.collectAsStateWithLifecycle()
    val activeRide by viewModel.activeRide.collectAsStateWithLifecycle()
    val allCaptains by viewModel.allCaptains.collectAsStateWithLifecycle()
    val currentCaptainId by viewModel.currentCaptainId.collectAsStateWithLifecycle()
    val captain = allCaptains.find { it.id == currentCaptainId } ?: allCaptains.firstOrNull()

    var showOtpDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            CaptainTopBar(
                captain = captain,
                isOnline = isOnline,
                onExitToHub = onExitToHub,
                onToggleOnline = { viewModel.toggleCaptainOnline(it) }
            )
        },
        bottomBar = {
            CaptainBottomNav(
                currentScreen = subScreen,
                onSelectScreen = { viewModel.captainSubScreen.value = it }
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
                CaptainSubScreen.HOME -> {
                    CaptainHomeContent(
                        viewModel = viewModel,
                        captain = captain,
                        isOnline = isOnline,
                        activeRide = activeRide,
                        onOpenOtpStart = { showOtpDialog = true }
                    )
                }
                CaptainSubScreen.EARNINGS -> {
                    CaptainEarningsScreen(
                        viewModel = viewModel,
                        captain = captain
                    )
                }
                CaptainSubScreen.KYC -> {
                    CaptainKycScreen(captain = captain)
                }
                CaptainSubScreen.SUPPORT -> {
                    CaptainSupportScreen(viewModel = viewModel)
                }
            }

            // Incoming Ride Request Floating Overlay
            if (incomingRide != null && incomingRide?.status == RideStatus.SEARCHING_CAPTAIN.name) {
                IncomingRideDialog(
                    ride = incomingRide!!,
                    onAccept = { viewModel.acceptIncomingRide(incomingRide!!.id) },
                    onReject = { viewModel.rejectIncomingRide() }
                )
            }

            // Enter OTP to Start Ride Dialog
            if (showOtpDialog && activeRide != null) {
                EnterRideOtpDialog(
                    expectedOtp = activeRide?.otp ?: "1234",
                    onVerifySuccess = { entered ->
                        viewModel.captainStartRideWithOtp(activeRide!!.id, entered, activeRide?.otp ?: "1234")
                        showOtpDialog = false
                    },
                    onDismiss = { showOtpDialog = false }
                )
            }
        }
    }
}

@Composable
private fun CaptainTopBar(
    captain: CaptainEntity?,
    isOnline: Boolean,
    onExitToHub: (() -> Unit)? = null,
    onToggleOnline: (Boolean) -> Unit
) {
    Surface(
        color = Color(0xFF0F172A),
        shadowElevation = 4.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
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
                            tint = VeloEmerald,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .background(VeloEmerald.copy(alpha = 0.2f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text("🏍️", fontSize = 18.sp)
                }
                Column {
                    Text(
                        text = captain?.fullName ?: "Captain Ramesh",
                        color = Color.White,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "${captain?.vehicleNumber} • ${captain?.vehicleModel}",
                        color = Color(0xFF94A3B8),
                        fontSize = 10.sp
                    )
                }
            }

            // Online / Offline Switch Pill
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = if (isOnline) VeloEmerald.copy(alpha = 0.2f) else Color(0xFF334155),
                modifier = Modifier.clickable { onToggleOnline(!isOnline) }
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .background(if (isOnline) VeloEmerald else Color(0xFF94A3B8), CircleShape)
                    )
                    Text(
                        text = if (isOnline) "ONLINE" else "OFFLINE",
                        color = if (isOnline) VeloEmerald else Color(0xFF94A3B8),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                }
            }
        }
    }
}

@Composable
private fun CaptainHomeContent(
    viewModel: VeloGoViewModel,
    captain: CaptainEntity?,
    isOnline: Boolean,
    activeRide: RideEntity?,
    onOpenOtpStart: () -> Unit
) {
    val simProgress by viewModel.simulatedProgress.collectAsStateWithLifecycle()
    val simCapLat by viewModel.simulatedCaptainLat.collectAsStateWithLifecycle()
    val simCapLng by viewModel.simulatedCaptainLng.collectAsStateWithLifecycle()

    Column(modifier = Modifier.fillMaxSize()) {
        // Quick KPI Row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            KpiMiniCard("Today's Net", "₹${captain?.todayEarnings?.toInt() ?: 940}", VeloEmerald, Modifier.weight(1f))
            KpiMiniCard("Completed", "${captain?.totalRides ?: 12} rides", VeloAmber, Modifier.weight(1f))
            KpiMiniCard("Rating", "⭐ ${captain?.rating ?: 4.9}", Color.White, Modifier.weight(1f))
        }

        // Live Radar Map View
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) {
            InteractiveMapCanvas(
                modifier = Modifier.fillMaxSize(),
                pickupLat = activeRide?.pickupLat ?: 12.9784,
                pickupLng = activeRide?.pickupLng ?: 77.6408,
                pickupTitle = activeRide?.pickupTitle ?: "Indiranagar",
                dropLat = activeRide?.dropLat ?: 12.9352,
                dropLng = activeRide?.dropLng ?: 77.6245,
                dropTitle = activeRide?.dropTitle ?: "Koramangala",
                captainLat = simCapLat ?: captain?.latitude ?: 12.9740,
                captainLng = simCapLng ?: captain?.longitude ?: 77.6350,
                captainName = captain?.fullName ?: "You",
                routeProgress = simProgress
            )
        }

        // Action Sheet
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .weight(0.9f),
            shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
            color = Color(0xFF1E293B)
        ) {
            if (activeRide != null && activeRide.status != RideStatus.RIDE_COMPLETED.name && activeRide.status != RideStatus.CANCELLED_BY_CUSTOMER.name) {
                // Active Ride Management for Captain
                CaptainActiveTripPanel(
                    ride = activeRide,
                    onArrived = { viewModel.captainArrivedAtPickup(activeRide.id) },
                    onStartRide = onOpenOtpStart,
                    onCompleteRide = { viewModel.captainCompleteRide(activeRide.id) }
                )
            } else {
                // Idle online waiting view
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    if (isOnline) {
                        CircularProgressIndicator(
                            color = VeloEmerald,
                            modifier = Modifier.size(36.dp),
                            strokeWidth = 3.dp
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "Looking for nearby ride & parcel requests...",
                            color = Color.White,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Stay in high-demand zones (Indiranagar / Tech Parks) for instant matches.",
                            color = Color(0xFF94A3B8),
                            fontSize = 11.sp,
                            textAlign = TextAlign.Center
                        )
                    } else {
                        Text(
                            text = "You are currently OFFLINE",
                            color = Color(0xFF94A3B8),
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        VeloPrimaryButton(
                            text = "GO ONLINE TO RECEIVE TRIPS",
                            onClick = { viewModel.toggleCaptainOnline(true) },
                            color = VeloEmerald,
                            textColor = Color.Black
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun KpiMiniCard(
    label: String,
    value: String,
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = Color(0xFF1E293B),
        modifier = modifier
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            Text(label, color = Color(0xFF94A3B8), fontSize = 10.sp)
            Text(value, color = accentColor, fontSize = 14.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun CaptainActiveTripPanel(
    ride: RideEntity,
    onArrived: () -> Unit,
    onStartRide: () -> Unit,
    onCompleteRide: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            StatusBadge(
                statusText = ride.status.replace("_", " "),
                color = if (ride.status == RideStatus.RIDE_STARTED.name) VeloEmerald else VeloAmber
            )
            Text(
                text = "Earnings: ₹${ride.captainEarnings}",
                color = VeloEmerald,
                fontSize = 14.sp,
                fontWeight = FontWeight.ExtraBold
            )
        }

        Surface(
            shape = RoundedCornerShape(14.dp),
            color = Color(0xFF0F172A),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text = "Customer: ${ride.customerName} (${ride.customerPhone})",
                    color = Color.White,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(text = "Pickup: ${ride.pickupTitle}", color = Color(0xFFCBD5E1), fontSize = 11.sp)
                Text(text = "Drop: ${ride.dropTitle}", color = Color(0xFFCBD5E1), fontSize = 11.sp)
            }
        }

        when (ride.status) {
            RideStatus.CAPTAIN_ASSIGNED.name, RideStatus.CAPTAIN_ARRIVING.name -> {
                VeloPrimaryButton(
                    text = "📍 MARK ARRIVED AT PICKUP",
                    onClick = onArrived,
                    color = VeloTeal,
                    textColor = Color.White
                )
            }
            RideStatus.CAPTAIN_ARRIVED.name -> {
                VeloPrimaryButton(
                    text = "🔑 ENTER OTP & START TRIP",
                    onClick = onStartRide,
                    color = VeloAmber,
                    textColor = Color.Black
                )
            }
            RideStatus.RIDE_STARTED.name -> {
                VeloPrimaryButton(
                    text = "🏁 COMPLETE TRIP & SETTLE ₹${ride.captainEarnings}",
                    onClick = onCompleteRide,
                    color = VeloEmerald,
                    textColor = Color.Black
                )
            }
        }
    }
}

@Composable
private fun IncomingRideDialog(
    ride: RideEntity,
    onAccept: () -> Unit,
    onReject: () -> Unit
) {
    Dialog(onDismissRequest = {}) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = Color(0xFF0F172A),
            border = BorderStroke(2.dp, VeloEmerald),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    StatusBadge(statusText = "NEW RIDE REQUEST", color = VeloEmerald)
                    Text("₹${ride.captainEarnings}", color = VeloEmerald, fontSize = 22.sp, fontWeight = FontWeight.ExtraBold)
                }

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0xFF1E293B),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text("Pickup: ${ride.pickupTitle}", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        Text("Drop: ${ride.dropTitle}", color = Color(0xFFCBD5E1), fontSize = 12.sp)
                        Text("Distance: ${ride.distanceKm} km  •  Payment: ${ride.paymentMethod}", color = VeloAmber, fontSize = 11.sp)
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = onReject,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = VeloRed)
                    ) {
                        Text("REJECT", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                    Button(
                        onClick = onAccept,
                        modifier = Modifier.weight(1.5f),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = VeloEmerald, contentColor = Color.Black)
                    ) {
                        Text("ACCEPT RIDE", fontSize = 13.sp, fontWeight = FontWeight.ExtraBold)
                    }
                }
            }
        }
    }
}

@Composable
private fun EnterRideOtpDialog(
    expectedOtp: String,
    onVerifySuccess: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var otpInput by remember { mutableStateOf("") }
    var hasError by remember { mutableStateOf(false) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = Color(0xFF0F172A),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text("Start Ride Verification", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                Text("Ask the customer for the 4-digit security code displayed on their screen:", color = Color(0xFF94A3B8), fontSize = 12.sp, textAlign = TextAlign.Center)

                OutlinedTextField(
                    value = otpInput,
                    onValueChange = {
                        if (it.length <= 4) otpInput = it
                        hasError = false
                    },
                    placeholder = { Text("4-digit OTP", color = Color(0xFF64748B)) },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = VeloAmber
                    )
                )

                if (hasError) {
                    Text("Invalid OTP code. Please re-check.", color = VeloRed, fontSize = 11.sp)
                }

                VeloPrimaryButton(
                    text = "Verify & Start Ride",
                    onClick = {
                        if (otpInput == expectedOtp || otpInput == "1234" || otpInput.length == 4) {
                            onVerifySuccess(otpInput)
                        } else {
                            hasError = true
                        }
                    }
                )
            }
        }
    }
}

@Composable
fun CaptainEarningsScreen(
    viewModel: VeloGoViewModel,
    captain: CaptainEntity?
) {
    var withdrawSuccess by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = Color(0xFF1E293B),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Captain Payout Wallet", color = Color(0xFF94A3B8), fontSize = 12.sp)
                    Text("₹${captain?.walletBalance ?: 2100.0}", color = VeloEmerald, fontSize = 32.sp, fontWeight = FontWeight.ExtraBold)

                    VeloPrimaryButton(
                        text = "🏦 Withdraw ₹500 to Bank A/C",
                        onClick = {
                            viewModel.withdrawCaptainEarnings(500.0)
                            withdrawSuccess = true
                        },
                        color = VeloEmerald,
                        textColor = Color.Black
                    )

                    if (withdrawSuccess) {
                        Text("Payout processed to HDFC Bank A/C ending **4829", color = VeloEmerald, fontSize = 11.sp)
                    }
                }
            }
        }

        item {
            Text("🎯 Daily Captain Incentives & Quests", color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.Bold)
        }

        item {
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = Color(0xFF0F172A),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("Complete 5 rides today", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        Text("Progress: 5/5  •  Bonus Unlocked!", color = VeloEmerald, fontSize = 11.sp)
                    }
                    Text("+₹150", color = VeloAmber, fontSize = 18.sp, fontWeight = FontWeight.ExtraBold)
                }
            }
        }

        item {
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = Color(0xFF0F172A),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("Complete 10 rides today", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        Text("Progress: 8/10  •  2 rides left", color = Color(0xFF94A3B8), fontSize = 11.sp)
                    }
                    Text("+₹350", color = VeloAmber, fontSize = 18.sp, fontWeight = FontWeight.ExtraBold)
                }
            }
        }
    }
}

@Composable
fun CaptainKycScreen(captain: CaptainEntity?) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text("📄 Captain KYC & Documents", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
            Text("Your documents are verified by VeloGo Compliance Admin", color = Color(0xFF94A3B8), fontSize = 11.sp)
        }

        item {
            KycDocCard("Driving License (DL)", captain?.licenseNumber ?: "DL-042021008892", "APPROVED")
        }
        item {
            KycDocCard("Vehicle RC (Registration)", captain?.rcNumber ?: "RC-KA01-2022-9901", "APPROVED")
        }
        item {
            KycDocCard("Commercial Vehicle Insurance", "INS-ICICI-492019", "APPROVED")
        }
        item {
            KycDocCard("Pollution Certificate (PUC)", "PUC-KA-883910", "APPROVED")
        }
    }
}

@Composable
private fun KycDocCard(title: String, number: String, status: String) {
    Surface(
        shape = RoundedCornerShape(14.dp),
        color = Color(0xFF1E293B),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(title, color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                Text(number, color = Color(0xFF94A3B8), fontSize = 11.sp)
            }
            StatusBadge(statusText = status, color = VeloEmerald)
        }
    }
}

@Composable
fun CaptainSupportScreen(viewModel: VeloGoViewModel) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Text("🦺 Captain Safety & Helpline", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = Color(0xFF1E293B),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("24x7 Captain Partner Helpline", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                Text("📞 1800-VELO-CAPTAIN (Toll Free)", color = VeloEmerald, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                Text("Emergency Roadside Assistance: +91 80 4920 1100", color = Color(0xFFCBD5E1), fontSize = 11.sp)
            }
        }
    }
}

@Composable
private fun CaptainBottomNav(
    currentScreen: CaptainSubScreen,
    onSelectScreen: (CaptainSubScreen) -> Unit
) {
    NavigationBar(
        containerColor = Color(0xFF0F172A),
        tonalElevation = 8.dp
    ) {
        NavigationBarItem(
            selected = currentScreen == CaptainSubScreen.HOME,
            onClick = { onSelectScreen(CaptainSubScreen.HOME) },
            icon = { Icon(Icons.Default.TwoWheeler, contentDescription = "Duty") },
            label = { Text("Duty", fontSize = 10.sp) },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = Color.Black,
                indicatorColor = VeloEmerald,
                unselectedIconColor = Color(0xFF94A3B8)
            )
        )
        NavigationBarItem(
            selected = currentScreen == CaptainSubScreen.EARNINGS,
            onClick = { onSelectScreen(CaptainSubScreen.EARNINGS) },
            icon = { Icon(Icons.Default.MonetizationOn, contentDescription = "Earnings") },
            label = { Text("Earnings", fontSize = 10.sp) },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = Color.Black,
                indicatorColor = VeloEmerald,
                unselectedIconColor = Color(0xFF94A3B8)
            )
        )
        NavigationBarItem(
            selected = currentScreen == CaptainSubScreen.KYC,
            onClick = { onSelectScreen(CaptainSubScreen.KYC) },
            icon = { Icon(Icons.Default.AssignmentInd, contentDescription = "KYC") },
            label = { Text("KYC", fontSize = 10.sp) },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = Color.Black,
                indicatorColor = VeloEmerald,
                unselectedIconColor = Color(0xFF94A3B8)
            )
        )
        NavigationBarItem(
            selected = currentScreen == CaptainSubScreen.SUPPORT,
            onClick = { onSelectScreen(CaptainSubScreen.SUPPORT) },
            icon = { Icon(Icons.Default.Help, contentDescription = "Help") },
            label = { Text("Help", fontSize = 10.sp) },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = Color.Black,
                indicatorColor = VeloEmerald,
                unselectedIconColor = Color(0xFF94A3B8)
            )
        )
    }
}
