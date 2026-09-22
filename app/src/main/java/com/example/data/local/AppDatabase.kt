package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.data.local.dao.*
import com.example.data.local.entity.*

@Database(
    entities = [
        UserEntity::class,
        CaptainEntity::class,
        RideEntity::class,
        ParcelEntity::class,
        WalletTransactionEntity::class,
        CouponEntity::class,
        SupportTicketEntity::class,
        KycDocumentEntity::class,
        PricingConfigEntity::class,
        SavedPlaceEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun captainDao(): CaptainDao
    abstract fun rideDao(): RideDao
    abstract fun parcelDao(): ParcelDao
    abstract fun walletTransactionDao(): WalletTransactionDao
    abstract fun couponDao(): CouponDao
    abstract fun supportTicketDao(): SupportTicketDao
    abstract fun kycDocumentDao(): KycDocumentDao
    abstract fun pricingConfigDao(): PricingConfigDao
    abstract fun savedPlaceDao(): SavedPlaceDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "velogo_database"
                ).fallbackToDestructiveMigration().build()
                INSTANCE = instance
                instance
            }
        }
    }
}
