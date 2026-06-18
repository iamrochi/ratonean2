package com.example.ratonean2_app.list.presentation.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import com.example.ratonean2_app.R
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun KeywordRow(
    keywords: List<String>,
    onKeywordClick: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        Text(
            text = "Búsquedas rápidas",
            style = MaterialTheme.typography.titleMedium
        )

        val chunkedKeywords = keywords.chunked(2) // 2 keywords por columna

        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(chunkedKeywords) { columnKeywords ->
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    columnKeywords.forEach { keyword ->
                        Card(
                            onClick = { onKeywordClick(keyword) },
                            modifier = Modifier
                                .height(70.dp)
                                .widthIn(min = 90.dp),
                            shape = MaterialTheme.shapes.medium,
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surface,
                                contentColor = MaterialTheme.colorScheme.onSurface
                            ),
                            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(8.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Text(
                                    text = keyword,
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        fontWeight = FontWeight.Bold
                                    )
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Icon(
                                    painter = painterResource(id = R.drawable.trendingup),
                                    contentDescription = "Tendencia",
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
