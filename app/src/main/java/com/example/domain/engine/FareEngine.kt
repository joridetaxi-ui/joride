package com.example.domain.engine

import com.example.data.local.entity.PricingConfigEntity
import com.example.data.model.PackageSize
import com.example.data.model.VehicleType
import kotlin.math.max
import kotlin.math.roundToInt

data class FareCalculationResult(
    val baseFare: Double,
    val distanceFare: Double,
    val timeFare: Double,
    val surgeMultiplier: Double,
    val surgeAmount: Double,
    val platformFee: Double,
    val taxes: Double,
    val discountAmount: Double,
    val totalFare: Double,
    val captainEarnings: Double
)

object FareEngine {
    fun calculateRideFare(
        distanceKm: Double,
        durationMinutes: Int,
        vehicleType: VehicleType,
        config: PricingConfigEntity?,
        couponDiscountPercent: Int = 0,
        couponMaxDiscount: Double = 0.0
    ): FareCalculationResult {
        val base = config?.bikeBaseFare ?: vehicleType.baseRate
        val perKm = config?.bikePerKmRate ?: vehicleType.perKmRate
        val perMin = config?.bikePerMinRate ?: 1.0
        val surge = config?.surgeMultiplier ?: 1.0

        val distFare = distanceKm * perKm
        val timeFare = durationMinutes * perMin
        val subtotal = (base + distFare + timeFare)
        val surgeAmount = subtotal * (surge - 1.0)
        val withSurge = subtotal + surgeAmount

        val rawDiscount = if (couponDiscountPercent > 0) {
            val calc = withSurge * (couponDiscountPercent / 100.0)
            if (couponMaxDiscount > 0) calc.coerceAtMost(couponMaxDiscount) else calc
        } else 0.0

        val platformFee = config?.platformFee ?: 5.0
        val gst = (withSurge - rawDiscount + platformFee) * ((config?.gstPercent ?: 5.0) / 100.0)
        val finalFare = max(base, withSurge - rawDiscount + platformFee + gst).roundTo2Decimals()
        
        // Captain gets 80% of net ride fare (Platform takes 20% commission)
        val captainEarnings = ((withSurge - rawDiscount) * 0.80).coerceAtLeast(base * 0.75).roundTo2Decimals()

        return FareCalculationResult(
            baseFare = base.roundTo2Decimals(),
            distanceFare = distFare.roundTo2Decimals(),
            timeFare = timeFare.roundTo2Decimals(),
            surgeMultiplier = surge,
            surgeAmount = surgeAmount.roundTo2Decimals(),
            platformFee = platformFee,
            taxes = gst.roundTo2Decimals(),
            discountAmount = rawDiscount.roundTo2Decimals(),
            totalFare = finalFare,
            captainEarnings = captainEarnings
        )
    }

    fun calculateParcelFare(
        distanceKm: Double,
        size: PackageSize,
        weightKg: Double,
        config: PricingConfigEntity?,
        couponDiscountPercent: Int = 0,
        couponMaxDiscount: Double = 0.0
    ): FareCalculationResult {
        val base = (config?.parcelBaseFare ?: 40.0) * size.multiplier
        val perKm = config?.parcelPerKmRate ?: 8.5
        val weightSurcharge = if (weightKg > 3.0) (weightKg - 3.0) * 10.0 else 0.0

        val distFare = distanceKm * perKm
        val subtotal = base + distFare + weightSurcharge
        val rawDiscount = if (couponDiscountPercent > 0) {
            val calc = subtotal * (couponDiscountPercent / 100.0)
            if (couponMaxDiscount > 0) calc.coerceAtMost(couponMaxDiscount) else calc
        } else 0.0

        val platformFee = 6.0
        val gst = (subtotal - rawDiscount + platformFee) * 0.05
        val finalFare = max(base, subtotal - rawDiscount + platformFee + gst).roundTo2Decimals()
        val captainEarnings = ((subtotal - rawDiscount) * 0.82).roundTo2Decimals()

        return FareCalculationResult(
            baseFare = base.roundTo2Decimals(),
            distanceFare = distFare.roundTo2Decimals(),
            timeFare = weightSurcharge.roundTo2Decimals(),
            surgeMultiplier = 1.0,
            surgeAmount = 0.0,
            platformFee = platformFee,
            taxes = gst.roundTo2Decimals(),
            discountAmount = rawDiscount.roundTo2Decimals(),
            totalFare = finalFare,
            captainEarnings = captainEarnings
        )
    }

    private fun Double.roundTo2Decimals(): Double {
        return (this * 100.0).roundToInt() / 100.0
    }
}
