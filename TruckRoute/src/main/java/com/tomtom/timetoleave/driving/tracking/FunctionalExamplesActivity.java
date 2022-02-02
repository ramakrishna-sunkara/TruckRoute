/**
 * Copyright (c) 2015-2021 TomTom N.V. All rights reserved.
 * <p>
 * This software is the proprietary copyright of TomTom N.V. and its subsidiaries and may be used
 * for internal evaluation purposes or commercial use strictly subject to separate licensee
 * agreement between you and TomTom. If you are the licensee, you are only permitted to use
 * this Software in accordance with the terms of your license agreement. If you are not the
 * licensee then you are not authorised to use this software in any manner and should
 * immediately return it to TomTom N.V.
 */
package com.tomtom.timetoleave.driving.tracking;

import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.google.common.base.Optional;
import com.tomtom.online.sdk.common.location.LatLng;
import com.tomtom.online.sdk.common.permission.AppPermissionHandler;
import com.tomtom.online.sdk.map.ApiKeyType;
import com.tomtom.online.sdk.map.BaseGpsPositionIndicator;
import com.tomtom.online.sdk.map.CameraPosition;
import com.tomtom.online.sdk.map.GpsIndicator;
import com.tomtom.online.sdk.map.MapFragment;
import com.tomtom.online.sdk.map.MapProperties;
import com.tomtom.online.sdk.map.MapView;
import com.tomtom.online.sdk.map.OnMapReadyCallback;
import com.tomtom.online.sdk.map.TomtomMap;
import com.tomtom.timetoleave.R;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import timber.log.Timber;

//tag::doc_implement_tt_interfaces[]
public class FunctionalExamplesActivity extends AppCompatActivity {
    //end::doc_implement_tt_interfaces[]

    public static final String CURRENT_EXAMPLE_KEY = "CURRENT_EXAMPLE";

    public static final int EMPTY_EXAM = 1000;
    //Just for documentation - this callback is not registered.
    @SuppressWarnings("unused")
    private final OnMapReadyCallback onMapReadyCallbackSaveLogs = new OnMapReadyCallback() {
        //tag::doc_collect_logs_to_file_in_onready_callback[]
        @Override
        public void onMapReady(@NonNull TomtomMap tomtomMap) {
            //tomtomMap.collectLogsToFile(SampleApp.LOG_FILE_PATH);
        }
        //end::doc_collect_logs_to_file_in_onready_callback[]
    };
    private int currentExampleId = EMPTY_EXAM;
    private MapView mapView;
    //end::doc_implement_on_map_ready_callback[]
    private TomtomMap tomtomMap;
    //tag::doc_implement_on_map_ready_callback[]
    private final OnMapReadyCallback onMapReadyCallback =
            new OnMapReadyCallback() {
                @Override
                public void onMapReady(TomtomMap map) {
                    //Map is ready here
                    tomtomMap = map;
                    tomtomMap.setMyLocationEnabled(true);
                    //tomtomMap.collectLogsToFile(SampleApp.LOG_FILE_PATH);
                }
            };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Timber.d("onCreate()");
        super.onCreate(savedInstanceState);
        inflateActivity();

        mapView = new MapView(getApplicationContext());
        mapView.addOnMapReadyCallback(onMapReadyCallback);
        mapView.addOnMapReadyCallback(tomtomMap -> {
            initLocationPermissions();
        });
        mapView.setId(R.id.map_view);

        FrameLayout frameLayout = findViewById(R.id.map_container);
        frameLayout.addView(mapView);

