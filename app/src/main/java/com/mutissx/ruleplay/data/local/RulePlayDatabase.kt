package com.mutissx.ruleplay.data.local

import androidx.room.Dao
import androidx.room.Database
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.RoomDatabase
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "favorites")
data class FavoriteEntity(
    @PrimaryKey val key: String,
    val type: String,
    val sportId: String,
    val ruleId: String?,
    val savedAt: Long,
)

@Entity(tableName = "recent_sports")
data class RecentSportEntity(
    @PrimaryKey val sportId: String,
    val viewedAt: Long,
)

@Entity(tableName = "recent_searches")
data class RecentSearchEntity(
    @PrimaryKey val query: String,
    val searchedAt: Long,
)

@Dao
interface FavoritesDao {
    @Query("SELECT * FROM favorites ORDER BY savedAt DESC")
    fun observeAll(): Flow<List<FavoriteEntity>>

    @Query("SELECT EXISTS(SELECT 1 FROM favorites WHERE `key` = :key)")
    suspend fun exists(key: String): Boolean

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: FavoriteEntity)

    @Query("DELETE FROM favorites WHERE `key` = :key")
    suspend fun delete(key: String)
}

@Dao
interface HistoryDao {
    @Query("SELECT sportId FROM recent_sports ORDER BY viewedAt DESC LIMIT :limit")
    fun observeRecentSports(limit: Int): Flow<List<String>>

    @Upsert
    suspend fun upsertSport(entity: RecentSportEntity)

    @Query("DELETE FROM recent_sports WHERE sportId NOT IN (SELECT sportId FROM recent_sports ORDER BY viewedAt DESC LIMIT :keep)")
    suspend fun trimSports(keep: Int)

    @Query("SELECT `query` FROM recent_searches ORDER BY searchedAt DESC LIMIT :limit")
    fun observeRecentSearches(limit: Int): Flow<List<String>>

    @Upsert
    suspend fun upsertSearch(entity: RecentSearchEntity)

    @Query("DELETE FROM recent_searches WHERE `query` = :query")
    suspend fun deleteSearch(query: String)

    @Query("DELETE FROM recent_searches")
    suspend fun clearSearches()

    @Query("DELETE FROM recent_searches WHERE `query` NOT IN (SELECT `query` FROM recent_searches ORDER BY searchedAt DESC LIMIT :keep)")
    suspend fun trimSearches(keep: Int)
}

@Database(
    entities = [FavoriteEntity::class, RecentSportEntity::class, RecentSearchEntity::class],
    version = 1,
    exportSchema = true,
)
abstract class RulePlayDatabase : RoomDatabase() {
    abstract fun favoritesDao(): FavoritesDao
    abstract fun historyDao(): HistoryDao

    companion object {
        const val NAME = "ruleplay.db"
    }
}
