package ee.vincent.clearsky.database;

import android.provider.BaseColumns;

/**
 * Created by jakob on 3.01.2015.
 */
public class Contract {

    public static abstract class Routes implements BaseColumns {
        public static final String TABLE_NAME = "routes";
        public static final String COLUMN_ID = "id";
        public static final String COLUMN_NAME = "name";
        public static final String COLUMN_CREATED = "created";
    }

    public static abstract class Points implements BaseColumns {
        public static final String TABLE_NAME = "points";
        public static final String COLUMN_ID = "id";
        public static final String COLUMN_ROUTE_ID = "route_id";
        public static final String COLUMN_TIME = "time";
        public static final String COLUMN_LATITUDE = "latitude";
        public static final String COLUMN_LONGITUDE = "longitude";
        public static final String COLUMN_ALTITUDE = "altitude";
        public static final String COLUMN_SPEED = "speed";
    }

}
