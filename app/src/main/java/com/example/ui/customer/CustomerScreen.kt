package com.example.ui.customer

import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.local.entity.CouponEntity
import com.example.data.local.entity.ParcelEntity
import com.example.data.local.entity.RideEntity
import com.example.data.local.entity.UserEntity
import com.example.data.model.*
import com.example.domain.engine.IndianLocations
import com.example.ui.components.*
import com.example.ui.theme.*
import com.example.ui.viewmodel.*

enum class RapidoBottomNavTab {
    RIDE,
    ALL_SERVICES,
    TRAVEL,
    PROFILE
}

enum class CustomerBookingFlowState {
    HOME_EXPLORE,        // Image 2: Map + Where do you want to go? + Refer a Friend
    DROP_SEARCH,         // Image 1: Pickup + Drop location search card + Select on map + Add stops
    SELECT_VEHICLE,      // Bike Taxi, Auto, Prime selection with real fare
    ACTIVE_TRIP          // Live ride tracking
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomerScreen(
    viewModel: VeloGoViewModel,
    onExitToHub: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    var flowState by remember { mutableStateOf(CustomerBookingFlowState.HOME_EXPLORE) }
    var currentNavTab by remember { mutableStateOf(RapidoBottomNavTab.RIDE) }
    var forWhom by remember { mutableStateOf("For me") }

    val activeRide by viewModel.activeRide.collectAsStateWithLifecycle()
    val activeParcel by viewModel.activeParcel.collectAsStateWithLifecycle()
    val user by viewModel.currentUser.collectAsStateWithLifecycle()

    var showChatDialog by remember { mutableStateOf(false) }
    var showSosDialog by remember { mutableStateOf(false) }
    var showRatingDialog by remember { mutableStateOf(false) }
    var showCancelDialog by remember { mutableStateOf(false) }

    // Synchronize active trip with flow state
    LaunchedEffect(activeRide) {
        if (activeRide != null && activeRide!!.status != RideStatus.CANCELLED_BY_CUSTOMER.name) {
            flowState = CustomerBookingFlowState.ACTIVE_TRIP
        } else if (flowState == CustomerBookingFlowState.ACTIVE_TRIP) {
            flowState = CustomerBookingFlowState.HOME_EXPLORE
        }
    }

    Scaffold(
        bottomBar = {
            if (flowState == CustomerBookingFlowState.HOME_EXPLORE) {
                RapidoBottomNavigation(
                    currentTab = currentNavTab,
                    onSelectTab = { currentNavTab = it }
                )
            }
        },
        containerColor = Color(0xFFF8FAFC)
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(if (flowState == CustomerBookingFlowState.HOME_EXPLORE) innerPadding else PaddingValues())
        ) {
            when (currentNavTab) {
                RapidoBottomNavTab.RIDE -> {
                    when (flowState) {
                        CustomerBookingFlowState.HOME_EXPLORE -> {
                            RapidoHomeScreen(
                                viewModel = viewModel,
                                user = user,
                                onOpenSearch = { flowState = CustomerBookingFlowState.DROP_SEARCH },
                                onExitToHub = onExitToHub,
                                onOpenSafety = { showSosDialog = true }
                            )
                        }
                        CustomerBookingFlowState.DROP_SEARCH -> {
                            RapidoDropSearchScreen(
                                viewModel = viewModel,
                                forWhom = forWhom,
                                onForWhomChange = { forWhom = it },
                                onBack = { flowState = CustomerBookingFlowState.HOME_EXPLORE },
                                onLocationSelected = {
                                    flowState = CustomerBookingFlowState.SELECT_VEHICLE
                                }
                            )
                        }
                        CustomerBookingFlowState.SELECT_VEHICLE -> {
                            RapidoVehicleSelectionScreen(
                                viewModel = viewModel,
                                onBack = { flowState = CustomerBookingFlowState.DROP_SEARCH },
                                onRideBooked = {
                                    flowState = CustomerBookingFlowState.ACTIVE_TRIP
                                }
                            )
                        }
                        CustomerBookingFlowState.ACTIVE_TRIP -> {
                            RapidoActiveTripScreen(
                                viewModel = viewModel,
                                activeRide = activeRide,
                                onOpenChat = { showChatDialog = true },
                                onOpenSos = { showSosDialog = true },
                                onOpenCancel = { showCancelDialog = true },
                                onOpenRating = { showRatingDialog = true },
                                onDone = {
                                    viewModel.cancelActiveRide("Trip Completed")
                                    flowState = CustomerBookingFlowState.HOME_EXPLORE
                                }
                            )
                        }
                    }
                }
                RapidoBottomNavTab.ALL_SERVICES -> {
                    RapidoAllServicesScreen(
                        viewModel = viewModel,
                        onSelectService = { service ->
                            if (service == "BIKE_TAXI") {
                                currentNavTab = RapidoBottomNavTab.RIDE
                                flowState = CustomerBookingFlowState.DROP_SEARCH
                            } else {
                                currentNavTab = RapidoBottomNavTab.RIDE
                            }
                        }
                    )
                }
                RapidoBottomNavTab.TRAVEL -> {
                    RapidoTravelScreen(
                        onBookOutstation = {
                            currentNavTab = RapidoBottomNavTab.RIDE
                            flowState = CustomerBookingFlowState.DROP_SEARCH
                        }
                    )
                }
                RapidoBottomNavTab.PROFILE -> {
                    RapidoProfileScreen(
                        user = user,
                        viewModel = viewModel,
                        onExitToHub = onExitToHub
                    )
                }
            }

            // Dialogs
            if (showChatDialog && activeRide != null) {
                MaskedCallChatDialog(
                    contactName = activeRide?.captainName ?: "Captain",
                    contactPhone = activeRide?.captainPhone ?: "+91 98765 00000",
                    onDismiss = { showChatDialog = false }
                )
            }

            if (showSosDialog) {
                SafetySosDialog(
                    tripId = activeRide?.id ?: "SOS-LIVE",
                    onTriggerSos = {
                        viewModel.createSupportTicket(
                            SupportCategory.SAFETY_SOS,
                            SupportPriority.EMERGENCY_SOS,
                            "EMERGENCY SOS - ACTIVE TRIP",
                            "Emergency SOS triggered. Real-time GPS dispatched to emergency contacts."
                        )
                    },
                    onDismiss = { showSosDialog = false }
                )
            }

            if (showCancelDialog && activeRide != null) {
                RapidoCancelRideDialog(
                    onConfirmCancel = { reason ->
                        viewModel.cancelActiveRide(reason)
                        showCancelDialog = false
                        flowState = CustomerBookingFlowState.HOME_EXPLORE
                    },
                    onDismiss = { showCancelDialog = false }
                )
            }

            if (showRatingDialog && activeRide != null) {
                RapidoRateCaptainDialog(
                    captainName = activeRide?.captainName ?: "Captain",
                    onRate = { rating, review ->
                        viewModel.rateRide(activeRide!!.id, rating, review)
                        showRatingDialog = false
                        flowState = CustomerBookingFlowState.HOME_EXPLORE
                    },
                    onDismiss = {
                        showRatingDialog = false
                        flowState = CustomerBookingFlowState.HOME_EXPLORE
                    }
                )
            }
        }
    }
}

