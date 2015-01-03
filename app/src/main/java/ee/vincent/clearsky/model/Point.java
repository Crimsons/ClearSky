package ee.vincent.clearsky.model;

import android.database.Cursor;

import ee.vincent.clearsky.database.Contract;

/**
 * Created by jakob on 3.01.2015.
 */
public class Point {

    private long id;
    private long routeId;
    private long time;
    private double latitude;
    private double longitude;
    private double altitude;
    private float speed;

    public Point() {
    }

    public Point(Cursor cursor) {

        id = cursor.getLong(cursor.getColumnIndexOrThrow(Contract.Points.COLUMN_ID));
        routeId = cursor.getLong(cursor.getColumnIndexOrThrow(Contract.Points.COLUMN_ROUTE_ID));
        time = cursor.getLong(cursor.getColumnIndexOrThrow(Contract.Points.COLUMN_TIME));
        latitude = cursor.getDouble(cursor.getColumnIndexOrThrow(Contract.Points.COLUMN_LATITUDE));
        longitude = cursor.getDouble(cursor.getColumnIndexOrThrow(Contract.Points.COLUMN_LONGITUDE));
        altitude = cursor.getDouble(cursor.getColumnIndexOrThrow(Contract.Points.COLUMN_ALTITUDE));
        speed = cursor.getFloat(cursor.getColumnIndexOrThrow(Contract.Points.COLUMN_SPEED));
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getRouteId() {
        return routeId;
    }

    public void setRouteId(long routeId) {
        this.routeId = routeId;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public double getAltitude() {
        return altitude;
    }

    public void setAltitude(double altitude) {
        this.altitude = altitude;
    }

    public float getSpeed() {
        return speed;
    }

    public void setSpeed(float speed) {
        this.speed = speed;
    }

    @Override
    public String toString() {
        return "Point{" +
                "id=" + id +
                ", routeId=" + routeId +
                ", time=" + time +
                ", latitude=" + latitude +
                ", longitude=" + longitude +
                ", altitude=" + altitude +
                ", speed=" + speed +
                '}';
    }
}
