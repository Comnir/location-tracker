package com.jefferson.tracker.session

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import io.reactivex.Completable

@Dao
interface SessionDao {
    @Insert
    fun addSession(session: Session): Completable

    @Query("SELECT * FROM Session")
    fun allSessions(): LiveData<List<Session>>
}