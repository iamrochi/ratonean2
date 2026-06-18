package com.example.ratonean2_app.product.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.ratonean2_app.product.data.entity.ProductEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ProductDao {

    @Query("SELECT * FROM products WHERE branchId = :branchId")
    fun getByBranch(branchId: String): Flow<List<ProductEntity>>

    @Query("SELECT * FROM products WHERE productId = :productId LIMIT 1")
    fun getById(productId: String): Flow<ProductEntity?>

    @Query("SELECT * FROM products WHERE branchId IN (:branchIds) AND description LIKE '%' || :query || '%'")
    fun searchInBranches(branchIds: List<String>, query: String): Flow<List<ProductEntity>>

    @Query("SELECT * FROM products WHERE branchId IN (:branchIds) LIMIT :limit")
    fun getPopularInBranches(branchIds: List<String>, limit: Int): Flow<List<ProductEntity>>

    @Query("SELECT * FROM products WHERE branchId = :branchId AND promoPrice1 IS NOT NULL")
    fun getPromosByBranch(branchId: String): Flow<List<ProductEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(list: List<ProductEntity>)

    @Query("DELETE FROM products")
    suspend fun clear()
}