/**
 * SCREEN 2: EXACT RAPIDO HOME SCREEN FROM USER'S SCREENSHOT
 */
@Composable
private fun RapidoHomeScreen(
    viewModel: VeloGoViewModel,
    user: UserEntity?,
    onOpenSearch: () -> Unit,
    onExitToHub: (() -> Unit)? = null,
    onOpenSafety: () -> Unit
) {
    val pickup by viewModel.pickupLocation.collectAsStateWithLifecycle()
    val onlineCaptains by viewModel.onlineCaptains.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF1F5F9))
    ) {
        // TOP HALF: MAP VIEW WITH RAPIDO PICKUP PIN
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1.05f)
        ) {
            // Interactive Map
            InteractiveMapCanvas(
                modifier = Modifier.fillMaxSize(),
                pickupLat = pickup.lat,
                pickupLng = pickup.lng,
                pickupTitle = pickup.title,
                dropLat = null,
                dropLng = null,
                nearbyCaptains = onlineCaptains.map { Pair(it.latitude, it.longitude) }
            )

            // Top Floating Header: App Switcher / Wallet / Safety
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (onExitToHub != null) {
                    Surface(
                        shape = CircleShape,
                        color = Color.White,
                        shadowElevation = 6.dp,
                        modifier = Modifier.size(42.dp)
                    ) {
                        IconButton(onClick = onExitToHub) {
                            Icon(
                                imageVector = Icons.Default.Apps,
                                contentDescription = "Switch App",
                                tint = Color(0xFF0F172A),
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    }
                } else {
                    Spacer(modifier = Modifier.size(42.dp))
                }

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Safety Shield
                    Surface(
                        shape = CircleShape,
                        color = Color.White,
                        shadowElevation = 4.dp,
                        modifier = Modifier.size(40.dp)
                    ) {
                        IconButton(onClick = onOpenSafety) {
                            Icon(
                                imageVector = Icons.Default.Shield,
                                contentDescription = "Safety",
                                tint = VeloEmerald,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }

                    // Wallet pill
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = Color.White,
                        shadowElevation = 4.dp
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.AccountBalanceWallet,
                                contentDescription = "Wallet",
                                tint = VeloAmberDark,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = "₹${(user?.walletBalance ?: 420.0).toInt()}",
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 13.sp,
                                color = Color(0xFF0F172A)
                            )
                        }
                    }
                }
            }

            // RAPIDO GREEN "Pickup Point" CENTER PIN BADGE (Matching Screenshot 2)
            Column(
                modifier = Modifier.align(Alignment.Center),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Surface(
                    shape = RoundedCornerShape(18.dp),
                    color = Color(0xFF108A4E), // Rapido deep green
                    shadowElevation = 6.dp
                ) {
                    Text(
                        text = "Pickup Point",
                        color = Color.White,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 7.dp)
                    )
                }

                // Downward needle & ring
                Box(
                    modifier = Modifier
                        .width(2.dp)
                        .height(10.dp)
                        .background(Color(0xFF108A4E))
                )

                Box(
                    modifier = Modifier
                        .size(16.dp)
                        .background(Color(0xFF108A4E).copy(alpha = 0.3f), CircleShape)
                        .padding(3.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color(0xFF108A4E), CircleShape)
                            .border(2.dp, Color.White, CircleShape)
                    )
                }
            }

            // FLOATING CURRENT ADDRESS PILL (Bottom of Map, matching Screenshot 2)
            Surface(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(horizontal = 16.dp, vertical = 12.dp)
                    .fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                color = Color.White,
                shadowElevation = 6.dp
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .background(Color(0xFF108A4E), CircleShape)
                    )
                    Text(
                        text = pickup.address.ifEmpty { "7-16-160/2, Britan St, Abm Children Park Area, Pan..." },
                        color = Color(0xFF1E293B),
                        fontSize = 12.5.sp,
                        fontWeight = FontWeight.Medium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        // BOTTOM HALF: RAPIDO BOTTOM SHEET (Matching Screenshot 2)
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1.05f),
            shape = RoundedCornerShape(topStart = 26.dp, topEnd = 26.dp),
            color = Color.White,
            shadowElevation = 16.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 18.dp, vertical = 10.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Drag Handle Pill
                Box(
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .width(42.dp)
                        .height(4.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(Color(0xFFCBD5E1))
                )

                // 1. "Where do you want to go?" SEARCH BAR (Exact Rapido Component)
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(28.dp))
                        .clickable { onOpenSearch() },
                    shape = RoundedCornerShape(28.dp),
                    color = Color.White,
                    border = BorderStroke(1.2.dp, Color(0xFFE2E8F0)),
                    shadowElevation = 3.dp
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 18.dp, vertical = 14.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = "Search",
                                tint = Color(0xFF0F172A),
                                modifier = Modifier.size(24.dp)
                            )
                            Text(
                                text = "Where do you want to go?",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF0F172A)
                            )
                        }

                        // Rapido iconic yellow bottom glow line
                        Box(
                            modifier = Modifier
                                .align(Alignment.BottomStart)
                                .offset(y = 12.dp)
                                .fillMaxWidth()
                                .height(2.5.dp)
                                .background(
                                    Brush.horizontalGradient(
                                        listOf(
                                            Color(0xFFFFCC00).copy(alpha = 0.2f),
                                            Color(0xFFFFCC00),
                                            Color(0xFFFFCC00).copy(alpha = 0.2f)
                                        )
                                    )
                                )
                        )
                    }
                }

                // 2. QUICK SERVICE CATEGORIES (Bike, Auto, Cab, Parcel)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    QuickServiceCategoryItem(
                        title = "Bike Taxi",
                        icon = Icons.Default.DirectionsBike,
                        badge = "Fastest",
                        accentColor = Color(0xFFFFCC00),
                        onClick = onOpenSearch
                    )
                    QuickServiceCategoryItem(
                        title = "Auto",
                        icon = Icons.Default.ElectricRickshaw,
                        badge = "Pocket friendly",
                        accentColor = Color(0xFF108A4E),
                        onClick = onOpenSearch
                    )
                    QuickServiceCategoryItem(
                        title = "Parcel",
                        icon = Icons.Default.LocalShipping,
                        badge = "Express",
                        accentColor = Color(0xFF0284C7),
                        onClick = onOpenSearch
                    )
                    QuickServiceCategoryItem(
                        title = "Prime Bike",
                        icon = Icons.Default.TwoWheeler,
                        badge = "Top Captain",
                        accentColor = Color(0xFF8B5CF6),
                        onClick = onOpenSearch
                    )
                }

                // 3. REFERRAL BANNER CARD (Exact Rapido Component from Screenshot 2)
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(18.dp)),
                    shape = RoundedCornerShape(18.dp),
                    color = Color(0xFFF1F5F9),
                    border = BorderStroke(1.dp, Color(0xFFE2E8F0))
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                Brush.horizontalGradient(
                                    colors = listOf(
                                        Color(0xFFF8FAFC),
                                        Color(0xFFEFF6FF),
                                        Color(0xFFE0F2FE)
                                    )
                                )
                            )
                            .padding(16.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(
                                modifier = Modifier.weight(1f),
                                verticalArrangement = Arrangement.spacedBy(3.dp)
                            ) {
                                Text(
                                    text = "Enjoying Rapido rides?",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF0F172A)
                                )
                                Text(
                                    text = "Spread the word!",
                                    fontSize = 12.sp,
                                    color = Color(0xFF64748B)
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Refer a Friend  ›",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF2563EB)
                                )
                            }

                            // Megaphone 3D Illustration badge
                            Box(
                                modifier = Modifier
                                    .size(56.dp)
                                    .background(
                                        Brush.linearGradient(
                                            listOf(Color(0xFFFFCC00), Color(0xFFF59E0B))
                                        ),
                                        CircleShape
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Campaign,
                                    contentDescription = "Refer",
                                    tint = Color(0xFF0F172A),
                                    modifier = Modifier.size(32.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun QuickServiceCategoryItem(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    badge: String,
    accentColor: Color,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .clickable { onClick() }
            .padding(4.dp)
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .background(accentColor.copy(alpha = 0.15f), RoundedCornerShape(14.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = if (accentColor == Color(0xFFFFCC00)) Color(0xFFB45309) else accentColor,
                modifier = Modifier.size(26.dp)
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = title,
            fontSize = 11.5.sp,
            fontWeight = FontWeight.SemiBold,
            color = Color(0xFF1E293B)
        )
    }
}

/**
 * SCREEN 1: EXACT RAPIDO DROP SEARCH SCREEN FROM USER'S SCREENSHOT
 */
@Composable
private fun RapidoDropSearchScreen(
    viewModel: VeloGoViewModel,
    forWhom: String,
    onForWhomChange: (String) -> Unit,
    onBack: () -> Unit,
    onLocationSelected: () -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    var showForWhomDialog by remember { mutableStateOf(false) }
    val focusRequester = remember { FocusRequester() }
    val focusManager = LocalFocusManager.current
    val pickup by viewModel.pickupLocation.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }

    val filteredLocations = remember(searchQuery) {
        if (searchQuery.isBlank()) {
            IndianLocations.POPULAR_HOTSPOTS
        } else {
            IndianLocations.POPULAR_HOTSPOTS.filter {
                it.title.contains(searchQuery, ignoreCase = true) ||
                it.address.contains(searchQuery, ignoreCase = true)
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        // 1. TOP HEADER: Back Arrow | "Drop" title | "For me ∨" Pill (Screenshot 1)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                IconButton(
                    onClick = onBack,
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Back",
                        tint = Color(0xFF0F172A),
                        modifier = Modifier.size(24.dp)
                    )
                }

                Text(
                    text = "Drop",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF0F172A)
                )
            }

            // "For me ∨" Pill Dropdown
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = Color.White,
                border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                modifier = Modifier.clickable { showForWhomDialog = true }
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = forWhom,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF0F172A)
                    )
                    Icon(
                        imageVector = Icons.Default.KeyboardArrowDown,
                        contentDescription = "Dropdown",
                        tint = Color(0xFF0F172A),
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }

        // 2. UNIFIED PICKUP & DROP LOCATION CARD (Exact Screenshot 1 Layout)
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            shape = RoundedCornerShape(16.dp),
            color = Color.White,
            border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
            shadowElevation = 1.dp
        ) {
            Column(
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                // Pickup Point Row (Green Ring + Address)
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(14.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    // Green Ring Marker Dot
                    Box(
                        modifier = Modifier
                            .size(16.dp)
                            .border(3.dp, Color(0xFF108A4E), CircleShape)
                            .padding(2.dp)
                    )

                    Text(
                        text = pickup.address.ifEmpty { "7-16-160/2, Britan St, Abm Children Park Area" },
                        fontSize = 13.5.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFF1E293B),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                }

                // Dotted connector line
                Row(
                    modifier = Modifier.padding(start = 7.dp)
                ) {
                    Column(
                        modifier = Modifier.height(16.dp),
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        Box(modifier = Modifier.size(2.dp).background(Color(0xFF94A3B8), CircleShape))
                        Box(modifier = Modifier.size(2.dp).background(Color(0xFF94A3B8), CircleShape))
                        Box(modifier = Modifier.size(2.dp).background(Color(0xFF94A3B8), CircleShape))
                    }
                }

                // Drop Point Row (Red/Orange Ring + Editable Search TextField)
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(14.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    // Red/Brown Ring Marker Dot
                    Box(
                        modifier = Modifier
                            .size(16.dp)
                            .border(3.dp, Color(0xFFC2410C), CircleShape)
                            .padding(2.dp)
                    )

                    Box(modifier = Modifier.weight(1f)) {
                        if (searchQuery.isEmpty()) {
                            Text(
                                text = "Drop location",
                                fontSize = 14.sp,
                                color = Color(0xFF94A3B8)
                            )
                        }
                        BasicTextField(
                            value = searchQuery,
                            onValueChange = { searchQuery = it },
                            modifier = Modifier
                                .fillMaxWidth()
                                .focusRequester(focusRequester),
                            textStyle = TextStyle(
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFF0F172A)
                            ),
                            cursorBrush = SolidColor(Color(0xFF0F172A)),
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                            keyboardActions = KeyboardActions(
                                onSearch = { focusManager.clearFocus() }
                            )
                        )
                    }

                    if (searchQuery.isNotEmpty()) {
                        IconButton(
                            onClick = { searchQuery = "" },
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Clear",
                                tint = Color(0xFF64748B),
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }
        }

        // 3. QUICK ACTION PILLS (Select on map + Add stops) (Exact Screenshot 1)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // "Select on map" Pill
            Surface(
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .clickable {
                        viewModel.dropLocation.value = IndianLocations.POPULAR_HOTSPOTS.first()
                        onLocationSelected()
                    },
                shape = RoundedCornerShape(20.dp),
                color = Color.White,
                border = BorderStroke(1.dp, Color(0xFFE2E8F0))
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.LocationOn,
                        contentDescription = "Select on map",
                        tint = Color(0xFF0F172A),
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        text = "Select on map",
                        fontSize = 12.5.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF0F172A)
                    )
                }
            }

            // "Add stops" Pill
            Surface(
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .clickable { /* Multi-stop feature */ },
                shape = RoundedCornerShape(20.dp),
                color = Color.White,
                border = BorderStroke(1.dp, Color(0xFFE2E8F0))
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(16.dp)
                            .background(Color(0xFF0F172A), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Add",
                            tint = Color.White,
                            modifier = Modifier.size(12.dp)
                        )
                    }
                    Text(
                        text = "Add stops",
                        fontSize = 12.5.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF0F172A)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // 4. SEARCH SUGGESTIONS OR EMPTY STATE (Exact Screenshot 1)
        if (filteredLocations.isEmpty()) {
            // Cute Empty State Illustration ("Could not get address")
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(100.dp)
                        .background(Color(0xFFF1F5F9), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.FindInPage,
                        contentDescription = null,
                        tint = Color(0xFF94A3B8),
                        modifier = Modifier.size(54.dp)
                    )
                }
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "Could not get address",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF64748B)
                )
                Text(
                    text = "Try searching with a nearby landmark or street name",
                    fontSize = 12.sp,
                    color = Color(0xFF94A3B8),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 32.dp, vertical = 4.dp)
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item {
                    Text(
                        text = if (searchQuery.isBlank()) "Recent & Popular Destinations" else "Search Results",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF94A3B8),
                        modifier = Modifier.padding(vertical = 4.dp)
                    )
                }

                items(filteredLocations) { loc ->
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .clickable {
                                viewModel.dropLocation.value = loc
                                onLocationSelected()
                            },
                        color = Color.Transparent
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(38.dp)
                                    .background(Color(0xFFF1F5F9), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = if (loc.title.contains("Metro") || loc.title.contains("Station")) Icons.Default.Train else Icons.Default.LocationOn,
                                    contentDescription = null,
                                    tint = Color(0xFF64748B),
                                    modifier = Modifier.size(20.dp)
                                )
                            }

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = loc.title,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF0F172A)
                                )
                                Text(
                                    text = loc.address,
                                    fontSize = 12.sp,
                                    color = Color(0xFF64748B),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }

                            Icon(
                                imageVector = Icons.Default.NorthWest,
                                contentDescription = "Select",
                                tint = Color(0xFF94A3B8),
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                    Divider(color = Color(0xFFF1F5F9), thickness = 1.dp)
                }
            }
        }
    }

    // "For me" vs "For someone else" Dialog
    if (showForWhomDialog) {
        AlertDialog(
            onDismissRequest = { showForWhomDialog = false },
            title = { Text("Who are you booking for?", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .clickable {
                                onForWhomChange("For me")
                                showForWhomDialog = false
                            },
                        color = if (forWhom == "For me") Color(0xFFFFCC00).copy(alpha = 0.2f) else Color.Transparent
                    ) {
                        Text("🙋 For me (My account)", modifier = Modifier.padding(12.dp), fontWeight = FontWeight.SemiBold)
                    }

                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .clickable {
                                onForWhomChange("For someone else")
                                showForWhomDialog = false
                            },
                        color = if (forWhom == "For someone else") Color(0xFFFFCC00).copy(alpha = 0.2f) else Color.Transparent
                    ) {
                        Text("👥 For someone else (Choose contact)", modifier = Modifier.padding(12.dp), fontWeight = FontWeight.SemiBold)
                    }
                }
            },
            confirmButton = {}
        )
    }
}

