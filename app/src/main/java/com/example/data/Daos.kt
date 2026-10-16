package com.example.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface ShiftDao {
    @Query("SELECT * FROM shifts ORDER BY dateString ASC, startTimeString ASC")
    fun getAllShifts(): Flow<List<ShiftEntity>>

    @Query("SELECT * FROM shifts WHERE status = 'UPCOMING' ORDER BY dateString ASC, startTimeString ASC")
    fun getUpcomingShifts(): Flow<List<ShiftEntity>>

    @Query("SELECT * FROM shifts WHERE id = :id LIMIT 1")
    suspend fun getShiftById(id: Long): ShiftEntity?

    @Query("SELECT * FROM shifts WHERE sourceMessageId = :messageId LIMIT 1")
    suspend fun getShiftBySourceMessageId(messageId: String): ShiftEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertShift(shift: ShiftEntity): Long

    @Update
    suspend fun updateShift(shift: ShiftEntity)

    @Delete
    suspend fun deleteShift(shift: ShiftEntity)

    @Query("DELETE FROM shifts WHERE id = :id")
    suspend fun deleteShiftById(id: Long)

    @Query("DELETE FROM shifts")
    suspend fun deleteAllShifts()
}

@Dao
interface DetectedMessageDao {
    @Query("SELECT * FROM detected_messages ORDER BY receivedTimestamp DESC")
    fun getAllMessages(): Flow<List<DetectedMessageEntity>>

    @Query("SELECT * FROM detected_messages WHERE id = :id LIMIT 1")
    suspend fun getMessageById(id: Long): DetectedMessageEntity?

    @Query("SELECT * FROM detected_messages WHERE subject = :subject AND body = :body LIMIT 1")
    suspend fun findDuplicate(subject: String, body: String): DetectedMessageEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessage(message: DetectedMessageEntity): Long

    @Update
    suspend fun updateMessage(message: DetectedMessageEntity)

    @Query("DELETE FROM detected_messages WHERE id = :id")
    suspend fun deleteMessageById(id: Long)

    @Query("DELETE FROM detected_messages")
    suspend fun deleteAllMessages()
}

@Dao
interface SenderFilterDao {
    @Query("SELECT * FROM sender_filters ORDER BY id ASC")
    fun getAllFilters(): Flow<List<SenderFilterEntity>>

    @Query("SELECT * FROM sender_filters WHERE isEnabled = 1")
    suspend fun getActiveFilters(): List<SenderFilterEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFilter(filter: SenderFilterEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFilters(filters: List<SenderFilterEntity>)

    @Update
    suspend fun updateFilter(filter: SenderFilterEntity)

    @Delete
    suspend fun deleteFilter(filter: SenderFilterEntity)

    @Query("DELETE FROM sender_filters WHERE id = :id")
    suspend fun deleteFilterById(id: Long)
}

@Dao
interface UserPreferencesDao {
    @Query("SELECT * FROM user_preferences WHERE id = 1 LIMIT 1")
    fun getPreferences(): Flow<UserPreferencesEntity?>

    @Query("SELECT * FROM user_preferences WHERE id = 1 LIMIT 1")
    suspend fun getPreferencesDirect(): UserPreferencesEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun savePreferences(preferences: UserPreferencesEntity)
}
