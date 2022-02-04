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
package com.truckroute.ecoway.driving.matching.route;

import com.truckroute.ecoway.driving.AbstractTrackingFragment;
import com.truckroute.ecoway.driving.views.OptionsButtonsView;

public class RouteMatchingFragment extends AbstractTrackingFragment<RouteMatchingPresenter> {

    @Override
    public void onChange(boolean[] oldValues, boolean[] newValues) {

    }

    @Override
    protected RouteMatchingPresenter createPresenter() {
        return new RouteMatchingPresenter();
    }

    @Override
    protected void onOptionsButtonsView(final OptionsButtonsView view) {

    }

    @Override
    protected boolean isDescriptionVisible() {
        return true;
    }

}
