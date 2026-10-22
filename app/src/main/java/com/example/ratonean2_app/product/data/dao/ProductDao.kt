package com.example.ratonean2_app.product.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.ratonean2_app.product.data.entity.ProductEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ProductDao {

    // Páginas acotadas + orden estable (antes full-table sin ORDER BY).
    @Query(
        "SELECT * FROM products WHERE branchId = :branchId " +
            "ORDER BY description ASC LIMIT :limit OFFSET :offset"
    )
    fun getByBranch(branchId: String, limit: Int, offset: Int): Flow<List<ProductEntity>>

    @Query("SELECT * FROM products WHERE branchKey = :branchKey " +
            "ORDER BY description ASC LIMIT :limit OFFSET :offset")
    fun getByBranchKey(branchKey: String, limit: Int, offset: Int): Flow<List<ProductEntity>>

    @Query("SELECT * FROM products WHERE productId = :productId LIMIT 1")
    fun getById(productId: String): Flow<ProductEntity?>

    // FTS en vez de LIKE '%q%' (full-scan inviable con millones de filas).
    // El MATCH va contra productsFts; el filtro de sucursal contra products.
    @Query(
        "SELECT p.* FROM products p JOIN productsFts f ON p.rowid = f.rowid " +
            "WHERE f.productsFts MATCH :ftsQuery AND p.branchId IN (:branchIds) " +
            "ORDER BY p.description ASC LIMIT :limit OFFSET :offset"
    )
    fun searchInBranches(
        branchIds: List<String>,
        ftsQuery: String,
        limit: Int,
        offset: Int
    ): Flow<List<ProductEntity>>

    @Query(
        "SELECT p.* FROM products p JOIN productsFts f ON p.rowid = f.rowid " +
            "WHERE f.productsFts MATCH :ftsQuery AND p.branchKey IN (:branchKeys) " +
            "ORDER BY p.description ASC LIMIT :limit OFFSET :offset"
    )
    fun searchInBranchKeys(
        branchKeys: List<String>,
        ftsQuery: String,
        limit: Int,
        offset: Int
    ): Flow<List<ProductEntity>>

    @Query(
        "SELECT * FROM products WHERE branchId IN (:branchIds) " +
            "ORDER BY description ASC LIMIT :limit OFFSET :offset"
    )
    fun getPopularInBranches(branchIds: List<String>, limit: Int, offset: Int): Flow<List<ProductEntity>>

    @Query(
        "SELECT * FROM products WHERE branchId = :branchId AND promoPrice1 IS NOT NULL " +
            "ORDER BY description ASC LIMIT :limit OFFSET :offset"
    )
    fun getPromosByBranch(branchId: String, limit: Int, offset: Int): Flow<List<ProductEntity>>

    // Frescura sin cargar la tabla (antes maxOf en memoria sobre full-list).
    @Query("SELECT MAX(lastUpdated) FROM products WHERE branchId = :branchId")
    suspend fun maxUpdatedByBranch(branchId: String): Long?

    @Query("SELECT MAX(lastUpdated) FROM products WHERE branchKey = :branchKey")
    suspend fun maxUpdatedByBranchKey(branchKey: String): Long?

    @Query("SELECT MAX(lastUpdated) FROM products WHERE branchKey IN (:branchKeys)")
    suspend fun maxUpdatedInBranchKeys(branchKeys: List<String>): Long?

    @Query("SELECT MAX(lastUpdated) FROM products WHERE branchId IN (:branchIds)")
    suspend fun maxUpdatedInBranches(branchIds: List<String>): Long?

    @Query("SELECT COUNT(*) FROM products WHERE branchId = :branchId")
    suspend fun countByBranch(branchId: String): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(list: List<ProductEntity>)

    @Query("DELETE FROM products")
    suspend fun clear()

    // Evicción del caché: por antigüedad y por tope de filas. Los triggers
    // de FTS borran el índice en cascada. Las listas del usuario guardan su
    // propia copia (ShoppingItemEntity), así que el prune no las afecta.
    @Query("DELETE FROM products WHERE lastUpdated < :minFresh")
    suspend fun pruneOlderThan(minFresh: Long): Int

    @Query(
        "DELETE FROM products WHERE productKey NOT IN " +
            "(SELECT productKey FROM products ORDER BY lastUpdated DESC LIMIT :maxRows)"
    )
    suspend fun enforceRowCap(maxRows: Int): Int

    @Query("SELECT * FROM products WHERE ean = :ean ORDER BY listPrice ASC LIMIT :limit")
    suspend fun getByEan(ean: String, limit: Int): List<ProductEntity>
}

/**
 * Sanitiza input de usuario a query FTS segura: tokens con AND implícito y
 * prefijo en el último término ("leche ente*"). Sin esto, comillas o *,
 * OR/NEAR romperían el MATCH o cambiarían la semántica.
 */
fun String.toFtsQuery(): String {
    val tokens = trim().split(Regex("\\s+"))
        .map { it.replace("\"", "\"\"") }
        .filter { it.isNotBlank() }
    if (tokens.isEmpty()) return "\"\""
    val quoted = tokens.map { "\"$it\"" }
    return (quoted.dropLast(1) + "${quoted.last()}*").joinToString(" ")
}
