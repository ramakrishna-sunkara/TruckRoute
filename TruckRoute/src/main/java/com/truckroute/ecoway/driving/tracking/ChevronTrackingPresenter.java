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
package com.truckroute.ecoway.driving.tracking;

import com.tomtom.online.sdk.common.location.LatLng;
import com.tomtom.online.sdk.map.Route;
import com.truckroute.ecoway.driving.AbstractRoutingPresenter;
import com.truckroute.ecoway.driving.ChevronSimulatorUpdater;
import com.truckroute.ecoway.driving.FunctionalExampleModel;
import com.truckroute.ecoway.driving.utils.BaseSimulator;
import com.truckroute.ecoway.driving.utils.RouteSimulator;
import com.truckroute.ecoway.driving.utils.interpolator.BasicInterpolator;

import java.util.List;

public class ChevronTrackingPresenter extends AbstractRoutingPresenter {

    @Override
    protected BaseSimulator.SimulatorCallback getSimulatorCallback() {
        return new ChevronSimulatorUpdater(getChevron());
    }

    @Override
    public FunctionalExampleModel getModel() {
        return new ChevronTrackingFunctionalExample();
    }

    @Override
    protected BaseSimulator createSimulator() {
        //Route is stored within Maps SDK
        List<Route> routes = tomtomMap.getRoutes();
        return new RouteSimulator(routes, new BasicInterpolator());
    }

    @Override
    protected LatLng getDefaultMapPosition() {
        return ROUTE_CONFIG.getOrigin();
    }

}
