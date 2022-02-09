package com.au.gonannysingleactivity.fragments

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import com.au.gonannysingleactivity.R
import com.au.gonannysingleactivity.objects.Constants.ADMIN_EMAIL_ADDRESS
import com.au.gonannysingleactivity.utils.Extensions.Companion.showToast
import kotlinx.android.synthetic.main.frgament_setting_contact_us.*


class ContactUsFragment : BaseFragment() {

    override fun getLayoutToInflate(): Int {
        return R.layout.frgament_setting_contact_us
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setUpToolbar("Contact Us", true, null) {}

        btnSubmit.setOnClickListener {
            val subject = etSubject.text.trim().toString()
            if (subject.isBlank() && subject.length < 20) {
                getString(R.string.warning_enter_subject).showToast(requireContext())
            } else {
                val intent = Intent(Intent.ACTION_SENDTO)
                intent.apply {
                    data = Uri.parse("mailto:")
                    putExtra(Intent.EXTRA_EMAIL, arrayOf(ADMIN_EMAIL_ADDRESS))
                    putExtra(Intent.EXTRA_SUBJECT, subject)
                }
                contractSendEmail.launch(intent)
            }
        }
    }

    private val contractSendEmail =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            getString(R.string.sending).showToast(requireContext())
        }

}