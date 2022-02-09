package com.au.gonannysingleactivity.utils

import android.content.Context
import android.widget.Toast
import kotlin.math.round

class Extensions {

    companion object {
        fun String.showToast(context: Context) {
            Toast.makeText(context, this, Toast.LENGTH_SHORT).show()
        }

        fun Float.round(decimals: Int): Float {
            var multiplier = 1.0
            repeat(decimals) { multiplier *= 10 }
            return (round(this * multiplier) / multiplier).toFloat()
        }
    }
}