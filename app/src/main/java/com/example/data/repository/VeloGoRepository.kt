package com.example.data.repository

import com.example.data.local.AppDatabase
import com.example.data.local.entity.*
import com.example.data.model.*
import com.example.domain.engine.FareCalculationResult
import com.example.domain.engine.FareEngine
import com.example.domain.engine.IndianLocations
import com.example.domain.engine.MatchingEngine
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import java.util.UUID

class VeloGoRepository(
    private val database: AppDatabase,
    private val externalScope: CoroutineScope = CoroutineScope(Dispatchers.IO)
) {
    private val userDao = database.userDao()
    private val captainDao = database.captainDao()
    private val rideDao = database.rideDao()
    private val parcelDao = database.parcelDao()
    private val walletDao = database.walletTransactionDao()
    private val couponDao = database.couponDao()
    private val supportDao = database.supportTicketDao()
    private val kycDao = database.kycDocumentDao()
    private val pricingDao = database.pricingConfigDao()
    private val savedPlaceDao = database.savedPlaceDao()

    val currentUser: Flow<UserEntity?> = userDao.getUserById("current_user_id")
    val allCaptains: Flow<List<CaptainEntity>> = captainDao.getAllCaptains()
    val onlineCaptains: Flow<List<CaptainEntity>> = captainDao.getOnlineApprovedCaptains()
    val allRides: Flow<List<RideEntity>> = rideDao.getAllRides()
    val activeRide: Flow<RideEntity?> = rideDao.getActiveRide()
    val allParcels: Flow<List<ParcelEntity>> = parcelDao.getAllParcels()
    val activeParcel: Flow<ParcelEntity?> = parcelDao.getActiveParcel()
    val walletTransactions: Flow<List<WalletTransactionEntity>> = walletDao.getTransactionsForUser("current_user_id")
    val coupons: Flow<List<CouponEntity>> = couponDao.getActiveCoupons()
    val supportTickets: Flow<List<SupportTicketEntity>> = supportDao.getAllTickets()
    val pendingKycDocs: Flow<List<KycDocumentEntity>> = kycDao.getPendingDocuments()
    val pricingConfig: Flow<PricingConfigEntity?> = pricingDao.getPricingConfig("blr")
    val savedPlaces: Flow<List<SavedPlaceEntity>> = savedPlaceDao.getSavedPlaces("current_user_id")

    init {
        externalScope.launch {
            seedInitialDataIfNeeded()
        }
    }

    private suspend fun seedInitialDataIfNeeded() {
        val user = userDao.getUserById("current_user_id").firstOrNull()
        if (user == null) {
            // Seed Customer Profile
            userDao.insertUser(
                UserEntity(
                    id = "current_user_id",
                    name = "Aarav Sharma",
                    phone = "+91 98450 12345",
                    email = "aarav.sharma@velogo.in",
                    walletBalance = 420.0,
                    referralCode = "AARAV50"
                )
            )

            // Seed Captains in Bengaluru
            val seedCaptains = listOf(
                CaptainEntity(
                    id = "cpt_1",
                    fullName = "Ramesh Kumar",
                    phone = "+91 97412 88201",
                    vehicleType = VehicleType.BIKE_STANDARD.name,
                    vehicleModel = "Hero Splendor Plus (Black)",
                    vehicleNumber = "KA 05 MN 3829",
                    vehicleColor = "Black & Silver",
                    isOnline = true,
                    kycStatus = KycStatus.APPROVED.name,
                    rating = 4.92f,
                    totalRides = 482,
                    acceptanceRate = 98,
                    todayEarnings = 940.0,
                    totalEarnings = 56200.0,
                    walletBalance = 2100.0,
                    latitude = 12.9740,
                    longitude = 77.6350
                ),
                CaptainEntity(
                    id = "cpt_2",
                    fullName = "Venkatesh Rao",
                    phone = "+91 99001 44521",
                    vehicleType = VehicleType.MOTO_PRIME.name,
                    vehicleModel = "Honda CB Shine 125 (Blue)",
                    vehicleNumber = "KA 03 HQ 7192",
                    vehicleColor = "Imperial Blue",
                    isOnline = true,
                    kycStatus = KycStatus.APPROVED.name,
                    rating = 4.87f,
                    totalRides = 319,
                    acceptanceRate = 95,
                    todayEarnings = 680.0,
                    totalEarnings = 38400.0,
                    walletBalance = 1450.0,
                    latitude = 12.9380,
                    longitude = 77.6200
                ),
                CaptainEntity(
                    id = "cpt_3",
                    fullName = "Syed Imran",
                    phone = "+91 98862 10933",
                    vehicleType = VehicleType.ECO_EV.name,
                    vehicleModel = "Ather 450X Gen 3 (White)",
                    vehicleNumber = "KA 04 EV 1024",
                    vehicleColor = "Space Grey & Mint",
                    isOnline = true,
                    kycStatus = KycStatus.APPROVED.name,
                    rating = 4.96f,
                    totalRides = 612,
                    acceptanceRate = 99,
                    todayEarnings = 1240.0,
                    totalEarnings = 72100.0,
                    walletBalance = 3800.0,
                    latitude = 12.9760,
                    longitude = 77.6080
                ),
                CaptainEntity(
                    id = "cpt_4",
                    fullName = "Manjunath Gowda",
                    phone = "+91 94481 77209",
                    vehicleType = VehicleType.BIKE_STANDARD.name,
                    vehicleModel = "Bajaj Pulsar 150",
                    vehicleNumber = "KA 51 EA 9041",
                    vehicleColor = "Laser Black",
                    isOnline = true,
                    kycStatus = KycStatus.PENDING.name, // For Admin KYC demo!
                    rating = 4.75f,
                    totalRides = 14,
                    acceptanceRate = 92,
                    todayEarnings = 0.0,
                    totalEarnings = 1200.0,
                    walletBalance = 250.0,
                    latitude = 12.9150,
                    longitude = 77.6400
                ),
                CaptainEntity(
                    id = "cpt_5",
                    fullName = "Deepak Verma",
                    phone = "+91 91080 33490",
                    vehicleType = VehicleType.BIKE_STANDARD.name,
                    vehicleModel = "TVS Raider 125",
                    vehicleNumber = "KA 01 TR 4488",
                    vehicleColor = "Fiery Yellow",
                    isOnline = true,
                    kycStatus = KycStatus.APPROVED.name,
                    rating = 4.89f,
                    totalRides = 210,
                    acceptanceRate = 97,
                    todayEarnings = 820.0,
                    totalEarnings = 29500.0,
                    walletBalance = 1600.0,
                    latitude = 12.9820,
                    longitude = 77.7300
                )
            )
            captainDao.insertCaptains(seedCaptains)

            // Seed KYC Documents for Admin verification
            kycDao.insertDocuments(
                listOf(
                    KycDocumentEntity(
                        id = "kyc_doc_1",
                        captainId = "cpt_4",
                        documentType = "DRIVING_LICENSE",
                        documentNumber = "DL-052023004812",
                        status = KycStatus.PENDING.name
                    ),
                    KycDocumentEntity(
                        id = "kyc_doc_2",
                        captainId = "cpt_4",
                        documentType = "VEHICLE_RC",
                        documentNumber = "RC-KA51-2023-8841",
                        status = KycStatus.PENDING.name
                    ),
                    KycDocumentEntity(
                        id = "kyc_doc_3",
                        captainId = "cpt_4",
                        documentType = "VEHICLE_INSURANCE",
                        documentNumber = "INS-ICICI-99201948",
                        status = KycStatus.PENDING.name
                    )
                )
            )

            // Seed Coupons
            couponDao.insertCoupons(
                listOf(
                    CouponEntity(
                        code = "VELOFIRST",
                        discountPercent = 50,
                        maxDiscount = 50.0,
                        minRideAmount = 40.0,
                        description = "50% OFF up to ₹50 on your first 3 bike rides!"
                    ),
                    CouponEntity(
                        code = "BIKE30",
                        discountPercent = 30,
                        maxDiscount = 35.0,
                        minRideAmount = 50.0,
                        description = "30% OFF up to ₹35 on Prime & EV bike taxi"
                    ),
                    CouponEntity(
                        code = "PARCEL20",
                        discountPercent = 25,
                        maxDiscount = 40.0,
                        minRideAmount = 60.0,
                        description = "25% OFF on same-day city parcel deliveries",
                        applicableType = "PARCEL_ONLY"
                    ),
                    CouponEntity(
                        code = "RAINY40",
                        discountPercent = 40,
                        maxDiscount = 60.0,
                        minRideAmount = 80.0,
                        description = "Monsoon Special: ₹40 off on rides during peak rain hours"
                    )
                )
            )

            // Seed Pricing Configs for Indian Cities
            pricingDao.insertPricingConfigs(
                listOf(
                    PricingConfigEntity(
                        cityId = "blr",
                        cityName = "Bengaluru",
                        bikeBaseFare = 25.0,
                        bikePerKmRate = 9.5,
                        bikePerMinRate = 1.0,
                        parcelBaseFare = 40.0,
                        parcelPerKmRate = 8.0,
                        surgeMultiplier = 1.0
                    ),
                    PricingConfigEntity(
                        cityId = "hyd",
                        cityName = "Hyderabad",
                        bikeBaseFare = 22.0,
                        bikePerKmRate = 8.5,
                        bikePerMinRate = 0.8,
                        parcelBaseFare = 35.0,
                        parcelPerKmRate = 7.5,
                        surgeMultiplier = 1.0
                    ),
                    PricingConfigEntity(
                        cityId = "del",
                        cityName = "Delhi NCR",
                        bikeBaseFare = 25.0,
                        bikePerKmRate = 9.0,
                        bikePerMinRate = 1.0,
                        parcelBaseFare = 40.0,
                        parcelPerKmRate = 8.0,
                        surgeMultiplier = 1.15
                    )
                )
            )

            // Seed Saved Places
            savedPlaceDao.insertSavedPlaces(
                listOf(
                    SavedPlaceEntity(
                        id = "sp_1",
                        userId = "current_user_id",
                        title = "Home",
                        address = "Apt 402, Green Glen Layout, Bellandur, Bengaluru",
                        lat = 12.9298,
                        lng = 77.6748,
                        placeType = "HOME"
                    ),
                    SavedPlaceEntity(
                        id = "sp_2",
                        userId = "current_user_id",
                        title = "Work / Office",
                        address = "Embassy TechVillage, Outer Ring Road, Bengaluru",
                        lat = 12.9345,
                        lng = 77.6931,
                        placeType = "WORK"
                    ),
                    SavedPlaceEntity(
                        id = "sp_3",
                        userId = "current_user_id",
                        title = "Cult.fit Gym",
                        address = "100ft Road, Indiranagar, Bengaluru",
                        lat = 12.9784,
                        lng = 77.6408,
                        placeType = "GYM"
                    )
                )
            )

            // Seed Sample Wallet Ledger
            walletDao.insertTransaction(
                WalletTransactionEntity(
                    id = "tx_welcome",
                    userId = "current_user_id",
                    type = TransactionType.BONUS.name,
                    amount = 100.0,
                    title = "Welcome Signup Bonus",
                    description = "Added to VeloPay Wallet",
                    referenceId = "SIGNUP_100"
                )
            )
            walletDao.insertTransaction(
                WalletTransactionEntity(
                    id = "tx_upi_add",
                    userId = "current_user_id",
                    type = TransactionType.CREDIT.name,
                    amount = 320.0,
                    title = "Money Added via UPI",
                    description = "GPay Ref #UPI8829104",
                    referenceId = "UPI_ADD_320"
                )
            )
        }
    }

    // --- Customer Ride Actions ---
    suspend fun bookBikeRide(
        pickup: LocationPoint,
        drop: LocationPoint,
        vehicleType: VehicleType,
        paymentMethod: PaymentMethod,
        appliedCoupon: CouponEntity? = null
    ): RideEntity {
        val distanceKm = IndianLocations.calculateDistanceKm(pickup.lat, pickup.lng, drop.lat, drop.lng)
        val durationMins = IndianLocations.estimateDurationMinutes(distanceKm)
        val config = pricingDao.getPricingConfig("blr").firstOrNull()

        val fareResult = FareEngine.calculateRideFare(
            distanceKm = distanceKm,
            durationMinutes = durationMins,
            vehicleType = vehicleType,
            config = config,
            couponDiscountPercent = appliedCoupon?.discountPercent ?: 0,
            couponMaxDiscount = appliedCoupon?.maxDiscount ?: 0.0
        )

        val rideId = "RIDE-" + (1000..9999).random()
        val randomOtp = (1000..9999).random().toString()
        val user = userDao.getUserById("current_user_id").firstOrNull()

        val ride = RideEntity(
            id = rideId,
            customerId = "current_user_id",
            customerName = user?.name ?: "Customer",
            customerPhone = user?.phone ?: "+91 98450 12345",
            vehicleType = vehicleType.name,
            pickupTitle = pickup.title,
            pickupAddress = pickup.address,
            pickupLat = pickup.lat,
            pickupLng = pickup.lng,
            dropTitle = drop.title,
            dropAddress = drop.address,
            dropLat = drop.lat,
            dropLng = drop.lng,
            distanceKm = distanceKm,
            durationMinutes = durationMins,
            baseFare = fareResult.baseFare,
            distanceFare = fareResult.distanceFare,
            surgeMultiplier = fareResult.surgeMultiplier,
            discountAmount = fareResult.discountAmount,
            platformFee = fareResult.platformFee,
            taxes = fareResult.taxes,
            totalFare = fareResult.totalFare,
            captainEarnings = fareResult.captainEarnings,
            paymentMethod = paymentMethod.name,
            status = RideStatus.SEARCHING_CAPTAIN.name,
            otp = randomOtp
        )
        rideDao.insertRide(ride)

        // Find best nearby online captain
        val captains = captainDao.getOnlineApprovedCaptains().first()
        val matchedCaptain = MatchingEngine.findBestCaptain(pickup.lat, pickup.lng, captains)

        if (matchedCaptain != null) {
            val assignedRide = ride.copy(
                captainId = matchedCaptain.id,
                captainName = matchedCaptain.fullName,
                captainPhone = matchedCaptain.phone,
                captainVehicleNumber = matchedCaptain.vehicleNumber,
                captainVehicleModel = matchedCaptain.vehicleModel,
                captainRating = matchedCaptain.rating,
                status = RideStatus.CAPTAIN_ASSIGNED.name
            )
            rideDao.updateRide(assignedRide)
            return assignedRide
        }

        return ride
    }

    suspend fun cancelRide(rideId: String, reason: String = "Changed my mind") {
        val ride = rideDao.getRideById(rideId).firstOrNull() ?: return
        rideDao.updateRide(
            ride.copy(
                status = RideStatus.CANCELLED_BY_CUSTOMER.name,
                cancellationReason = reason
            )
        )
    }

    suspend fun rateCaptain(rideId: String, rating: Float, review: String?) {
        rideDao.rateRide(rideId, rating, review)
    }

    // --- Parcel Actions ---
    suspend fun bookParcel(
        senderName: String,
        senderPhone: String,
        senderAddress: String,
        senderLat: Double,
        senderLng: Double,
        receiverName: String,
        receiverPhone: String,
        receiverAddress: String,
        receiverLat: Double,
        receiverLng: Double,
        category: PackageCategory,
        size: PackageSize,
        weightKg: Double,
        instructions: String,
        paymentMethod: PaymentMethod,
        appliedCoupon: CouponEntity? = null
    ): ParcelEntity {
        val distanceKm = IndianLocations.calculateDistanceKm(senderLat, senderLng, receiverLat, receiverLng)
        val config = pricingDao.getPricingConfig("blr").firstOrNull()

        val fareResult = FareEngine.calculateParcelFare(
            distanceKm = distanceKm,
            size = size,
            weightKg = weightKg,
            config = config,
            couponDiscountPercent = appliedCoupon?.discountPercent ?: 0,
            couponMaxDiscount = appliedCoupon?.maxDiscount ?: 0.0
        )

        val parcelId = "EXP-" + (1000..9999).random()
        val pickupOtp = (1000..9999).random().toString()
        val deliveryOtp = (1000..9999).random().toString()

        val parcel = ParcelEntity(
            id = parcelId,
            customerId = "current_user_id",
            senderName = senderName,
            senderPhone = senderPhone,
            senderAddress = senderAddress,
            senderLat = senderLat,
            senderLng = senderLng,
            receiverName = receiverName,
            receiverPhone = receiverPhone,
            receiverAddress = receiverAddress,
            receiverLat = receiverLat,
            receiverLng = receiverLng,
            packageCategory = category.name,
            packageSize = size.name,
            approximateWeightKg = weightKg,
            specialInstructions = instructions,
            distanceKm = distanceKm,
            baseFee = fareResult.baseFare,
            distanceFee = fareResult.distanceFare,
            sizeFee = 0.0,
            totalFare = fareResult.totalFare,
            paymentMethod = paymentMethod.name,
            status = ParcelStatus.SEARCHING_CAPTAIN.name,
            pickupOtp = pickupOtp,
            deliveryOtp = deliveryOtp
        )
        parcelDao.insertParcel(parcel)

        val captains = captainDao.getOnlineApprovedCaptains().first()
        val matched = MatchingEngine.findBestCaptain(senderLat, senderLng, captains)
        if (matched != null) {
            val assigned = parcel.copy(
                captainId = matched.id,
                captainName = matched.fullName,
                captainPhone = matched.phone,
                captainVehicle = matched.vehicleNumber + " (" + matched.vehicleModel + ")",
                status = ParcelStatus.CAPTAIN_ASSIGNED.name
            )
            parcelDao.updateParcel(assigned)
            return assigned
        }

        return parcel
    }

    suspend fun updateRideStatus(rideId: String, newStatus: RideStatus) {
        val ride = rideDao.getRideById(rideId).firstOrNull() ?: return
        val updated = when (newStatus) {
            RideStatus.RIDE_STARTED -> ride.copy(status = newStatus.name, startedAt = System.currentTimeMillis())
            RideStatus.RIDE_COMPLETED -> {
                // Settle payment & captain earnings
                if (ride.captainId != null) {
                    captainDao.addCaptainEarnings(ride.captainId, ride.captainEarnings)
                }
                if (ride.paymentMethod == PaymentMethod.WALLET.name) {
                    userDao.updateWalletBalance(ride.customerId, -ride.totalFare)
                    walletDao.insertTransaction(
                        WalletTransactionEntity(
                            id = "tx_" + UUID.randomUUID().toString().take(8),
                            userId = ride.customerId,
                            type = TransactionType.DEBIT.name,
                            amount = ride.totalFare,
                            title = "Ride Payment - " + ride.id,
                            description = "Debited for bike ride to ${ride.dropTitle}",
                            referenceId = ride.id
                        )
                    )
                }
                ride.copy(
                    status = newStatus.name,
                    paymentStatus = PaymentStatus.COMPLETED.name,
                    completedAt = System.currentTimeMillis()
                )
            }
            else -> ride.copy(status = newStatus.name)
        }
        rideDao.updateRide(updated)
    }

    suspend fun updateParcelStatus(parcelId: String, newStatus: ParcelStatus) {
        val parcel = parcelDao.getParcelById(parcelId).firstOrNull() ?: return
        val updated = when (newStatus) {
            ParcelStatus.PACKAGE_PICKED_UP -> parcel.copy(status = newStatus.name, pickedUpAt = System.currentTimeMillis())
            ParcelStatus.DELIVERED -> {
                if (parcel.captainId != null) {
                    captainDao.addCaptainEarnings(parcel.captainId, parcel.totalFare * 0.8)
                }
                parcel.copy(status = newStatus.name, paymentStatus = PaymentStatus.COMPLETED.name, deliveredAt = System.currentTimeMillis())
            }
            else -> parcel.copy(status = newStatus.name)
        }
        parcelDao.updateParcel(updated)
    }

    // --- Wallet Actions ---
    suspend fun addMoneyToWallet(amount: Double) {
        userDao.updateWalletBalance("current_user_id", amount)
        walletDao.insertTransaction(
            WalletTransactionEntity(
                id = "tx_" + UUID.randomUUID().toString().take(8),
                userId = "current_user_id",
                type = TransactionType.CREDIT.name,
                amount = amount,
                title = "Added to VeloPay Wallet",
                description = "Instant UPI Top-up",
                referenceId = "UPI_TOPUP_" + System.currentTimeMillis()
            )
        )
    }

    // --- Support & Safety Actions ---
    suspend fun createSupportTicket(
        category: SupportCategory,
        priority: SupportPriority,
        subject: String,
        description: String,
        rideOrParcelId: String? = null
    ): SupportTicketEntity {
        val user = userDao.getUserById("current_user_id").firstOrNull()
        val ticket = SupportTicketEntity(
            id = "TCK-" + (10000..99999).random(),
            userId = "current_user_id",
            userName = user?.name ?: "Aarav Sharma",
            userPhone = user?.phone ?: "+91 98450 12345",
            userRole = UserRole.CUSTOMER.name,
            rideOrParcelId = rideOrParcelId,
            category = category.name,
            priority = priority.name,
            subject = subject,
            description = description,
            status = SupportStatus.OPEN.name
        )
        supportDao.insertTicket(ticket)
        return ticket
    }

    // --- Captain Actions ---
    suspend fun setCaptainOnline(captainId: String, isOnline: Boolean) {
        captainDao.setOnlineStatus(captainId, isOnline)
    }

    suspend fun withdrawCaptainEarnings(captainId: String, amount: Double) {
        val captain = captainDao.getCaptainById(captainId).firstOrNull() ?: return
        if (captain.walletBalance >= amount) {
            captainDao.insertCaptain(
                captain.copy(walletBalance = captain.walletBalance - amount)
            )
        }
    }

    // --- Admin Actions ---
    suspend fun reviewKycDocument(docId: String, approve: Boolean, reason: String? = null) {
        val status = if (approve) KycStatus.APPROVED.name else KycStatus.REJECTED.name
        val doc = kycDao.getPendingDocuments().first().find { it.id == docId } ?: return
        kycDao.updateDocument(
            doc.copy(
                status = status,
                rejectionReason = if (!approve) reason else null,
                verifiedAt = System.currentTimeMillis()
            )
        )

        if (approve) {
            // Also approve captain status
            captainDao.updateKycStatus(doc.captainId, KycStatus.APPROVED.name)
        }
    }

    suspend fun resolveSupportTicket(ticketId: String, resolutionReply: String, refundAmount: Double = 0.0) {
        val ticket = supportDao.getAllTickets().first().find { it.id == ticketId } ?: return
        supportDao.resolveTicket(ticketId, SupportStatus.RESOLVED.name, resolutionReply)

        if (refundAmount > 0.0) {
            userDao.updateWalletBalance(ticket.userId, refundAmount)
            walletDao.insertTransaction(
                WalletTransactionEntity(
                    id = "ref_" + UUID.randomUUID().toString().take(8),
                    userId = ticket.userId,
                    type = TransactionType.REFUND.name,
                    amount = refundAmount,
                    title = "Support Ticket Refund - ${ticket.id}",
                    description = "Refund approved by support team: $resolutionReply",
                    referenceId = ticket.id
                )
            )
        }
    }

    suspend fun updatePricingConfig(config: PricingConfigEntity) {
        pricingDao.updatePricingConfig(config)
    }
}
