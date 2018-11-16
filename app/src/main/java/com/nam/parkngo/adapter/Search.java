package com.nam.parkngo.adapter;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Service;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.Settings;
import android.util.Log;
import android.widget.Toast;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;

/**
 * Created by Nam on 11/8/2014.
 */
public class Search extends Service implements LocationListener{
    //http params
    public final static int GET = 1;
    public final static int POST = 2;
    private final String APIKEY = "AIzaSyBv_FhB83QqZzX6-qMib3uC99gVaEIrkz0";  //Firefox acct
    //private final String APIKEY = "AIzaSyBL0wiH56tJTTJg0_bI9D_1JtmFj88XvCA";    //Chrome acct
    private final String API = "https://maps.googleapis.com/maps/api/place";
    public final String TEXT = "/textsearch/";
    public final String DETAIL = "/details/";
    public final String NEARBY = "/nearbysearch/";

    //GPS params
    private final long TIME_INTV = 1000*60*1;
    private final long DIST_INTV = 10;

    //json params
    private final String TAG_RESULT = "results";

    private String next;
    private ArrayList<NameValuePair> params;
    private Location currLoc;
    private Context context;
    private int unit;
    private String option;

    /*
    * receives search keyword and initializes parameters (array)
     */
    public Search(Context context)
    {
        this.context = context;
        currLoc = getLocation();
        if (!isConnected() || currLoc == null) alert();
        params = new ArrayList<NameValuePair>();
        resetParams();
    }

