package com.au.gonannysingleactivity.activities

import android.app.Dialog
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import com.au.gonannysingleactivity.R
import com.au.gonannysingleactivity.fragments.*
import com.au.gonannysingleactivity.objects.Constants
import com.au.gonannysingleactivity.objects.Constants.BASE_IMAGE_URL
import com.au.gonannysingleactivity.objects.Constants.KEY_CHAT_FRAGMENT
import com.au.gonannysingleactivity.objects.Constants.KEY_FOR_FRAGMENT_REPLACEMENT
import com.au.gonannysingleactivity.objects.Constants.KEY_SHOW_BOOKING_DETAIL
import com.au.gonannysingleactivity.utils.ApplicationGlobal
import com.au.gonannysingleactivity.utils.ApplicationGlobal.Companion.fromNotification
import com.au.gonannysingleactivity.utils.ApplicationGlobal.Companion.isChanged
import com.au.gonannysingleactivity.utils.ApplicationGlobal.Companion.mapNotification
import com.au.gonannysingleactivity.utils.CommonFunctions
import com.au.gonannysingleactivity.utils.CommonFunctions.Companion.getColorForLatter
import com.au.gonannysingleactivity.utils.CommonFunctions.Companion.hideProgressBar
import com.au.gonannysingleactivity.utils.CommonFunctions.Companion.showLogOutDialog
import com.au.gonannysingleactivity.utils.CommonFunctions.Companion.showProgressBar
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DecodeFormat
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.Target.SIZE_ORIGINAL
import com.google.android.material.navigation.NavigationView
import kotlinx.android.synthetic.main.activity_home.*

class HomeActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {

    private val endSCALE: Float = 0.8f

    private var headerLayout: View? = null

    private val headerImageClickListener = View.OnClickListener {
        startActivity(
            Intent(
                this@HomeActivity,
                CommonReplacementActivity::class.java
            ).putExtra(KEY_FOR_FRAGMENT_REPLACEMENT, Constants.KEY_SHOW_PROFILE)
        )
    }


    override fun onCreate(savedInstanceState: Bundle?) {

        // push notification click event
        if (fromNotification) {
            when (mapNotification["notif_type"].toString().toInt()) {
                3 -> {
                    startActivity(Intent(this, CommonReplacementActivity::class.java).apply {
                        putExtra(KEY_FOR_FRAGMENT_REPLACEMENT, KEY_SHOW_BOOKING_DETAIL)
                        putExtra("booking_id", mapNotification["booking_id"].toString().toInt())
                    })
                }
                6 -> {
                    startActivity(Intent(this, CommonReplacementActivity::class.java).apply {
                        putExtra(KEY_FOR_FRAGMENT_REPLACEMENT, KEY_CHAT_FRAGMENT)
                    })
                }
            }
        }
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        // overview activity
        window.statusBarColor = getColor(R.color.appBackgroundColor)
        drawerLayout.drawerElevation = 0F
        contentView.setBackgroundResource(R.color.white)


        // header layout
        headerLayout = navigationViewHome.getHeaderView(0)
        headerLayout!!.findViewById<TextView>(R.id.tvCloseMenu).setOnClickListener { // close icon
            if (drawerLayout.isDrawerVisible(GravityCompat.START)) {
                drawerLayout.closeDrawer(GravityCompat.START)
            }
        }

        updateUserInfo(headerLayout!!) // updates user info

        // toolbar functionality
        toolbarHome.apply {

            setTitle("")
            setTitleTextAppearance(this@HomeActivity, R.style.toolbarTextFonts)

            // hamburger icon click listener
            setNavigationOnClickListener {
                if (drawerLayout.isDrawerVisible(GravityCompat.START)) {
                    drawerLayout.closeDrawer(GravityCompat.START)
                } else {
                    drawerLayout.openDrawer(GravityCompat.START)
                }
            }

            // notification icon at the end of toolbar
            ivNotification.setOnClickListener {
                supportFragmentManager.beginTransaction()
                    .add(R.id.flActivityHome, NotificationFragment()).commit()
                setTitle("Notifications")
                ivToolbarLogo.visibility = View.GONE
                ivNotification.visibility = View.GONE
                setItemChecked(false, false, false, true)
            }
        }

        navigationViewHome.setNavigationItemSelectedListener(this)
        navigationViewHome.setCheckedItem(R.id.menuHome)
        drawerLayout.setScrimColor(Color.TRANSPARENT)
        animateNavigationDrawer()

        // home fragment
        if (ApplicationGlobal.userType == 1) {
            supportFragmentManager.beginTransaction().add(
                R.id.flActivityHome,
                ParentHomeFragment()
            ).commit()
            setItemChecked(true, false, false, false)// to highlight navigation item
            ivToolbarLogo.visibility = View.VISIBLE
            ivNotification.visibility = View.VISIBLE
            rlSwitch.visibility = View.GONE
        } else {
            supportFragmentManager.beginTransaction().add(
                R.id.flActivityHome,
                NannyHomeFragment()
            ).commit()
            setItemChecked(true, false, false, false)// to highlight navigation item
            ivToolbarLogo.visibility = View.GONE
            ivNotification.visibility = View.GONE
            rlSwitch.visibility = View.VISIBLE
        }

    }

