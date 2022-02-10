package com.truckroute.ecoway.ui

import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.CountDownTimer
import android.os.Handler
import android.text.format.DateFormat
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import com.google.android.material.snackbar.BaseTransientBottomBar
import com.tomtom.online.sdk.common.location.LatLng
import com.tomtom.online.sdk.location.LocationUpdateListener
import com.tomtom.online.sdk.map.*
import com.tomtom.online.sdk.routing.OnlineRoutingApi
import com.tomtom.online.sdk.routing.RoutingApi
import com.tomtom.online.sdk.routing.RoutingException
import com.tomtom.online.sdk.routing.route.*
import com.tomtom.online.sdk.routing.route.calculation.InstructionsType
import com.tomtom.online.sdk.routing.route.description.RouteType
import com.tomtom.online.sdk.routing.route.description.TravelMode
import com.tomtom.online.sdk.routing.route.diagnostic.ReportType
import com.tomtom.online.sdk.routing.route.information.Instruction
import com.tomtom.online.sdk.routing.route.vehicle.VehicleDimensions
import com.tomtom.online.sdk.routing.route.vehicle.VehicleLoadType
import com.tomtom.online.sdk.routing.route.vehicle.VehicleRestrictions
import com.truckroute.ecoway.BuildConfig
import com.truckroute.ecoway.R
import com.truckroute.ecoway.maneuverslist.ManeuversListActivity
import kotlinx.android.synthetic.main.activity_countdown.*
import java.io.Serializable
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.math.abs


class CountdownActivity : AppCompatActivity(), OnMapReadyCallback {

    companion object {
        private const val BUNDLE_SETTINGS = "SETTINGS"
        private const val BUNDLE_DEPARTURE_LAT = "DEPARTURE_LAT"
        private const val BUNDLE_DEPARTURE_LNG = "DEPARTURE_LNG"
        private const val BUNDLE_DESTINATION_LAT = "DESTINATION_LAT"
        private const val BUNDLE_DESTINATION_LNG = "DESTINATION_LNG"
        private const val BUNDLE_BY_WHAT = "BY_WHAT"
        private const val BUNDLE_ARRIVE_AT = "ARRIVE_AT"
        private const val BUNDLE_DEPART_AT = "DEPART_AT"

        private const val BUNDLE_VEHICLE_LOAD_TYPE = "VEHICLE_LOAD_TYPE"
        private const val BUNDLE_VEHICLE_WEIGHT = "VEHICLE_WEIGHT"
        private const val BUNDLE_VEHICLE_HEIGHT = "VEHICLE_HEIGHT"
        private const val BUNDLE_VEHICLE_LENGTH = "VEHICLE_LENGTH"
        private const val BUNDLE_VEHICLE_WIDTH = "VEHICLE_WIDTH"

        //private const val BUNDLE_PREPARATION_TIME = "PREPARATION_TIME"
        private const val BUNDLE_ROUTE_TYPE = "ROUTE_TYPE"
        private const val COUNTDOWN_MODE_PREPARATION = "countdown_mode_preparation"
        private const val COUNTDOWN_MODE_FINISHED = "countdown_mode_finished"
        private const val ONE_MINUTE_IN_MILLIS = 60000
        private const val ONE_SECOND_IN_MILLIS = 1000
        private const val ROUTE_RECALCULATION_DELAY = ONE_MINUTE_IN_MILLIS

        fun prepareIntent(
            context: Context,
            departure: LatLng,
            destination: LatLng,
            strByWhat: TravelMode,
            arriveAtMillis: Long,
            departureAtMillis: Long,
            routeType: RouteType,
            vehicleLoadTypeString: String,
            vehicleWeight: Int,
            vehicleHeight: Double,
            vehicleLength: Double,
            vehicleWidth: Double
        ): Intent {
            val settings = Bundle().apply {
                this.putDouble(BUNDLE_DEPARTURE_LAT, departure.latitude)
                this.putDouble(BUNDLE_DEPARTURE_LNG, departure.longitude)
                this.putDouble(BUNDLE_DESTINATION_LAT, destination.latitude)
                this.putDouble(BUNDLE_DESTINATION_LNG, destination.longitude)
                this.putString(BUNDLE_BY_WHAT, strByWhat.toString())
                this.putLong(BUNDLE_ARRIVE_AT, arriveAtMillis)
                this.putLong(BUNDLE_DEPART_AT, departureAtMillis)
                this.putString(BUNDLE_ROUTE_TYPE, routeType.toString())
                this.putString(BUNDLE_VEHICLE_LOAD_TYPE, vehicleLoadTypeString)
                this.putInt(BUNDLE_VEHICLE_WEIGHT, vehicleWeight)
                this.putDouble(BUNDLE_VEHICLE_HEIGHT, vehicleHeight)
                this.putDouble(BUNDLE_VEHICLE_LENGTH, vehicleLength)
                this.putDouble(BUNDLE_VEHICLE_WIDTH, vehicleWidth)
            }
            val intent = Intent(context, CountdownActivity::class.java)
            intent.putExtra(BUNDLE_SETTINGS, settings)

            return intent
        }
    }

