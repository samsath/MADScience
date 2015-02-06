package work.ma.stationfeed;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class Database extends SQLiteOpenHelper{

    private static final String TAG = "MAwork";

    // Database information
    public static final int DATABASE_VERSION = 1;
    public static final String DATABASE_NAME = "StationNear";

    //Database tables
    public static final String T_STATION = "station";
    public static final String T_LINE = "line";
    public static final String T_CONNECTION = "connection";

    //Table general
    public static final String K_ID = "id";
    public static final String K_NAME = "name";

    //Table Station
    public static final String K_LAT = "lat";
    public static final String K_LONG = "long";
    public static final String K_PREF = "prefix";

    //Table Line
    public static final String K_COLOUR = "colour";



    //Table creation
    public static final String CREATE_TABLE_STATION = "CREATE TABLE " + T_STATION +
                                                      "(" + K_ID + " INTEGER PRIMARY KEY, " +
                                                      K_NAME + " VARCHAR(255), " +
                                                      K_LAT + " VARCHAR(255), " +
                                                      K_LONG + " VARCHAR(255), " +
                                                      K_PREF + " VARCHAR(255)" +
                                                      ")";

    public static final String CREATE_TABLE_LINE = "CREATE TABLE " + T_LINE +
                                                   "(" + K_ID + " INTEGER PRIMARY KEY, " +
                                                   K_NAME + " VARCHAR(255), " +
                                                   K_COLOUR + " VARCHAR(255)" +
                                                    ")";

    public static final String CREATE_TABLE_CONNECTION = "CREATE TABLE " + T_CONNECTION +
                                                        "(" + K_ID + " INTEGER PRIMARY KEY, " +
                                                        K_NAME + " LONG, " +
                                                        K_COLOUR + " LONG" +
                                                        ")";

    public Database(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }


    @Override
    public void onCreate(SQLiteDatabase db) {
        Log.d(TAG, "Database Create");
        db.execSQL(CREATE_TABLE_CONNECTION);
        db.execSQL(CREATE_TABLE_LINE);
        db.execSQL(CREATE_TABLE_STATION);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.d(TAG, "Database Upgrade");
        db.execSQL("DROP TABLE IF EXISTS " + CREATE_TABLE_CONNECTION);
        db.execSQL("DROP TABLE IF EXISTS " + CREATE_TABLE_LINE);
        db.execSQL("DROP TABLE IF EXISTS " + CREATE_TABLE_STATION);

        onCreate(db);
    }

    //Add Data to the Database

    public long createStation(String name, String lat, String longe, String pref){
        SQLiteDatabase db = this.getWritableDatabase();
        long station_id;

        String query = "SELECT * FROM " + T_STATION + " WHERE " + K_PREF + " = '" + pref +"';";

        Cursor c = db.rawQuery(query, null);

        if (c!=null){
            c.moveToFirst();
            return c.getInt(c.getColumnIndex(K_ID));
        }else{

            ContentValues values = new ContentValues();
            values.put(K_NAME, name);
            values.put(K_LAT, lat);
            values.put(K_LONG, longe);
            values.put(K_PREF, pref);

            station_id = db.insert(T_STATION, null, values);
        }

        return station_id;
    }

    public long createLine(String name, String colour){
        SQLiteDatabase db = this.getWritableDatabase();
        long line_id;

        String query = "SELECT * FROM " + T_LINE + " WHERE " + K_NAME + " = '" + name +"';";

        Cursor c = db.rawQuery(query, null);

        if(c!=null){
            c.moveToFirst();
            return c.getInt(c.getColumnIndex(K_ID));
        }else{
            ContentValues values = new ContentValues();
            values.put(K_NAME, name);
            values.put(K_COLOUR, colour);

            line_id = db.insert(T_LINE, null, values);
        }

        return line_id;
    }

    public long createConnection(long station, long line){

        SQLiteDatabase db = this.getWritableDatabase();
        long connection_id;

        String query = "SELECT * FROM " + T_CONNECTION + " WHERE " + T_LINE + " = " + line + " AND " + T_STATION + " = " + station +";";
        Cursor c = db.rawQuery(query, null);

        if(c!=null){
            c.moveToFirst();
            return c.getInt(c.getColumnIndex(K_ID));
        }else{
            ContentValues values = new ContentValues();
            values.put(T_LINE, line);
            values.put(T_STATION, station);

            connection_id = db.insert(T_CONNECTION, null, values);
        }
        return connection_id;
    }

    public long getLine(String name){
        SQLiteDatabase db = this.getWritableDatabase();

        String query = "SELECT * FROM " + T_LINE + " WHERE " + K_NAME + " = '" + name + "';";
        Cursor c = db.rawQuery(query, null);

        if(c==null){
            return 0;
        }else{
            c.moveToFirst();
            return c.getLong(c.getColumnIndex(K_ID));
        }
    }


}
