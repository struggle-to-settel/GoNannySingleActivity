package com.au.gonannysingleactivity.fragments

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.text.SpannableString
import android.text.style.ClickableSpan
import android.text.style.ForegroundColorSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat.getColor
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import com.au.gonannysingleactivity.R
import com.au.gonannysingleactivity.webservices.Kid
import com.au.gonannysingleactivity.webservices.NannyData
import kotlinx.android.synthetic.main.common_toolbar.*

abstract class BaseFragment() : Fragment() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (registerFirebaseBroadcastReceiver())
            requireActivity().registerReceiver(broadcastReceiver, IntentFilter("firebase_message"))
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        setTheme(requireContext())
        return inflater.inflate(getLayoutToInflate(), container, false)
    }


    open fun onFirebaseMessageReceived(data: Bundle) {}

    open fun registerFirebaseBroadcastReceiver(): Boolean = false

    override fun onDestroy() {
        super.onDestroy()
        if(registerFirebaseBroadcastReceiver()) {
            requireActivity().unregisterReceiver(broadcastReceiver)
        }
    }

    private var broadcastReceiver = object : BroadcastReceiver() {

        override fun onReceive(context: Context?, intent: Intent?) {
            onFirebaseMessageReceived(intent!!.getBundleExtra("data")!!)
        }
    }

    abstract fun getLayoutToInflate(): Int

    protected open fun setTheme(context: Context) {

    }

    companion object {
        var kidsList: MutableList<Kid> = mutableListOf()
        var listIds = arrayListOf<Int>()
        var nannyData: NannyData? = null
    }


    protected open fun setUpToolbar(
        title: String,
        isBackIcon: Boolean,
        endTextView: String?,
        clickListener: View.OnClickListener?
    ) {
        applicationToolbar.apply {
            this.title = title
            if (isBackIcon) {
                setNavigationIcon(R.drawable.ic_back)
                setNavigationOnClickListener {
                    if (modifyBackIcon())
                        modifyBackIconFunctionality()
                    else
                        requireActivity().onBackPressed()
                }
            } else {
                navigationIcon = null
            }
            setTitleTextAppearance(requireContext(), R.style.toolbarTextFonts)
            setTitleTextColor(
                resources.getColor(
                    R.color.oftenTextColors,
                    requireContext().theme
                )
            )
        }
        if (endTextView == null) {
            tvToolbarNavigation.visibility = View.GONE
        } else {
            tvToolbarNavigation.text = endTextView
            tvToolbarNavigation.setOnClickListener(clickListener)
        }
    }


    protected open fun modifyBackIcon(): Boolean = false

    protected open fun modifyBackIconFunctionality() {}

    protected fun addFragment(layoutId: Int, fragment: Fragment, isBackstack: Boolean) {
        if (isBackstack) {
            requireActivity().supportFragmentManager.beginTransaction()
                .add(layoutId, fragment).addToBackStack(null).commit()
        } else {
            requireActivity().supportFragmentManager.beginTransaction()
                .add(layoutId, fragment).commit()
        }
    }

    protected fun replaceFragment(layoutId: Int, fragment: Fragment, isBackstack: Boolean) {
        if (isBackstack) {
            requireActivity().supportFragmentManager.beginTransaction()
                .replace(layoutId, fragment)
                .addToBackStack(null).commit()
        } else {
            requireActivity().supportFragmentManager.beginTransaction()
                .replace(layoutId, fragment).commit()
        }
    }

    protected fun getSpannableString(
        stringId: Int,
        string: String,
        colorId: Int,
        clickAction: ClickableSpan,
    ): SpannableString {

        val spannableString = SpannableString(getString(stringId))
        val foreGroundColor =
            ForegroundColorSpan(resources.getColor(colorId, resources.newTheme()))

        spannableString.setSpan(
            clickAction, 0,
            getString(stringId).length,
            SpannableString.SPAN_EXCLUSIVE_EXCLUSIVE
        )

        spannableString.setSpan(
            ForegroundColorSpan(resources.getColor(R.color.oftenTextColors, resources.newTheme())),
            0,
            spannableString.indexOf(string),
            SpannableString.SPAN_EXCLUSIVE_EXCLUSIVE
        )

        spannableString.setSpan(
            foreGroundColor,
            spannableString.indexOf(string),
            spannableString.indexOf(string) + string.length,
            SpannableString.SPAN_EXCLUSIVE_EXCLUSIVE
        )

        return spannableString
    }

    protected fun getSpannableString(
        stringId: Int,
        stringOne: String,
        stringTwo: String,
        colorIdOne: Int,
        colorIdTwo: Int,
        clickActionOne: ClickableSpan,
        clickActionTwo: ClickableSpan,
    ): SpannableString {

        val spannableString = SpannableString(getString(stringId))
        val foreGroundColorOne = ForegroundColorSpan(getColor(requireContext(), colorIdOne))
        val foreGroundColorTwo = ForegroundColorSpan(getColor(requireContext(), colorIdTwo))

        spannableString.setSpan(
            foreGroundColorOne,
            spannableString.indexOf(stringOne),
            spannableString.indexOf(stringOne) + stringOne.length,
            SpannableString.SPAN_EXCLUSIVE_EXCLUSIVE
        )

        spannableString.setSpan(
            foreGroundColorTwo,
            spannableString.indexOf(stringTwo),
            spannableString.indexOf(stringTwo) + stringTwo.length,
            SpannableString.SPAN_EXCLUSIVE_EXCLUSIVE
        )


        spannableString.setSpan(
            clickActionOne, spannableString.indexOf(stringOne),
            spannableString.indexOf(stringOne) + stringOne.length,
            SpannableString.SPAN_EXCLUSIVE_EXCLUSIVE
        )

        spannableString.setSpan(
            clickActionTwo, spannableString.indexOf(stringTwo),
            spannableString.indexOf(stringTwo) + stringTwo.length,
            SpannableString.SPAN_EXCLUSIVE_EXCLUSIVE
        )

        return spannableString

    }

    protected fun clearStack() {
        requireActivity().supportFragmentManager.popBackStack(
            null,
            FragmentManager.POP_BACK_STACK_INCLUSIVE
        )
    }
}