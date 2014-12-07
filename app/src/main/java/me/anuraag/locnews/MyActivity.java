package me.anuraag.locnews;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;


public class MyActivity extends Activity {
    private Location curLoc;
    private LocationManager locationManager;
    private LocationListener locationListener;
    private ListView items;
    private String lastloc="";
    private String loc,loc2;
    private ArrayList<NewsObject> list = new ArrayList<NewsObject>();
    ;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my);
        doLocation();


    }
    public void doLocation(){
        locationManager = (LocationManager)this.getSystemService(Context.LOCATION_SERVICE);
        locationListener = new LocationListener() {
            public void onLocationChanged(Location location) {
                // Called when a new location is found by the network location provider.
                displayLocation(location);
                curLoc = location;
                //doTimeCheck();
            }
            public void onStatusChanged(String provider, int status, Bundle extras) {}

            public void onProviderEnabled(String provider) {}

            public void onProviderDisabled(String provider) {}
        };
        locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, locationListener);
    }
    public void displayLocation(Location l){
        Geocoder gcd = new Geocoder(this, Locale.getDefault());
        try {
            List<Address> addresses = gcd.getFromLocation(l.getLatitude(), l.getLongitude(), 1);
            if (addresses.size() > 0)
               loc = addresses.get(0).getLocality();
                Log.i("City",loc);
                if(lastloc.equals("") || !lastloc.equals(loc)) {
                    RequestTask task = new RequestTask();
                    Log.i("tihng", loc);
                    task.execute(new String[]{"http://api.feedzilla.com/v1/categories/26/articles/search.json?q=" + loc});
                    lastloc = loc;
                }

        }catch(IOException e){

        }


    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.my, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
    public static class NewsAdapter extends ArrayAdapter<NewsObject> {
        public NewsAdapter(Context context, ArrayList<NewsObject> news) {
            super(context, 0, news);
        }

        @Override
        public View getView(int position, View rootView, ViewGroup parent) {
            // Get the data item for this position
            NewsObject notif = getItem(position);
            // Check if an existing view is being reused, otherwise inflate the view
            if (rootView == null) {
                rootView = LayoutInflater.from(getContext()).inflate(R.layout.notif_item, parent, false);
            }
            // Lookup view for data population
            TextView name = (TextView) rootView.findViewById(R.id.name);
            TextView author = (TextView) rootView.findViewById(R.id.author);
            TextView description = (TextView) rootView.findViewById(R.id.descript);


            // Populate the data into the template view using the data object
            name.setText("Title: " + notif.getName());
            author.setText("Author: " + notif.getAuthor());
            description.setText("Descrption: " + notif.getDescription());

            // Return the completed view to render on screen
            return rootView;
        }
    }
    public class RequestTask extends AsyncTask<String, String, String> {
        private TextView myView;
        private String s;
        @Override
        protected String doInBackground(String... uri) {
            HttpClient httpclient = new DefaultHttpClient();
            HttpResponse response;
            String responseString = null;
            try {
                response = httpclient.execute(new HttpGet(uri[0]));
                StatusLine statusLine = response.getStatusLine();
                if (statusLine.getStatusCode() == HttpStatus.SC_OK) {
                    ByteArrayOutputStream out = new ByteArrayOutputStream();
                    response.getEntity().writeTo(out);
                    out.close();
                    responseString = out.toString();
                } else {
                    //Closes the connection.
                    response.getEntity().getContent().close();
                    throw new IOException(statusLine.getReasonPhrase());
                }
            } catch (ClientProtocolException e) {
                //TODO Handle problems..
            } catch (IOException e) {
                //TODO Handle problems..
            }

//                Log.i("", responseString);
            return responseString;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
//            Log.i("Response",result);

            try{
                JSONObject articles = new JSONObject(result);
                 JSONArray art = articles.getJSONArray("articles");

                int l = art.length();
                for(int x = 0; x<l ; x++){
                    String name = art.getJSONObject(x).getString("title");
                    String author = art.getJSONObject(x).getString("author");
                    String description = art.getJSONObject(x).getString("summary").substring(0,100);
                    String url = art.getJSONObject(x).getString("url");
                    NewsObject obj = new NewsObject(name,description,url,author);
                    Log.i("news",obj.toString());
                    list.add(obj);
                }
                try {
                    NewsObject na = new NewsObject("Anuraag is president","this news is breaking","nfl.com","Anuraag Yachamameni");
                    list.add(na);
                    NewsAdapter n = new NewsAdapter(getApplicationContext(), list);
                    items = (ListView) findViewById(R.id.listView);
                    items.setAdapter(n);
                    items.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                        @Override
                        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                            Uri webpage = Uri.parse(list.get(position).getUrl());
                            Intent webIntent = new Intent(Intent.ACTION_VIEW, webpage);
                            startActivity(webIntent);
                        }
                    });
                }catch(NullPointerException n){
                    Log.i("Fail","Fail");
                }
//                Log.i("Articles",art.toString());
//                String author = articles.getJSONObject(0).getString("author");
//                Log.i("First author",author);
            }catch(JSONException j){
                Log.i("Issue","issue");
            }
        }
    }
}
