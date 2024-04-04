package com.example.diplov_v1

import android.content.Context
import androidx.room.ColumnInfo
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Room
import androidx.room.RoomDatabase

@Entity(tableName = "Test")
data class TestEntity(
    @PrimaryKey(autoGenerate = true)
    var id: Int? = null,
    @ColumnInfo(name = "test")
    var test: String
)

@Entity(tableName = "Products")
data class ProductsEntity(
    @PrimaryKey(autoGenerate = true)
    var id: Int? = null,
    @ColumnInfo(name = "flag")
    var flag: Int,
    @ColumnInfo(name = "count")
    var count: Int,
    @ColumnInfo(name = "productName")
    var productName: String,
    @ColumnInfo(name = "proteinValue")
    var proteinValue: Double,
    @ColumnInfo(name = "fatValue")
    var fatValue: Double,
    @ColumnInfo(name = "carbohydrateValue")
    var carbohydrateValue: Double,
    @ColumnInfo(name = "kcalValue")
    var kcalValue: Double
)

@Entity(tableName = "ListsNutrition")
data class ListNutrEntity(
    @PrimaryKey(autoGenerate = true)
    var id: Int? = null,
    @ColumnInfo(name = "nutrName")
    var nutrName: String,
    @ColumnInfo(name = "date")
    var date: String,
    @ColumnInfo(name = "productName")
    var productName: String,
    @ColumnInfo(name = "weight")
    var weight: Int,
    @ColumnInfo(name = "kcal")
    var kcal: Double,
    @ColumnInfo(name = "protein")
    var protein: Double,
    @ColumnInfo(name = "fats")
    var fats: Double,
    @ColumnInfo(name = "carb")
    var carb: Double
)

@Entity(tableName = "listNutrTotal")
data class ListNutrTotalEntity(
    @PrimaryKey(autoGenerate = true)
    var id: Int? = null,
    @ColumnInfo(name = "nutrName")
    var nutrName: String,
    @ColumnInfo(name = "time")
    var time: Int,
    @ColumnInfo(name = "kcalTotal")
    var kcalTotal: Int,
    @ColumnInfo(name = "proteinTotal")
    var proteinTotal: Int,
    @ColumnInfo(name = "fatsTotal")
    var fatsTotal: Int,
    @ColumnInfo(name = "carbTotal")
    var carbTotal: Int
)

@Entity(tableName = "Profile")
data class ProfileEntity(
    @PrimaryKey(autoGenerate = true)
    var id: Int? = null,
    @ColumnInfo(name = "date")
    var date: String,
    @ColumnInfo(name = "name")
    var name: String,
    @ColumnInfo(name = "gender")
    var gender: Int,
    @ColumnInfo(name = "height")
    var height: Double,
    @ColumnInfo(name = "weight")
    var weight: Double,
    @ColumnInfo(name = "age")
    var age: Double,
    @ColumnInfo(name = "activityLvlId")
    var activityLvlId: Int,
    @ColumnInfo(name = "avatar")
    var avatar: String,
    @ColumnInfo(name = "kcal")
    var kcal: Double,
    @ColumnInfo(name = "protein")
    var protein: Double,
    @ColumnInfo(name = "fats")
    var fats: Double,
    @ColumnInfo(name = "carb")
    var carb: Double
)

@Entity(tableName = "WaterCounter")
data class WaterCounterEntity(
    @PrimaryKey(autoGenerate = true)
    var id: Int? = null,
    @ColumnInfo(name = "date")
    var date: String,
    @ColumnInfo(name = "idProfile")
    var idProfile: Int,
    @ColumnInfo(name = "waterNorm")
    var waterNorm: Int,
    @ColumnInfo(name = "glassVolume")
    var glassVolume: Int,
    @ColumnInfo(name = "waterItem")
    var waterItem: String
)

