package com.dataart.vyakunin.udacitystudyproject.data;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;

import java.util.regex.Matcher;

/**
 * Created by vyakunin on 11/7/2014.
 */
public class WeatherProvider extends ContentProvider {
    private static final int WEATHER = 100;
    private static final int WEATHER_WITH_LOCATION = 101;
    private static final int WEATHER_WITH_LOCATION_AND_DATE = 102;
    private static final int LOCATION = 300;
    private static final int LOCATION_ID = 301;

    private UriMatcher matcher = buildUriMatcher();
    private WeatherDBHelper db;
    private static final SQLiteQueryBuilder weatherByLocationSettingsQueryBuilder;

    static {
        weatherByLocationSettingsQueryBuilder = new SQLiteQueryBuilder();
        weatherByLocationSettingsQueryBuilder.setTables(WeatherContract.WeatherEntry.TABLE_NAME + " INNER JOIN " +
                WeatherContract.LocationEntry.TABLE_NAME + " ON " +
                WeatherContract.WeatherEntry.TABLE_NAME + "." + WeatherContract.WeatherEntry.COLUMN_LOC_KEY + " = " +
                WeatherContract.LocationEntry.TABLE_NAME + "." + WeatherContract.LocationEntry._ID);
    }

    private static final String locationSettingsSelection =
            WeatherContract.LocationEntry.TABLE_NAME + "." + WeatherContract.LocationEntry.COLUMN_LOCATION_SETTING + "=?";

    private static final String locationSettingsWithStartDateSelection =
            WeatherContract.LocationEntry.TABLE_NAME + "." + WeatherContract.LocationEntry.COLUMN_LOCATION_SETTING + "=? AND " +
                    WeatherContract.WeatherEntry.COLUMN_DATETEXT + " >= ?";
    private static final String locationSettingsWithDaySelection =
            WeatherContract.LocationEntry.TABLE_NAME + "." + WeatherContract.LocationEntry.COLUMN_LOCATION_SETTING + "=? AND " +
                    WeatherContract.WeatherEntry.COLUMN_DATETEXT + " = ?";

    private Cursor getWeatherByLocationSetting(Uri uri, String[] projection, String sortOrder) {
        String locationSetting = WeatherContract.WeatherEntry.getLocationSettingFromUri(uri);
        String startDate = WeatherContract.WeatherEntry.getStartDateFromUri(uri);

        String[] selectionArgs;
        String selection;
        if (startDate == null) {
            selection = locationSettingsSelection;
            selectionArgs = new String[]{locationSetting};
        } else {
            selection = locationSettingsWithStartDateSelection;
            selectionArgs = new String[]{locationSetting, startDate};
        }
        return weatherByLocationSettingsQueryBuilder.query(db.getReadableDatabase(), projection, selection, selectionArgs, null, null, sortOrder);
    }

    private Cursor getWeatherByLocationSettingWithDate(Uri uri, String[] projection, String sortOrder) {
        String locationSetting = WeatherContract.WeatherEntry.getLocationSettingFromUri(uri);
        String day = WeatherContract.WeatherEntry.getDateFromUri(uri);
        return weatherByLocationSettingsQueryBuilder.query(db.getReadableDatabase(), projection, locationSettingsWithDaySelection, new String[]{locationSetting, day}, null, null, sortOrder);
    }

    private static UriMatcher buildUriMatcher() {
        final UriMatcher matcher = new UriMatcher(UriMatcher.NO_MATCH);
        final String authority = WeatherContract.CONTENT_AUTHORITY;
        matcher.addURI(authority, WeatherContract.PATH_WEATHER, WEATHER);
        matcher.addURI(authority, WeatherContract.PATH_WEATHER + "/*", WEATHER_WITH_LOCATION);
        matcher.addURI(authority, WeatherContract.PATH_WEATHER + "/*/*", WEATHER_WITH_LOCATION_AND_DATE);

        matcher.addURI(authority, WeatherContract.PATH_LOCATION, LOCATION);
        matcher.addURI(authority, WeatherContract.PATH_LOCATION + "/#", LOCATION_ID);
        return matcher;
    }