    private fun updateUserInfo(view: View) {
        view.apply {
            val ivProfile: ImageView = findViewById(R.id.ivHeaderNavigation)
            val userName: TextView = findViewById(R.id.tvHeaderName)
            val userEmail: TextView = findViewById(R.id.tvHeaderEmail)
            val tvProfile: TextView = findViewById(R.id.tvHeaderNavigation)

            ApplicationGlobal.userObject!!.let {
                userEmail.text = it.email.toString()
                userName.text = "${it.first_name} ${it.last_name}"

                if (it.profile_image == null) {
                    tvProfile.visibility = View.VISIBLE
                    (ivProfile.background as GradientDrawable).setColor(
                        getColor(
                            getColorForLatter(
                                it.first_name.toString()[0].lowercase()
                            )
                        )
                    )
                    tvProfile.text = it.first_name.toString()[0].uppercase()
                    tvProfile.setOnClickListener(headerImageClickListener)

                } else {
                    tvProfile.visibility = View.GONE
                    ivProfile.setOnClickListener(headerImageClickListener)
                    Glide.with(this@HomeActivity)
                        .load("$BASE_IMAGE_URL${it.profile_image}")
                        .apply(
                            RequestOptions()
                                .circleCrop()
                                .format(DecodeFormat.PREFER_ARGB_8888)
                                .override(SIZE_ORIGINAL)
                                .placeholder(R.drawable.ic_profile_large)
                        )
                        .into(ivProfile)
                }
            }
        }
        isChanged = false
        hideProgressBar()
    }

    override fun onResume() {
        super.onResume()
        if (isChanged) {
            showProgressBar(this)
            updateUserInfo(headerLayout!!)
        }
    }

