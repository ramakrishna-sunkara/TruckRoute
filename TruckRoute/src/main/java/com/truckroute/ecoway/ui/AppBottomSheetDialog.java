package com.truckroute.ecoway.ui;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.tomtom.online.sdk.routing.data.RouteType;
import com.tomtom.online.sdk.routing.data.TravelMode;
import com.truckroute.ecoway.R;

import java.util.ArrayList;
import java.util.List;

public class AppBottomSheetDialog extends BottomSheetDialogFragment {
    private static final String ARG_NAME = "item_name";
    public static final String TAG = "AppBottomSheetDialog";
    private Listener mListener;
    private String header;
    private Context mContext;
    private OperatorAdapter operatorAdapter;
    private TextView headerTV;
    private RecyclerView recyclerView;
    private FilterType filterType;
    private String selectedValue;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_operator_list_dialog_list_dialog, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {

        headerTV = view.findViewById(R.id.title);
        recyclerView = view.findViewById(R.id.list);

        if (getArguments() != null) {
            filterType = (FilterType) getArguments().getSerializable(Constants.EXTRA_FILTER_TYPE);
            selectedValue = getArguments().getString(Constants.EXTRA_SELECTED_VALUE);
            initView(filterType, selectedValue);
        }else {
            dismiss();
        }
        view.findViewById(R.id.dismiss).setOnClickListener(v -> dismiss());
    }

    private void initView(FilterType filterType, String selectedValue) {
        String header = "";
            header = getString(R.string.select);
            switch (filterType){
                case VEHICLE_ROUTE_TYPE:
                    header = getString(R.string.route_type);
                    RouteType[] routeTypes = RouteType.values();
                    List<String> routeTypesListData = new ArrayList<>();
                    for (RouteType routeType :
                            routeTypes) {
                        routeTypesListData.add(routeType.name());
                    }
                    operatorAdapter = new OperatorAdapter(routeTypesListData, selectedValue);
                    break;
                case TRAVEL_MODE:
                    header = getString(R.string.travel_mode);
                    TravelMode[] travelModes = TravelMode.values();
                    List<String> travelModesListData = new ArrayList<>();
                    for (TravelMode travelMode :
                            travelModes) {
                        travelModesListData.add(travelMode.name());
                    }
                    operatorAdapter = new OperatorAdapter(travelModesListData, selectedValue);
                    break;
                case VEHICLE_LOAD_TYPE:
                    header = getString(R.string.vehicle_load_type);
                    MyVehicleLoadType[] myVehicleLoadTypes = MyVehicleLoadType.values();
                    List<String> myVehicleLoadTypesListData = new ArrayList<>();
                    for (MyVehicleLoadType myVehicleLoadType:
                            myVehicleLoadTypes) {
                        myVehicleLoadTypesListData.add(myVehicleLoadType.toString());
                    }
                    operatorAdapter = new OperatorAdapter(myVehicleLoadTypesListData, selectedValue);
                    break;
            }
        headerTV.setText(header);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(operatorAdapter);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        mContext = context;

        final Fragment parent = getParentFragment();
        if (parent != null) {
            mListener = (Listener) parent;
        } else {
            mListener = (Listener) context;
        }
    }

    @Override
    public void onDetach() {
        mListener = null;
        super.onDetach();
    }

    public interface Listener {
        void onFilterSelected(String listValue);
    }

    private class ViewHolder extends RecyclerView.ViewHolder {

        final TextView text;
        ImageView logo;

        ViewHolder(LayoutInflater inflater, ViewGroup parent) {
            // TODO: Customize the item layout
            super(inflater.inflate(R.layout.fragment_operator_list_dialog_list_dialog_item, parent, false));
            text = itemView.findViewById(R.id.txt_option_name);
            logo = itemView.findViewById(R.id.iv_select_icon);
        }
    }

    private class OperatorAdapter extends RecyclerView.Adapter<ViewHolder> {

        private List<String> listData;
        private String selectedValue;

        OperatorAdapter(List<String> listData, String selectedValue) {
            this.listData = listData;
            this.selectedValue = selectedValue;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            return new ViewHolder(LayoutInflater.from(parent.getContext()), parent);
        }

        @Override
        public void onBindViewHolder(final ViewHolder holder, final int position) {
            final String listValue = listData.get(position);
            holder.text.setText(listValue);
            holder.logo.setVisibility(listValue.equals(selectedValue) ? View.VISIBLE : View.INVISIBLE);
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mListener.onFilterSelected(listValue);
                    dismiss();
                }
            });
        }

        @Override
        public int getItemCount() {
            return listData.size();
        }
    }
}
