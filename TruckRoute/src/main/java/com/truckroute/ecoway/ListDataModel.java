package com.truckroute.ecoway;

import com.tomtom.online.sdk.routing.data.RouteType;
import com.tomtom.online.sdk.routing.data.VehicleLoadType;

import java.io.Serializable;
import java.util.List;

public class ListDataModel implements Serializable {
    private FilterType filterType;
    private RouteType selectedRouteType;

    public FilterType getFilterType() {
        return filterType;
    }

    public void setFilterType(FilterType filterType) {
        this.filterType = filterType;
    }

    public RouteType getSelectedRouteType() {
        return selectedRouteType;
    }

    public void setSelectedRouteType(RouteType selectedRouteType) {
        this.selectedRouteType = selectedRouteType;
    }
}