/**
 * SCREEN 3: RAPIDO VEHICLE SELECTION & BOOKING CONFIRMATION
 */
@Composable
private fun RapidoVehicleSelectionScreen(
    viewModel: VeloGoViewModel,
    onBack: () -> Unit,
    onRideBooked: () -> Unit
) {
    val pickup by viewModel.pickupLocation.collectAsStateWithLifecycle()
    val drop by viewModel.dropLocation.collectAsStateWithLifecycle()
    val vehicleType by viewModel.selectedVehicleType.collectAsStateWithLifecycle()
    val paymentMethod by viewModel.selectedPaymentMethod.collectAsStateWithLifecycle()
    val appliedCoupon by viewModel.appliedCoupon.collectAsStateWithLifecycle()
    val calculatedFare by viewModel.calculatedFare.collectAsStateWithLifecycle()
    val onlineCaptains by viewModel.onlineCaptains.collectAsStateWithLifecycle()

    var showCouponDialog by remember { mutableStateOf(false) }
    var showPaymentDialog by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8FAFC))
    ) {
        // TOP: Map preview with route polyline
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) {
            InteractiveMapCanvas(
                modifier = Modifier.fillMaxSize(),
                pickupLat = pickup.lat,
                pickupLng = pickup.lng,
                pickupTitle = pickup.title,
                dropLat = drop.lat,
                dropLng = drop.lng,
                dropTitle = drop.title,
                nearbyCaptains = onlineCaptains.map { Pair(it.latitude, it.longitude) }
            )

            // Back Button
            IconButton(
                onClick = onBack,
                modifier = Modifier
                    .statusBarsPadding()
                    .padding(16.dp)
                    .size(40.dp)
                    .background(Color.White, CircleShape)
                    .shadow(4.dp, CircleShape)
            ) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color(0xFF0F172A))
            }
        }

        // BOTTOM: Rapido vehicle options sheet
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1.3f),
            shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
            color = Color.White,
            shadowElevation = 16.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    // Route Info Header
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "To: ${drop.title}",
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                color = Color(0xFF0F172A)
                            )
                            Text(
                                text = "Est. Distance: ${(calculatedFare.distanceFare / 9.5).format(1)} km  •  8 mins away",
                                fontSize = 12.sp,
                                color = Color(0xFF64748B)
                            )
                        }

                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color(0xFFECFDF5)
                        ) {
                            Text(
                                text = "⚡ High Demand",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF059669),
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }

                    Divider(color = Color(0xFFF1F5F9))

                    // Vehicle Type Options
                    VehicleOptionCard(
                        title = "Bike Taxi",
                        subtitle = "Beat the traffic, fastest ride",
                        price = "₹${calculatedFare.totalFare.toInt()}",
                        eta = "3 min away",
                        icon = Icons.Default.DirectionsBike,
                        isSelected = vehicleType == VehicleType.BIKE_STANDARD,
                        onClick = { viewModel.selectedVehicleType.value = VehicleType.BIKE_STANDARD }
                    )

                    VehicleOptionCard(
                        title = "Prime Bike (Captain + Helmet)",
                        subtitle = "Top rated captains with sanitized helmet",
                        price = "₹${(calculatedFare.totalFare * 1.2).toInt()}",
                        eta = "2 min away",
                        icon = Icons.Default.TwoWheeler,
                        isSelected = vehicleType == VehicleType.MOTO_PRIME,
                        onClick = { viewModel.selectedVehicleType.value = VehicleType.MOTO_PRIME }
                    )

                    VehicleOptionCard(
                        title = "Electric EV Bike",
                        subtitle = "Zero emissions, eco-friendly",
                        price = "₹${(calculatedFare.totalFare * 0.95).toInt()}",
                        eta = "5 min away",
                        icon = Icons.Default.ElectricBike,
                        isSelected = vehicleType == VehicleType.ECO_EV,
                        onClick = { viewModel.selectedVehicleType.value = VehicleType.ECO_EV }
                    )

                    // Payment & Coupon Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Surface(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(10.dp))
                                .clickable { showPaymentDialog = true },
                            color = Color(0xFFF8FAFC),
                            border = BorderStroke(1.dp, Color(0xFFE2E8F0))
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(Icons.Default.Payment, contentDescription = null, tint = Color(0xFF108A4E), modifier = Modifier.size(16.dp))
                                Text(paymentMethod.name, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0F172A))
                            }
                        }

                        Surface(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(10.dp))
                                .clickable { showCouponDialog = true },
                            color = Color(0xFFF8FAFC),
                            border = BorderStroke(1.dp, Color(0xFFE2E8F0))
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(Icons.Default.LocalOffer, contentDescription = null, tint = Color(0xFFD97706), modifier = Modifier.size(16.dp))
                                Text(appliedCoupon?.code ?: "Coupons", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0F172A))
                            }
                        }
                    }
                }

                // BOOK BIKE TAXI PRIMARY BUTTON (Rapido Yellow Style)
                Button(
                    onClick = {
                        viewModel.bookBikeRide()
                        onRideBooked()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFFFCC00), // Rapido Yellow
                        contentColor = Color(0xFF0F172A)
                    ),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp)
                ) {
                    Text(
                        text = "BOOK BIKE TAXI  •  ₹${calculatedFare.totalFare.toInt()}",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 0.5.sp
                    )
                }
            }
        }
    }

    if (showPaymentDialog) {
        AlertDialog(
            onDismissRequest = { showPaymentDialog = false },
            title = { Text("Select Payment Method", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    PaymentMethod.values().forEach { method ->
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .clickable {
                                    viewModel.selectedPaymentMethod.value = method
                                    showPaymentDialog = false
                                },
                            color = if (paymentMethod == method) Color(0xFFFFCC00).copy(alpha = 0.2f) else Color.Transparent
                        ) {
                            Text(method.label, modifier = Modifier.padding(12.dp), fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            },
            confirmButton = {}
        )
    }

    if (showCouponDialog) {
        CustomerCouponsDialog(
            viewModel = viewModel,
            onDismiss = { showCouponDialog = false }
        )
    }
}

@Composable
private fun VehicleOptionCard(
    title: String,
    subtitle: String,
    price: String,
    eta: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .clickable { onClick() },
        shape = RoundedCornerShape(14.dp),
        color = if (isSelected) Color(0xFFFFFBEB) else Color.White,
        border = BorderStroke(if (isSelected) 2.dp else 1.dp, if (isSelected) Color(0xFFFFCC00) else Color(0xFFE2E8F0))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .background(if (isSelected) Color(0xFFFFCC00) else Color(0xFFF1F5F9), RoundedCornerShape(10.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(icon, contentDescription = title, tint = Color(0xFF0F172A), modifier = Modifier.size(24.dp))
                }

                Column {
                    Text(text = title, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0F172A))
                    Text(text = "$eta  •  $subtitle", fontSize = 11.sp, color = Color(0xFF64748B), maxLines = 1)
                }
            }

            Text(text = price, fontSize = 16.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFF0F172A))
        }
    }
}