@Entity(tableName = "StepsCounter")
data class StepsCounterEntity(
    @PrimaryKey(autoGenerate = true)
    var id: Int? = null,
    @ColumnInfo(name = "date")
    var date: String,
    @ColumnInfo(name = "idProfile")
    var idProfile: Int,
    @ColumnInfo(name = "stepsNorm")
    var stepsNorm: Int,
    @ColumnInfo(name = "stepCountStart")
    var stepCountStart: Int,
    @ColumnInfo(name = "stepCountEnd")
    var stepCountEnd: Int
)

@Dao
interface TestDao {
    @Insert
    fun insert(data: TestEntity)

    @Query("SELECT * FROM test")
    fun getAllData(): List<TestEntity>
}

@Dao
interface ProductsDao {
    @Insert
    fun insertProduct(product: ProductsEntity)

    @Query("SELECT * FROM Products WHERE flag = 1")
    fun getUserProducts(): List<ProductsEntity>

    @Query("SELECT * FROM Products WHERE count > 0 ORDER BY count DESC")
    fun getRecentProducts(): List<ProductsEntity>

    @Query("UPDATE Products SET count = :count WHERE productName = :productName")
    fun updateCount(count: Int, productName: String)

    @Query("DELETE FROM Products WHERE productName = :productName")
    fun deleteUserProduct(productName: String)

    @Query("SELECT * FROM Products")
    fun getAllProducts(): List<ProductsEntity>

    //здесь было suspend fun
    @Query("SELECT * FROM Products WHERE productName = :productName")
    fun getProductInfo(productName: String): ProductsEntity

    //здесь было suspend fun
    @Query("SELECT * FROM Products WHERE productName LIKE :query")
    fun search(query: String): List<ProductsEntity>
}

@Dao
interface ListNutrDao {
    @Insert
    fun insertData(data: ListNutrEntity)

    @Query("SELECT * FROM listsnutrition")
    fun getAllData(): List<ListNutrEntity>

    //Получить приёмы пищи
    @Query("SELECT * FROM listsnutrition WHERE date BETWEEN :startDate AND :endDate")
    fun getDayData(startDate: String, endDate: String): List<ListNutrEntity>

    @Query("SElECT * FROM listsnutrition WHERE nutrName = :nutrName AND date BETWEEN :startDate AND :endDate")
    fun getNutrNameProducts(
        nutrName: String,
        startDate: String,
        endDate: String
    ): List<ListNutrEntity>

    @Query("SELECT * FROM listsnutrition WHERE nutrName = :nutrName AND productName = :productName AND date BETWEEN :startDate AND :endDate")
    fun getProductInfo(
        nutrName: String,
        productName: String,
        startDate: String,
        endDate: String
    ): ListNutrEntity

    @Query("DELETE FROM listsnutrition WHERE nutrName = :nutrName AND productName = :productName AND date BETWEEN :startDate AND :endDate")
    fun deleteProduct(nutrName: String, productName: String, startDate: String, endDate: String)

    @Query("UPDATE listsnutrition SET weight = :weight, kcal = :kcal, protein = :protein, fats = :fats, carb = :carb WHERE nutrName = :nutrName AND productName = :productName AND date BETWEEN :startDate AND :endDate")
    fun updateProduct(
        nutrName: String,
        productName: String,
        weight: Int,
        kcal: Double,
        protein: Double,
        fats: Double,
        carb: Double,
        startDate: String,
        endDate: String
    )

    @Query("DELETE FROM listsnutrition")
    fun deleteAllData()
}

/*
@Dao
interface ListNutrTotalDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertOrUpdateData(entity: ListNutrTotalEntity)

    @Query("SELECT * FROM listNutrTotal WHERE nutrName = :nutrName")
    fun getByColumnName(nutrName: String): ListNutrTotalEntity?

        @Update
        fun update(entity: ListNutrTotalEntity)

    @Query("SELECT * FROM listNutrTotal")
    fun getAllData(): List<ListNutrTotalEntity>

    @Query("DELETE FROM listnutrtotal")
    fun deleteAllData()
}
*/

