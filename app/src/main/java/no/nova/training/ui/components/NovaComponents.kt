package no.nova.training.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.outlined.FitnessCenter
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import no.nova.training.ui.theme.*

data class BottomItem(val key: String, val label: String, val icon: ImageVector)

val BottomItems = listOf(
    BottomItem("program", "Program", Icons.Outlined.FitnessCenter),
    BottomItem("transfer", "Overfør", Icons.Default.QrCodeScanner),
    BottomItem("more", "Mer", Icons.Default.MoreHoriz)
)

@Composable fun NovaHeader(title: String? = null, trailing: (@Composable () -> Unit)? = null) {
    Row(Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 14.dp), verticalAlignment = Alignment.CenterVertically) {
        Text(title ?: "N O V A", fontSize = if (title == null) 25.sp else 21.sp, fontWeight = if (title == null) FontWeight.Light else FontWeight.SemiBold, color = NovaText)
        Spacer(Modifier.weight(1f))
        trailing?.invoke()
    }
}

@Composable fun NovaCard(modifier: Modifier = Modifier, onClick: (() -> Unit)? = null, content: @Composable ColumnScope.() -> Unit) {
    val shape = RoundedCornerShape(16.dp)
    val base = modifier.background(NovaSurface, shape).then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)
    Column(base.fillMaxWidth().padding(14.dp), content = content)
}

@Composable fun SegmentedPill(text: String, selected: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier.height(44.dp).clickable(onClick = onClick),
        color = if (selected) NovaBlue else NovaSurface2,
        shape = RoundedCornerShape(11.dp),
        border = BorderStroke(1.dp, if (selected) NovaBlue else NovaBorder)
    ) {
        Box(contentAlignment = Alignment.Center) { Text(text, fontSize = 13.sp, fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium) }
    }
}

@Composable fun NovaBottomBar(selected: String, onSelect: (String) -> Unit) {
    NavigationBar(containerColor = NovaBackground, tonalElevation = 0.dp) {
        BottomItems.forEach { item ->
            NavigationBarItem(
                selected = selected == item.key,
                onClick = { onSelect(item.key) },
                icon = { Icon(item.icon, null) },
                label = { Text(item.label, fontSize = 11.sp) },
                colors = NavigationBarItemDefaults.colors(selectedIconColor = NovaBlue, selectedTextColor = NovaBlue, indicatorColor = NovaSurface2, unselectedIconColor = NovaMuted, unselectedTextColor = NovaMuted)
            )
        }
    }
}
