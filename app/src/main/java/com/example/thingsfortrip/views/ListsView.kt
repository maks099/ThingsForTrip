package com.example.thingsfortrip.views

import android.widget.Toast
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SelectableDates
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.thingsfortrip.R
import com.example.thingsfortrip.data.repositories.local.AppDatabase
import com.example.thingsfortrip.data.repositories.local.AppRepository
import com.example.thingsfortrip.model.ListsContract
import com.example.thingsfortrip.model.MainContract
import com.example.thingsfortrip.view_models.ListsViewModel
import java.text.SimpleDateFormat
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ListsView(navController: NavController){

    val context = LocalContext.current
    val database by lazy { AppDatabase.getDatabase(context) }
    val repository by lazy { AppRepository(database.createDAO()) }
    val dateDialogIsShown = remember { mutableStateOf(false) }
    val viewModel: ListsViewModel = viewModel()
    viewModel.appRepository = repository
    viewModel.setEvent(ListsContract.Event.LoadLists)

    var deleteAlertDialogIsShown = remember { mutableStateOf(-1) }
    Scaffold(
        modifier = Modifier
            .padding(
                horizontal = 16.dp,
                vertical = 8.dp
            ),
    ) {
        paddingValues ->
        Surface(
            modifier = Modifier
                .padding(paddingValues)
        ) {
            Column(
                modifier = Modifier.fillMaxSize()
            ) {
                LazyColumn(
                    modifier = Modifier
                        .border(
                            width = 1.dp,
                            color = Color.Gray,
                            shape = RoundedCornerShape(16.dp)
                        )
                        .weight(2f)
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                ){
                    items(viewModel.lists){
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    navController.navigate(
                                        "things_list/{listId}"
                                            .replace(
                                                oldValue = "{listId}",
                                                newValue = "${it.id}"
                                            )
                                    )
                            }
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,

                            ) {
                                Text(
                                    text = it.name,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                val format = SimpleDateFormat("dd/MM/yyy")
                                Text(
                                    text = format.format(it.time),
                                    modifier = Modifier
                                        .padding(8.dp)
                                )
                            }
                            IconButton(onClick = {
                                deleteAlertDialogIsShown.value = it.id
                            }) {
                                Icon(imageVector = Icons.Filled.Delete, contentDescription = null)
                            }
                        }
                    }
                }

                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                        .border(
                            width = 1.dp,
                            color = Color.Gray,
                            shape = RoundedCornerShape(16.dp)
                        )
                ) {
                    OutlinedTextField(
                        value = viewModel.newListName.value,
                        label = {
                            Text(text = stringResource(id = R.string.input_new_list_name))
                        },
                        onValueChange = {
                            viewModel.setEvent(ListsContract.Event.OnNewListNameInput(it))
                        },
                        modifier = Modifier
                            .padding(8.dp)
                    )
                    IconButton(
                        onClick = {
                            if (viewModel.newListName.value.trim().isEmpty()) {
                                Toast.makeText(context, context.getString(R.string.input_list_name), Toast.LENGTH_SHORT).show()
                            } else {
                                dateDialogIsShown.value = true
                            }
                        }
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.baseline_save_24),
                            contentDescription = null,
                            modifier = Modifier
                                .size(32.dp)
                        )
                    }
                }
            }
        }
    }

    if(deleteAlertDialogIsShown.value > -1){
        AlertDialog(
            onDismissRequest = {
                deleteAlertDialogIsShown.value = -1
            },
            title = {
                Text(text = stringResource(id = R.string.alert))
            },
            text = {
                Text(text = stringResource(id = R.string.list_delete_confirmation))
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.setEvent(ListsContract.Event.OnDeleteList(deleteAlertDialogIsShown.value))
                        deleteAlertDialogIsShown.value = -1
                    }) {
                    Text(stringResource(id = R.string.ok))
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        deleteAlertDialogIsShown.value = -1
                    }) {
                    Text(stringResource(id = R.string.cancel))
                }
            }
        )
    }

    if(dateDialogIsShown.value) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = Calendar.getInstance().timeInMillis,
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
                            ListsContract.Event.OnSaveNewList(datePickerState.selectedDateMillis ?: 0)
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
    LaunchedEffect(Unit){
        viewModel.uiState.collect{
            when(it){
                ListsContract.State.Initial -> {

                }
                is ListsContract.State.ShowToast -> Toast.makeText(context, context.getString(it.stringResource), Toast.LENGTH_SHORT).show()
            }
        }
    }
}