@Dao
interface ProfileDao {
    @Query("SELECT * FROM Profile")
    fun getProfileData(): List<ProfileEntity>

    @Query("SELECT * FROM Profile WHERE date BETWEEN :startDate AND :endDate")
    fun getDayData(startDate: String, endDate: String): List<ProfileEntity>

    @Insert
    fun insert(data: ProfileEntity)
}

@Dao
interface WaterCounterDao {
    @Insert
    fun insert(data: WaterCounterEntity)

    @Query("UPDATE WaterCounter SET waterNorm = :waterNorm, glassVolume = :glassVolume WHERE date BETWEEN :startDate AND :endDate")
    fun updateWaterNormAndGlassVolume(
        waterNorm: Int,
        glassVolume: Int,
        startDate: String,
        endDate: String
    )

    @Query("SELECT * FROM WaterCounter WHERE date BETWEEN :startDate AND :endDate")
    fun getDayData(startDate: String, endDate: String): List<WaterCounterEntity>

    @Query("SELECT * FROM WaterCounter")
    fun getWaterData(): List<WaterCounterEntity>

    @Query("SELECT * FROM WaterCounter WHERE waterItem = :waterItem AND date BETWEEN :startDate AND :endDate")
    fun getItemInfo(waterItem: String, startDate: String, endDate: String): WaterCounterEntity

    @Query("DELETE FROM WaterCounter WHERE waterItem = :waterItem AND date BETWEEN :startDate AND :endDate")
    fun deleteItem(waterItem: String, startDate: String, endDate: String)

    @Query("DELETE FROM WaterCounter")
    fun deleteAllWater()
}

@Dao
interface StepsCounterDao {
    @Insert
    fun insert(data: StepsCounterEntity)

    @Query("UPDATE StepsCounter SET stepsNorm = :stepsNorm WHERE date BETWEEN :startDate AND :endDate")
    fun updateStepsNorm(
        stepsNorm: Int,
        startDate: String,
        endDate: String
    )

    @Query("UPDATE StepsCounter SET stepCountEnd = :currentSteps WHERE date BETWEEN :startDate AND :endDate")
    fun updateCurrentSteps(
        currentSteps: Int,
        startDate: String,
        endDate: String
    )

    @Query("SELECT * FROM StepsCounter")
    fun getStepsData(): List<StepsCounterEntity>

    @Query("SELECT * FROM StepsCounter WHERE date BETWEEN :startDate AND :endDate")
    fun getDayData(startDate: String, endDate: String): List<StepsCounterEntity>

    @Query("DELETE FROM StepsCounter")
    fun deleteStepsData()

    @Query("DELETE FROM StepsCounter WHERE id = (SELECT MAX(id) FROM StepsCounter)")
    fun deleteLastNote()
}

@Database(
    entities = [ProductsEntity::class, ProfileEntity::class, ListNutrEntity::class, WaterCounterEntity::class, StepsCounterEntity::class, TestEntity::class],
    version = 1
)
abstract class Db : RoomDatabase() {
    abstract fun testDao(): TestDao

    abstract fun productsDao(): ProductsDao

    //abstract fun userProductsDao(): UserProductsDao
    abstract fun profileDao(): ProfileDao
    abstract fun listNutrDao(): ListNutrDao

    //abstract fun listNutrTotalDao(): ListNutrTotalDao
    abstract fun waterCounterDao(): WaterCounterDao
    abstract fun stepsCounterDao(): StepsCounterDao

    companion object {
        private var INSTANCE: Db? = null

        fun getDb(context: Context): Db {
            if (INSTANCE == null) {
                synchronized(Db::class) {
                    INSTANCE = Room.databaseBuilder(
                        context.applicationContext,
                        Db::class.java,
                        "Database.db"
                    )
                        .createFromAsset("databaseV5.db")
                        .build()
                }
            }
            return INSTANCE!!
        }
    }
}
