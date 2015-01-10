package ee.vincent.clearsky.model;

import android.database.Cursor;

import java.util.ArrayList;
import java.util.List;

import ee.vincent.clearsky.database.Contract;

/**
 * Created by jakob on 3.01.2015.
 */
public class Route {

    private long id;
    private String name;
    private long created;
    private List<Point> points;


    public Route() {
    }

    public Route(Cursor cursor) {

        id = cursor.getLong(cursor.getColumnIndexOrThrow(Contract.Routes.COLUMN_ID));
        name = cursor.getString(cursor.getColumnIndexOrThrow(Contract.Routes.COLUMN_NAME));
        created = cursor.getLong(cursor.getColumnIndexOrThrow(Contract.Routes.COLUMN_CREATED));

        points = new ArrayList<>();
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public long getCreated() {
        return created;
    }

    public void setCreated(long created) {
        this.created = created;
    }

    public List<Point> getPoints() {
        return points;
    }

    public void setPoints(List<Point> points) {
        this.points = points;
    }

    @Override
    public String toString() {
        return "Route{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", created=" + created +
                '}';
    }
}
