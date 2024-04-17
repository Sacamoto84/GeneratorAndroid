package com.example.generator2.features.presets

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import org.burnoutcrew.reorderable.ReorderableItem
import org.burnoutcrew.reorderable.detectReorderAfterLongPress
import org.burnoutcrew.reorderable.rememberReorderableLazyListState
import org.burnoutcrew.reorderable.reorderable

@Preview
@Composable
fun preview() {
    PresetsDraggableList()
}

@Composable
fun PresetsDraggableList() {


    val data = remember { mutableStateOf(List(100) { "Item $it" }) }

    val state = rememberReorderableLazyListState(

        onMove = { from, to ->
            data.value = data.value.toMutableList().apply {
                add(to.index, removeAt(from.index))
            }

        })

    var rating: Float by remember { mutableFloatStateOf(3.2f) }


    LazyColumn(
        state = state.listState,
        modifier = Modifier
            .reorderable(state)
            .detectReorderAfterLongPress(state)
    ) {
        items(data.value, { it }) { item ->

            ReorderableItem(state, key = item) { isDragging ->

                val elevation = animateDpAsState(if (isDragging) 16.dp else 0.dp, label = "")

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(elevation.value)
                        .background(Color.Gray),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {

                    Text(item)

                }
            }
        }
    }

}