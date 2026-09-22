package com.example.data.local.dao

import androidx.room.*
import com.example.data.local.entity.*
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {
    @Query("SELECT * FROM users WHERE id = :id")
    fun getUserById(id: String): Flow<UserEntity?>

    @Query("SELECT * FROM users")
    fun getAllUsers(): Flow<List<UserEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: UserEntity)

    @Update
    suspend fun updateUser(user: UserEntity)

    @Query("UPDATE users SET walletBalance = walletBalance + :amount WHERE id = :userId")
    suspend fun updateWalletBalance(userId: String, amount: Double)
}

@Dao
interface CaptainDao {
    @Query("SELECT * FROM captains WHERE id = :id")
    fun getCaptainById(id: String): Flow<CaptainEntity?>

    @Query("SELECT * FROM captains")
    fun getAllCaptains(): Flow<List<CaptainEntity>>

    @Query("SELECT * FROM captains WHERE isOnline = 1 AND kycStatus = 'APPROVED'")
    fun getOnlineApprovedCaptains(): Flow<List<CaptainEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCaptain(captain: CaptainEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCaptains(captains: List<CaptainEntity>)

    @Update
    suspend fun updateCaptain(captain: CaptainEntity)

    @Query("UPDATE captains SET isOnline = :isOnline WHERE id = :id")
    suspend fun setOnlineStatus(id: String, isOnline: Boolean)

    @Query("UPDATE captains SET kycStatus = :status WHERE id = :id")
    suspend fun updateKycStatus(id: String, status: String)

    @Query("UPDATE captains SET latitude = :lat, longitude = :lng, lastUpdated = :time WHERE id = :id")
    suspend fun updateLocation(id: String, lat: Double, lng: Double, time: Long = System.currentTimeMillis())

    @Query("UPDATE captains SET todayEarnings = todayEarnings + :amount, totalEarnings = totalEarnings + :amount, walletBalance = walletBalance + :amount, totalRides = totalRides + 1 WHERE id = :id")
    suspend fun addCaptainEarnings(id: String, amount: Double)
}

@Dao
interface RideDao {
    @Query("SELECT * FROM rides ORDER BY createdAt DESC")
    fun getAllRides(): Flow<List<RideEntity>>

    @Query("SELECT * FROM rides WHERE customerId = :customerId ORDER BY createdAt DESC")
    fun getCustomerRides(customerId: String): Flow<List<RideEntity>>

    @Query("SELECT * FROM rides WHERE captainId = :captainId ORDER BY createdAt DESC")
    fun getCaptainRides(captainId: String): Flow<List<RideEntity>>

    @Query("SELECT * FROM rides WHERE id = :id")
    fun getRideById(id: String): Flow<RideEntity?>

    @Query("SELECT * FROM rides WHERE status NOT IN ('RIDE_COMPLETED', 'CANCELLED_BY_CUSTOMER', 'CANCELLED_BY_CAPTAIN') ORDER BY createdAt DESC LIMIT 1")
    fun getActiveRide(): Flow<RideEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRide(ride: RideEntity)

    @Update
    suspend fun updateRide(ride: RideEntity)

    @Query("UPDATE rides SET status = :status WHERE id = :id")
    suspend fun updateRideStatus(id: String, status: String)

    @Query("UPDATE rides SET customerRating = :rating, customerReview = :review WHERE id = :id")
    suspend fun rateRide(id: String, rating: Float, review: String?)
}

@Dao
interface ParcelDao {
    @Query("SELECT * FROM parcels ORDER BY createdAt DESC")
    fun getAllParcels(): Flow<List<ParcelEntity>>

    @Query("SELECT * FROM parcels WHERE customerId = :customerId ORDER BY createdAt DESC")
    fun getCustomerParcels(customerId: String): Flow<List<ParcelEntity>>

    @Query("SELECT * FROM parcels WHERE id = :id")
    fun getParcelById(id: String): Flow<ParcelEntity?>

    @Query("SELECT * FROM parcels WHERE status NOT IN ('DELIVERED', 'DELIVERY_FAILED', 'RETURNED', 'CANCELLED') ORDER BY createdAt DESC LIMIT 1")
    fun getActiveParcel(): Flow<ParcelEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertParcel(parcel: ParcelEntity)

    @Update
    suspend fun updateParcel(parcel: ParcelEntity)

    @Query("UPDATE parcels SET status = :status WHERE id = :id")
    suspend fun updateParcelStatus(id: String, status: String)
}

@Dao
interface WalletTransactionDao {
    @Query("SELECT * FROM wallet_transactions WHERE userId = :userId ORDER BY timestamp DESC")
    fun getTransactionsForUser(userId: String): Flow<List<WalletTransactionEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransaction(transaction: WalletTransactionEntity)
}

@Dao
interface CouponDao {
    @Query("SELECT * FROM coupons WHERE isActive = 1")
    fun getActiveCoupons(): Flow<List<CouponEntity>>

    @Query("SELECT * FROM coupons WHERE code = :code LIMIT 1")
    suspend fun getCouponByCode(code: String): CouponEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCoupon(coupon: CouponEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCoupons(coupons: List<CouponEntity>)
}

@Dao
interface SupportTicketDao {
    @Query("SELECT * FROM support_tickets ORDER BY createdAt DESC")
    fun getAllTickets(): Flow<List<SupportTicketEntity>>

    @Query("SELECT * FROM support_tickets WHERE userId = :userId ORDER BY createdAt DESC")
    fun getUserTickets(userId: String): Flow<List<SupportTicketEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTicket(ticket: SupportTicketEntity)

    @Update
    suspend fun updateTicket(ticket: SupportTicketEntity)

    @Query("UPDATE support_tickets SET status = :status, adminReply = :reply, updatedAt = :time WHERE id = :id")
    suspend fun resolveTicket(id: String, status: String, reply: String, time: Long = System.currentTimeMillis())
}

@Dao
interface KycDocumentDao {
    @Query("SELECT * FROM kyc_documents WHERE captainId = :captainId")
    fun getDocumentsByCaptain(captainId: String): Flow<List<KycDocumentEntity>>

    @Query("SELECT * FROM kyc_documents WHERE status = 'PENDING'")
    fun getPendingDocuments(): Flow<List<KycDocumentEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDocument(doc: KycDocumentEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDocuments(docs: List<KycDocumentEntity>)

    @Update
    suspend fun updateDocument(doc: KycDocumentEntity)
}

@Dao
interface PricingConfigDao {
    @Query("SELECT * FROM pricing_configs WHERE cityId = :cityId LIMIT 1")
    fun getPricingConfig(cityId: String = "blr"): Flow<PricingConfigEntity?>

    @Query("SELECT * FROM pricing_configs")
    fun getAllCityConfigs(): Flow<List<PricingConfigEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPricingConfig(config: PricingConfigEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPricingConfigs(configs: List<PricingConfigEntity>)

    @Update
    suspend fun updatePricingConfig(config: PricingConfigEntity)
}

@Dao
interface SavedPlaceDao {
    @Query("SELECT * FROM saved_places WHERE userId = :userId")
    fun getSavedPlaces(userId: String): Flow<List<SavedPlaceEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSavedPlace(place: SavedPlaceEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSavedPlaces(places: List<SavedPlaceEntity>)

    @Delete
    suspend fun deleteSavedPlace(place: SavedPlaceEntity)
}
