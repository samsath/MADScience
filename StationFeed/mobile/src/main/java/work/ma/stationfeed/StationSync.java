package work.ma.stationfeed;


import android.content.Context;
import android.os.AsyncTask;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class StationSync extends AsyncTask<String, Integer, String> {

    private final static String url = "https://gist.githubusercontent.com/oobrien/8525859/raw/8960b8a45998c73b458495e7a68439d7c14610b1/tfl_stations.json";

    private Context context;
    private Database db;

    private HttpClient httpclient = new DefaultHttpClient();

    public StationSync(Context contextin){context = contextin; }


    public String readEntity(InputStream input) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(input), 8);
        StringBuilder buidler = new StringBuilder();

        String line;

        while((line =  reader.readLine()) != null){
            buidler.append(line + "\n");
        }
        return buidler.toString();
    }


    @Override
    protected void onPreExecute(){
        super.onPreExecute();
        db = new Database(context);
    }

    @Override
    protected String doInBackground(String... params) {

        try{
            HttpPost httpPost = new HttpPost(url);
            HttpResponse response = httpclient.execute(httpPost);

            HttpEntity entity = response.getEntity();

            String result = readEntity(entity.getContent());

            JSONArray array = new JSONArray(result);


            // each item will then be saved in to the database.

        } catch (ClientProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return null;
    }
}
