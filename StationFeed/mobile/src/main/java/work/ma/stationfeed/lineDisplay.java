package work.ma.stationfeed;

import android.app.Activity;
import android.os.Bundle;
import android.widget.TextView;


public class lineDisplay extends Activity {

    private TextView mTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_display);
        mTextView = (TextView) findViewById(R.id.text);
    }
}