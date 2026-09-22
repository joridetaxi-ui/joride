package com.example.domain.engine

import com.example.data.local.entity.CaptainEntity
import com.example.data.model.KycStatus

object MatchingEngine {
    fun findBestCaptain(
        pickupLat: Double,
        pickupLng: Double,
        availableCaptains: List<CaptainEntity>
    ): CaptainEntity? {
        val eligible = availableCaptains.filter {
            it.isOnline && it.kycStatus == KycStatus.APPROVED.name
        }
        if (eligible.isEmpty()) return null

        // Score based on distance (closer is higher score), rating, acceptance rate
        return eligible.minByOrNull { captain ->
            val dist = IndianLocations.calculateDistanceKm(pickupLat, pickupLng, captain.latitude, captain.longitude)
            val ratingPenalty = (5.0 - captain.rating) * 2.0
            val acceptancePenalty = (100 - captain.acceptanceRate) * 0.05
            dist + ratingPenalty + acceptancePenalty
        }
    }
}
