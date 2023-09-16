package com.example.thingsfortrip.views

import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.SemanticsProperties.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.thingsfortrip.R
import com.example.thingsfortrip.data.models.Thing
import com.example.thingsfortrip.data.repositories.local.AppDatabase
import com.example.thingsfortrip.data.repositories.local.AppRepository
import com.example.thingsfortrip.model.ListsContract
import com.example.thingsfortrip.model.ThingsListContract
import com.example.thingsfortrip.view_models.ThingsListViewModel

@Composable
fun ThingsList(listId: Int){
    val context = LocalContext.current
    val viewModel: ThingsListViewModel = viewModel()
    val database by lazy { AppDatabase.getDatabase(context) }
    val repository by lazy { AppRepository(database.createDAO()) }
    viewModel.appRepository = repository
    viewModel.setEvent(ThingsListContract.Event.LoadThingsByListCode(listId))

    var deleteAlertDialogIsShown = remember { mutableStateOf(-1) }
    val localFocusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current
    Scaffold(
        modifier = Modifier.padding(
            horizontal = 8.dp,
            vertical = 16.dp
        )) {
        paddingValues ->
        Surface(
            modifier = Modifier
                .padding(paddingValues)
        ) {

            Column(
                modifier = Modifier
                .pointerInput(Unit) {
                detectTapGestures(onTap = {
                    localFocusManager.clearFocus()
                })
            }
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
                    items(viewModel.things){
                        ThingListItemDeleted(thing = it, onCheckBoxChange = {
                            viewModel.setEvent(ThingsListContract.Event.ChangeThingStatus(it))
                        }) {
                            deleteAlertDialogIsShown.value = it.index
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
                        value = viewModel.newThingName.value,
                        label = {
                            Text(text = stringResource(id = R.string.input_new_thing_name))
                        },
                        onValueChange = {
                            viewModel.setEvent(ThingsListContract.Event.UpdateNewThingName(it))
                        },
                        keyboardOptions = KeyboardOptions(imeAction = androidx.compose.ui.text.input.ImeAction.Done),
                        keyboardActions = KeyboardActions(
                            onDone = { keyboardController?.hide() }),
                        modifier = Modifier
                            .padding(8.dp)
                    )
                    IconButton(
                        onClick = {
                            keyboardController?.hide()
                            if (viewModel.newThingName.value.trim().isEmpty()) {
                                Toast.makeText(context, context.getString(R.string.input_new_list_name), Toast.LENGTH_SHORT).show()
                            } else {
                               viewModel.setEvent(ThingsListContract.Event.SaveNewThing)
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
                Text(text = stringResource(id = R.string.thing_delete_confirmation))
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.setEvent(ThingsListContract.Event.DeleteThing(deleteAlertDialogIsShown.value))
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
    LaunchedEffect(Unit){
        viewModel.uiState.collect{
            when(it){
                ThingsListContract.State.Init -> {

                }
                is ThingsListContract.State.ShowToast -> {
                    Toast.makeText(context, context.getString(it.stringResource), Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}


@Composable
fun ThingListItemDeleted(thing: Thing, onCheckBoxChange: () -> Unit, onDelete: () -> Unit){
    Log.d("ThingListItemDeleted", "create ThingListItemDeleted view")
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier
            .fillMaxWidth()
    ) {
       Row(
           verticalAlignment = Alignment.CenterVertically
       ) {
           var isSelect by remember {
               mutableStateOf(thing.isChecked)
           }
           Checkbox(
               checked = isSelect,
               onCheckedChange = {
                   isSelect = it
                   onCheckBoxChange()
               }
           )
           Text(
               text = thing.name,
               fontSize = 16.sp
           )
        }
        IconButton(onClick = {
            onDelete()
        }) {
            Icon(imageVector = Icons.Default.Delete, contentDescription = null)
        }

    }

}
