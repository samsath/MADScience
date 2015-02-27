package work.ma.cardboardtest;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.util.ArrayList;

/**
 * Created by sam on 20/02/15.
 */
public class Database extends SQLiteOpenHelper{

    public static final int DATABASE_VERSION = 1;
    public static final String DATABASE_NAME = "cardboardBeacon";

    public static final String TAG = "MAwork";

    public static final String T_BEACON = "beacon";
    public static final String T_OBJECTS = "objects";
    public static final String T_LINKS = "links";


    public static final String KEY_ID = "id";
    public static final String KEY_UUID = "uuid";
    public static final String KEY_MAJOR = "major";
    public static final String KEY_MINOR = "minor";
    public static final String KEY_PROX = "proximity";

    public static final String KEY_OBJECT = "object";
    public static final String KEY_FILE = "object";
    public static final String KEY_MESSAGE = "message";
    public static final String KEY_NAME = "name";

    public static final String KEY_BEACON = "beacon";


    public static final String CREATE_TABLE_BEACON = "CREATE TABLE " + T_BEACON +
            "(" + KEY_ID +" INTEGER PRIMARY KEY," +
            KEY_UUID + " STRING," +
            KEY_MAJOR +" INTEGER," +
            KEY_MINOR + " INTEGER, " +
            KEY_PROX + " INTEGER" +
            ")";

    public static final String CREATE_TABLE_OBJECT = "CREATE TABLE " + T_OBJECTS +
            "(" + KEY_ID +" INTEGER PRIMARY KEY," +
            KEY_OBJECT + " STRING," +
            KEY_FILE + " STRING," +
            KEY_MESSAGE + " STRING" +
            KEY_NAME + " STRING" +
            ")";

    public static final String CREATE_TABLE_LINK = "CREATE TABLE " + T_LINKS +
            "(" + KEY_ID +" INTEGER PRIMARY KEY," +
            KEY_BEACON +" INTEGER PRIMARY KEY," +
            KEY_OBJECT +" INTEGER PRIMARY KEY" +
            ")";

    public Database(Context context){
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }


    @Override
    public void onCreate(SQLiteDatabase db) {
        Log.d(TAG, "Database Create");
        db.execSQL(CREATE_TABLE_BEACON);
        db.execSQL(CREATE_TABLE_OBJECT);
        db.execSQL(CREATE_TABLE_LINK);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.d(TAG,"Database update");
        db.execSQL("DROP TABLE IF EXISTS " + T_BEACON);
        db.execSQL("DROP TABLE IF EXISTS " + T_LINKS);
        db.execSQL("DROP TABLE IF EXISTS " + T_OBJECTS);

        onCreate(db);
    }

    public void removeAll(){
        SQLiteDatabase db = this.getWritableDatabase();

        db.execSQL("DROP TABLE IF EXISTS " + T_BEACON);
        db.execSQL("DROP TABLE IF EXISTS " + T_LINKS);
        db.execSQL("DROP TABLE IF EXISTS " + T_OBJECTS);
    }

    public long createBeacon(Beacon beck){
        // To create the beacon information
        SQLiteDatabase db = this.getWritableDatabase();
        long beacon_id;

        Cursor c = db.rawQuery("SELECT * FROM "+T_BEACON+" WHERE "+KEY_UUID+" = '"+ beck.uuid+"' AND "+KEY_MAJOR+" = '"+beck.major+"' AND "+KEY_MINOR+" = '"+beck.minor+"'",null);
        if(c.moveToFirst()){
            beacon_id = c.getInt(c.getColumnIndex(KEY_ID));
        }else{
            ContentValues values = new ContentValues();
            values.put(KEY_UUID, beck.uuid);
            values.put(KEY_MAJOR, beck.major);
            values.put(KEY_MINOR, beck.minor);
            values.put(KEY_PROX, beck.proximity);

            beacon_id = db.insert(T_BEACON, null, values);
        }
        return beacon_id;
    }

    public int updateProx(Beacon beck){
        //To update the proximarty of the beacons
        SQLiteDatabase db = this.getReadableDatabase();
        int proxim;

        Cursor c = db.rawQuery("SELECT * FROM "+T_BEACON+" WHERE "+KEY_UUID+" = '"+ beck.uuid+"' AND "+KEY_MAJOR+" = '"+beck.major+"' AND "+KEY_MINOR+" = '"+beck.minor+"'",null);
        if(c.moveToFirst()){
            int beacon_id = c.getInt(c.getColumnIndex(KEY_ID));
            Cursor d = db.rawQuery("UPDATE" + T_BEACON +" SET "+KEY_PROX+" = '"+beck.proximity+" WHERE "+KEY_ID+" = '"+beacon_id+"';",null);
            proxim = beck.proximity;
        }else{
            createBeacon(beck);
            proxim = beck.proximity;
        }

        return proxim;
    }

    public ArrayList<Beacon> beaconProx(){
        ArrayList<Beacon> beacons = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor c = db.rawQuery("SELECT * FROM " + T_BEACON + " ORDER BY " + KEY_PROX +" ASC;", null);

        if(c!=null){
            if(c.moveToFirst()){
                do{
                    Beacon bec = new Beacon();
                    bec.id = c.getInt(c.getColumnIndex(KEY_ID));
                    bec.uuid = c.getString(c.getColumnIndex(KEY_UUID));
                    bec.major = c.getInt(c.getColumnIndex(KEY_MAJOR));
                    bec.minor = c.getInt(c.getColumnIndex(KEY_MINOR));
                    bec.proximity = c.getInt(c.getColumnIndex(KEY_PROX));
                    beacons.add(bec);
                }while(c.moveToNext());
            }
        }

        return beacons;
    }

    public long createModel(Models mod){
        long modelid;
        SQLiteDatabase db = this.getWritableDatabase();

        Cursor c = db.rawQuery("SELECT * FROM "+T_OBJECTS+" WHERE "+KEY_NAME+" = '"+ mod.getName()+"' AND "+KEY_MESSAGE+" = '"+mod.getMessage()+"';",null);
        if(c.moveToFirst()){
            modelid = c.getInt(c.getColumnIndex(KEY_ID));
        }else{
            ContentValues values = new ContentValues();
            values.put(KEY_OBJECT, mod.getObject());
            values.put(KEY_FILE, mod.getFile());
            values.put(KEY_MESSAGE, mod.getMessage());
            values.put(KEY_NAME, mod.getName());

            modelid = db.insert(T_OBJECTS, null, values);
        }
        return modelid;
    }

    public long createLink(long model, long beacon){
        long linkid;
        SQLiteDatabase db = this.getWritableDatabase();

        Cursor c = db.rawQuery("SELECT * FROM "+T_LINKS+" WHERE "+KEY_BEACON+" = '"+beacon+"' AND "+KEY_OBJECT+" = '"+model+"';",null);
        if(c.moveToFirst()){
            linkid = c.getInt(c.getColumnIndex(KEY_ID));
        }else{
            ContentValues values = new ContentValues();
            values.put(KEY_BEACON, beacon);
            values.put(KEY_OBJECT, model);

            linkid = db.insert(T_LINKS, null, values);
        }

        return linkid;
    }


    public long modelFromBeacon(long beacon){
        long mod;
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor c = db.rawQuery("SELECT * FROM "+T_LINKS+" WHERE "+KEY_BEACON+" = '"+beacon+"';",null);
        if(c.moveToFirst()){
            mod = c.getInt(c.getColumnIndex(KEY_ID));
        }else{
            mod = 0;
        }
        return mod;
    }



}