    // simple drawer listener for side animation
    private fun animateNavigationDrawer() {

        drawerLayout.addDrawerListener(object : DrawerLayout.SimpleDrawerListener() {

            override fun onDrawerSlide(drawerView: View, slideOffset: Float) {

                val diffScaledOffset = slideOffset * (1 - endSCALE)
                val offsetScale = 1 - diffScaledOffset
                contentView.scaleX = offsetScale
                contentView.scaleY = offsetScale

                val xOffset = drawerView.width * slideOffset
                val xOffsetDiff = contentView.width * diffScaledOffset / 2
                val xTranslation = xOffset - xOffsetDiff
                contentView.translationX = xTranslation

            }

            override fun onDrawerOpened(drawerView: View) {
                contentView.setBackgroundResource(R.drawable.full_curved_layout)
                window.statusBarColor = getColor(R.color.defaultRed)
            }

            override fun onDrawerClosed(drawerView: View) {
                super.onDrawerClosed(drawerView)
                contentView.setBackgroundResource(R.color.white)
                window.statusBarColor = getColor(R.color.appBackgroundColor)
            }

        })

    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        ApplicationGlobal.userType.let { user ->
            View.VISIBLE.let { visible ->
                View.GONE.let { gone ->
                    when (item.itemId) {
                        R.id.menuHome -> {
                            if (user == 1) {// if user is parent
                                replaceFragment(ParentHomeFragment())
                                ivToolbarLogo.visibility = visible
                                ivNotification.visibility = visible
                                rlSwitch.visibility = gone
                            } else { //else if user is nanny
                                replaceFragment(NannyHomeFragment())
                                ivToolbarLogo.visibility = gone
                                ivNotification.visibility = gone
                                rlSwitch.visibility = visible
                            }
                            toolbarHome.setTitle("")
                            setItemChecked(true, false, false, false)
                            drawerLayout.closeDrawers()
                        }
                        R.id.menuBooking -> {
                            replaceFragment(BookingFragment())
                            toolbarHome.setTitle("Bookings")
                            ivToolbarLogo.visibility = gone
                            ivNotification.visibility = gone
                            rlSwitch.visibility = gone
                            setItemChecked(false, true, false, false)
                            drawerLayout.closeDrawers()
                        }
                        R.id.menuSetting -> {
                            replaceFragment(SettingsFragment())
                            toolbarHome.setTitle("Settings")
                            ivToolbarLogo.visibility = gone
                            ivNotification.visibility = gone
                            rlSwitch.visibility = gone
                            setItemChecked(false, false, true, false)
                            drawerLayout.closeDrawers()
                        }
                        R.id.menuNotification -> {
                            replaceFragment(NotificationFragment())
                            toolbarHome.setTitle("Notifications")
                            ivToolbarLogo.visibility = gone
                            ivNotification.visibility = gone
                            rlSwitch.visibility = gone
                            setItemChecked(false, false, false, true)
                            drawerLayout.closeDrawers()
                        }
                        R.id.menuLogOut -> showLogOutDialog(
                            this@HomeActivity,
                            this@HomeActivity
                        )
                    }
                    return true
                }
            }
        }
    }

    private fun replaceFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.flActivityHome, fragment).commit()
    }

    private fun setItemChecked(one: Boolean, two: Boolean, three: Boolean, four: Boolean) {
        navigationViewHome.menu.apply {
            getItem(0).isChecked = one
            getItem(1).isChecked = two
            getItem(2).isChecked = three
            getItem(3).isChecked = four
        }
    }

    override fun onBackPressed() {
        when (supportFragmentManager.findFragmentById(R.id.flActivityHome)) {
            is ParentHomeFragment -> showExitAppDialog()
            else -> onNavigationItemSelected(navigationViewHome.menu.getItem(0))
        }
    }

    private fun showExitAppDialog() {
        val dialog = Dialog(this)
        dialog.apply {
            setCanceledOnTouchOutside(false)
            val view = layoutInflater.inflate(R.layout.basic_alert_dialog_layout, null)
            val title = view.findViewById<TextView>(R.id.tvTitle)
            val message = view.findViewById<TextView>(R.id.tvMessage)
            val noButton: Button = view.findViewById(R.id.btnNo)
            val yesButton: Button = view.findViewById(R.id.btnYes)
            title.text = getString(R.string.title_close_app)
            message.text = getString(R.string.message_close_app)
            noButton.setOnClickListener {
                dialog.dismiss()
            }
            yesButton.setOnClickListener {
                dialog.dismiss()
                finishAffinity()
            }
            setContentView(view)
            CommonFunctions.setWidthCustomization(dialog)
            show()
        }
    }
}