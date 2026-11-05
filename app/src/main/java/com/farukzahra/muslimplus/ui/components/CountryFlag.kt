package com.farukzahra.muslimplus.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.farukzahra.muslimplus.adhan.AdhanCountry

/**
 * Bandeira pequena usada nas listas de vozes. Sem país, cai num quadrado com
 * uma nota musical, para a linha não perder o alinhamento.
 */
@Composable
fun CountryFlag(country: AdhanCountry?, modifier: Modifier = Modifier) {
    if (country == null) {
        Box(
            modifier = modifier
                .size(width = 24.dp, height = 16.dp)
                .clip(RoundedCornerShape(3.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = "♪",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    } else {
        Image(
            painter = painterResource(country.flagRes),
            contentDescription = stringResource(country.labelRes),
            modifier = modifier
                .size(width = 24.dp, height = 16.dp)
                .clip(RoundedCornerShape(3.dp)),
            contentScale = ContentScale.Crop,
        )
    }
}
