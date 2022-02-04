package com.truckroute.ecoway;

import java.util.Arrays;

public enum MyVehicleLoadType {
    NONE("None"),
    US_Hazmat_Class_1("Explosives"),
    US_Hazmat_Class_2("Compressed gas"),
    US_Hazmat_Class_3("Flammable liquids"),
    US_Hazmat_Class_4("Flammable solids"),
    US_Hazmat_Class_5("Oxidizers"),
    US_Hazmat_Class_6("Poisons"),
    US_Hazmat_Class_7("Radioactive"),
    US_Hazmat_Class_8("Corrosives"),
    US_Hazmat_Class_9("Miscellaneous");

    private String name;

    MyVehicleLoadType(final String value) {
        this.name = value;
    }

    @Override
    public String toString() {
        return name;
    }

    public static MyVehicleLoadType getLoadType(String name){
        for (MyVehicleLoadType vehicleLoadType :
                MyVehicleLoadType.values()) {
            if (vehicleLoadType.toString().equals(name)) {
                return vehicleLoadType;
            }
        }
        return null;
    }
}