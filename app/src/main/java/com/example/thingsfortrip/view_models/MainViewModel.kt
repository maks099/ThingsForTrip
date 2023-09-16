package com.example.thingsfortrip.view_models

import android.util.Log
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.android.volley.RequestQueue
import com.android.volley.toolbox.JsonObjectRequest
import com.example.thingsfortrip.data.AutocompleteResult
import com.example.thingsfortrip.data.WorkCreds
import com.example.thingsfortrip.model.MainContract
import com.example.thingsfortrip.data.WeatherData
import com.example.thingsfortrip.data.models.Category
import com.example.thingsfortrip.data.models.ListData
import com.example.thingsfortrip.data.models.Thing
import com.google.android.gms.maps.model.LatLng
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.net.FetchPlaceRequest
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsRequest
import com.google.android.libraries.places.api.net.PlacesClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import org.json.JSONException
import org.json.JSONObject
import java.nio.charset.StandardCharsets
import java.util.Calendar
import java.util.Date
import com.example.thingsfortrip.data.repositories.FirebaseRepo
import com.example.thingsfortrip.data.repositories.local.AppRepository
import kotlinx.coroutines.async
import kotlinx.coroutines.cancel
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext


class MainViewModel : BaseViewModel<MainContract.Event, MainContract.State>() {


    lateinit var placesClient: PlacesClient
    lateinit var requestQueue: RequestQueue
    lateinit var apiKey: String
    lateinit var errorMsg: String
    lateinit var listNameError: String
    lateinit var appRepository: AppRepository

    private var job: Job? = null
    private val firebaseRepository = FirebaseRepo.instance

    val weatherData = mutableStateOf(WeatherData.empty)
    val workCreds = mutableStateOf(WorkCreds())
    val categoriesList = mutableStateListOf<Category>()
    val locationsAutofill = mutableStateListOf<AutocompleteResult>()
    val things = mutableStateListOf<Thing>()
    val thingsListName = mutableStateOf("")



    init {
        viewModelScope.launch(Dispatchers.IO) {
            firebaseRepository.getCategories.addOnCompleteListener{
                if(it.isSuccessful){
                    it.result.forEach {
                        val cat: Category = it.toObject(Category::class.java)
                        categoriesList.add(cat)
                    }
                }
            }
        }
    }


    private fun searchPlaces(query: String) {
        job?.cancel()
        locationsAutofill.clear()
        if(query.trim().isNotEmpty()) {
            job = viewModelScope.launch(Dispatchers.IO) {
                val request = FindAutocompletePredictionsRequest
                    .builder()
                    .setQuery(query)
                    .build()
                placesClient
                    .findAutocompletePredictions(request)
                    .addOnSuccessListener { response ->
                        response.autocompletePredictions.forEach {
                            locationsAutofill.add(AutocompleteResult(it.getFullText(null).toString(), it.placeId))
                        }
                    }
                    .addOnFailureListener {
                        it.printStackTrace()
                        println(it.cause)
                        println(it.message)
                    }
            }
        }
    }



    override fun createInitialState(): MainContract.State {
        return MainContract.State.Initial
    }

    override fun handleEvents(it: MainContract.Event) {
        when(it){
            is MainContract.Event.OnLocationInputChanged -> searchPlaces(it.location)
            is MainContract.Event.OnLocationSelect -> {
                locationsAutofill.clear()
                getCoordinates(result = it.result)
                if(workCreds.value.isFilled()) loadThings()
            }
            is MainContract.Event.OnDateChange -> {
                workCreds.value = workCreds.value.copy(
                    date = Date(it.date)
                )
                if(workCreds.value.isWeatherOptionAvailable()){
                    updateWeather()
                }
                if(workCreds.value.isFilled()) loadThings()
            }
            is MainContract.Event.SetOnCategory -> {
                workCreds.value = workCreds.value.copy(category = it.category)
                if(workCreds.value.isFilled()) loadThings()
            }

            is MainContract.Event.ChangeThingStatus -> {
                val t = it.thing
                things.remove(t)
                t.isChecked = !t.isChecked
                if(!t.isChecked){
                    things.add(0, t)
                    return
                }
                things.add(t)
            }

            MainContract.Event.OnSaveList -> {
                if(thingsListName.value.trim().isEmpty()){
                    setState(MainContract.State.Error(listNameError))
                    return
                }
                val ld = ListData(name = thingsListName.value, time = workCreds.value.date.time)
                viewModelScope.launch {
                    val v = async {
                        appRepository.insertListData(ld)
                    }
                    v.await()

                    val newListData = async {
                        appRepository.getListDataByName(ld.name)
                    }
                    things.forEach {
                        it.listCode = newListData.await().id
                    }
                    appRepository.insertThings(things)
                    setState(MainContract.State.OnThingsListWasSaved(newListData.await().id))
                }

            }

            MainContract.Event.CheckSavedLists -> {
                viewModelScope.launch(Dispatchers.IO) {
                    appRepository.getListNames().collect{
                        if(it.size > 0){
                            setState(MainContract.State.ShowSavedListButton)
                        }
                    }
                }

            }
        }
    }



