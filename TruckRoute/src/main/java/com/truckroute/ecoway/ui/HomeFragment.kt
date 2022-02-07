package com.truckroute.ecoway.ui

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.os.Handler
import android.text.Editable
import android.text.TextWatcher
import android.text.format.DateFormat
import android.view.*
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import com.google.android.gms.location.LocationRequest
import com.tomtom.online.sdk.common.location.LatLng
import com.tomtom.online.sdk.common.permission.AndroidPermissionChecker
import com.tomtom.online.sdk.location.FusedLocationSource
import com.tomtom.online.sdk.location.LocationSource
import com.tomtom.online.sdk.location.LocationUpdateListener
import com.tomtom.online.sdk.routing.route.description.RouteType
import com.tomtom.online.sdk.routing.route.description.TravelMode
import com.tomtom.online.sdk.search.OnlineSearchApi
import com.tomtom.online.sdk.search.SearchApi
import com.tomtom.online.sdk.search.data.fuzzy.FuzzySearchQueryBuilder
import com.tomtom.online.sdk.search.data.fuzzy.FuzzySearchResponse
import com.tomtom.online.sdk.search.data.reversegeocoder.ReverseGeocoderFullAddress
import com.tomtom.online.sdk.search.data.reversegeocoder.ReverseGeocoderSearchQueryBuilder
import com.tomtom.online.sdk.search.data.reversegeocoder.ReverseGeocoderSearchResponse
import com.truckroute.ecoway.*
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.observers.DisposableSingleObserver
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_main.*
import timber.log.Timber
import java.text.SimpleDateFormat
import java.util.*

