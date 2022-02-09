package com.au.gonannysingleactivity.fragments

import android.Manifest
import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Looper
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import com.au.gonannysingleactivity.R
import com.au.gonannysingleactivity.objects.Constants
import com.au.gonannysingleactivity.objects.Constants.KEY_BROADCAST
import com.au.gonannysingleactivity.objects.Constants.KEY_EDIT_ADDRESS
import com.au.gonannysingleactivity.objects.Constants.KEY_FROM_WHERE
import com.au.gonannysingleactivity.objects.Constants.KEY_SELECT_ADDRESS
import com.au.gonannysingleactivity.objects.Constants.KEY_SELECT_MAILING_ADDRESS
import com.au.gonannysingleactivity.utils.ApplicationGlobal
import com.au.gonannysingleactivity.utils.CommonFunctions
import com.au.gonannysingleactivity.utils.CommonFunctions.Companion.addressFromLatLng
import com.au.gonannysingleactivity.utils.CommonFunctions.Companion.checkPermissionEnabled
import com.au.gonannysingleactivity.utils.CommonFunctions.Companion.hideProgressBar
import com.au.gonannysingleactivity.utils.CommonFunctions.Companion.showProgressBar
import com.au.gonannysingleactivity.utils.CommonFunctions.Companion.showToast
import com.au.gonannysingleactivity.webservices.MessageResponse
import com.au.gonannysingleactivity.webservices.api.RetrofitInstance
import com.google.android.gms.location.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.widget.Autocomplete
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode
import kotlinx.android.synthetic.main.fragment_add_address.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class AddAddressFragment : BaseFragment(), OnMapReadyCallback, View.OnClickListener {

    // for sending data through api call
    private var hashMap = HashMap<String, Any>()

    // calling same function for multiple functionalities
    private var forSelectAddress: Boolean = false // to select address
    private var forEditAddress: Boolean = false // to edit the added address
    private var forMailingAddress: Boolean = false // to add mailing address

    // to access location
    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    lateinit var locationRequest: LocationRequest
    private lateinit var googleMap: GoogleMap

    // getting data into these variables that to be edited
    private var houseNumber: String = ""
    private var landmark: String = ""
    private lateinit var latLng: LatLng
    private var addressId: Int? = null


    // fused location client location callback
    private var locationCallback = object : LocationCallback() {
        override fun onLocationResult(result: LocationResult) {
            super.onLocationResult(result)

            result.locations.apply {
                if (houseNumber != "" && landmark != "") {
                    etAddress.setText(houseNumber)
                    etAddressLandmark.setText(landmark)
                    setInfoOnUi(latLng)
                } else {
                    setInfoOnUi(LatLng(last().latitude, last().longitude))
                }
            }

        }
    }

    // second time location permission result
    private val permissionResultSecond = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) {
        if (it) {
            setUpLocationClient()
        }
    }

    // first time location permission result
    private val permissionResult = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) {
        if (it) {
            setUpLocationClient()
        } else {
            AlertDialog.Builder(requireActivity()).apply {
                setTitle(getString(R.string.location_permission_title))
                setMessage(getString(R.string.location_permission_message))
                setCancelable(false)
                setPositiveButton("Ok") { _, _ ->
                    permissionResultSecond.launch(Manifest.permission.ACCESS_FINE_LOCATION)
                }
                create()
                show()
            }
        }
    }

    // result contract for autoComplete intent action for changing the address
    private val resultContract = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.data != null) {
            try {
                val place = Autocomplete.getPlaceFromIntent(result.data!!)
                setInfoOnUi(place.latLng!!)
            } catch (e: IllegalArgumentException) {

            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // getting arguments
        arguments?.let {
            forEditAddress = it.getBoolean(KEY_EDIT_ADDRESS, false)
            forSelectAddress = it.getBoolean(KEY_SELECT_ADDRESS, false)
            forMailingAddress = it.getBoolean(KEY_SELECT_MAILING_ADDRESS, false)
            houseNumber = it.getString("apartment_no", "")
            landmark = it.getString("landmark", "")
            latLng = LatLng(it.getDouble("lat", 0.0), it.getDouble("lng", 0.0))
            addressId = it.getInt("address_id")
        }

        // initializing location clients
        fusedLocationProviderClient =
            LocationServices.getFusedLocationProviderClient(requireContext())
        locationRequest = LocationRequest.create().apply {
            interval = 12000
            fastestInterval = 12000
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        }
    }

    override fun getLayoutToInflate(): Int {
        return R.layout.fragment_add_address
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mapView.onCreate(savedInstanceState)
        mapView.getMapAsync(this)

        setUpToolbar(
            if (ApplicationGlobal.userType == 1) getString(R.string.add_address) else getString(
                R.string.select_booking_address
            ), false, getString(R.string.skip)
        ) {
            if (ApplicationGlobal.userType == 1)
                replaceFragment(R.id.frameLayout, AddCardFragment(), true)
            else replaceFragment(R.id.frameLayout, AddPhotosFragment(), true)
        }

        // managing layout for different functionalities
        if (forSelectAddress || forMailingAddress || forEditAddress) {
            cbMailingAddress.visibility = View.GONE
            if (!forMailingAddress) setUpToolbar(getString(R.string.select_address), true, null) {}
        } else {
            cbMailingAddress.visibility = View.VISIBLE
            ApplicationGlobal.preferenceManager.setScreenState(Constants.KEY_ADD_ADDRESS)
        }

        if (checkPermissionEnabled(requireContext())) {
            if (!forEditAddress) setUpLocationClient() // not getting current location as the location is already provided
        } else {
            permissionResult.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        }

        btnAddAddress.setOnClickListener(this)
        btnChange.setOnClickListener(this)

        mapView.setOnTouchListener { _, _ ->
            // Disallow the touch request for parent scroll on touch of  child view
            (scrollView).requestDisallowInterceptTouchEvent(true)
            false
        }

    }

    @SuppressLint("SetTextI18n")
    private fun setInfoOnUi(latLng: LatLng) {
        addressFromLatLng(requireContext(), latLng).apply {
            tvAddress.text = "$subLocality, $locality, $postalCode, $countryName"
            tvCityName.text = locality
        }
        hideProgressBar()

        googleMap.apply {
            clear()
            animateCamera(
                CameraUpdateFactory.newLatLngZoom(latLng, 17F),
                5000,
                null
            )
            addMarker(MarkerOptions().position(latLng))
        }

        fillHashMapFromLatLng(latLng)
    }

    private fun setUpLocationClient() {
        if (ActivityCompat.checkSelfPermission(
                requireActivity(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                requireActivity(),
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        } else {
            showProgressBar(requireContext())
            fusedLocationProviderClient.requestLocationUpdates(
                locationRequest,
                locationCallback,
                Looper.myLooper()!!
            )
        }
    }

    override fun onMapReady(p0: GoogleMap) {
        googleMap = p0
        googleMap.uiSettings.isZoomGesturesEnabled = true

        // if here to edit data fill info on ui as soon as google map is ready
        if (forEditAddress) {
            etAddress.setText(houseNumber)
            etAddressLandmark.setText(landmark)
            btnAddAddress.text = getString(R.string.edit_address)
            setInfoOnUi(latLng)
        }

    }

//    override fun onSaveInstanceState(outState: Bundle) {
//        super.onSaveInstanceState(outState)
//        mapView.onSaveInstanceState(outState)
//    }

    override fun onResume() {
        super.onResume()
        mapView.onResume()
    }

    override fun onStart() {
        super.onStart()
        mapView.onStart()
    }

    override fun onStop() {
        super.onStop()
        mapView.onStop()
    }

    override fun onPause() {
        mapView.onPause()
        super.onPause()
        if (fusedLocationProviderClient != null) {
            fusedLocationProviderClient.removeLocationUpdates(locationCallback)
        }
    }

    override fun onLowMemory() {
        super.onLowMemory()
        mapView.onLowMemory()
    }

    private fun fillHashMapFromLatLng(latLng: LatLng) {

        addressFromLatLng(requireContext(), latLng).apply {
            hashMap["state"] = adminArea
            hashMap["city"] = locality
            hashMap["lat"] = latLng.latitude
            hashMap["lng"] = latLng.longitude
            hashMap["address"] = tvAddress.text.toString().trim()
        }
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.btnChange -> {
                val fields: List<Place.Field> =
                    listOf(
                        Place.Field.ID, Place.Field.NAME, Place.Field.LAT_LNG,
                        Place.Field.ADDRESS,
                        Place.Field.ADDRESS_COMPONENTS
                    )
                val intent = Autocomplete
                    .IntentBuilder(AutocompleteActivityMode.FULLSCREEN, fields)
                    .build(requireContext())
                resultContract.launch(intent)
            }

            R.id.btnAddAddress -> {
                showProgressBar(requireContext())
                if (etAddress.text.toString().trim().isEmpty()) {
                    hideProgressBar()
                    showToast(requireContext(), getString(R.string.enter_house_number_warning))
                } else if (etAddressLandmark.text.toString().trim().isEmpty()) {
                    hideProgressBar()
                    showToast(requireContext(), getString(R.string.enter_landmark_warning))
                } else if (hashMap.isEmpty()) {
                    hideProgressBar()
                    showToast(requireContext(), getString(R.string.select_address_warning))
                } else {

                    hashMap["apartment_no"] = etAddress.text.toString().trim()
                    hashMap["landmark"] = etAddressLandmark.text.toString().trim()

                    if (forMailingAddress) {
                        hashMap["address_type"] = 2
                    } else if (!(forMailingAddress && forSelectAddress)) {
                        hashMap["is_mailing_address_same"] = 1
                        hashMap["address_type"] = 1
                    }

                    val call: Call<MessageResponse> = if (forEditAddress) {
                        hashMap["address_id"] = addressId!!
                        RetrofitInstance.create().editAddress(hashMap)
                    } else
                        RetrofitInstance.create().addAddress(hashMap)


                    call.enqueue(object : Callback<MessageResponse> {

                        override fun onResponse(
                            call: Call<MessageResponse>,
                            response: Response<MessageResponse>
                        ) {
                            if (response.isSuccessful) {
                                hideProgressBar()
                                if (forSelectAddress) {
                                    requireActivity().sendBroadcast(Intent(KEY_BROADCAST).apply {
                                        putExtra(KEY_FROM_WHERE, KEY_SELECT_ADDRESS)
                                        putExtra("address", tvAddress.text)
                                        putExtra("apartment_no", etAddress.text.toString().trim())
                                        putExtra(
                                            "landmark",
                                            etAddressLandmark.text.toString().trim()
                                        )
                                        putExtra("city", hashMap["city"].toString())
                                        putExtra("lat", hashMap["lat"].toString())
                                        putExtra("lng", hashMap["lng"].toString())
                                    })
                                    requireActivity().finish()
                                } else if (cbMailingAddress.isChecked || forMailingAddress) {
                                    if (ApplicationGlobal.userType == 1)
                                        replaceFragment(
                                            R.id.frameLayout,
                                            AddCardFragment(),
                                            !forMailingAddress
                                        )
                                    else
                                        replaceFragment(
                                            R.id.frameLayout,
                                            AddPhotosFragment(),
                                            true
                                        )

                                } else if (forEditAddress) {
                                    requireActivity().onBackPressed()
                                } else {
                                    replaceFragment(R.id.frameLayout, AddAddressFragment().apply {
                                        arguments = Bundle().apply {
                                            putBoolean(KEY_SELECT_MAILING_ADDRESS, true)
                                        }
                                    }, true)
                                }

                            } else {
                                hideProgressBar()
                                CommonFunctions.getErrorMessage(
                                    response.errorBody()!!,
                                    requireContext(),
                                    response.code()
                                )
                            }
                        }

                        override fun onFailure(call: Call<MessageResponse>, t: Throwable) {
                            hideProgressBar()
                            showToast(requireContext(), t.message.toString())
                        }

                    })
                }
            }

        }
    }

}