/**
 * SCREEN 4: ACTIVE TRIP TRACKING
 */
@Composable
private fun RapidoActiveTripScreen(
    viewModel: VeloGoViewModel,
    activeRide: RideEntity?,
    onOpenChat: () -> Unit,
    onOpenSos: () -> Unit,
    onOpenCancel: () -> Unit,
    onOpenRating: () -> Unit,
    onDone: () -> Unit
) {
    val simProgress by viewModel.simulatedProgress.collectAsStateWithLifecycle()
    val simCapLat by viewModel.simulatedCaptainLat.collectAsStateWithLifecycle()
    val simCapLng by viewModel.simulatedCaptainLng.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8FAFC))
    ) {
        // Map Tracking Top
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1.1f)
        ) {
            InteractiveMapCanvas(
                modifier = Modifier.fillMaxSize(),
                pickupLat = activeRide?.pickupLat,
                pickupLng = activeRide?.pickupLng,
                pickupTitle = activeRide?.pickupTitle,
                dropLat = activeRide?.dropLat,
                dropLng = activeRide?.dropLng,
                dropTitle = activeRide?.dropTitle,
                captainLat = simCapLat,
                captainLng = simCapLng,
                captainName = activeRide?.captainName ?: "Nearby Captain",
                routeProgress = simProgress
            )
        }

        // Bottom Trip Status Card
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
            color = Color.White,
            shadowElevation = 16.dp
        ) {
            if (activeRide != null && activeRide.status == RideStatus.RIDE_COMPLETED.name) {
                // Trip completed dialog & feedback
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color(0xFF108A4E), modifier = Modifier.size(48.dp))
                        Text("Ride Completed! 🎉", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0F172A))
                        Text("Total Paid: ₹${activeRide.totalFare.toInt()}", fontSize = 16.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFF0F172A))
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Button(
                            onClick = onOpenRating,
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFCC00), contentColor = Color(0xFF0F172A))
                        ) {
                            Text("⭐ Rate Captain", fontWeight = FontWeight.Bold)
                        }
                        OutlinedButton(
                            onClick = onDone,
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("Done")
                        }
                    }
                }
            } else if (activeRide != null) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            StatusBadge(
                                statusText = activeRide.status.replace("_", " "),
                                color = if (activeRide.status == RideStatus.RIDE_STARTED.name) Color(0xFF108A4E) else Color(0xFFD97706)
                            )

                            // OTP Pill
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = Color(0xFFFEF3C7)
                            ) {
                                Text(
                                    text = "START OTP: ${activeRide.otp}",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Color(0xFFB45309),
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                )
                            }
                        }

                        // Captain Information
                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = Color(0xFFF8FAFC),
                            border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(44.dp)
                                            .background(Color(0xFFFFCC00), CircleShape),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text("🏍️", fontSize = 20.sp)
                                    }
                                    Column {
                                        Text(
                                            text = activeRide.captainName ?: "Connecting Captain...",
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFF0F172A)
                                        )
                                        Text(
                                            text = "${activeRide.captainVehicleModel} • ${activeRide.captainVehicleNumber}",
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = Color(0xFFB45309)
                                        )
                                        Text(
                                            text = "⭐ 4.9 (450+ rides) • Safe Captain",
                                            fontSize = 11.sp,
                                            color = Color(0xFF64748B)
                                        )
                                    }
                                }

                                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    IconButton(
                                        onClick = onOpenChat,
                                        modifier = Modifier
                                            .size(38.dp)
                                            .background(Color(0xFFE2E8F0), CircleShape)
                                    ) {
                                        Icon(Icons.Default.Chat, contentDescription = "Chat", tint = Color(0xFF0F172A), modifier = Modifier.size(18.dp))
                                    }
                                    IconButton(
                                        onClick = onOpenChat,
                                        modifier = Modifier
                                            .size(38.dp)
                                            .background(Color(0xFF108A4E).copy(alpha = 0.15f), CircleShape)
                                    ) {
                                        Icon(Icons.Default.Call, contentDescription = "Call", tint = Color(0xFF108A4E), modifier = Modifier.size(18.dp))
                                    }
                                }
                            }
                        }
                    }

                    // Bottom Action Controls
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Button(
                            onClick = onOpenSos,
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFEE2E2), contentColor = Color(0xFFDC2626))
                        ) {
                            Icon(Icons.Default.Shield, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Safety SOS", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }

                        OutlinedButton(
                            onClick = onOpenCancel,
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("Cancel Ride", fontSize = 12.sp)
                        }
                    }
                }
            }
        }
    }
}