    //private var preparationTime: Int = 0
    private var previousTravelTime: Int = 0
    private var isPreparationMode = false
    private var isInPauseMode = false
    private var arriveAt: Date? = null
    private var departAt: Date? = null
    private var vehicleLoadType: VehicleLoadType? = null
    private var vehicleWeight: Int? = null
    private var vehicleHeight: Double? = null
    private var vehicleLength: Double? = null
    private var vehicleWidth: Double? = null

    private lateinit var routeSpecification: RouteSpecification

    private lateinit var infoSnackbar: CustomSnackbar
    private lateinit var warningSnackbar: CustomSnackbar
    private lateinit var dialogInProgress: AlertDialog
    private var dialogInfo: AlertDialog? = null
    private lateinit var tomtomMap: TomtomMap
    private lateinit var routingApi: RoutingApi
    private lateinit var departureIcon: Icon
    private lateinit var destinationIcon: Icon
    private lateinit var navEnableIcon: Icon
    private lateinit var navDisableIcon: Icon
    private var travelMode: TravelMode? = null
    private var routeType: RouteType? = null
    private var destination: LatLng? = null
    private var departure: LatLng? = null
    private var countDownTimer: CountDownTimer? = null
    private val timerHandler = Handler()
    private var isNavStarted = false
    private lateinit var chevron: Chevron
    private lateinit var instructions: List<Instruction>