        Timber.d("Phone language " + Locale.getDefault().getLanguage());
        Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.functional_example_control_container);
        if (fragment != null) {
            setCurrentExample();
        }
    }

    private void initLocationPermissions() {
        AppPermissionHandler permissionHandler = new AppPermissionHandler(this);
        permissionHandler.addLocationChecker();
        permissionHandler.askForNotGrantedPermissions();
    }

    private void inflateActivity() {
        setContentView(R.layout.activity_live_navigation);
    }

    @Override
    protected void onStart() {
        super.onStart();
        mapView.onStart();
        mapView.addOnMapReadyCallback(map -> map.getUiSettings().setCopyrightsViewAdapter(() -> this));
        mapView.addOnMapReadyCallback(map -> map.getUiSettings().getCurrentLocationView().setCurrentLocationViewAdapter(() -> this));
    }

    @Override
    protected void onResume() {
        super.onResume();
        mapView.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mapView.onPause();
    }

    @Override
    protected void onDestroy() {
        mapView.onDestroy();
        super.onDestroy();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        //outState.putInt(CURRENT_EXAMPLE_KEY, currentExampleId);
        super.onSaveInstanceState(outState);
    }

    public void setCurrentExample() {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.functional_example_control_container, new ChevronTrackingFragment(), "ChevronTrackingFragment")
                .commit();
    }

    //tag::doc_map_permissions[]
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        tomtomMap.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }
    //end::doc_map_permissions[]

    @SuppressWarnings("unused")
    private void initMap() {
        int mapFragmentId = 0;
        //tag::doc_obtain_fragment_reference[]
        MapFragment mapFragment = (MapFragment) getSupportFragmentManager().findFragmentById(mapFragmentId);
        //end::doc_obtain_fragment_reference[]
        //tag::doc_initialise_map[]
        mapFragment.getAsyncMap(onMapReadyCallback);
        //end::doc_initialise_map[]
    }

    @SuppressWarnings("unused")
    private void initMapProperties() {
        //tag::doc_initial_map_properties[]
        Map<ApiKeyType, String> keysMap = new HashMap<>();
        keysMap.put(ApiKeyType.MAPS_API_KEY, "online-maps-key");
        CameraPosition cameraPosition = CameraPosition.builder()
                .focusPosition(new LatLng(12.34, 23.45))
                .zoom(10.0)
                .bearing(24.0)
                .build();
        MapProperties mapProperties = new MapProperties.Builder()
                .customStyleUri("asset://styles/style.json")
                .backgroundColor(Color.BLUE)
                .keys(keysMap)
                .cameraPosition(cameraPosition)
                .build();
        //end::doc_initial_map_properties[]
    }

    @SuppressWarnings("unused")
    private void changeGPSIndicatorRadiusColor() {
        new OnMapReadyCallback() {
            @Override
            public void onMapReady(@NonNull TomtomMap tomtomMap) {
                final int COLOR_RGBA = Color.argb(128, 128, 128, 128);
                GpsIndicator gpsIndicator = tomtomMap.getGpsPositionIndicator().get();
                //tag::doc_obtain_gps_indicator[]
                Optional<GpsIndicator> indicatorOptional = tomtomMap.getGpsPositionIndicator();
                if (indicatorOptional.isPresent()) {
                    gpsIndicator = indicatorOptional.get();
                }
                //end::doc_obtain_gps_indicator[]

                //tag::doc_set_gps_indicator_active_radius[]
                gpsIndicator.setInaccuracyAreaColor(COLOR_RGBA);
                //end::doc_set_gps_indicator_active_radius[]

                //tag::doc_set_gps_indicator_inactive_radius[]
                gpsIndicator.setDimmedInaccuracyAreaColor(COLOR_RGBA);
                //end::doc_set_gps_indicator_inactive_radius[]
            }
        };
    }
    //end::doc_custom_gps_position_indicator

    /**
     * Custom GPS position indicator that forces accuracy to 0.
     */
    //tag::doc_custom_gps_position_indicator
    static class CustomGpsPositionIndicator extends BaseGpsPositionIndicator {
        private static final long serialVersionUID = -2164040010108911434L;

        //To use this class, call setCustomGpsPositionIndicator on TomtomMap:
        //tomtomMap.setGpsPositionIndicator(new CustomGpsPositionIndicator());

        @Override
        public void setLocation(Location location) {
            setLocation(new LatLng(location), 0.0, 0.0, location.getTime());
        }

        @Override
        public void setLocation(LatLng latLng, double bearingInDegrees, double accuracyInMeters, long timeInMillis) {
            super.show();
            super.setDimmed(false);
            super.setLocation(latLng, 0.0, 0.0, timeInMillis);
        }
    }
}