/**
 * RAPIDO BOTTOM NAVIGATION BAR (Matching Screenshot 2)
 */
@Composable
private fun RapidoBottomNavigation(
    currentTab: RapidoBottomNavTab,
    onSelectTab: (RapidoBottomNavTab) -> Unit
) {
    NavigationBar(
        containerColor = Color.White,
        tonalElevation = 8.dp
    ) {
        NavigationBarItem(
            selected = currentTab == RapidoBottomNavTab.RIDE,
            onClick = { onSelectTab(RapidoBottomNavTab.RIDE) },
            icon = { Icon(Icons.Default.Home, contentDescription = "Ride") },
            label = { Text("Ride", fontSize = 11.sp, fontWeight = if (currentTab == RapidoBottomNavTab.RIDE) FontWeight.Bold else FontWeight.Normal) },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = Color(0xFF0F172A),
                selectedTextColor = Color(0xFF0F172A),
                indicatorColor = Color(0xFFFFCC00).copy(alpha = 0.4f),
                unselectedIconColor = Color(0xFF94A3B8),
                unselectedTextColor = Color(0xFF94A3B8)
            )
        )

        NavigationBarItem(
            selected = currentTab == RapidoBottomNavTab.ALL_SERVICES,
            onClick = { onSelectTab(RapidoBottomNavTab.ALL_SERVICES) },
            icon = { Icon(Icons.Default.NearMe, contentDescription = "All Services") },
            label = { Text("All Services", fontSize = 11.sp) },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = Color(0xFF0F172A),
                indicatorColor = Color(0xFFFFCC00).copy(alpha = 0.4f),
                unselectedIconColor = Color(0xFF94A3B8)
            )
        )

        NavigationBarItem(
            selected = currentTab == RapidoBottomNavTab.TRAVEL,
            onClick = { onSelectTab(RapidoBottomNavTab.TRAVEL) },
            icon = { Icon(Icons.Default.BeachAccess, contentDescription = "Travel") },
            label = { Text("Travel", fontSize = 11.sp) },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = Color(0xFF0F172A),
                indicatorColor = Color(0xFFFFCC00).copy(alpha = 0.4f),
                unselectedIconColor = Color(0xFF94A3B8)
            )
        )

        NavigationBarItem(
            selected = currentTab == RapidoBottomNavTab.PROFILE,
            onClick = { onSelectTab(RapidoBottomNavTab.PROFILE) },
            icon = { Icon(Icons.Default.Person, contentDescription = "Profile") },
            label = { Text("Profile", fontSize = 11.sp) },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = Color(0xFF0F172A),
                indicatorColor = Color(0xFFFFCC00).copy(alpha = 0.4f),
                unselectedIconColor = Color(0xFF94A3B8)
            )
        )
    }
}

