package com.nadiabsag.database.entities

import androidx.room.Entity
import androidx.room.ColumnInfo
import androidx.room.PrimaryKey
import com.nadiabsag.room.SuperheroItemResponse

@Entity(tableName = "list_table")

data class ListEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo (name = "id") val id: Int = 0,
    @ColumnInfo (name = "idApi") val idApi: String,
    @ColumnInfo  (name = "name") val name: String,
    @ColumnInfo  (name = "image") val image: String
)
//fun SuperheroItemResponse.toDatabase() = ListEntity(name = name, image = superheroImage.url)

fun SuperheroItemResponse.toDatabase(): ListEntity {
    return ListEntity(idApi = superheroId, name = name, image = superheroImage.url)
}
