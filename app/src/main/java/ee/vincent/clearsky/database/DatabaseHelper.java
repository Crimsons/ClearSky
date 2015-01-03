package ee.vincent.clearsky.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by jakob on 3.01.2015.
 */
public class DatabaseHelper extends SQLiteOpenHelper {

    public static final int DATABASE_VERSION = 1;
    public static final String DATABASE_NAME = "clearsky.db";

    private static DatabaseHelper databaseHelper;

    public static final String SQL_CREATE_TABLE_ROUTES =
            "CREATE TABLE " + Contract.Routes.TABLE_NAME + " (" +
                    Contract.Routes.COLUMN_ID + " INTEGER PRIMARY KEY," +
                    Contract.Routes.COLUMN_NAME + " TEXT, " +
                    Contract.Routes.COLUMN_CREATED + " INTEGER " +
                    ")";

    public static final String SQL_CREATE_TABLE_POINTS =
            "CREATE TABLE " + Contract.Points.TABLE_NAME + " (" +
                    Contract.Points.COLUMN_ID + " INTEGER PRIMARY KEY," +
                    Contract.Points.COLUMN_ROUTE_ID + " INTEGER," +
                    Contract.Points.COLUMN_TIME + " INTEGER, " +
                    Contract.Points.COLUMN_LATITUDE + " INTEGER, " +
                    Contract.Points.COLUMN_LONGITUDE + " INTEGER, " +
                    Contract.Points.COLUMN_ALTITUDE + " INTEGER, " +
                    Contract.Points.COLUMN_SPEED + " REAL " +
                    ")";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

        db.execSQL(SQL_CREATE_TABLE_ROUTES);
        db.execSQL(SQL_CREATE_TABLE_POINTS);

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {}


    public static DatabaseHelper getInstance(Context context) {
        if ( databaseHelper == null )
            databaseHelper = new DatabaseHelper(context);

        return databaseHelper;
    }

}