@Composable
private fun RapidoAllServicesScreen(
    viewModel: VeloGoViewModel,
    onSelectService: (String) -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Text("All Rapido Services", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0F172A))
            Text("Fastest commute & delivery solutions", fontSize = 13.sp, color = Color(0xFF64748B))
        }

        item {
            ServiceHighlightCard(
                title = "Bike Taxi",
                desc = "Fastest way to cut through city traffic",
                badge = "⚡ 1 Seat",
                icon = Icons.Default.DirectionsBike,
                color = Color(0xFFFFCC00),
                onClick = { onSelectService("BIKE_TAXI") }
            )
        }

        item {
            ServiceHighlightCard(
                title = "Rapido Auto",
                desc = "Affordable door-to-door 3-wheeler auto rides",
                badge = "🛺 3 Seats",
                icon = Icons.Default.ElectricRickshaw,
                color = Color(0xFF108A4E),
                onClick = { onSelectService("AUTO") }
            )
        }

        item {
            ServiceHighlightCard(
                title = "Parcel Express Delivery",
                desc = "Send packages, boxes, documents, or keys anywhere in town",
                badge = "📦 Up to 10kg",
                icon = Icons.Default.LocalShipping,
                color = Color(0xFF0284C7),
                onClick = { onSelectService("PARCEL") }
            )
        }
    }
}

