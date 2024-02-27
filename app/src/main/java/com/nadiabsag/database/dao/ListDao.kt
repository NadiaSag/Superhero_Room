package com.nadiabsag.database.dao

import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.nadiabsag.database.entities.ListEntity

interface ListDao{
        @Query ("SELECT * FROM list_table")
        suspend fun getAllSuperhero():List<ListEntity>

        @Query ("SELECT * FROM list_table WHERE idApi LIKE :query")
        suspend fun getSuperheroes(query:String):List<ListEntity>

        @Query ("SELECT * FROM list_table WHERE id LIKE :id")
        suspend fun getSuperhero(id:Int):List<ListEntity>

        @Insert (onConflict = OnConflictStrategy.REPLACE)
        suspend fun insertAll (superheroes:List<ListEntity>)

        @Update(onConflict = OnConflictStrategy.REPLACE)
        suspend fun update(superhero: ListEntity)

        @Insert(onConflict = OnConflictStrategy.REPLACE)
        suspend fun insert(superhero:ListEntity)

        @Query ("DELETE FROM list_table")
        suspend fun deleteAllSuperheroes()
    }
