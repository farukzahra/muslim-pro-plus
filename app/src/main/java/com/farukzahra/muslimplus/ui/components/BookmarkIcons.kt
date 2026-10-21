package com.farukzahra.muslimplus.ui.components

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.PathData
import androidx.compose.ui.unit.dp

/**
 * Marcador do leitor. `material-icons-core` (a unica dependencia de icones do
 * projeto) nao traz Bookmark, e puxar o extended inteiro por um icone custa
 * ~1 MB no APK: o vetor e desenhado aqui.
 *
 * O `tint` do `Icon` recolore tanto o preenchimento quanto o contorno.
 */
val BookmarkFilled: ImageVector by lazy { bookmark("BookmarkFilled", filled = true) }

val BookmarkOutline: ImageVector by lazy { bookmark("BookmarkOutline", filled = false) }

private fun bookmark(name: String, filled: Boolean): ImageVector {
    val path = PathData {
        moveTo(6f, 3f)
        horizontalLineTo(18f)
        verticalLineTo(21f)
        lineTo(12f, 17f)
        lineTo(6f, 21f)
        close()
    }
    val builder = ImageVector.Builder(
        name = name,
        defaultWidth = 24.dp,
        defaultHeight = 24.dp,
        viewportWidth = 24f,
        viewportHeight = 24f,
    )
    if (filled) {
        builder.addPath(pathData = path, fill = SolidColor(Color.Black))
    } else {
        builder.addPath(
            pathData = path,
            fill = null,
            stroke = SolidColor(Color.Black),
            strokeLineWidth = 1.8f,
            strokeLineCap = StrokeCap.Round,
            strokeLineJoin = StrokeJoin.Round,
        )
    }
    return builder.build()
}