@Composable
private fun ServiceHighlightCard(
    title: String,
    desc: String,
    badge: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: Color,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        color = Color.White,
        border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
        shadowElevation = 2.dp
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .background(color.copy(alpha = 0.2f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = title, tint = if (color == Color(0xFFFFCC00)) Color(0xFFB45309) else color, modifier = Modifier.size(28.dp))
            }

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(title, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = Color(0xFF0F172A))
                    Surface(shape = RoundedCornerShape(6.dp), color = Color(0xFFF1F5F9)) {
                        Text(badge, fontSize = 10.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                    }
                }
                Text(desc, fontSize = 12.sp, color = Color(0xFF64748B), modifier = Modifier.padding(top = 2.dp))
            }

            Icon(Icons.Default.ArrowForward, contentDescription = null, tint = Color(0xFF94A3B8), modifier = Modifier.size(18.dp))
        }
    }
}

@Composable
private fun RapidoTravelScreen(onBookOutstation: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Text("Travel & Outstation", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0F172A))
        Text("Rentals, Outstation trips and Airport transfers", fontSize = 13.sp, color = Color(0xFF64748B))

        Surface(
            shape = RoundedCornerShape(16.dp),
            color = Color(0xFFEFF6FF),
            border = BorderStroke(1.dp, Color(0xFFBFDBFE)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("✈️ Airport Direct Express", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = Color(0xFF1E3A8A))
                Text("Flat rates with guaranteed on-time bike or cab transfers to Kempegowda Airport.", fontSize = 12.sp, color = Color(0xFF3B82F6))
                Button(
                    onClick = onBookOutstation,
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2563EB))
                ) {
                    Text("Book Airport Transfer")
                }
            }
        }
    }
}