    @Override
    public boolean onCreate() {
        db = new WeatherDBHelper(getContext());
        return db != null;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        Cursor cursor;
        switch (matcher.match(uri)) {
            case WEATHER_WITH_LOCATION_AND_DATE: {
                cursor = getWeatherByLocationSettingWithDate(uri, projection, sortOrder);
                break;
            }
            case WEATHER_WITH_LOCATION: {
                cursor = getWeatherByLocationSetting(uri, projection, sortOrder);
                break;
            }
            case WEATHER: {
                cursor = db.getReadableDatabase().query(WeatherContract.WeatherEntry.TABLE_NAME, projection, selection, selectionArgs, null, null, sortOrder);
                break;
            }
            case LOCATION: {
                cursor = db.getReadableDatabase().query(
                        WeatherContract.LocationEntry.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder
                );
                break;
            }
            case LOCATION_ID: {
                cursor = db.getReadableDatabase().query(
                        WeatherContract.LocationEntry.TABLE_NAME,
                        projection,
                        WeatherContract.LocationEntry._ID + " = '" + ContentUris.parseId(uri) + "'",
                        null,
                        null,
                        null,
                        sortOrder
                );
                break;
            }
            default:
                throw new UnsupportedOperationException("Unknown Uri: " + uri);
        }
        cursor.setNotificationUri(getContext().getContentResolver(), uri);
        return cursor;
    }

    @Override
    public String getType(Uri uri) {
        final int match = matcher.match(uri);
        switch (match) {
            case WEATHER_WITH_LOCATION_AND_DATE:
                return WeatherContract.WeatherEntry.CONTENT_ITEM;
            case WEATHER_WITH_LOCATION:
                return WeatherContract.WeatherEntry.CONTENT_DIR;
            case WEATHER:
                return WeatherContract.WeatherEntry.CONTENT_DIR;
            case LOCATION:
                return WeatherContract.LocationEntry.CONTENT_DIR;
            case LOCATION_ID:
                return WeatherContract.LocationEntry.CONTENT_ITEM;
            default:
                throw new UnsupportedOperationException("Unknown Uri:" + uri);
        }
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        final SQLiteDatabase writableDB = db.getWritableDatabase();
        final int match = matcher.match(uri);
        Uri returnUri;
        long _id;
        switch (match) {
            case WEATHER:
                _id = writableDB.insert(WeatherContract.WeatherEntry.TABLE_NAME, null, values);
                if (_id > 0)
                    returnUri = WeatherContract.WeatherEntry.buildWeatherUri(_id);
                else
                    throw new android.database.SQLException("Failed to insert row into " + uri);
                break;
            case LOCATION:
                _id = writableDB.insert(WeatherContract.LocationEntry.TABLE_NAME, null, values);
                if (_id > 0)
                    returnUri = WeatherContract.LocationEntry.buildLocationUri(_id);
                else
                    throw new android.database.SQLException("Failed to insert row into " + uri);
                break;
            default:
                throw new UnsupportedOperationException("Unknown Uri:" + uri);
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return returnUri;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        final SQLiteDatabase writableDB = db.getWritableDatabase();
        final int match = matcher.match(uri);
        int rowAffected;
        switch (match) {
            case WEATHER:
                rowAffected = writableDB.delete(WeatherContract.WeatherEntry.TABLE_NAME, selection, selectionArgs);
                break;
            case LOCATION:
                rowAffected = writableDB.delete(WeatherContract.LocationEntry.TABLE_NAME, selection, selectionArgs);
                break;
            default:
                throw new UnsupportedOperationException("Unknown Uri:" + uri);
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return rowAffected;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        final SQLiteDatabase writableDB = db.getWritableDatabase();
        final int match = matcher.match(uri);
        int rowAffected;
        switch (match) {
            case WEATHER:
                rowAffected = writableDB.update(WeatherContract.WeatherEntry.TABLE_NAME, values, selection, selectionArgs);
                break;
            case LOCATION:
                rowAffected = writableDB.update(WeatherContract.LocationEntry.TABLE_NAME, values, selection, selectionArgs);
                break;
            default:
                throw new UnsupportedOperationException("Unknown Uri:" + uri);
        }
        if (rowAffected!=0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return rowAffected;
    }
    @Override
    public int bulkInsert(Uri uri, ContentValues[] values) {
        final SQLiteDatabase writableDatabase = db.getWritableDatabase();
        final int match = matcher.match(uri);
        switch (match) {
            case WEATHER:
                writableDatabase.beginTransaction();
                int returnCount = 0;
                try {
                    for (ContentValues value : values) {
                        if (writableDatabase.insert(WeatherContract.WeatherEntry.TABLE_NAME, null, value) != -1) {
                            returnCount++;
                        }
                    }
                    writableDatabase.setTransactionSuccessful();
                } finally {
                    writableDatabase.endTransaction();
                }
                getContext().getContentResolver().notifyChange(uri, null);
                return returnCount;
            default:
                return super.bulkInsert(uri, values);
        }
    }
}
