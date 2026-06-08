package com.example.friendzone.presentation.events

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.friendzone.domain.result.ApiResult

@Composable
fun EventsScreen(viewModel: EventsViewModel) {
    val eventsState by viewModel.eventsState.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.loadEvents()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text("My Events", style = MaterialTheme.typography.headlineMedium)

        when (val state = eventsState) {
            ApiResult.Loading -> CircularProgressIndicator()
            is ApiResult.Error -> Text(
                viewModel.errorMessage().orEmpty(),
                color = MaterialTheme.colorScheme.error,
            )
            is ApiResult.Success -> {
                if (state.data.isEmpty()) {
                    Text("No events yet.")
                } else {
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(state.data) { event ->
                            Text("${event.title} • ${event.status.name.lowercase()}")
                        }
                    }
                }
            }
        }
    }
}
