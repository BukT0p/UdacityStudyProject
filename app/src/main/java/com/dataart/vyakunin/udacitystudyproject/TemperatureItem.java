package com.dataart.vyakunin.udacitystudyproject;

import android.database.Cursor;

import java.util.ArrayList;
import java.util.Date;

public class TemperatureItem {
    private double dayTemp;
    private double nightTemp;

    public double getDayTemp() {
        return dayTemp;
    }

    public void setDayTemp(double dayTemp) {
        this.dayTemp = dayTemp;
    }

    public double getNightTemp() {
        return nightTemp;
    }

    public void setNightTemp(double nightTemp) {
        this.nightTemp = nightTemp;
    }

    public static ArrayList<TemperatureItem> populateFromCursor(Cursor data) {
        ArrayList<TemperatureItem> result = new ArrayList<TemperatureItem>(data.getCount());
        if (data.moveToFirst()) {
            do {
                TemperatureItem item = new TemperatureItem();
                // Read high temperature from cursor
                double high = data.getDouble(HomeFragment.COL_WEATHER_MAX_TEMP);
                item.setDayTemp(Utility.convertTemperature(high,true));

                // Read low temperature from cursor
                double low = data.getDouble(HomeFragment.COL_WEATHER_MIN_TEMP);
                item.setNightTemp(Utility.convertTemperature(low,true));

                result.add(item);
            } while (data.moveToNext());
        }
        return result;
    }
}
