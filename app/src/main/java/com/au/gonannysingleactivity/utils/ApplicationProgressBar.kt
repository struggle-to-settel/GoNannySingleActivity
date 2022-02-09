package com.au.gonannysingleactivity.utils

import android.content.Context
import android.view.View
import android.widget.ProgressBar
import android.widget.RelativeLayout
import com.au.gonannysingleactivity.R

class ApplicationProgressBar() {

    companion object {

        fun get(context: Context): ProgressBar {
            val params = RelativeLayout
                .LayoutParams(80, 80)
            params.addRule(RelativeLayout.CENTER_IN_PARENT)

            return ProgressBar(context).apply {
                layoutParams = params
                visibility = View.GONE
            }
        }

    }

}