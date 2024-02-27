package com.nadiabsag.room


import SuperheroDatabase
import android.os.Bundle
import android.util.TypedValue
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.nadiabsag.database.entities.DetailEntity
import com.nadiabsag.database.entities.toDatabase
import com.nadiabsag.room.SuperheroListActivity.Companion.EXTRA_ID
import com.nadiabsag.room.databinding.ActivityDetailSuperheroBinding
import com.squareup.picasso.Picasso
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import kotlin.math.roundToInt

class DetailSuperheroActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDetailSuperheroBinding
    private lateinit var room: SuperheroDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityDetailSuperheroBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val id: String = intent.getStringExtra(EXTRA_ID).orEmpty()
        room = DatabaseProvider.getDatabase(applicationContext)
        getSuperheroInformation(id.toInt())

    }

    private fun getSuperheroInformation(id: Int) {
        CoroutineScope(Dispatchers.IO).launch {
            val superheroDetail =
                getRetrofit().create(ApiService::class.java).getSuperheroDetail()
            val responseSuperheroDetail : SuperheroDetailResponse? = superheroDetail.body()
            if (responseSuperheroDetail != null) {
                val superheroItemDetail = responseSuperheroDetail.listDetail.map { it.toDatabase() }
                if(room.getDetailDao().getAllSuperheroDetail().isNotEmpty()){
                    for(superheroDetail in superheroItemDetail){
                        val superheroDetailRoom = room.getDetailDao().getSuperheroDetailByID(id)
                        if(superheroDetailRoom[0].id==id){
                            room.getDetailDao().update(superheroDetail)
                        }else{
                            room.getDetailDao().insert(superheroDetail)
                        }
                    }

                } else{
                    room.getDetailDao().insertAll(superheroItemDetail)
                }
                val listDetails = room.getDetailDao().getAllSuperheroDetail()
                runOnUiThread { createUI(listDetails, id) }

            }
        }
    }

    private fun getRetrofit(): Retrofit {
        return Retrofit
            .Builder()
            .baseUrl("https://superheroapi.com/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    private fun createUI(superhero: List<DetailEntity>, id : Int) {
        CoroutineScope(Dispatchers.IO).launch{
            for (detail in superhero.listIterator()){
                if(detail.id==id){
                    var image = Picasso.get().load(room.getDetailDao().getImage(detail.id))
                    CoroutineScope(Dispatchers.Main).launch { image.into(binding.ivSuperhero) }

                    binding.tvSuperheroName.text = room.getDetailDao().getSuperheroName(detail.id)
                    binding.tvSuperheroRealName.text = detail.fullName
                    binding.tvPublisher.text = detail.publisher
                    prepareStats(detail)

                }
            }
        }
    }

    private fun prepareStats(detail: DetailEntity) {
        updateHeight(binding.viewIntelligence, detail.intelligence.orEmpty())
        updateHeight(binding.viewStrength, detail.strength.orEmpty())
        updateHeight(binding.viewSpeed, detail.speed.orEmpty())
        updateHeight(binding.viewDurability, detail.durability.orEmpty())
        updateHeight(binding.viewPower, detail.power.orEmpty())
        updateHeight(binding.viewCombat, detail.combat.orEmpty())
    }

    private fun updateHeight(view: View, stat: String) {
        val params = view.layoutParams
        if (stat != "null") {
            params.height = pxToDp(stat.toFloat())
        } else {
            params.height = 0
        }
        CoroutineScope(Dispatchers.Main).launch { view.layoutParams = params }
    }

    private fun pxToDp(px: Float): Int {
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, px, resources.displayMetrics)
            .roundToInt()
    }

    private fun formatConnections(connections: String): String {
        val relativesArray: List<String> = connections.split("), ")
        var relativesFormatted = ""
        relativesArray.forEach {
            s -> relativesFormatted = relativesFormatted.plus("• $s)\n")
        }
        return relativesFormatted.dropLast(2)
    }

}