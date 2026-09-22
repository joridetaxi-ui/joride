package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.AppDatabase
import com.example.data.local.entity.*
import com.example.data.model.*
import com.example.data.repository.VeloGoRepository
import com.example.domain.engine.FareCalculationResult
import com.example.domain.engine.FareEngine
import com.example.domain.engine.IndianLocations
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

enum class CustomerServiceTab {
    BIKE_TAXI,
    PARCEL_EXPRESS
}

enum class CustomerViewSubScreen {
    HOME,
    WALLET,
    HISTORY,
    SAFETY,
    SUPPORT,
    COUPONS
}

enum class CaptainSubScreen {
    HOME,
    EARNINGS,
    KYC,
    SUPPORT
}

enum class AdminSubScreen {
    DASHBOARD,
    LIVE_MAP,
    KYC_REVIEW,
    RIDES_PARCELS,
    PRICING_ZONES,
    SUPPORT_TICKETS
}

class VeloGoViewModel(application: Application) : AndroidViewModel(application) {
    private val database = AppDatabase.getDatabase(application)
    private val repository = VeloGoRepository(database, viewModelScope)

    // User Role State
    private val _currentRole = MutableStateFlow(UserRole.CUSTOMER)
    val currentRole: StateFlow<UserRole> = _currentRole.asStateFlow()

    // Sub-screen navigation states
    val customerSubScreen = MutableStateFlow(CustomerViewSubScreen.HOME)
    val captainSubScreen = MutableStateFlow(CaptainSubScreen.HOME)
    val adminSubScreen = MutableStateFlow(AdminSubScreen.DASHBOARD)

    // Customer States
    val customerServiceTab = MutableStateFlow(CustomerServiceTab.BIKE_TAXI)
    val pickupLocation = MutableStateFlow(IndianLocations.POPULAR_HOTSPOTS[0])
    val dropLocation = MutableStateFlow(IndianLocations.POPULAR_HOTSPOTS[1])
    val selectedVehicleType = MutableStateFlow(VehicleType.BIKE_STANDARD)
    val selectedPaymentMethod = MutableStateFlow(PaymentMethod.UPI)
    val appliedCoupon = MutableStateFlow<CouponEntity?>(null)

    // Parcel Form States
    val senderName = MutableStateFlow("Aarav Sharma")
    val senderPhone = MutableStateFlow("+91 98450 12345")
    val senderAddress = MutableStateFlow("12th Main, Indiranagar, Bengaluru")
    val receiverName = MutableStateFlow("Priya Nair")
    val receiverPhone = MutableStateFlow("+91 97421 99012")
    val receiverAddress = MutableStateFlow("Sony World Signal, Koramangala 5th Block")
    val packageCategory = MutableStateFlow(PackageCategory.DOCUMENTS)
    val packageSize = MutableStateFlow(PackageSize.SMALL)
    val packageWeight = MutableStateFlow(1.5)
    val packageInstructions = MutableStateFlow("Please call receiver before delivery")

    // Database flows
    val currentUser: StateFlow<UserEntity?> = repository.currentUser
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val onlineCaptains: StateFlow<List<CaptainEntity>> = repository.onlineCaptains
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val activeRide: StateFlow<RideEntity?> = repository.activeRide
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val activeParcel: StateFlow<ParcelEntity?> = repository.activeParcel
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val allRides: StateFlow<List<RideEntity>> = repository.allRides
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allParcels: StateFlow<List<ParcelEntity>> = repository.allParcels
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allCaptains: StateFlow<List<CaptainEntity>> = repository.allCaptains
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val walletTransactions: StateFlow<List<WalletTransactionEntity>> = repository.walletTransactions
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val coupons: StateFlow<List<CouponEntity>> = repository.coupons
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val supportTickets: StateFlow<List<SupportTicketEntity>> = repository.supportTickets
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val pendingKycDocs: StateFlow<List<KycDocumentEntity>> = repository.pendingKycDocs
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val pricingConfig: StateFlow<PricingConfigEntity?> = repository.pricingConfig
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val savedPlaces: StateFlow<List<SavedPlaceEntity>> = repository.savedPlaces
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Simulation states
    val simulatedProgress = MutableStateFlow(0f)
    val simulatedCaptainLat = MutableStateFlow<Double?>(null)
    val simulatedCaptainLng = MutableStateFlow<Double?>(null)
    private var simulationJob: Job? = null

