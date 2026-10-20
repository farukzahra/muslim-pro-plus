package com.farukzahra.muslimplus.ui.nav

import androidx.annotation.StringRes
import com.farukzahra.muslimplus.R

enum class Tab(val route: String, @StringRes val labelRes: Int) {
    TODAY("today", R.string.tab_today),
    PRAYERS("prayers", R.string.tab_prayers),
    QURAN("quran", R.string.tab_quran),
    QIBLA("qibla", R.string.tab_qibla),
    MORE("more", R.string.tab_more),
}
