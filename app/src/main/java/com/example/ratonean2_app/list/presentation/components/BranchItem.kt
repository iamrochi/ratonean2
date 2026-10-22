package com.example.ratonean2_app.list.presentation.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.ratonean2_app.branch.domain.helper.assignImageToCommerce
import com.example.ratonean2_app.branch.domain.model.Branch

@Composable
fun BranchItem(
    branch: Branch,
    onClick: () -> Unit = {}
) {
    // Card(onClick): ripple M3 + Role.Button gratis (antes clickable sin ripple).
    // Sin altura fija: 70dp recortaba imagen (60dp) + padding + texto.
    Card(
        onClick = onClick,
        modifier = Modifier
            .heightIn(min = 96.dp)
            .widthIn(min = 90.dp),
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.onSurface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(
                branch.name,
                style = MaterialTheme.typography.titleMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            AsyncImage(
                model = assignImageToCommerce(branch.commerceId),
                // Decorativa: el nombre ya está en el Text (evita doble anuncio).
                contentDescription = null,
                modifier = Modifier
                    .size(60.dp)
                    .padding(top = 8.dp)
            )
        }
    }
}