class HomeFragment : Fragment(), LocationUpdateListener, AppBottomSheetDialog.Listener {
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_home, container, false)
    }

    companion object {
        private const val PREPARATION_FIRST_OPT = 0
        private const val PREPARATION_SECOND_OPT = 5
        private const val PREPARATION_THIRD_OPT = 10
        private val DEFAULT_DEPARTURE_LATLNG = LatLng(52.376368, 4.908113)
        private val DEFAULT_DESTINATION_LATLNG = LatLng(52.3076865, 4.767424099999971)
        private const val TIME_PICKER_DIALOG_TAG = "TimePicker"
        private const val ARRIVE_TIME_AHEAD_HOURS = 5
        private const val AUTOCOMPLETE_SEARCH_DELAY_MILLIS = 600
        private const val AUTOCOMPLETE_SEARCH_THRESHOLD = 3
        private const val TIME_24H_FORMAT = "HH:mm"
        private const val TIME_12H_FORMAT = "hh:mm"
        private const val SEARCH_FUZZY_LVL_MIN = 2
        private const val PERMISSION_REQUEST_LOCATION = 0

        @JvmStatic
        fun newInstance() =
            HomeFragment().apply {
                arguments = Bundle().apply {
                }
            }
    }

    /*var calArriveAt: Calendar = Calendar.getInstance().apply {
        this.add(Calendar.HOUR, ARRIVE_TIME_AHEAD_HOURS)
    }*/
    private lateinit var searchApi: SearchApi
    private lateinit var locationSource: LocationSource
    private lateinit var searchAdapter: ArrayAdapter<String>
    private lateinit var searchAutocompleteList: MutableList<String>
    private lateinit var searchResultsMap: MutableMap<String, LatLng>
    private val searchTimerHandler = Handler()
    private var searchRunnable: Runnable? = null
    private var travelModeSelected = TravelMode.TRUCK
    private var arrivalCalender: Calendar = Calendar.getInstance(Locale.getDefault())
    private var departCalender: Calendar? = null
    private var latLngCurrentPosition: LatLng? = null
    private var latLngDeparture = DEFAULT_DEPARTURE_LATLNG
    private var latLngDestination = DEFAULT_DESTINATION_LATLNG
    private var preparationTimeSelected = PREPARATION_FIRST_OPT
    private var routeTypeSelected = RouteType.ECO
    private var vehicleLoadTypeSelected = MyVehicleLoadType.NONE
    private var filterType = FilterType.NONE

    private val userPreferredHourPattern: String
        get() = if (DateFormat.is24HourFormat(activity)) TIME_24H_FORMAT else TIME_12H_FORMAT

    private val currentTimeInMillis: Long
        get() {
            val calendar = Calendar.getInstance()
            return calendar.timeInMillis
        }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        initTomTomServices()
        initSearchFieldsWithDefaultValues()
        initWhereSection()
        //initByWhenSection()
        initData();
        initStartSection()
    }

    private fun initData() {
        txt_route_type.text = routeTypeSelected.toString()
        txt_travel_mode.text = travelModeSelected.toString()
        txt_vehicle_load_type.text = vehicleLoadTypeSelected.toString()
        //val sdf = SimpleDateFormat(DateTimePicker.getFormat("d"), Locale.getDefault())
        //txt_arrive_at.text = sdf.format(arrivalCalender.time)
    }

    private fun openBottomSheet(filterType: FilterType, selectedValue: String) {
        val bottomSheetDialog = AppBottomSheetDialog()
        val args = Bundle();
        args.putSerializable(Constants.EXTRA_FILTER_TYPE, filterType);
        args.putString(Constants.EXTRA_SELECTED_VALUE, selectedValue)
        bottomSheetDialog.arguments = args;
        bottomSheetDialog.show(
            requireFragmentManager(),
            AppBottomSheetDialog.TAG
        )
    }

    override fun onResume() {
        super.onResume()
        //resetDaysInArriveAt()
        val checker = AndroidPermissionChecker.createLocationChecker(activity)
        if (!checker.ifNotAllPermissionGranted()) {
            locationSource.activate()
        }
    }

    override fun onLocationChanged(location: Location) {
        if (latLngCurrentPosition == null) {
            latLngCurrentPosition = LatLng(location)
            //initDepartureWithDefaultValue()
            locationSource.deactivate()
        }
    }

    private fun initTomTomServices() {
        searchApi = OnlineSearchApi.create(requireContext(), BuildConfig.SEARCH_API_KEY)
    }

    private fun initSearchFieldsWithDefaultValues() {
        initLocationSource()
        //initDepartureWithDefaultValue()
        //initDestinationWithDefaultValue()
    }

    private fun initLocationSource() {
        val permissionChecker = AndroidPermissionChecker.createLocationChecker(activity)
        if (permissionChecker.ifNotAllPermissionGranted()) {
            ActivityCompat.requestPermissions(
                requireActivity(),
                arrayOf(
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ),
                PERMISSION_REQUEST_LOCATION
            )
        }
        locationSource = FusedLocationSource(activity, LocationRequest.create())
        locationSource.addLocationUpdateListener(this)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PERMISSION_REQUEST_LOCATION) {
            if (grantResults.size >= 2 &&
                grantResults[0] == PackageManager.PERMISSION_GRANTED &&
                grantResults[1] == PackageManager.PERMISSION_GRANTED
            ) {
                locationSource.activate()
            } else {
                Toast.makeText(activity, R.string.location_permissions_denied, Toast.LENGTH_SHORT)
                    .show()
            }
        }
    }

    private fun initDepartureWithDefaultValue() {
        //latLngDeparture = DEFAULT_DEPARTURE_LATLNG
        //setAddressForLocation(latLngCurrentPosition!!, atv_main_departure_location)
        if (latLngCurrentPosition != null) {
            val currentLocationTitle = getString(R.string.main_current_position)
            searchAutocompleteList.add(currentLocationTitle)
            searchResultsMap[currentLocationTitle] = latLngCurrentPosition!!
            atv_main_departure_location.listSelection = 0;
        }
    }

    private fun initDestinationWithDefaultValue() {
        latLngDestination = DEFAULT_DESTINATION_LATLNG
        setAddressForLocation(latLngDestination, atv_main_destination_location)
    }

    private fun initWhereSection() {
        searchAutocompleteList = ArrayList()
        searchResultsMap = HashMap()
        searchAdapter =
            ArrayAdapter(
                requireActivity(),
                android.R.layout.simple_dropdown_item_1line,
                searchAutocompleteList
            )

        setTextWatcherToAutoCompleteField(atv_main_departure_location, button_departure_clear)
        setClearButtonToAutocompleteField(atv_main_departure_location, button_departure_clear)
        setTextWatcherToAutoCompleteField(atv_main_destination_location, button_destination_clear)
        setClearButtonToAutocompleteField(atv_main_destination_location, button_destination_clear)
    }

    private fun setTextWatcherToAutoCompleteField(
        autoCompleteTextView: AutoCompleteTextView,
        imageButton: ImageButton
    ) {
        autoCompleteTextView.setAdapter(searchAdapter)
        autoCompleteTextView.addTextChangedListener(object : BaseTextWatcher() {
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                searchRunnable?.let {
                    searchTimerHandler.removeCallbacks(it)
                }
            }

            override fun afterTextChanged(s: Editable) {
                if (s.isNotEmpty()) {
                    imageButton.visibility = View.VISIBLE
                    if (s.length >= AUTOCOMPLETE_SEARCH_THRESHOLD) {
                        searchRunnable =
                            Runnable { searchAddress(s.toString(), autoCompleteTextView) }
                        searchAdapter.clear()
                        searchTimerHandler.postDelayed(
                            searchRunnable!!,
                            AUTOCOMPLETE_SEARCH_DELAY_MILLIS.toLong()
                        )
                    }
                } else {
                    imageButton.visibility = View.INVISIBLE
                }
            }
        })
        autoCompleteTextView.setOnItemClickListener { parent, view, position, _ ->
            val item = parent.getItemAtPosition(position) as String
            if (autoCompleteTextView === atv_main_departure_location) {
                latLngDeparture = searchResultsMap[item]!!
            } else if (autoCompleteTextView === atv_main_destination_location) {
                latLngDestination = searchResultsMap[item]!!
            }
            hideKeyboard(view)
        }
    }

    private fun searchAddress(searchWord: String, autoCompleteTextView: AutoCompleteTextView) {
        searchApi.search(
            FuzzySearchQueryBuilder(searchWord)
                .withLanguage(Locale.getDefault().toLanguageTag())
                .withTypeAhead(true)
                .withMinFuzzyLevel(SEARCH_FUZZY_LVL_MIN).build()
        )
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(object : DisposableSingleObserver<FuzzySearchResponse>() {
                override fun onSuccess(fuzzySearchResponse: FuzzySearchResponse) {
                    if (!fuzzySearchResponse.results.isEmpty()) {
                        searchAutocompleteList.clear()
                        searchResultsMap.clear()
                        if (autoCompleteTextView === atv_main_departure_location && latLngCurrentPosition != null) {
                            val currentLocationTitle = getString(R.string.main_current_position)
                            searchAutocompleteList.add(currentLocationTitle)
                            searchResultsMap[currentLocationTitle] = latLngCurrentPosition!!
                        }
                        for (result in fuzzySearchResponse.results) {
                            val addressString = result.address.freeformAddress
                            searchAutocompleteList.add(addressString)
                            searchResultsMap[addressString] = result.position
                        }
                        searchAdapter.apply {
                            this.clear()
                            this.addAll(searchAutocompleteList)
                            this.filter.filter("")
                        }
                    }
                }

                override fun onError(e: Throwable) {
                    Toast.makeText(activity, e.localizedMessage, Toast.LENGTH_SHORT).show()
                }
            })
    }

    private fun setAddressForLocation(
        location: LatLng,
        autoCompleteTextView: AutoCompleteTextView?
    ) {
        searchApi.reverseGeocoding(
            ReverseGeocoderSearchQueryBuilder(
                location.latitude,
                location.longitude
            ).build()
        )
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(object : DisposableSingleObserver<ReverseGeocoderSearchResponse>() {
                override fun onSuccess(reverseGeocoderSearchResponse: ReverseGeocoderSearchResponse) {
                    val addressesList = reverseGeocoderSearchResponse.addresses
                    if (!addressesList.isEmpty()) {
                        val address =
                            (addressesList[0] as ReverseGeocoderFullAddress).address.freeformAddress
                        autoCompleteTextView?.apply {
                            this.setText(address)
                            this.dismissDropDown()
                        }
                    }
                }

                override fun onError(e: Throwable) {
                    Toast.makeText(
                        activity,
                        getString(
                            R.string.toast_error_message_error_getting_location,
                            e.localizedMessage
                        ),
                        Toast.LENGTH_LONG
                    ).show()
                    Timber.e(
                        e,
                        getString(
                            R.string.toast_error_message_error_getting_location,
                            e.localizedMessage
                        )
                    )
                }
            })
    }

    private fun setClearButtonToAutocompleteField(
        autoCompleteTextView: AutoCompleteTextView,
        imageButton: ImageButton
    ) {
        imageButton.setOnClickListener {
            autoCompleteTextView.apply {
                this.setText("")
                this.requestFocus()
            }
            imageButton.visibility = View.GONE
        }
    }

    private fun selectPreparationButton(preparationButton: View) {
        preparationButton.isSelected = true
        val elevationButtonPressed = resources.getDimension(R.dimen.main_elevation_button_pressed)
        preparationButton.elevation = elevationButtonPressed
    }

    private fun initStartSection() {
        button_main_start.setOnClickListener {
            if (departureFiledIsEmpty()) {
                Toast.makeText(
                    activity,
                    R.string.select_from_location,
                    Toast.LENGTH_SHORT
                ).show()
                return@setOnClickListener
            } else if (destinationFieldIsEmpty()) {
                //initDestinationWithDefaultValue()
                Toast.makeText(activity, R.string.select_to_location, Toast.LENGTH_SHORT)
                    .show()
                return@setOnClickListener
            }

            val intent = CountdownActivity.prepareIntent(
                requireContext(),
                latLngDeparture,
                latLngDestination,
                travelModeSelected,
                if (arrivalCalender != null) arrivalCalender!!.timeInMillis else 0L,
                if (departCalender != null) departCalender!!.timeInMillis else 0L,
                routeTypeSelected,
                vehicleLoadTypeSelected.name,
                vehicleWeight = if (edt_vehicle_weight.text.toString()
                        .isEmpty()
                ) 0 else edt_vehicle_weight.text.toString().toInt(),
                vehicleHeight = if (edt_vehicle_height.text.toString()
                        .isEmpty()
                ) 0.0 else edt_vehicle_height.text.toString().toDouble(),
                vehicleLength = if (edt_vehicle_length.text.toString()
                        .isEmpty()
                ) 0.0 else edt_vehicle_length.text.toString().toDouble(),
                vehicleWidth = if (edt_vehicle_width.text.toString()
                        .isEmpty()
                ) 0.0 else edt_vehicle_width.text.toString().toDouble()
            )
            startActivity(intent)
        }

        txt_route_type.setOnClickListener {
            filterType = FilterType.VEHICLE_ROUTE_TYPE
            openBottomSheet(filterType, routeTypeSelected.name)
        }
        txt_vehicle_load_type.setOnClickListener {
            filterType = FilterType.VEHICLE_LOAD_TYPE
            openBottomSheet(filterType, vehicleLoadTypeSelected.toString())
        }
        txt_travel_mode.setOnClickListener {
            filterType = FilterType.TRAVEL_MODE
            openBottomSheet(filterType, travelModeSelected.name)
        }
        txt_arrive_at.setOnClickListener {
            DateTimePicker(requireContext(), true) {
                val sdf = SimpleDateFormat(DateTimePicker.getFormat("d"), Locale.getDefault())
                txt_arrive_at.text = sdf.format(it.calendar.time)
                arrivalCalender = it.calendar
                // reset depart date when selecting arrive date
                departCalender = null
                txt_depart_at.text = ""
            }.show()
        }
        txt_depart_at.setOnClickListener {
            DateTimePicker(requireContext(), true) {
                val sdf = SimpleDateFormat(DateTimePicker.getFormat("d"), Locale.getDefault())
                txt_depart_at.text = sdf.format(it.calendar.time)
                departCalender = it.calendar
            }.show()
        }
    }

    private fun textViewIsEmpty(textView: AutoCompleteTextView): Boolean {
        return textView.text.toString().isEmpty()
    }

    private fun departureFiledIsEmpty(): Boolean {
        return textViewIsEmpty(atv_main_departure_location)
    }

    private fun destinationFieldIsEmpty(): Boolean {
        return textViewIsEmpty(atv_main_destination_location)
    }

    private fun hideKeyboard(view: View) {
        val inputMethodManager =
            requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.hideSoftInputFromWindow(view.applicationWindowToken, 0)
    }

    private abstract inner class BaseTextWatcher : TextWatcher {
        override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
    }

    override fun onFilterSelected(listValue: String) {
        when (filterType) {
            FilterType.VEHICLE_ROUTE_TYPE -> {
                routeTypeSelected = RouteType.valueOf(listValue)
                txt_route_type.text = listValue
            }
            FilterType.TRAVEL_MODE -> {
                travelModeSelected = TravelMode.valueOf(listValue)
                txt_travel_mode.text = listValue
                resetVehicleDimens()
            }
            FilterType.VEHICLE_LOAD_TYPE -> {
                vehicleLoadTypeSelected = MyVehicleLoadType.getLoadType(listValue)
                txt_vehicle_load_type.text = listValue
            }
            FilterType.NONE -> {
            }
        }
    }

    private fun resetVehicleDimens() {
        edt_vehicle_height.setText("")
        edt_vehicle_length.setText("")
        edt_vehicle_weight.setText("")
        edt_vehicle_width.setText("")
    }
}