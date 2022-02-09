package com.au.gonannysingleactivity.activities

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.au.gonannysingleactivity.R
import com.au.gonannysingleactivity.fragments.*
import com.au.gonannysingleactivity.objects.Constants
import com.au.gonannysingleactivity.objects.Constants.KEY_FOR_ID
import com.au.gonannysingleactivity.utils.ApplicationGlobal
import com.au.gonannysingleactivity.utils.ApplicationGlobal.Companion.fromNotification

class CommonReplacementActivity : AppCompatActivity() {

    private var fragment: Fragment? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_common_replacement)

        //styling the activity
        window.statusBarColor = getColor(R.color.appBackgroundColor)

        // replacing fragments
        when (intent.getStringExtra(Constants.KEY_FOR_FRAGMENT_REPLACEMENT)) {

            Constants.KEY_SELECT_DATE_TIME -> fragment = SelectDateTimeFragment().apply {
                arguments = Bundle().apply {
                    putString("selectedDate", intent.getStringExtra("selectedDate"))
                }
            }

            Constants.KEY_SELECT_DURATION -> fragment = SelectDurationFragment().apply {
                arguments = Bundle().apply {
                    putString("dateOne", intent.getStringExtra(Constants.KEY_SELECT_DATE_TIME))
                    putInt("selectedDuration", intent.getIntExtra("selectedDuration", 8))
                    putString("selectedStartTime", intent.getStringExtra("selectedStartTime"))
                    putString("selectedEndTime", intent.getStringExtra("selectedEndTime"))
                }
            }

            Constants.KEY_SELECT_KIDS -> fragment = SelectKidsFragment()

            Constants.KEY_SELECT_ADDRESS -> fragment = AddressesFragment()

            Constants.KEY_GET_PRICING -> fragment = GetPricingFragment().apply {
                arguments = Bundle().apply {
                    putInt(KEY_FOR_ID, intent.getIntExtra(KEY_FOR_ID, 0))
                }
            }

            Constants.KEY_SHOW_PROFILE -> fragment = ShowProfileFragment()

            // settings fragments
            Constants.KEY_SETTING_KIDS -> fragment = SelectKidsFragment.fromSettingKids()

            Constants.KEY_SETTING_USED_HOUR -> fragment = UsedHourFragment()

            Constants.KEY_SETTING_ADDRESSES -> fragment = AddressesFragment.fromSettingFragment()

            Constants.KEY_SETTINGS_CARD -> fragment = CardsFragment()

            Constants.KEY_SETTING_CHANGE_PASSWORD -> fragment = ChangePasswordFragment()

            Constants.KEY_SETTING_CONTACT_US -> fragment = ContactUsFragment()

            Constants.KEY_FAQ -> fragment = FaqFragment()

            Constants.KEY_SHOW_BOOKING_DETAIL -> fragment =
                    ShowBookingDetailFragment.newInstance(intent.getIntExtra("booking_id", 0))

            Constants.KEY_CHAT_FRAGMENT -> {
                ApplicationGlobal.mapNotification.apply {
                    fragment = ChatsFragment.newInstance(
                        this["booking_id"].toString().toInt(),
                        this["from_user_id"].toString().toInt(),
                        this["from_user_image"].toString(),
                        this["from_user_first_name"].toString()
                    )
                }
            }
            Constants.KEY_ADD_WWCC_FRAGMENT-> fragment = AddWWCCFragment()

            Constants.KEY_EARNINGS_FRAGMENT -> fragment = EarningsFragment()
            Constants.KEY_WORKING_HOUR_FRAGMENT -> fragment = WorkingHoursFragment()
            Constants.KEY_DOCUMENTS_FRAGMENT -> fragment = ShowDocumentsFragment()
            Constants.KEY_PHOTOS_FRAGMENT -> fragment = NannyPhotosFragment().apply {
                arguments =Bundle().apply {
                    putBoolean("fromSetting",true)
                }
            }
            Constants.KEY_LINKED_BANK_ACC_FRAGMENT -> fragment = LinekdBankAccountFragment()

        }

        supportFragmentManager.beginTransaction()
            .add(R.id.flCommonReplacement, fragment!!).commit()

    }

}