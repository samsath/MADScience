package work.ma.cardboardtest;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Created by sam on 20/02/15.
 */
public class beaconAdapter extends BaseAdapter {
    
    private Context context;
    private List<Beacon> beaconList = new ArrayList<>();
    
    public beaconAdapter(Context c){
        context = c;
        beaconList.addAll(getbeacons());
    }
    
    @Override
    public int getCount() {
        return 0;
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        return null;
    }

    public List<Beacon>getbeacons(){

        return null;
    }
}
