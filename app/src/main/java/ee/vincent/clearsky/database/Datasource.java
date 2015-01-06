package ee.vincent.clearsky.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.List;

import ee.vincent.clearsky.model.Point;
import ee.vincent.clearsky.model.Route;

/**
 * Created by jakob on 3.01.2015.
 */
public class Datasource {

    private SQLiteOpenHelper dbHelper;
    private static Datasource datasource;


    public Datasource(Context context) {
        dbHelper = DatabaseHelper.getInstance(context);
    }

    public static Datasource getInstance(Context context) {

        if ( datasource == null ) {
            datasource = new Datasource(context);
        }

        return datasource;
    }


    // insert methods
    public long insertRoute(Route route) {

        SQLiteDatabase db = dbHelper.getWritableDatabase();

        ContentValues contentValues = new ContentValues();
        contentValues.put(Contract.Routes.COLUMN_NAME, route.getName());
        contentValues.put(Contract.Routes.COLUMN_CREATED, route.getCreated());
        long rowId = db.insert(Contract.Routes.TABLE_NAME, null, contentValues);

        dbHelper.close();

        return rowId;
    }

    public long insertPoint(Point point) {

        SQLiteDatabase db = dbHelper.getWritableDatabase();

        ContentValues contentValues = new ContentValues();
        contentValues.put(Contract.Points.COLUMN_ROUTE_ID, point.getRouteId());
        contentValues.put(Contract.Points.COLUMN_TIME, point.getTime());
        contentValues.put(Contract.Points.COLUMN_LATITUDE, point.getLatitude());
        contentValues.put(Contract.Points.COLUMN_LONGITUDE, point.getLongitude());
        contentValues.put(Contract.Points.COLUMN_ALTITUDE, point.getAltitude());
        contentValues.put(Contract.Points.COLUMN_SPEED, point.getSpeed());
        long rowId = db.insert(Contract.Points.TABLE_NAME, null, contentValues);

        dbHelper.close();

        return rowId;
    }


    // query methods
    public List<Route> getRoutes() {
        List<Route> routes = new ArrayList<>();

        String sql =
                "SELECT " +
                    "r.* " +
                "FROM routes AS r " +
                "ORDER BY r.created DESC" +
                "";

        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery(sql, null);

        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            routes.add(new Route(cursor));
            cursor.moveToNext();
        }
        cursor.close();

        dbHelper.close();

        return routes;
    }


}