    private fun getCoordinates(result: AutocompleteResult) {
        viewModelScope.launch(Dispatchers.IO) {
            val placeFields = listOf(Place.Field.LAT_LNG)
            val request = FetchPlaceRequest.newInstance(result.placeId, placeFields)
            placesClient.fetchPlace(request)
                .addOnSuccessListener {
                    if (it != null && it.place.latLng != null) {
                        workCreds.value = workCreds.value.copy(
                            latLng = it.place.latLng as LatLng
                        )
                        if(workCreds.value.isWeatherOptionAvailable()){
                            updateWeather()
                        }
                    }
                }
                .addOnFailureListener {
                    it.printStackTrace()
                }
        }
    }

    private fun loadThings(){
        things.clear()
        firebaseRepository.getThings(workCreds.value.category!!.code)
            .addOnCompleteListener{
                if(it.isSuccessful){
                   it.result.forEach {
                       val t = it.toObject(Thing::class.java)
                       things.add(t)
                   }
                } else{
                    setState(MainContract.State.Error("${errorMsg} ${it.exception?.message}"))
                }
            }

        firebaseRepository.getClothes(weatherData.value.code)
            .addOnCompleteListener{
                if(it.isSuccessful){
                    it.result.forEach {
                        val t = it.toObject(Thing::class.java)
                        things.add(t)
                    }
                } else{
                    setState(MainContract.State.Error("$errorMsg ${it.exception?.message}"))
                }
            }
    }

    private fun updateWeather() {
        viewModelScope.launch(Dispatchers.IO) {
            val mStringRequest = JsonObjectRequest(getURL(),
                { response ->
                    try {
                        val weatherCode = response
                            ?.getJSONObject("data")
                            ?.getJSONArray("timelines")
                            ?.getJSONObject(0)
                            ?.getJSONArray("intervals")
                            ?.getJSONObject(0)
                            ?.getJSONObject("values")
                            ?.getInt("weatherCode")
                        val wm: WeatherData? = WeatherData.findByCode(weatherCode!!)
                        if (wm != null) {
                            weatherData.value = wm
                        }
                    } catch (e: JSONException) {
                        setState(MainContract.State.Error(e.message + ""))
                    }
                }) {
                    val errorResponse: String = String(it.networkResponse.data, StandardCharsets.UTF_8)
                    val message: String = JSONObject(errorResponse).getString("message")
                    setState(MainContract.State.Error(message))
            }
            requestQueue.add(mStringRequest)
        }

    }

    private fun getURL(): String {
        val c = Calendar.getInstance()
        c.time = workCreds.value.date
        val dateStr = "${c.get(Calendar.YEAR)}-${c.get(Calendar.MONTH) + 1}-${c.get(Calendar.DAY_OF_MONTH)}";
        return "https://api.tomorrow.io/v4/timelines?" +
                "location=" + workCreds.value.latLng.latitude + "," + workCreds.value.latLng.longitude +
                "&fields=temperature,weatherCode" +
                "&timesteps=1h" +
                "&startTime=${dateStr}T11:00:00" +
                "&endTime=${dateStr}T13:00:00" +
                "&units=metric&" +
                "apikey=${apiKey}"
    }



}