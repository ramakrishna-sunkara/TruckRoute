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
package com.tomtom.timetoleave.driving.matching.map;

import com.tomtom.online.sdk.common.location.LatLng;
import com.tomtom.online.sdk.map.driving.Matcher;
import com.tomtom.online.sdk.map.driving.MatcherFactory;
import com.tomtom.online.sdk.map.driving.MatcherListener;
import com.tomtom.online.sdk.matching.MatchingDataProvider;
import com.tomtom.timetoleave.driving.AbstractTrackingPresenter;
import com.tomtom.timetoleave.driving.ChevronMatcherUpdater;
import com.tomtom.timetoleave.driving.FunctionalExampleModel;
import com.tomtom.timetoleave.driving.utils.BaseSimulator;
import com.tomtom.timetoleave.driving.utils.GpsCsvSimulator;
import com.tomtom.timetoleave.driving.utils.interpolator.BasicInterpolator;

public class MapMatchingPresenter extends AbstractTrackingPresenter {

    private final static LatLng PROBE_INIT_POSITION = new LatLng(51.745516, 19.438692);

    private Matcher matcher;

    @Override
    public FunctionalExampleModel getModel() {
        return new MapMatchingFunctionalExample();
    }

    @Override
    protected void onPresenterCreated() {
        super.onPresenterCreated();
        createMatcher();
        restoreSimulator();
    }

    @Override
    protected void onPresenterRestored() {
        super.onPresenterRestored();
        createMatcher();
    }

    @Override
    protected void onPresenterReady() {
        super.onPresenterReady();
        blockZoomGestures();
        startTracking();
    }

    @Override
    public void cleanup() {
        //tag::doc_dispose_map_matcher[]
        matcher.dispose();
        //end::doc_dispose_map_matcher[]
        super.cleanup();
    }

    @Override
    protected BaseSimulator createSimulator() {
        return new GpsCsvSimulator(getContext(), new BasicInterpolator());
    }

    @Override
    protected LatLng getDefaultMapPosition() {
        return PROBE_INIT_POSITION;
    }

    private void createMatcher() {
        //tag::doc_create_map_matcher_provider[]
        MatchingDataProvider matchingDataProvider = tomtomMap.asMatchingDataProvider();
        //end::doc_create_map_matcher_provider[]

        MatcherListener matcherListener = new ChevronMatcherUpdater(getChevron(), tomtomMap);
        //tag::doc_create_matcher[]
        matcher = MatcherFactory.createMatcher(matchingDataProvider);
        matcher.setMatcherListener(matcherListener);
        //end::doc_create_matcher[]
    }

    @Override
    protected BaseSimulator.SimulatorCallback getSimulatorCallback() {
        return simulatorCallback;
    }

    private BaseSimulator.SimulatorCallback simulatorCallback = location -> {
        //tag::doc_update_map_matcher[]
        matcher.match(location);
        //end::doc_update_map_matcher[]
    };

}