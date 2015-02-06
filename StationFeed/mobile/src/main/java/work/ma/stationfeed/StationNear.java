package work.ma.stationfeed;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;

import java.util.ArrayList;

public class StationNear extends Service {

    private static final String TAG = "MAwork";
    private LocationManager mLocationManager = null;
    private static final int LOCATION_INTERVAL = 1000;
    private static final float LCOATION_DISTANCE = 10f;

    public Database db;
    public ArrayList<Location> stations = new ArrayList<Location>();

    private class LocationListener implements android.location.LocationListener{

        Location mLastLocation;

        public LocationListener(String provider){
            Log.e(TAG, "LocationListener " + provider);
            mLastLocation = new Location(provider);
        }

        @Override
        public void onLocationChanged(Location location) {
            mLastLocation.set(location);
            areStationNear(location);
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {
            Log.e(TAG, "StatusChanged: " + provider);
        }

        @Override
        public void onProviderEnabled(String provider) {
            Log.e(TAG, "ProviderEnabled " + provider);
        }

        @Override
        public void onProviderDisabled(String provider) {
            Log.e(TAG, "Provider Disabled "+provider);
        }
    }

    LocationListener[] mLocationListeners = new LocationListener[]{
            new LocationListener(LocationManager.GPS_PROVIDER),
            new LocationListener(LocationManager.NETWORK_PROVIDER),
    };

    public StationNear() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startid){
        Log.e(TAG, "Service Start Command");
        super.onStartCommand(intent, flags, startid);
        return START_STICKY;
    }


    @Override
    public void onCreate(){
        Log.e(TAG, "Service Connected");
        initializeLocationManager();

        db = new Database(getApplicationContext());
        loadStations();
        try{
            mLocationManager.requestLocationUpdates(
                    LocationManager.NETWORK_PROVIDER, LOCATION_INTERVAL, LCOATION_DISTANCE, mLocationListeners[1]
            );
        }catch (SecurityException ex){
            Log.i(TAG, "Failed to request location update, ignore", ex);
        }catch(IllegalArgumentException ex){
            Log.d(TAG, "GPS provider does not exists " + ex.getMessage());
        }
    }

    @Override
    public void onDestroy(){
        Log.e(TAG, "Service Destroy");
        super.onDestroy();
        if (mLocationManager != null){
            for(int i = 0; i < mLocationListeners.length; i++){
                try{
                    mLocationManager.removeUpdates(mLocationListeners[i]);
                }catch (Exception ex){
                    Log.i(TAG, "failed to remove location listenr. ignore " + ex);
                }
            }
        }
    }

    private void initializeLocationManager(){
        Log.e(TAG, "Initial Location Manager");
        if(mLocationManager == null){
            mLocationManager = (LocationManager) getApplicationContext().getSystemService(Context.LOCATION_SERVICE);
        }
    }

    public void loadStations(){

        SQLiteDatabase dbc = db.getReadableDatabase();
        Cursor c = dbc.rawQuery("SELECT * FROM " + db.T_STATION + ";", null);

        if(c!=null){
            if(c.moveToFirst()){
                do{
                    Location loca = new Location(c.getString(c.getColumnIndex(db.K_NAME)));
                    loca.setLatitude(Double.parseDouble(c.getString(c.getColumnIndex(db.K_LAT))));
                    loca.setLongitude(Double.parseDouble(c.getString(c.getColumnIndex(db.K_LONG))));
                    stations.add(loca);
                }while(c.moveToNext());
            }
        }
    }

    public void areStationNear(Location location){
        float distance = 100000;
        String station = null;
        for(int i =0; i < stations.size(); i++){
            float d = location.distanceTo(stations.get(i));
            if(d < distance){
                distance = d;
                station = stations.get(i).getProvider();
            }
        }
        Log.d(TAG,"User is closest to, "+ station);
    }


}