    /*
* returns HttpResponse (results) from server for an url
 */
    public String getResponse(String searchOption, int method)
    {
        if (!isConnected() || currLoc == null) return "";
        String response = null;
        String url = API + searchOption + "json";
        try
        {
            //http client
            DefaultHttpClient httpClient = new DefaultHttpClient();
            HttpEntity httpEntity;
            HttpResponse httpResponse = null;

            //check request method
            if (method == POST)
            {
                HttpPost httpPost = new HttpPost(url);

                //add POST param
                if (params != null) httpPost.setEntity(new UrlEncodedFormEntity(params));
                httpResponse = httpClient.execute(httpPost);
            }
            else if (method == GET)
            {
                //append params to URL
                if (params != null) url += "?" + URLEncodedUtils.format(params, "utf-8");
                Log.d("Search", "URL = " + url);
                HttpGet httpGet = new HttpGet(url);
                httpResponse = httpClient.execute(httpGet);
            }

            httpEntity = httpResponse.getEntity();
            response = EntityUtils.toString(httpEntity);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (ClientProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return response;
    }

    public ArrayList<CarPark> search(String keyword)
    {
        resetParams();
        addParam("query", "parking+" + keyword);
        addParam("types","parking");
        this.option = TEXT;
        return getResults();
    }

    /*
    * Converts search results into a list of carparks
     */
    public ArrayList<CarPark> getResults()
    {
        ArrayList<CarPark> cps = new ArrayList<CarPark>();
        String response = getResponse(option, GET);
        try
        {
            JSONObject jsonResponse = new JSONObject(response);
            JSONArray jsonArray = jsonResponse.getJSONArray(TAG_RESULT);
            for (int i = 0; i < jsonArray.length(); i++)
            {
                JSONObject info = jsonArray.getJSONObject(i);
                JSONObject location = info.getJSONObject("geometry").getJSONObject("location");
                CarPark cp = new CarPark(
                        info.getString("place_id"),
                        info.getString("name"),
                        location.getDouble("lat"),
                        location.getDouble("lng"));
                cp.getDist(currLoc, 1/(1000+unit*609.344));
                //if (i > 0 && cp.getFeatureName().equals(cps.get(i-1).getFeatureName())) continue;   //limit duplicates
                cps.add(cp);
                try {
                    cp.setAddress(info.getString("formatted_address"));
                }
                catch (JSONException e)
                {
                    //e.printStackTrace();
                    cp.setAddress(info.getString("vicinity"));
                    Log.w("Search", "getting vicinity instead of formatted_address");
                }
             }
            next = jsonResponse.getString("next_page_token");
            Log.w("Search", "next page = " + next);
        }
        catch (JSONException e)
        {
            //e.printStackTrace();
            next = null;
            Log.w("Search","Last result reached.");
            ((Activity) context).runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(context,"Last result reached.",Toast.LENGTH_LONG).show();
                }
            });
         }
        finally {
            return cps;
        }
    }

    /*
    * fetch more details for a carpark from server via HttpResponse
     */
    public void searchDetails(CarPark cp)
    {
        resetParams();
        addParam("placeid", cp.getPlaceid());
        String response = getResponse(DETAIL, GET);
        String time = "";
        try {
            JSONObject details = new JSONObject(response).getJSONObject("result");
            try {
            cp.setPhone(details.getString("formatted_phone_number")
                            + "|"
                            + details.getString("international_phone_number"));
            } catch (JSONException e) {
                //e.printStackTrace();
            }
            try {
            JSONArray timeArr = details.getJSONObject("opening_hours").getJSONArray("weekday_text");

            for (int i = 0; i < timeArr.length(); i++)
            {
                time += timeArr.getString(i) + "|";
            }
            cp.setTime(time
                    .substring(0,time.length()-1)    //remove the last "|"
                    .replace("nesday","")   //Wednesday -> Wed
                    .replace("urday","")    //Saturday -> Sat
                    .replace("rsday","")    //Thursday -> Thu
                    .replace("sday","")     //Tuesday -> Tue
                    .replace("day","")      //Monday -> Mon, Sunday -> Sun
                    );
            } catch (JSONException e) {
                //e.printStackTrace();
            }
            try {
            cp.setWebsite(details.getString("website"));
            } catch (JSONException e) {
                //e.printStackTrace();
            }
            cp.setDetailed(true);
        } catch (JSONException e) {
            //e.printStackTrace();
        }
    }

    /*
    * check whether the search results has next page
     */
    public boolean hasNext()
    {
        if (next == null) return false;
        resetParams();
        addParam("pagetoken", next);
        return true;
    }

    public Location getLocation()
    {
        Location loc = null;
        try
        {
            LocationManager lm = (LocationManager) context.getSystemService(LOCATION_SERVICE);
            if (lm.isProviderEnabled(lm.NETWORK_PROVIDER) && lm.isProviderEnabled(lm.NETWORK_PROVIDER)) {
                lm.requestLocationUpdates(lm.NETWORK_PROVIDER, TIME_INTV, DIST_INTV, this);
                loc = lm.getLastKnownLocation(lm.NETWORK_PROVIDER);
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        return loc;
    }
    /*
    * checks internet connection
     */
    public boolean isConnected()
    {
        ConnectivityManager conn = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (conn != null)
        {
            NetworkInfo[] connInfo = conn.getAllNetworkInfo();
            if (connInfo != null)
            {
                for (NetworkInfo info : connInfo)
                    if (info.getState().equals(NetworkInfo.State.CONNECTED)) return true;
            }
        }
        return false;
    }

    public void alert()
    {
        final AlertDialog.Builder dialog = new AlertDialog.Builder(context);
        dialog.setTitle("Connection Error");
        dialog.setMessage("Please enable connection and GPS service");
        dialog.setPositiveButton("Settings", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface d, int i)
            {
                Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                context.startActivity(intent);
            }
        });
        dialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface d, int i) {
                d.cancel();
            }
        });
        dialog.show();
    }

    @Override
    public void onLocationChanged(Location location) {

    }

    @Override
    public void onStatusChanged(String s, int i, Bundle bundle) {

    }

    @Override
    public void onProviderEnabled(String s) {

    }

    @Override
    public void onProviderDisabled(String s) {

    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    public ArrayList<CarPark> getListFromFile(String fname)
    {
        Log.d("Search", "Loading " + fname + "...");
        ArrayList<CarPark> cps = new ArrayList<CarPark>();
        File f = context.getFileStreamPath(fname);
        if (f.exists()) {
            try {
                InputStream is = context.openFileInput(fname);
                BufferedReader br = new BufferedReader(
                        new InputStreamReader(is));
                String line = br.readLine();
                while (line != null && !line.isEmpty()) {
                    cps.add(cpConv(line));
                    line = br.readLine();
                }
                is.close();
            } catch (IOException e) {
            }
        }
        return cps;
    }

    /*
    * Convert a string to a CarPark
     */
    public CarPark cpConv(String line)
    {
        CarPark cp = null;
        String str[] = line.split(",");
        if (str.length >= 5) {
            cp = new CarPark(
                    str[0], //place_id
                    str[1], //name
                    Double.parseDouble(str[2]), //lat
                    Double.parseDouble(str[3])); //long
            cp.setAddress(str[4]);
            cp.getDist(currLoc, 1/(1000+unit*609.344));
        }
        //Load from bookmark
        if (str.length >= 8) {
            cp.setPhone(str[5]);
            cp.setTime(str[6]);
            cp.setWebsite(str[7]);
            cp.setDetailed(true);
        }
        return cp;
    }
    /*
    * Insert parameters for URL
     */
    private void addParam(String name, String value)
    {
        params.add(new BasicNameValuePair(name, value));
    }

    /*
    * Reset parameters
     */
    private void resetParams()
    {
        params.clear();
        addParam("key", APIKEY);
    }

    public int getUnit() {
        return unit;
    }

    public void setUnit(int unit) {
        this.unit = unit;
    }

    public void saveBookmark(CarPark cp)
    {
        Log.d("FragDetails", "Saving bookmark...");
        OutputStream output = null;
        try {
            Log.d("FragDetails",cp.toLongString());
            output = context.openFileOutput("bookmark.cpl", Context.MODE_APPEND);
            output.write((cp.toLongString() + "\n").getBytes());
            output.flush();
            output.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public ArrayList<CarPark> getNearby()
    {
        resetParams();
        addParam("location",currLoc.getLatitude() + "," + currLoc.getLongitude());
        addParam("rankby","distance");
        addParam("types","parking");
        this.option = NEARBY;
        return getResults();
    }

    public void share(CarPark cp)
    {
        Intent intent = new Intent(Intent.ACTION_SEND);

        intent.setType("text/plain");
        intent.putExtra(Intent.EXTRA_SUBJECT,cp.getFeatureName());
        intent.putExtra(Intent.EXTRA_TEXT,cp.toShare());
        context.startActivity(Intent.createChooser(intent,"Choose an app:"));
    }
}