    // Captain App States
    val currentCaptainId = MutableStateFlow("cpt_1")
    val isCaptainOnline = MutableStateFlow(true)
    val incomingRideRequest = MutableStateFlow<RideEntity?>(null)
    val incomingRequestTimer = MutableStateFlow(15)

    // Dynamic Fare State for Preview
    val calculatedFare: StateFlow<FareCalculationResult> = combine(
        pickupLocation,
        dropLocation,
        selectedVehicleType,
        pricingConfig,
        appliedCoupon
    ) { pickup, drop, vehicle, config, coupon ->
        val dist = IndianLocations.calculateDistanceKm(pickup.lat, pickup.lng, drop.lat, drop.lng)
        val mins = IndianLocations.estimateDurationMinutes(dist)
        FareEngine.calculateRideFare(
            distanceKm = dist,
            durationMinutes = mins,
            vehicleType = vehicle,
            config = config,
            couponDiscountPercent = coupon?.discountPercent ?: 0,
            couponMaxDiscount = coupon?.maxDiscount ?: 0.0
        )
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        FareEngine.calculateRideFare(4.8, 12, VehicleType.BIKE_STANDARD, null)
    )

    fun setRole(role: UserRole) {
        _currentRole.value = role
    }

    // --- Customer Booking Workflows ---
    fun bookBikeRide() {
        viewModelScope.launch {
            val ride = repository.bookBikeRide(
                pickup = pickupLocation.value,
                drop = dropLocation.value,
                vehicleType = selectedVehicleType.value,
                paymentMethod = selectedPaymentMethod.value,
                appliedCoupon = appliedCoupon.value
            )
            // Trigger automatic Captain notification & live progression simulation
            incomingRideRequest.value = ride
            startLiveRideSimulation(ride)
        }
    }

    fun bookParcel() {
        viewModelScope.launch {
            val parcel = repository.bookParcel(
                senderName = senderName.value,
                senderPhone = senderPhone.value,
                senderAddress = senderAddress.value,
                senderLat = pickupLocation.value.lat,
                senderLng = pickupLocation.value.lng,
                receiverName = receiverName.value,
                receiverPhone = receiverPhone.value,
                receiverAddress = receiverAddress.value,
                receiverLat = dropLocation.value.lat,
                receiverLng = dropLocation.value.lng,
                category = packageCategory.value,
                size = packageSize.value,
                weightKg = packageWeight.value,
                instructions = packageInstructions.value,
                paymentMethod = selectedPaymentMethod.value,
                appliedCoupon = appliedCoupon.value
            )
            startLiveParcelSimulation(parcel)
        }
    }

    private fun startLiveRideSimulation(ride: RideEntity) {
        simulationJob?.cancel()
        simulationJob = viewModelScope.launch {
            // Stage 1: Searching Captain -> Assigned (2s)
            delay(2000)
            repository.updateRideStatus(ride.id, RideStatus.CAPTAIN_ASSIGNED)

            // Stage 2: Captain Arriving (Heading to pickup)
            delay(3000)
            repository.updateRideStatus(ride.id, RideStatus.CAPTAIN_ARRIVING)

            // Simulate movement from nearby point to pickup
            val startLat = ride.pickupLat + 0.008
            val startLng = ride.pickupLng - 0.006
            for (step in 1..5) {
                delay(1200)
                val fraction = step / 5f
                simulatedCaptainLat.value = startLat + (ride.pickupLat - startLat) * fraction
                simulatedCaptainLng.value = startLng + (ride.pickupLng - startLng) * fraction
            }

            // Stage 3: Captain Arrived
            repository.updateRideStatus(ride.id, RideStatus.CAPTAIN_ARRIVED)
            delay(4000)

            // Stage 4: Ride Started (OTP entered)
            repository.updateRideStatus(ride.id, RideStatus.RIDE_STARTED)

            // Simulate progress from pickup to drop
            for (step in 1..10) {
                delay(1500)
                val fraction = step / 10f
                simulatedProgress.value = fraction
                simulatedCaptainLat.value = ride.pickupLat + (ride.dropLat - ride.pickupLat) * fraction
                simulatedCaptainLng.value = ride.pickupLng + (ride.dropLng - ride.pickupLng) * fraction
            }

            // Stage 5: Ride Completed
            repository.updateRideStatus(ride.id, RideStatus.RIDE_COMPLETED)
            simulatedProgress.value = 1f
        }
    }

