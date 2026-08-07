package com.learning.mychatbotapp

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Login
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocalOffer
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Store
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.outlined.QuestionAnswer
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.learning.mychatbotapp.ui.theme.AccentBlueLight

// Menú Principal: grid de tarjetas (una por sección del FAQ) + "Pregunta directa" fija abajo.
@Composable
fun MenuScreen(
    modifier: Modifier = Modifier,
    categories: List<CategoryItem>,
    onCategoryClick: (CategoryItem) -> Unit,
    onDirectQuestion: (String) -> Unit
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Text(
            modifier = Modifier.padding(20.dp),
            text = stringResource(R.string.main_menu),
            color = MaterialTheme.colorScheme.onBackground,
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold
        )

        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(categories) { category ->
                CategoryCard(category = category, onClick = { onCategoryClick(category) })
            }
        }

        DirectQuestionBar(onSend = onDirectQuestion)
    }
}

@Composable
private fun CategoryCard(category: CategoryItem, onClick: () -> Unit) {
    Column(
        modifier = Modifier
            .aspectRatio(1.15f)
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surface)
            .clickable { onClick() }
            .padding(14.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Box(
            modifier = Modifier
                .size(38.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.background),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = iconForSection(category.section),
                contentDescription = category.section,
                tint = AccentBlueLight,
                modifier = Modifier.size(20.dp)
            )
        }
        Text(
            text = category.section,
            color = MaterialTheme.colorScheme.onSurface,
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun DirectQuestionBar(onSend: (String) -> Unit) {
    var text by remember { mutableStateOf("") }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        OutlinedTextField(
            modifier = Modifier.weight(1f),
            value = text,
            onValueChange = { text = it },
            placeholder = { Text(stringResource(R.string.direct_question), color = MaterialTheme.colorScheme.onSurfaceVariant) },
            shape = RoundedCornerShape(24.dp),
            colors = OutlinedTextFieldDefaults.colors(
                unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                unfocusedBorderColor = MaterialTheme.colorScheme.surfaceVariant,
                focusedBorderColor = AccentBlueLight
            )
        )
        IconButton(
            modifier = Modifier
                .padding(start = 8.dp)
                .clip(CircleShape)
                .background(AccentBlueLight),
            onClick = {
                if (text.isNotBlank()) {
                    onSend(text)
                    text = ""
                }
            }
        ) {
            Icon(imageVector = Icons.AutoMirrored.Filled.Send, contentDescription = stringResource(R.string.send), tint = Color.White)
        }
    }
}

// Mapea cada sección del FAQ a un ícono representativo.
private fun iconForSection(section: String): ImageVector = when (section) {
    "Información General" -> Icons.Default.Info
    "Registro e Inicio de Sesión" -> Icons.AutoMirrored.Filled.Login
    "Mi Perfil" -> Icons.Default.Person
    "Negocios" -> Icons.Default.Store
    "Búsqueda y Exploración" -> Icons.Default.Search
    "Favoritos" -> Icons.Default.Favorite
    "Calificaciones y Comentarios" -> Icons.Default.Star
    "Ofertas Push" -> Icons.Default.LocalOffer
    "Notificaciones" -> Icons.Default.Notifications
    "Modo Invitado" -> Icons.Default.VisibilityOff
    "Administración (Para administradores)" -> Icons.Default.AdminPanelSettings
    "Problemas Técnicos" -> Icons.Default.Build
    "Datos del ISTG" -> Icons.Default.School
    else -> Icons.Outlined.QuestionAnswer
}
