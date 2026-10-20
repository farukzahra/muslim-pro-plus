package com.farukzahra.muslimplus

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import com.farukzahra.muslimplus.ui.MuslimPlusApp
import com.farukzahra.muslimplus.ui.theme.MuslimPlusTheme

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MuslimPlusTheme {
                MuslimPlusApp()
            }
        }
    }
}