@Composable
private fun RapidoProfileScreen(
    user: UserEntity?,
    viewModel: VeloGoViewModel,
    onExitToHub: (() -> Unit)?
) {
    val txs by viewModel.walletTransactions.collectAsStateWithLifecycle()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            // Profile Card
            Surface(
                shape = RoundedCornerShape(18.dp),
                color = Color.White,
                border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                modifier = Modifier.fillMaxWidth(),
                shadowElevation = 2.dp
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(54.dp)
                            .background(Color(0xFFFFCC00), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(user?.name?.take(1) ?: "A", fontSize = 22.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFF0F172A))
                    }

                    Column(modifier = Modifier.weight(1f)) {
                        Text(user?.name ?: "Aarav Sharma", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0F172A))
                        Text(user?.phone ?: "+91 98765 43210", fontSize = 12.sp, color = Color(0xFF64748B))
                        Text("⭐ 4.9 Rating  •  Rapido Power Rider", fontSize = 11.sp, color = Color(0xFF108A4E), fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }

        item {
            // VeloPay / Rapido Wallet
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = Color(0xFF0F172A),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("VeloPay Wallet", color = Color(0xFF94A3B8), fontSize = 12.sp)
                    Text("₹${user?.walletBalance ?: 420.0}", color = Color(0xFFFFCC00), fontSize = 26.sp, fontWeight = FontWeight.ExtraBold)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(
                            onClick = { viewModel.addMoneyToWallet(100.0) },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF334155), contentColor = Color.White)
                        ) {
                            Text("+ ₹100")
                        }
                        Button(
                            onClick = { viewModel.addMoneyToWallet(500.0) },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF334155), contentColor = Color.White)
                        ) {
                            Text("+ ₹500")
                        }
                    }
                }
            }
        }

        if (onExitToHub != null) {
            item {
                OutlinedButton(
                    onClick = onExitToHub,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.Apps, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Switch Application Suite (Captain / Admin)")
                }
            }
        }
    }
}

@Composable
private fun RapidoCancelRideDialog(
    onConfirmCancel: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var selectedReason by remember { mutableStateOf("Changed my mind / Plans changed") }
    val reasons = listOf(
        "Changed my mind / Plans changed",
        "Captain taking too long to arrive",
        "Wrong pickup location entered",
        "Booked another ride",
        "Captain asked to cancel"
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Cancel Booking?", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Please choose a reason for cancellation:", fontSize = 12.sp, color = Color(0xFF64748B))
                reasons.forEach { reason ->
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .clickable { selectedReason = reason },
                        color = if (selectedReason == reason) Color(0xFFFEE2E2) else Color(0xFFF1F5F9)
                    ) {
                        Text(
                            text = reason,
                            modifier = Modifier.padding(10.dp),
                            fontSize = 12.sp,
                            fontWeight = if (selectedReason == reason) FontWeight.Bold else FontWeight.Normal,
                            color = if (selectedReason == reason) Color(0xFFDC2626) else Color(0xFF1E293B)
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirmCancel(selectedReason) },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDC2626))
            ) {
                Text("Cancel Ride")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Don't Cancel")
            }
        }
    )
}

@Composable
private fun RapidoRateCaptainDialog(
    captainName: String,
    onRate: (Float, String) -> Unit,
    onDismiss: () -> Unit
) {
    var rating by remember { mutableFloatStateOf(5f) }
    var reviewText by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Rate your ride with $captainName", fontWeight = FontWeight.Bold) },
        text = {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    (1..5).forEach { star ->
                        IconButton(onClick = { rating = star.toFloat() }) {
                            Icon(
                                imageVector = if (star <= rating) Icons.Default.Star else Icons.Default.StarBorder,
                                contentDescription = "$star stars",
                                tint = Color(0xFFFFCC00),
                                modifier = Modifier.size(32.dp)
                            )
                        }
                    }
                }
                OutlinedTextField(
                    value = reviewText,
                    onValueChange = { reviewText = it },
                    placeholder = { Text("Add feedback for the captain (optional)") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onRate(rating, reviewText) },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFCC00), contentColor = Color(0xFF0F172A))
            ) {
                Text("Submit Rating", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Skip")
            }
        }
    )
}

@Composable
private fun CustomerCouponsDialog(
    viewModel: VeloGoViewModel,
    onDismiss: () -> Unit
) {
    val coupons by viewModel.coupons.collectAsStateWithLifecycle()

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Available Promo Coupons", fontWeight = FontWeight.Bold) },
        text = {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(coupons) { coupon ->
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .clickable {
                                viewModel.appliedCoupon.value = coupon
                                onDismiss()
                            },
                        color = Color(0xFFF1F5F9),
                        border = BorderStroke(1.dp, Color(0xFFCBD5E1))
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Text(coupon.code, fontWeight = FontWeight.ExtraBold, color = Color(0xFFB45309))
                            Text(coupon.description, fontSize = 12.sp, color = Color(0xFF475569))
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Close")
            }
        }
    )
}

private fun Double.format(digits: Int) = "%.${digits}f".format(this)
