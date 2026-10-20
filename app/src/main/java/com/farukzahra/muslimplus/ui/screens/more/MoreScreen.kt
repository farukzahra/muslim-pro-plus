package com.farukzahra.muslimplus.ui.screens.more

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.farukzahra.muslimplus.R

@Composable
fun MoreScreen(onLanguageClick: () -> Unit) {
    Column(modifier = Modifier.fillMaxSize()) {
        ListItem(
            headlineContent = { Text(stringResource(R.string.more_language)) },
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onLanguageClick)
                .padding(horizontal = 4.dp),
        )
        HorizontalDivider()
        ListItem(
            headlineContent = { Text(stringResource(R.string.more_about)) },
            supportingContent = {
                Text(
                    text = stringResource(R.string.about_version, "0.1.0"),
                    style = MaterialTheme.typography.bodySmall,
                )
            },
        )
    }
}
