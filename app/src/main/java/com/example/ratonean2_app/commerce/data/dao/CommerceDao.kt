package com.example.ratonean2_app.commerce.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.ratonean2_app.commerce.data.entity.CommerceEntity
import kotlinx.coroutines.flow.Flow


@Dao
interface CommerceDao {

    @Query("SELECT * FROM commerce")
    fun getAll(): Flow<List<CommerceEntity>>

    // Agregamos este para el detalle rápido
    @Query("SELECT * FROM commerce WHERE id = :id LIMIT 1")
    fun getById(id: String): Flow<CommerceEntity?>

    @Query("SELECT * FROM commerce WHERE commerceKey = :key LIMIT 1")
    fun getByKey(key: String): Flow<CommerceEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(list: List<CommerceEntity>)

    // Agregamos este para inserciones individuales
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(commerce: CommerceEntity)

    @Query("DELETE FROM commerce")
    suspend fun clear()
}