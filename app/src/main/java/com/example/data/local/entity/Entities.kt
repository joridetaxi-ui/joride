package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.data.model.*

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey val id: String,
    val name: String,
    val phone: String,
    val email: String,
    val role: String = UserRole.CUSTOMER.name,
    val rating: Float = 4.9f,
    val walletBalance: Double = 350.0,
    val referralCode: String = "VELO882",
    val emergencyContactName: String = "Father (Praveen)",
    val emergencyContactPhone: String = "+91 98765 43210",
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "captains")
data class CaptainEntity(
    @PrimaryKey val id: String,
    val fullName: String,
    val phone: String,
    val vehicleType: String = VehicleType.BIKE_STANDARD.name,
    val vehicleModel: String = "Hero Splendor Plus",
    val vehicleNumber: String = "KA 01 EK 4920",
    val vehicleColor: String = "Ebony Black",
    val isOnline: Boolean = true,
    val kycStatus: String = KycStatus.APPROVED.name,
    val rating: Float = 4.88f,
    val totalRides: Int = 342,
    val acceptanceRate: Int = 96,
    val todayEarnings: Double = 780.0,
    val totalEarnings: Double = 42800.0,
    val walletBalance: Double = 1450.0,
    val latitude: Double = 12.9716,
    val longitude: Double = 77.5946,
    val licenseNumber: String = "DL-042021008892",
    val rcNumber: String = "RC-KA01-2022-9901",
    val lastUpdated: Long = System.currentTimeMillis()
)

@Entity(tableName = "rides")
data class RideEntity(
    @PrimaryKey val id: String,
    val customerId: String,
    val customerName: String,
    val customerPhone: String,
    val captainId: String? = null,
    val captainName: String? = null,
    val captainPhone: String? = null,
    val captainVehicleNumber: String? = null,
    val captainVehicleModel: String? = null,
    val captainRating: Float = 4.9f,
    val vehicleType: String = VehicleType.BIKE_STANDARD.name,
    val pickupTitle: String,
    val pickupAddress: String,
    val pickupLat: Double,
    val pickupLng: Double,
    val dropTitle: String,
    val dropAddress: String,
    val dropLat: Double,
    val dropLng: Double,
    val distanceKm: Double,
    val durationMinutes: Int,
    val baseFare: Double,
    val distanceFare: Double,
    val surgeMultiplier: Double = 1.0,
    val discountAmount: Double = 0.0,
    val platformFee: Double = 5.0,
    val taxes: Double = 4.0,
    val totalFare: Double,
    val captainEarnings: Double,
    val paymentMethod: String = PaymentMethod.UPI.name,
    val paymentStatus: String = PaymentStatus.PENDING.name,
    val status: String = RideStatus.REQUESTED.name,
    val otp: String = "4921",
    val cancellationReason: String? = null,
    val customerRating: Float? = null,
    val customerReview: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val startedAt: Long? = null,
    val completedAt: Long? = null
)

@Entity(tableName = "parcels")
data class ParcelEntity(
    @PrimaryKey val id: String,
    val customerId: String,
    val senderName: String,
    val senderPhone: String,
    val senderAddress: String,
    val senderLat: Double,
    val senderLng: Double,
    val receiverName: String,
    val receiverPhone: String,
    val receiverAddress: String,
    val receiverLat: Double,
    val receiverLng: Double,
    val packageCategory: String = PackageCategory.DOCUMENTS.name,
    val packageSize: String = PackageSize.SMALL.name,
    val approximateWeightKg: Double = 1.0,
    val specialInstructions: String = "Handle with care",
    val captainId: String? = null,
    val captainName: String? = null,
    val captainPhone: String? = null,
    val captainVehicle: String? = null,
    val distanceKm: Double = 6.4,
    val baseFee: Double = 40.0,
    val distanceFee: Double = 45.0,
    val sizeFee: Double = 0.0,
    val totalFare: Double = 90.0,
    val paymentMethod: String = PaymentMethod.UPI.name,
    val paymentStatus: String = PaymentStatus.PENDING.name,
    val status: String = ParcelStatus.PARCEL_REQUESTED.name,
    val pickupOtp: String = "3192",
    val deliveryOtp: String = "7483",
    val proofOfDeliveryNote: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val pickedUpAt: Long? = null,
    val deliveredAt: Long? = null
)

@Entity(tableName = "wallet_transactions")
data class WalletTransactionEntity(
    @PrimaryKey val id: String,
    val userId: String,
    val type: String, // CREDIT, DEBIT, EARNING, WITHDRAWAL, REFUND, BONUS
    val amount: Double,
    val title: String,
    val description: String,
    val referenceId: String,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "coupons")
data class CouponEntity(
    @PrimaryKey val code: String,
    val discountPercent: Int,
    val maxDiscount: Double,
    val minRideAmount: Double,
    val description: String,
    val applicableType: String = "ALL", // ALL, BIKE_ONLY, PARCEL_ONLY
    val isActive: Boolean = true
)

@Entity(tableName = "support_tickets")
data class SupportTicketEntity(
    @PrimaryKey val id: String,
    val userId: String,
    val userName: String,
    val userPhone: String,
    val userRole: String,
    val rideOrParcelId: String? = null,
    val category: String = SupportCategory.RIDE_ISSUE.name,
    val priority: String = SupportPriority.NORMAL.name,
    val subject: String,
    val description: String,
    val status: String = SupportStatus.OPEN.name,
    val adminReply: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "kyc_documents")
data class KycDocumentEntity(
    @PrimaryKey val id: String,
    val captainId: String,
    val documentType: String, // DRIVING_LICENSE, VEHICLE_RC, INSURANCE, PAN_CARD
    val documentNumber: String,
    val status: String = KycStatus.PENDING.name,
    val rejectionReason: String? = null,
    val uploadedAt: Long = System.currentTimeMillis(),
    val verifiedAt: Long? = null
)

@Entity(tableName = "pricing_configs")
data class PricingConfigEntity(
    @PrimaryKey val cityId: String = "blr",
    val cityName: String = "Bengaluru",
    val bikeBaseFare: Double = 25.0,
    val bikePerKmRate: Double = 9.5,
    val bikePerMinRate: Double = 1.0,
    val parcelBaseFare: Double = 40.0,
    val parcelPerKmRate: Double = 8.0,
    val surgeMultiplier: Double = 1.0,
    val platformFee: Double = 5.0,
    val gstPercent: Double = 5.0,
    val isServiceActive: Boolean = true
)

@Entity(tableName = "saved_places")
data class SavedPlaceEntity(
    @PrimaryKey val id: String,
    val userId: String,
    val title: String,
    val address: String,
    val lat: Double,
    val lng: Double,
    val placeType: String = "HOME" // HOME, WORK, GYM, CAFE, OTHER
)
