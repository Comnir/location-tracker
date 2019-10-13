package com.jefferson.tracker.persistance

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import io.reactivex.Completable
import io.reactivex.Flowable

@Dao
interface LocationDao {
    @Insert
    fun insertLocation(location: Location): Completable

    @Query("SELECT * FROM Location WHERE sessionId = :sessionId")
    fun locationsForSession(sessionId: Long): Flowable<List<Location>>
}