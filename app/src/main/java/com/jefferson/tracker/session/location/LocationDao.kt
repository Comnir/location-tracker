package com.jefferson.tracker.session.location

import androidx.lifecycle.LiveData
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

    @Query("SELECT * FROM Location")
    fun allLocations(): LiveData<List<Location>>
}