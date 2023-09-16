package com.example.thingsfortrip.views

import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.List
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SelectableDates
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.android.volley.toolbox.Volley
import com.example.thingsfortrip.R
import com.example.thingsfortrip.model.MainContract
import com.example.thingsfortrip.data.WeatherData
import com.example.thingsfortrip.data.models.Thing
import com.example.thingsfortrip.data.repositories.local.AppDatabase
import com.example.thingsfortrip.data.repositories.local.AppRepository
import com.example.thingsfortrip.view_models.MainViewModel
import com.google.android.libraries.places.api.Places
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainView(navController: NavController){
    val context = LocalContext.current

    val viewModel: MainViewModel = viewModel()
    viewModel.placesClient = Places.createClient(context)
    viewModel.requestQueue = Volley.newRequestQueue(context)
    viewModel.apiKey = context.getString(R.string.weather_api_key)
    viewModel.errorMsg = context.getString(R.string.error)
    viewModel.listNameError = context.getString(R.string.list_name_error)
    val database by lazy { AppDatabase.getDatabase(context) }
    val repository by lazy { AppRepository(database.createDAO()) }
    viewModel.appRepository = repository



    val dateDialogIsShown = remember { mutableStateOf(false) }
    val thingsListDialogIsShown = remember { mutableStateOf(false) }
    var dropdownExpanded by remember { mutableStateOf(false) }
    var listsButtonIsShowed by remember { mutableStateOf(false) }

    Scaffold(
        modifier = Modifier
            .padding(
                horizontal = 16.dp,
                vertical = 8.dp
            ),
        floatingActionButton = {
            Row {
                if(viewModel.things.size > 0){
                    FloatingActionButton(
                        modifier = Modifier.padding(8.dp),
                        onClick = {
                            thingsListDialogIsShown.value = true
                        }
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.baseline_save_24),
                            contentDescription = ""
                        )
                    }
                }
                viewModel.setEvent(MainContract.Event.CheckSavedLists)
                if(listsButtonIsShowed){
                    FloatingActionButton(
                        modifier = Modifier.padding(8.dp),
                        onClick = {
                            navController.navigate("saved_lists")
                        }
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.List,
                            contentDescription = ""
                        )
                    }
                }
            }

        }
    ) {
        paddingValues ->

        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            color = MaterialTheme.colorScheme.background
        ) {
            Column {
                var address by remember {
                    mutableStateOf("")
                }
                address.useDebounce(onChange = {
                    viewModel.setEvent(MainContract.Event.OnLocationInputChanged(address))
                })

                OutlinedTextField(
                    value = address,
                    label = {
                        Text(text = stringResource(id = R.string.input_location))
                    },
                    onValueChange = {
                        address = it
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(4.dp)
                )
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(viewModel.locationsAutofill) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(8.dp)
                                .clickable {
                                    viewModel.setEvent(MainContract.Event.OnLocationSelect(it))
                                }
                        ) {
                            Text(
                                it.address,
                                modifier = Modifier
                                    .padding(8.dp)
                            )
                        }
                    }
                }
                Row (
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp)
                ) {
                    Button(
                        onClick = {
                            dateDialogIsShown.value = true
                        },
                        modifier = Modifier
                            .padding(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.DateRange,
                            contentDescription = ""
                        )
                        val date = viewModel.workCreds.value.date
                        Text(
                            text = if(date == Date()){
                                stringResource(id = R.string.date)
                            } else{
                                val format = SimpleDateFormat("dd/MM/yyy")
                                format.format(date)
                            },
                            modifier = Modifier.padding(horizontal = 4.dp)
                        )
                    }
                    val weatherModel = viewModel.weatherData.value
                    if(weatherModel != WeatherData.empty){
                        Spacer(modifier = Modifier.width(50.dp))
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                painter = painterResource(id = weatherModel.drawableResource),
                                tint = Color.Unspecified,
                                contentDescription = stringResource(id = weatherModel.stringResource),
                                modifier = Modifier
                                    .width(75.dp)
                                    .height(75.dp)
                            )
                            Text(
                                text = stringResource(id = weatherModel.stringResource),
                                fontSize = 18.sp,
                                modifier = Modifier
                                    .padding(8.dp)
                            )
                        }
                    }
                }
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(4.dp)
                ){
                    Button(
                        onClick = {
                            dropdownExpanded = true
                        }
                    ){
                        val currentCategory = viewModel.workCreds.value.category
                        Text(text = currentCategory?.name ?: stringResource(id = R.string.pick_category))
                    }
                    DropdownMenu(
                        expanded = dropdownExpanded,
                        onDismissRequest = { dropdownExpanded = false },


                        ) {

                        viewModel.categoriesList.forEach {
                            DropdownMenuItem(
                                text = { Text(text = it.name) },
                                onClick = {
                                    dropdownExpanded = false
                                    viewModel.setEvent(MainContract.Event.SetOnCategory(it))
                                }
                            )
                        }
                    }
                }
                if(viewModel.things.size > 0){
                    LazyColumn(
                        modifier = Modifier
                            .border(
                                width = 1.dp,
                                color = Color.Gray,
                                shape = RoundedCornerShape(16.dp)
                            )
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp)

                    ){
                        items(viewModel.things){ thing ->
                            ThingListItem(thing){
                                viewModel.setEvent(MainContract.Event.ChangeThingStatus(thing))
                            }
                        }
                    }
                }
            }
    }


        if(dateDialogIsShown.value) {
            val datePickerState = rememberDatePickerState(
                initialSelectedDateMillis = viewModel.workCreds.value.date.time,
                selectableDates = object : SelectableDates {
                    override fun isSelectableDate(utcTimeMillis: Long): Boolean {
                        return utcTimeMillis >= System.currentTimeMillis()
                    }
                }
            )
            DatePickerDialog(
                onDismissRequest = {
                    dateDialogIsShown.value = false
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            dateDialogIsShown.value = false
                            viewModel.setEvent(
                                MainContract.Event.OnDateChange(datePickerState.selectedDateMillis ?: 0)
                            )
                        },
                    ) {
                        Text(stringResource(id = R.string.ok))
                    }
                },
                dismissButton = {
                    TextButton(
                        onClick = {
                            dateDialogIsShown.value = false
                        }
                    ) {
                        Text(stringResource(id = R.string.cancel))
                    }
                },

            ) {
                DatePicker(state = datePickerState)
            }
        }
        
        if(thingsListDialogIsShown.value){
            Dialog(
                onDismissRequest = { thingsListDialogIsShown.value },
            ) {
                Card (
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 168.dp)
                        .padding(16.dp),
                    shape = RoundedCornerShape(16.dp),
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(8.dp),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally,

                        ) {
                        OutlinedTextField(
                            value = viewModel.thingsListName.value,
                            onValueChange = {
                                viewModel.thingsListName.value = it
                            },
                            label = { Text(stringResource(id = R.string.input_list_name)) }
                        )
                        Row {
                            TextButton(onClick = {
                                viewModel.setEvent(MainContract.Event.OnSaveList)
                                thingsListDialogIsShown.value = false
                            }) {
                                Text(text = stringResource(id = R.string.save))
                            }
                            TextButton(onClick = {
                                thingsListDialogIsShown.value = false
                            }) {
                                Text(text = stringResource(id = R.string.cancel))
                            }
                        }
                    }
                }


            }
        }
    }
    LaunchedEffect(Unit){
        viewModel.uiState.collect{
            when(it){
                is MainContract.State.Error -> Toast.makeText(context, it.message, Toast.LENGTH_SHORT).show()
                MainContract.State.Initial -> {}
                is MainContract.State.OnThingsListWasSaved ->
                    navController.navigate(
                        "things_list/{listId}"
                        .replace(
                            oldValue = "{listId}",
                            newValue = "${it.listId}"
                        ))

                MainContract.State.ShowSavedListButton -> listsButtonIsShowed = true
            }
        }
    }
}

@Composable
fun ThingListItem(thing: Thing, onCheckBoxChange: () -> Unit){
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier
            .fillMaxWidth()
    ) {
        Text(
            text = thing.name,
            fontSize = 16.sp
        )
        Checkbox(
            checked = thing.isChecked,
            onCheckedChange = {
                onCheckBoxChange()
            }
        )
    }
}

@Composable
fun <T> T.useDebounce(
    delayMillis: Long = 5000L,
    coroutineScope: CoroutineScope = rememberCoroutineScope(),
    onChange: (T) -> Unit
): T{
    val state by rememberUpdatedState(this)

    DisposableEffect(state){
        val job = coroutineScope.launch {
            Log.d("DEBOUNCE", "on start")

            delay(delayMillis)
            Log.d("DEBOUNCE", "on stop")
            onChange(state)
        }
        onDispose {
            job.cancel()
        }
    }
    return state
}


