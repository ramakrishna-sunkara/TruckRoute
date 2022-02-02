/**
 * Copyright (c) 2015-2021 TomTom N.V. All rights reserved.
 *
 * This software is the proprietary copyright of TomTom N.V. and its subsidiaries and may be used
 * for internal evaluation purposes or commercial use strictly subject to separate licensee
 * agreement between you and TomTom. If you are the licensee, you are only permitted to use
 * this Software in accordance with the terms of your license agreement. If you are not the
 * licensee then you are not authorised to use this software in any manner and should
 * immediately return it to TomTom N.V.
 */
package com.tomtom.timetoleave.driving.utils;

import android.content.Context;
import android.location.Location;

import java.util.List;

public class GpsCsvSimulator extends BaseSimulator {

    private final static String DRIVING_PROBE_CSV_FILE = "probes/simple_route.csv";
    private final static int DEFAULT_HEADER_LINES = 0;
    private final List<Location> locations;

    public GpsCsvSimulator(Context context, LocationInterpolator locationInterpolator) {
        super(locationInterpolator);
        locations = new CsvLocationsProvider(context, DRIVING_PROBE_CSV_FILE, DEFAULT_HEADER_LINES).getLocations();
    }

    @Override
    protected List<Location> getLocations() {
        return locations;
    }

}
