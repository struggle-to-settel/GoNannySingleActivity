package com.au.gonannysingleactivity.fragments

import android.content.Intent
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.view.View
import androidx.core.content.ContextCompat.getColor
import com.au.gonannysingleactivity.R
import com.au.gonannysingleactivity.activities.CommonReplacementActivity
import com.au.gonannysingleactivity.objects.Constants
import com.au.gonannysingleactivity.objects.Constants.BASE_IMAGE_URL
import com.au.gonannysingleactivity.utils.ApplicationGlobal
import com.au.gonannysingleactivity.utils.ApplicationGlobal.Companion.userType
import com.au.gonannysingleactivity.utils.CommonFunctions.Companion.getColorForLatter
import com.au.gonannysingleactivity.utils.CommonFunctions.Companion.showLogOutDialog
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.Target.SIZE_ORIGINAL
import kotlinx.android.synthetic.main.fragment_settings.*

class SettingsFragment : BaseFragment(), View.OnClickListener {

    private lateinit var intent: Intent
    private var key: String = ""

    override fun getLayoutToInflate(): Int {
        return R.layout.fragment_settings
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        intent = Intent(requireActivity(), CommonReplacementActivity::class.java)

        if (userType == 3) {
            tvKids.text = getString(R.string.photos)
            tvKids.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_photos, 0)
            tvCards.text = getString(R.string.documents)
            tvCards.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_documents, 0)
        } else {
            tvWorkingHours.visibility = View.GONE
            tvEarnings.visibility = View.GONE
            tvLinkedBankAccount.visibility = View.GONE
        }

        tvShowProfile.setOnClickListener(this)
        tvKids.setOnClickListener(this)
        tvUsedHours.setOnClickListener(this)
        tvAddresses.setOnClickListener(this)
        tvCards.setOnClickListener(this)
        tvChangePassword.setOnClickListener(this)
        tvContactUs.setOnClickListener(this)
        tvFaq.setOnClickListener(this)
        tvLogOut.setOnClickListener(this)
        tvWorkingHours.setOnClickListener(this)
        tvEarnings.setOnClickListener(this)
        tvLinkedBankAccount.setOnClickListener(this)
    }

    override fun onResume() {
        super.onResume()
        ApplicationGlobal.userObject?.apply {
            tvName.text = "$first_name $last_name"
            if (profile_image == null) {
                tvProfilePic.visibility = View.VISIBLE
                (ivProfilePic.background as GradientDrawable).setColor(
                    getColor(
                        requireContext(),
                        getColorForLatter(first_name.toString()[0].lowercase())
                    )
                )
                tvProfilePic.text = first_name.toString()[0].uppercase()
            } else {
                tvProfilePic.visibility = View.GONE
                Glide.with(requireContext()).load("$BASE_IMAGE_URL$profile_image").apply(
                    RequestOptions().circleCrop().override(SIZE_ORIGINAL)
                        .placeholder(R.drawable.ic_profile_large)
                ).into(ivProfilePic)
            }
        }
    }

    override fun onClick(v: View?) {
        when (v!!.id) {
            R.id.tvShowProfile -> startActivity(
                intent.putExtra(
                    Constants.KEY_FOR_FRAGMENT_REPLACEMENT,
                    Constants.KEY_SHOW_PROFILE
                )
            )
            R.id.tvKids ->
                startActivity(
                    intent.putExtra(
                        Constants.KEY_FOR_FRAGMENT_REPLACEMENT,
                        if(userType==3) Constants.KEY_PHOTOS_FRAGMENT else Constants.KEY_SETTING_KIDS
                    )
                )

            R.id.tvUsedHours ->
                startActivity(
                    intent.putExtra(
                        Constants.KEY_FOR_FRAGMENT_REPLACEMENT,
                        Constants.KEY_SETTING_USED_HOUR
                    )
                )
            R.id.tvAddresses ->
                startActivity(
                    intent.putExtra(
                        Constants.KEY_FOR_FRAGMENT_REPLACEMENT,
                        Constants.KEY_SETTING_ADDRESSES
                    )
                )
            R.id.tvCards -> startActivity(
                intent.putExtra(
                    Constants.KEY_FOR_FRAGMENT_REPLACEMENT,
                    if(userType==3) Constants.KEY_DOCUMENTS_FRAGMENT else Constants.KEY_SETTINGS_CARD
                )
            )
            R.id.tvChangePassword -> startActivity(
                intent.putExtra(
                    Constants.KEY_FOR_FRAGMENT_REPLACEMENT,
                    Constants.KEY_SETTING_CHANGE_PASSWORD
                )
            )
            R.id.tvContactUs ->
                startActivity(
                    intent.putExtra(
                        Constants.KEY_FOR_FRAGMENT_REPLACEMENT,
                        Constants.KEY_SETTING_CONTACT_US
                    )
                )
            R.id.tvFaq -> startActivity(
                intent.putExtra(
                    Constants.KEY_FOR_FRAGMENT_REPLACEMENT,
                    Constants.KEY_FAQ
                )
            )
            R.id.tvLogOut -> {
                showLogOutDialog(
                    requireContext(),
                    requireActivity()
                )

            }
            R.id.tvWorkingHours -> startActivity(
                intent.putExtra(
                    Constants.KEY_FOR_FRAGMENT_REPLACEMENT,
                    Constants.KEY_WORKING_HOUR_FRAGMENT
                )
            )
            R.id.tvEarnings -> startActivity(
                intent.putExtra(
                    Constants.KEY_FOR_FRAGMENT_REPLACEMENT,
                    Constants.KEY_EARNINGS_FRAGMENT
                )
            )
            R.id.tvLinkedBankAccount -> startActivity(
                intent.putExtra(
                    Constants.KEY_FOR_FRAGMENT_REPLACEMENT,
                    Constants.KEY_LINKED_BANK_ACC_FRAGMENT
                )
            )
        }

    }

}