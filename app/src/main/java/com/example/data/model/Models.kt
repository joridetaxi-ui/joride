package com.example.data.model

enum class UserRole {
    CUSTOMER,
    CAPTAIN,
    ADMIN
}

enum class VehicleType(val label: String, val baseRate: Double, val perKmRate: Double, val speedFactor: Float) {
    BIKE_STANDARD("Velo Standard", 25.0, 9.5, 1.0f),
    MOTO_PRIME("Velo Prime (Helmet+Comfort)", 35.0, 12.0, 1.2f),
    ECO_EV("Velo Electric (Zero Emission)", 28.0, 8.5, 1.05f)
}

enum class RideStatus(val label: String) {
    REQUESTED("Requested"),
    SEARCHING_CAPTAIN("Searching Captain"),
    CAPTAIN_ASSIGNED("Captain Assigned"),
    CAPTAIN_ARRIVING("Captain Arriving"),
    CAPTAIN_ARRIVED("Captain Arrived"),
    RIDE_STARTED("Ride in Progress"),
    RIDE_COMPLETED("Completed"),
    CANCELLED_BY_CUSTOMER("Cancelled by Customer"),
    CANCELLED_BY_CAPTAIN("Cancelled by Captain")
}

enum class ParcelStatus(val label: String) {
    PARCEL_REQUESTED("Parcel Booked"),
    SEARCHING_CAPTAIN("Finding Express Courier"),
    CAPTAIN_ASSIGNED("Courier Assigned"),
    CAPTAIN_ARRIVING_PICKUP("Heading to Pickup"),
    CAPTAIN_AT_PICKUP("At Sender Location"),
    PACKAGE_PICKED_UP("Package In Transit"),
    ARRIVED_DESTINATION("Arrived at Destination"),
    DELIVERED("Delivered Successfully"),
    DELIVERY_FAILED("Delivery Failed"),
    RETURNED("Returned to Sender"),
    CANCELLED("Cancelled")
}

enum class PackageCategory(val label: String, val iconName: String) {
    DOCUMENTS("Documents & Letters", "description"),
    ELECTRONICS("Electronics & Gadgets", "devices"),
    FOOD_GROCERY("Food & Essentials", "restaurant"),
    CLOTHES("Apparel & Fashion", "checkroom"),
    PERSONAL_PARCEL("Personal Box / Keys", "inventory_2")
}

enum class PackageSize(val label: String, val maxWeightKg: String, val multiplier: Double) {
    SMALL("Small (Fits in backpack)", "Up to 2 kg", 1.0),
    MEDIUM("Medium (Bag/Box)", "2 - 5 kg", 1.3),
    LARGE("Large (Cargo bag)", "5 - 12 kg", 1.7)
}

enum class PaymentMethod(val label: String) {
    UPI("UPI (GPay / PhonePe / Paytm)"),
    WALLET("VeloPay Wallet"),
    CASH("Cash on Delivery / Ride"),
    CARD("Credit / Debit Card")
}

enum class PaymentStatus {
    PENDING,
    COMPLETED,
    FAILED,
    REFUNDED
}

enum class KycStatus(val label: String) {
    PENDING("Under Verification"),
    APPROVED("Approved & Active"),
    REJECTED("Rejected"),
    SUSPENDED("Suspended")
}

enum class SupportCategory(val label: String) {
    RIDE_ISSUE("Ride Issue"),
    PAYMENT_ISSUE("Billing / Payment"),
    CAPTAIN_FEEDBACK("Captain Behavior"),
    PARCEL_ISSUE("Parcel Delivery Issue"),
    LOST_ITEM("Lost & Found Item"),
    SAFETY_SOS("Safety / Emergency"),
    GENERAL("General Inquiry")
}

enum class SupportStatus {
    OPEN,
    IN_PROGRESS,
    RESOLVED,
    CLOSED
}

enum class SupportPriority {
    NORMAL,
    HIGH,
    EMERGENCY_SOS
}

enum class TransactionType {
    CREDIT,
    DEBIT,
    EARNING,
    WITHDRAWAL,
    REFUND,
    BONUS
}

data class LocationPoint(
    val title: String,
    val address: String,
    val lat: Double,
    val lng: Double
)
