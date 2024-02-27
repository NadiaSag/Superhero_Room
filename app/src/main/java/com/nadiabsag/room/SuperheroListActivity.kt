package com.nadiabsag.room
import SuperheroDatabase
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.room.Room
import com.nadiabsag.database.entities.DetailEntity
import com.nadiabsag.database.entities.toDatabase
import com.nadiabsag.room.databinding.ActivitySuperheroListBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class SuperheroListActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySuperheroListBinding
    private lateinit var retrofit: Retrofit
    private lateinit var room: SuperheroDatabase
    private lateinit var adapter: SuperheroAdapter


    companion object {
        const val EXTRA_ID = "extra_id"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySuperheroListBinding.inflate(layoutInflater)
        setContentView(binding.root)
        retrofit = getRetrofit()
        room = DatabaseProvider.getDatabase(applicationContext)
        fillDatabase()
        //room = Room.databaseBuilder(this, SuperheroDatabase::class.java, "superheroes").build()
        initUI()
    }

    private fun navigateToDetail(id: String) {
        val intent = Intent(this, DetailSuperheroActivity::class.java)
        intent.putExtra(EXTRA_ID, id)
        startActivity(intent)
    }

    private fun initUI() {
       /* binding.searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
          override fun onQueryTextSubmit(query: String?): Boolean {
               searchByName(query.orEmpty())
              return false
           }

            override fun onQueryTextChange(newText: String?) = false
        })*/

        adapter = SuperheroAdapter { superheroId -> navigateToDetail(superheroId) }
        binding.rvSuperhero.setHasFixedSize(true)
        binding.rvSuperhero.layoutManager = LinearLayoutManager(this)
        binding.rvSuperhero.adapter = adapter
    }

    /*private fun searchByName(query: String) {
        binding.progressBar.isVisible = true
        CoroutineScope(Dispatchers.IO).launch {
            val myResponse: Response<SuperheroDataResponse> =
                retrofit.create(ApiService::class.java).getSuperheroes()
            if (myResponse.isSuccessful) {
                Log.i("Consulta", "Funciona :)")
                val response: SuperheroDataResponse? = myResponse.body()
                if (response != null) {
                    Log.i("Cuerpo de la consulta", response.toString())
                    runOnUiThread {
                        adapter.updateList(response.superheroes)
                        binding.progressBar.isVisible = false
                    }
                }
            } else {
                Log.i("Consulta", "No funciona :(")
            }
        }
    }*/
    private fun fillDatabase() {
        binding.progressBar.isVisible = true
        CoroutineScope(Dispatchers.IO).launch {
            val myResponse: Response<SuperheroDataResponse> =
                retrofit.create(ApiService::class.java).getSuperheroes()
            val response: SuperheroDataResponse? = myResponse.body()
            val superheroItemList = response?.superheroes?.map { it.toDatabase() }
            if (superheroItemList != null) {
                if(room.getListDao().getAllSuperhero().isNotEmpty()){
                    for (superheroApi in superheroItemList){
                        val superheroRoom = room.getListDao().getSuperheroes(superheroApi.idApi)
                        if(superheroRoom[0].idApi==superheroApi.idApi){
                            room.getListDao().update(superheroApi)
                        }else{
                            room.getListDao().insert(superheroApi)
                        }
                    }
                }else{
                room.getListDao().insertAll(superheroItemList)
                }
                adapter.updateList(room.getListDao().getAllSuperhero())
                runOnUiThread{binding.progressBar.isVisible = false}

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

}