    private val requestRouteRunnable =
        Runnable {
            requestRoute(
                departure,
                destination,
                travelMode,
                routeType,
                arriveAt,
                departAt,
                vehicleLoadType,
                vehicleWeight,
                vehicleHeight,
                vehicleLength,
                vehicleWidth
            )
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_countdown)
        initTomTomServices()
        initToolbarSettings()
        initActivitySettings(intent.getBundleExtra(BUNDLE_SETTINGS))
        initClickEvents()
        initViews()
    }

    private fun initViews() {
        routeType?.let {
            when (it) {
                RouteType.ECO -> {
                    changeRouteTypeButtonStates(btnEcho.text.toString())
                }
                RouteType.FASTEST -> {
                    changeRouteTypeButtonStates(btnFastest.text.toString())
                }
                RouteType.SHORTEST -> {
                    changeRouteTypeButtonStates(btnShortest.text.toString())
                }
                else -> {
                }
            }
        }
    }

    private val listener = LocationUpdateListener { location ->
        chevron.position = ChevronPosition.Builder(location).build()
        chevron.show()
    }

    private fun initClickEvents() {
        imgBack.setOnClickListener { finish() }
        imgNavigation.setOnClickListener {
            /*val uriString = "google.navigation:q=${departure?.latitude},${departure?.longitude};${destination?.latitude},${destination?.longitude}"
            val intent = Intent(
                Intent.ACTION_VIEW,
                Uri.parse(uriString)
            )
            intent.setPackage("com.google.android.apps.maps");
            startActivity(intent)*/
            val uriString = "http://maps.google.com/maps?saddr=${departure?.latitude},${departure?.longitude}&daddr=${destination?.latitude},${destination?.longitude}&dirflg=d"
            val uri = Uri.parse(uriString)
            val intent = Intent(Intent.ACTION_VIEW, uri)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
        }
        imgManeuversList.setOnClickListener { showManeuversList() }
        btnEcho.setOnClickListener {
            changeRouteTypeButtonStates(btnEcho.text.toString());
            showDialog(dialogInProgress)
            routeType = RouteType.ECO
            requestRoute(
                departure,
                destination,
                travelMode,
                routeType,
                arriveAt,
                departAt,
                vehicleLoadType,
                vehicleWeight,
                vehicleHeight,
                vehicleLength,
                vehicleWidth
            )
        }
        btnFastest.setOnClickListener {
            changeRouteTypeButtonStates(btnFastest.text.toString())
            showDialog(dialogInProgress)
            routeType = RouteType.FASTEST
            requestRoute(
                departure,
                destination,
                travelMode,
                routeType,
                arriveAt,
                departAt,
                vehicleLoadType,
                vehicleWeight,
                vehicleHeight,
                vehicleLength,
                vehicleWidth
            )
        }
        btnShortest.setOnClickListener {
            changeRouteTypeButtonStates(btnShortest.text.toString())
            showDialog(dialogInProgress)
            routeType = RouteType.SHORTEST
            requestRoute(
                departure,
                destination,
                travelMode,
                routeType,
                arriveAt,
                departAt,
                vehicleLoadType,
                vehicleWeight,
                vehicleHeight,
                vehicleLength,
                vehicleWidth
            )
        }
    }

    private fun changeRouteTypeButtonStates(btnText: String) {
        when (btnText) {
            getString(R.string.echo) -> {
                btnEcho.background =
                    ContextCompat.getDrawable(applicationContext, R.drawable.btn_bg_selected)
                btnFastest.background =
                    ContextCompat.getDrawable(applicationContext, R.drawable.btn_bg_unselected)
                btnShortest.background =
                    ContextCompat.getDrawable(applicationContext, R.drawable.btn_bg_unselected)
            }
            getString(R.string.fastest) -> {
                btnEcho.background =
                    ContextCompat.getDrawable(applicationContext, R.drawable.btn_bg_unselected)
                btnFastest.background =
                    ContextCompat.getDrawable(applicationContext, R.drawable.btn_bg_selected)
                btnShortest.background =
                    ContextCompat.getDrawable(applicationContext, R.drawable.btn_bg_unselected)
            }
            else -> {
                btnEcho.background =
                    ContextCompat.getDrawable(applicationContext, R.drawable.btn_bg_unselected)
                btnFastest.background =
                    ContextCompat.getDrawable(applicationContext, R.drawable.btn_bg_unselected)
                btnShortest.background =
                    ContextCompat.getDrawable(applicationContext, R.drawable.btn_bg_selected)
            }
        }
    }

    private fun showManeuversList() {
        val intent = Intent(applicationContext, ManeuversListActivity::class.java)
        intent.putExtra(Constants.EXTRA_INSTRUCTIONS, instructions as Serializable)
        startActivity(intent)
    }

    private fun startNavigation() {
        Toast.makeText(this, R.string.navigation_started, Toast.LENGTH_SHORT)
            .show()
        imgNavigation.setImageDrawable(
            ContextCompat.getDrawable(
                this,
                R.drawable.ic_baseline_navigation_blue_24
            )
        )
        tomtomMap.drivingSettings.startTracking(chevron);
    }

    private fun stopNavigation() {
        Toast.makeText(this, R.string.navigation_stopped, Toast.LENGTH_SHORT)
            .show()
        imgNavigation.setImageDrawable(
            ContextCompat.getDrawable(
                this,
                R.drawable.ic_baseline_navigation_24
            )
        )
        tomtomMap.drivingSettings.stopTracking();
    }

    override fun onPause() {
        super.onPause()
        isInPauseMode = true
    }

    override fun onResume() {
        super.onResume()
        if (!dialogIsShowing(dialogInfo)) {
            isInPauseMode = false
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()
        timerHandler.removeCallbacks(requestRouteRunnable)
        countDownTimer?.cancel()
    }

    override fun onMapReady(tomtomMap: TomtomMap) {
        this.tomtomMap = tomtomMap
        tomtomMap.apply {
            this.isMyLocationEnabled = true
            this.clear()
        }
        val chevronBuilder = ChevronBuilder.create(navEnableIcon, navDisableIcon)
        chevron = tomtomMap.drivingSettings.addChevron(chevronBuilder)
        tomtomMap.addLocationUpdateListener(listener)
        showDialog(dialogInProgress)
        requestRoute(
            departure,
            destination,
            travelMode,
            routeType,
            arriveAt,
            departAt,
            vehicleLoadType,
            vehicleWeight,
            vehicleHeight,
            vehicleLength,
            vehicleWidth
        )
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        this.tomtomMap.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    private fun requestRoute(
        departure: LatLng?,
        destination: LatLng?,
        byWhat: TravelMode?,
        routeType: RouteType?,
        arriveAt: Date?,
        departAt: Date?,
        vehicleLoadType: VehicleLoadType?,
        vehicleWeight: Int?,
        vehicleHeight: Double?,
        vehicleLength: Double?,
        vehicleWidth: Double?
    ) {
        if (!isInPauseMode) {
            val routeDescriptor = RouteDescriptor.Builder()
                .considerTraffic(true)
                .travelMode(byWhat!!)
                .routeType(routeType!!)

            departAt?.let {
                routeDescriptor.departAt(it)
            }

            val routeCalculationDescriptor = RouteCalculationDescriptor.Builder()
                .routeDescription(routeDescriptor.build())
                .reportType(ReportType.NONE)
                .instructionType(InstructionsType.TEXT)
                .language("en-GB")

            if (arriveAt == null){
                routeCalculationDescriptor.arriveAt(Calendar.getInstance().time)
            }else{
                routeCalculationDescriptor.arriveAt(arriveAt)
            }

            val vehicleRestrictions = VehicleRestrictions.Builder()
            vehicleLoadType?.let {
                vehicleRestrictions.vehicleLoadType(it)
            }
            val vehicleDimensions = VehicleDimensions.Builder()
            vehicleHeight?.let {
                vehicleDimensions.vehicleHeightInMeters(it)
            }
            vehicleWeight?.let {
                vehicleDimensions.vehicleWeightInKg(it)
            }
            vehicleLength?.let {
                vehicleDimensions.vehicleLengthInMeters(it)
            }
            vehicleWidth?.let {
                vehicleDimensions.vehicleWidthInMeters(it)
            }

            val combustionVehicleDescriptor = CombustionVehicleDescriptor.Builder()
                .vehicleDimensions(vehicleDimensions.build())
                .vehicleRestrictions(vehicleRestrictions.build())
                .build()

            routeSpecification = RouteSpecification.Builder(departure!!, destination!!)
                .routeCalculationDescriptor(routeCalculationDescriptor.build())
                .combustionVehicleDescriptor(combustionVehicleDescriptor)
                .build()

            routingApi.planRoute(routeSpecification, object : RouteCallback {
                override fun onSuccess(routePlan: RoutePlan) {
                    handleSuccessResponse(routePlan)
                }

                override fun onError(error: RoutingException) {
                    handleErrorResponse(error)
                }
            })
        } else {
            timerHandler.removeCallbacks(requestRouteRunnable)
            timerHandler.postDelayed(requestRouteRunnable, ROUTE_RECALCULATION_DELAY.toLong())
        }
    }

    private fun handleSuccessResponse(routePlan: RoutePlan) {
        hideDialog(dialogInProgress)
        if (routePlan.routes.isNotEmpty()) {
            val fullRoute = routePlan.routes.first()
            val currentTravelTime = fullRoute.summary.travelTimeInSeconds
            if (previousTravelTime != currentTravelTime) {
                saveManeuverList(fullRoute.guidance.instructions)
                val travelDifference = previousTravelTime - currentTravelTime
                if (previousTravelTime != 0) {
                    showWarningSnackbar(prepareWarningMessage(travelDifference))
                }
                previousTravelTime = currentTravelTime
                displayRouteOnMap(fullRoute.getCoordinates())
                //val departureTimeString = fullRoute.summary.departureTime
            } else {
                infoSnackbar.show()
            }
        }
        timerHandler.removeCallbacks(requestRouteRunnable)
        timerHandler.postDelayed(
            requestRouteRunnable,
            ROUTE_RECALCULATION_DELAY.toLong()
        )
    }

    private fun handleErrorResponse(error: RoutingException) {
        hideDialog(dialogInProgress)
        Toast.makeText(
            this@CountdownActivity,
            getString(R.string.toast_error_message_cannot_find_route),
            Toast.LENGTH_LONG
        ).show()
        this@CountdownActivity.finish()
    }

    private fun prepareWarningMessage(travelDifference: Int): String {
        val travelTimeDifference =
            formatTimeFromSecondsDisplayWithSeconds(travelDifference.toLong())
        return getString(
            R.string.dialog_recalculation_info,
            getTimeInfoWithPrefix(travelDifference, travelTimeDifference)
        )
    }

    private fun formatTimeFromSecondsDisplayWithSeconds(secondsTotal: Long): String {
        return formatTimeFromSeconds(secondsTotal, true)
    }

    private fun formatTimeFromSecondsDisplayWithoutSeconds(secondsTotal: Long): String {
        return formatTimeFromSeconds(secondsTotal, false)
    }

    private fun getTimeInfoWithPrefix(
        travelDifference: Int,
        travelTimeDifference: String
    ): String {
        val prefix = if (travelDifference < 0) "-" else "+"
        return prefix + travelTimeDifference
    }

    private fun displayRouteOnMap(coordinates: List<LatLng>) {
        val routeBuilder = RouteBuilder(coordinates)
            .startIcon(departureIcon)
            .endIcon(destinationIcon)
        tomtomMap.clear()
        tomtomMap.addRoute(routeBuilder)
        tomtomMap.displayRoutesOverview()
    }

    private fun formatTimeFromSeconds(
        secondsTotal: Long,
        showSeconds: Boolean
    ): String {
        var secondsTotal = secondsTotal
        val timeFormatHoursMinutes = "H'h' m'min'"
        val timeFormatMinutes = "m'min'"
        val timeFormatSeconds = " s'sec'"

        val hours = TimeUnit.SECONDS.toHours(secondsTotal)
        val minutes =
            TimeUnit.SECONDS.toMinutes(secondsTotal) - TimeUnit.HOURS.toMinutes(hours)
        var timeFormat = ""

        if (hours != 0L) {
            timeFormat = timeFormatHoursMinutes
        } else {
            if (minutes != 0L) {
                timeFormat = timeFormatMinutes
            }
        }

        if (showSeconds) {
            timeFormat += timeFormatSeconds
        }
        secondsTotal = abs(secondsTotal)
        return DateFormat.format(
            timeFormat,
            TimeUnit.SECONDS.toMillis(secondsTotal)
        ) as String
    }

    private fun showWarningSnackbar(warningMessage: String) {
        warningSnackbar.apply {
            this.setText(warningMessage)
            this.show()
        }
    }

    private fun requestRoute(routeSpecification: RouteSpecification) {
        if (!isInPauseMode) {
            routingApi.planRoute(routeSpecification, object : RouteCallback {
                override fun onSuccess(routePlan: RoutePlan) {
                    handleSuccessResponse(routePlan)
                }

                override fun onError(error: RoutingException) {
                    handleErrorResponse(error)
                }
            })
        } else {
            timerHandler.removeCallbacks(requestRouteRunnable)
            timerHandler.postDelayed(requestRouteRunnable, ROUTE_RECALCULATION_DELAY.toLong())
        }
    }

    private fun saveManeuverList(instructions: List<Instruction>) {
        this.instructions = instructions
    }

    private fun initTomTomServices() {
        routingApi = OnlineRoutingApi.create(applicationContext, BuildConfig.ROUTING_API_KEY)
        val mapKeys = mapOf(ApiKeyType.MAPS_API_KEY to BuildConfig.MAPS_API_KEY)
        val mapProperties = MapProperties.Builder().keys(mapKeys).build()
        val mapFragment = MapFragment.newInstance(mapProperties)
        supportFragmentManager
            .beginTransaction()
            .replace(R.id.mapFragment, mapFragment)
            .commit()
        mapFragment.getAsyncMap(this)
    }

    private fun initToolbarSettings() {
        setSupportActionBar(custom_toolbar as Toolbar)
        supportActionBar?.apply {
            this.setDisplayHomeAsUpEnabled(true)
            this.setDisplayShowHomeEnabled(true)
            this.setDisplayShowTitleEnabled(false)
        }
    }

    private fun initActivitySettings(settings: Bundle?) {
        departureIcon =
            Icon.Factory.fromResources(this@CountdownActivity, R.drawable.ic_map_route_departure)
        destinationIcon =
            Icon.Factory.fromResources(this@CountdownActivity, R.drawable.ic_map_route_destination)
        navEnableIcon =
            Icon.Factory.fromResources(this@CountdownActivity, R.drawable.navigation_enable_64)
        navDisableIcon =
            Icon.Factory.fromResources(this@CountdownActivity, R.drawable.navigate_disable_64)

        initBundleSettings(settings)
        img_countdown_by_what.setImageResource(getTravelModeIcon(travelMode!!))

        previousTravelTime = 0

        createWarningSnackBar()
        createInfoSnackBar()
        createDialogInProgress()
    }

    private fun initBundleSettings(settings: Bundle?) {
        val calendar = Calendar.getInstance()
        settings?.let {
            val arriveAtMillis = settings.getLong(BUNDLE_ARRIVE_AT)
            calendar.timeInMillis = arriveAtMillis
            arriveAt = calendar.time

            val departAtMillis = settings.getLong(BUNDLE_DEPART_AT, 0L)
            if (departAtMillis != 0L) {
                calendar.timeInMillis = departAtMillis
                departAt = calendar.time
            }

            departure = LatLng(
                settings.getDouble(BUNDLE_DEPARTURE_LAT),
                settings.getDouble(BUNDLE_DEPARTURE_LNG)
            )
            destination = LatLng(
                settings.getDouble(BUNDLE_DESTINATION_LAT),
                settings.getDouble(BUNDLE_DESTINATION_LNG)
            )
            travelMode = TravelMode.valueOf(
                settings.getString(BUNDLE_BY_WHAT)!!.toUpperCase(Locale.getDefault())
            )
            routeType = RouteType.valueOf(
                settings.getString(BUNDLE_ROUTE_TYPE)!!.toUpperCase(Locale.getDefault())
            )
            val vehicleTypeString = settings.getString(BUNDLE_VEHICLE_LOAD_TYPE)
            vehicleTypeString?.let {
                //Should not set any type if its none
                if (vehicleTypeString != MyVehicleLoadType.NONE.name) {
                    vehicleLoadType = VehicleLoadType.valueOf(
                        it.toUpperCase(Locale.getDefault())
                    )
                }
            }
            val mVehicleHeight = settings.getDouble(BUNDLE_VEHICLE_HEIGHT, 0.0)
            if (mVehicleHeight != 0.0) {
                vehicleHeight = mVehicleHeight
            }
            val mVehicleLength = settings.getDouble(BUNDLE_VEHICLE_LENGTH, 0.0)
            if (mVehicleLength != 0.0) {
                vehicleLength = mVehicleLength
            }
            val mVehicleWidth = settings.getDouble(BUNDLE_VEHICLE_WIDTH, 0.0)
            if (mVehicleWidth != 0.0) {
                vehicleWidth = mVehicleWidth
            }
            val mVehicleWeight = settings.getInt(BUNDLE_VEHICLE_WEIGHT, 0)
            if (mVehicleWeight != 0) {
                vehicleWeight = mVehicleWeight
            }
            //preparationTime = settings.getInt(BUNDLE_PREPARATION_TIME)
        }
    }

    private fun getTravelModeIcon(selectedTravelMode: TravelMode): Int {
        return when (selectedTravelMode) {
            TravelMode.TAXI -> R.drawable.button_main_travel_mode_cab
            TravelMode.PEDESTRIAN -> R.drawable.button_main_travel_mode_by_foot
            TravelMode.CAR -> R.drawable.button_main_travel_mode_car
            else -> R.drawable.button_main_travel_mode_car
        }
    }

    private fun createWarningSnackBar() {
        val view = findViewById<ViewGroup>(android.R.id.content)
        warningSnackbar = CustomSnackbar.make(
            view,
            BaseTransientBottomBar.LENGTH_INDEFINITE,
            R.layout.snackbar_recalculation_warning
        ).apply {
            this.setAction(getString(R.string.button_ok)) { warningSnackbar.dismiss() }
        }
        setCustomSnackbar(warningSnackbar)
    }

    private fun createInfoSnackBar() {
        val view = findViewById<ViewGroup>(android.R.id.content)
        infoSnackbar = CustomSnackbar.make(
            view,
            BaseTransientBottomBar.LENGTH_LONG,
            R.layout.snackbar_recalculation_info
        ).apply {
            this.setText(getString(R.string.dialog_recalculation_no_changes))
        }
        setCustomSnackbar(infoSnackbar)
    }

    private fun createDialogInProgress() {
        val inflater = layoutInflater
        val builder = AlertDialog.Builder(this@CountdownActivity).apply {
            this.setView(inflater.inflate(R.layout.dialog_in_progress, null))
        }
        dialogInProgress = builder.create().apply {
            this.setCanceledOnTouchOutside(false)
        }
    }

    private fun setCustomSnackbar(snackbar: CustomSnackbar) {
        val transparentColor = ContextCompat.getColor(this@CountdownActivity, R.color.transparent)
        snackbar.view.setBackgroundColor(transparentColor)
        val paddingSnackbar = resources.getDimension(R.dimen.padding_snackbar).toInt()
        snackbar.view.setPadding(paddingSnackbar, paddingSnackbar, paddingSnackbar, paddingSnackbar)
    }

    private fun hideDialog(dialog: Dialog?) {
        if (dialogIsShowing(dialog)) {
            dialog?.dismiss()
        }
    }

    private fun showDialog(dialog: Dialog?) {
        if (!dialogIsShowing(dialog)) {
            dialog?.show()
        }
    }

    private fun dialogIsShowing(dialog: Dialog?): Boolean {
        return dialog != null && dialog.isShowing
    }

    override fun onDestroy() {
        super.onDestroy()
        tomtomMap.removeLocationUpdateListener(listener)
    }
}