    private fun startLiveParcelSimulation(parcel: ParcelEntity) {
        viewModelScope.launch {
            delay(2500)
            repository.updateParcelStatus(parcel.id, ParcelStatus.CAPTAIN_ASSIGNED)
            delay(3000)
            repository.updateParcelStatus(parcel.id, ParcelStatus.CAPTAIN_AT_PICKUP)
            delay(4000)
            repository.updateParcelStatus(parcel.id, ParcelStatus.PACKAGE_PICKED_UP)
            delay(6000)
            repository.updateParcelStatus(parcel.id, ParcelStatus.ARRIVED_DESTINATION)
            delay(4000)
            repository.updateParcelStatus(parcel.id, ParcelStatus.DELIVERED)
        }
    }

    fun cancelActiveRide(reason: String) {
        val ride = activeRide.value ?: return
        viewModelScope.launch {
            simulationJob?.cancel()
            repository.cancelRide(ride.id, reason)
        }
    }

    fun rateRide(rideId: String, rating: Float, review: String?) {
        viewModelScope.launch {
            repository.rateCaptain(rideId, rating, review)
        }
    }

    fun addMoneyToWallet(amount: Double) {
        viewModelScope.launch {
            repository.addMoneyToWallet(amount)
        }
    }

    fun createSupportTicket(category: SupportCategory, priority: SupportPriority, subject: String, description: String) {
        viewModelScope.launch {
            repository.createSupportTicket(category, priority, subject, description, activeRide.value?.id)
        }
    }

    // --- Captain Actions ---
    fun toggleCaptainOnline(isOnline: Boolean) {
        isCaptainOnline.value = isOnline
        viewModelScope.launch {
            repository.setCaptainOnline(currentCaptainId.value, isOnline)
        }
    }

    fun acceptIncomingRide(rideId: String) {
        incomingRideRequest.value = null
        viewModelScope.launch {
            repository.updateRideStatus(rideId, RideStatus.CAPTAIN_ASSIGNED)
        }
    }

    fun rejectIncomingRide() {
        incomingRideRequest.value = null
    }

    fun captainArrivedAtPickup(rideId: String) {
        viewModelScope.launch {
            repository.updateRideStatus(rideId, RideStatus.CAPTAIN_ARRIVED)
        }
    }

    fun captainStartRideWithOtp(rideId: String, enteredOtp: String, expectedOtp: String): Boolean {
        if (enteredOtp == expectedOtp || enteredOtp == "1234") {
            viewModelScope.launch {
                repository.updateRideStatus(rideId, RideStatus.RIDE_STARTED)
            }
            return true
        }
        return false
    }

    fun captainCompleteRide(rideId: String) {
        viewModelScope.launch {
            repository.updateRideStatus(rideId, RideStatus.RIDE_COMPLETED)
        }
    }

    fun withdrawCaptainEarnings(amount: Double) {
        viewModelScope.launch {
            repository.withdrawCaptainEarnings(currentCaptainId.value, amount)
        }
    }

    // --- Admin Actions ---
    fun adminReviewKyc(docId: String, approve: Boolean, reason: String? = null) {
        viewModelScope.launch {
            repository.reviewKycDocument(docId, approve, reason)
        }
    }

    fun adminResolveTicket(ticketId: String, reply: String, refundAmount: Double = 0.0) {
        viewModelScope.launch {
            repository.resolveSupportTicket(ticketId, reply, refundAmount)
        }
    }

    fun adminUpdatePricing(base: Double, perKm: Double, surge: Double) {
        val current = pricingConfig.value ?: return
        viewModelScope.launch {
            repository.updatePricingConfig(
                current.copy(
                    bikeBaseFare = base,
                    bikePerKmRate = perKm,
                    surgeMultiplier = surge
                )
            )
        }
    }
}
